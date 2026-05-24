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

    private String contractDocumentUrl; // Security contract PDF URL

    private String additionalClauses; // Optional additional terms
}