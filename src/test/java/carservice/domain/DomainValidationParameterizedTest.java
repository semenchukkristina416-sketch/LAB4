package carservice.domain;

import carservice.domain.enumeration.OrderPriority;
import carservice.exception.DomainException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;

class DomainValidationParameterizedTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void rejectsBlankClientNames(String name) {
        assertThrows(DomainException.class, () -> new Client(name, "+380991234567"));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -30})
    void rejectsNonPositiveOrderDurations(int duration) {
        Client client = new Client("Client", "+380991234567");
        Car car = new Car("AA" + Math.abs(duration) + "ZZ", "Toyota", "Camry", client);

        assertThrows(DomainException.class, () ->
                new RepairOrder(car, OrderPriority.NORMAL, LocalDateTime.now(), duration));
    }
}
