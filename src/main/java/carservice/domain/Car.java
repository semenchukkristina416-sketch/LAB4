package carservice.domain;

import carservice.exception.DomainException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;

import java.time.Year;

@Entity
@Table(name = "cars")
@Getter
@ToString(exclude = "owner")
public class Car {

    private static final int MIN_PRODUCTION_YEAR = 1980;
    private static final int BMW_X5_FIRST_PRODUCTION_YEAR = 1999;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "license_plate", unique = true, nullable = false)
    private String licensePlate;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private String model;

    @Column(name = "production_year", nullable = false)
    private int productionYear;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client owner;

    protected Car() {
    }

    public Car(final String licensePlate, final String brand, final String model,
               final int productionYear, final Client owner) {
        if (licensePlate == null || licensePlate.isBlank())
            throw new DomainException("License plate cannot be empty");
        if (brand == null || brand.isBlank())
            throw new DomainException("Brand cannot be empty");
        if (model == null || model.isBlank())
            throw new DomainException("Model cannot be empty");

        if (brand.equalsIgnoreCase("BMW") && model.equalsIgnoreCase("X5")
                && productionYear < BMW_X5_FIRST_PRODUCTION_YEAR) {
            throw new DomainException("BMW X5 production started in " + BMW_X5_FIRST_PRODUCTION_YEAR);
        }

        int currentYear = Year.now().getValue();
        if (productionYear < MIN_PRODUCTION_YEAR || productionYear > currentYear)
            throw new DomainException(
                    "Production year must be between " + MIN_PRODUCTION_YEAR + " and " + currentYear);

        if (owner == null)
            throw new DomainException("Owner cannot be null");

        this.licensePlate = licensePlate;
        this.brand = brand;
        this.model = model;
        this.productionYear = productionYear;
        this.owner = owner;
    }

    public Car(final String licensePlate, final String brand, final String model, final Client owner) {
        this(licensePlate, brand, model, 2020, owner);
    }
}

