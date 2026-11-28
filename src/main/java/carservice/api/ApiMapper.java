package carservice.api;

import carservice.api.dto.CarResponse;
import carservice.api.dto.ClientResponse;
import carservice.api.dto.JobResponse;
import carservice.api.dto.MechanicResponse;
import carservice.api.dto.OrderResponse;
import carservice.domain.Car;
import carservice.domain.Client;
import carservice.domain.Job;
import carservice.domain.Mechanic;
import carservice.domain.RepairOrder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ApiMapper {

    public ClientResponse toResponse(final Client client) {
        return new ClientResponse(client.getId(), client.getName(), client.getPhone());
    }

    public CarResponse toResponse(final Car car) {
        return new CarResponse(car.getId(), car.getLicensePlate(), car.getBrand(), car.getModel(),
                car.getProductionYear(), car.getOwner().getId());
    }

    public MechanicResponse toResponse(final Mechanic mechanic) {
        return toResponse(mechanic, mechanic.isBusy());
    }

    public MechanicResponse toResponse(final Mechanic mechanic, final boolean busy) {
        return new MechanicResponse(mechanic.getId(), mechanic.getName(), mechanic.getSpecialization(), busy);
    }

    public JobResponse toResponse(final Job job) {
        return new JobResponse(job.getId(), job.getName(), job.getDescription(), job.getPartsCost(),
                job.getLaborCost(), job.getBasePrice(), job.getCommercialPrice(),
                job.getRequiredSpecialization(), job.getStatus());
    }

    public OrderResponse toResponse(final RepairOrder order) {
        return new OrderResponse(
                order.getId(),
                order.getCar().getId(),
                order.getMechanic() == null ? null : order.getMechanic().getId(),
                order.getPriority(),
                order.getStatus(),
                order.getPlannedStart(),
                order.getPlannedEnd(),
                order.getFinalCommercialPrice(),
                order.getJobs().stream().map(this::toResponse).toList());
    }

    public List<JobResponse> toJobResponses(final List<Job> jobs) {
        return jobs.stream().map(this::toResponse).toList();
    }
}
