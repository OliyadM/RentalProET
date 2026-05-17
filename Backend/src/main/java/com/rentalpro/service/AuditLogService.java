package com.rentalpro.service;

import com.rentalpro.model.entity.AuditLog;
import com.rentalpro.model.entity.User;

import java.util.List;
import java.util.UUID;

public interface AuditLogService {

    void logAction(String action, String entityType, UUID entityId, User user, String details);

    List<AuditLog> getLogsByUser(UUID userId);

    List<AuditLog> getLogsByEntity(String entityType, UUID entityId);
}