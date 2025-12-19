# 12. Notification Service - Complete Guide

## 📋 Overview

| Property | Value |
|----------|-------|
| Port | 8090 |
| Service Name | NOTIFICATION-SERVICE |
| Database | PostgreSQL (notification_db) |
| Message Broker | Apache Kafka |
| Architecture | Event-Driven |

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         PRODUCER SERVICES                                │
│  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐                    │
│  │  Order  │  │ Payment │  │Delivery │  │Restaurant│                    │
│  │ Service │  │ Service │  │ Service │  │ Service │                    │
│  └────┬────┘  └────┬────┘  └────┬────┘  └────┬────┘                    │
│       │            │            │            │                          │
│       └────────────┴────────────┴────────────┘                          │
│                           │                                              │
│                           ▼                                              │
│                    ┌─────────────┐                                       │
│                    │    KAFKA    │                                       │
│                    │notification-│                                       │
│                    │   events    │                                       │
│                    └──────┬──────┘                                       │
└───────────────────────────┼──────────────────────────────────────────────┘
                            │
                            ▼
┌───────────────────────────────────────────────────────────────────────────┐
│                    NOTIFICATION SERVICE (Port 8090)                       │
│  ┌─────────────────────────────────────────────────────────────────────┐ │
│  │                    NotificationEventConsumer                         │ │
│  │                    (Kafka Listener)                                  │ │
│  └──────────────────────────────┬──────────────────────────────────────┘ │
│                                 │                                         │
│                                 ▼                                         │
│  ┌─────────────────────────────────────────────────────────────────────┐ │
│  │                    NotificationService                               │ │
│  │                    (Orchestration)                                   │ │
│  └──────────────────────────────┬──────────────────────────────────────┘ │
│                                 │                                         │
│            ┌────────────────────┼────────────────────┐                   │
│            ▼                    ▼                    ▼                   │
│  ┌──────────────────┐ ┌──────────────────┐ ┌──────────────────┐         │
│  │  LOG Channel     │ │  EMAIL Channel   │ │  SMS Channel     │         │
│  │  (Implemented)   │ │  (Future)        │ │  (Future)        │         │
│  └──────────────────┘ └──────────────────┘ └──────────────────┘         │
│                                 │                                         │
│                                 ▼                                         │
│  ┌─────────────────────────────────────────────────────────────────────┐ │
│  │                    PostgreSQL (notification_db)                      │ │
│  └─────────────────────────────────────────────────────────────────────┘ │
└───────────────────────────────────────────────────────────────────────────┘
```

---

## 📨 Supported Events

| Event Type | Description | Priority |
|------------|-------------|----------|
| ORDER_CREATED | New order placed | HIGH |
| PAYMENT_SUCCESS | Payment completed | HIGH |
| PAYMENT_FAILED | Payment failed | CRITICAL |
| DELIVERY_ASSIGNED | Rider assigned | MEDIUM |
| DELIVERY_PICKED | Order picked up | MEDIUM |
| DELIVERY_COMPLETED | Order delivered | HIGH |

---

## 🗄️ Database Schema

```sql
CREATE TABLE notification_logs (
    id UUID PRIMARY KEY,
    event_type VARCHAR(50) NOT NULL,
    reference_id VARCHAR(100) NOT NULL,
    user_id VARCHAR(100),
    message TEXT,
    channel VARCHAR(20),          -- LOG, EMAIL, SMS, PUSH
    status VARCHAR(20) NOT NULL,  -- PENDING, PROCESSING, SUCCESS, FAILED, SKIPPED
    retry_count INTEGER DEFAULT 0,
    error_message TEXT,
    trace_id VARCHAR(100),
    event_timestamp TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- Indexes
CREATE INDEX idx_notification_reference ON notification_logs(reference_id);
CREATE INDEX idx_notification_user ON notification_logs(user_id);
CREATE INDEX idx_notification_status ON notification_logs(status);
CREATE INDEX idx_notification_event_type ON notification_logs(event_type);
```

---

## 📦 Project Structure

```
notification-service/
├── src/main/java/com/fooddelivery/notification/
│   ├── NotificationServiceApplication.java
│   ├── config/
│   │   ├── KafkaConfig.java           # Kafka consumer configuration
│   │   └── TracingConfig.java         # Distributed tracing
│   ├── consumer/
│   │   └── NotificationEventConsumer.java  # Kafka listener
│   ├── controller/
│   │   └── NotificationController.java     # REST endpoints
│   ├── dto/
│   │   ├── NotificationEvent.java     # Kafka event DTO
│   │   └── EventType.java             # Event type enum
│   ├── exception/
│   │   └── GlobalExceptionHandler.java
│   ├── model/
│   │   └── NotificationLog.java       # JPA entity
│   ├── repository/
│   │   └── NotificationRepository.java
│   ├── service/
│   │   ├── NotificationService.java   # Interface
│   │   ├── impl/
│   │   │   └── NotificationServiceImpl.java
│   │   └── channel/
│   │       ├── NotificationChannel.java      # Strategy interface
│   │       └── LogNotificationChannel.java   # LOG implementation
│   └── util/
│       └── NotificationEventBuilder.java
└── src/main/resources/
    └── application.yml
```

---

## 🔧 Key Components

### 1. Kafka Consumer Configuration

```java
@Configuration
@EnableKafka
public class KafkaConfig {
    
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, NotificationEvent> 
            kafkaListenerContainerFactory() {
        // Manual acknowledgment
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        // 3 concurrent consumers
        factory.setConcurrency(3);
        // Error handler with retry
        factory.setCommonErrorHandler(errorHandler());
        return factory;
    }
    
    @Bean
    public DefaultErrorHandler errorHandler() {
        // Retry 3 times with 1 second backoff
        FixedBackOff backOff = new FixedBackOff(1000, 3);
        return new DefaultErrorHandler(backOff);
    }
}
```

### 2. Event Consumer

```java
@Component
public class NotificationEventConsumer {
    
    @KafkaListener(topics = "notification-events", groupId = "notification-group")
    public void consume(ConsumerRecord<String, NotificationEvent> record, 
                       Acknowledgment acknowledgment) {
        try {
            notificationService.processNotification(record.value());
            acknowledgment.acknowledge();  // Manual ACK after success
        } catch (Exception e) {
            // Don't acknowledge - Kafka will retry
            throw e;
        }
    }
}
```

### 3. Strategy Pattern for Channels

```java
// Interface
public interface NotificationChannel {
    NotificationLog.NotificationChannel getChannelType();
    boolean supports(NotificationEvent event);
    SendResult send(NotificationEvent event);
}

// LOG Implementation
@Component
public class LogNotificationChannel implements NotificationChannel {
    @Override
    public SendResult send(NotificationEvent event) {
        log.info("NOTIFICATION: {}", event.getMessage());
        return SendResult.success("Logged successfully");
    }
}

// Future: Email Implementation
@Component
public class EmailNotificationChannel implements NotificationChannel {
    @Override
    public SendResult send(NotificationEvent event) {
        // Send via SMTP/SendGrid/SES
    }
}
```

---

## 🔌 REST API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/notifications/health` | Health check |
| GET | `/api/notifications/reference/{id}` | Get by reference ID |
| GET | `/api/notifications/user/{userId}` | Get user notifications |
| GET | `/api/notifications/failed` | Get failed notifications |
| POST | `/api/notifications/{id}/retry` | Retry failed notification |
| GET | `/api/notifications/stats` | Get statistics |

---

## 📤 Kafka Event Format

```json
{
  "eventType": "ORDER_CREATED",
  "referenceId": "ORD-2024-001234",
  "userId": "user-789",
  "message": "Your order has been placed successfully!",
  "metadata": {
    "restaurantName": "Pizza Palace",
    "totalAmount": "599.00",
    "estimatedDelivery": "30-40 mins"
  },
  "timestamp": "2025-12-19T10:30:00Z",
  "traceId": "abc123xyz789",
  "email": "customer@example.com",
  "phone": "+919876543210"
}
```

---

## 🔄 Producer Integration

Other services publish events like this:

```java
@Service
public class OrderService {
    
    @Autowired
    private KafkaTemplate<String, NotificationEvent> kafkaTemplate;
    
    public Order createOrder(CreateOrderRequest request) {
        Order order = processOrder(request);
        
        // Publish notification event
        NotificationEvent event = NotificationEvent.builder()
            .eventType("ORDER_CREATED")
            .referenceId(order.getId())
            .userId(order.getUserId())
            .message("Your order has been placed!")
            .build();
        
        kafkaTemplate.send("notification-events", order.getId(), event);
        
        return order;
    }
}
```

---

## ✅ Key Features

1. **Event-Driven** - No REST calls to other services
2. **Idempotency** - Duplicate events are skipped
3. **Retry Mechanism** - Failed messages retried 3 times
4. **Manual ACK** - Messages acknowledged only after success
5. **Strategy Pattern** - Easy to add new channels
6. **Audit Trail** - All notifications logged to DB
7. **Distributed Tracing** - TraceId propagation

---

## 📊 Interview Questions

### Q1: Kafka kyu use kiya REST ke jagah?
**Answer:**
- **Loose Coupling:** Services independent hain
- **Async Processing:** Non-blocking notifications
- **Reliability:** Messages persist until consumed
- **Scalability:** Multiple consumers parallel process kar sakte hain

### Q2: Manual ACK kyu use kiya?
**Answer:**
- Auto-commit se message loss ho sakta hai
- Manual ACK ensures message processed successfully
- Failure pe message re-delivered hota hai

### Q3: Idempotency kaise handle ki?
**Answer:**
```java
// Check before processing
if (notificationRepository.existsByReferenceIdAndEventType(
        event.getReferenceId(), event.getEventType())) {
    return createSkippedLog(event, "Duplicate event");
}
```

### Q4: New channel (Email) add kaise karoge?
**Answer:**
1. Create `EmailNotificationChannel implements NotificationChannel`
2. Implement `send()` method
3. Add `@Component` annotation
4. Spring auto-injects into `List<NotificationChannel>`
5. No changes needed in existing code!

---

## 🧪 Testing Results

| Test Case | Status |
|-----------|--------|
| Service Health | ✅ PASSED |
| Eureka Registration | ✅ PASSED |
| Kafka Connection | ✅ PASSED |
| ORDER_CREATED Event | ✅ PASSED |
| PAYMENT_SUCCESS Event | ✅ PASSED |
| PAYMENT_FAILED Event | ✅ PASSED |
| DELIVERY_ASSIGNED Event | ✅ PASSED |
| DELIVERY_PICKED Event | ✅ PASSED |
| DELIVERY_COMPLETED Event | ✅ PASSED |
| Duplicate Handling | ✅ PASSED |
| Statistics API | ✅ PASSED |

**All 11 tests passed!**
