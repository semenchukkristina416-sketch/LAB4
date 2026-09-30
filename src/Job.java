import java.math.BigDecimal;

public class Job {
    private String description;
    private BigDecimal partsCost;
    private BigDecimal laborCost;
    private MechanicSpecialization requiredSpecialization;
    private JobStatus status;

    public Job(final String description, final BigDecimal partsCost, final BigDecimal laborCost, final MechanicSpecialization reqSpec) {
        if (description == null || description.isBlank())
            throw new DomainException("Description cannot be empty");
        if (partsCost == null || partsCost.compareTo(BigDecimal.ZERO) < 0)
            throw new DomainException("Invalid parts cost");
        if (laborCost == null || laborCost.compareTo(BigDecimal.ZERO) < 0)
            throw new DomainException("Invalid labor cost");
        if (reqSpec == null)
            throw new DomainException("Required specialization cannot be null");

        this.description = description;
        this.partsCost = partsCost;
        this.laborCost = laborCost;
        this.requiredSpecialization = reqSpec;
        this.status = JobStatus.PENDING;
    }

    public final String getDescription() { return description; }
    public final BigDecimal getPartsCost() { return partsCost; }
    public final BigDecimal getLaborCost() { return laborCost; }
    public final MechanicSpecialization getRequiredSpecialization() { return requiredSpecialization; }
    public final JobStatus getStatus() { return status; }

    public final void start() {
        if (status != JobStatus.PENDING) {
            throw new DomainException("Job can only be started from PENDING status");
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
