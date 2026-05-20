package com.rentalpro.repository;

import com.rentalpro.model.entity.Notification;
import com.rentalpro.model.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    /** All notifications for a recipient, newest first. */
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(UUID recipientId);

    /** Unread count for the bell badge. */
    long countByRecipientIdAndIsRead(UUID recipientId, Boolean isRead);

    /** Mark all unread notifications for a user as read in one query. */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipient.id = :recipientId AND n.isRead = false")
    void markAllReadForRecipient(@Param("recipientId") UUID recipientId);

    /**
     * Find all SUBCITY_STAFF users whose subCityZone matches the given sub-city,
     * used to fan-out appeal notifications to the correct officers.
     */
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.subCityZone = :subCity")
    List<com.rentalpro.model.entity.User> findUsersByRoleAndSubCity(
            @Param("role") UserRole role,
            @Param("subCity") String subCity);
}
