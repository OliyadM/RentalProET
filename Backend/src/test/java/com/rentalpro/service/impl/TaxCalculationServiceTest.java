package com.rentalpro.service.impl;

import com.rentalpro.model.enums.EntityType;
import com.rentalpro.model.enums.PropertyType;
import com.rentalpro.model.dto.response.TaxCalculationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaxCalculationServiceTest {

    private TaxCalculationServiceImpl taxService;

    @BeforeEach
    void setUp() {
        taxService = new TaxCalculationServiceImpl();
    }

    // ── Given / When / Then: verified 1395/2025 example ───────────────────────

    @Test
    void calculate_individual_taxable60000_noDeduction_matchesOfficialExample() {
        // Given: 5,000 ETB/month → 60,000 annual gross, no deduction → taxable = 60,000
        // When
        TaxCalculationResponse result = taxService.calculate(
                5_000, EntityType.INDIVIDUAL, PropertyType.HOUSE, false);

        // Then: (60,000 × 20%) − 6,000 = 6,000 ETB annual tax
        assertEquals(60_000, result.getAnnualGrossRent());
        assertEquals(60_000, result.getTaxableAnnualIncome());
        assertFalse(result.getDeductionApplied());
        assertEquals(6_000, result.getAnnualTax());
        assertEquals(500, result.getMonthlyTax());
        assertEquals(0.10, result.getEffectiveTaxRate());
        assertEquals("48,001 – 78,000 ETB", result.getAppliedBandLabel());
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
        // Given: 10,000/month → 120,000 gross, 40% deduction → 72,000 taxable
        TaxCalculationResponse result = taxService.calculate(
                10_000, EntityType.INDIVIDUAL, PropertyType.APARTMENT_BUILDING, true);

        assertTrue(result.getDeductionApplied());
        assertEquals(0.40, result.getDeductionPercent());
        assertEquals(48_000, result.getDeductionAmount());
        assertEquals(72_000, result.getTaxableAnnualIncome());
        // (72,000 × 20%) − 6,000 = 8,400
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
        assertEquals("Flat 30% (Business)", result.getAppliedBandLabel());
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
        assertEquals("0 – 24,000 ETB",
                TaxCalculationServiceImpl.resolveBand(24_000).getLabel());
        assertEquals("24,001 – 48,000 ETB",
                TaxCalculationServiceImpl.resolveBand(24_001).getLabel());
        assertEquals("Above 168,000 ETB",
                TaxCalculationServiceImpl.resolveBand(200_000).getLabel());
    }
}
