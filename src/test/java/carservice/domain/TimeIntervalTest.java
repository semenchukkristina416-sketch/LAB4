package carservice.domain;

import carservice.exception.DomainException;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class TimeIntervalTest {

    @Test
    void shouldCreateTimeIntervalSuccessfully() {
        LocalDateTime start = LocalDateTime.now();
        TimeInterval interval = new TimeInterval(start, 120);

        assertEquals(start, interval.getStart());
        assertEquals(start.plusMinutes(120), interval.getEnd());
    }

    @Test
    void shouldCreateTimeIntervalWithZeroDuration() {
        LocalDateTime start = LocalDateTime.now();
        TimeInterval interval = new TimeInterval(start, 0);

        assertEquals(start, interval.getStart());
        assertEquals(start, interval.getEnd());
    }

    @Test
    void shouldThrowExceptionWhenStartTimeIsNull() {
        assertThrows(DomainException.class, () -> new TimeInterval(null, 120));
    }

    @Test
    void shouldThrowExceptionWhenDurationIsNegative() {
        assertThrows(DomainException.class, () -> new TimeInterval(LocalDateTime.now(), -30));
    }

    @Test
    void shouldReturnTrueWhenIntervalsOverlap() {
        LocalDateTime now = LocalDateTime.now();
        TimeInterval first = new TimeInterval(now, 120);
        TimeInterval second = new TimeInterval(now.plusMinutes(60), 120);

        assertTrue(first.overlaps(second));
        assertTrue(second.overlaps(first));
    }

    @Test
    void shouldReturnFalseWhenIntervalsDoNotOverlap() {
        LocalDateTime now = LocalDateTime.now();
        TimeInterval first = new TimeInterval(now, 60);
        TimeInterval second = new TimeInterval(now.plusMinutes(60), 60);

        assertFalse(first.overlaps(second));
        assertFalse(second.overlaps(first));
    }

    @Test
    void shouldReturnFalseWhenOtherIntervalIsNull() {
        TimeInterval interval = new TimeInterval(LocalDateTime.now(), 60);
        assertFalse(interval.overlaps(null));
    }
}
