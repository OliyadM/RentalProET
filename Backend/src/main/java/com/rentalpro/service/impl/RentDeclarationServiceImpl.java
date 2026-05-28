package com.rentalpro.service.impl;

import com.rentalpro.model.dto.response.RentDeclarationResponse;
import com.rentalpro.model.dto.response.TaxCalculationResponse;
import com.rentalpro.model.entity.RentDeclaration;
import com.rentalpro.model.entity.RentalContract;
import com.rentalpro.model.entity.RentalUnit;
import com.rentalpro.model.entity.Property;
import com.rentalpro.model.entity.User;
import com.rentalpro.model.enums.PropertyType;
import com.rentalpro.model.enums.NotificationType;
import com.rentalpro.repository.RentDeclarationRepository;
import com.rentalpro.repository.RentalContractRepository;
import com.rentalpro.service.AdminService;
import com.rentalpro.service.AuditLogService;
import com.rentalpro.service.NotificationService;
import com.rentalpro.service.RentAnalyzerService;
import com.rentalpro.service.RentDeclarationService;
import com.rentalpro.service.TaxCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RentDeclarationServiceImpl implements RentDeclarationService {

    private final RentDeclarationRepository declarationRepository;
    private final RentalContractRepository contractRepository;
    private final RentAnalyzerService analyzerService;
    private final AuditLogService auditLogService;
    private final AdminService adminService;
    private final TaxCalculationService taxCalculationService;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public RentDeclarationResponse createDeclaration(
            UUID contractId, LocalDate period, Double declaredRent, UUID landlordId, boolean claimDeduction) {
        RentalContract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Contract not found"));

        if (!contract.getLandlord().getId().equals(landlordId)) {
            throw new RuntimeException("Not authorized to declare rent for this contract");
        }

        // Guard: declared rent cannot be less than the contracted monthly rent.
        // Under-declaring below the agreed amount is a tax evasion signal —
        // the landlord cannot claim they received less than what the tenant
        // is contractually obligated to pay.
        if (declaredRent < contract.getMonthlyRent()) {
            throw new RuntimeException(String.format(
                "Declared rent (ETB %.2f) cannot be less than the contracted monthly rent (ETB %.2f). " +
                "If the tenant paid less than agreed, please contact your officer.",
                declaredRent, contract.getMonthlyRent()));
        }

        RentDeclaration declaration = RentDeclaration.builder()
                .contract(contract)
                .declarationPeriod(period.withDayOfMonth(1))
                .declaredRent(declaredRent)
                .claimDeduction(claimDeduction)
                .isVerified(false)
                .build();

        RentDeclaration saved = declarationRepository.save(declaration);
        RentDeclaration analyzed = analyzeDeclaration(saved.getId());

        auditLogService.logAction("CREATE_DECLARATION", "RentDeclaration", saved.getId(),
                contract.getLandlord(), "Declared rent: " + declaredRent);

        // Notify officers in the property's sub-city
        String subCity = contract.getRentalUnit().getProperty().getSubCity();
        if (subCity != null && !subCity.isBlank()) {
            try {
                String landlordName = contract.getLandlord().getFirstName() + " " + contract.getLandlord().getLastName();
                String anomalyNote = analyzed.getIsAnomaly() ? " ⚠ Anomaly detected." : "";
                String msg = String.format(
                        "%s declared ETB %.0f for %s (period: %s).%s",
                        landlordName, declaredRent,
                        contract.getPropertyAddress(),
                        period.toString(),
                        anomalyNote);
                notificationService.sendToSubCityOfficers(
                        subCity,
                        NotificationType.DECLARATION_SUBMITTED,
                        msg,
                        analyzed.getId());
            } catch (Exception e) {
                log.warn("Failed to notify officers of declaration {}: {}", analyzed.getId(), e.getMessage());
            }
        }

        return mapToResponse(analyzed);
    }

    @Override
    @Transactional
    public RentDeclaration analyzeDeclaration(UUID declarationId) {
        RentDeclaration declaration = declarationRepository.findById(declarationId)
                .orElseThrow(() -> new RuntimeException("Declaration not found"));

        RentalContract contract = declaration.getContract();
        RentalUnit unit         = contract.getRentalUnit();
        Property property       = unit.getProperty();

        // Use unit-aware benchmark (price-per-m² algorithm)
        var benchmark = analyzerService.calculateFairRent(property.getId(), unit.getId());

        double declaredRent  = declaration.getDeclaredRent();
        double suggestedRent = benchmark.getSuggestedRent();
        double lowerBound    = benchmark.getMinRent();
        double upperBound    = benchmark.getMaxRent();

        // Determine anomaly using the benchmark range (not a fixed threshold)
        boolean isAnomaly;
        String  direction = null;
        double  deviation = 0;

        if (declaredRent < lowerBound) {
            isAnomaly = true;
            direction = "UNDER_REPORTED";
            deviation = (suggestedRent - declaredRent) / suggestedRent;
        } else if (declaredRent > upperBound) {
            isAnomaly = true;
            direction = "OVER_REPORTED";
            deviation = (declaredRent - suggestedRent) / suggestedRent;
        } else {
            isAnomaly = false;
            deviation = Math.abs(declaredRent - suggestedRent) / suggestedRent;
        }

        // Severity bands
        String severity = null;
        if (isAnomaly) {
            double pct = deviation * 100;
            severity = pct < 25 ? "LOW" : pct < 50 ? "MEDIUM" : "HIGH";
        }

        // anomalyScore kept as 0–1 for backward compatibility
        double anomalyThreshold = adminService.getConfigEntity().getAnomalyThresholdPercentage();
        double anomalyScore = Math.min(deviation / (anomalyThreshold * 2), 1.0);

        // Human-readable reason
        String reason = null;
        if (isAnomaly) {
            reason = String.format(
                    "Declared rent ETB %.0f is %.1f%% %s the expected range of ETB %.0f–%.0f " +
                    "for comparable properties in %s (based on %d records, Level %d match). " +
                    "Severity: %s.",
                    declaredRent,
                    deviation * 100,
                    "UNDER_REPORTED".equals(direction) ? "below" : "above",
                    lowerBound, upperBound,
                    property.getSubCity(),
                    benchmark.getSampleSize() != null ? benchmark.getSampleSize() : 0,
                    benchmark.getFallbackLevel() != null ? benchmark.getFallbackLevel() : 6,
                    severity);
        }

        // Store all benchmark metadata
        declaration.setAiBenchmarkRent(suggestedRent);
        declaration.setAnomalyScore(anomalyScore);
        declaration.setIsAnomaly(isAnomaly);
        declaration.setAnomalyReason(reason);
        declaration.setBenchmarkPricePerM2(benchmark.getPricePerSqm());
        declaration.setBenchmarkLowerBound(lowerBound);
        declaration.setBenchmarkUpperBound(upperBound);
        declaration.setBenchmarkSampleSize(benchmark.getSampleSize());
        declaration.setBenchmarkFallbackLevel(benchmark.getFallbackLevel());
        declaration.setBenchmarkStdDev(benchmark.getStdDev());
        declaration.setAnomalySeverity(severity);
        declaration.setAnomalyDirection(direction);

        applyTaxCalculation(declaration);

        return declarationRepository.save(declaration);
    }

    private void applyTaxCalculation(RentDeclaration declaration) {
        RentalContract contract = declaration.getContract();
        User landlord = contract.getLandlord();
        PropertyType propertyType = contract.getRentalUnit().getProperty().getPropertyType();
        boolean claimDeduction = Boolean.TRUE.equals(declaration.getClaimDeduction());

        TaxCalculationResponse tax = taxCalculationService.calculate(
                declaration.getDeclaredRent(),
                landlord.getEntityType(),
                propertyType,
                claimDeduction
        );

        declaration.setEstimatedTax(tax.getMonthlyTax());
        declaration.setDeductionApplied(tax.getDeductionApplied());
        declaration.setDeductionAmount(tax.getDeductionAmount());
        declaration.setTaxableAnnualIncome(tax.getTaxableAnnualIncome());
        declaration.setAnnualTax(tax.getAnnualTax());
        declaration.setEffectiveTaxRate(tax.getEffectiveTaxRate());
        declaration.setTaxRuleVersion(tax.getTaxRuleVersion());
        declaration.setMixedUseDeductionWarning(tax.getMixedUseDeductionWarning());
        declaration.setTaxAdvisoryNote(String.join(" ", tax.getAdvisoryNotes()));
        declaration.setTaxCompliant(tax.getAnnualTax() >= 0);
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
                .filter(d -> !d.getIsVerified())
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RentDeclarationResponse getDeclarationById(UUID declarationId) {
        RentDeclaration declaration = declarationRepository.findById(declarationId)
                .orElseThrow(() -> new RuntimeException("Declaration not found"));
        return mapToResponse(declaration);
    }

    @Override
    @Transactional
    public RentDeclarationResponse verifyDeclaration(UUID declarationId, String notes, UUID staffId) {
        RentDeclaration declaration = declarationRepository.findById(declarationId)
                .orElseThrow(() -> new RuntimeException("Declaration not found"));

        if (Boolean.TRUE.equals(declaration.getIsVerified())) {
            throw new IllegalStateException("This declaration has already been verified.");
        }

        declaration.setIsVerified(true);

        RentDeclaration saved = declarationRepository.save(declaration);

        auditLogService.logAction("VERIFY_DECLARATION", "RentDeclaration", saved.getId(),
                null, "Verified with notes: " + notes);

        // Notify the landlord that their declaration has been verified
        try {
            RentalContract contract = saved.getContract();
            String msg = String.format(
                    "Your rent declaration of ETB %.0f for %s (period: %s) has been verified by an officer.",
                    saved.getDeclaredRent(),
                    contract.getPropertyAddress(),
                    saved.getDeclarationPeriod().toString());
            notificationService.send(
                    contract.getLandlord().getId(),
                    NotificationType.DECLARATION_VERIFIED,
                    msg,
                    saved.getId());
        } catch (Exception e) {
            log.warn("Failed to notify landlord of declaration verification {}: {}", saved.getId(), e.getMessage());
        }

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public RentDeclarationResponse rejectDeclaration(UUID declarationId, String reason, UUID staffId) {
        RentDeclaration declaration = declarationRepository.findById(declarationId)
                .orElseThrow(() -> new RuntimeException("Declaration not found"));

        if (Boolean.TRUE.equals(declaration.getIsVerified())) {
            throw new IllegalStateException("Cannot reject a declaration that has already been verified.");
        }
        if (Boolean.TRUE.equals(declaration.getIsRejected())) {
            throw new IllegalStateException("This declaration has already been rejected.");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Rejection reason is required.");
        }

        declaration.setIsRejected(true);
        declaration.setRejectionReason(reason);

        RentDeclaration saved = declarationRepository.save(declaration);

        auditLogService.logAction("REJECT_DECLARATION", "RentDeclaration", saved.getId(),
                null, "Rejected: " + reason);

        // Notify the landlord that their declaration was rejected
        try {
            RentalContract contract = saved.getContract();
            String msg = String.format(
                    "Your rent declaration of ETB %.0f for %s (period: %s) was rejected by an officer. Reason: %s",
                    saved.getDeclaredRent(),
                    contract.getPropertyAddress(),
                    saved.getDeclarationPeriod().toString(),
                    reason);
            notificationService.send(
                    contract.getLandlord().getId(),
                    NotificationType.DECLARATION_REJECTED,
                    msg,
                    saved.getId());
        } catch (Exception e) {
            log.warn("Failed to notify landlord of declaration rejection {}: {}", saved.getId(), e.getMessage());
        }

        return mapToResponse(saved);
    }

    private RentDeclarationResponse mapToResponse(RentDeclaration d) {
        TaxCalculationResponse taxDetails = TaxCalculationResponse.builder()
                .monthlyGrossRent(d.getDeclaredRent())
                .annualGrossRent(d.getDeclaredRent() != null ? d.getDeclaredRent() * 12 : null)
                .deductionApplied(d.getDeductionApplied())
                .deductionAmount(d.getDeductionAmount())
                .taxableAnnualIncome(d.getTaxableAnnualIncome())
                .annualTax(d.getAnnualTax())
                .monthlyTax(d.getEstimatedTax())
                .effectiveTaxRate(d.getEffectiveTaxRate())
                .taxRuleVersion(d.getTaxRuleVersion())
                .mixedUseDeductionWarning(d.getMixedUseDeductionWarning())
                .build();

        return RentDeclarationResponse.builder()
                .id(d.getId())
                .contractId(d.getContract().getId())
                .declarationPeriod(d.getDeclarationPeriod())
                .declaredRent(d.getDeclaredRent())
                .aiBenchmarkRent(d.getAiBenchmarkRent())
                .anomalyScore(d.getAnomalyScore())
                .isAnomaly(d.getIsAnomaly())
                .anomalyReason(d.getAnomalyReason())
                .benchmarkPricePerM2(d.getBenchmarkPricePerM2())
                .benchmarkLowerBound(d.getBenchmarkLowerBound())
                .benchmarkUpperBound(d.getBenchmarkUpperBound())
                .benchmarkSampleSize(d.getBenchmarkSampleSize())
                .benchmarkFallbackLevel(d.getBenchmarkFallbackLevel())
                .benchmarkStdDev(d.getBenchmarkStdDev())
                .anomalySeverity(d.getAnomalySeverity())
                .anomalyDirection(d.getAnomalyDirection())
                .estimatedTax(d.getEstimatedTax())
                .claimDeduction(d.getClaimDeduction())
                .deductionApplied(d.getDeductionApplied())
                .deductionAmount(d.getDeductionAmount())
                .taxableAnnualIncome(d.getTaxableAnnualIncome())
                .annualTax(d.getAnnualTax())
                .effectiveTaxRate(d.getEffectiveTaxRate())
                .taxRuleVersion(d.getTaxRuleVersion())
                .mixedUseDeductionWarning(d.getMixedUseDeductionWarning())
                .taxAdvisoryNote(d.getTaxAdvisoryNote())
                .taxDetails(taxDetails)
                .isVerified(d.getIsVerified())
                .isRejected(d.getIsRejected())
                .rejectionReason(d.getRejectionReason())
                .createdAt(d.getCreatedAt())
                .build();
    }
}
