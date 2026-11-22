package carservice.pricing;

import carservice.domain.RepairOrder;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class MinimumPriceRule implements PricingRule {
    private final BigDecimal minPrice = new BigDecimal("500.00");

    @Override
    public BigDecimal apply(RepairOrder order, BigDecimal currentPrice) {
        if (currentPrice.compareTo(BigDecimal.ZERO) == 0) {
            return currentPrice;
        }
        return currentPrice.max(minPrice);
    }
}