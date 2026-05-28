package com.rentalpro.model.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class SystemConfigRequest {

    // ── Platform settings ────────────────────────────────────────────────────

    @NotNull @DecimalMin("1.0") @DecimalMax("100.0")
    private Double anomalyThresholdPercent;

    @NotNull @DecimalMin("0.0") @DecimalMax("100.0")
    private Double maxRentIncreaseCapPercent;

    @NotNull @Min(1) @Max(10)
    private Integer minimumContractYears;

    // ── Tax rule metadata ────────────────────────────────────────────────────

    @NotBlank
    private String taxRuleVersion;

    // ── Business flat rate ───────────────────────────────────────────────────

    @NotNull @DecimalMin("0.0") @DecimalMax("100.0")
    private Double businessFlatTaxRatePercent;   // sent as % e.g. 30.0

    // ── Individual residential deduction ────────────────────────────────────

    @NotNull @DecimalMin("0.0") @DecimalMax("100.0")
    private Double residentialDeductionPercent;  // sent as % e.g. 40.0

    // ── Progressive tax bands (exactly 6) ───────────────────────────────────

    @NotNull @Size(min = 6, max = 6, message = "Exactly 6 tax bands are required")
    @Valid
    private List<TaxBandRequest> taxBands;

    @Data
    public static class TaxBandRequest {
        @NotNull private Double minIncome;   // ETB annual
        @NotNull private Double maxIncome;   // ETB annual (use 999999999 for "above" band)
        @NotNull @DecimalMin("0.0") @DecimalMax("100.0") private Double ratePercent;
        @NotNull @DecimalMin("0.0") private Double deductibleAmount;
    }
}
