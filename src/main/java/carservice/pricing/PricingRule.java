package carservice.pricing;

import carservice.domain.RepairOrder;
import java.math.BigDecimal;

public interface PricingRule {
    BigDecimal apply(RepairOrder order, BigDecimal currentPrice);
}
