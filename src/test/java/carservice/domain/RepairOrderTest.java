package carservice.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class RepairOrderTest {

    private Car car;
    private Mechanic mechanic;
    private Job job;

    @BeforeEach
    void setUp() {
        Client client = new Client("Ivan", "0991234567");
        car = new Car("BC1234AC", "Toyota", "Camry", client);
        mechanic = new Mechanic("Oleh", MechanicSpecialization.ENGINE);
        job = new Job("Test Repair", "Engine repair", new BigDecimal("500"), new BigDecimal("300"), MechanicSpecialization.ENGINE);
    }

    @Test
    void shouldCreateOrderSuccessfully() {
        LocalDateTime startTime = LocalDateTime.now();
        RepairOrder order = new RepairOrder(car, OrderPriority.NORMAL, startTime, 120);

        assertEquals(car, order.getCar());
        assertEquals(OrderPriority.NORMAL, order.getPriority());
        assertEquals(OrderStatus.CREATED, order.getStatus());
        assertNotNull(order.getPlannedInterval());
        assertTrue(order.getJobs().isEmpty());
        assertNull(order.getId());
    }

    @Test
    void shouldThrowExceptionWhenCarIsNull() {
        assertThrows(DomainException.class, () -> new RepairOrder(null, OrderPriority.NORMAL, LocalDateTime.now(), 120));
    }

    @Test
    void shouldThrowExceptionWhenPriorityIsNull() {
        assertThrows(DomainException.class, () -> new RepairOrder(car, null, LocalDateTime.now(), 120));
    }

    @Test
    void shouldThrowExceptionWhenStartTimeIsNull() {
        assertThrows(DomainException.class, () -> new RepairOrder(car, OrderPriority.NORMAL, null, 120));
    }

    @Test
    void shouldThrowExceptionWhenDurationIsZero() {
        assertThrows(DomainException.class, () -> new RepairOrder(car, OrderPriority.NORMAL, LocalDateTime.now(), 0));
    }

    @Test
    void shouldThrowExceptionWhenDurationIsNegative() {
        assertThrows(DomainException.class, () -> new RepairOrder(car, OrderPriority.NORMAL, LocalDateTime.now(), -30));
    }

    @Test
    void shouldAddJobSuccessfully() {
        RepairOrder order = new RepairOrder(car, OrderPriority.NORMAL, LocalDateTime.now(), 120);
        order.addJob(job);

        assertEquals(1, order.getJobs().size());
        assertEquals(job, order.getJobs().get(0));
        assertEquals(order, job.getRepairOrder());
    }

    @Test
    void shouldThrowExceptionWhenAddingNullJob() {
        RepairOrder order = new RepairOrder(car, OrderPriority.NORMAL, LocalDateTime.now(), 120);
        assertThrows(DomainException.class, () -> order.addJob(null));
    }

    @Test
    void shouldChangeStatusToDiagnosis() {
        RepairOrder order = new RepairOrder(car, OrderPriority.NORMAL, LocalDateTime.now(), 120);
        order.diagnose();

        assertEquals(OrderStatus.DIAGNOSIS, order.getStatus());
    }

    @Test
    void shouldChangeStatusToApproved() {
        RepairOrder order = new RepairOrder(car, OrderPriority.NORMAL, LocalDateTime.now(), 120);
        order.addJob(job);
        order.approve();

        assertEquals(OrderStatus.APPROVED, order.getStatus());
    }

    @Test
    void shouldThrowExceptionWhenApprovingOrderWithoutJobs() {
        RepairOrder order = new RepairOrder(car, OrderPriority.NORMAL, LocalDateTime.now(), 120);
        assertThrows(DomainException.class, order::approve);
    }

    @Test
    void shouldAssignMechanicSuccessfully() {
        RepairOrder order = new RepairOrder(car, OrderPriority.NORMAL, LocalDateTime.now(), 120);
        order.assignMechanic(mechanic);

        assertEquals(mechanic, order.getMechanic());
    }

    @Test
    void shouldThrowExceptionWhenAssigningNullMechanic() {
        RepairOrder order = new RepairOrder(car, OrderPriority.NORMAL, LocalDateTime.now(), 120);
        assertThrows(DomainException.class, () -> order.assignMechanic(null));
    }

    @Test
    void shouldAssignServicePostSuccessfully() {
        RepairOrder order = new RepairOrder(car, OrderPriority.NORMAL, LocalDateTime.now(), 120);
        ServicePost post = new ServicePost("1", "Box 1");
        order.assignServicePost(post);

        assertEquals(post, order.getServicePost());
    }

    @Test
    void shouldThrowExceptionWhenAssigningNullServicePost() {
        RepairOrder order = new RepairOrder(car, OrderPriority.NORMAL, LocalDateTime.now(), 120);
        assertThrows(DomainException.class, () -> order.assignServicePost(null));
    }

    @Test
    void shouldStartExecutionSuccessfully() {
        RepairOrder order = new RepairOrder(car, OrderPriority.NORMAL, LocalDateTime.now(), 120);
        order.addJob(job);
        order.approve();
        order.assignMechanic(mechanic);
        order.startExecution();

        assertEquals(OrderStatus.IN_PROGRESS, order.getStatus());
    }

    @Test
    void shouldThrowExceptionWhenStartingExecutionWithoutMechanic() {
        RepairOrder order = new RepairOrder(car, OrderPriority.NORMAL, LocalDateTime.now(), 120);
        order.addJob(job);
        order.approve();

        assertThrows(DomainException.class, order::startExecution);
    }

    @Test
    void shouldCompleteOrderSuccessfully() {
        RepairOrder order = new RepairOrder(car, OrderPriority.NORMAL, LocalDateTime.now(), 120);
        order.completeOrder();

        assertEquals(OrderStatus.COMPLETED, order.getStatus());
    }

    @Test
    void shouldCalculateBaseTotalCorrectlyWithMultipleJobs() {
        RepairOrder order = new RepairOrder(car, OrderPriority.NORMAL, LocalDateTime.now(), 120);
        Job job1 = new Job("Test Repair 1", "Job 1", new BigDecimal("100"), new BigDecimal("50"), MechanicSpecialization.ENGINE);
        Job job2 = new Job("Test Repair 2", "Job 2", new BigDecimal("200"), new BigDecimal("100"), MechanicSpecialization.ENGINE);

        order.addJob(job1);
        order.addJob(job2);

        BigDecimal expectedTotal = new BigDecimal("450");
        assertEquals(0, expectedTotal.compareTo(order.calculateBaseTotal()));
    }

    @Test
    void shouldReturnZeroBaseTotalWhenNoJobsAssigned() {
        RepairOrder order = new RepairOrder(car, OrderPriority.NORMAL, LocalDateTime.now(), 120);
        assertEquals(0, BigDecimal.ZERO.compareTo(order.calculateBaseTotal()));
    }

    @Test
    void shouldSetFinalCommercialPriceSuccessfully() {
        RepairOrder order = new RepairOrder(car, OrderPriority.NORMAL, LocalDateTime.now(), 120);
        BigDecimal finalPrice = new BigDecimal("999.99");
        order.setFinalCommercialPrice(finalPrice);

        assertEquals(finalPrice, order.getFinalCommercialPrice());
    }
}