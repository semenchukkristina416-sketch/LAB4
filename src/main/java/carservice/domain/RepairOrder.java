package carservice.domain;

import carservice.exception.DomainException;
import carservice.domain.enumeration.OrderPriority;
import carservice.domain.enumeration.OrderStatus;
import jakarta.persistence.Version;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "repair_orders")
public class RepairOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    public Long getVersion() {
        return version;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;

    @OneToMany(mappedBy = "repairOrder", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Job> jobs = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private OrderPriority priority;


    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "mechanic_id")
    private Mechanic mechanic;

    @Transient
    private ServicePost servicePost;

    @Transient
    private TimeInterval plannedInterval;

    @Column(name = "planned_start", nullable = false)
    private LocalDateTime plannedStart;

    @Column(name = "planned_end", nullable = false)
    private LocalDateTime plannedEnd;

    @Column(name = "total_price")
    private BigDecimal finalCommercialPrice;

    protected RepairOrder() {
    }

    public RepairOrder(final Car car, final OrderPriority priority, final LocalDateTime plannedStart, final int plannedDurationMinutes) {
        if (car == null)
            throw new DomainException("Car cannot be null");
        if (priority == null)
            throw new DomainException("Priority cannot be null");
        if (plannedStart == null)
            throw new DomainException("Planned start time cannot be null");
        if (plannedDurationMinutes <= 0)
            throw new DomainException("Duration must be greater than zero");

        this.car = car;
        this.priority = priority;

        this.plannedInterval = new TimeInterval(plannedStart, plannedDurationMinutes);
        this.plannedStart = plannedStart;
        this.plannedEnd = this.plannedInterval.getEnd();

        this.jobs = new ArrayList<>();
        this.status = OrderStatus.CREATED;
    }

    public void addJob(Job job) {
        if (job == null)
            throw new DomainException("Job cannot be null");
        jobs.add(job);
        job.setRepairOrder(this);
    }

    public void diagnose() {
        if (status != OrderStatus.CREATED) {
            throw new DomainException("Only a created order can be sent to diagnosis");
        }
        this.status = OrderStatus.DIAGNOSIS;
    }

    public void approve() {
        if (status != OrderStatus.CREATED && status != OrderStatus.DIAGNOSIS) {
            throw new DomainException("Only a created or diagnosed order can be approved");
        }
        if (jobs.isEmpty()) {
            throw new DomainException("It is not possible to approve an order without work items");
        }
        this.status = OrderStatus.APPROVED;
    }

    public void assignMechanic(Mechanic mechanic) {
        if (mechanic == null)
            throw new DomainException("Mechanic cannot be null");
        this.mechanic = mechanic;
    }

    public void assignServicePost(ServicePost post) {
        if (post == null)
            throw new DomainException("Service post cannot be null");
        this.servicePost = post;
    }

    public void startExecution() {
        if (status != OrderStatus.APPROVED) {
            throw new DomainException("Only an approved order can be started");
        }
        if (mechanic == null) {
            throw new DomainException("Cannot start work: no mechanic assigned.");
        }
        this.status = OrderStatus.IN_PROGRESS;
    }

    public void completeOrder() {
        if (status != OrderStatus.IN_PROGRESS) {
            throw new DomainException("Only an order in progress can be completed");
        }
        this.status = OrderStatus.COMPLETED;
    }

    public void cancel() {
        if (status == OrderStatus.COMPLETED || status == OrderStatus.CANCELLED) {
            throw new DomainException("Completed or cancelled orders cannot be cancelled");
        }
        this.status = OrderStatus.CANCELLED;
    }

    public BigDecimal calculateBaseTotal() {
        return jobs.stream()
                .map(job -> job.getLaborCost().add(job.getPartsCost()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void setFinalCommercialPrice(BigDecimal price) {
        this.finalCommercialPrice = price;
    }

    public Long getId() {
        return id;
    }

    public Car getCar() {
        return car;
    }
    public List<Job> getJobs() {
        return jobs;
    }
    public OrderPriority getPriority() {
        return priority;
    }
    public OrderStatus getStatus() {
        return status;
    }
    public Mechanic getMechanic() {
        return mechanic;
    }
    public ServicePost getServicePost() {
        return servicePost;
    }
    public TimeInterval getPlannedInterval() {
        return plannedInterval;
    }
    public LocalDateTime getPlannedStart() {
        return plannedStart;
    }
    public LocalDateTime getPlannedEnd() {
        return plannedEnd;
    }
    public BigDecimal getFinalCommercialPrice() {
        return finalCommercialPrice;
    }
}
