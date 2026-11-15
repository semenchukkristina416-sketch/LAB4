package carservice.domain;

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
        this.status = OrderStatus.DIAGNOSIS;
    }

    public void approve() {
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
        if (mechanic == null) {
            throw new DomainException("Cannot start work: no mechanic assigned.");
        }
        this.status = OrderStatus.IN_PROGRESS;
    }

    public void completeOrder() {
        this.status = OrderStatus.COMPLETED;
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
    public BigDecimal getFinalCommercialPrice() {
        return finalCommercialPrice;
    }
}