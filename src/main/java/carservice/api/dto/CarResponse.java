package carservice.api.dto;

public record CarResponse(Long id, String licensePlate, String brand, String model,
                          int productionYear, Long ownerId) {
}
