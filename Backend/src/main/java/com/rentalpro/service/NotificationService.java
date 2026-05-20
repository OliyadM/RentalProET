package com.rentalpro.service;

import com.rentalpro.model.dto.response.NotificationResponse;
import com.rentalpro.model.enums.NotificationType;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

    /**
     * Create and persist a notification for a single recipient.
     *
     * @param recipientId     UUID of the user who receives the notification
     * @param type            the notification category
     * @param message         human-readable text shown in the bell dropdown
     * @param relatedEntityId optional UUID of the triggering entity (contract, appeal, etc.)
     */
    void send(UUID recipientId, NotificationType type, String message, UUID relatedEntityId);

    /**
     * Fan-out: send the same notification to every SUBCITY_STAFF user
     * whose subCityZone matches the given sub-city string.
     */
    void sendToSubCityOfficers(String subCity, NotificationType type, String message, UUID relatedEntityId);

    /** Return all notifications for the authenticated user, newest first. */
    List<NotificationResponse> getMyNotifications(UUID recipientId);

    /** Return only unread count — used by the bell badge. */
    long getUnreadCount(UUID recipientId);

    /** Mark a single notification as read. */
    NotificationResponse markAsRead(UUID notificationId, UUID recipientId);

    /** Mark every unread notification for a user as read. */
    void markAllAsRead(UUID recipientId);
}
