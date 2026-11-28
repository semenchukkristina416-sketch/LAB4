package carservice.api;

import carservice.api.dto.MechanicCreateRequest;
import carservice.api.dto.MechanicResponse;
import carservice.service.MechanicService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/mechanics")
@RequiredArgsConstructor
public class MechanicController {
    private final MechanicService service;
    private final ApiMapper mapper;

    @PostMapping
    ResponseEntity<MechanicResponse> create(@Valid @RequestBody MechanicCreateRequest request) {
        var mechanic = service.create(request.name(), request.specialization());
        MechanicResponse response = mapper.toResponse(mechanic, service.isBusy(mechanic.getId()));
        return ResponseEntity.created(URI.create("/api/mechanics/" + response.id())).body(response);
    }

    @GetMapping("/{id}")
    MechanicResponse get(@PathVariable Long id) {
        var mechanic = service.getById(id);
        return mapper.toResponse(mechanic, service.isBusy(mechanic.getId()));
    }
}
