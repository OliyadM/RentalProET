package com.rentalpro.service;

import com.rentalpro.model.dto.response.AnomalyConcentrationPoint;
import com.rentalpro.model.dto.response.RentDensityPoint;
import com.rentalpro.model.enums.PropertyType;

import java.time.LocalDate;
import java.util.List;

public interface GISAnalyticsService {
    
    /**
     * Get rent density heatmap data
     * @param propertyType Optional filter by property type
     * @param subCity Optional filter by sub-city
     * @param startDate Optional filter by contract start date
     * @param endDate Optional filter by contract end date
     * @return List of rent density points for mapping
     */
    List<RentDensityPoint> getRentDensityHeatmap(
        PropertyType propertyType,
        String subCity,
        LocalDate startDate,
        LocalDate endDate
    );
    
    /**
     * Get anomaly concentration heatmap data
     * @param minSeverity Optional minimum anomaly severity (0.0 - 1.0)
     * @param subCity Optional filter by sub-city
     * @param startDate Optional filter by declaration period
     * @param endDate Optional filter by declaration period
     * @return List of anomaly concentration points for mapping
     */
    List<AnomalyConcentrationPoint> getAnomalyConcentration(
        Double minSeverity,
        String subCity,
        LocalDate startDate,
        LocalDate endDate
    );
}
