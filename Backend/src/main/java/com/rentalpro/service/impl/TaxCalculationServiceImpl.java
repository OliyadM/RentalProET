package com.rentalpro.service.impl;

import com.rentalpro.model.dto.response.TaxCalculationResponse;
import com.rentalpro.model.entity.SystemConfig;
import com.rentalpro.model.enums.EntityType;
import com.rentalpro.model.enums.PropertyType;
import com.rentalpro.model.tax.TaxBand;
import com.rentalpro.model.tax.TaxRule1395;
import com.rentalpro.service.AdminService;
import com.rentalpro.service.TaxCalculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaxCalculationServiceImpl implements TaxCalculationService {

    private final AdminService adminService;

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

        if (monthlyRent < 0) throw new IllegalArgumentException("Monthly rent cannot be negative");

        SystemConfig cfg = adminService.getConfigEntity();
        double annualGross = round(monthlyRent * 12);
        List<String> advisoryNotes = new ArrayList<>();
        advisoryNotes.add(AGGREGATE_ADVISORY);

        boolean mixedUseWarning = false;
        double deductionAmount = 0;
        boolean deductionApplied = false;
        double deductionPercent = 0;

        if (entityType == EntityType.BUSINESS) {
            double rate = cfg.getBusinessFlatTaxRate();
            double annualTax = round(annualGross * rate);
            return buildResponse(
                    monthlyRent, annualGross, false, 0, 0,
                    annualGross, annualTax, entityType,
                    String.format("Flat %.0f%% (Business)", rate * 100), rate, 0,
                    false, advisoryNotes, cfg.getTaxRuleVersion());
        }

        if (entityType == EntityType.INDIVIDUAL && claimDeduction && isDeductionEligible(propertyType)) {
            deductionPercent = cfg.getResidentialDeductionPercent();
            deductionAmount = round(annualGross * deductionPercent);
            deductionApplied = true;
            if (propertyType == PropertyType.MIXED_USE_BUILDING) {
                mixedUseWarning = true;
                advisoryNotes.add(MIXED_USE_WARNING);
            }
        }

        double taxableAnnual = round(Math.max(0, annualGross - deductionAmount));
        TaxBand band = resolveBandFromConfig(taxableAnnual, cfg);
        double annualTax = round(Math.max(0, taxableAnnual * band.getRate() - band.getDeductibleAmount()));

        return buildResponse(
                monthlyRent, annualGross, deductionApplied, deductionPercent, deductionAmount,
                taxableAnnual, annualTax, entityType,
                band.getLabel(), band.getRate(), band.getDeductibleAmount(),
                mixedUseWarning, advisoryNotes, cfg.getTaxRuleVersion());
    }

    static boolean isDeductionEligible(PropertyType propertyType) {
        return propertyType == PropertyType.HOUSE
                || propertyType == PropertyType.APARTMENT_BUILDING
                || propertyType == PropertyType.MIXED_USE_BUILDING;
    }

    /** Build the 6 TaxBand objects from the live SystemConfig. */
    public static List<TaxBand> getBandsFromConfig(SystemConfig cfg) {
        return List.of(
            new TaxBand(cfg.getBand1Min(), cfg.getBand1Max(), cfg.getBand1Rate(), cfg.getBand1Deductible(), bandLabel(cfg.getBand1Min(), cfg.getBand1Max())),
            new TaxBand(cfg.getBand2Min(), cfg.getBand2Max(), cfg.getBand2Rate(), cfg.getBand2Deductible(), bandLabel(cfg.getBand2Min(), cfg.getBand2Max())),
            new TaxBand(cfg.getBand3Min(), cfg.getBand3Max(), cfg.getBand3Rate(), cfg.getBand3Deductible(), bandLabel(cfg.getBand3Min(), cfg.getBand3Max())),
            new TaxBand(cfg.getBand4Min(), cfg.getBand4Max(), cfg.getBand4Rate(), cfg.getBand4Deductible(), bandLabel(cfg.getBand4Min(), cfg.getBand4Max())),
            new TaxBand(cfg.getBand5Min(), cfg.getBand5Max(), cfg.getBand5Rate(), cfg.getBand5Deductible(), bandLabel(cfg.getBand5Min(), cfg.getBand5Max())),
            new TaxBand(cfg.getBand6Min(), cfg.getBand6Max(), cfg.getBand6Rate(), cfg.getBand6Deductible(), bandLabel(cfg.getBand6Min(), cfg.getBand6Max()))
        );
    }

    private static String bandLabel(double min, double max) {
        return max >= Double.MAX_VALUE / 2
                ? String.format("Above ETB %.0f", min - 1)
                : String.format("ETB %.0f – %.0f", min, max);
    }

    private static TaxBand resolveBandFromConfig(double taxableAnnualIncome, SystemConfig cfg) {
        List<TaxBand> bands = getBandsFromConfig(cfg);
        for (TaxBand band : bands) {
            if (band.contains(taxableAnnualIncome)) return band;
        }
        return bands.get(bands.size() - 1);
    }

    /** Kept for backward compatibility with any callers that don't have a config. */
    static TaxBand resolveBand(double taxableAnnualIncome) {
        for (TaxBand band : TaxRule1395.INDIVIDUAL_BANDS) {
            if (band.contains(taxableAnnualIncome)) return band;
        }
        return TaxRule1395.INDIVIDUAL_BANDS.get(TaxRule1395.INDIVIDUAL_BANDS.size() - 1);
    }

    private TaxCalculationResponse buildResponse(
            double monthlyRent, double annualGross,
            boolean deductionApplied, double deductionPercent, double deductionAmount,
            double taxableAnnual, double annualTax,
            EntityType entityType, String bandLabel, double bandRate, double bandDeductible,
            boolean mixedUseWarning, List<String> advisoryNotes, String version) {

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
                .taxRuleVersion(version)
                .ruleEffectiveDate(TaxRule1395.EFFECTIVE_DATE)
                .appliedBandLabel(bandLabel)
                .appliedBandRate(bandRate)
                .appliedBandDeductible(entityType == EntityType.BUSINESS ? null : bandDeductible)
                .mixedUseDeductionWarning(mixedUseWarning)
                .advisoryNotes(advisoryNotes)
                .build();
    }

    static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
