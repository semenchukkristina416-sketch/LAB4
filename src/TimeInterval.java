import java.time.LocalDateTime;

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

    public final LocalDateTime getStart() { return start; }
    public final LocalDateTime getEnd() { return end; }

    public boolean overlaps(TimeInterval other) {
        return this.start.isBefore(other.end)
                && other.start.isBefore(this.end);
    }
}
