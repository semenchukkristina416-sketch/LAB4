package carservice.integration;

import carservice.domain.Car;
import carservice.domain.Client;
import carservice.domain.Job;
import carservice.domain.enumeration.MechanicSpecialization;
import carservice.domain.enumeration.OrderPriority;
import carservice.domain.RepairOrder;
import carservice.repository.CarRepository;
import carservice.repository.ClientRepository;
import carservice.repository.MechanicRepository;
import carservice.repository.RepairOrderRepository;
import carservice.service.RepairOrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OptimisticLockingIT extends IntegrationTest {

    @Autowired
    private RepairOrderService repairOrderService;

    @Autowired
    private RepairOrderRepository repairOrderRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private MechanicRepository mechanicRepository;

    private Long orderId;

    @BeforeEach
    void setUp() {
        repairOrderRepository.deleteAll();
        carRepository.deleteAll();
        clientRepository.deleteAll();
        mechanicRepository.deleteAll();

        Client client = clientRepository.save(new Client("Taras Melnyk", "+380509998877"));
        Car car = carRepository.save(new Car("KA5678BB", "Honda", "Civic", client));

        RepairOrder order = new RepairOrder(
                car,
                OrderPriority.NORMAL,
                LocalDateTime.of(2026, 11, 15, 15, 0),
                90
        );
        order.addJob(new Job(
                "Battery Replacement",
                "Install new 12V battery",
                new BigDecimal("500.00"),
                new BigDecimal("200.00"),
                MechanicSpecialization.GENERAL
        ));
        repairOrderService.processNewOrder(order);
        this.orderId = order.getId();
    }

    @Test
    @DisplayName("Throws ObjectOptimisticLockingFailureException on concurrent order update")
    void throwsOptimisticLockingExceptionOnConcurrentUpdate() {

        RepairOrder manager1Copy = repairOrderService.getOrderById(orderId);
        RepairOrder manager2Copy = repairOrderService.getOrderById(orderId);

        assertEquals(manager1Copy.getVersion(), manager2Copy.getVersion());

        manager1Copy.diagnose();
        repairOrderRepository.saveAndFlush(manager1Copy);

        manager2Copy.completeOrder();

        assertThrows(ObjectOptimisticLockingFailureException.class, () ->
                repairOrderRepository.saveAndFlush(manager2Copy)
        );
    }
}