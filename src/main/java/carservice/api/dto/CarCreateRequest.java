package carservice.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CarCreateRequest(
        @NotBlank String licensePlate,
        @NotBlank String brand,
        @NotBlank String model,
        @NotNull @Min(1980) @Max(2100) Integer productionYear,
        @NotNull Long ownerId
) {
}
