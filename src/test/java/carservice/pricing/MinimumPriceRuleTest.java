package carservice.pricing;

import carservice.domain.Car;
import carservice.domain.Client;
import carservice.domain.enumeration.OrderPriority;
import carservice.domain.RepairOrder;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MinimumPriceRuleTest {

    @Test
    void shouldApplyMinimumPriceWhenCurrentPriceIsLower() {
        MinimumPriceRule rule = new MinimumPriceRule();
        Client client = new Client("Ivan", "0991234567");
        Car car = new Car("BC1234AC", "Toyota", "Camry", client);
        RepairOrder order = new RepairOrder(car, OrderPriority.NORMAL, LocalDateTime.now(), 120);

        BigDecimal currentPrice = new BigDecimal("300.00");
        BigDecimal result = rule.apply(order, currentPrice);

        assertEquals(0, new BigDecimal("500.00").compareTo(result));
    }

    @Test
    void shouldKeepCurrentPriceWhenItIsHigherThanMinimum() {
        MinimumPriceRule rule = new MinimumPriceRule();
        Client client = new Client("Ivan", "0991234567");
        Car car = new Car("BC1234AC", "Toyota", "Camry", client);
        RepairOrder order = new RepairOrder(car, OrderPriority.NORMAL, LocalDateTime.now(), 120);

        BigDecimal currentPrice = new BigDecimal("800.00");
        BigDecimal result = rule.apply(order, currentPrice);

        assertEquals(0, new BigDecimal("800.00").compareTo(result));
    }

    @Test
    void shouldReturnZeroWhenCurrentPriceIsZero() {
        MinimumPriceRule rule = new MinimumPriceRule();
        Client client = new Client("Ivan", "0991234567");
        Car car = new Car("BC1234AC", "Toyota", "Camry", client);
        RepairOrder order = new RepairOrder(car, OrderPriority.NORMAL, LocalDateTime.now(), 120);

        BigDecimal currentPrice = BigDecimal.ZERO;
        BigDecimal result = rule.apply(order, currentPrice);

        assertEquals(0, BigDecimal.ZERO.compareTo(result));
    }
}