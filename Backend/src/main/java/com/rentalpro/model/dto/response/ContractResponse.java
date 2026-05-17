package com.rentalpro.model.dto.response;

import com.rentalpro.model.enums.ContractStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ContractResponse {
    private UUID id;
    private UUID unitId;
    private String unitNumber;
    private UUID propertyId;
    private String propertyName;
    private String propertyAddress;
    private UUID tenantId;
    private String tenantName;
    private String tenantEmail;
    private UUID landlordId;
    private String landlordName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double monthlyRent;
    private String currency;
    private ContractStatus status;
    private String termsAndConditions;
    private String tenantSignature;
    private String landlordSignature;
    private LocalDateTime tenantConfirmedAt;
    private LocalDateTime landlordSubmittedAt;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;
}