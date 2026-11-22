package carservice.service;

import carservice.domain.Car;
import carservice.domain.Client;
import carservice.exception.DomainException;
import carservice.repository.CarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CarService {

    private final CarRepository repository;
    private final ClientService clientService;

    @Transactional
    public Car create(final String licensePlate, final String brand, final String model,
                      final Long ownerId) {
        return save(new Car(licensePlate, brand, model, clientService.getById(ownerId)));
    }

    @Transactional
    public Car save(final Car car) {
        if (car == null) {
            throw new DomainException("Car cannot be null");
        }
        return repository.save(car);
    }

    @Transactional(readOnly = true)
    public Car getById(final Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new DomainException("Car with ID " + id + " not found"));
    }
}
