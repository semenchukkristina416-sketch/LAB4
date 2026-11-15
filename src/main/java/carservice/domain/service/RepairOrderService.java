package carservice.domain.service;

import carservice.domain.DomainException;
import carservice.domain.Job;
import carservice.domain.Mechanic;
import carservice.domain.RepairOrder;
import carservice.domain.pricing.PriceCalculator;
import carservice.domain.repository.RepairOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class RepairOrderService {
    private final RepairOrderRepository repository;
    private final PriceCalculator priceCalculator;

    public RepairOrderService(RepairOrderRepository repository, PriceCalculator priceCalculator) {
        this.repository = repository;
        this.priceCalculator = priceCalculator;
    }

    @Transactional
    public void processNewOrder(RepairOrder order) {
        order.approve();
        repository.save(order);
    }

    @Transactional
    public void startOrderExecution(Long orderId, Mechanic mechanic) {
        RepairOrder order = getOrderById(orderId);
        order.assignMechanic(mechanic);
        order.startExecution();
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

    public RepairOrder getOrderById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new DomainException("Order with ID " + id + " not found"));
    }
}