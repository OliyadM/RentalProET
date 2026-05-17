package com.rentalpro.service.impl;

import com.rentalpro.model.dto.request.ContractRequest;
import com.rentalpro.model.dto.response.ContractResponse;
import com.rentalpro.model.entity.AuditLog;
import com.rentalpro.model.entity.RentalContract;
import com.rentalpro.model.entity.RentalUnit;
import com.rentalpro.model.entity.User;
import com.rentalpro.model.enums.ContractStatus;
import com.rentalpro.model.enums.UserRole;
import com.rentalpro.repository.AuditLogRepository;
import com.rentalpro.repository.RentalContractRepository;
import com.rentalpro.repository.RentalUnitRepository;
import com.rentalpro.repository.UserRepository;
import com.rentalpro.service.RentalContractService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RentalContractServiceImpl implements RentalContractService {

    private final RentalContractRepository contractRepository;
    private final RentalUnitRepository unitRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    @Override
    public ContractResponse createContract(ContractRequest request, UUID landlordId) {
        User landlord = userRepository.findById(landlordId)
                .orElseThrow(() -> new EntityNotFoundException("Landlord not found"));

        RentalUnit unit = unitRepository.findById(request.getUnitId())
                .orElseThrow(() -> new EntityNotFoundException("Rental unit not found"));

        // Verify landlord owns this unit
        if (!unit.getProperty().getOwner().getId().equals(landlordId)) {
            throw new RuntimeException("You do not own this rental unit");
        }

        // Check if unit already has an active contract
        if (contractRepository.existsActiveContractByUnitId(request.getUnitId())) {
            throw new RuntimeException("Unit already has an active contract");
        }

        // Find tenant by email - if not found, throw error with helpful message
        User tenant = userRepository.findByEmail(request.getTenantEmail())
                .orElseThrow(() -> new EntityNotFoundException(
                    "Tenant with email '" + request.getTenantEmail() + "' not found. " +
                    "Please ask the tenant to register first with this email address."
                ));

        // Verify user is actually a tenant
        if (tenant.getRole() != UserRole.TENANT) {
            throw new RuntimeException("User with email '" + request.getTenantEmail() + "' is not registered as a tenant");
        }

        RentalContract contract = RentalContract.builder()
                .rentalUnit(unit)
                .tenant(tenant)
                .landlord(landlord)
                .propertyAddress(unit.getProperty().getAddress())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .monthlyRent(request.getMonthlyRent())
                .currency("ETB")
                .termsAndConditions(request.getTermsAndConditions())
                .status(ContractStatus.DRAFT)
                .build();

        RentalContract saved = contractRepository.save(contract);
        logAction("CREATE_CONTRACT", "RentalContract", saved.getId(), landlordId, "Created contract draft");

        return mapToResponse(saved);
    }

    @Override
    public ContractResponse submitContract(UUID contractId, UUID landlordId) {
        RentalContract contract = contractRepository.findByIdAndLandlordId(contractId, landlordId)
                .orElseThrow(() -> new EntityNotFoundException("Contract not found or access denied"));

        if (contract.getStatus() != ContractStatus.DRAFT) {
            throw new RuntimeException("Only DRAFT contracts can be submitted");
        }

        contract.setStatus(ContractStatus.PENDING_CONFIRMATION);
        contract.setLandlordSubmittedAt(LocalDateTime.now());

        RentalContract saved = contractRepository.save(contract);
        logAction("SUBMIT_CONTRACT", "RentalContract", saved.getId(), landlordId, "Submitted for tenant confirmation");

        return mapToResponse(saved);
    }

    @Override
    public ContractResponse updateContractDraft(UUID contractId, ContractRequest request, UUID landlordId) {
        RentalContract contract = contractRepository.findByIdAndLandlordId(contractId, landlordId)
                .orElseThrow(() -> new EntityNotFoundException("Contract not found or access denied"));

        if (contract.getStatus() != ContractStatus.DRAFT) {
            throw new RuntimeException("Only DRAFT contracts can be edited");
        }

        // Verify new unit if changed
        if (!contract.getRentalUnit().getId().equals(request.getUnitId())) {
            RentalUnit newUnit = unitRepository.findById(request.getUnitId())
                    .orElseThrow(() -> new EntityNotFoundException("Rental unit not found"));
            if (!newUnit.getProperty().getOwner().getId().equals(landlordId)) {
                throw new RuntimeException("You do not own this rental unit");
            }
            contract.setRentalUnit(newUnit);
            contract.setPropertyAddress(newUnit.getProperty().getAddress());
        }

        // Verify new tenant by email if changed
        if (!contract.getTenant().getEmail().equals(request.getTenantEmail())) {
            User newTenant = userRepository.findByEmail(request.getTenantEmail())
                    .orElseThrow(() -> new EntityNotFoundException(
                        "Tenant with email '" + request.getTenantEmail() + "' not found. " +
                        "Please ask the tenant to register first with this email address."
                    ));
            if (newTenant.getRole() != UserRole.TENANT) {
                throw new RuntimeException("User with email '" + request.getTenantEmail() + "' is not registered as a tenant");
            }
            contract.setTenant(newTenant);
        }

        contract.setStartDate(request.getStartDate());
        contract.setEndDate(request.getEndDate());
        contract.setMonthlyRent(request.getMonthlyRent());
        contract.setTermsAndConditions(request.getTermsAndConditions());

        RentalContract saved = contractRepository.save(contract);
        logAction("UPDATE_CONTRACT", "RentalContract", saved.getId(), landlordId, "Updated contract draft");

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractResponse> getMyContractsAsLandlord(UUID landlordId) {
        return contractRepository.findByLandlordId(landlordId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ContractResponse confirmContract(UUID contractId, UUID tenantId, String signature) {
        RentalContract contract = contractRepository.findByIdAndTenantId(contractId, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Contract not found or access denied"));

        if (contract.getStatus() != ContractStatus.PENDING_CONFIRMATION) {
            throw new RuntimeException("Contract is not pending confirmation");
        }

        contract.setStatus(ContractStatus.CONFIRMED);
        contract.setTenantSignature(signature);
        contract.setTenantConfirmedAt(LocalDateTime.now());

        // Auto-transition to ACTIVE if start date is today or past
        if (!contract.getStartDate().isAfter(java.time.LocalDate.now())) {
            contract.setStatus(ContractStatus.ACTIVE);
        }

        RentalContract saved = contractRepository.save(contract);
        logAction("CONFIRM_CONTRACT", "RentalContract", saved.getId(), tenantId, "Tenant confirmed contract");

        return mapToResponse(saved);
    }

    @Override
    public ContractResponse rejectContract(UUID contractId, UUID tenantId, String reason) {
        RentalContract contract = contractRepository.findByIdAndTenantId(contractId, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Contract not found or access denied"));

        if (contract.getStatus() != ContractStatus.PENDING_CONFIRMATION) {
            throw new RuntimeException("Contract is not pending confirmation");
        }

        contract.setStatus(ContractStatus.REJECTED);
        contract.setRejectionReason(reason);

        RentalContract saved = contractRepository.save(contract);
        logAction("REJECT_CONTRACT", "RentalContract", saved.getId(), tenantId, "Tenant rejected: " + reason);

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractResponse> getMyContractsAsTenant(UUID tenantId) {
        return contractRepository.findByTenantId(tenantId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ContractResponse getContractById(UUID id) {
        RentalContract contract = contractRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Contract not found"));
        return mapToResponse(contract);
    }

    @Override
    @Transactional(readOnly = true)
    public ContractResponse getContractByIdForUser(UUID id, UUID userId) {
        RentalContract contract = contractRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Contract not found"));

        // Verify user has access to this contract
        if (!contract.getLandlord().getId().equals(userId) &&
                !contract.getTenant().getId().equals(userId)) {
            throw new RuntimeException("Access denied to this contract");
        }

        return mapToResponse(contract);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractResponse> getContractsByStatus(ContractStatus status) {
        return contractRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractResponse> getContractsByUnit(UUID unitId) {
        return contractRepository.findByRentalUnitId(unitId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ContractResponse terminateContract(UUID contractId, String reason, UUID staffId) {
        RentalContract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new EntityNotFoundException("Contract not found"));

        if (contract.getStatus() != ContractStatus.ACTIVE &&
                contract.getStatus() != ContractStatus.CONFIRMED) {
            throw new RuntimeException("Only ACTIVE or CONFIRMED contracts can be terminated");
        }

        contract.setStatus(ContractStatus.TERMINATED);

        RentalContract saved = contractRepository.save(contract);
        logAction("TERMINATE_CONTRACT", "RentalContract", saved.getId(), staffId, "Terminated: " + reason);

        return mapToResponse(saved);
    }

    private ContractResponse mapToResponse(RentalContract contract) {
        return ContractResponse.builder()
                .id(contract.getId())
                .unitId(contract.getRentalUnit().getId())
                .unitNumber(contract.getRentalUnit().getUnitNumber())
                .propertyId(contract.getRentalUnit().getProperty().getId())
                .propertyName(contract.getRentalUnit().getProperty().getPropertyName())
                .propertyAddress(contract.getPropertyAddress())
                .tenantId(contract.getTenant().getId())
                .tenantName(contract.getTenant().getFirstName() + " " + contract.getTenant().getLastName())
                .tenantEmail(contract.getTenant().getEmail())
                .landlordId(contract.getLandlord().getId())
                .landlordName(contract.getLandlord().getFirstName() + " " + contract.getLandlord().getLastName())
                .startDate(contract.getStartDate())
                .endDate(contract.getEndDate())
                .monthlyRent(contract.getMonthlyRent())
                .currency(contract.getCurrency())
                .status(contract.getStatus())
                .termsAndConditions(contract.getTermsAndConditions())
                .tenantSignature(contract.getTenantSignature())
                .landlordSignature(contract.getLandlordSignature())
                .tenantConfirmedAt(contract.getTenantConfirmedAt())
                .landlordSubmittedAt(contract.getLandlordSubmittedAt())
                .rejectionReason(contract.getRejectionReason())
                .createdAt(contract.getCreatedAt())
                .updatedAt(contract.getUpdatedAt())
                .version(contract.getVersion())
                .build();
    }

    private void logAction(String action, String entityType, UUID entityId, UUID userId, String details) {
        AuditLog log = AuditLog.builder()
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .user(userRepository.findById(userId).orElse(null))
                .details(details)
                .build();
        auditLogRepository.save(log);
    }
}