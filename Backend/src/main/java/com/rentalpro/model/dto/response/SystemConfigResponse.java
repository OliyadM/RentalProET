package com.rentalpro.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfigResponse {

    private UUID id;

    // ── Platform settings ────────────────────────────────────────────────────
    private Double anomalyThresholdPercent;
    private Double maxRentIncreaseCapPercent;
    private Integer minimumContractYears;

    // ── Tax rule metadata ────────────────────────────────────────────────────
    private String taxRuleVersion;

    // ── Business flat rate (as %) ────────────────────────────────────────────
    private Double businessFlatTaxRatePercent;

    // ── Individual residential deduction (as %) ──────────────────────────────
    private Double residentialDeductionPercent;

    // ── Progressive tax bands ────────────────────────────────────────────────
    private List<TaxBandResponse> taxBands;

    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaxBandResponse {
        private Double minIncome;
        private Double maxIncome;
        private Double ratePercent;
        private Double deductibleAmount;
        private String label;
    }
}
