package carservice.repository;

import carservice.domain.RepairOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import carservice.domain.enumeration.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Collection;

@Repository
public interface RepairOrderRepository extends JpaRepository<RepairOrder, Long> {
    Page<RepairOrder> findAllByStatus(OrderStatus status, Pageable pageable);
    boolean existsByMechanicIdAndStatusIn(Long mechanicId, Collection<OrderStatus> statuses);
}
