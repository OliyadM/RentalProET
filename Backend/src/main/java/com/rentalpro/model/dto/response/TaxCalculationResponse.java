package com.rentalpro.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxCalculationResponse {

    private Double monthlyGrossRent;
    private Double annualGrossRent;

    private Boolean deductionApplied;
    private Double deductionPercent;
    private Double deductionAmount;

    private Double taxableAnnualIncome;
    private Double annualTax;
    private Double monthlyTax;
    private Double effectiveTaxRate;

    private String taxRuleVersion;
    private LocalDate ruleEffectiveDate;

    private String appliedBandLabel;
    private Double appliedBandRate;
    private Double appliedBandDeductible;

    private Boolean mixedUseDeductionWarning;
    private List<String> advisoryNotes;
}
