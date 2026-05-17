package com.rentalpro.service;

import com.rentalpro.model.dto.response.PropertyResponse;
import com.rentalpro.model.entity.Property;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface PropertyService {

    Property createProperty(UUID ownerId, Property property);

    List<PropertyResponse> getPropertiesByOwner(UUID ownerId);

    List<PropertyResponse> getPropertiesBySubCity(String subCity);

    PropertyResponse getPropertyById(UUID id);

    @Transactional
    PropertyResponse verifyProperty(UUID id);
}