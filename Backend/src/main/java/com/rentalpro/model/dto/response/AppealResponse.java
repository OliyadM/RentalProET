package com.rentalpro.model.dto.response;

import com.rentalpro.model.enums.AppealStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class AppealResponse {
    private UUID id;
    private UUID contractId;
    private String contractAddress;
    private UUID tenantId;
    private String tenantName;
    private String tenantEmail;
    private String appealType;
    private String reason;
    private String evidenceDocuments;
    private AppealStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime reviewedAt;
    private String resolutionDecision;
    private String resolutionNotes;
    private UUID reviewedById;
    private String reviewedByName;
}