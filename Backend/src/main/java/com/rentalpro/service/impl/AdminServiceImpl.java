package com.rentalpro.service.impl;

import com.rentalpro.model.dto.request.CreateOfficerRequest;
import com.rentalpro.model.dto.request.SystemConfigRequest;
import com.rentalpro.model.dto.response.OfficerResponse;
import com.rentalpro.model.dto.response.SystemConfigResponse;
import com.rentalpro.model.entity.SystemConfig;
import com.rentalpro.model.entity.User;
import com.rentalpro.model.enums.UserRole;
import com.rentalpro.repository.SystemConfigRepository;
import com.rentalpro.repository.UserRepository;
import com.rentalpro.service.AdminService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final SystemConfigRepository configRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ── System Config ────────────────────────────────────────────────────────

    @Override
    @Transactional          // must be writable — getOrCreateConfig() may INSERT on first call
    public SystemConfigResponse getConfig() {
        return mapConfigToResponse(getOrCreateConfig());
    }

    @Override
    @Transactional
    public SystemConfigResponse updateConfig(SystemConfigRequest request) {
        SystemConfig config = getOrCreateConfig();

        config.setAnomalyThresholdPercentage(request.getAnomalyThresholdPercent() / 100.0);
        config.setMaxRentIncreaseCap(request.getMaxRentIncreaseCapPercent() / 100.0);
        config.setMinimumContractYears(request.getMinimumContractYears());
        config.setTaxRuleVersion(request.getTaxRuleVersion());
        config.setBusinessFlatTaxRate(request.getBusinessFlatTaxRatePercent() / 100.0);
        config.setResidentialDeductionPercent(request.getResidentialDeductionPercent() / 100.0);

        var bands = request.getTaxBands();
        config.setBand1Min(bands.get(0).getMinIncome());  config.setBand1Max(bands.get(0).getMaxIncome());
        config.setBand1Rate(bands.get(0).getRatePercent() / 100.0); config.setBand1Deductible(bands.get(0).getDeductibleAmount());
        config.setBand2Min(bands.get(1).getMinIncome());  config.setBand2Max(bands.get(1).getMaxIncome());
        config.setBand2Rate(bands.get(1).getRatePercent() / 100.0); config.setBand2Deductible(bands.get(1).getDeductibleAmount());
        config.setBand3Min(bands.get(2).getMinIncome());  config.setBand3Max(bands.get(2).getMaxIncome());
        config.setBand3Rate(bands.get(2).getRatePercent() / 100.0); config.setBand3Deductible(bands.get(2).getDeductibleAmount());
        config.setBand4Min(bands.get(3).getMinIncome());  config.setBand4Max(bands.get(3).getMaxIncome());
        config.setBand4Rate(bands.get(3).getRatePercent() / 100.0); config.setBand4Deductible(bands.get(3).getDeductibleAmount());
        config.setBand5Min(bands.get(4).getMinIncome());  config.setBand5Max(bands.get(4).getMaxIncome());
        config.setBand5Rate(bands.get(4).getRatePercent() / 100.0); config.setBand5Deductible(bands.get(4).getDeductibleAmount());
        config.setBand6Min(bands.get(5).getMinIncome());  config.setBand6Max(bands.get(5).getMaxIncome());
        config.setBand6Rate(bands.get(5).getRatePercent() / 100.0); config.setBand6Deductible(bands.get(5).getDeductibleAmount());

        SystemConfig saved = configRepository.save(config);
        log.info("SystemConfig updated — version={} businessRate={}% deduction={}%",
                request.getTaxRuleVersion(),
                request.getBusinessFlatTaxRatePercent(),
                request.getResidentialDeductionPercent());
        return mapConfigToResponse(saved);
    }

    @Override
    @Transactional          // must be writable — getOrCreateConfig() may INSERT on first call
    public SystemConfig getConfigEntity() {
        return getOrCreateConfig();
    }

    // ── Officer Management ───────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<OfficerResponse> getAllOfficers() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == UserRole.SUBCITY_STAFF)
                .map(this::mapOfficerToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OfficerResponse createOfficer(CreateOfficerRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("An account with email '" + request.getEmail() + "' already exists");
        }

        User officer = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.SUBCITY_STAFF)
                .subCityZone(request.getSubCityZone())
                .isActive(true)
                .accountStatus(com.rentalpro.model.enums.AccountStatus.VERIFIED)
                .entityType(com.rentalpro.model.enums.EntityType.INDIVIDUAL)
                .build();

        User saved = userRepository.save(officer);
        log.info("New officer provisioned — email={} subCity={}", saved.getEmail(), saved.getSubCityZone());
        return mapOfficerToResponse(saved);
    }

    @Override
    @Transactional
    public OfficerResponse setOfficerStatus(UUID officerId, boolean active) {
        User officer = userRepository.findById(officerId)
                .orElseThrow(() -> new EntityNotFoundException("Officer not found: " + officerId));

        if (officer.getRole() != UserRole.SUBCITY_STAFF) {
            throw new RuntimeException("User is not a SUBCITY_STAFF officer");
        }

        officer.setIsActive(active);
        User saved = userRepository.save(officer);
        log.info("Officer {} status set to active={}", officerId, active);
        return mapOfficerToResponse(saved);
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    /**
     * Returns the singleton config row, creating it with safe defaults
     * if the table is empty (first-run scenario).
     *
     * Must always be called from a writable @Transactional context.
     */
    @Transactional
    protected SystemConfig getOrCreateConfig() {
        return configRepository.findTopByOrderByUpdatedAtAsc().orElseGet(() -> {
            log.info("No SystemConfig found — inserting default configuration");
            SystemConfig defaults = SystemConfig.builder()
                    .taxRate(0.10)
                    .anomalyThresholdPercentage(0.15)
                    .maxRentIncreaseCap(0.10)
                    .build();
            return configRepository.saveAndFlush(defaults);
        });
    }

    private SystemConfigResponse mapConfigToResponse(SystemConfig c) {
        var bands = List.of(
            bandResp(c.getBand1Min(), c.getBand1Max(), c.getBand1Rate(), c.getBand1Deductible()),
            bandResp(c.getBand2Min(), c.getBand2Max(), c.getBand2Rate(), c.getBand2Deductible()),
            bandResp(c.getBand3Min(), c.getBand3Max(), c.getBand3Rate(), c.getBand3Deductible()),
            bandResp(c.getBand4Min(), c.getBand4Max(), c.getBand4Rate(), c.getBand4Deductible()),
            bandResp(c.getBand5Min(), c.getBand5Max(), c.getBand5Rate(), c.getBand5Deductible()),
            bandResp(c.getBand6Min(), c.getBand6Max(), c.getBand6Rate(), c.getBand6Deductible())
        );
        return SystemConfigResponse.builder()
                .id(c.getId())
                .anomalyThresholdPercent(c.getAnomalyThresholdPercentage() * 100.0)
                .maxRentIncreaseCapPercent(c.getMaxRentIncreaseCap() * 100.0)
                .minimumContractYears(c.getMinimumContractYears())
                .taxRuleVersion(c.getTaxRuleVersion())
                .businessFlatTaxRatePercent(c.getBusinessFlatTaxRate() * 100.0)
                .residentialDeductionPercent(c.getResidentialDeductionPercent() * 100.0)
                .taxBands(bands)
                .updatedAt(c.getUpdatedAt())
                .build();
    }

    private SystemConfigResponse.TaxBandResponse bandResp(
            double min, double max, double rate, double deductible) {
        String label = max >= 999_999_999
                ? String.format("Above ETB %.0f", min - 1)
                : String.format("ETB %.0f – %.0f", min, max);
        return SystemConfigResponse.TaxBandResponse.builder()
                .minIncome(min).maxIncome(max)
                .ratePercent(rate * 100.0)
                .deductibleAmount(deductible)
                .label(label)
                .build();
    }

    private OfficerResponse mapOfficerToResponse(User u) {
        return OfficerResponse.builder()
                .id(u.getId())
                .firstName(u.getFirstName())
                .lastName(u.getLastName())
                .email(u.getEmail())
                .phoneNumber(u.getPhoneNumber())
                .subCityZone(u.getSubCityZone())
                .isActive(u.getIsActive())
                .createdAt(u.getCreatedAt())
                .build();
    }
}
