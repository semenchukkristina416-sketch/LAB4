package carservice.service;

import carservice.exception.DomainException;
import carservice.exception.ResourceNotFoundException;
import carservice.exception.ConflictException;
import carservice.domain.Job;
import carservice.domain.Mechanic;
import carservice.domain.RepairOrder;
import carservice.domain.Car;
import carservice.domain.enumeration.OrderPriority;
import carservice.domain.enumeration.OrderStatus;
import carservice.pricing.PriceCalculator;
import carservice.repository.RepairOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
@Service
public class RepairOrderService {
    private final RepairOrderRepository repository;
    private final PriceCalculator priceCalculator;
    private final CarService carService;

    @Transactional
    public RepairOrder createOrder(final Long carId, final OrderPriority priority,
                                   final LocalDateTime plannedStart, final int plannedDurationMinutes) {
        Car car = carService.getById(carId);
        return repository.save(new RepairOrder(car, priority, plannedStart, plannedDurationMinutes));
    }


    @Transactional
    public void processNewOrder(RepairOrder order) {
        order.approve();
        repository.save(order);
    }

    @Transactional
    public void startOrderExecution(Long orderId, Mechanic mechanic) {
        RepairOrder order = getOrderById(orderId);
        if (mechanic == null) {
            throw new DomainException("Mechanic cannot be null");
        }
        if (repository.existsByMechanicIdAndStatusIn(mechanic.getId(),
                List.of(OrderStatus.IN_PROGRESS))) {
            throw new ConflictException("Mechanic is already assigned to an active order");
        }
        order.assignMechanic(mechanic);
        order.startExecution();
        repository.save(order);
    }

    @Transactional
    public void approveOrder(final Long orderId) {
        RepairOrder order = getOrderById(orderId);
        order.approve();
        repository.save(order);
    }

    @Transactional
    public void cancelOrder(final Long orderId) {
        RepairOrder order = getOrderById(orderId);
        order.cancel();
        repository.save(order);
    }

    @Transactional
    public void completeOrder(Long orderId) {
        RepairOrder order = getOrderById(orderId);
        order.completeOrder();

        BigDecimal commercialPrice = priceCalculator.calculateCommercialPrice(order);
        order.setFinalCommercialPrice(commercialPrice);

        repository.save(order);
    }

    @Transactional
    public void addBatchJobsToOrder(Long orderId, List<Job> jobs) {
        RepairOrder order = getOrderById(orderId);

        for (Job job : jobs) {
            if (job == null) {
                throw new DomainException("Batch processing failed: job cannot be null");
            }
            order.addJob(job);
        }

        repository.save(order);
    }

    @Transactional
    public Job addJobToOrder(final Long orderId, final Job job) {
        RepairOrder order = getOrderById(orderId);
        order.addJob(job);
        repository.save(order);
        return job;
    }

    @Transactional
    public Job startJob(final Long orderId, final Long jobId) {
        Job job = getJob(orderId, jobId);
        job.start();
        repository.save(getOrderById(orderId));
        return job;
    }

    @Transactional
    public Job completeJob(final Long orderId, final Long jobId) {
        Job job = getJob(orderId, jobId);
        job.complete();
        repository.save(getOrderById(orderId));
        return job;
    }

    @Transactional(readOnly = true)
    public Page<RepairOrder> getOrders(final OrderStatus status, final Pageable pageable) {
        return status == null ? repository.findAll(pageable) : repository.findAllByStatus(status, pageable);
    }

    public RepairOrder getOrderById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order with ID " + id + " not found"));
    }

    private Job getJob(final Long orderId, final Long jobId) {
        return getOrderById(orderId).getJobs().stream()
                .filter(job -> jobId.equals(job.getId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Job with ID " + jobId + " not found in order " + orderId));
    }
}
