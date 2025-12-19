package com.fooddelivery.notification.service;

import com.fooddelivery.notification.dto.NotificationEvent;
import com.fooddelivery.notification.model.NotificationLog;

import java.util.List;

/**
 * NotificationService Interface
 * 
 * Defines the contract for notification processing.
 * 
 * WHY INTERFACE:
 * - Abstraction allows multiple implementations
 * - Easy to mock for testing
 * - Follows Dependency Inversion Principle
 * - Can swap implementations without changing consumers
 */
public interface NotificationService {

    /**
     * Process a notification event from Kafka
     * 
     * @param event The notification event to process
     * @return The created notification log
     */
    NotificationLog processNotification(NotificationEvent event);

    /**
     * Get notification history for a reference ID
     * 
     * @param referenceId The order/payment/delivery ID
     * @return List of notification logs
     */
    List<NotificationLog> getNotificationsByReference(String referenceId);

    /**
     * Get failed notifications for monitoring
     * 
     * @return List of failed notification logs
     */
    List<NotificationLog> getFailedNotifications();

    /**
     * Retry a failed notification
     * 
     * @param notificationId The notification log ID to retry
     * @return Updated notification log
     */
    NotificationLog retryNotification(String notificationId);
}
