package carservice.domain;

import lombok.Getter;
import lombok.ToString;
import java.time.LocalDateTime;

@Getter
@ToString
public class TimeInterval {
    private LocalDateTime start;
    private LocalDateTime end;

    public TimeInterval(final LocalDateTime start, final int durationMinutes) {
        if (start == null)
            throw new DomainException("Start time cannot be null");
        if (durationMinutes < 0)
            throw new DomainException("Duration cannot be negative");

        this.start = start;
        this.end = start.plusMinutes(durationMinutes);
    }


    public boolean overlaps(TimeInterval other) {
        if (other == null) {
            return false;
        }
        return this.start.isBefore(other.end)
                && other.start.isBefore(this.end);
    }
}
