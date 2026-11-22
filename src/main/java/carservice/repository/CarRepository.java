package carservice.repository;

import carservice.domain.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {
    boolean existsByLicensePlateIgnoreCase(String licensePlate);
}
