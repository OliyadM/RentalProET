package com.rentalpro.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class AppealRequest {
    @NotNull(message = "Contract ID is required")
    private UUID contractId;

    @NotBlank(message = "Appeal type is required")
    private String appealType;

    @NotBlank(message = "Reason is required")
    @Size(min = 20, message = "Reason must be at least 20 characters")
    private String reason;

    private String evidenceDocuments; // JSON array of file URLs
}