package carservice.api.dto;

import carservice.domain.enumeration.MechanicSpecialization;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MechanicCreateRequest(
        @NotBlank String name,
        @NotNull MechanicSpecialization specialization
) {
}
