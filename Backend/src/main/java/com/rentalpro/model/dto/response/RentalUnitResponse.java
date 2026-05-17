package com.rentalpro.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RentalUnitResponse {
    private UUID id;
    private UUID propertyId;
    private String propertyName;
    private String unitNumber;
    private Double floorArea;
    private Integer floorLevel;
    private Integer numberOfRooms;
    private Boolean hasParking;
    private Boolean hasElevator;
    private String amenities;
    private LocalDateTime createdAt;
}