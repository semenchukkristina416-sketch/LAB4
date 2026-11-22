package carservice.service;

import carservice.domain.Car;
import carservice.domain.Client;
import carservice.exception.DomainException;
import carservice.repository.CarRepository;
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
class CarServiceTest {

    @Mock
    private CarRepository repository;

    @Mock
    private ClientService clientService;

    @InjectMocks
    private CarService service;

    @Test
    void createResolvesOwnerAndSavesCar() {
        Client owner = new Client("Ivan", "0991234567");
        when(clientService.getById(1L)).thenReturn(owner);
        when(repository.save(any(Car.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Car car = service.create("BC1234AC", "Toyota", "Camry", 1L);

        assertEquals("BC1234AC", car.getLicensePlate());
        assertSame(owner, car.getOwner());
        verify(clientService).getById(1L);
        verify(repository).save(any(Car.class));
    }

    @Test
    void saveRejectsNullCar() {
        assertThrows(DomainException.class, () -> service.save(null));
    }

    @Test
    void getByIdReturnsExistingCar() {
        Client owner = new Client("Ivan", "0991234567");
        Car car = new Car("BC1234AC", "Toyota", "Camry", owner);
        when(repository.findById(1L)).thenReturn(Optional.of(car));

        assertSame(car, service.getById(1L));
    }

    @Test
    void getByIdThrowsWhenCarDoesNotExist() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(DomainException.class, () -> service.getById(1L));
    }
}
