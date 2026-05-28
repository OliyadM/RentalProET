package com.rentalpro.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rentalpro.model.enums.ContractStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "rental_contracts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalContract {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id")
    @JsonIgnoreProperties({"contracts", "hibernateLazyInitializer", "handler"})
    private RentalUnit rentalUnit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    @JsonIgnoreProperties({"tenantContracts", "hibernateLazyInitializer", "handler"})
    private User tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "landlord_id", nullable = false)
    @JsonIgnoreProperties({"landlordContracts", "hibernateLazyInitializer", "handler"})
    private User landlord;

    @Column(nullable = false)
    private String propertyAddress;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    @Column(nullable = false)
    private Double monthlyRent;

    private String paymentFrequency; // Monthly, Quarterly, Annually

    // Ethiopian contract required fields
    @Builder.Default
    private Integer paymentDueDay = 1; // Day of month (1-31)

    @Builder.Default
    private String paymentMethod = "BANK_TRANSFER"; // BANK_TRANSFER, CASH, MOBILE_MONEY, CHECK
    
    private Double securityDepositAmount;
    
    @Builder.Default
    private Integer noticePeriodDays = 30; // Notice period for termination

    @Builder.Default
    private String renewalType = "RENEGOTIATE"; // AUTO_RENEW, RENEGOTIATE, FIXED_TERM

    @Column(nullable = false)
    @Builder.Default
    private String currency = "ETB";

    private String contractDocumentUrl; // Security contract PDF

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContractStatus status;

    @Column(length = 2000)
    private String additionalClauses; // Replaces termsAndConditions

    @Column(columnDefinition = "TEXT")
    private String tenantSignature;

    @Column(columnDefinition = "TEXT")
    private String landlordSignature;

    private LocalDateTime tenantConfirmedAt;

    private LocalDateTime landlordSubmittedAt;

    private LocalDateTime officerReviewedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_id")
    private User reviewedBy;

    private String rejectionReason;

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"contract"})
    private List<RentDeclaration> declarations;

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"contract"})
    private List<Appeal> appeals;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Version
    private Long version;
}