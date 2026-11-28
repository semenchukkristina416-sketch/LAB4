package carservice.domain;

import carservice.domain.enumeration.MechanicSpecialization;
import carservice.domain.enumeration.OrderStatus;
import carservice.exception.DomainException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mechanics")
@Getter
@ToString(exclude = "orders")
public class Mechanic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MechanicSpecialization specialization;

    @OneToMany(mappedBy = "mechanic", fetch = FetchType.LAZY)
    private List<RepairOrder> orders = new ArrayList<>();

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

    @Transient
    public boolean isBusy() {
        return orders != null && orders.stream()
                .anyMatch(order -> order.getStatus() == OrderStatus.IN_PROGRESS);
    }

    void registerOrder(final RepairOrder order) {
        if (order != null && !orders.contains(order)) {
            orders.add(order);
        }
    }

    void unregisterOrder(final RepairOrder order) {
        orders.remove(order);
    }

    public final boolean canPerform(final MechanicSpecialization requiredSpec) {
        if (this.specialization == MechanicSpecialization.GENERAL) return true;
        if (requiredSpec == MechanicSpecialization.GENERAL) return true;
        return this.specialization == requiredSpec;
    }
}
