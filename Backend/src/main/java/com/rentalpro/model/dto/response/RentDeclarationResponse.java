package com.rentalpro.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RentDeclarationResponse {
    private UUID id;
    private UUID contractId;
    private LocalDate declarationPeriod;
    private Double declaredRent;
    private Double aiBenchmarkRent;
    private Double anomalyScore;
    private Boolean isAnomaly;
    private String anomalyReason;
    private Double estimatedTax;
    private Boolean isVerified;
    private LocalDateTime createdAt;
}