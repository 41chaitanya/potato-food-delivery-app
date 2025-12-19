package com.fooddelivery.notification.repository;

import com.fooddelivery.notification.model.NotificationLog;
import com.fooddelivery.notification.model.NotificationLog.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * NotificationRepository
 * 
 * Data access layer for notification logs.
 * Uses Spring Data JPA for automatic query generation.
 * 
 * WHY THESE QUERIES:
 * - findByReferenceId: Debug all notifications for a specific order/payment
 * - findByUserId: Get notification history for a user
 * - findByStatus: Monitor failed notifications for retry/alerting
 * - countByEventType: Analytics on notification volume by type
 */
@Repository
public interface NotificationRepository extends JpaRepository<NotificationLog, UUID> {

    /**
     * Find all notifications for a specific reference (order, payment, delivery)
     * Useful for debugging and customer support
     */
    List<NotificationLog> findByReferenceIdOrderByCreatedAtDesc(String referenceId);

    /**
     * Find all notifications for a user
     * Useful for notification history in user profile
     */
    Page<NotificationLog> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    /**
     * Find notifications by status
     * Useful for monitoring and retry mechanisms
     */
    List<NotificationLog> findByStatus(NotificationStatus status);

    /**
     * Find failed notifications for retry
     * Only get failures that haven't exceeded max retries
     */
    @Query("SELECT n FROM NotificationLog n WHERE n.status = 'FAILED' AND n.retryCount < :maxRetries")
    List<NotificationLog> findFailedForRetry(@Param("maxRetries") int maxRetries);

    /**
     * Find notifications by event type within time range
     * Useful for analytics dashboards
     */
    @Query("SELECT n FROM NotificationLog n WHERE n.eventType = :eventType " +
           "AND n.createdAt BETWEEN :startTime AND :endTime")
    List<NotificationLog> findByEventTypeAndTimeRange(
            @Param("eventType") String eventType,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime);

    /**
     * Count notifications by status
     * Useful for health monitoring
     */
    long countByStatus(NotificationStatus status);

    /**
     * Count notifications by event type
     * Useful for analytics
     */
    long countByEventType(String eventType);

    /**
     * Check if notification already exists (idempotency)
     * Prevents duplicate notifications for same event
     */
    boolean existsByReferenceIdAndEventType(String referenceId, String eventType);

    /**
     * Get notification statistics by event type
     */
    @Query("SELECT n.eventType, n.status, COUNT(n) FROM NotificationLog n " +
           "WHERE n.createdAt >= :since GROUP BY n.eventType, n.status")
    List<Object[]> getNotificationStats(@Param("since") Instant since);
}
