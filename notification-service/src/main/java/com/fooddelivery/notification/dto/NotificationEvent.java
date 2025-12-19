package com.fooddelivery.notification.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

/**
 * NotificationEvent DTO
 * 
 * This is the contract for events consumed from Kafka.
 * All producer services (Order, Payment, Delivery) must publish events in this format.
 * 
 * WHY THIS STRUCTURE:
 * - eventType: Determines which notification template/handler to use
 * - referenceId: Links back to the source entity (orderId, deliveryId, etc.)
 * - userId: Target user for the notification
 * - metadata: Flexible key-value pairs for channel-specific data
 * - timestamp: When the event occurred (for ordering and debugging)
 * - traceId: Propagated from source service for distributed tracing
 * 
 * SAMPLE KAFKA MESSAGE:
 * {
 *   "eventType": "ORDER_CREATED",
 *   "referenceId": "ORD-12345",
 *   "userId": "user-789",
 *   "message": "Your order has been placed successfully!",
 *   "metadata": {
 *     "restaurantName": "Pizza Palace",
 *     "totalAmount": "599.00",
 *     "estimatedDelivery": "30 mins"
 *   },
 *   "timestamp": "2024-12-19T10:30:00Z",
 *   "traceId": "abc123xyz"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)  // Ignore unknown fields for forward compatibility
public class NotificationEvent {

    /**
     * Type of event - determines notification handling logic
     * Values: ORDER_CREATED, PAYMENT_SUCCESS, PAYMENT_FAILED, 
     *         DELIVERY_ASSIGNED, DELIVERY_PICKED, DELIVERY_COMPLETED
     */
    @JsonProperty("eventType")
    private String eventType;

    /**
     * Reference ID linking to source entity
     * Could be orderId, paymentId, deliveryId based on eventType
     */
    @JsonProperty("referenceId")
    private String referenceId;

    /**
     * Target user ID for the notification
     * Used to fetch user contact details if needed
     */
    @JsonProperty("userId")
    private String userId;

    /**
     * Pre-formatted notification message
     * Producer service creates the message, we just deliver it
     */
    @JsonProperty("message")
    private String message;

    /**
     * Additional metadata for rich notifications
     * Examples: restaurantName, riderName, amount, ETA
     */
    @JsonProperty("metadata")
    private Map<String, Object> metadata;

    /**
     * Event timestamp from producer
     * Used for ordering and latency tracking
     */
    @JsonProperty("timestamp")
    private Instant timestamp;

    /**
     * Distributed tracing ID
     * Propagated from source service for end-to-end tracing
     */
    @JsonProperty("traceId")
    private String traceId;

    /**
     * Optional: Email address for email notifications
     */
    @JsonProperty("email")
    private String email;

    /**
     * Optional: Phone number for SMS notifications
     */
    @JsonProperty("phone")
    private String phone;

    /**
     * Optional: Device token for push notifications
     */
    @JsonProperty("deviceToken")
    private String deviceToken;
}
