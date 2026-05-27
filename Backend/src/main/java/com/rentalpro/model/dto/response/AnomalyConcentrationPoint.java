package com.rentalpro.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnomalyConcentrationPoint {
    private Double latitude;
    private Double longitude;
    private String subCity;
    private Long anomalyCount;
    private Double averageSeverity;
    private Long totalDeclarations;
    private Boolean hasExactGPS;
}
