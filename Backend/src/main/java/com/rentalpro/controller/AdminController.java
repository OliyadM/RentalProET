package com.rentalpro.controller;

import com.rentalpro.model.dto.request.CreateOfficerRequest;
import com.rentalpro.model.dto.request.SystemConfigRequest;
import com.rentalpro.model.dto.response.OfficerResponse;
import com.rentalpro.model.dto.response.SystemConfigResponse;
import com.rentalpro.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
// No class-level @PreAuthorize — each method declares its own so the public
// contract-duration endpoint can be accessed by any authenticated user.
public class AdminController {

    private final AdminService adminService;

    // ── System Configuration ─────────────────────────────────────────────────

    /** GET /api/admin/config — full config, admin only */
    @GetMapping("/config")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<SystemConfigResponse> getConfig() {
        return ResponseEntity.ok(adminService.getConfig());
    }

    /** PUT /api/admin/config — update all settings, admin only */
    @PutMapping("/config")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<SystemConfigResponse> updateConfig(
            @Valid @RequestBody SystemConfigRequest request) {
        return ResponseEntity.ok(adminService.updateConfig(request));
    }

    /**
     * GET /api/admin/settings/contract-duration
     * Public (any authenticated user) — returns the current minimum contract
     * duration so the landlord contract form can enforce it client-side.
     */
    @GetMapping("/settings/contract-duration")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Integer>> getContractDurationSetting() {
        Integer minYears = adminService.getConfigEntity().getMinimumContractYears();
        return ResponseEntity.ok(Map.of("minimumContractYears", minYears));
    }

    // ── Officer Management ───────────────────────────────────────────────────

    /** GET /api/admin/officers — list all officers, admin only */
    @GetMapping("/officers")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<List<OfficerResponse>> getAllOfficers() {
        return ResponseEntity.ok(adminService.getAllOfficers());
    }

    /** POST /api/admin/officers — provision new officer, admin only */
    @PostMapping("/officers")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<OfficerResponse> createOfficer(
            @Valid @RequestBody CreateOfficerRequest request) {
        return ResponseEntity.ok(adminService.createOfficer(request));
    }

    /** PUT /api/admin/officers/{id}/status — activate/deactivate, admin only */
    @PutMapping("/officers/{id}/status")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<OfficerResponse> setOfficerStatus(
            @PathVariable UUID id,
            @RequestParam boolean active) {
        return ResponseEntity.ok(adminService.setOfficerStatus(id, active));
    }
}
