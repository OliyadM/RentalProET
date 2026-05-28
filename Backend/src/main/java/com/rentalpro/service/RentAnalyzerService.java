package com.rentalpro.service;

import com.rentalpro.model.dto.response.AIBenchmarkResponse;

import java.util.UUID;

public interface RentAnalyzerService {

    /**
     * Calculate fair rent benchmark for a specific unit using price-per-m² algorithm.
     * Falls back to property-level estimate if unit has no floor area.
     */
    AIBenchmarkResponse calculateFairRent(UUID propertyId, UUID unitId);

    /**
     * Legacy overload — used by AnalyticsController which only has propertyId.
     * Uses property-level estimate without unit normalization.
     */
    AIBenchmarkResponse calculateFairRent(UUID propertyId);
}
