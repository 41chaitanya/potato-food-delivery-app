package com.fooddelivery.notification.consumer;

import com.fooddelivery.notification.dto.NotificationEvent;
import com.fooddelivery.notification.model.NotificationLog;
import com.fooddelivery.notification.service.NotificationService;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * NotificationEventConsumer
 * 
 * Kafka consumer that listens for notification events.
 * This is the entry point for all notification processing.
 * 
 * KEY DESIGN DECISIONS:
 * 
 * 1. MANUAL ACKNOWLEDGMENT:
 *    - We only acknowledge after successful processing
 *    - If processing fails, message stays in Kafka for retry
 *    - Prevents message loss
 * 
 * 2. TRACE ID PROPAGATION:
 *    - Extract traceId from event and add to MDC
 *    - All logs within this request will include traceId
 *    - Enables end-to-end tracing across services
 * 
 * 3. ERROR HANDLING:
 *    - Exceptions are logged but not swallowed
 *    - Kafka's error handler (configured in KafkaConfig) handles retries
 *    - After max retries, message is logged and skipped
 * 
 * 4. SINGLE TOPIC:
 *    - All notification events come through one topic
 *    - Event type determines processing logic
 *    - Simplifies infrastructure management
 * 
 * TOPIC STRUCTURE:
 * - Topic: notification-events
 * - Key: referenceId (for partition ordering)
 * - Value: NotificationEvent JSON
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventConsumer {

    private final NotificationService notificationService;

    /**
     * Main Kafka listener for notification events
     * 
     * @param record The Kafka consumer record containing the event
     * @param acknowledgment Manual acknowledgment handle
     */
    @KafkaListener(
            topics = "${notification.kafka.topic:notification-events}",
            groupId = "${spring.kafka.consumer.group-id:notification-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Observed(name = "kafka.consume", contextualName = "consume-notification-event")
    public void consume(ConsumerRecord<String, NotificationEvent> record, Acknowledgment acknowledgment) {
        NotificationEvent event = record.value();
        
        // Set up MDC for distributed tracing
        setupMDC(event, record);
        
        log.info("Received notification event: type={}, referenceId={}, partition={}, offset={}",
                event.getEventType(),
                event.getReferenceId(),
                record.partition(),
                record.offset());

        try {
            // Process the notification
            NotificationLog result = notificationService.processNotification(event);
            
            log.info("Notification processed: id={}, status={}, referenceId={}",
                    result.getId(),
                    result.getStatus(),
                    result.getReferenceId());

            // Acknowledge only after successful processing
            acknowledgment.acknowledge();
            
            log.debug("Message acknowledged: partition={}, offset={}", 
                    record.partition(), record.offset());

        } catch (IllegalArgumentException e) {
            // Invalid event - acknowledge to skip (won't succeed on retry)
            log.error("Invalid notification event, skipping: {}", e.getMessage());
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            // Processing failed - don't acknowledge, let Kafka retry
            log.error("Failed to process notification event: type={}, referenceId={}, error={}",
                    event.getEventType(),
                    event.getReferenceId(),
                    e.getMessage(),
                    e);
            // Don't acknowledge - message will be redelivered
            throw e;
            
        } finally {
            // Clean up MDC
            clearMDC();
        }
    }

    /**
     * Set up MDC (Mapped Diagnostic Context) for logging
     * 
     * MDC values are included in all log statements within this thread.
     * This enables correlation of logs across the request lifecycle.
     */
    private void setupMDC(NotificationEvent event, ConsumerRecord<String, NotificationEvent> record) {
        if (event.getTraceId() != null) {
            MDC.put("traceId", event.getTraceId());
        }
        MDC.put("eventType", event.getEventType());
        MDC.put("referenceId", event.getReferenceId());
        MDC.put("kafkaPartition", String.valueOf(record.partition()));
        MDC.put("kafkaOffset", String.valueOf(record.offset()));
    }

    /**
     * Clear MDC after processing
     */
    private void clearMDC() {
        MDC.remove("traceId");
        MDC.remove("eventType");
        MDC.remove("referenceId");
        MDC.remove("kafkaPartition");
        MDC.remove("kafkaOffset");
    }
}
