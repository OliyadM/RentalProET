package com.rentalpro.service.impl;

import com.rentalpro.model.dto.response.PropertyResponse;
import com.rentalpro.model.entity.Property;
import com.rentalpro.model.entity.User;
import com.rentalpro.model.enums.AccountStatus;
import com.rentalpro.model.enums.NotificationType;
import com.rentalpro.model.enums.PropertyStatus;
import com.rentalpro.repository.PropertyRepository;
import com.rentalpro.repository.UserRepository;
import com.rentalpro.service.NotificationService;
import com.rentalpro.service.PropertyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PropertyServiceImpl implements PropertyService {

    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public Property createProperty(UUID ownerId, Property property) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        if (owner.getAccountStatus() != AccountStatus.VERIFIED) {
            throw new RuntimeException("Your account must be verified before you can register properties. Please complete your profile and wait for verification.");
        }

        property.setOwner(owner);
        property.setStatus(PropertyStatus.PENDING_OFFICER_REVIEW);
        Property saved = propertyRepository.saveAndFlush(property);

        // Notify officers in the property's sub-city that a new property needs review
        String subCity = saved.getSubCity();
        if (subCity != null && !subCity.isBlank()) {
            try {
                String ownerName = owner.getFirstName() + " " + owner.getLastName();
                String msg = String.format(
                        "New property \"%s\" at %s has been submitted by %s and requires officer review.",
                        saved.getPropertyName(), saved.getAddress(), ownerName);
                notificationService.sendToSubCityOfficers(
                        subCity,
                        NotificationType.PROPERTY_PENDING_REVIEW,
                        msg,
                        saved.getId());
            } catch (Exception e) {
                log.warn("Failed to notify officers of new property {}: {}", saved.getId(), e.getMessage());
            }
        }

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PropertyResponse> getPropertiesByOwner(UUID ownerId) {
        return propertyRepository.findByOwnerId(ownerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PropertyResponse> getPropertiesBySubCity(String subCity) {
        return propertyRepository.findBySubCity(subCity).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PropertyResponse getPropertyById(UUID id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Property not found"));
        return mapToResponse(property);
    }

    @Transactional
    @Override
    public PropertyResponse verifyProperty(UUID id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Property not found"));

        property.setStatus(PropertyStatus.ACTIVE);
        Property saved = propertyRepository.saveAndFlush(property);

        // Notify the landlord that their property has been approved
        try {
            String msg = String.format(
                    "Your property \"%s\" at %s has been verified and is now active.",
                    saved.getPropertyName(), saved.getAddress());
            notificationService.send(
                    saved.getOwner().getId(),
                    NotificationType.PROPERTY_VERIFIED,
                    msg,
                    saved.getId());
        } catch (Exception e) {
            log.warn("Failed to notify landlord of property verification {}: {}", saved.getId(), e.getMessage());
        }

        return mapToResponse(saved);
    }

    private PropertyResponse mapToResponse(Property property) {
        return PropertyResponse.builder()
                .id(property.getId())
                .propertyName(property.getPropertyName())
                .address(property.getAddress())
                .subCity(property.getSubCity())
                .woreda(property.getWoreda())
                .propertyType(property.getPropertyType())
                .latitude(property.getLatitude())
                .longitude(property.getLongitude())
                .totalArea(property.getTotalArea())
                .yearBuilt(property.getYearBuilt())
                .status(property.getStatus())
                .ownerName(property.getOwner().getFirstName() + " " + property.getOwner().getLastName())
                .createdAt(property.getCreatedAt())
                .build();
    }
}