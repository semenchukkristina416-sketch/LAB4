package carservice.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;

@Entity
@Table(name = "mechanics")
@Getter
@ToString
public class Mechanic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MechanicSpecialization specialization;

    protected Mechanic() {
    }

    public Mechanic(final String name, final MechanicSpecialization specialization) {
        if (name == null || name.isBlank())
            throw new DomainException("Mechanic name cannot be empty");

        if (specialization == null)
            throw new DomainException("Specialization cannot be null");

        this.name = name;
        this.specialization = specialization;
    }

    public final boolean canPerform(final MechanicSpecialization requiredSpec) {
        if (this.specialization == MechanicSpecialization.GENERAL) return true;
        if (requiredSpec == MechanicSpecialization.GENERAL) return true;
        return this.specialization == requiredSpec;
    }
}
