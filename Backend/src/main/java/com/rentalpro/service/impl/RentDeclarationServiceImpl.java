package com.rentalpro.service.impl;

import com.rentalpro.model.dto.response.RentDeclarationResponse;
import com.rentalpro.model.dto.response.TaxCalculationResponse;
import com.rentalpro.model.entity.RentDeclaration;
import com.rentalpro.model.entity.RentalContract;
import com.rentalpro.model.entity.User;
import com.rentalpro.model.enums.PropertyType;
import com.rentalpro.repository.RentDeclarationRepository;
import com.rentalpro.repository.RentalContractRepository;
import com.rentalpro.service.AdminService;
import com.rentalpro.service.AuditLogService;
import com.rentalpro.service.RentAnalyzerService;
import com.rentalpro.service.RentDeclarationService;
import com.rentalpro.service.TaxCalculationService;
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
    private final AdminService adminService;
    private final TaxCalculationService taxCalculationService;

    @Override
    @Transactional
    public RentDeclarationResponse createDeclaration(
            UUID contractId, LocalDate period, Double declaredRent, UUID landlordId, boolean claimDeduction) {
        RentalContract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Contract not found"));

        if (!contract.getLandlord().getId().equals(landlordId)) {
            throw new RuntimeException("Not authorized to declare rent for this contract");
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

        return mapToResponse(analyzed);
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

        double anomalyThreshold = adminService.getConfigEntity().getAnomalyThresholdPercentage();
        boolean isAnomaly = deviation > anomalyThreshold;
        double anomalyScore = Math.min(deviation / (anomalyThreshold * 2), 1.0);

        String reason = null;
        if (isAnomaly) {
            if (declaredRent < suggestedRent * (1 - anomalyThreshold)) {
                reason = String.format("Declared rent (%.2f) is %.1f%% below AI benchmark (%.2f). Possible under-declaration.",
                        declaredRent, deviation * 100, suggestedRent);
            } else if (declaredRent > suggestedRent * (1 + anomalyThreshold)) {
                reason = String.format("Declared rent (%.2f) is %.1f%% above AI benchmark (%.2f). Verify luxury features.",
                        declaredRent, deviation * 100, suggestedRent);
            }
        }

        declaration.setAiBenchmarkRent(suggestedRent);
        declaration.setAnomalyScore(anomalyScore);
        declaration.setIsAnomaly(isAnomaly);
        declaration.setAnomalyReason(reason);

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
                .createdAt(d.getCreatedAt())
                .build();
    }
}
