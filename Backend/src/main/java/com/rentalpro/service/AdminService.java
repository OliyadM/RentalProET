package com.rentalpro.service;

import com.rentalpro.model.dto.request.CreateOfficerRequest;
import com.rentalpro.model.dto.request.SystemConfigRequest;
import com.rentalpro.model.dto.response.OfficerResponse;
import com.rentalpro.model.dto.response.SystemConfigResponse;
import com.rentalpro.model.entity.SystemConfig;

import java.util.List;
import java.util.UUID;

public interface AdminService {

    // ── System Config ────────────────────────────────────────────────────────

    /** Returns the singleton config, creating defaults if none exists yet. */
    SystemConfigResponse getConfig();

    /** Updates the singleton config and returns the updated record. */
    SystemConfigResponse updateConfig(SystemConfigRequest request);

    /**
     * Returns the raw entity (used internally by services that need the
     * decimal fraction values, not the display percentages).
     */
    SystemConfig getConfigEntity();

    // ── Officer Management ───────────────────────────────────────────────────

    /** Returns all users with role SUBCITY_STAFF. */
    List<OfficerResponse> getAllOfficers();

    /** Creates a new SUBCITY_STAFF account and assigns them to a sub-city. */
    OfficerResponse createOfficer(CreateOfficerRequest request);

    /**
     * Activates or deactivates an officer's account.
     *
     * @param officerId the officer's UUID
     * @param active    true to activate, false to deactivate
     */
    OfficerResponse setOfficerStatus(UUID officerId, boolean active);
}
