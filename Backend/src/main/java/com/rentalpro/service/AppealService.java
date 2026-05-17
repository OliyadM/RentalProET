package com.rentalpro.service;

import com.rentalpro.model.dto.request.AppealRequest;
import com.rentalpro.model.dto.response.AppealResponse;
import java.util.List;
import java.util.UUID;

public interface AppealService {
    AppealResponse createAppeal(AppealRequest request, UUID tenantId);
    AppealResponse resolveAppeal(UUID appealId, String decision, String notes, UUID staffId);

    // Added for Rejection logic
    AppealResponse rejectAppeal(UUID appealId, String reason, UUID staffId);

    List<AppealResponse> getAppealsByTenant(UUID tenantId);
    List<AppealResponse> getPendingAppeals();
    AppealResponse getAppealById(UUID id);
}