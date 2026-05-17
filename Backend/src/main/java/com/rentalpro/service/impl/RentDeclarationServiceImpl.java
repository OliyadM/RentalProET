package com.rentalpro.service.impl;

import com.rentalpro.model.dto.response.RentDeclarationResponse;
import com.rentalpro.model.entity.RentDeclaration;
import com.rentalpro.model.entity.RentalContract;
import com.rentalpro.repository.RentDeclarationRepository;
import com.rentalpro.repository.RentalContractRepository;
import com.rentalpro.service.AuditLogService;
import com.rentalpro.service.RentAnalyzerService;
import com.rentalpro.service.RentDeclarationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RentDeclarationServiceImpl implements RentDeclarationService {

    private final RentDeclarationRepository declarationRepository;
    private final RentalContractRepository contractRepository;
    private final RentAnalyzerService analyzerService;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public RentDeclarationResponse createDeclaration(UUID contractId, LocalDate period, Double declaredRent, UUID landlordId) {
        RentalContract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Contract not found"));

        if (!contract.getLandlord().getId().equals(landlordId)) {
            throw new RuntimeException("Not authorized to declare rent for this contract");
        }

        RentDeclaration declaration = RentDeclaration.builder()
                .contract(contract)
                .declarationPeriod(period.withDayOfMonth(1))
                .declaredRent(declaredRent)
                .isVerified(false)
                .build();

        RentDeclaration saved = declarationRepository.save(declaration);
        analyzeDeclaration(saved.getId());

        auditLogService.logAction("CREATE_DECLARATION", "RentDeclaration", saved.getId(),
                contract.getLandlord(), "Declared rent: " + declaredRent);

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public RentDeclaration analyzeDeclaration(UUID declarationId) {
        RentDeclaration declaration = declarationRepository.findById(declarationId)
                .orElseThrow(() -> new RuntimeException("Declaration not found"));

        var benchmark = analyzerService.calculateFairRent(
                declaration.getContract().getRentalUnit().getProperty().getId()
        );

        double declaredRent = declaration.getDeclaredRent();
        double suggestedRent = benchmark.getSuggestedRent();
        double deviation = Math.abs(declaredRent - suggestedRent) / suggestedRent;

        boolean isAnomaly = deviation > 0.15;
        double anomalyScore = Math.min(deviation / 0.3, 1.0);

        String reason = null;
        if (isAnomaly) {
            if (declaredRent < suggestedRent * 0.85) {
                reason = String.format("Declared rent (%.2f) is %.1f%% below AI benchmark (%.2f). Possible under-declaration.",
                        declaredRent, deviation * 100, suggestedRent);
            } else if (declaredRent > suggestedRent * 1.15) {
                reason = String.format("Declared rent (%.2f) is %.1f%% above AI benchmark (%.2f). Verify luxury features.",
                        declaredRent, deviation * 100, suggestedRent);
            }
        }

        declaration.setAiBenchmarkRent(suggestedRent);
        declaration.setAnomalyScore(anomalyScore);
        declaration.setIsAnomaly(isAnomaly);
        declaration.setAnomalyReason(reason);
        declaration.setEstimatedTax(declaredRent * 0.10);

        return declarationRepository.save(declaration);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RentDeclarationResponse> getDeclarationsByContract(UUID contractId) {
        return declarationRepository.findByContractId(contractId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RentDeclarationResponse> getAnomalies(String subCity) {
        return declarationRepository.findBySubCity(subCity).stream()
                .filter(RentDeclaration::getIsAnomaly)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RentDeclarationResponse> getUnverifiedDeclarations(String subCity) {
        return declarationRepository.findBySubCity(subCity).stream()
                .filter(d -> !d.getIsVerified()) // Filter for unverified only
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    @Override
    @Transactional
    public RentDeclarationResponse verifyDeclaration(UUID declarationId, String notes, UUID staffId) {
        RentDeclaration declaration = declarationRepository.findById(declarationId)
                .orElseThrow(() -> new RuntimeException("Declaration not found"));

        // 1. Add the state check
        if (Boolean.TRUE.equals(declaration.getIsVerified())) {
            throw new IllegalStateException("This declaration has already been verified.");
        }

        // 2. Proceed with verification
        declaration.setIsVerified(true);
        // declaration.setVerifiedBy(staffId); // Recommended if you have this field
        // declaration.setVerificationDate(LocalDateTime.now());

        RentDeclaration saved = declarationRepository.save(declaration);

        auditLogService.logAction("VERIFY_DECLARATION", "RentDeclaration", saved.getId(),
                null, "Verified with notes: " + notes);

        return mapToResponse(saved);
    }
    private RentDeclarationResponse mapToResponse(RentDeclaration d) {
        return RentDeclarationResponse.builder()
                .id(d.getId())
                .contractId(d.getContract().getId())
                .declarationPeriod(d.getDeclarationPeriod())
                .declaredRent(d.getDeclaredRent())
                .aiBenchmarkRent(d.getAiBenchmarkRent())
                .anomalyScore(d.getAnomalyScore())
                .isAnomaly(d.getIsAnomaly())
                .anomalyReason(d.getAnomalyReason())
                .estimatedTax(d.getEstimatedTax())
                .isVerified(d.getIsVerified())
                .createdAt(d.getCreatedAt())
                .build();
    }
}