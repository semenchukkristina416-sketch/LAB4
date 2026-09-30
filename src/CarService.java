import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CarService {
    private final List<Client> clients = new ArrayList<>();
    private final List<Car> cars = new ArrayList<>();
    private final List<Mechanic> mechanics = new ArrayList<>();
    private final List<ServicePost> servicePosts = new ArrayList<>();
    private final List<Order> orders = new ArrayList<>();

    public final void registerClient(final Client client) {
        if (client == null)
            throw new DomainException("Client cannot be null");
        clients.add(client);
    }

    public final void registerCar(final Car car) {
        if (car == null)
            throw new DomainException("Car cannot be null");
        ensureVinIsNotRegistered(car.getVin());
        cars.add(car);
    }

    /**
     * Registers a whole fleet in one operation. The service is not changed if
     * any item is invalid or if a VIN is already registered.
     */
    public final void registerCars(final List<Car> carsToRegister) {
        if (carsToRegister == null) {
            throw new DomainException("Car list cannot be null");
        }

        final Set<String> registeredVins = getRegisteredVins();
        for (Car car : carsToRegister) {
            if (car == null) {
                throw new DomainException("Car list cannot contain null");
            }
            if (!registeredVins.add(car.getVin())) {
                throw new DomainException("Car with VIN " + car.getVin() + " is already registered");
            }
        }
        cars.addAll(carsToRegister);
    }

    public final List<Car> getCars() {
        return Collections.unmodifiableList(cars);
    }

    public final void registerMechanic(final Mechanic mechanic) {
        if (mechanic == null)
            throw new DomainException("Mechanic cannot be null");
        mechanics.add(mechanic);
    }

    public final void registerServicePost(final ServicePost post) {
        if (post == null)
            throw new DomainException("Post cannot be null");
        servicePosts.add(post);
    }

    public final void registerOrder(final Order order) {
        if (order == null)
            throw new DomainException("Order cannot be null");
        orders.add(order);
    }

    public final boolean isMechanicAvailable(final Order targetOrder, final Mechanic targetMechanic) {
        TimeInterval targetInterval = targetOrder.getPlannedInterval();

        for (Order order : orders) {
            if (order == targetOrder) {
                continue;
            }

            Mechanic assignedMechanic = order.getMechanic();
            if (assignedMechanic != targetMechanic) {
                continue;
            }

            OrderStatus currentStatus = order.getStatus();
            boolean isApproved = (currentStatus == OrderStatus.APPROVED);
            boolean isInProgress = (currentStatus == OrderStatus.IN_PROGRESS);

            if (isApproved || isInProgress) {
                TimeInterval existingInterval = order.getPlannedInterval();
                boolean isOverlapping = existingInterval.overlaps(targetInterval);
                if (isOverlapping) {
                    return false;
                }
            }
        }
        return true;
    }

    private void ensureVinIsNotRegistered(final String vin) {
        if (getRegisteredVins().contains(vin)) {
            throw new DomainException("Car with VIN " + vin + " is already registered");
        }
    }

    private Set<String> getRegisteredVins() {
        final Set<String> registeredVins = new HashSet<>();
        for (Car car : cars) {
            registeredVins.add(car.getVin());
        }
        return registeredVins;
    }
}
