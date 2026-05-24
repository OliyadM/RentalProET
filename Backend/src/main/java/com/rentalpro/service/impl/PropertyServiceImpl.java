package com.rentalpro.service.impl;

import com.rentalpro.model.dto.response.PropertyResponse;
import com.rentalpro.model.entity.Property;
import com.rentalpro.model.entity.User;
import com.rentalpro.repository.PropertyRepository;
import com.rentalpro.repository.UserRepository;
import com.rentalpro.service.PropertyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PropertyServiceImpl implements PropertyService {

    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Property createProperty(UUID ownerId, Property property) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        // Guard: Only VERIFIED users can create properties
        if (owner.getAccountStatus() != com.rentalpro.model.enums.AccountStatus.VERIFIED) {
            throw new RuntimeException("Your account must be verified before you can register properties. Please complete your profile and wait for verification.");
        }

        property.setOwner(owner);
        property.setIsVerified(false);
        return propertyRepository.save(property);
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
                .isVerified(property.getIsVerified())
                .ownerName(property.getOwner().getFirstName() + " " + property.getOwner().getLastName())
                .createdAt(property.getCreatedAt())
                .build();
    }
    @Transactional
    @Override
    public PropertyResponse verifyProperty(UUID id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Property not found"));

        property.setIsVerified(true);
        Property savedProperty = propertyRepository.save(property);

        return mapToResponse(savedProperty);
    }
}