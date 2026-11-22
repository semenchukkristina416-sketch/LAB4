package carservice.domain;

import carservice.exception.DomainException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

@Embeddable
@Getter
@EqualsAndHashCode
@ToString
public class JobCost {

    @Column(name = "parts_cost")
    private BigDecimal partsCost;

    @Column(name = "labor_cost")
    private BigDecimal laborCost;

    @Column(name = "base_price", nullable = false)
    private BigDecimal basePrice;

    @Column(name = "commercial_price", nullable = false)
    private BigDecimal commercialPrice;

    protected JobCost() {
    }

    public JobCost(BigDecimal partsCost, BigDecimal laborCost) {
        if (partsCost == null || partsCost.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException("Invalid parts cost");
        }
        if (laborCost == null || laborCost.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException("Invalid labor cost");
        }

        this.partsCost = partsCost;
        this.laborCost = laborCost;
        this.basePrice = partsCost.add(laborCost);
        this.commercialPrice = this.basePrice;
    }

    public void setCommercialPrice(BigDecimal commercialPrice) {
        this.commercialPrice = commercialPrice;
    }
}
