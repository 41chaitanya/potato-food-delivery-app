# Menu Service (Port: 8084)

## Ye Service Kya Karti Hai?

Menu Service **menu items management** handle karti hai. Restaurant ke menu items create, update, delete, aur availability toggle - sab yahan hota hai. Redis caching se fast menu fetch hota hai.

---

## Features

| Feature | Description |
|---------|-------------|
| Menu CRUD | Create, Read, Update, Delete menu items |
| Restaurant Validation | Menu item sirf active restaurant ke liye |
| Meal Type Filter | Breakfast, Lunch, Dinner, Snacks |
| Availability Toggle | Item available/unavailable |
| Redis Caching | Menu by restaurant cached |

---

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                  Menu Service (8084)                     │
│                                                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │
│  │  Controller  │  │   Service    │  │  Repository  │   │
│  │              │  │              │  │              │   │
│  │ /api/menus/* │→ │  MenuService │→ │MenuItemRepo  │   │
│  └──────────────┘  └──────────────┘  └──────────────┘   │
│         │                 │                  │           │
│         │                 ▼                  ▼           │
│         │         ┌──────────────┐   ┌──────────────┐   │
│         │         │    Redis     │   │  PostgreSQL  │   │
│         │         │   (Cache)    │   │   menu_db    │   │
│         │         └──────────────┘   └──────────────┘   │
│         │                                                │
│         │         ┌──────────────┐                      │
│         └────────→│ Restaurant   │ ← Validation         │
│                   │   Client     │                      │
│                   └──────────────┘                      │
└─────────────────────────────────────────────────────────┘
```

---

## Tech Stack

| Technology | Purpose |
|------------|---------|
| Spring Data JPA | Database operations |
| Redis Cache | Menu items caching |
| WebClient | Restaurant Service call |
| PostgreSQL | Menu data storage |

---

## Database Schema

```sql
CREATE TABLE menu_items (
    id UUID PRIMARY KEY,
    restaurant_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    meal_type VARCHAR(20),      -- BREAKFAST, LUNCH, DINNER, SNACKS
    occasion_type VARCHAR(20),  -- REGULAR, SPECIAL, FESTIVAL
    status VARCHAR(20),         -- ACTIVE, INACTIVE
    available BOOLEAN DEFAULT true,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

### Enums
```java
public enum MealType {
    BREAKFAST, LUNCH, DINNER, SNACKS
}

public enum OccasionType {
    REGULAR, SPECIAL, FESTIVAL
}

public enum MenuStatus {
    ACTIVE, INACTIVE
}
```

---

## API Endpoints

### Public Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/menus/{id} | Get menu item by ID |
| GET | /api/menus/restaurant/{restaurantId} | Get menu by restaurant |
| GET | /api/menus/restaurant/{id}/meal-type/{type} | Filter by meal type |

### Restaurant Owner Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/menus | Create menu item |
| PATCH | /api/menus/{id} | Update menu item |
| PATCH | /api/menus/{id}/toggle-availability | Toggle available |
| DELETE | /api/menus/{id} | Soft delete |

### Internal API (for Cart Service)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/menus/internal/{id} | Get menu item for Cart |

---

## Key Flows

### 1. Create Menu Item Flow
```
Owner: POST /api/menus
{
  "restaurantId": "restaurant-uuid",
  "name": "Butter Chicken",
  "description": "Creamy tomato curry",
  "price": 299.00,
  "mealType": "DINNER"
}
        │
        ▼
1. Validate restaurant exists (call Restaurant Service)
2. Check restaurant is active
3. Create menu item
4. Evict cache for this restaurant
        │
        ▼
Response:
{
  "id": "menu-uuid",
  "name": "Butter Chicken",
  "price": 299.00,
  "available": true
}
```

### 2. Get Menu by Restaurant (Cached)
```
Client: GET /api/menus/restaurant/{restaurantId}
        │
        ▼
1. Check Redis cache: "menu:restaurant:{restaurantId}"
   ├── Cache HIT → Return cached data (~60ms)
   └── Cache MISS ↓
        │
        ▼
2. Query database
3. Store in Redis (TTL: 15 minutes)
4. Return response (~1000ms first time)
```

### 3. Restaurant Validation
```
Menu Service: "Is restaurant valid?"
        │
        ▼
WebClient call to Restaurant Service:
GET http://RESTAURANT-SERVICE/api/restaurants/internal/{id}
        │
        ▼
Response: { "id": "...", "active": true }
        │
        ▼
If active = false → RestaurantNotActiveException
If not found → RestaurantNotFoundException
```

---

## Redis Caching Strategy

```
Cache Keys:
1. menu:restaurant:{restaurantId}
   - All menu items for a restaurant
   - TTL: 15 minutes

2. menu:restaurant:meal:{restaurantId}:{mealType}
   - Menu items filtered by meal type
   - TTL: 15 minutes

Cache Eviction:
- Menu item created → Evict restaurant's cache
- Menu item updated → Evict restaurant's cache
- Menu item deleted → Evict restaurant's cache
- Availability toggled → Evict restaurant's cache

Performance:
- Without cache: ~1000ms
- With cache: ~60ms
- Improvement: ~16x faster
```

### Caching Code
```java
@Service
public class MenuServiceImpl {

    @Cacheable(value = "menu:restaurant", key = "#restaurantId")
    public List<MenuItemResponse> getMenuByRestaurant(UUID restaurantId) {
        log.info("Fetching from DATABASE (cache miss)");
        return menuItemRepository.findByRestaurantIdAndStatus(...)...;
    }

    @Caching(evict = {
        @CacheEvict(value = "menu:restaurant", key = "#request.restaurantId"),
        @CacheEvict(value = "menu:restaurant:meal", allEntries = true)
    })
    public MenuItemResponse createMenuItem(CreateMenuItemRequest request) {
        // Create menu item
    }
}
```

---

## Service-to-Service Communication

### Restaurant Client (WebClient)
```java
@Component
public class RestaurantClient {

    private final WebClient webClient;

    public RestaurantClient(WebClient.Builder builder) {
        this.webClient = builder
            .baseUrl("http://RESTAURANT-SERVICE")  // Eureka service name
            .build();
    }

    public RestaurantInternalResponse getRestaurantInternal(UUID restaurantId) {
        return webClient.get()
            .uri("/api/restaurants/internal/{id}", restaurantId)
            .retrieve()
            .bodyToMono(RestaurantInternalResponse.class)
            .block();
    }
}
```

---

## Internal API for Cart Service

Cart Service ko menu item details chahiye:

```
Cart Service: "Ye menu item available hai?"
        │
        ▼
GET /api/menus/internal/{menuItemId}
        │
        ▼
Response:
{
  "id": "menu-uuid",
  "restaurantId": "restaurant-uuid",
  "name": "Butter Chicken",
  "price": 299.00,
  "available": true
}
        │
        ▼
Cart Service: "Haan, cart mein add kar sakte hain"
```

---

## Interview Questions

**Q: Menu Service mein caching kyun important hai?**
A: Menu data frequently access hota hai (customer browsing), but rarely change hota hai. Cache se 16x faster response milta hai.

**Q: Restaurant validation kyun kiya?**
A: Data integrity ke liye. Menu item sirf valid, active restaurant ke liye create hona chahiye. Invalid restaurant ke menu items create nahi hone chahiye.

**Q: WebClient kyun use kiya RestTemplate nahi?**
A: WebClient non-blocking hai, better performance. RestTemplate deprecated hai Spring 5+. WebClient reactive programming support karta hai.

**Q: Cache key design kaise kiya?**
A: Hierarchical keys: `menu:restaurant:{id}`. Restaurant-wise cache hota hai. Ek restaurant ka menu change ho toh sirf uska cache clear hota hai.

**Q: Meal type filter kyun add kiya?**
A: User experience ke liye. Customer breakfast time pe breakfast items dekhna chahta hai, dinner time pe dinner items.

---

## Best Practices

1. **Validate Dependencies** - Restaurant check before menu create
2. **Cache by Restaurant** - Granular cache invalidation
3. **Soft Delete** - Data preserve karo
4. **Internal APIs** - Simplified responses for services
5. **Non-blocking Calls** - WebClient use karo
