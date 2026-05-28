package com.rentalpro.service.impl;

import com.rentalpro.model.dto.response.TaxCalculationResponse;
import com.rentalpro.model.entity.SystemConfig;
import com.rentalpro.model.enums.EntityType;
import com.rentalpro.model.enums.PropertyType;
import com.rentalpro.service.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaxCalculationServiceTest {

    @Mock
    private AdminService adminService;

    private TaxCalculationServiceImpl taxService;

    /** Default SystemConfig matching Proclamation 1395/2025 — same values as @Builder.Default. */
    private static SystemConfig defaultConfig() {
        return SystemConfig.builder()
                .taxRuleVersion("1395/2025")
                .businessFlatTaxRate(0.30)
                .residentialDeductionPercent(0.40)
                .band1Min(0.0)      .band1Max(24_000.0)  .band1Rate(0.00) .band1Deductible(0.0)
                .band2Min(24_001.0) .band2Max(48_000.0)  .band2Rate(0.10) .band2Deductible(2_400.0)
                .band3Min(48_001.0) .band3Max(78_000.0)  .band3Rate(0.20) .band3Deductible(6_000.0)
                .band4Min(78_001.0) .band4Max(120_000.0) .band4Rate(0.25) .band4Deductible(9_900.0)
                .band5Min(120_001.0).band5Max(168_000.0) .band5Rate(0.30) .band5Deductible(15_900.0)
                .band6Min(168_001.0).band6Max(Double.MAX_VALUE).band6Rate(0.30).band6Deductible(15_900.0)
                .anomalyThresholdPercentage(0.15)
                .maxRentIncreaseCap(0.10)
                .minimumContractYears(2)
                .build();
    }

    @BeforeEach
    void setUp() {
        // lenient: some tests (negativeRent, resolveBand) don't invoke the service
        // or use the static method, so the stub would be flagged as unnecessary otherwise
        org.mockito.Mockito.lenient()
                .when(adminService.getConfigEntity()).thenReturn(defaultConfig());
        taxService = new TaxCalculationServiceImpl(adminService);
    }

    // ── Given / When / Then: verified 1395/2025 examples ─────────────────────

    @Test
    void calculate_individual_taxable60000_noDeduction_matchesOfficialExample() {
        // 5,000 ETB/month → 60,000 annual gross, no deduction → taxable = 60,000
        // Band 3: (60,000 × 20%) − 6,000 = 6,000 ETB annual tax
        TaxCalculationResponse result = taxService.calculate(
                5_000, EntityType.INDIVIDUAL, PropertyType.HOUSE, false);

        assertEquals(60_000, result.getAnnualGrossRent());
        assertEquals(60_000, result.getTaxableAnnualIncome());
        assertFalse(result.getDeductionApplied());
        assertEquals(6_000, result.getAnnualTax());
        assertEquals(500, result.getMonthlyTax());
        assertEquals(0.10, result.getEffectiveTaxRate());
    }

    @Test
    void calculate_individual_annual24000_orLess_isTaxFree() {
        TaxCalculationResponse result = taxService.calculate(
                2_000, EntityType.INDIVIDUAL, PropertyType.HOUSE, false);

        assertEquals(24_000, result.getAnnualGrossRent());
        assertEquals(0, result.getAnnualTax());
        assertEquals(0, result.getMonthlyTax());
    }

    @Test
    void calculate_individual_with40PercentDeduction_reducesTaxableBase() {
        // 10,000/month → 120,000 gross, 40% deduction → 72,000 taxable
        // Band 3: (72,000 × 20%) − 6,000 = 8,400
        TaxCalculationResponse result = taxService.calculate(
                10_000, EntityType.INDIVIDUAL, PropertyType.APARTMENT_BUILDING, true);

        assertTrue(result.getDeductionApplied());
        assertEquals(0.40, result.getDeductionPercent());
        assertEquals(48_000, result.getDeductionAmount());
        assertEquals(72_000, result.getTaxableAnnualIncome());
        assertEquals(8_400, result.getAnnualTax());
    }

    @Test
    void calculate_business_flat30Percent_noDeductionOption() {
        TaxCalculationResponse result = taxService.calculate(
                5_000, EntityType.BUSINESS, PropertyType.COMMERCIAL_BUILDING, false);

        assertEquals(60_000, result.getAnnualGrossRent());
        assertFalse(result.getDeductionApplied());
        assertEquals(18_000, result.getAnnualTax());
        assertEquals(1_500, result.getMonthlyTax());
        assertEquals(0.30, result.getEffectiveTaxRate());
    }

    @Test
    void calculate_business_ignoresDeductionClaimEvenIfTrue() {
        TaxCalculationResponse result = taxService.calculate(
                5_000, EntityType.BUSINESS, PropertyType.HOUSE, true);

        assertFalse(result.getDeductionApplied());
        assertEquals(18_000, result.getAnnualTax());
    }

    @Test
    void calculate_mixedUse_withDeduction_appliesFullDeductionAndWarning() {
        TaxCalculationResponse result = taxService.calculate(
                10_000, EntityType.INDIVIDUAL, PropertyType.MIXED_USE_BUILDING, true);

        assertTrue(result.getDeductionApplied());
        assertTrue(result.getMixedUseDeductionWarning());
        assertTrue(result.getAdvisoryNotes().stream()
                .anyMatch(n -> n.contains("officer should verify commercial split")));
    }

    @Test
    void calculate_commercialProperty_individualCannotClaimResidentialDeduction() {
        TaxCalculationResponse result = taxService.calculate(
                5_000, EntityType.INDIVIDUAL, PropertyType.COMMERCIAL_BUILDING, true);

        assertFalse(result.getDeductionApplied());
        assertEquals(60_000, result.getTaxableAnnualIncome());
        assertEquals(6_000, result.getAnnualTax());
    }

    @Test
    void calculate_includesAggregateAdvisoryNote() {
        TaxCalculationResponse result = taxService.calculate(
                5_000, EntityType.INDIVIDUAL, PropertyType.HOUSE, false);

        assertTrue(result.getAdvisoryNotes().stream()
                .anyMatch(n -> n.toLowerCase().contains("rental income only")));
    }

    @Test
    void calculate_includesRuleVersionAndEffectiveDate() {
        TaxCalculationResponse result = taxService.calculate(
                5_000, EntityType.INDIVIDUAL, PropertyType.HOUSE, false);

        assertEquals("1395/2025", result.getTaxRuleVersion());
        assertNotNull(result.getRuleEffectiveDate());
    }

    @Test
    void calculate_negativeRent_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                taxService.calculate(-100, EntityType.INDIVIDUAL, PropertyType.HOUSE, false));
    }

    @Test
    void resolveBand_picksCorrectBandForBoundaries() {
        // Static method still works with hardcoded TaxRule1395 for backward compat
        assertEquals("0 – 24,000 ETB",
                TaxCalculationServiceImpl.resolveBand(24_000).getLabel());
        assertEquals("24,001 – 48,000 ETB",
                TaxCalculationServiceImpl.resolveBand(24_001).getLabel());
        assertEquals("Above 168,000 ETB",
                TaxCalculationServiceImpl.resolveBand(200_000).getLabel());
    }
}
