package carservice.api.dto;

import carservice.domain.enumeration.MechanicSpecialization;

public record MechanicResponse(Long id, String name, MechanicSpecialization specialization, boolean busy) {
}
