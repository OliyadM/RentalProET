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

        // Frontend sends percentage values (e.g. 10.0); store as fractions (0.10)
        config.setTaxRate(request.getTaxRatePercent() / 100.0);
        config.setAnomalyThresholdPercentage(request.getAnomalyThresholdPercent() / 100.0);
        config.setMaxRentIncreaseCap(request.getMaxRentIncreaseCapPercent() / 100.0);

        SystemConfig saved = configRepository.save(config);
        log.info("SystemConfig updated — taxRate={}% anomalyThreshold={}% rentCap={}%",
                request.getTaxRatePercent(),
                request.getAnomalyThresholdPercent(),
                request.getMaxRentIncreaseCapPercent());

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
        return SystemConfigResponse.builder()
                .id(c.getId())
                // Convert fractions back to percentages for the frontend
                .taxRatePercent(c.getTaxRate() * 100.0)
                .anomalyThresholdPercent(c.getAnomalyThresholdPercentage() * 100.0)
                .maxRentIncreaseCapPercent(c.getMaxRentIncreaseCap() * 100.0)
                .updatedAt(c.getUpdatedAt())
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
