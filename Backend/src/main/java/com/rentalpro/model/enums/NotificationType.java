package com.rentalpro.model.enums;

public enum NotificationType {
    // Contract lifecycle
    CONTRACT_CONFIRMED,       // Tenant confirmed  → Landlord notified
    CONTRACT_REJECTED,        // Tenant rejected   → Landlord notified
    CONTRACT_SUBMITTED,       // Landlord submitted → Tenant notified

    // Appeals
    APPEAL_SUBMITTED,         // Tenant submitted appeal → Officer notified
    APPEAL_RESOLVED,          // Officer resolved appeal → Tenant notified
    APPEAL_REJECTED,          // Officer rejected appeal → Tenant notified

    // Declarations
    DECLARATION_ANOMALY,      // Anomaly detected on declaration → Officer notified
    DECLARATION_SUBMITTED,    // Landlord submitted declaration → Officers notified
    DECLARATION_VERIFIED,     // Officer verified declaration → Landlord notified
    DECLARATION_REJECTED,     // Officer rejected declaration → Landlord notified

    // Properties
    PROPERTY_PENDING_REVIEW,  // Landlord submitted property → Officers notified
    PROPERTY_VERIFIED,        // Officer verified property → Landlord notified

    // Account lifecycle
    ACCOUNT_CREATED,          // User registered → notify user
    PROFILE_PENDING_REVIEW,   // Profile submitted → notify officers in sub-city
    ACCOUNT_VERIFIED,         // Officer approved profile → notify user
    ACCOUNT_REJECTED,         // Officer rejected profile → notify user (with reason)

    // General
    SYSTEM                    // System-generated message
}
