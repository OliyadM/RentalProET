package com.rentalpro.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "rent_declarations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentDeclaration {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    @JsonIgnoreProperties({"declarations", "hibernateLazyInitializer", "handler"})
    private RentalContract contract;

    @Column(nullable = false)
    private LocalDate declarationPeriod; // First day of the month

    @Column(nullable = false)
    private Double declaredRent;

    private Double aiBenchmarkRent;

    private Double anomalyScore; // 0-1 score

    @Builder.Default
    private Boolean isAnomaly = false;

    private String anomalyReason;

    private Boolean taxCompliant;

    /** Monthly estimated tax (Proclamation 1395/2025). */
    private Double estimatedTax;

    /** Whether landlord claimed the residential maintenance deduction. */
    @Builder.Default
    private Boolean claimDeduction = false;

    private Boolean deductionApplied;

    private Double deductionAmount;

    private Double taxableAnnualIncome;

    private Double annualTax;

    private Double effectiveTaxRate;

    private String taxRuleVersion;

    private Boolean mixedUseDeductionWarning;

    @Column(length = 1000)
    private String taxAdvisoryNote;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isVerified = false;

    private String verificationNotes;

    @CreationTimestamp
    private LocalDateTime createdAt;
}