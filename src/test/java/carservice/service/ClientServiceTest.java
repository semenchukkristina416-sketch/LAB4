package carservice.service;

import carservice.domain.Client;
import carservice.exception.DomainException;
import carservice.repository.ClientRepository;
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
class ClientServiceTest {

    @Mock
    private ClientRepository repository;

    @InjectMocks
    private ClientService service;

    @Test
    void createSavesAndReturnsClient() {
        when(repository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Client client = service.create("Ivan", "0991234567");

        assertEquals("Ivan", client.getName());
        assertEquals("0991234567", client.getPhone());
        verify(repository).save(any(Client.class));
    }

    @Test
    void saveRejectsNullClient() {
        assertThrows(DomainException.class, () -> service.save(null));
    }

    @Test
    void getByIdReturnsExistingClient() {
        Client client = new Client("Ivan", "0991234567");
        when(repository.findById(1L)).thenReturn(Optional.of(client));

        assertSame(client, service.getById(1L));
    }

    @Test
    void getByIdThrowsWhenClientDoesNotExist() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(DomainException.class, () -> service.getById(1L));
    }
}
