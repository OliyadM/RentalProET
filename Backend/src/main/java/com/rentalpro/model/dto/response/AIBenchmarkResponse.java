package com.rentalpro.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIBenchmarkResponse {
    private Double suggestedRent;
    private Double minRent;
    private Double maxRent;
    private Double confidenceScore;
    private String marketTrend; // RISING, STABLE, DECLINING
    private String reasoning;
}