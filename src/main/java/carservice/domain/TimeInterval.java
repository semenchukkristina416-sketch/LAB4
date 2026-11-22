package carservice.domain;

import carservice.exception.DomainException;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Value object describing the planned time interval of a repair order.
 */
@Getter
@ToString
public class TimeInterval {
    private final LocalDateTime start;
    private final LocalDateTime end;

    public TimeInterval(final LocalDateTime start, final int durationMinutes) {
        if (start == null) {
            throw new DomainException("Start time cannot be null");
        }
        if (durationMinutes < 0) {
            throw new DomainException("Duration cannot be negative");
        }

        this.start = start;
        this.end = start.plusMinutes(durationMinutes);
    }

    public boolean overlaps(final TimeInterval other) {
        if (other == null) {
            return false;
        }
        return this.start.isBefore(other.end)
                && other.start.isBefore(this.end);
    }
}
