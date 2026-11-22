package carservice.api;

import carservice.api.dto.ClientCreateRequest;
import carservice.api.dto.ClientResponse;
import carservice.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {
    private final ClientService service;
    private final ApiMapper mapper;

    @PostMapping
    ResponseEntity<ClientResponse> create(@Valid @RequestBody ClientCreateRequest request) {
        ClientResponse response = mapper.toResponse(service.create(request.name(), request.phone()));
        return ResponseEntity.created(URI.create("/api/clients/" + response.id())).body(response);
    }

    @GetMapping("/{id}")
    ClientResponse get(@PathVariable Long id) {
        return mapper.toResponse(service.getById(id));
    }
}
