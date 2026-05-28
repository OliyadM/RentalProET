package com.rentalpro.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Singleton configuration record for platform-wide settings.
 * Stores the full tax rule configuration so the admin can update
 * brackets, rates and deductions without a code deployment.
 */
@Entity
@Table(name = "system_config")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // ── Anomaly & contract settings ──────────────────────────────────────────

    @Column(nullable = false)
    @Builder.Default
    private Double anomalyThresholdPercentage = 0.15;

    @Column(nullable = false)
    @Builder.Default
    private Double maxRentIncreaseCap = 0.10;

    @Column(nullable = false)
    @Builder.Default
    private Integer minimumContractYears = 2;

    // ── Business entity tax ──────────────────────────────────────────────────

    /** Flat tax rate for BUSINESS entities. Stored as fraction: 0.30 = 30%. */
    @Column(nullable = false)
    @Builder.Default
    private Double businessFlatTaxRate = 0.30;

    // ── Individual entity — residential deduction ────────────────────────────

    /** Deduction % applied to annual gross rent for eligible residential properties. 0.40 = 40%. */
    @Column(nullable = false)
    @Builder.Default
    private Double residentialDeductionPercent = 0.40;

    // ── Individual entity — progressive tax bands (stored as parallel arrays) ─
    // Six bands matching Proclamation 1395/2025.
    // band_min_N / band_max_N in ETB annual income.
    // band_rate_N as fraction (0.10 = 10%).
    // band_deductible_N in ETB (the constant subtracted after applying the rate).

    @Column(name = "band1_min",        nullable = false) @Builder.Default private Double band1Min = 0.0;
    @Column(name = "band1_max",        nullable = false) @Builder.Default private Double band1Max = 24000.0;
    @Column(name = "band1_rate",       nullable = false) @Builder.Default private Double band1Rate = 0.00;
    @Column(name = "band1_deductible", nullable = false) @Builder.Default private Double band1Deductible = 0.0;

    @Column(name = "band2_min",        nullable = false) @Builder.Default private Double band2Min = 24001.0;
    @Column(name = "band2_max",        nullable = false) @Builder.Default private Double band2Max = 48000.0;
    @Column(name = "band2_rate",       nullable = false) @Builder.Default private Double band2Rate = 0.10;
    @Column(name = "band2_deductible", nullable = false) @Builder.Default private Double band2Deductible = 2400.0;

    @Column(name = "band3_min",        nullable = false) @Builder.Default private Double band3Min = 48001.0;
    @Column(name = "band3_max",        nullable = false) @Builder.Default private Double band3Max = 78000.0;
    @Column(name = "band3_rate",       nullable = false) @Builder.Default private Double band3Rate = 0.20;
    @Column(name = "band3_deductible", nullable = false) @Builder.Default private Double band3Deductible = 6000.0;

    @Column(name = "band4_min",        nullable = false) @Builder.Default private Double band4Min = 78001.0;
    @Column(name = "band4_max",        nullable = false) @Builder.Default private Double band4Max = 120000.0;
    @Column(name = "band4_rate",       nullable = false) @Builder.Default private Double band4Rate = 0.25;
    @Column(name = "band4_deductible", nullable = false) @Builder.Default private Double band4Deductible = 9900.0;

    @Column(name = "band5_min",        nullable = false) @Builder.Default private Double band5Min = 120001.0;
    @Column(name = "band5_max",        nullable = false) @Builder.Default private Double band5Max = 168000.0;
    @Column(name = "band5_rate",       nullable = false) @Builder.Default private Double band5Rate = 0.30;
    @Column(name = "band5_deductible", nullable = false) @Builder.Default private Double band5Deductible = 15900.0;

    @Column(name = "band6_min",        nullable = false) @Builder.Default private Double band6Min = 168001.0;
    @Column(name = "band6_max",        nullable = false) @Builder.Default private Double band6Max = Double.MAX_VALUE;
    @Column(name = "band6_rate",       nullable = false) @Builder.Default private Double band6Rate = 0.30;
    @Column(name = "band6_deductible", nullable = false) @Builder.Default private Double band6Deductible = 15900.0;

    /** Human-readable version label, e.g. "1395/2025". */
    @Column(nullable = false)
    @Builder.Default
    private String taxRuleVersion = "1395/2025";

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // ── Legacy field kept for backward compatibility ──────────────────────────
    /** @deprecated Use businessFlatTaxRate instead. Kept so old DB rows don't break. */
    @Deprecated
    @Column(name = "tax_rate")
    @Builder.Default
    private Double taxRate = 0.30;
}
