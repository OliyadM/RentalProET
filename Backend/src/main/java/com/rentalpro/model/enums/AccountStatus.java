package com.rentalpro.model.enums;

public enum AccountStatus {
    PENDING_PROFILE,        // Just registered, needs to complete profile
    PENDING_VERIFICATION,   // Profile submitted, waiting for officer review
    VERIFIED,               // Officer approved, can use platform
    REJECTED                // Officer rejected, cannot use platform
}
