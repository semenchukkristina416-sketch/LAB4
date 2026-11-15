package carservice.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TaxiCompany {
    private final String name;
    private final List<Car> cars = new ArrayList<>();

    public TaxiCompany(final String name) {
        if (name == null || name.isBlank()) {
            throw new DomainException("Taxi company name cannot be empty");
        }
        this.name = name;
    }

    public final String getName() {
        return name;
    }

    /** Adds a car to the taxi fleet. VIN must be unique within the company. */
    public final void addCar(final Car car) {
        if (car == null) {
            throw new DomainException("domain.Car cannot be null");
        }
        if (containsVin(car.getLicensePlate())) {
            throw new DomainException("A car with VIN " + car.getLicensePlate() + " already belongs to the taxi company");
        }
        cars.add(car);
    }

    /** Removes a car from the fleet by VIN. */
    public final void removeCar(final String vin) {
        if (vin == null || vin.isBlank()) {
            throw new DomainException("VIN cannot be empty");
        }

        boolean removed = cars.removeIf(car -> car.getLicensePlate().equals(vin));
        if (!removed) {
            throw new DomainException("domain.Car with VIN " + vin + " does not belong to the taxi company");
        }
    }

    /** Returns a read-only view of the current fleet. */
    public final List<Car> getCars() {
        return Collections.unmodifiableList(cars);
    }

    /** Returns the company's cars whose VIN values are in the provided list. */
    public final List<Car> getCarsByVins(final List<String> vins) {
        if (vins == null) {
            throw new DomainException("VIN list cannot be null");
        }
        for (String vin : vins) {
            if (vin == null || vin.isBlank()) {
                throw new DomainException("VIN cannot be empty");
            }
        }

        final List<Car> selectedCars = new ArrayList<>();
        for (Car car : cars) {
            if (vins.contains(car.getLicensePlate())) {
                selectedCars.add(car);
            }
        }
        return Collections.unmodifiableList(selectedCars);
    }

    private boolean containsVin(final String vin) {
        for (Car car : cars) {
            if (car.getLicensePlate().equals(vin)) {
                return true;
            }
        }
        return false;
    }
}
