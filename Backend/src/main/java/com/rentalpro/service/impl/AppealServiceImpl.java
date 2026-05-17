package com.rentalpro.service.impl;

import com.rentalpro.model.dto.request.AppealRequest;
import com.rentalpro.model.dto.response.AppealResponse;
import com.rentalpro.model.entity.Appeal;
import com.rentalpro.model.entity.RentalContract;
import com.rentalpro.model.entity.User;
import com.rentalpro.model.enums.AppealStatus;
import com.rentalpro.model.enums.ContractStatus;
import com.rentalpro.repository.AppealRepository;
import com.rentalpro.repository.RentalContractRepository;
import com.rentalpro.repository.UserRepository;
import com.rentalpro.service.AppealService;
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
public class AppealServiceImpl implements AppealService {

    private final AppealRepository appealRepository;
    private final RentalContractRepository contractRepository;
    private final UserRepository userRepository;

    @Override
    public AppealResponse createAppeal(AppealRequest request, UUID tenantId) {
        User tenant = userRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Tenant not found"));

        RentalContract contract = contractRepository.findById(request.getContractId())
                .orElseThrow(() -> new EntityNotFoundException("Contract not found"));

        if (!contract.getTenant().getId().equals(tenantId)) {
            throw new RuntimeException("Unauthorized: You are not the tenant of this contract.");
        }

        boolean canAppeal = contract.getStatus() == ContractStatus.ACTIVE ||
                contract.getStatus() == ContractStatus.CONFIRMED ||
                contract.getStatus() == ContractStatus.UNDER_APPEAL;

        if (!canAppeal) {
            throw new RuntimeException("Cannot appeal contract in status: " + contract.getStatus());
        }

        List<Appeal> existingAppeals = appealRepository.findByContractId(contract.getId());
        if (existingAppeals.stream().anyMatch(a -> a.getStatus() == AppealStatus.PENDING)) {
            throw new RuntimeException("There is already a pending appeal for this contract");
        }

        Appeal appeal = Appeal.builder()
                .contract(contract)
                .tenant(tenant)
                .appealType(request.getAppealType())
                .reason(request.getReason())
                .evidenceDocuments(request.getEvidenceDocuments())
                .status(AppealStatus.PENDING)
                .build();

        contract.setStatus(ContractStatus.UNDER_APPEAL);
        return mapToResponse(appealRepository.save(appeal));
    }

    @Override
    public AppealResponse resolveAppeal(UUID appealId, String decision, String notes, UUID staffId) {
        Appeal appeal = appealRepository.findById(appealId)
                .orElseThrow(() -> new EntityNotFoundException("Appeal not found"));

        // Validation to prevent duplicate resolution (Return 400 via Exception)
        if (appeal.getStatus() != AppealStatus.PENDING) {
            throw new IllegalStateException("Appeal is already processed. Current status: " + appeal.getStatus());
        }

        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new EntityNotFoundException("Staff not found"));

        appeal.setStatus(AppealStatus.RESOLVED);
        appeal.setResolutionDecision(decision);
        appeal.setResolutionNotes(notes);
        appeal.setReviewedBy(staff);
        appeal.setReviewedAt(LocalDateTime.now());

        RentalContract contract = appeal.getContract();
        contract.setStatus(ContractStatus.ACTIVE);

        return mapToResponse(appealRepository.save(appeal));
    }

    @Override
    public AppealResponse rejectAppeal(UUID appealId, String reason, UUID staffId) {
        Appeal appeal = appealRepository.findById(appealId)
                .orElseThrow(() -> new EntityNotFoundException("Appeal not found"));

        if (appeal.getStatus() != AppealStatus.PENDING) {
            throw new IllegalStateException("Only pending appeals can be rejected.");
        }

        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new EntityNotFoundException("Staff not found"));

        appeal.setStatus(AppealStatus.REJECTED);
        appeal.setResolutionNotes(reason);
        appeal.setReviewedBy(staff);
        appeal.setReviewedAt(LocalDateTime.now());

        // Restore contract to ACTIVE if it was under appeal
        RentalContract contract = appeal.getContract();
        contract.setStatus(ContractStatus.ACTIVE);

        return mapToResponse(appealRepository.save(appeal));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppealResponse> getAppealsByTenant(UUID tenantId) {
        return appealRepository.findByTenantId(tenantId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppealResponse> getPendingAppeals() {
        return appealRepository.findByStatus(AppealStatus.PENDING).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AppealResponse getAppealById(UUID id) {
        Appeal appeal = appealRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Appeal not found"));
        return mapToResponse(appeal);
    }

    private AppealResponse mapToResponse(Appeal appeal) {
        return AppealResponse.builder()
                .id(appeal.getId())
                .contractId(appeal.getContract().getId())
                .contractAddress(appeal.getContract().getPropertyAddress())
                .tenantId(appeal.getTenant().getId())
                .tenantName(appeal.getTenant().getFirstName() + " " + appeal.getTenant().getLastName())
                .tenantEmail(appeal.getTenant().getEmail())
                .appealType(appeal.getAppealType())
                .reason(appeal.getReason())
                .evidenceDocuments(appeal.getEvidenceDocuments())
                .status(appeal.getStatus())
                .createdAt(appeal.getCreatedAt())
                .updatedAt(appeal.getUpdatedAt())
                .reviewedAt(appeal.getReviewedAt())
                .resolutionDecision(appeal.getResolutionDecision())
                .resolutionNotes(appeal.getResolutionNotes())
                .reviewedById(appeal.getReviewedBy() != null ? appeal.getReviewedBy().getId() : null)
                .reviewedByName(appeal.getReviewedBy() != null ?
                        appeal.getReviewedBy().getFirstName() + " " + appeal.getReviewedBy().getLastName() : null)
                .build();
    }
}