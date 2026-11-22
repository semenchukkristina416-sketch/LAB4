package carservice.pricing;

import carservice.domain.Car;
import carservice.domain.Client;
import carservice.domain.enumeration.OrderPriority;
import carservice.domain.RepairOrder;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DiscountRuleTest {

    @Test
    void shouldApplyDiscountWhenPriceExceedsThreshold() {
        DiscountRule rule = new DiscountRule();
        Client client = new Client("Ivan", "0991234567");
        Car car = new Car("BC1234AC", "Toyota", "Camry", client);
        RepairOrder order = new RepairOrder(car, OrderPriority.NORMAL, LocalDateTime.now(), 120);

        BigDecimal currentPrice = new BigDecimal("1200.00");
        BigDecimal result = rule.apply(order, currentPrice);

        assertEquals(0, new BigDecimal("1080.0000").compareTo(result));
    }

    @Test
    void shouldNotApplyDiscountWhenPriceEqualsThreshold() {
        DiscountRule rule = new DiscountRule();
        Client client = new Client("Ivan", "0991234567");
        Car car = new Car("BC1234AC", "Toyota", "Camry", client);
        RepairOrder order = new RepairOrder(car, OrderPriority.NORMAL, LocalDateTime.now(), 120);

        BigDecimal currentPrice = new BigDecimal("1000.00");
        BigDecimal result = rule.apply(order, currentPrice);

        assertEquals(0, new BigDecimal("1000.00").compareTo(result));
    }

    @Test
    void shouldNotApplyDiscountWhenPriceIsBelowThreshold() {
        DiscountRule rule = new DiscountRule();
        Client client = new Client("Ivan", "0991234567");
        Car car = new Car("BC1234AC", "Toyota", "Camry", client);
        RepairOrder order = new RepairOrder(car, OrderPriority.NORMAL, LocalDateTime.now(), 120);

        BigDecimal currentPrice = new BigDecimal("800.00");
        BigDecimal result = rule.apply(order, currentPrice);

        assertEquals(0, new BigDecimal("800.00").compareTo(result));
    }
}