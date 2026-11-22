package carservice.api.dto;

import carservice.domain.enumeration.OrderPriority;
import carservice.domain.enumeration.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        Long carId,
        Long mechanicId,
        OrderPriority priority,
        OrderStatus status,
        LocalDateTime plannedStart,
        LocalDateTime plannedEnd,
        BigDecimal finalCommercialPrice,
        List<JobResponse> jobs
) {
}
