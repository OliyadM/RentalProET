package com.rentalpro.model.dto.response;

import com.rentalpro.model.enums.PropertyStatus;
import com.rentalpro.model.enums.PropertyType;
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
public class PropertyResponse {
    private UUID id;
    private String propertyName;
    private String address;
    private String subCity;
    private String woreda;
    private PropertyType propertyType;
    private Double latitude;
    private Double longitude;
    private Double totalArea;
    private Integer yearBuilt;
    private PropertyStatus status;
    private String ownerName;  // Just the name, not the full object
    private LocalDateTime createdAt;
}