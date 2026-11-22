package carservice.api;

import carservice.api.dto.JobRequest;
import carservice.api.dto.JobResponse;
import carservice.api.dto.OrderCreateRequest;
import carservice.api.dto.OrderResponse;
import carservice.api.dto.PageResponse;
import carservice.api.dto.StartOrderRequest;
import carservice.domain.Job;
import carservice.domain.enumeration.OrderStatus;
import carservice.service.MechanicService;
import carservice.service.RepairOrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Validated
public class RepairOrderController {
    private final RepairOrderService service;
    private final MechanicService mechanicService;
    private final ApiMapper mapper;

    @PostMapping
    ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderCreateRequest request) {
        OrderResponse response = mapper.toResponse(service.createOrder(request.carId(), request.priority(),
                request.plannedStart(), request.plannedDurationMinutes()));
        return ResponseEntity.created(URI.create("/api/orders/" + response.id())).body(response);
    }

    @GetMapping
    PageResponse<OrderResponse> list(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "id") String sort) {
        if (!List.of("id", "priority", "status").contains(sort)) {
            throw new IllegalArgumentException("Unsupported sort field: " + sort);
        }
        var result = service.getOrders(status, PageRequest.of(page, size, Sort.by(sort)));
        return new PageResponse<>(result.getContent().stream().map(mapper::toResponse).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    @GetMapping("/{id}")
    OrderResponse get(@PathVariable Long id) {
        return mapper.toResponse(service.getOrderById(id));
    }

    @PostMapping("/{id}/approve")
    OrderResponse approve(@PathVariable Long id) {
        service.approveOrder(id);
        return get(id);
    }

    @PostMapping("/{id}/start")
    OrderResponse start(@PathVariable Long id, @Valid @RequestBody StartOrderRequest request) {
        service.startOrderExecution(id, mechanicService.getById(request.mechanicId()));
        return get(id);
    }

    @PostMapping("/{id}/complete")
    OrderResponse complete(@PathVariable Long id) {
        service.completeOrder(id);
        return get(id);
    }

    @PostMapping("/{id}/cancel")
    OrderResponse cancel(@PathVariable Long id) {
        service.cancelOrder(id);
        return get(id);
    }

    @PostMapping("/{orderId}/jobs")
    ResponseEntity<JobResponse> addJob(@PathVariable Long orderId, @Valid @RequestBody JobRequest request) {
        JobResponse response = mapper.toResponse(service.addJobToOrder(orderId, toJob(request)));
        return ResponseEntity.created(URI.create("/api/orders/" + orderId + "/jobs/" + response.id()))
                .body(response);
    }

    @PostMapping("/{orderId}/jobs/batch")
    List<JobResponse> addJobs(@PathVariable Long orderId,
                              @RequestBody List<@Valid JobRequest> requests) {
        List<Job> jobs = requests.stream().map(this::toJob).toList();
        service.addBatchJobsToOrder(orderId, jobs);
        return mapper.toJobResponses(jobs);
    }

    @PostMapping("/{orderId}/jobs/{jobId}/start")
    JobResponse startJob(@PathVariable Long orderId, @PathVariable Long jobId) {
        return mapper.toResponse(service.startJob(orderId, jobId));
    }

    @PostMapping("/{orderId}/jobs/{jobId}/complete")
    JobResponse completeJob(@PathVariable Long orderId, @PathVariable Long jobId) {
        return mapper.toResponse(service.completeJob(orderId, jobId));
    }

    private Job toJob(final JobRequest request) {
        return new Job(request.name(), request.description(), request.partsCost(), request.laborCost(),
                request.requiredSpecialization());
    }
}
