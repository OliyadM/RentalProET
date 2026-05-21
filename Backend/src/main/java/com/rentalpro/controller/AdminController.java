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
import java.util.UUID;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRATOR')")   // All routes in this controller require ADMINISTRATOR
public class AdminController {

    private final AdminService adminService;

    // ── System Configuration ─────────────────────────────────────────────────

    /**
     * GET /api/admin/config
     * Returns the current platform-wide system configuration.
     */
    @GetMapping("/config")
    public ResponseEntity<SystemConfigResponse> getConfig() {
        return ResponseEntity.ok(adminService.getConfig());
    }

    /**
     * PUT /api/admin/config
     * Updates tax rate, anomaly threshold, and rent increase cap.
     */
    @PutMapping("/config")
    public ResponseEntity<SystemConfigResponse> updateConfig(
            @Valid @RequestBody SystemConfigRequest request) {
        return ResponseEntity.ok(adminService.updateConfig(request));
    }

    // ── Officer Management ───────────────────────────────────────────────────

    /**
     * GET /api/admin/officers
     * Returns all users with role SUBCITY_STAFF.
     */
    @GetMapping("/officers")
    public ResponseEntity<List<OfficerResponse>> getAllOfficers() {
        return ResponseEntity.ok(adminService.getAllOfficers());
    }

    /**
     * POST /api/admin/officers
     * Provisions a new Government Officer account.
     */
    @PostMapping("/officers")
    public ResponseEntity<OfficerResponse> createOfficer(
            @Valid @RequestBody CreateOfficerRequest request) {
        return ResponseEntity.ok(adminService.createOfficer(request));
    }

    /**
     * PUT /api/admin/officers/{id}/status?active=true|false
     * Activates or deactivates an officer's account.
     */
    @PutMapping("/officers/{id}/status")
    public ResponseEntity<OfficerResponse> setOfficerStatus(
            @PathVariable UUID id,
            @RequestParam boolean active) {
        return ResponseEntity.ok(adminService.setOfficerStatus(id, active));
    }
}
