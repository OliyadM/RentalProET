package com.rentalpro.model.dto.response;

import com.rentalpro.model.enums.AccountStatus;
import com.rentalpro.model.enums.EntityType;
import com.rentalpro.model.enums.UserRole;
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
public class ProfileResponse {

    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private UserRole role;
    private String subCityZone;
    private AccountStatus accountStatus;
    
    // KYC fields
    private LocalDate dateOfBirth;
    private String residentialAddress;
    private String nationalIdNumber;
    private String nationalIdDocumentUrl;
    private String tinNumber;
    private EntityType entityType;
    private String businessRegNumber;
    private String businessRegDocumentUrl;
    
    // Verification info
    private String verificationNotes;
    private UUID verifiedById;
    private String verifiedByName;
    private LocalDateTime verifiedAt;
    private String rejectionReason;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
