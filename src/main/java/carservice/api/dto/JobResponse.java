package carservice.api.dto;

import carservice.domain.enumeration.JobStatus;
import carservice.domain.enumeration.MechanicSpecialization;

import java.math.BigDecimal;

public record JobResponse(
        Long id,
        String name,
        String description,
        BigDecimal partsCost,
        BigDecimal laborCost,
        BigDecimal basePrice,
        BigDecimal commercialPrice,
        MechanicSpecialization requiredSpecialization,
        JobStatus status
) {
}
