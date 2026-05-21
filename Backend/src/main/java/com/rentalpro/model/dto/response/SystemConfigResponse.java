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
public class SystemConfigResponse {
    private UUID id;
    /** Tax rate as a percentage value for display: 0.10 → 10.0 */
    private Double taxRatePercent;
    /** Anomaly threshold as a percentage value for display: 0.15 → 15.0 */
    private Double anomalyThresholdPercent;
    /** Max rent increase cap as a percentage value for display: 0.10 → 10.0 */
    private Double maxRentIncreaseCapPercent;
    private LocalDateTime updatedAt;
}
