package com.rentalpro.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RentDeclarationResponse {
    private UUID id;
    private UUID contractId;
    private LocalDate declarationPeriod;
    private Double declaredRent;
    private Double aiBenchmarkRent;
    private Double anomalyScore;
    private Boolean isAnomaly;
    private String anomalyReason;
    // Enhanced benchmark metadata
    private Double benchmarkPricePerM2;
    private Double benchmarkLowerBound;
    private Double benchmarkUpperBound;
    private Integer benchmarkSampleSize;
    private Integer benchmarkFallbackLevel;
    private Double benchmarkStdDev;
    private String anomalySeverity;
    private String anomalyDirection;
    private Double estimatedTax;
    private Boolean claimDeduction;
    private Boolean deductionApplied;
    private Double deductionAmount;
    private Double taxableAnnualIncome;
    private Double annualTax;
    private Double effectiveTaxRate;
    private String taxRuleVersion;
    private Boolean mixedUseDeductionWarning;
    private String taxAdvisoryNote;
    /** Full tax breakdown for landlord/officer views. */
    private TaxCalculationResponse taxDetails;
    private Boolean isVerified;
    private LocalDateTime createdAt;
}