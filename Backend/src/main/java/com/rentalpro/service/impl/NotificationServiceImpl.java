package com.rentalpro.service.impl;

import com.rentalpro.model.dto.response.NotificationResponse;
import com.rentalpro.model.entity.Notification;
import com.rentalpro.model.entity.User;
import com.rentalpro.model.enums.NotificationType;
import com.rentalpro.model.enums.UserRole;
import com.rentalpro.repository.NotificationRepository;
import com.rentalpro.repository.UserRepository;
import com.rentalpro.service.NotificationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void send(UUID recipientId, NotificationType type, String message, UUID relatedEntityId) {
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new EntityNotFoundException("Recipient not found: " + recipientId));

        Notification notification = Notification.builder()
                .recipient(recipient)
                .type(type)
                .message(message)
                .isRead(false)
                .relatedEntityId(relatedEntityId)
                .build();

        notificationRepository.save(notification);
        log.debug("Notification sent to user {} — type={} message={}", recipientId, type, message);
    }

    @Override
    @Transactional
    public void sendToSubCityOfficers(String subCity, NotificationType type, String message, UUID relatedEntityId) {
        List<User> officers = notificationRepository.findUsersByRoleAndSubCity(UserRole.SUBCITY_STAFF, subCity);

        if (officers.isEmpty()) {
            log.warn("No SUBCITY_STAFF found for subCity='{}' — notification not delivered", subCity);
            return;
        }

        List<Notification> notifications = officers.stream()
                .map(officer -> Notification.builder()
                        .recipient(officer)
                        .type(type)
                        .message(message)
                        .isRead(false)
                        .relatedEntityId(relatedEntityId)
                        .build())
                .collect(Collectors.toList());

        notificationRepository.saveAll(notifications);
        log.debug("Notification fanned out to {} officers in subCity='{}' — type={}", officers.size(), subCity, type);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications(UUID recipientId) {
        return notificationRepository
                .findByRecipientIdOrderByCreatedAtDesc(recipientId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(UUID recipientId) {
        return notificationRepository.countByRecipientIdAndIsRead(recipientId, false);
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(UUID notificationId, UUID recipientId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found: " + notificationId));

        // Security: only the recipient can mark their own notification as read
        if (!notification.getRecipient().getId().equals(recipientId)) {
            throw new RuntimeException("Access denied: notification does not belong to this user");
        }

        notification.setIsRead(true);
        return mapToResponse(notificationRepository.save(notification));
    }

    @Override
    @Transactional
    public void markAllAsRead(UUID recipientId) {
        notificationRepository.markAllReadForRecipient(recipientId);
    }

    // ── Mapping ──────────────────────────────────────────────────────────────

    private NotificationResponse mapToResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .type(n.getType())
                .message(n.getMessage())
                .isRead(n.getIsRead())
                .relatedEntityId(n.getRelatedEntityId())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
