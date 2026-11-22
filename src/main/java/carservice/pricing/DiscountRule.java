package carservice.pricing;

import carservice.domain.RepairOrder;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class DiscountRule implements PricingRule {
    private final BigDecimal threshold = new BigDecimal("1000.00");
    private final BigDecimal discountRate = new BigDecimal("0.90"); // -10%

    @Override
    public BigDecimal apply(RepairOrder order, BigDecimal currentPrice) {
        if (currentPrice.compareTo(threshold) > 0) {
            return currentPrice.multiply(discountRate);
        }
        return currentPrice;
    }
}