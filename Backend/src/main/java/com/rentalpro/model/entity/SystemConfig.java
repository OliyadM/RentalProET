package com.rentalpro.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Singleton configuration record for platform-wide settings.
 * There is always exactly one row in this table.
 * Use AdminServiceImpl.getConfig() which auto-creates defaults on first access.
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

    /**
     * Flat tax rate applied to declared rent.
     * Stored as a decimal fraction: 0.10 = 10%.
     */
    @Column(nullable = false)
    @Builder.Default
    private Double taxRate = 0.10;

    /**
     * Deviation threshold above which a declaration is flagged as anomalous.
     * Stored as a decimal fraction: 0.15 = 15%.
     */
    @Column(nullable = false)
    @Builder.Default
    private Double anomalyThresholdPercentage = 0.15;

    /**
     * Maximum allowed rent increase percentage per renewal cycle.
     * Stored as a decimal fraction: 0.10 = 10%.
     */
    @Column(nullable = false)
    @Builder.Default
    private Double maxRentIncreaseCap = 0.10;

    /**
     * Minimum contract duration in years that landlords must meet.
     * Default: 2 years.
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer minimumContractYears = 2;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
