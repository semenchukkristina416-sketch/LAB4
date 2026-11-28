package carservice.domain;

import carservice.domain.enumeration.MechanicSpecialization;
import carservice.domain.enumeration.OrderPriority;
import java.time.LocalDateTime;
import carservice.exception.DomainException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MechanicTest {

    @Test
    void shouldCreateMechanicSuccessfully() {
        Mechanic mechanic = new Mechanic("Oleh", MechanicSpecialization.ENGINE);

        assertEquals("Oleh", mechanic.getName());
        assertEquals(MechanicSpecialization.ENGINE, mechanic.getSpecialization());
        assertNull(mechanic.getId());
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        assertThrows(DomainException.class, () -> new Mechanic(null, MechanicSpecialization.ENGINE));
    }

    @Test
    void shouldThrowExceptionWhenNameIsBlank() {
        assertThrows(DomainException.class, () -> new Mechanic("", MechanicSpecialization.ENGINE));
        assertThrows(DomainException.class, () -> new Mechanic("   ", MechanicSpecialization.ENGINE));
    }

    @Test
    void shouldThrowExceptionWhenSpecializationIsNull() {
        assertThrows(DomainException.class, () -> new Mechanic("Oleh", null));
    }

    @Test
    void shouldReturnTrueWhenMechanicIsGeneral() {
        Mechanic mechanic = new Mechanic("Ivan", MechanicSpecialization.GENERAL);

        assertTrue(mechanic.canPerform(MechanicSpecialization.ENGINE));
    }

    @Test
    void shouldReturnTrueWhenRequiredSpecIsGeneral() {
        Mechanic mechanic = new Mechanic("Oleh", MechanicSpecialization.ENGINE);

        assertTrue(mechanic.canPerform(MechanicSpecialization.GENERAL));
    }

    @Test
    void shouldReturnTrueWhenSpecializationsMatch() {
        Mechanic mechanic = new Mechanic("Oleh", MechanicSpecialization.ENGINE);

        assertTrue(mechanic.canPerform(MechanicSpecialization.ENGINE));
    }

    @Test
    void shouldReturnFalseWhenSpecializationsDoNotMatch() {
        Mechanic mechanic = new Mechanic("Oleh", MechanicSpecialization.ENGINE);

        for (MechanicSpecialization spec : MechanicSpecialization.values()) {
            if (spec != MechanicSpecialization.ENGINE && spec != MechanicSpecialization.GENERAL) {
                assertFalse(mechanic.canPerform(spec));
                break;
            }
        }
    }

    @Test
    void busyFlagIsComputedFromAssignedOrderStatus() {
        Client client = new Client("Ivan", "0991112233");
        Car car = new Car("AA0001AA", "Toyota", "Camry", client);
        Mechanic mechanic = new Mechanic("Oleh", MechanicSpecialization.GENERAL);
        RepairOrder order = new RepairOrder(car, OrderPriority.NORMAL, LocalDateTime.now(), 60);
        order.addJob(new Job("Inspection", "General inspection", java.math.BigDecimal.ONE,
                java.math.BigDecimal.ONE, MechanicSpecialization.GENERAL));
        order.approve();
        order.assignMechanic(mechanic);

        assertFalse(mechanic.isBusy());

        order.startExecution();
        assertTrue(mechanic.isBusy());

        order.completeOrder();
        assertFalse(mechanic.isBusy());
    }
}
