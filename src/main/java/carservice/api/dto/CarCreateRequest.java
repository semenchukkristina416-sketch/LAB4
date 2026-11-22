package carservice.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CarCreateRequest(
        @NotBlank String licensePlate,
        @NotBlank String brand,
        @NotBlank String model,
        @NotNull Long ownerId
) {
}
