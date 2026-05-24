package com.rentalpro.model.enums;

public enum PropertyStatus {
    PENDING_OFFICER_REVIEW,  // Just submitted, waiting for officer approval
    ACTIVE,                  // Officer approved, can add units
    REJECTED                 // Officer rejected
}
