package com.rentalpro.model.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SystemConfigRequest {

    /**
     * Tax rate as a percentage (e.g. 10.0 means 10%).
     * Accepted range: 0% – 50%.
     */
    @NotNull(message = "Tax rate is required")
    @DecimalMin(value = "0.0",  message = "Tax rate cannot be negative")
    @DecimalMax(value = "50.0", message = "Tax rate cannot exceed 50%")
    private Double taxRatePercent;

    /**
     * Anomaly detection threshold as a percentage (e.g. 15.0 means 15%).
     * Accepted range: 1% – 100%.
     */
    @NotNull(message = "Anomaly threshold is required")
    @DecimalMin(value = "1.0",   message = "Anomaly threshold must be at least 1%")
    @DecimalMax(value = "100.0", message = "Anomaly threshold cannot exceed 100%")
    private Double anomalyThresholdPercent;

    /**
     * Maximum rent increase cap as a percentage (e.g. 10.0 means 10%).
     * Accepted range: 0% – 100%.
     */
    @NotNull(message = "Max rent increase cap is required")
    @DecimalMin(value = "0.0",   message = "Rent increase cap cannot be negative")
    @DecimalMax(value = "100.0", message = "Rent increase cap cannot exceed 100%")
    private Double maxRentIncreaseCapPercent;
}
