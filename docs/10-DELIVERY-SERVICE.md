# Delivery Service (Port: 8087)

## Ye Service Kya Karti Hai?

Delivery Service **delivery management** handle karti hai. Order ko rider assign karna, pickup, delivery tracking - sab yahan hota hai. Admin rider assign karta hai, Rider pickup aur deliver karta hai.

---

## Features

| Feature | Description |
|---------|-------------|
| Assign Delivery | Admin order ko rider assign karta hai |
| Pickup | Rider restaurant se order pickup |
| Deliver | Rider customer ko deliver |
| Rider Dashboard | Rider ke assigned deliveries |
| Delivery Tracking | Status updates |

---

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                 Delivery Service (8087)                  │
│                                                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │
│  │  Controller  │  │   Service    │  │  Repository  │   │
│  │              │  │              │  │              │   │
│  │/api/delivery │→ │DeliveryService│→│DeliveryRepo  │   │
│  └──────────────┘  └──────────────┘  └──────────────┘   │
│         │                 │                  │           │
│         │                 ▼                  ▼           │
│         │         ┌──────────────┐   ┌──────────────┐   │
│         │         │    Order     │   │  PostgreSQL  │   │
│         │         │   Client     │   │ delivery_db  │   │
│         │         └──────────────┘   └──────────────┘   │
└─────────────────────────────────────────────────────────┘
```

---

## Tech Stack

| Technology | Purpose |
|------------|---------|
| Spring Data JPA | Database operations |
| WebClient | Order Service call |
| PostgreSQL | Delivery data storage |

---

## Database Schema

```sql
CREATE TABLE deliveries (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL UNIQUE,
    rider_id UUID NOT NULL,
    customer_name VARCHAR(100),
    customer_address TEXT,
    restaurant_name VARCHAR(100),
    restaurant_address TEXT,
    status VARCHAR(20),  -- ASSIGNED, PICKED_UP, DELIVERED, CANCELLED
    assigned_at TIMESTAMP,
    picked_up_at TIMESTAMP,
    delivered_at TIMESTAMP,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

### Delivery Status Flow
```
ASSIGNED → PICKED_UP → DELIVERED
    │          │
    └──────────┴──→ CANCELLED
```

---

## API Endpoints

### Admin Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/delivery/assign/{orderId} | Assign rider to order |
| GET | /api/delivery/all | Get all deliveries |

### Rider Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/delivery/rider | Get my deliveries |
| PUT | /api/delivery/{id}/pickup | Mark as picked up |
| PUT | /api/delivery/{id}/deliver | Mark as delivered |

### Public Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/delivery/{id} | Get delivery by ID |
| GET | /api/delivery/order/{orderId} | Get delivery by order |

---

## Key Flows

### 1. Assign Delivery Flow (Admin)
```
Admin: POST /api/delivery/assign/{orderId}
Headers: X-User-Id: admin-uuid, X-User-Role: ADMIN
Body: { "riderId": "rider-uuid" }
        │
        ▼
1. Verify admin role
2. Get order details from Order Service
3. Check order status (should be CONFIRMED/PAID)
4. Check rider capacity (max 3 active deliveries)
5. Create delivery record
        │
        ▼
Response:
{
  "id": "delivery-uuid",
  "orderId": "order-uuid",
  "riderId": "rider-uuid",
  "status": "ASSIGNED",
  "customerName": "Rahul Sharma",
  "restaurantName": "Spice Garden"
}
```

### 2. Pickup Flow (Rider)
```
Rider: PUT /api/delivery/{id}/pickup
Headers: X-User-Id: rider-uuid, X-User-Role: RIDER
        │
        ▼
1. Verify rider owns this delivery
2. Check current status = ASSIGNED
3. Update status = PICKED_UP
4. Set picked_up_at timestamp
5. Update Order status = OUT_FOR_DELIVERY
        │
        ▼
Response:
{
  "id": "delivery-uuid",
  "status": "PICKED_UP",
  "pickedUpAt": "2024-12-19T15:30:00"
}
```

### 3. Deliver Flow (Rider)
```
Rider: PUT /api/delivery/{id}/deliver
Headers: X-User-Id: rider-uuid, X-User-Role: RIDER
        │
        ▼
1. Verify rider owns this delivery
2. Check current status = PICKED_UP
3. Update status = DELIVERED
4. Set delivered_at timestamp
5. Update Order status = DELIVERED
        │
        ▼
Response:
{
  "id": "delivery-uuid",
  "status": "DELIVERED",
  "deliveredAt": "2024-12-19T16:00:00"
}
```

---

## Role-Based Access

```
┌─────────────────────────────────────────────────────────┐
│                    Access Control                        │
├─────────────────────────────────────────────────────────┤
│ ADMIN:                                                   │
│   ✓ Assign delivery to rider                            │
│   ✓ View all deliveries                                 │
│   ✓ Cancel any delivery                                 │
│                                                          │
│ RIDER:                                                   │
│   ✓ View own assigned deliveries                        │
│   ✓ Pickup own delivery                                 │
│   ✓ Deliver own delivery                                │
│   ✗ Cannot see other riders' deliveries                 │
│                                                          │
│ USER:                                                    │
│   ✓ Track own order's delivery                          │
│   ✗ Cannot modify delivery                              │
└─────────────────────────────────────────────────────────┘
```

### Authorization Check
```java
// Rider can only access own deliveries
public DeliveryResponse pickup(UUID deliveryId, UUID riderId) {
    Delivery delivery = findDeliveryOrThrow(deliveryId);
    
    if (!delivery.getRiderId().equals(riderId)) {
        throw new UnauthorizedRiderException(
            "You can only pickup your own deliveries"
        );
    }
    
    // Proceed with pickup
}
```

---

## Rider Capacity Management

```java
// Rider max 3 active deliveries at a time
public void assignDelivery(UUID orderId, UUID riderId) {
    long activeDeliveries = deliveryRepository
        .countByRiderIdAndStatusIn(riderId, 
            List.of(ASSIGNED, PICKED_UP));
    
    if (activeDeliveries >= 3) {
        throw new RiderCapacityExceededException(
            "Rider already has 3 active deliveries"
        );
    }
    
    // Proceed with assignment
}
```

---

## Order Service Integration

```java
@Component
public class OrderClient {

    public OrderBasicResponse getOrderBasic(UUID orderId) {
        return webClient.get()
            .uri("/api/orders/internal/{id}", orderId)
            .retrieve()
            .bodyToMono(OrderBasicResponse.class)
            .block();
    }

    public void updateOrderStatus(UUID orderId, String status) {
        webClient.patch()
            .uri("/api/orders/{id}/status?status={status}", orderId, status)
            .retrieve()
            .bodyToMono(Void.class)
            .block();
    }
}
```

---

## Interview Questions

**Q: Delivery assignment kyun manual hai, automatic kyun nahi?**
A: Simple implementation. Real system mein automatic assignment hota hai based on:
- Rider location
- Rider availability
- Restaurant proximity
- Delivery route optimization

**Q: Rider capacity limit kyun rakha?**
A: Quality of service ke liye. Zyada deliveries = delays. 3 limit reasonable hai for timely delivery.

**Q: Order status sync kaise hota hai?**
A: Delivery Service Order Service ko call karta hai status update ke liye. Eventual consistency - thoda delay ho sakta hai.

**Q: Real-time tracking kaise implement karoge?**
A: WebSocket use karenge. Rider app location updates bhejega. Customer app real-time location dikhayega.

**Q: Delivery cancel kaise hoga?**
A: Admin cancel kar sakta hai. Order Service ko notify hoga. Refund process trigger hoga.

---

## Best Practices

1. **Role Verification** - Har action pe role check
2. **Ownership Check** - Rider apni delivery hi access kare
3. **Capacity Limits** - Overload prevent karo
4. **Status Transitions** - Valid transitions only
5. **Audit Trail** - Timestamps for all actions
6. **Sync with Order** - Order status updated rakho
