package carservice.domain.pricing;

import carservice.domain.Car;
import carservice.domain.Client;
import carservice.domain.Job;
import carservice.domain.MechanicSpecialization;
import carservice.domain.OrderPriority;
import carservice.domain.RepairOrder;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PriceCalculatorTest {

    @Test
    void shouldCalculateFinalPriceApplyingAllRules() {
        List<PricingRule> rules = List.of(
                new DiscountRule(),
                new MinimumPriceRule()
        );

        PriceCalculator calculator = new PriceCalculator(rules);

        Client client = new Client("Ivan", "0991234567");
        Car car = new Car("VIN123", "Toyota", "Camry", client);
        RepairOrder order = new RepairOrder(car, OrderPriority.NORMAL, LocalDateTime.now(), 120);

        Job job = new Job("Engine Repair", "Fix engine", new BigDecimal("600"), new BigDecimal("400"), MechanicSpecialization.ENGINE);
        order.addJob(job);

        BigDecimal finalPrice = calculator.calculateCommercialPrice(order);

        assertNotNull(finalPrice);
    }
}