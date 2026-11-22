package carservice.service;

import carservice.domain.Client;
import carservice.exception.DomainException;
import carservice.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository repository;

    @Transactional
    public Client create(final String name, final String phone) {
        return repository.save(new Client(name, phone));
    }

    @Transactional
    public Client save(final Client client) {
        if (client == null) {
            throw new DomainException("Client cannot be null");
        }
        return repository.save(client);
    }

    @Transactional(readOnly = true)
    public Client getById(final Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new DomainException("Client with ID " + id + " not found"));
    }
}
