package com.fooddelivery.notification.util;

import com.fooddelivery.notification.dto.NotificationEvent;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * NotificationEventBuilder
 * 
 * Utility class for building notification events.
 * 
 * THIS CLASS IS FOR REFERENCE ONLY.
 * Copy this to producer services (Order, Payment, Delivery) to build events.
 * 
 * USAGE IN ORDER SERVICE:
 * ```java
 * NotificationEvent event = NotificationEventBuilder.orderCreated()
 *     .referenceId(order.getId())
 *     .userId(order.getUserId())
 *     .message("Your order has been placed!")
 *     .metadata("restaurantName", restaurant.getName())
 *     .metadata("totalAmount", order.getTotalAmount())
 *     .build();
 * 
 * kafkaTemplate.send("notification-events", order.getId(), event);
 * ```
 */
public class NotificationEventBuilder {

    private String eventType;
    private String referenceId;
    private String userId;
    private String message;
    private Map<String, Object> metadata = new HashMap<>();
    private String email;
    private String phone;
    private String deviceToken;
    private String traceId;

    private NotificationEventBuilder(String eventType) {
        this.eventType = eventType;
    }

    // Factory methods for each event type
    public static NotificationEventBuilder orderCreated() {
        return new NotificationEventBuilder("ORDER_CREATED");
    }

    public static NotificationEventBuilder paymentSuccess() {
        return new NotificationEventBuilder("PAYMENT_SUCCESS");
    }

    public static NotificationEventBuilder paymentFailed() {
        return new NotificationEventBuilder("PAYMENT_FAILED");
    }

    public static NotificationEventBuilder deliveryAssigned() {
        return new NotificationEventBuilder("DELIVERY_ASSIGNED");
    }

    public static NotificationEventBuilder deliveryPicked() {
        return new NotificationEventBuilder("DELIVERY_PICKED");
    }

    public static NotificationEventBuilder deliveryCompleted() {
        return new NotificationEventBuilder("DELIVERY_COMPLETED");
    }

    // Builder methods
    public NotificationEventBuilder referenceId(String referenceId) {
        this.referenceId = referenceId;
        return this;
    }

    public NotificationEventBuilder userId(String userId) {
        this.userId = userId;
        return this;
    }

    public NotificationEventBuilder message(String message) {
        this.message = message;
        return this;
    }

    public NotificationEventBuilder metadata(String key, Object value) {
        this.metadata.put(key, value);
        return this;
    }

    public NotificationEventBuilder metadata(Map<String, Object> metadata) {
        this.metadata.putAll(metadata);
        return this;
    }

    public NotificationEventBuilder email(String email) {
        this.email = email;
        return this;
    }

    public NotificationEventBuilder phone(String phone) {
        this.phone = phone;
        return this;
    }

    public NotificationEventBuilder deviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
        return this;
    }

    public NotificationEventBuilder traceId(String traceId) {
        this.traceId = traceId;
        return this;
    }

    public NotificationEvent build() {
        return NotificationEvent.builder()
                .eventType(eventType)
                .referenceId(referenceId)
                .userId(userId)
                .message(message)
                .metadata(metadata.isEmpty() ? null : metadata)
                .email(email)
                .phone(phone)
                .deviceToken(deviceToken)
                .traceId(traceId != null ? traceId : UUID.randomUUID().toString())
                .timestamp(Instant.now())
                .build();
    }
}
