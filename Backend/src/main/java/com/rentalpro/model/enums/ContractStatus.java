package com.rentalpro.model.enums;

public enum ContractStatus {
    DRAFT,                      // Tenant not registered yet
    PENDING_CONFIRMATION,       // Waiting for tenant to confirm
    PENDING_OFFICER_REVIEW,     // Tenant confirmed, waiting for officer approval
    ACTIVE,                     // Officer approved, contract is active
    UNDER_APPEAL,
    UNDER_REVIEW,
    REJECTED,                   // Officer rejected
    TERMINATED,
    EXPIRED
}