package carservice.api.dto;

import carservice.domain.enumeration.MechanicSpecialization;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record JobRequest(
        @NotBlank String name,
        @NotBlank String description,
        @NotNull @DecimalMin("0.0") BigDecimal partsCost,
        @NotNull @DecimalMin("0.0") BigDecimal laborCost,
        @NotNull MechanicSpecialization requiredSpecialization
) {
}
