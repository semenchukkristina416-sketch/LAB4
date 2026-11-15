package carservice.domain.service.integration;

import carservice.domain.*;
import carservice.domain.repository.CarRepository;
import carservice.domain.repository.ClientRepository;
import carservice.domain.repository.MechanicRepository;
import carservice.domain.repository.RepairOrderRepository;
import carservice.domain.service.RepairOrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class RepairOrderServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private RepairOrderService repairOrderService;

    @Autowired
    private RepairOrderRepository repairOrderRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired(required = false)
    private MechanicRepository mechanicRepository;

    private Car savedCar;

    @BeforeEach
    void setUp() {
        repairOrderRepository.deleteAll();
        carRepository.deleteAll();
        clientRepository.deleteAll();
        if (mechanicRepository != null) {
            mechanicRepository.deleteAll();
        }

        Client client = new Client("Ivan Petrenko", "0991234567");
        clientRepository.save(client);

        Car car = new Car("BC1234AA", "Toyota", "Camry", client);
        savedCar = carRepository.save(car);
    }

    @Test
    @DisplayName("1. Happy Path: Create and approve order with persisted jobs in DB")
    void shouldApproveAndPersistNewOrderWithJobs() {
        RepairOrder order = new RepairOrder(savedCar, OrderPriority.NORMAL, LocalDateTime.now(), 90);
        order.addJob(new Job(
                "Diagnostics",
                "Computer diagnostics",
                new BigDecimal("200.00"),
                new BigDecimal("0.00"),
                MechanicSpecialization.GENERAL
        ));
        order.addJob(new Job(
                "Brake Service",
                "Brake pads replacement",
                new BigDecimal("400.00"),
                new BigDecimal("800.00"),
                MechanicSpecialization.GENERAL
        ));

        repairOrderService.processNewOrder(order);

        assertNotNull(order.getId());
        RepairOrder persistedOrder = repairOrderRepository.findById(order.getId()).orElseThrow();
        assertEquals(OrderStatus.APPROVED, persistedOrder.getStatus());
        assertEquals(2, persistedOrder.getJobs().size());
        assertEquals(new BigDecimal("1400.00"), persistedOrder.calculateBaseTotal());
    }

    @Test
    @DisplayName("2. Happy Path: Full order lifecycle (APPROVED -> IN_PROGRESS -> COMPLETED)")
    void shouldExecuteFullOrderLifecycleAndCalculateCommercialPrice() {
        RepairOrder order = new RepairOrder(savedCar, OrderPriority.NORMAL, LocalDateTime.now(), 120);
        order.addJob(new Job(
                "Engine Repair",
                "Timing belt replacement",
                new BigDecimal("500.00"),
                new BigDecimal("600.00"),
                MechanicSpecialization.ENGINE
        ));

        repairOrderService.processNewOrder(order);
        Long orderId = order.getId();

        Mechanic mechanic = new Mechanic("Oleh", MechanicSpecialization.ENGINE);
        repairOrderService.startOrderExecution(orderId, mechanic);

        RepairOrder inProgressOrder = repairOrderRepository.findById(orderId).orElseThrow();
        assertEquals(OrderStatus.IN_PROGRESS, inProgressOrder.getStatus());
        assertNotNull(inProgressOrder.getMechanic());
        assertEquals("Oleh", inProgressOrder.getMechanic().getName());

        repairOrderService.completeOrder(orderId);

        RepairOrder completedOrder = repairOrderRepository.findById(orderId).orElseThrow();
        assertEquals(OrderStatus.COMPLETED, completedOrder.getStatus());
        assertNotNull(completedOrder.getFinalCommercialPrice());
        assertEquals(new BigDecimal("1100.00"), completedOrder.calculateBaseTotal());
        assertTrue(completedOrder.getFinalCommercialPrice().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("3. Find Order: Retrieve existing order and throw exception for non-existent ID")
    void shouldGetOrderByIdOrThrowWhenNotFound() {
        RepairOrder order = new RepairOrder(savedCar, OrderPriority.NORMAL, LocalDateTime.now(), 60);
        order.addJob(new Job(
                "Oil Change",
                "Synthetic oil and filter",
                new BigDecimal("250.00"),
                new BigDecimal("950.00"),
                MechanicSpecialization.GENERAL
        ));
        repairOrderService.processNewOrder(order);

        RepairOrder found = repairOrderService.getOrderById(order.getId());
        assertEquals(order.getId(), found.getId());
        assertEquals(savedCar.getLicensePlate(), found.getCar().getLicensePlate());

        Long nonExistentId = 999999L;
        DomainException exception = assertThrows(DomainException.class, () ->
                repairOrderService.getOrderById(nonExistentId)
        );
        assertTrue(exception.getMessage().contains(String.valueOf(nonExistentId)));
    }

    @Test
    @DisplayName("4. Edge Case: Reject approval of an empty order without jobs")
    void shouldRejectEmptyOrderWithoutJobs() {
        RepairOrder emptyOrder = new RepairOrder(savedCar, OrderPriority.URGENT, LocalDateTime.now(), 30);

        assertThrows(DomainException.class, () ->
                repairOrderService.processNewOrder(emptyOrder)
        );

        assertEquals(0, repairOrderRepository.count(),
                "Empty order must not be persisted in the database");
    }

    @Test
    @DisplayName("5. Edge Case: Reject starting execution without an assigned mechanic")
    void shouldRejectStartingExecutionWithoutMechanic() {
        RepairOrder order = new RepairOrder(savedCar, OrderPriority.NORMAL, LocalDateTime.now(), 60);
        order.addJob(new Job(
                "Inspection",
                "General checkup",
                new BigDecimal("150.00"),
                new BigDecimal("0.00"),
                MechanicSpecialization.GENERAL
        ));
        repairOrderService.processNewOrder(order);
        Long orderId = order.getId();

        assertThrows(DomainException.class, () ->
                repairOrderService.startOrderExecution(orderId, null)
        );

        RepairOrder unchangedOrder = repairOrderRepository.findById(orderId).orElseThrow();
        assertEquals(OrderStatus.APPROVED, unchangedOrder.getStatus(),
                "Order status in DB must remain APPROVED");
    }

    @Test
    @DisplayName("6. Transactional Batch: Successfully add a batch of valid jobs")
    void shouldCommitAllJobsWhenBatchIsValid() {
        RepairOrder order = new RepairOrder(savedCar, OrderPriority.NORMAL, LocalDateTime.now(), 60);
        order.addJob(new Job(
                "Diagnostics",
                "Initial Inspection",
                new BigDecimal("100.00"),
                new BigDecimal("50.00"),
                MechanicSpecialization.GENERAL
        ));
        repairOrderService.processNewOrder(order);
        Long orderId = order.getId();

        List<Job> validBatch = Arrays.asList(
                new Job("Maintenance 1", "Oil filter", new BigDecimal("100.00"), new BigDecimal("200.00"), MechanicSpecialization.GENERAL),
                new Job("Maintenance 2", "Air filter", new BigDecimal("80.00"), new BigDecimal("150.00"), MechanicSpecialization.GENERAL)
        );

        repairOrderService.addBatchJobsToOrder(orderId, validBatch);

        RepairOrder updatedOrder = repairOrderRepository.findById(orderId).orElseThrow();
        assertEquals(3, updatedOrder.getJobs().size(),
                "Order must contain both the initial job and all jobs from the batch");
    }

    @Test
    @DisplayName("7. Transactional Rollback: Full transaction rollback on error in job batch")
    void shouldRollbackBatchInsertWhenOneJobIsNull() {
        RepairOrder order = new RepairOrder(savedCar, OrderPriority.NORMAL, LocalDateTime.now(), 60);
        order.addJob(new Job(
                "Diagnostics",
                "Initial Inspection",
                new BigDecimal("100.00"),
                new BigDecimal("50.00"),
                MechanicSpecialization.GENERAL
        ));
        repairOrderService.processNewOrder(order);
        Long orderId = order.getId();

        Job validJob = new Job(
                "Maintenance",
                "Oil change",
                new BigDecimal("300.00"),
                new BigDecimal("100.00"),
                MechanicSpecialization.GENERAL
        );
        List<Job> brokenBatch = Arrays.asList(validJob, null);

        assertThrows(DomainException.class, () ->
                repairOrderService.addBatchJobsToOrder(orderId, brokenBatch)
        );

        RepairOrder orderAfterRollback = repairOrderRepository.findById(orderId).orElseThrow();
        assertEquals(1, orderAfterRollback.getJobs().size(),
                "Transaction rolled back: no new jobs from the broken batch were persisted in DB");
    }

    @Test
    @DisplayName("8. Optimistic Locking (@Version): Detect concurrent update conflict")
    void shouldDetectOptimisticLockingConflictOnConcurrentModification() {
        RepairOrder order = new RepairOrder(savedCar, OrderPriority.NORMAL, LocalDateTime.now(), 60);
        repairOrderRepository.save(order);
        Long orderId = order.getId();
        assertEquals(0L, order.getVersion());

        RepairOrder manager1Order = repairOrderRepository.findById(orderId).orElseThrow();
        RepairOrder manager2Order = repairOrderRepository.findById(orderId).orElseThrow();

        manager1Order.diagnose();
        repairOrderRepository.save(manager1Order);

        RepairOrder updatedInDb = repairOrderRepository.findById(orderId).orElseThrow();
        assertEquals(1L, updatedInDb.getVersion());

        manager2Order.completeOrder();
        assertThrows(ObjectOptimisticLockingFailureException.class, () ->
                repairOrderRepository.save(manager2Order)
        );
    }
}