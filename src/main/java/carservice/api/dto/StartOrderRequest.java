package carservice.api.dto;

import jakarta.validation.constraints.NotNull;

public record StartOrderRequest(@NotNull Long mechanicId) {
}
