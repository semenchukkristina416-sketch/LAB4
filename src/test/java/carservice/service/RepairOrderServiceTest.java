package carservice.service;

import carservice.*;
import carservice.domain.*;
import carservice.domain.enumeration.MechanicSpecialization;
import carservice.domain.enumeration.OrderPriority;
import carservice.domain.enumeration.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import carservice.pricing.PriceCalculator;
import carservice.repository.RepairOrderRepository;
import carservice.exception.DomainException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepairOrderServiceTest {

    @Mock
    private RepairOrderRepository repository;

    @Mock
    private PriceCalculator calculator;

    @InjectMocks
    private RepairOrderService service;

    private RepairOrder order;

    @BeforeEach
    void setUp() {
        Client client = new Client("C1", "Client");
        Car car = new Car("V1", "B", "M", client);
        order = new RepairOrder(car, OrderPriority.NORMAL, LocalDateTime.now(), 60);
    }

    @Test
    @DisplayName("Service: process new order saves approved order")
    void processNewOrder_logic() {
        order.addJob(new Job("Test Job", "J", BigDecimal.ONE, BigDecimal.ONE, MechanicSpecialization.ENGINE));

        service.processNewOrder(order);

        assertEquals(OrderStatus.APPROVED, order.getStatus());
        verify(repository, times(1)).save(order);
    }

    @Test
    @DisplayName("Service: start order execution assigns mechanic and updates state")
    void startOrderExecution_logic() {
        when(repository.findById(1L)).thenReturn(Optional.of(order));
        Mechanic mech = new Mechanic("M1", MechanicSpecialization.ENGINE);
        order.addJob(new Job("Test Job", "J", BigDecimal.ONE, BigDecimal.ONE,
                MechanicSpecialization.ENGINE));
        order.approve();

        service.startOrderExecution(1L, mech);

        assertEquals(mech, order.getMechanic());
        assertEquals(OrderStatus.IN_PROGRESS, order.getStatus());
        verify(repository, times(1)).save(order);
    }

    @Test
    @DisplayName("Service: complete order calculates price and updates state")
    void completeOrder_logic() {
        when(repository.findById(1L)).thenReturn(Optional.of(order));
        when(calculator.calculateCommercialPrice(order)).thenReturn(new BigDecimal("1000.00"));
        order.addJob(new Job("Test Job", "J", BigDecimal.ONE, BigDecimal.ONE,
                MechanicSpecialization.ENGINE));
        order.approve();
        order.assignMechanic(new Mechanic("M1", MechanicSpecialization.ENGINE));
        order.startExecution();

        service.completeOrder(1L);

        assertEquals(OrderStatus.COMPLETED, order.getStatus());
        assertEquals(new BigDecimal("1000.00"), order.getFinalCommercialPrice());
        verify(repository, times(1)).save(order);
    }

    @Test
    @DisplayName("Service: getOrderById throws exception if not found")
    void getOrderById_notFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(DomainException.class, () -> service.getOrderById(1L));
    }
}
