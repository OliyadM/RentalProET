package com.rentalpro.service.impl;

import com.rentalpro.model.dto.response.TaxCalculationResponse;
import com.rentalpro.model.enums.EntityType;
import com.rentalpro.model.enums.PropertyType;
import com.rentalpro.model.tax.TaxBand;
import com.rentalpro.model.tax.TaxRule1395;
import com.rentalpro.service.TaxCalculationService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TaxCalculationServiceImpl implements TaxCalculationService {

    private static final String AGGREGATE_ADVISORY =
            "This estimate covers rental income only. Total tax liability may differ if the landlord has other income sources.";

    private static final String MIXED_USE_WARNING =
            "Residential deduction applied — officer should verify commercial split.";

    @Override
    public TaxCalculationResponse calculate(
            double monthlyRent,
            EntityType entityType,
            PropertyType propertyType,
            boolean claimDeduction) {

        if (monthlyRent < 0) {
            throw new IllegalArgumentException("Monthly rent cannot be negative");
        }

        double annualGross = round(monthlyRent * 12);
        List<String> advisoryNotes = new ArrayList<>();
        advisoryNotes.add(AGGREGATE_ADVISORY);

        boolean mixedUseWarning = false;
        double deductionAmount = 0;
        boolean deductionApplied = false;
        double deductionPercent = 0;

        if (entityType == EntityType.BUSINESS) {
            double annualTax = round(annualGross * TaxRule1395.BUSINESS_FLAT_RATE);
            return buildResponse(
                    monthlyRent, annualGross, false, 0, 0,
                    annualGross, annualTax, entityType,
                    "Flat 30% (Business)", TaxRule1395.BUSINESS_FLAT_RATE, 0,
                    mixedUseWarning, advisoryNotes
            );
        }

        if (entityType == EntityType.INDIVIDUAL && claimDeduction && isDeductionEligible(propertyType)) {
            deductionPercent = TaxRule1395.RESIDENTIAL_DEDUCTION_PERCENT;
            deductionAmount = round(annualGross * deductionPercent);
            deductionApplied = true;
            if (propertyType == PropertyType.MIXED_USE_BUILDING) {
                mixedUseWarning = true;
                advisoryNotes.add(MIXED_USE_WARNING);
            }
        }

        double taxableAnnual = round(Math.max(0, annualGross - deductionAmount));
        TaxBand band = resolveBand(taxableAnnual);
        double annualTax = round(Math.max(0, taxableAnnual * band.getRate() - band.getDeductibleAmount()));

        return buildResponse(
                monthlyRent, annualGross, deductionApplied, deductionPercent, deductionAmount,
                taxableAnnual, annualTax, entityType,
                band.getLabel(), band.getRate(), band.getDeductibleAmount(),
                mixedUseWarning, advisoryNotes
        );
    }

    static boolean isDeductionEligible(PropertyType propertyType) {
        return propertyType == PropertyType.HOUSE
                || propertyType == PropertyType.APARTMENT_BUILDING
                || propertyType == PropertyType.MIXED_USE_BUILDING;
    }

    static TaxBand resolveBand(double taxableAnnualIncome) {
        for (TaxBand band : TaxRule1395.INDIVIDUAL_BANDS) {
            if (band.contains(taxableAnnualIncome)) {
                return band;
            }
        }
        return TaxRule1395.INDIVIDUAL_BANDS.get(TaxRule1395.INDIVIDUAL_BANDS.size() - 1);
    }

    private TaxCalculationResponse buildResponse(
            double monthlyRent,
            double annualGross,
            boolean deductionApplied,
            double deductionPercent,
            double deductionAmount,
            double taxableAnnual,
            double annualTax,
            EntityType entityType,
            String bandLabel,
            double bandRate,
            double bandDeductible,
            boolean mixedUseWarning,
            List<String> advisoryNotes) {

        double effectiveRate = annualGross > 0 ? round(annualTax / annualGross) : 0;

        return TaxCalculationResponse.builder()
                .monthlyGrossRent(round(monthlyRent))
                .annualGrossRent(annualGross)
                .deductionApplied(deductionApplied)
                .deductionPercent(deductionApplied ? deductionPercent : null)
                .deductionAmount(deductionApplied ? deductionAmount : null)
                .taxableAnnualIncome(taxableAnnual)
                .annualTax(annualTax)
                .monthlyTax(round(annualTax / 12))
                .effectiveTaxRate(effectiveRate)
                .taxRuleVersion(TaxRule1395.VERSION)
                .ruleEffectiveDate(TaxRule1395.EFFECTIVE_DATE)
                .appliedBandLabel(entityType == EntityType.BUSINESS ? bandLabel : bandLabel)
                .appliedBandRate(entityType == EntityType.BUSINESS ? bandRate : bandRate)
                .appliedBandDeductible(entityType == EntityType.BUSINESS ? null : bandDeductible)
                .mixedUseDeductionWarning(mixedUseWarning)
                .advisoryNotes(advisoryNotes)
                .build();
    }

    static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
