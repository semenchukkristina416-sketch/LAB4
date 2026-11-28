package carservice.service;

import carservice.domain.Mechanic;
import carservice.domain.enumeration.MechanicSpecialization;
import carservice.exception.DomainException;
import carservice.repository.MechanicRepository;
import carservice.repository.RepairOrderRepository;
import carservice.domain.enumeration.OrderStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MechanicServiceTest {

    @Mock
    private MechanicRepository repository;

    @Mock
    private RepairOrderRepository repairOrderRepository;

    @InjectMocks
    private MechanicService service;

    @Test
    void createSavesAndReturnsMechanic() {
        when(repository.save(any(Mechanic.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Mechanic mechanic = service.create("Oleh", MechanicSpecialization.ENGINE);

        assertEquals("Oleh", mechanic.getName());
        assertEquals(MechanicSpecialization.ENGINE, mechanic.getSpecialization());
        verify(repository).save(any(Mechanic.class));
    }

    @Test
    void saveRejectsNullMechanic() {
        assertThrows(DomainException.class, () -> service.save(null));
    }

    @Test
    void getByIdReturnsExistingMechanic() {
        Mechanic mechanic = new Mechanic("Oleh", MechanicSpecialization.ENGINE);
        when(repository.findById(1L)).thenReturn(Optional.of(mechanic));

        assertSame(mechanic, service.getById(1L));
    }

    @Test
    void getByIdThrowsWhenMechanicDoesNotExist() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(DomainException.class, () -> service.getById(1L));
    }

    @Test
    void isBusyIsComputedFromActiveOrders() {
        when(repairOrderRepository.existsByMechanicIdAndStatusIn(1L,
                java.util.List.of(OrderStatus.IN_PROGRESS))).thenReturn(true);

        assertEquals(true, service.isBusy(1L));
    }
}
