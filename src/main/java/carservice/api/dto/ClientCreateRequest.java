package carservice.api.dto;

import jakarta.validation.constraints.NotBlank;

public record ClientCreateRequest(
        @NotBlank String name,
        @NotBlank String phone
) {
}
