package carservice;

import carservice.domain.*;
import carservice.domain.enumeration.MechanicSpecialization;
import carservice.domain.enumeration.OrderPriority;
import carservice.exception.DomainException;
import carservice.repository.RepairOrderRepository; // Додано імпорт
import carservice.service.CarService;
import carservice.service.ClientService;
import carservice.service.MechanicService;
import carservice.service.RepairOrderService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class CarServiceRunner implements CommandLineRunner {

    private final RepairOrderService service;
    private final ClientService clientService;
    private final CarService carService;
    private final MechanicService mechanicService;
    private final RepairOrderRepository repairOrderRepository;

    public CarServiceRunner(RepairOrderService service, ClientService clientService,
                            CarService carService, MechanicService mechanicService,
                            RepairOrderRepository repairOrderRepository) {
        this.service = service;
        this.clientService = clientService;
        this.carService = carService;
        this.mechanicService = mechanicService;
        this.repairOrderRepository = repairOrderRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n=== CAR SERVICE MANAGEMENT SYSTEM (CLEAN ARCHITECTURE) ===\n");

        Client client = clientService.create("Ivan", "0991234567");

        Car car = carService.create("BC" + System.currentTimeMillis() % 10000 + "AC",
                "Toyota", "Camry", client.getId());

        Mechanic mechanic = mechanicService.create("Oleh", MechanicSpecialization.ENGINE);

        System.out.println("--- Scenario 1: Successful order execution ---");
        try {
            RepairOrder order = new RepairOrder(car, OrderPriority.NORMAL, LocalDateTime.now(), 120);
            order.addJob(new Job("Engine Repair", "Timing belt replacement", new BigDecimal("500"), new BigDecimal("600"), MechanicSpecialization.ENGINE));

            service.processNewOrder(order);
            System.out.println("Order approved and saved to DB! Status: " + order.getStatus());

            Long orderId = order.getId();

            service.startOrderExecution(orderId, mechanic);
            System.out.println("Execution started. Status: " + service.getOrderById(orderId).getStatus());

            service.completeOrder(orderId);

            RepairOrder completedOrder = service.getOrderById(orderId);
            System.out.println("Order completed! Status: " + completedOrder.getStatus());
            System.out.println("Base price: " + completedOrder.calculateBaseTotal() + " UAH");
            System.out.println("Commercial price (to pay): " + completedOrder.getFinalCommercialPrice() + " UAH");

        } catch (Exception e) {
            System.out.println("Unexpected error occurred: " + e.getMessage());
        }

        System.out.println("\n--- Scenario 2: Attempt to approve an empty order ---");
        try {
            RepairOrder badOrder = new RepairOrder(car, OrderPriority.URGENT, LocalDateTime.now(), 30);
            service.processNewOrder(badOrder);
        } catch (DomainException e) {
            System.out.println("SUCCESSFULLY CAUGHT EXCEPTION: " + e.getMessage());
        }

        System.out.println("\n--- Scenario 3: Transaction rollback on batch job insert ---");
        try {
            RepairOrder batchOrder = new RepairOrder(car, OrderPriority.NORMAL, LocalDateTime.now(), 60);
            batchOrder.addJob(new Job("Diagnostics", "Initial Inspection", new BigDecimal("100"), new BigDecimal("50"), MechanicSpecialization.GENERAL));
            service.processNewOrder(batchOrder);
            Long batchOrderId = batchOrder.getId();

            System.out.println("Order initially saved with " + batchOrder.getJobs().size() + " job.");

            Job validJob = new Job("Maintenance", "Oil change", new BigDecimal("300"), new BigDecimal("100"), MechanicSpecialization.GENERAL);
            Job invalidJob = null;
            List<Job> batchJobs = Arrays.asList(validJob, invalidJob);

            System.out.println("Attempting to add batch (1 valid, 1 invalid)...");
            service.addBatchJobsToOrder(batchOrderId, batchJobs);

        } catch (DomainException e) {
            System.out.println("BATCH EXCEPTION CAUGHT: " + e.getMessage());
            System.out.println("Transaction rolled back! The valid job from the batch was NOT saved to the database.");
        }

        System.out.println("\n--- Scenario 4: Optimistic Locking Conflict (@Version) ---");
        try {
            RepairOrder optOrder = new RepairOrder(car, OrderPriority.NORMAL, LocalDateTime.now(), 60);
            repairOrderRepository.save(optOrder);
            Long optId = optOrder.getId();
            System.out.println("Order created with version: " + optOrder.getVersion());

            RepairOrder manager1Order = repairOrderRepository.findById(optId).orElseThrow();
            RepairOrder manager2Order = repairOrderRepository.findById(optId).orElseThrow();

            manager1Order.diagnose();
            repairOrderRepository.save(manager1Order);
            System.out.println("Manager 1 successfully updated status to DIAGNOSIS. New version: " + manager1Order.getVersion());

            manager2Order.completeOrder();
            System.out.println("Manager 2 attempting to save stale order (version " + manager2Order.getVersion() + ")...");
            repairOrderRepository.save(manager2Order);

        } catch (org.springframework.orm.ObjectOptimisticLockingFailureException e) {
            System.out.println("SUCCESSFULLY CAUGHT OPTIMISTIC LOCKING EXCEPTION!");
            System.out.println("Conflict detected: Another user has already updated this order.");
        }

        System.out.println("\n=== DEMO FINISHED ===");
    }
}
