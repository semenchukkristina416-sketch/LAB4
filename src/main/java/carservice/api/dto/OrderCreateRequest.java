package carservice.api.dto;

import carservice.domain.enumeration.OrderPriority;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record OrderCreateRequest(
        @NotNull Long carId,
        @NotNull OrderPriority priority,
        @NotNull LocalDateTime plannedStart,
        @Positive int plannedDurationMinutes
) {
}
