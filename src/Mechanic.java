public class Mechanic {
    private String name;
    private MechanicSpecialization specialization;

    public Mechanic(final String name, final MechanicSpecialization specialization) {
        if (name == null || name.isBlank())
            throw new DomainException("Mechanic name cannot be empty");
        if (specialization == null)
            throw new DomainException("Specialization cannot be null");

        this.name = name;
        this.specialization = specialization;
    }

    public final String getName() {
        return name;
    }
    public final MechanicSpecialization getSpecialization() {
        return specialization;
    }

    public final boolean canPerform(final MechanicSpecialization requiredSpec) {
        if (this.specialization == MechanicSpecialization.GENERAL) return true;
        if (requiredSpec == MechanicSpecialization.GENERAL) return true;
        return this.specialization == requiredSpec;
    }
}
