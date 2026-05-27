package com.rentalpro.service;

import com.rentalpro.model.dto.response.TaxCalculationResponse;
import com.rentalpro.model.enums.EntityType;
import com.rentalpro.model.enums.PropertyType;

public interface TaxCalculationService {

    /**
     * Computes rental income tax per Proclamation 1395/2025 (advisory, rental-only).
     *
     * @param monthlyRent          declared monthly rent in ETB
     * @param entityType           INDIVIDUAL or BUSINESS
     * @param propertyType         property classification for deduction eligibility
     * @param claimDeduction       whether individual landlord claims the residential deduction
     */
    TaxCalculationResponse calculate(
            double monthlyRent,
            EntityType entityType,
            PropertyType propertyType,
            boolean claimDeduction
    );
}
