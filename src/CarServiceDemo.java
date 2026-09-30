import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class CarServiceDemo {

    public static void main(final String[] args) {
        final CarService carService = new CarService();

        System.out.println("========================================");
        System.out.println("       CAR SERVICE MANAGEMENT SYSTEM");
        System.out.println("========================================\n");

        runPositiveScenario(carService);

        System.out.println("\n========== NEGATIVE SCENARIOS ==========\n");
        runNegativeScenarios(carService);
    }

    private static void runPositiveScenario(final CarService carService) {
        final Client client = new Client("ID-1", "Alexander", "+380990000000");
        final Car car = new Car("VIN123456789", "Toyota", "Camry", 2018, client);
        final TaxiCompany taxiCompany = new TaxiCompany("City Taxi");
        final Mechanic mechanic = new Mechanic("Ivan", MechanicSpecialization.ENGINE);
        final ServicePost post = new ServicePost("POST-1", "Post #1");

        carService.registerClient(client);
        System.out.println("Client registered: " + client.getName());

        taxiCompany.addCar(car);
        taxiCompany.registerCarsAtService(carService);
        System.out.println("Taxi fleet registered: " + taxiCompany.getCars().size() + " car(s)");

        carService.registerMechanic(mechanic);
        System.out.println("Mechanic registered: " + mechanic.getName());

        carService.registerServicePost(post);
        System.out.println("Service post registered: " + post.getName() + "\n");

        System.out.println("========== POSITIVE SCENARIO ==========");

        final LocalDateTime plannedTime = LocalDateTime.now();
        final LocalDateTime executionTime = plannedTime.plusDays(1);


        final String orderId = UUID.randomUUID().toString();
        final Order order = new Order(orderId, car, OrderPriority.NORMAL, executionTime, 120);
        carService.registerOrder(order);
        System.out.println("Order created: " + order.getOrderId());

        final BigDecimal partsCost1 = new BigDecimal("1000.00");
        final BigDecimal laborCost1 = new BigDecimal("500.00");
        final Job job1 = new Job("Job 1", partsCost1, laborCost1, MechanicSpecialization.ENGINE);
        order.addJob(job1);

        final BigDecimal partsCost2 = new BigDecimal("560.00");
        final BigDecimal laborCost2 = new BigDecimal("500.00");
        final Job job2 = new Job("Job 2", partsCost2, laborCost2, MechanicSpecialization.ENGINE);
        order.addJob(job2);

        System.out.println("2 jobs added to the order");

        order.diagnose();
        System.out.println("Status: " + order.getStatus());

        order.approve();
        System.out.println("Status: " + order.getStatus());

        final boolean available = carService.isMechanicAvailable(order, mechanic);
        if (!available) {
            throw new DomainException("Mechanic is busy");
        }

        order.assignMechanic(mechanic);
        order.assignServicePost(post);
        System.out.println("Mechanic and service post assigned");

        order.startExecution();
        System.out.println("Status: " + order.getStatus());

        order.completeJob(job1);
        System.out.println("Job 1 completed");

        order.completeJob(job2);
        System.out.println("Job 2 completed");

        order.completeOrder();
        System.out.println("Status: " + order.getStatus());

        final BigDecimal total = order.calculateTotal();
        System.out.println("Total cost: " + total + " UAH");
    }

    private static void runNegativeScenarios(final CarService carService) {
        final Client tempClient = new Client("ID-2", "Ivan", "+380501112233");
        final Car tempCar = new Car("VIN987", "Honda", "Civic", 2019, tempClient);

        runScenario("Attempt to approve order without jobs", () -> {
            final LocalDateTime now = LocalDateTime.now();
            final Order badOrder = new Order("ORD-002", tempCar, OrderPriority.NORMAL, now, 60);
            badOrder.diagnose();
            badOrder.approve();
        });

        runScenario("Attempt to start execution without mechanic", () -> {
            final LocalDateTime now = LocalDateTime.now();
            final Order order = new Order("ORD-003", tempCar, OrderPriority.NORMAL, now, 60);
            final Job job = new Job("Test", BigDecimal.ZERO, BigDecimal.TEN, MechanicSpecialization.ENGINE);
            order.addJob(job);
            order.diagnose();
            order.approve();

            // Призначаємо тільки пост, але не призначаємо механіка
            final ServicePost post = new ServicePost("P2", "Post 2");
            order.assignServicePost(post);


            order.startExecution();
        });
    }

    private static void runScenario(final String title, final Runnable action) {
        try {
            action.run();
            System.out.println("[Scenario] " + title + " -> FAILED (Error expected)\n");
        } catch (DomainException e) {
            final String message = e.getMessage();
            System.out.println("[Scenario] " + title + " -> SUCCESS");
            System.out.println("Reason: " + message + "\n");
        } catch (Exception e) {
            final Class<?> exceptionClass = e.getClass();
            final String exceptionName = exceptionClass.getSimpleName();
            System.out.println("[Scenario] " + title + " -> ERROR: Incorrect exception type: " + exceptionName + "\n");
        }
    }
}
