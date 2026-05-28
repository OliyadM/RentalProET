package com.rentalpro.model.dto.response;

import com.rentalpro.model.enums.AccountStatus;
import com.rentalpro.model.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private UserRole role;
    private String subCityZone;
    private AccountStatus accountStatus; // needed by frontend to gate property/contract creation
}