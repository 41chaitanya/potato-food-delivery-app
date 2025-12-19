# Admin Service (Port: 8088)

## Ye Service Kya Karti Hai?

Admin Service **platform management** handle karti hai. Platform analytics, commission configuration, aur aggregated stats - sab yahan hota hai. Ye service dusri services se data aggregate karke admin dashboard ke liye provide karti hai.

---

## Features

| Feature | Description |
|---------|-------------|
| Platform Analytics | Overall platform stats |
| Commission Config | Platform commission settings |
| User Stats | User counts by role |
| Restaurant Stats | Restaurant counts by status |
| Order Stats | Order analytics |

---

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                   Admin Service (8088)                   │
│                                                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │
│  │  Controller  │  │   Service    │  │  Repository  │   │
│  │              │  │              │  │              │   │
│  │ /api/admin/* │→ │ AdminService │→ │  ConfigRepo  │   │
│  └──────────────┘  └──────────────┘  └──────────────┘   │
│         │                 │                              │
│         │                 ▼                              │
│         │    ┌────────────────────────────┐             │
│         │    │      Service Clients       │             │
│         │    │                            │             │
│         │    │  ┌──────┐ ┌──────┐ ┌────┐ │             │
│         │    │  │ User │ │ Rest │ │Order│ │             │
│         │    │  │Client│ │Client│ │Client│             │
│         │    │  └──────┘ └──────┘ └────┘ │             │
│         │    └────────────────────────────┘             │
└─────────────────────────────────────────────────────────┘
                              │
              ┌───────────────┼───────────────┐
              ▼               ▼               ▼
        ┌─────────┐     ┌─────────┐     ┌─────────┐
        │  User   │     │Restaurant│    │  Order  │
        │  Auth   │     │ Service │     │ Service │
        └─────────┘     └─────────┘     └─────────┘
```

---

## Tech Stack

| Technology | Purpose |
|------------|---------|
| Spring Data JPA | Database operations |
| WebClient | Other services call |
| PostgreSQL | Config data storage |

---

## Database Schema

```sql
-- Commission Configuration
CREATE TABLE commission_config (
    id UUID PRIMARY KEY,
    commission_percentage DECIMAL(5,2) DEFAULT 10.00,
    min_order_value DECIMAL(10,2) DEFAULT 100.00,
    delivery_charge DECIMAL(10,2) DEFAULT 30.00,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Platform Stats (Historical)
CREATE TABLE platform_stats (
    id UUID PRIMARY KEY,
    stats_date DATE UNIQUE,
    total_users BIGINT,
    total_restaurants BIGINT,
    total_riders BIGINT,
    total_orders BIGINT,
    completed_orders BIGINT,
    cancelled_orders BIGINT,
    total_revenue DECIMAL(15,2),
    total_commission DECIMAL(15,2),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

---

## API Endpoints

### Analytics Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/admin/analytics | Get platform analytics |
| GET | /api/admin/analytics/users | Get user stats |
| GET | /api/admin/analytics/restaurants | Get restaurant stats |
| GET | /api/admin/analytics/orders | Get order stats |

### Commission Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/admin/commission | Get current commission config |
| PUT | /api/admin/commission | Update commission config |

---

## Key Flows

### 1. Get Platform Analytics
```
Admin: GET /api/admin/analytics
        │
        ▼
1. Call User Auth Service → Get user stats
   {
     "totalUsers": 150,
     "activeUsers": 140,
     "blockedUsers": 10,
     "admins": 2,
     "riders": 15
   }
        │
        ▼
2. Call Restaurant Service → Get restaurant stats
   {
     "totalRestaurants": 50,
     "activeRestaurants": 40,
     "pendingRestaurants": 5,
     "rejectedRestaurants": 5
   }
        │
        ▼
3. Call Order Service → Get order stats
   {
     "totalOrders": 1000,
     "completedOrders": 900,
     "cancelledOrders": 50,
     "pendingOrders": 50
   }
        │
        ▼
4. Aggregate and return
Response:
{
  "users": { ... },
  "restaurants": { ... },
  "orders": { ... },
  "revenue": {
    "totalRevenue": 500000.00,
    "totalCommission": 50000.00
  }
}
```

### 2. Commission Configuration
```
Admin: PUT /api/admin/commission
{
  "commissionPercentage": 12.5,
  "minOrderValue": 150.00,
  "deliveryCharge": 40.00
}
        │
        ▼
1. Validate values
2. Update config
3. Return updated config
        │
        ▼
Response:
{
  "id": "config-uuid",
  "commissionPercentage": 12.5,
  "minOrderValue": 150.00,
  "deliveryCharge": 40.00,
  "active": true
}
```

---

## Service Clients

### User Auth Client
```java
@Component
public class UserAuthClient {

    public UserStatsResponse getUserStats() {
        return webClient.get()
            .uri("/api/auth/admin/stats")
            .retrieve()
            .bodyToMono(UserStatsResponse.class)
            .block();
    }
}
```

### Restaurant Client
```java
@Component
public class RestaurantClient {

    public RestaurantStatsResponse getRestaurantStats() {
        return webClient.get()
            .uri("/api/restaurants/admin/stats")
            .retrieve()
            .bodyToMono(RestaurantStatsResponse.class)
            .block();
    }
}
```

### Order Client
```java
@Component
public class OrderClient {

    public OrderStatsResponse getOrderStats() {
        return webClient.get()
            .uri("/api/orders/admin/stats")
            .retrieve()
            .bodyToMono(OrderStatsResponse.class)
            .block();
    }
}
```

---

## Analytics Response Structure

```java
public class PlatformAnalyticsResponse {
    
    private UserStats users;
    private RestaurantStats restaurants;
    private OrderStats orders;
    private RevenueStats revenue;
    
    @Data
    public static class UserStats {
        private long totalUsers;
        private long activeUsers;
        private long blockedUsers;
        private long admins;
        private long riders;
    }
    
    @Data
    public static class RestaurantStats {
        private long totalRestaurants;
        private long activeRestaurants;
        private long pendingRestaurants;
        private long rejectedRestaurants;
    }
    
    @Data
    public static class OrderStats {
        private long totalOrders;
        private long completedOrders;
        private long cancelledOrders;
        private long pendingOrders;
    }
    
    @Data
    public static class RevenueStats {
        private BigDecimal totalRevenue;
        private BigDecimal totalCommission;
        private BigDecimal averageOrderValue;
    }
}
```

---

## Interview Questions

**Q: Admin Service alag kyun banaya?**
A: Separation of concerns. Analytics aur config management alag service mein. Dusri services apne core functionality pe focus karti hain.

**Q: Data aggregation kaise kiya?**
A: Multiple service calls karke data collect kiya. WebClient se parallel calls bhi kar sakte hain for better performance.

**Q: Real-time analytics kaise karoge?**
A: 
1. Event-driven architecture (Kafka)
2. Services events publish karein
3. Admin Service consume karke aggregate kare
4. Or use dedicated analytics service (Elasticsearch)

**Q: Commission calculation kahan hota hai?**
A: Order Service mein. Order create time pe commission calculate hota hai based on current config.

**Q: Historical stats kaise store karte ho?**
A: Daily cron job platform_stats table mein snapshot store karta hai. Trend analysis ke liye useful.

---

## Best Practices

1. **Caching** - Analytics data cache karo (expensive queries)
2. **Async Calls** - Parallel service calls for speed
3. **Fallback** - Service down ho toh cached/default data
4. **Rate Limiting** - Analytics endpoints pe limit
5. **Historical Data** - Trends ke liye snapshots store karo
