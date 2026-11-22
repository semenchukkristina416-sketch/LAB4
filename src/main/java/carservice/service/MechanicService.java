package carservice.service;

import carservice.domain.Mechanic;
import carservice.domain.enumeration.MechanicSpecialization;
import carservice.exception.DomainException;
import carservice.exception.ResourceNotFoundException;
import carservice.repository.MechanicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MechanicService {

    private final MechanicRepository repository;

    @Transactional
    public Mechanic create(final String name, final MechanicSpecialization specialization) {
        return repository.save(new Mechanic(name, specialization));
    }

    @Transactional
    public Mechanic save(final Mechanic mechanic) {
        if (mechanic == null) {
            throw new DomainException("Mechanic cannot be null");
        }
        return repository.save(mechanic);
    }

    @Transactional(readOnly = true)
    public Mechanic getById(final Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mechanic with ID " + id + " not found"));
    }
}
