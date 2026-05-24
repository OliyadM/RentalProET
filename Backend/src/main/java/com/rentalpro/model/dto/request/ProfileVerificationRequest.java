package com.rentalpro.model.dto.request;

import com.rentalpro.model.enums.AccountStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileVerificationRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Status is required (VERIFIED or REJECTED)")
    private AccountStatus status;

    private String verificationNotes;

    // Required if status = REJECTED
    private String rejectionReason;
}
