import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Order {
    private String orderId;
    private Car car;
    private List<Job> jobs;
    private OrderPriority priority;
    private OrderStatus status;
    private Mechanic mechanic;
    private ServicePost servicePost;
    private TimeInterval plannedInterval;

    Order(final String orderId, final Car car, final OrderPriority priority, final LocalDateTime plannedStart, final int plannedDurationMinutes) {
        if (orderId == null || orderId.isBlank()) throw new DomainException("Order ID cannot be empty");
        if (car == null) throw new DomainException("Car cannot be null");
        if (priority == null) throw new DomainException("Priority cannot be null");
        if (plannedStart == null) throw new DomainException("Time cannot be null");
        if (plannedDurationMinutes < 0) throw new DomainException("Duration cannot be negative");

        this.orderId = orderId;
        this.car = car;
        this.priority = priority;
        this.jobs = new ArrayList<>();
        this.status = OrderStatus.CREATED;
        this.plannedInterval = new TimeInterval(plannedStart, plannedDurationMinutes);
    }

    public final String getOrderId() { return orderId; }
    public final Car getCar() { return car; }
    public final OrderStatus getStatus() { return status; }
    public final OrderPriority getPriority() { return priority; }
    public final Mechanic getMechanic() { return mechanic; }
    public final ServicePost getServicePost() { return servicePost; }
    public final TimeInterval getPlannedInterval() { return plannedInterval; }
    public final List<Job> getJobs() { return Collections.unmodifiableList(jobs); }

    public final void addJob(final Job job) {
        if (job == null) throw new DomainException("Job cannot be null");
        assertModifiable();
        jobs.add(job);
    }

    public final void diagnose() {
        requireStatus(OrderStatus.CREATED);
        status = OrderStatus.DIAGNOSED;
    }

    public final void approve() {
        requireStatus(OrderStatus.DIAGNOSED);
        boolean hasJobs = !jobs.isEmpty();
        if (!hasJobs) {
            throw new DomainException("Cannot approve order without jobs");
        }
        status = OrderStatus.APPROVED;
    }

    final void assignMechanic(final Mechanic mechanic) {
        requireStatus(OrderStatus.APPROVED);
        if (mechanic == null) throw new DomainException("Mechanic cannot be null");

        for (Job job : jobs) {
            MechanicSpecialization requiredSpec = job.getRequiredSpecialization();
            boolean canPerform = mechanic.canPerform(requiredSpec);
            if (!canPerform) {
                String mechanicName = mechanic.getName();
                throw new DomainException("Mechanic " + mechanicName + " lacks required specialization");
            }
        }
        this.mechanic = mechanic;
    }

    final void assignServicePost(final ServicePost servicePost) {
        requireStatus(OrderStatus.APPROVED);
        if (servicePost == null) throw new DomainException("Service post cannot be null");
        this.servicePost = servicePost;
    }

    public final void startExecution() {
        requireStatus(OrderStatus.APPROVED);
        if (mechanic == null) throw new DomainException("Mechanic is not assigned");
        if (servicePost == null) throw new DomainException("Service post is not assigned");

        for (Job job : jobs) {
            job.start();
        }
        status = OrderStatus.IN_PROGRESS;
    }

    public final void completeJob(final Job job) {
        requireStatus(OrderStatus.IN_PROGRESS);
        if (job == null) throw new DomainException("Job cannot be null");

        boolean containsJob = jobs.contains(job);
        if (!containsJob) {
            throw new DomainException("This job does not belong to the order");
        }
        job.complete();
    }

    public final void completeOrder() {
        requireStatus(OrderStatus.IN_PROGRESS);
        for (Job job : jobs) {
            JobStatus jobStatus = job.getStatus();
            if (jobStatus != JobStatus.COMPLETED) {
                throw new DomainException("Not all jobs are completed");
            }
        }
        status = OrderStatus.COMPLETED;
    }

    public final void cancel() {
        if (status != OrderStatus.CREATED && status != OrderStatus.DIAGNOSED && status != OrderStatus.APPROVED) {
            throw new DomainException("Order cannot be cancelled in status " + status);
        }
        status = OrderStatus.CANCELLED;
    }

    public final BigDecimal calculateTotal() {
        BigDecimal partsTotal = BigDecimal.ZERO;
        BigDecimal laborTotal = BigDecimal.ZERO;

        for (Job job : jobs) {
            BigDecimal partsCost = job.getPartsCost();
            BigDecimal laborCost = job.getLaborCost();
            partsTotal = partsTotal.add(partsCost);
            laborTotal = laborTotal.add(laborCost);
        }

        if (priority == OrderPriority.URGENT) {
            BigDecimal multiplier = new BigDecimal("1.20");
            laborTotal = laborTotal.multiply(multiplier);
        }

        return partsTotal.add(laborTotal);
    }

    private void requireStatus(final OrderStatus expected) {
        if (status != expected) {
            throw new DomainException("Invalid transition. Expected: " + expected);
        }
    }

    private void assertModifiable() {
        if (status == OrderStatus.COMPLETED || status == OrderStatus.CANCELLED || status == OrderStatus.IN_PROGRESS) {
            throw new DomainException("Order cannot be modified");
        }
        if (status == OrderStatus.APPROVED) {
            throw new DomainException("Cannot add jobs to an approved order");
        }
    }
}
