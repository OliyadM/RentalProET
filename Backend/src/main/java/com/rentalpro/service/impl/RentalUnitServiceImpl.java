package com.rentalpro.service.impl;

import com.rentalpro.model.dto.response.RentalUnitResponse;
import com.rentalpro.model.entity.Property;
import com.rentalpro.model.entity.RentalUnit;
import com.rentalpro.repository.PropertyRepository;
import com.rentalpro.repository.RentalUnitRepository;
import com.rentalpro.service.AuditLogService;
import com.rentalpro.service.RentalUnitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RentalUnitServiceImpl implements RentalUnitService {

    private final RentalUnitRepository unitRepository;
    private final PropertyRepository propertyRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public RentalUnitResponse createUnit(UUID propertyId, RentalUnit unit, UUID landlordId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));

        if (!property.getOwner().getId().equals(landlordId)) {
            throw new RuntimeException("Not authorized to add units to this property");
        }

        // Guard: Only ACTIVE properties can have units added
        if (property.getStatus() != com.rentalpro.model.enums.PropertyStatus.ACTIVE) {
            throw new RuntimeException("Units can only be added to active properties. This property is pending officer review.");
        }

        unit.setProperty(property);
        RentalUnit saved = unitRepository.save(unit);

        auditLogService.logAction("CREATE_UNIT", "RentalUnit", saved.getId(),
                property.getOwner(), "Created unit " + saved.getUnitNumber());

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RentalUnitResponse> getUnitsByProperty(UUID propertyId) {
        return unitRepository.findByPropertyId(propertyId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RentalUnitResponse getUnitById(UUID id) {
        RentalUnit unit = unitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Unit not found"));
        return mapToResponse(unit);
    }

    private RentalUnitResponse mapToResponse(RentalUnit unit) {
        return RentalUnitResponse.builder()
                .id(unit.getId())
                .propertyId(unit.getProperty().getId())
                .propertyName(unit.getProperty().getPropertyName())
                .unitNumber(unit.getUnitNumber())
                .floorArea(unit.getFloorArea())
                .floorLevel(unit.getFloorLevel())
                .numberOfRooms(unit.getNumberOfRooms())
                .hasParking(unit.getHasParking())
                .hasElevator(unit.getHasElevator())
                .amenities(unit.getAmenities())
                .createdAt(unit.getCreatedAt())
                .build();
    }
}