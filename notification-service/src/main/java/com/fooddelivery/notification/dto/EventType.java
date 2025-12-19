package com.fooddelivery.notification.dto;

/**
 * EventType Enum
 * 
 * Defines all supported notification event types.
 * Each type maps to a specific business event in the food delivery flow.
 * 
 * WHY ENUM:
 * - Type safety - prevents typos in event type strings
 * - Easy to add new types without breaking existing code
 * - Can attach metadata (priority, default channel) to each type
 */
public enum EventType {
    
    // Order lifecycle events
    ORDER_CREATED("Order placed successfully", Priority.HIGH),
    ORDER_CONFIRMED("Order confirmed by restaurant", Priority.MEDIUM),
    ORDER_CANCELLED("Order has been cancelled", Priority.HIGH),
    
    // Payment events
    PAYMENT_SUCCESS("Payment completed successfully", Priority.HIGH),
    PAYMENT_FAILED("Payment failed", Priority.CRITICAL),
    PAYMENT_REFUNDED("Refund processed", Priority.HIGH),
    
    // Delivery events
    DELIVERY_ASSIGNED("Delivery partner assigned", Priority.MEDIUM),
    DELIVERY_PICKED("Order picked up", Priority.MEDIUM),
    DELIVERY_COMPLETED("Order delivered", Priority.HIGH),
    
    // Generic fallback
    UNKNOWN("Unknown event", Priority.LOW);

    private final String defaultMessage;
    private final Priority priority;

    EventType(String defaultMessage, Priority priority) {
        this.defaultMessage = defaultMessage;
        this.priority = priority;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

    public Priority getPriority() {
        return priority;
    }

    /**
     * Safely parse event type from string
     * Returns UNKNOWN if not found (fail-safe)
     */
    public static EventType fromString(String type) {
        if (type == null || type.isBlank()) {
            return UNKNOWN;
        }
        try {
            return EventType.valueOf(type.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }

    /**
     * Priority levels for notification routing
     * CRITICAL events might trigger multiple channels
     */
    public enum Priority {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
}
