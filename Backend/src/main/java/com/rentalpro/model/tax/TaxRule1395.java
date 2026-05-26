package com.rentalpro.model.tax;

import java.time.LocalDate;
import java.util.List;

/**
 * Default tax rule configuration for Income Tax Amendment Proclamation No. 1395/2025.
 * Bands and rates are admin-overridable in a later sprint; this is the canonical default.
 */
public final class TaxRule1395 {

    public static final String VERSION = "1395/2025";
    public static final LocalDate EFFECTIVE_DATE = LocalDate.of(2025, 7, 7);
    public static final double BUSINESS_FLAT_RATE = 0.30;
    public static final double RESIDENTIAL_DEDUCTION_PERCENT = 0.40;

    public static final List<TaxBand> INDIVIDUAL_BANDS = List.of(
            new TaxBand(0, 24_000, 0.00, 0, "0 – 24,000 ETB"),
            new TaxBand(24_001, 48_000, 0.10, 2_400, "24,001 – 48,000 ETB"),
            new TaxBand(48_001, 78_000, 0.20, 6_000, "48,001 – 78,000 ETB"),
            new TaxBand(78_001, 120_000, 0.25, 9_900, "78,001 – 120,000 ETB"),
            new TaxBand(120_001, 168_000, 0.30, 15_900, "120,001 – 168,000 ETB"),
            new TaxBand(168_001, Double.MAX_VALUE, 0.30, 15_900, "Above 168,000 ETB")
    );

    private TaxRule1395() {
    }
}
