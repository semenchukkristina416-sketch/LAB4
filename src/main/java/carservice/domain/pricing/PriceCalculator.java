package carservice.domain.pricing;

import carservice.domain.RepairOrder;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class PriceCalculator {
    private final List<PricingRule> rules;

    public PriceCalculator(List<PricingRule> rules) {
        this.rules = rules;
    }

    public BigDecimal calculateCommercialPrice(RepairOrder order) {
        BigDecimal price = order.calculateBaseTotal();

        for (PricingRule rule : rules) {
            price = rule.apply(order, price);
        }

        return price.setScale(2, RoundingMode.HALF_UP);
    }
}