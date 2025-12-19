package com.fooddelivery.notification.service.impl;

import com.fooddelivery.notification.dto.EventType;
import com.fooddelivery.notification.dto.NotificationEvent;
import com.fooddelivery.notification.model.NotificationLog;
import com.fooddelivery.notification.model.NotificationLog.NotificationStatus;
import com.fooddelivery.notification.repository.NotificationRepository;
import com.fooddelivery.notification.service.NotificationService;
import com.fooddelivery.notification.service.channel.NotificationChannel;
import com.fooddelivery.notification.service.channel.NotificationChannel.SendResult;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * NotificationServiceImpl
 * 
 * Core service that orchestrates notification processing.
 * 
 * RESPONSIBILITIES:
 * 1. Receive events from Kafka consumer
 * 2. Validate and enrich event data
 * 3. Route to appropriate notification channels
 * 4. Persist notification logs
 * 5. Handle failures and retries
 * 
 * DESIGN DECISIONS:
 * 
 * 1. CHANNEL ABSTRACTION:
 *    - Uses List<NotificationChannel> for extensibility
 *    - New channels auto-register via Spring DI
 *    - Each channel decides if it handles an event
 * 
 * 2. IDEMPOTENCY:
 *    - Checks for duplicate events before processing
 *    - Prevents duplicate notifications on Kafka redelivery
 * 
 * 3. TRANSACTIONAL:
 *    - DB operations are transactional
 *    - Ensures consistent state on failures
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final List<NotificationChannel> notificationChannels;

    /**
     * Process a notification event
     * 
     * Flow:
     * 1. Validate event
     * 2. Check for duplicates (idempotency)
     * 3. Create pending log entry
     * 4. Send through all applicable channels
     * 5. Update log with result
     */
    @Override
    @Transactional
    @Observed(name = "notification.process", contextualName = "process-notification")
    public NotificationLog processNotification(NotificationEvent event) {
        log.debug("Processing notification event: type={}, referenceId={}", 
                event.getEventType(), event.getReferenceId());

        // Validate event
        validateEvent(event);

        // Idempotency check - prevent duplicate notifications
        if (isDuplicateEvent(event)) {
            log.warn("Duplicate event detected, skipping: type={}, referenceId={}", 
                    event.getEventType(), event.getReferenceId());
            return createSkippedLog(event, "Duplicate event");
        }

        // Create initial log entry
        NotificationLog notificationLog = createPendingLog(event);
        notificationLog = notificationRepository.save(notificationLog);

        // Process through all applicable channels
        boolean anySuccess = false;
        StringBuilder errorMessages = new StringBuilder();

        for (NotificationChannel channel : notificationChannels) {
            if (channel.supports(event)) {
                try {
                    SendResult result = channel.send(event);
                    
                    if (result.success()) {
                        anySuccess = true;
                        log.info("Notification sent via {}: referenceId={}", 
                                channel.getChannelType(), event.getReferenceId());
                    } else {
                        errorMessages.append(channel.getChannelType())
                                .append(": ")
                                .append(result.errorDetails())
                                .append("; ");
                    }
                } catch (Exception e) {
                    log.error("Channel {} failed for event {}: {}", 
                            channel.getChannelType(), event.getReferenceId(), e.getMessage());
                    errorMessages.append(channel.getChannelType())
                            .append(": ")
                            .append(e.getMessage())
                            .append("; ");
                }
            }
        }

        // Update log with final status
        if (anySuccess) {
            notificationLog.setStatus(NotificationStatus.SUCCESS);
        } else {
            notificationLog.setStatus(NotificationStatus.FAILED);
            notificationLog.setErrorMessage(errorMessages.toString());
        }

        return notificationRepository.save(notificationLog);
    }

    /**
     * Validate incoming event
     * Throws IllegalArgumentException for invalid events
     */
    private void validateEvent(NotificationEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Notification event cannot be null");
        }
        if (event.getEventType() == null || event.getEventType().isBlank()) {
            throw new IllegalArgumentException("Event type is required");
        }
        if (event.getReferenceId() == null || event.getReferenceId().isBlank()) {
            throw new IllegalArgumentException("Reference ID is required");
        }
    }

    /**
     * Check if this event was already processed
     * Prevents duplicate notifications on Kafka redelivery
     */
    private boolean isDuplicateEvent(NotificationEvent event) {
        return notificationRepository.existsByReferenceIdAndEventType(
                event.getReferenceId(), 
                event.getEventType()
        );
    }

    /**
     * Create a pending notification log entry
     */
    private NotificationLog createPendingLog(NotificationEvent event) {
        EventType eventType = EventType.fromString(event.getEventType());
        String message = event.getMessage() != null ? 
                event.getMessage() : eventType.getDefaultMessage();

        return NotificationLog.builder()
                .eventType(event.getEventType())
                .referenceId(event.getReferenceId())
                .userId(event.getUserId())
                .message(message)
                .channel(NotificationLog.NotificationChannel.LOG)
                .status(NotificationStatus.PROCESSING)
                .retryCount(0)
                .traceId(event.getTraceId())
                .eventTimestamp(event.getTimestamp() != null ? event.getTimestamp() : Instant.now())
                .build();
    }

    /**
     * Create a skipped notification log (for duplicates)
     */
    private NotificationLog createSkippedLog(NotificationEvent event, String reason) {
        NotificationLog log = createPendingLog(event);
        log.setStatus(NotificationStatus.SKIPPED);
        log.setErrorMessage(reason);
        return notificationRepository.save(log);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationLog> getNotificationsByReference(String referenceId) {
        return notificationRepository.findByReferenceIdOrderByCreatedAtDesc(referenceId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationLog> getFailedNotifications() {
        return notificationRepository.findByStatus(NotificationStatus.FAILED);
    }

    @Override
    @Transactional
    public NotificationLog retryNotification(String notificationId) {
        UUID id = UUID.fromString(notificationId);
        NotificationLog notification = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));

        if (notification.getStatus() != NotificationStatus.FAILED) {
            throw new IllegalStateException("Can only retry failed notifications");
        }

        // Increment retry count
        notification.setRetryCount(notification.getRetryCount() + 1);
        notification.setStatus(NotificationStatus.PROCESSING);
        notification = notificationRepository.save(notification);

        // Rebuild event and reprocess
        NotificationEvent event = NotificationEvent.builder()
                .eventType(notification.getEventType())
                .referenceId(notification.getReferenceId())
                .userId(notification.getUserId())
                .message(notification.getMessage())
                .traceId(notification.getTraceId())
                .timestamp(notification.getEventTimestamp())
                .build();

        // Process through channels
        boolean success = false;
        for (NotificationChannel channel : notificationChannels) {
            if (channel.supports(event)) {
                SendResult result = channel.send(event);
                if (result.success()) {
                    success = true;
                    break;
                }
            }
        }

        notification.setStatus(success ? NotificationStatus.SUCCESS : NotificationStatus.FAILED);
        return notificationRepository.save(notification);
    }
}
