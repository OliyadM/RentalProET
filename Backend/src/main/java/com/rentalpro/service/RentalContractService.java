package com.rentalpro.service;

import com.rentalpro.model.dto.request.ContractRequest;
import com.rentalpro.model.dto.response.ContractResponse;
import com.rentalpro.model.enums.ContractStatus;

import java.util.List;
import java.util.UUID;

public interface RentalContractService {

    // Landlord operations
    ContractResponse createContract(ContractRequest request, UUID landlordId);
    ContractResponse submitContract(UUID contractId, UUID landlordId);
    ContractResponse updateContractDraft(UUID contractId, ContractRequest request, UUID landlordId);
    List<ContractResponse> getMyContractsAsLandlord(UUID landlordId);

    // Tenant operations
    ContractResponse confirmContract(UUID contractId, UUID tenantId, String signature);
    ContractResponse rejectContract(UUID contractId, UUID tenantId, String reason);
    List<ContractResponse> getMyContractsAsTenant(UUID tenantId);

    // Shared operations
    ContractResponse getContractById(UUID id);
    ContractResponse getContractByIdForUser(UUID id, UUID userId);

    // Staff/Admin operations
    List<ContractResponse> getContractsByStatus(ContractStatus status);
    List<ContractResponse> getContractsByUnit(UUID unitId);
    ContractResponse terminateContract(UUID contractId, String reason, UUID staffId);
    
    // Officer operations
    ContractResponse approveContract(UUID contractId, UUID officerId);
    ContractResponse rejectContractByOfficer(UUID contractId, UUID officerId, String reason);
    List<ContractResponse> getPendingOfficerReview();
}