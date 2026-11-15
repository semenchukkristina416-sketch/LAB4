package carservice.domain;

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