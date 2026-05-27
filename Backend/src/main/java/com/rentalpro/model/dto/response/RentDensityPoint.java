package com.rentalpro.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RentDensityPoint {
    private Double latitude;
    private Double longitude;
    private String subCity;
    private Double averageRent;
    private Long propertyCount;
    private Boolean hasExactGPS;
}
