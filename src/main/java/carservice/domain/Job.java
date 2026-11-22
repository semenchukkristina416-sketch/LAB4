package carservice.domain;

import carservice.exception.DomainException;
import carservice.domain.enumeration.JobStatus;
import carservice.domain.enumeration.MechanicSpecialization;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;
import java.math.BigDecimal;

@Entity
@Table(name = "jobs")
@Getter
@ToString(exclude = "repairOrder")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Embedded
    private JobCost cost;

    @Enumerated(EnumType.STRING)
    @Column(name = "required_specialization")
    private MechanicSpecialization requiredSpecialization;

    @Enumerated(EnumType.STRING)
    private JobStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repair_order_id")
    private RepairOrder repairOrder;

    protected Job() {
    }

    public Job(final String name, final String description, final BigDecimal partsCost, final BigDecimal laborCost, final MechanicSpecialization reqSpec) {
        if (name == null || name.isBlank())
            throw new DomainException("Name cannot be empty");
        if (description == null || description.isBlank())
            throw new DomainException("Description cannot be empty");
        if (reqSpec == null)
            throw new DomainException("Required specialization cannot be null");

        this.name = name;
        this.description = description;
        this.cost = new JobCost(partsCost, laborCost);
        this.requiredSpecialization = reqSpec;
        this.status = JobStatus.PENDING;
    }

    public BigDecimal getPartsCost() {
        return cost != null ? cost.getPartsCost() : null;
    }

    public BigDecimal getLaborCost() {
        return cost != null ? cost.getLaborCost() : null;
    }

    public BigDecimal getBasePrice() {
        return cost != null ? cost.getBasePrice() : null;
    }

    public BigDecimal getCommercialPrice() { return cost != null ? cost.getCommercialPrice() : null;}

    public void setRepairOrder(RepairOrder repairOrder) {
        this.repairOrder = repairOrder;
    }

    public final void start() {
        if (status != JobStatus.PENDING) {
            throw new DomainException("domain.Job can only be started from PENDING status");
        }
        status = JobStatus.IN_PROGRESS;
    }

    public final void complete() {
        if (status != JobStatus.IN_PROGRESS) {
            throw new DomainException("Only a job in progress can be completed");
        }
        status = JobStatus.COMPLETED;
    }
}
