package com.fooddelivery.notification.service.channel;

import com.fooddelivery.notification.dto.NotificationEvent;
import com.fooddelivery.notification.model.NotificationLog;

/**
 * NotificationChannel Interface
 * 
 * Strategy pattern for different notification delivery channels.
 * 
 * WHY STRATEGY PATTERN:
 * - Each channel (LOG, EMAIL, SMS, PUSH) has different delivery logic
 * - Easy to add new channels without modifying existing code
 * - Channels can be enabled/disabled independently
 * - Each channel can have its own retry logic
 * 
 * FUTURE CHANNELS:
 * - EmailNotificationChannel: Send via SMTP/SendGrid/SES
 * - SmsNotificationChannel: Send via Twilio/SNS
 * - PushNotificationChannel: Send via FCM/APNs
 */
public interface NotificationChannel {

    /**
     * Get the channel type
     * 
     * @return The channel type enum
     */
    NotificationLog.NotificationChannel getChannelType();

    /**
     * Check if this channel can handle the event
     * Some events might only go to specific channels
     * 
     * @param event The notification event
     * @return true if this channel should process the event
     */
    boolean supports(NotificationEvent event);

    /**
     * Send the notification through this channel
     * 
     * @param event The notification event
     * @return Result of the send operation
     */
    SendResult send(NotificationEvent event);

    /**
     * Result of a send operation
     */
    record SendResult(boolean success, String message, String errorDetails) {
        public static SendResult success(String message) {
            return new SendResult(true, message, null);
        }

        public static SendResult failure(String message, String errorDetails) {
            return new SendResult(false, message, errorDetails);
        }
    }
}
