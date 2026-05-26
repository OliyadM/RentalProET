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
import com.rentalpro.service.NotificationService;
import com.rentalpro.model.enums.NotificationType;
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
    private final NotificationService notificationService;

    @Override
    public ContractResponse createContract(ContractRequest request, UUID landlordId) {
        User landlord = userRepository.findById(landlordId)
                .orElseThrow(() -> new EntityNotFoundException("Landlord not found"));

        // Guard: Only VERIFIED landlords can create contracts
        if (landlord.getAccountStatus() != com.rentalpro.model.enums.AccountStatus.VERIFIED) {
            throw new RuntimeException("Your account must be verified before you can create contracts. Please complete your profile and wait for verification.");
        }

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
                .paymentFrequency(request.getPaymentFrequency())
                .paymentDueDay(request.getPaymentDueDay() != null ? request.getPaymentDueDay() : 1)
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "BANK_TRANSFER")
                .securityDepositAmount(request.getSecurityDepositAmount())
                .noticePeriodDays(request.getNoticePeriodDays() != null ? request.getNoticePeriodDays() : 30)
                .renewalType(request.getRenewalType() != null ? request.getRenewalType() : "RENEGOTIATE")
                .contractDocumentUrl(request.getContractDocumentUrl())
                .additionalClauses(request.getAdditionalClauses())
                .currency("ETB")
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

        // Notify the tenant that a contract is waiting for their signature
        String tenantMsg = String.format(
                "A new rental contract for %s has been submitted and is awaiting your confirmation.",
                saved.getPropertyAddress());
        notificationService.send(
                saved.getTenant().getId(),
                NotificationType.CONTRACT_SUBMITTED,
                tenantMsg,
                saved.getId());

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
        contract.setPaymentFrequency(request.getPaymentFrequency());
        contract.setPaymentDueDay(request.getPaymentDueDay() != null ? request.getPaymentDueDay() : 1);
        contract.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "BANK_TRANSFER");
        contract.setSecurityDepositAmount(request.getSecurityDepositAmount());
        contract.setNoticePeriodDays(request.getNoticePeriodDays() != null ? request.getNoticePeriodDays() : 30);
        contract.setRenewalType(request.getRenewalType() != null ? request.getRenewalType() : "RENEGOTIATE");
        contract.setContractDocumentUrl(request.getContractDocumentUrl());
        contract.setAdditionalClauses(request.getAdditionalClauses());

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

        // NEW FLOW: After tenant confirms, send to officer for review
        contract.setStatus(ContractStatus.PENDING_OFFICER_REVIEW);
        contract.setTenantSignature(signature);
        contract.setTenantConfirmedAt(LocalDateTime.now());

        RentalContract saved = contractRepository.save(contract);
        logAction("CONFIRM_CONTRACT", "RentalContract", saved.getId(), tenantId, "Tenant confirmed contract - pending officer review");

        // Notify the landlord that tenant has signed
        String tenantFullName = saved.getTenant().getFirstName() + " " + saved.getTenant().getLastName();
        String landlordMsg = String.format(
                "%s has confirmed the contract for %s. Awaiting officer approval.",
                tenantFullName, saved.getPropertyAddress());
        notificationService.send(
                saved.getLandlord().getId(),
                NotificationType.CONTRACT_CONFIRMED,
                landlordMsg,
                saved.getId());

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

        // Notify the landlord that their tenant has rejected the contract
        String tenantFullName = saved.getTenant().getFirstName() + " " + saved.getTenant().getLastName();
        String landlordMsg = String.format(
                "%s has rejected the contract for %s. Reason: %s",
                tenantFullName, saved.getPropertyAddress(), reason);
        notificationService.send(
                saved.getLandlord().getId(),
                NotificationType.CONTRACT_REJECTED,
                landlordMsg,
                saved.getId());

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
                contract.getStatus() != ContractStatus.PENDING_OFFICER_REVIEW) {
            throw new RuntimeException("Only ACTIVE or PENDING_OFFICER_REVIEW contracts can be terminated");
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
                .paymentFrequency(contract.getPaymentFrequency())
                .paymentDueDay(contract.getPaymentDueDay())
                .paymentMethod(contract.getPaymentMethod())
                .securityDepositAmount(contract.getSecurityDepositAmount())
                .noticePeriodDays(contract.getNoticePeriodDays())
                .renewalType(contract.getRenewalType())
                .contractDocumentUrl(contract.getContractDocumentUrl())
                .currency(contract.getCurrency())
                .status(contract.getStatus())
                .additionalClauses(contract.getAdditionalClauses())
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

    @Override
    public ContractResponse approveContract(UUID contractId, UUID officerId) {
        RentalContract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new EntityNotFoundException("Contract not found"));

        if (contract.getStatus() != ContractStatus.PENDING_OFFICER_REVIEW) {
            throw new RuntimeException("Contract is not pending officer review");
        }

        User officer = userRepository.findById(officerId)
                .orElseThrow(() -> new EntityNotFoundException("Officer not found"));

        contract.setStatus(ContractStatus.ACTIVE);
        contract.setReviewedBy(officer);
        contract.setOfficerReviewedAt(LocalDateTime.now());

        RentalContract saved = contractRepository.save(contract);
        logAction("APPROVE_CONTRACT", "RentalContract", saved.getId(), officerId, "Officer approved contract");

        // Notify landlord and tenant
        String msg = String.format("Contract for %s has been approved and is now active.", saved.getPropertyAddress());
        notificationService.send(saved.getLandlord().getId(), NotificationType.CONTRACT_CONFIRMED, msg, saved.getId());
        notificationService.send(saved.getTenant().getId(), NotificationType.CONTRACT_CONFIRMED, msg, saved.getId());

        return mapToResponse(saved);
    }

    @Override
    public ContractResponse rejectContractByOfficer(UUID contractId, UUID officerId, String reason) {
        RentalContract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new EntityNotFoundException("Contract not found"));

        if (contract.getStatus() != ContractStatus.PENDING_OFFICER_REVIEW) {
            throw new RuntimeException("Contract is not pending officer review");
        }

        User officer = userRepository.findById(officerId)
                .orElseThrow(() -> new EntityNotFoundException("Officer not found"));

        contract.setStatus(ContractStatus.REJECTED);
        contract.setReviewedBy(officer);
        contract.setOfficerReviewedAt(LocalDateTime.now());
        contract.setRejectionReason(reason);

        RentalContract saved = contractRepository.save(contract);
        logAction("REJECT_CONTRACT", "RentalContract", saved.getId(), officerId, "Officer rejected contract: " + reason);

        // Notify landlord and tenant
        String msg = String.format("Contract for %s has been rejected by officer. Reason: %s", saved.getPropertyAddress(), reason);
        notificationService.send(saved.getLandlord().getId(), NotificationType.CONTRACT_REJECTED, msg, saved.getId());
        notificationService.send(saved.getTenant().getId(), NotificationType.CONTRACT_REJECTED, msg, saved.getId());

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractResponse> getPendingOfficerReview() {
        return contractRepository.findByStatus(ContractStatus.PENDING_OFFICER_REVIEW).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractResponse> getContractsForOfficer(ContractStatus status, String subCity, String search, String sortBy) {
        // Parse sort parameter (format: "field,direction")
        org.springframework.data.domain.Sort sort;
        if (sortBy != null && !sortBy.isEmpty()) {
            String[] parts = sortBy.split(",");
            String field = parts[0];
            String direction = parts.length > 1 ? parts[1] : "desc";
            
            // Map frontend sort fields to entity fields
            String entityField = switch (field) {
                case "newest" -> "tenantConfirmedAt";
                case "oldest" -> "tenantConfirmedAt";
                case "rent" -> "monthlyRent";
                default -> "tenantConfirmedAt";
            };
            
            sort = direction.equalsIgnoreCase("asc") 
                ? org.springframework.data.domain.Sort.by(entityField).ascending()
                : org.springframework.data.domain.Sort.by(entityField).descending();
        } else {
            sort = org.springframework.data.domain.Sort.by("tenantConfirmedAt").descending();
        }
        
        return contractRepository.findContractsForOfficer(status, subCity, search, sort).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
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