package carservice.domain;

import carservice.exception.DomainException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;

@Entity
@Table(name = "cars")
@Getter
@ToString(exclude = "owner")
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "license_plate", unique = true, nullable = false)
    private String licensePlate;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private String model;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client owner;

    protected Car() {
    }

    public Car(final String licensePlate, final String brand, final String model, final Client owner) {
        if (licensePlate == null || licensePlate.isBlank())
            throw new DomainException("License plate cannot be empty");
        if (brand == null || brand.isBlank())
            throw new DomainException("Brand cannot be empty");
        if (model == null || model.isBlank())
            throw new DomainException("Model cannot be empty");
        if (owner == null)
            throw new DomainException("Owner cannot be null");

        this.licensePlate = licensePlate;
        this.brand = brand;
        this.model = model;
        this.owner = owner;
    }
}


