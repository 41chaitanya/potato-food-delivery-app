package com.fooddelivery.notification.controller;

import com.fooddelivery.notification.model.NotificationLog;
import com.fooddelivery.notification.repository.NotificationRepository;
import com.fooddelivery.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * NotificationController
 * 
 * REST endpoints for notification management and monitoring.
 * 
 * NOTE: This service is primarily EVENT-DRIVEN via Kafka.
 * These REST endpoints are for:
 * 1. Health checks and debugging
 * 2. Admin operations (view logs, retry failed)
 * 3. Integration with monitoring dashboards
 * 
 * NOT FOR:
 * - Triggering notifications (use Kafka events)
 * - Business logic (handled by producer services)
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;

    /**
     * Health check endpoint
     * Used by load balancers and Kubernetes probes
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "NOTIFICATION-SERVICE");
        health.put("timestamp", Instant.now());
        return ResponseEntity.ok(health);
    }

    /**
     * Get notifications by reference ID
     * Useful for debugging and customer support
     * 
     * Example: GET /api/notifications/reference/ORD-12345
     */
    @GetMapping("/reference/{referenceId}")
    public ResponseEntity<List<NotificationLog>> getByReference(@PathVariable String referenceId) {
        log.debug("Fetching notifications for reference: {}", referenceId);
        List<NotificationLog> notifications = notificationService.getNotificationsByReference(referenceId);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Get notifications by user ID with pagination
     * Useful for user notification history
     * 
     * Example: GET /api/notifications/user/user-789?page=0&size=20
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<NotificationLog>> getByUser(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.debug("Fetching notifications for user: {}, page: {}", userId, page);
        Page<NotificationLog> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(
                userId, 
                PageRequest.of(page, size, Sort.by("createdAt").descending())
        );
        return ResponseEntity.ok(notifications);
    }

    /**
     * Get failed notifications
     * Useful for monitoring and alerting
     * 
     * Example: GET /api/notifications/failed
     */
    @GetMapping("/failed")
    public ResponseEntity<List<NotificationLog>> getFailedNotifications() {
        log.debug("Fetching failed notifications");
        List<NotificationLog> failed = notificationService.getFailedNotifications();
        return ResponseEntity.ok(failed);
    }

    /**
     * Retry a failed notification
     * Manual retry for failed notifications
     * 
     * Example: POST /api/notifications/{id}/retry
     */
    @PostMapping("/{id}/retry")
    public ResponseEntity<NotificationLog> retryNotification(@PathVariable String id) {
        log.info("Retrying notification: {}", id);
        NotificationLog result = notificationService.retryNotification(id);
        return ResponseEntity.ok(result);
    }

    /**
     * Get notification statistics
     * Useful for dashboards and monitoring
     * 
     * Example: GET /api/notifications/stats?hours=24
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats(
            @RequestParam(defaultValue = "24") int hours) {
        
        Instant since = Instant.now().minus(hours, ChronoUnit.HOURS);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("period", hours + " hours");
        stats.put("since", since);
        
        // Count by status
        Map<String, Long> statusCounts = new HashMap<>();
        for (NotificationLog.NotificationStatus status : NotificationLog.NotificationStatus.values()) {
            statusCounts.put(status.name(), notificationRepository.countByStatus(status));
        }
        stats.put("byStatus", statusCounts);
        
        // Get detailed stats
        List<Object[]> detailedStats = notificationRepository.getNotificationStats(since);
        stats.put("detailed", detailedStats);
        
        return ResponseEntity.ok(stats);
    }
}
