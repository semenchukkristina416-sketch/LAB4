package carservice.domain;

import carservice.domain.enumeration.MechanicSpecialization;
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
}
