package carservice.domain;

import carservice.exception.DomainException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CarTest {

    @Test
    void shouldCreateCarSuccessfully() {
        Client client = new Client("Ivan", "0991112233");
        Car car = new Car("BC1234AC", "Toyota", "Camry", client);

        assertEquals("BC1234AC", car.getLicensePlate());
        assertEquals("Toyota", car.getBrand());
        assertEquals("Camry", car.getModel());
        assertEquals(client, car.getOwner());
    }

    @Test
    void shouldRejectBMWX5ProducedBeforeItsFirstProductionYear() {
        Client client = new Client("Ivan", "0991112233");

        DomainException exception = assertThrows(DomainException.class,
                () -> new Car("AA0000AA", "BMW", "X5", 1922, client));

        assertTrue(exception.getMessage().contains("BMW X5"));
    }

    @Test
    void shouldKeepProductionYearOnCar() {
        Client client = new Client("Ivan", "0991112233");

        Car car = new Car("AA0000AA", "BMW", "X5", 2020, client);

        assertEquals(2020, car.getProductionYear());
    }

    @Test
    void shouldThrowExceptionWhenLicensePlateIsBlank() {
        Client client = new Client("Ivan", "0991112233");
        assertThrows(DomainException.class, () -> new Car("", "Toyota", "Camry", client));
        assertThrows(DomainException.class, () -> new Car(null, "Toyota", "Camry", client));
    }

    @Test
    void shouldThrowExceptionWhenBrandIsBlank() {
        Client client = new Client("Ivan", "0991112233");
        assertThrows(DomainException.class, () -> new Car("BC1234AC", "", "Camry", client));
        assertThrows(DomainException.class, () -> new Car("BC1234AC", null, "Camry", client));
    }

    @Test
    void shouldThrowExceptionWhenModelIsBlank() {
        Client client = new Client("Ivan", "0991112233");
        assertThrows(DomainException.class, () -> new Car("BC1234AC", "Toyota", "", client));
        assertThrows(DomainException.class, () -> new Car("BC1234AC", "Toyota", null, client));
    }

    @Test
    void shouldThrowExceptionWhenOwnerIsNull() {
        assertThrows(DomainException.class, () -> new Car("BC1234AC", "Toyota", "Camry", null));
    }
}
