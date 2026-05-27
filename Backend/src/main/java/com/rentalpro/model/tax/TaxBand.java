package com.rentalpro.model.tax;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Progressive income tax band per Proclamation 1395/2025.
 * Formula: tax = (taxableIncome × rate) − deductibleAmount
 */
@Getter
@AllArgsConstructor
public class TaxBand {

    private final double minInclusive;
    private final double maxInclusive;
    private final double rate;
    private final double deductibleAmount;
    private final String label;

    public boolean contains(double taxableAnnualIncome) {
        return taxableAnnualIncome >= minInclusive && taxableAnnualIncome <= maxInclusive;
    }
}
