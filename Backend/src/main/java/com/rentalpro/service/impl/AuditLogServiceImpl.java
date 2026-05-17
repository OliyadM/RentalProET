package com.rentalpro.service.impl;

import com.rentalpro.model.entity.AuditLog;
import com.rentalpro.model.entity.User;
import com.rentalpro.repository.AuditLogRepository;
import com.rentalpro.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Async
    public void logAction(String action, String entityType, UUID entityId, User user, String details) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .user(user)
                    .userRole(user != null ? user.getRole().name() : "SYSTEM")
                    .details(details)
                    .ipAddress(getCurrentIpAddress())
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Audit log created: {} - {}", action, entityType);
        } catch (Exception e) {
            log.error("Failed to save audit log", e);
        }
    }

    @Override
    public List<AuditLog> getLogsByUser(UUID userId) {
        return auditLogRepository.findByUserId(userId);
    }

    @Override
    public List<AuditLog> getLogsByEntity(String entityType, UUID entityId) {
        return auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId);
    }

    private String getCurrentIpAddress() {
        // In a real implementation, get from request context
        return "127.0.0.1";
    }
}