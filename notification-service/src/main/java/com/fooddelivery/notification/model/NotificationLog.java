package com.fooddelivery.notification.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * NotificationLog Entity
 * 
 * Persists every notification attempt for:
 * 1. Audit trail - who received what notification and when
 * 2. Debugging - track failures and retry attempts
 * 3. Analytics - notification delivery metrics
 * 4. Compliance - proof of notification delivery
 * 
 * WHY UUID:
 * - Globally unique across distributed systems
 * - No coordination needed between services
 * - Safe for Kafka message deduplication
 */
@Entity
@Table(name = "notification_logs", indexes = {
    @Index(name = "idx_notification_reference", columnList = "referenceId"),
    @Index(name = "idx_notification_user", columnList = "userId"),
    @Index(name = "idx_notification_status", columnList = "status"),
    @Index(name = "idx_notification_event_type", columnList = "eventType"),
    @Index(name = "idx_notification_created", columnList = "createdAt")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Type of event that triggered this notification
     * Maps to EventType enum values
     */
    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    /**
     * Reference to source entity (orderId, paymentId, deliveryId)
     * Used for correlation and debugging
     */
    @Column(name = "reference_id", nullable = false, length = 100)
    private String referenceId;

    /**
     * Target user ID
     */
    @Column(name = "user_id", length = 100)
    private String userId;

    /**
     * Notification message content
     */
    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    /**
     * Notification channel used (LOG, EMAIL, SMS, PUSH)
     */
    @Column(name = "channel", length = 20)
    @Enumerated(EnumType.STRING)
    private NotificationChannel channel;

    /**
     * Delivery status
     */
    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private NotificationStatus status;

    /**
     * Number of retry attempts
     */
    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;

    /**
     * Error message if failed
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * Distributed trace ID for correlation
     */
    @Column(name = "trace_id", length = 100)
    private String traceId;

    /**
     * Original event timestamp from producer
     */
    @Column(name = "event_timestamp")
    private Instant eventTimestamp;

    /**
     * When this record was created
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * When this record was last updated
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    /**
     * Notification delivery status
     */
    public enum NotificationStatus {
        PENDING,    // Queued for processing
        PROCESSING, // Currently being processed
        SUCCESS,    // Successfully delivered
        FAILED,     // Failed after all retries
        SKIPPED     // Skipped (e.g., user opted out)
    }

    /**
     * Notification channels
     * Extensible for future channels
     */
    public enum NotificationChannel {
        LOG,    // Console + DB logging (current implementation)
        EMAIL,  // Email notification (future)
        SMS,    // SMS notification (future)
        PUSH    // Push notification (future)
    }
}
