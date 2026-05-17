package com.rentalpro.service;

import com.rentalpro.model.dto.response.RentalUnitResponse;
import com.rentalpro.model.entity.RentalUnit;

import java.util.List;
import java.util.UUID;

public interface RentalUnitService {

    RentalUnitResponse createUnit(UUID propertyId, RentalUnit unit, UUID landlordId);

    List<RentalUnitResponse> getUnitsByProperty(UUID propertyId);

    RentalUnitResponse getUnitById(UUID id);
}