package com.rentalpro.model.dto.request;

import com.rentalpro.model.enums.EntityType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUpdateRequest {

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotNull(message = "Residential address is required")
    private String residentialAddress;

    @NotNull(message = "National ID number is required")
    private String nationalIdNumber;

    private String nationalIdDocumentUrl;

    @NotNull(message = "TIN number is required")
    private String tinNumber;

    @NotNull(message = "Entity type is required")
    private EntityType entityType;

    // Required only if entityType = BUSINESS
    private String businessRegNumber;
    private String businessRegDocumentUrl;
}
