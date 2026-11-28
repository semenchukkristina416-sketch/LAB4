package carservice.service;

import carservice.domain.Car;
import carservice.domain.Client;
import carservice.exception.DomainException;
import carservice.exception.ConflictException;
import carservice.exception.ResourceNotFoundException;
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
                      final int productionYear, final Long ownerId) {
        if (repository.existsByLicensePlateIgnoreCase(licensePlate)) {
            throw new ConflictException("Car with license plate " + licensePlate + " already exists");
        }
        return save(new Car(licensePlate, brand, model, productionYear, clientService.getById(ownerId)));
    }

    @Transactional
    public Car create(final String licensePlate, final String brand, final String model,
                      final Long ownerId) {
        return create(licensePlate, brand, model, 2020, ownerId);
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
                .orElseThrow(() -> new ResourceNotFoundException("Car with ID " + id + " not found"));
    }
}
