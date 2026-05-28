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

    // ── Price-per-m² benchmark metadata ──────────────────────────────────────
    private Double pricePerSqm;        // ETB/m² used as basis
    private Double stdDev;             // standard deviation of comparison group
    private Integer sampleSize;        // number of comparable records used
    private Integer fallbackLevel;     // 1–6: which filter level was used
    private Double unitFloorArea;      // m² of the unit being benchmarked
}
