package carservice.integration;

import carservice.domain.Car;
import carservice.domain.Client;
import carservice.exception.DomainException;
import carservice.domain.Job;
import carservice.domain.Mechanic;
import carservice.domain.enumeration.MechanicSpecialization;
import carservice.domain.enumeration.OrderPriority;
import carservice.domain.enumeration.OrderStatus;
import carservice.domain.RepairOrder;
import carservice.pricing.PriceCalculator;
import carservice.repository.CarRepository;
import carservice.repository.ClientRepository;
import carservice.repository.MechanicRepository;
import carservice.repository.RepairOrderRepository;
import carservice.service.RepairOrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

class RepairOrderIT extends IntegrationTest {

    @Autowired
    private RepairOrderService repairOrderService;

    @Autowired
    private RepairOrderRepository repairOrderRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private MechanicRepository mechanicRepository;

    @MockitoSpyBean
    private PriceCalculator priceCalculator;

    private Car savedCar;

    @BeforeEach
    void setUp() {
        repairOrderRepository.deleteAll();
        carRepository.deleteAll();
        clientRepository.deleteAll();
        mechanicRepository.deleteAll();

        Client client = clientRepository.save(new Client("Olena Koval", "+380971112233"));
        savedCar = carRepository.save(new Car("BC1234AA", "Toyota", "Camry", client));
    }

    @Test
    @DisplayName("Approves and persists a new repair order with jobs")
    void approvesAndPersistsNewOrder() {

        RepairOrder order = buildOrderWithDefaultJobs(OrderPriority.NORMAL, LocalDateTime.of(2026, 11, 15, 9, 0));

        repairOrderService.processNewOrder(order);

        RepairOrder persistedOrder = repairOrderService.getOrderById(order.getId());
        assertNotNull(persistedOrder.getId());
        assertEquals(OrderStatus.APPROVED, persistedOrder.getStatus());
        assertEquals(2, persistedOrder.getJobs().size());
    }

    @Test
    @DisplayName("Assigns mechanic and transitions approved order to IN_PROGRESS")
    void startsOrderExecutionWithAssignedMechanic() {

        Long orderId = createApprovedOrder(OrderPriority.NORMAL);
        Mechanic mechanic = new Mechanic("Ivan Petrenko", MechanicSpecialization.GENERAL);

        repairOrderService.startOrderExecution(orderId, mechanic);

        RepairOrder inProgressOrder = repairOrderService.getOrderById(orderId);
        assertEquals(OrderStatus.IN_PROGRESS, inProgressOrder.getStatus());
        assertEquals("Ivan Petrenko", inProgressOrder.getMechanic().getName());
    }

    @Test
    @DisplayName("Completes the full repair order lifecycle and calculates final commercial price")
    void completesFullOrderLifecycleAndCalculatesCommercialPrice() {
        Long orderId = createInProgressOrder(OrderPriority.NORMAL, "Ivan Petrenko");

        repairOrderService.completeOrder(orderId);

        RepairOrder completedOrder = repairOrderService.getOrderById(orderId);
        assertEquals(OrderStatus.COMPLETED, completedOrder.getStatus());
        assertNotNull(completedOrder.getFinalCommercialPrice());
        assertEquals(0, new BigDecimal("990.00").compareTo(completedOrder.getFinalCommercialPrice()));
    }

    @Test
    @DisplayName("Rejects order approval when no jobs are attached")
    void rejectsApprovalForOrderWithoutJobs() {
        RepairOrder emptyOrder = new RepairOrder(
                savedCar,
                OrderPriority.NORMAL,
                LocalDateTime.of(2026, 11, 15, 11, 0),
                60
        );

        assertThrows(DomainException.class, () -> repairOrderService.processNewOrder(emptyOrder));
        assertEquals(0, repairOrderRepository.count());
    }

    @Test
    @DisplayName("Rolls back transaction when batch job insertion fails on a null element")
    void rollsBackTransactionWhenBatchJobInsertFails() {
        Long orderId = createApprovedOrderWithSingleJob("Wheel Balancing", "100.00", "300.00");
        Job validJob = new Job(
                "Wheel Alignment",
                "3D wheel alignment check",
                new BigDecimal("150.00"),
                new BigDecimal("650.00"),
                MechanicSpecialization.GENERAL
        );
        List<Job> invalidBatch = Arrays.asList(validJob, null);

        assertThrows(DomainException.class, () ->
                repairOrderService.addBatchJobsToOrder(orderId, invalidBatch)
        );
        RepairOrder persistedOrder = repairOrderService.getOrderById(orderId);
        assertEquals(1, persistedOrder.getJobs().size());
        assertEquals("Wheel Balancing", persistedOrder.getJobs().get(0).getName());
    }

    @Test
    @DisplayName("Rolls back order completion when price calculation throws an exception")
    void rollsBackCompletionWhenPriceCalculationFails() {
        Long orderId = createInProgressOrder(OrderPriority.URGENT, "Stepan Boyko");
        doThrow(new DomainException("Forced pricing calculation failure"))
                .when(priceCalculator)
                .calculateCommercialPrice(any(RepairOrder.class));

        assertThrows(DomainException.class, () -> repairOrderService.completeOrder(orderId));
        RepairOrder persistedOrder = repairOrderService.getOrderById(orderId);
        assertEquals(OrderStatus.IN_PROGRESS, persistedOrder.getStatus());
        assertNull(persistedOrder.getFinalCommercialPrice());
    }

    private RepairOrder buildOrderWithDefaultJobs(OrderPriority priority, LocalDateTime startTime) {
        RepairOrder order = new RepairOrder(savedCar, priority, startTime, 120);
        order.addJob(new Job(
                "Diagnostics",
                "Computer engine diagnostics",
                new BigDecimal("200.00"),
                new BigDecimal("300.00"),
                MechanicSpecialization.ENGINE
        ));
        order.addJob(new Job(
                "Oil Change",
                "Engine oil and filter replacement",
                new BigDecimal("400.00"),
                new BigDecimal("200.00"),
                MechanicSpecialization.GENERAL
        ));
        return order;
    }

    private Long createApprovedOrder(OrderPriority priority) {
        RepairOrder order = buildOrderWithDefaultJobs(priority, LocalDateTime.of(2026, 11, 15, 10, 0));
        repairOrderService.processNewOrder(order);
        return order.getId();
    }

    private Long createInProgressOrder(OrderPriority priority, String mechanicName) {
        Long orderId = createApprovedOrder(priority);
        Mechanic mechanic = new Mechanic(mechanicName, MechanicSpecialization.GENERAL);
        repairOrderService.startOrderExecution(orderId, mechanic);
        return orderId;
    }

    private Long createApprovedOrderWithSingleJob(String jobName, String partsCost, String laborCost) {
        RepairOrder order = new RepairOrder(
                savedCar,
                OrderPriority.NORMAL,
                LocalDateTime.of(2026, 11, 15, 12, 0),
                90
        );
        order.addJob(new Job(
                jobName,
                "Initial work item",
                new BigDecimal(partsCost),
                new BigDecimal(laborCost),
                MechanicSpecialization.GENERAL
        ));
        repairOrderService.processNewOrder(order);
        return order.getId();
    }
}
