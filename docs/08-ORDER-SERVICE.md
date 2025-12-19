# Order Service (Port: 8081)

## Ye Service Kya Karti Hai?

Order Service **order processing** handle karti hai. Order create, payment integration, status management, aur order history - sab yahan hota hai. Resilience4j Circuit Breaker se payment failures handle hote hain.

---

## Features

| Feature | Description |
|---------|-------------|
| Order Creation | Cart se order create |
| Payment Integration | Payment Service call |
| Circuit Breaker | Payment failure handling |
| Status Management | Order lifecycle |
| Order History | User ke past orders |

---

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                   Order Service (8081)                   │
│                                                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │
│  │  Controller  │  │   Service    │  │  Repository  │   │
│  │              │  │              │  │              │   │
│  │/api/orders/* │→ │ OrderService │→ │  OrderRepo   │   │
│  └──────────────┘  └──────────────┘  └──────────────┘   │
│                           │                  │           │
│                           ▼                  ▼           │
│                    ┌──────────────┐   ┌──────────────┐   │
│                    │  Resilience4j│   │  PostgreSQL  │   │
│                    │Circuit Breaker│  │   order_db   │   │
│                    └──────────────┘   └──────────────┘   │
│                           │                              │
│                           ▼                              │
│                    ┌──────────────┐                      │
│                    │   Payment    │                      │
│                    │   Client     │                      │
│                    └──────────────┘                      │
└─────────────────────────────────────────────────────────┘
```

---

## Tech Stack

| Technology | Purpose |
|------------|---------|
| Spring Data JPA | Database operations |
| Resilience4j | Circuit Breaker, Retry |
| WebClient | Payment Service call |
| PostgreSQL | Order data storage |

---

## Database Schema

```sql
CREATE TABLE orders_tb (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    customer_name VARCHAR(100),
    restaurant_name VARCHAR(100),
    total_amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20),  -- PENDING, CONFIRMED, PAID, CANCELLED, DELIVERED
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE order_items (
    id UUID PRIMARY KEY,
    order_id UUID REFERENCES orders_tb(id),
    menu_item_id UUID,
    item_name VARCHAR(100),
    quantity INT,
    price DECIMAL(10,2),
    created_at TIMESTAMP
);
```

### Order Status Flow
```
PENDING → CONFIRMED → PAID → PREPARING → OUT_FOR_DELIVERY → DELIVERED
    │         │
    └─────────┴──→ CANCELLED
```

---

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/orders | Create order |
| GET | /api/orders/{orderId} | Get order by ID |
| GET | /api/orders/user/{userId} | Get user's orders |
| PATCH | /api/orders/{orderId}/cancel | Cancel order |
| PATCH | /api/orders/{orderId}/status | Update status |

### Internal API (for Delivery Service)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/orders/internal/{orderId} | Get order for delivery |

---

## Key Flows

### 1. Create Order Flow
```
Client: POST /api/orders
{
  "userId": "user-uuid",
  "customerName": "Rahul Sharma",
  "restaurantName": "Spice Garden",
  "totalAmount": 794.00,
  "items": [
    { "menuItemId": "...", "itemName": "Butter Chicken", "quantity": 2, "price": 598 },
    { "menuItemId": "...", "itemName": "Naan", "quantity": 4, "price": 196 }
  ]
}
        │
        ▼
1. Create order with status = PENDING
        │
        ▼
2. Call Payment Service (Circuit Breaker protected)
   POST /api/payments
   {
     "orderId": "order-uuid",
     "amount": 794.00,
     "paymentMethod": "CARD"
   }
        │
        ├── Payment SUCCESS
        │   └── Order status = CONFIRMED
        │
        └── Payment FAILED
            └── Order status = PENDING (retry later)
        │
        ▼
Response:
{
  "id": "order-uuid",
  "status": "CONFIRMED",
  "totalAmount": 794.00
}
```

### 2. Circuit Breaker Flow
```
Normal Flow:
Order Service → Payment Service → Success

When Payment Service Down:
Request 1: Timeout → Failure recorded
Request 2: Timeout → Failure recorded
Request 3: Timeout → Failure recorded
Request 4: Timeout → Failure recorded
Request 5: Timeout → CIRCUIT OPEN!

Circuit OPEN (10 seconds):
Request 6: Immediately fail (no call to Payment)
Request 7: Immediately fail
...

After 10 seconds → Circuit HALF-OPEN:
Request N: Try Payment Service
├── Success → Circuit CLOSED (normal)
└── Failure → Circuit OPEN again
```

---

## Resilience4j Configuration

```yaml
resilience4j:
  circuitbreaker:
    instances:
      paymentCB:
        slidingWindowSize: 10        # Last 10 calls track karo
        failureRateThreshold: 50     # 50% fail → open circuit
        waitDurationInOpenState: 10s # 10 sec wait before retry
        minimumNumberOfCalls: 5      # Minimum 5 calls before decision
        permittedNumberOfCallsInHalfOpenState: 3

  retry:
    instances:
      paymentRetry:
        maxAttempts: 3               # 3 baar try karo
        waitDuration: 1s             # 1 sec wait between retries

  timelimiter:
    instances:
      paymentTimeLimiter:
        timeoutDuration: 2s          # 2 sec timeout
```

### Code Implementation
```java
@Service
public class OrderService {

    @CircuitBreaker(name = "paymentCB", fallbackMethod = "paymentFallback")
    @Retry(name = "paymentRetry")
    @TimeLimiter(name = "paymentTimeLimiter")
    public CompletableFuture<PaymentResponse> processPayment(PaymentRequest request) {
        return CompletableFuture.supplyAsync(() -> 
            paymentClient.processPayment(request)
        );
    }

    // Fallback when circuit is open or payment fails
    public CompletableFuture<PaymentResponse> paymentFallback(
            PaymentRequest request, Throwable t) {
        log.error("Payment failed, using fallback: {}", t.getMessage());
        return CompletableFuture.completedFuture(
            new PaymentResponse("FAILED", "Payment service unavailable")
        );
    }
}
```

---

## Order Status Management

```java
public enum OrderStatus {
    PENDING,          // Order created, payment pending
    CONFIRMED,        // Payment successful
    PAID,             // Alternative to CONFIRMED
    PREPARING,        // Restaurant preparing
    OUT_FOR_DELIVERY, // Rider picked up
    DELIVERED,        // Order delivered
    CANCELLED         // Order cancelled
}

// Valid transitions
PENDING → CONFIRMED (payment success)
PENDING → CANCELLED (user cancel)
CONFIRMED → PREPARING (restaurant starts)
PREPARING → OUT_FOR_DELIVERY (rider picks up)
OUT_FOR_DELIVERY → DELIVERED (rider delivers)
```

---

## Interview Questions

**Q: Circuit Breaker kyun use kiya?**
A: Cascading failures se bachne ke liye. Agar Payment Service down hai, toh Order Service bhi hang ho jayegi. Circuit Breaker fast fail karta hai aur system stable rehta hai.

**Q: Circuit Breaker states explain karo.**
A: 
- CLOSED: Normal operation, calls go through
- OPEN: Service down, calls immediately fail
- HALF-OPEN: Testing if service recovered

**Q: Retry vs Circuit Breaker mein difference?**
A: Retry temporary failures ke liye (network glitch). Circuit Breaker prolonged failures ke liye (service down). Dono saath use karte hain.

**Q: Fallback method kya karta hai?**
A: Jab circuit open ho ya call fail ho, fallback execute hota hai. Graceful degradation - user ko error nahi, alternative response milta hai.

**Q: Order aur Payment atomicity kaise handle ki?**
A: Saga pattern use kiya. Order create → Payment call → Success/Fail based status update. Distributed transaction nahi, eventual consistency.

---

## Best Practices

1. **Circuit Breaker** - External service calls protect karo
2. **Retry with Backoff** - Temporary failures handle karo
3. **Timeout** - Hanging calls prevent karo
4. **Fallback** - Graceful degradation
5. **Idempotency** - Duplicate orders prevent karo
6. **Status Audit** - Status changes log karo
