package com.fooddelivery.notification.service.channel;

import com.fooddelivery.notification.dto.EventType;
import com.fooddelivery.notification.dto.NotificationEvent;
import com.fooddelivery.notification.model.NotificationLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * LogNotificationChannel
 * 
 * Implements notification delivery via logging.
 * This is the primary channel for the current implementation.
 * 
 * WHY LOG CHANNEL:
 * - Simple to implement and test
 * - Provides audit trail in logs
 * - Combined with DB persistence for complete record
 * - Foundation for adding other channels later
 * 
 * IN PRODUCTION:
 * - Logs are shipped to ELK/CloudWatch/Splunk
 * - Searchable by traceId, referenceId, userId
 * - Alerts can be set up on ERROR logs
 */
@Component
@Slf4j
public class LogNotificationChannel implements NotificationChannel {

    @Override
    public NotificationLog.NotificationChannel getChannelType() {
        return NotificationLog.NotificationChannel.LOG;
    }

    /**
     * LOG channel supports all events
     * Other channels might be selective (e.g., SMS only for critical events)
     */
    @Override
    public boolean supports(NotificationEvent event) {
        return true; // Log channel handles all events
    }

    /**
     * Send notification by logging it
     * 
     * Uses structured logging for easy parsing and searching.
     * Different log levels based on event type priority.
     */
    @Override
    public SendResult send(NotificationEvent event) {
        try {
            EventType eventType = EventType.fromString(event.getEventType());
            
            // Build structured log message
            String logMessage = buildLogMessage(event, eventType);
            
            // Log at appropriate level based on priority
            switch (eventType.getPriority()) {
                case CRITICAL -> log.error("🚨 CRITICAL NOTIFICATION: {}", logMessage);
                case HIGH -> log.warn("📢 HIGH PRIORITY NOTIFICATION: {}", logMessage);
                case MEDIUM -> log.info("📬 NOTIFICATION: {}", logMessage);
                case LOW -> log.debug("📝 LOW PRIORITY NOTIFICATION: {}", logMessage);
            }
            
            // Also log in a structured format for log aggregation
            log.info("NOTIFICATION_SENT | eventType={} | referenceId={} | userId={} | channel=LOG | status=SUCCESS",
                    event.getEventType(),
                    event.getReferenceId(),
                    event.getUserId());
            
            return SendResult.success("Notification logged successfully");
            
        } catch (Exception e) {
            log.error("Failed to log notification: {}", e.getMessage(), e);
            return SendResult.failure("Failed to log notification", e.getMessage());
        }
    }

    /**
     * Build a human-readable log message
     */
    private String buildLogMessage(NotificationEvent event, EventType eventType) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("╔══════════════════════════════════════════════════════════════╗\n");
        sb.append(String.format("║ EVENT TYPE: %-48s ║%n", event.getEventType()));
        sb.append("╠══════════════════════════════════════════════════════════════╣\n");
        sb.append(String.format("║ Reference ID: %-46s ║%n", event.getReferenceId()));
        sb.append(String.format("║ User ID: %-51s ║%n", event.getUserId()));
        sb.append(String.format("║ Priority: %-50s ║%n", eventType.getPriority()));
        sb.append("╠══════════════════════════════════════════════════════════════╣\n");
        sb.append(String.format("║ Message: %-51s ║%n", truncate(event.getMessage(), 50)));
        
        if (event.getMetadata() != null && !event.getMetadata().isEmpty()) {
            sb.append("╠══════════════════════════════════════════════════════════════╣\n");
            sb.append("║ Metadata:                                                    ║\n");
            event.getMetadata().forEach((key, value) -> 
                sb.append(String.format("║   • %-15s: %-40s ║%n", key, truncate(String.valueOf(value), 40)))
            );
        }
        
        sb.append("╠══════════════════════════════════════════════════════════════╣\n");
        sb.append(String.format("║ Trace ID: %-50s ║%n", event.getTraceId()));
        sb.append(String.format("║ Timestamp: %-49s ║%n", event.getTimestamp()));
        sb.append("╚══════════════════════════════════════════════════════════════╝");
        
        return sb.toString();
    }

    private String truncate(String str, int maxLength) {
        if (str == null) return "N/A";
        return str.length() > maxLength ? str.substring(0, maxLength - 3) + "..." : str;
    }
}
