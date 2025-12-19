# Restaurant Service (Port: 8083)

## Ye Service Kya Karti Hai?

Restaurant Service **restaurant management** handle karti hai. Restaurant registration, approval workflow, status management (open/closed), aur admin operations - sab yahan hota hai.

---

## Features

| Feature | Description |
|---------|-------------|
| Restaurant CRUD | Create, Read, Update, Delete restaurants |
| Approval Workflow | PENDING → ACTIVE/REJECTED |
| Status Toggle | Open/Close restaurant |
| Redis Caching | Active restaurants list cached |
| Admin Dashboard | Stats, pending approvals |

---

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│               Restaurant Service (8083)                  │
│                                                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │
│  │  Controller  │  │   Service    │  │  Repository  │   │
│  │              │  │              │  │              │   │
│  │/api/restaurants│→│RestaurantSvc│→ │RestaurantRepo│   │
│  └──────────────┘  └──────────────┘  └──────────────┘   │
│                           │                  │           │
│                           ▼                  ▼           │
│                    ┌──────────────┐   ┌──────────────┐   │
│                    │    Redis     │   │  PostgreSQL  │   │
│                    │   (Cache)    │   │restaurant_db │   │
│                    └──────────────┘   └──────────────┘   │
└─────────────────────────────────────────────────────────┘
```

---

## Tech Stack

| Technology | Purpose |
|------------|---------|
| Spring Data JPA | Database operations |
| Redis Cache | Active restaurants caching |
| PostgreSQL | Restaurant data storage |

---

## Database Schema

```sql
CREATE TABLE restaurants (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    address TEXT NOT NULL,
    phone VARCHAR(15),
    cuisine_type VARCHAR(50),  -- INDIAN, CHINESE, ITALIAN, etc.
    status VARCHAR(20),        -- PENDING, ACTIVE, CLOSED, REJECTED
    active BOOLEAN DEFAULT false,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

### Cuisine Types
```java
public enum CuisineType {
    INDIAN, CHINESE, ITALIAN, MEXICAN, 
    FAST_FOOD, STREET_FOOD, CONTINENTAL
}
```

### Restaurant Status
```java
public enum RestaurantStatus {
    PENDING,   // Newly registered, awaiting approval
    ACTIVE,    // Approved and open
    CLOSED,    // Temporarily closed
    REJECTED   // Admin rejected
}
```

---

## API Endpoints

### Public Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/restaurants | Get all active restaurants |
| GET | /api/restaurants/{id} | Get restaurant by ID |

### Restaurant Owner Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/restaurants | Create new restaurant |
| PATCH | /api/restaurants/{id} | Update restaurant |
| PATCH | /api/restaurants/{id}/toggle-status | Open/Close |
| DELETE | /api/restaurants/{id} | Soft delete |

### Admin Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/restaurants/admin/all | Get all restaurants |
| GET | /api/restaurants/admin/pending | Get pending approvals |
| PATCH | /api/restaurants/admin/{id}/approve | Approve restaurant |
| PATCH | /api/restaurants/admin/{id}/reject | Reject restaurant |
| GET | /api/restaurants/admin/stats | Get statistics |

### Internal API (for other services)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/restaurants/internal/{id} | Get restaurant for Menu Service |

---

## Key Flows

### 1. Restaurant Registration Flow
```
Owner: POST /api/restaurants
{
  "name": "Spice Garden",
  "address": "MG Road, Bangalore",
  "phone": "9800000001",
  "cuisineType": "INDIAN"
}
        │
        ▼
1. Restaurant create with status = PENDING
2. active = false
3. Save to database
        │
        ▼
Response:
{
  "id": "uuid",
  "name": "Spice Garden",
  "status": "PENDING",
  "active": false
}
```

### 2. Approval Flow
```
Admin: PATCH /api/restaurants/admin/{id}/approve
        │
        ▼
1. Find restaurant
2. Set status = ACTIVE
3. Set active = true
4. Evict Redis cache (new restaurant added)
        │
        ▼
Restaurant now visible in active list
```

### 3. Get Active Restaurants (Cached)
```
Client: GET /api/restaurants
        │
        ▼
1. Check Redis cache: "restaurants:active:list"
   ├── Cache HIT → Return cached data (fast!)
   └── Cache MISS ↓
        │
        ▼
2. Query database: findByActiveTrue()
3. Store in Redis cache (TTL: 10 minutes)
4. Return response
```

---

## Redis Caching Strategy

```
Cache Key: restaurants:active:list
TTL: 10 minutes
Value: List<RestaurantResponse> (JSON)

Cache Eviction (Clear cache when):
- New restaurant created
- Restaurant approved/rejected
- Restaurant status toggled
- Restaurant updated/deleted

Why Cache?
- Restaurant list frequently accessed
- Data rarely changes
- ~12x performance improvement (800ms → 65ms)
```

### Caching Annotations
```java
@Service
public class RestaurantServiceImpl {

    // Cache active restaurants
    @Cacheable(value = "restaurants:active:list")
    public List<RestaurantResponse> getAllActiveRestaurants() {
        log.info("Fetching from DATABASE (cache miss)");
        return restaurantRepository.findByActiveTrue()...;
    }

    // Evict cache on create
    @CacheEvict(value = "restaurants:active:list", allEntries = true)
    public RestaurantResponse createRestaurant(CreateRestaurantRequest request) {
        // Create restaurant
    }

    // Evict cache on approve
    @CacheEvict(value = "restaurants:active:list", allEntries = true)
    public RestaurantResponse approveRestaurant(UUID restaurantId) {
        // Approve restaurant
    }
}
```

---

## Internal API Usage

Menu Service ko restaurant validate karna hota hai:

```
Menu Service: "Kya ye restaurant exist karta hai aur active hai?"
        │
        ▼
GET /api/restaurants/internal/{restaurantId}
        │
        ▼
Response:
{
  "id": "uuid",
  "name": "Spice Garden",
  "active": true
}
        │
        ▼
Menu Service: "Haan, menu item create kar sakte hain"
```

---

## Interview Questions

**Q: Restaurant approval workflow kyun implement kiya?**
A: Quality control ke liye. Koi bhi restaurant register kar sakta hai, but admin verify karta hai before it goes live. Fake restaurants filter hote hain.

**Q: Redis caching kyun use kiya?**
A: Restaurant list frequently access hoti hai (homepage pe), but rarely change hoti hai. Cache se 12x faster response milta hai.

**Q: Cache invalidation kaise handle kiya?**
A: `@CacheEvict` annotation use kiya. Jab bhi restaurant create/update/delete hota hai, cache clear ho jata hai.

**Q: Soft delete kyun kiya hard delete nahi?**
A: Data integrity ke liye. Restaurant delete karne se orders, menus ka reference break ho sakta hai. Soft delete se data preserve rehta hai.

**Q: Internal API kyun banaya?**
A: Service-to-service communication ke liye. Menu Service ko restaurant validate karna hota hai. Internal API simplified response deta hai.

---

## Best Practices

1. **Soft Delete** - Data preserve karo
2. **Status Workflow** - Clear state transitions
3. **Cache Wisely** - Frequently read, rarely write data
4. **Internal APIs** - Simplified responses for services
5. **Validation** - Input validation on create/update
