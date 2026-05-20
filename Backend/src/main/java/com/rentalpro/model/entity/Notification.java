package com.rentalpro.model.entity;

import com.rentalpro.model.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notification_recipient", columnList = "recipient_id"),
        @Index(name = "idx_notification_is_read",   columnList = "is_read")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** The user who should receive this notification. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    /** Human-readable message shown in the bell dropdown. */
    @Column(nullable = false, length = 500)
    private String message;

    /** Whether the recipient has read/dismissed this notification. */
    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    /**
     * Optional reference to the entity that triggered this notification
     * (e.g. the contract UUID, appeal UUID). Stored as string to stay
     * type-agnostic — callers cast as needed.
     */
    @Column(name = "related_entity_id")
    private UUID relatedEntityId;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
