package carservice.api;

import carservice.api.dto.CarCreateRequest;
import carservice.api.dto.CarResponse;
import carservice.service.CarService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/cars")
@RequiredArgsConstructor
public class CarController {
    private final CarService service;
    private final ApiMapper mapper;

    @PostMapping
    ResponseEntity<CarResponse> create(@Valid @RequestBody CarCreateRequest request) {
        CarResponse response = mapper.toResponse(service.create(request.licensePlate(), request.brand(),
                request.model(), request.productionYear(), request.ownerId()));
        return ResponseEntity.created(URI.create("/api/cars/" + response.id())).body(response);
    }

    @GetMapping("/{id}")
    CarResponse get(@PathVariable Long id) {
        return mapper.toResponse(service.getById(id));
    }
}
