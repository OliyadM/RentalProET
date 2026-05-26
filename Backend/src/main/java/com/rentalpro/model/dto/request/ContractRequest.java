package com.rentalpro.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class ContractRequest {

    @NotNull(message = "Unit ID is required")
    private UUID unitId;

    @NotBlank(message = "Tenant email is required")
    @Email(message = "Invalid email format")
    private String tenantEmail;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotNull(message = "Monthly rent is required")
    @Positive(message = "Monthly rent must be positive")
    private Double monthlyRent;

    @NotBlank(message = "Payment frequency is required")
    private String paymentFrequency; // Monthly, Quarterly, Annually

    // Ethiopian contract required fields
    private Integer paymentDueDay = 1; // Day of month (1-31)
    
    private String paymentMethod = "BANK_TRANSFER"; // BANK_TRANSFER, CASH, MOBILE_MONEY, CHECK
    
    private Double securityDepositAmount;
    
    private Integer noticePeriodDays = 30; // Notice period for termination (days)
    
    private String renewalType = "RENEGOTIATE"; // AUTO_RENEW, RENEGOTIATE, FIXED_TERM

    private String contractDocumentUrl; // Security contract PDF URL

    private String additionalClauses; // Optional additional terms
}