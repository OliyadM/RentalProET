package com.rentalpro.service;

import com.rentalpro.model.dto.response.RentDeclarationResponse;
import com.rentalpro.model.entity.RentDeclaration;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface RentDeclarationService {

    RentDeclarationResponse createDeclaration(UUID contractId, LocalDate period, Double declaredRent, UUID landlordId);

    RentDeclaration analyzeDeclaration(UUID declarationId);

    List<RentDeclarationResponse> getDeclarationsByContract(UUID contractId);

    List<RentDeclarationResponse> getAnomalies(String subCity);
    //  New
    List<RentDeclarationResponse> getUnverifiedDeclarations(String subCity);
    // New
    RentDeclarationResponse verifyDeclaration(UUID declarationId, String notes, UUID staffId);
}