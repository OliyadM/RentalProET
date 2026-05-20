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

    // Properties
    PROPERTY_VERIFIED,        // Officer verified property → Landlord notified

    // General
    SYSTEM                    // System-generated message
}
