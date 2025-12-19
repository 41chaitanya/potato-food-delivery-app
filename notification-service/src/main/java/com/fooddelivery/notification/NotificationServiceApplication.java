package com.fooddelivery.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Notification Service Application
 * 
 * This service is the central notification hub for the Food Delivery platform.
 * It follows EVENT-DRIVEN architecture and is completely decoupled from other services.
 * 
 * KEY DESIGN DECISIONS:
 * 1. NO REST calls to other services - only consumes Kafka events
 * 2. All notification attempts are logged to DB for audit trail
 * 3. Supports multiple notification channels (LOG now, EMAIL/SMS/PUSH later)
 * 4. Uses Strategy pattern for extensibility
 * 
 * EVENTS CONSUMED:
 * - ORDER_CREATED: When a new order is placed
 * - PAYMENT_SUCCESS: When payment is completed
 * - PAYMENT_FAILED: When payment fails
 * - DELIVERY_ASSIGNED: When rider is assigned
 * - DELIVERY_PICKED: When order is picked up
 * - DELIVERY_COMPLETED: When order is delivered
 */
@SpringBootApplication
@EnableDiscoveryClient  // Register with Eureka
@EnableKafka           // Enable Kafka consumer
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
