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
            throw new DomainException("Car cannot be null");
        }
        if (containsVin(car.getVin())) {
            throw new DomainException("A car with VIN " + car.getVin() + " already belongs to the taxi company");
        }
        cars.add(car);
    }

    /** Removes a car from the fleet by VIN. */
    public final void removeCar(final String vin) {
        if (vin == null || vin.isBlank()) {
            throw new DomainException("VIN cannot be empty");
        }

        boolean removed = cars.removeIf(car -> car.getVin().equals(vin));
        if (!removed) {
            throw new DomainException("Car with VIN " + vin + " does not belong to the taxi company");
        }
    }

    /** Returns a read-only view of the current fleet. */
    public final List<Car> getCars() {
        return Collections.unmodifiableList(cars);
    }

    /** Registers the complete current taxi fleet in the car service. */
    public final void registerCarsAtService(final CarService carService) {
        if (carService == null) {
            throw new DomainException("Car service cannot be null");
        }
        carService.registerCars(cars);
    }

    private boolean containsVin(final String vin) {
        for (Car car : cars) {
            if (car.getVin().equals(vin)) {
                return true;
            }
        }
        return false;
    }
}
