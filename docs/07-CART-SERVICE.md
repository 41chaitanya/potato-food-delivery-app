# Cart Service (Port: 8085)

## Ye Service Kya Karti Hai?

Cart Service **shopping cart management** handle karti hai. Items add, remove, quantity update, aur cart clear - sab yahan hota hai. Redis caching se fast cart access hota hai.

---

## Features

| Feature | Description |
|---------|-------------|
| Add to Cart | Menu item cart mein add |
| Update Quantity | Item quantity change |
| Remove Item | Cart se item remove |
| Clear Cart | Poora cart empty |
| Restaurant Lock | Ek cart = Ek restaurant |
| Redis Caching | User cart cached |

---

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                   Cart Service (8085)                    │
│                                                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │
│  │  Controller  │  │   Service    │  │  Repository  │   │
│  │              │  │              │  │              │   │
│  │ /api/cart/*  │→ │  CartService │→ │  CartRepo    │   │
│  └──────────────┘  └──────────────┘  └──────────────┘   │
│         │                 │                  │           │
│         │                 ▼                  ▼           │
│         │         ┌──────────────┐   ┌──────────────┐   │
│         │         │    Redis     │   │  PostgreSQL  │   │
│         │         │   (Cache)    │   │   cart_db    │   │
│         │         └──────────────┘   └──────────────┘   │
│         │                                                │
│         │         ┌──────────────┐                      │
│         └────────→│    Menu      │ ← Item Validation    │
│                   │   Client     │                      │
│                   └──────────────┘                      │
└─────────────────────────────────────────────────────────┘
```

---

## Tech Stack

| Technology | Purpose |
|------------|---------|
| Spring Data JPA | Database operations |
| Redis Cache | User cart caching |
| WebClient | Menu Service call |
| PostgreSQL | Cart data storage |

---

## Database Schema

```sql
-- Cart table
CREATE TABLE carts (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    restaurant_id UUID NOT NULL,
    total_amount DECIMAL(10,2) DEFAULT 0,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Cart Items table
CREATE TABLE cart_items (
    id UUID PRIMARY KEY,
    cart_id UUID REFERENCES carts(id),
    menu_item_id UUID NOT NULL,
    quantity INT NOT NULL,
    price_per_item DECIMAL(10,2) NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

---

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/cart/items | Add item to cart |
| GET | /api/cart/{userId} | Get user's cart |
| PATCH | /api/cart/items/{itemId}?quantity=N | Update quantity |
| DELETE | /api/cart/items/{itemId} | Remove item |
| DELETE | /api/cart/{userId} | Clear cart |

---

## Key Flows

### 1. Add Item to Cart Flow
```
Client: POST /api/cart/items
{
  "userId": "user-uuid",
  "menuItemId": "menu-uuid",
  "quantity": 2
}
        │
        ▼
1. Validate menu item (call Menu Service)
   - Item exists?
   - Item available?
        │
        ▼
2. Get or Create cart for user
   - Existing cart? Use it
   - No cart? Create new
        │
        ▼
3. Restaurant match check
   - Cart restaurant = Item restaurant?
   - Mismatch → RestaurantMismatchException
        │
        ▼
4. Add/Update item
   - Item already in cart? Update quantity
   - New item? Add to cart
        │
        ▼
5. Recalculate total
6. Evict user's cache
        │
        ▼
Response:
{
  "id": "cart-uuid",
  "userId": "user-uuid",
  "restaurantId": "restaurant-uuid",
  "totalAmount": 598.00,
  "items": [
    {
      "id": "item-uuid",
      "menuItemId": "menu-uuid",
      "quantity": 2,
      "pricePerItem": 299.00,
      "totalPrice": 598.00
    }
  ]
}
```

### 2. Restaurant Lock Logic
```
Scenario: User has cart from Restaurant A
          User tries to add item from Restaurant B

Cart: { restaurantId: "restaurant-A" }
New Item: { restaurantId: "restaurant-B" }

Result: RestaurantMismatchException
Message: "Cart already has items from another restaurant. 
          Clear cart to order from different restaurant."

Why?
- Delivery logistics
- One order = One restaurant
- Zomato/Swiggy bhi same karte hain
```

### 3. Get Cart (Cached)
```
Client: GET /api/cart/{userId}
        │
        ▼
1. Check Redis cache: "cart:user:{userId}"
   ├── Cache HIT → Return cached data (~60ms)
   └── Cache MISS ↓
        │
        ▼
2. Query database
3. Store in Redis (TTL: 30 minutes)
4. Return response
```

---

## Redis Caching Strategy

```
Cache Key: cart:user:{userId}
TTL: 30 minutes
Value: CartResponse (JSON)

Cache Eviction:
- Item added → Evict user's cache
- Quantity updated → Evict user's cache
- Item removed → Evict user's cache
- Cart cleared → Evict user's cache

Why 30 minutes TTL?
- Cart data more dynamic than menu
- User might be actively shopping
- Balance between freshness and performance
```

### Caching Code
```java
@Service
public class CartServiceImpl {

    @Cacheable(value = "cart:user", key = "#userId")
    public CartResponse getCartByUserId(UUID userId) {
        log.info("Fetching from DATABASE (cache miss)");
        return cartRepository.findActiveCartByUserId(userId)...;
    }

    @CacheEvict(value = "cart:user", key = "#request.userId")
    public CartResponse addItemToCart(AddCartItemRequest request) {
        // Add item logic
    }

    @CacheEvict(value = "cart:user", key = "#userId")
    public void clearCart(UUID userId) {
        // Clear cart logic
    }
}
```

---

## Menu Service Integration

### Menu Client (WebClient)
```java
@Component
public class MenuClient {

    public MenuItemInternalResponse getMenuItemInternal(UUID menuItemId) {
        return webClient.get()
            .uri("/api/menus/internal/{id}", menuItemId)
            .retrieve()
            .bodyToMono(MenuItemInternalResponse.class)
            .block();
    }
}
```

### Validation Flow
```
Cart Service: "Ye menu item valid hai?"
        │
        ▼
Menu Service: GET /api/menus/internal/{id}
        │
        ▼
Response: {
  "id": "menu-uuid",
  "restaurantId": "restaurant-uuid",
  "price": 299.00,
  "available": true
}
        │
        ▼
Checks:
1. Item exists? ✓
2. Item available? ✓
3. Get price for cart calculation
```

---

## Cart Calculations

```java
// Cart Item total
cartItem.totalPrice = cartItem.pricePerItem * cartItem.quantity;

// Cart total
cart.totalAmount = cart.items.stream()
    .mapToDouble(item -> item.totalPrice)
    .sum();

Example:
Item 1: Butter Chicken × 2 = 299 × 2 = 598
Item 2: Naan × 4 = 49 × 4 = 196
Cart Total = 598 + 196 = 794
```

---

## Interview Questions

**Q: Restaurant lock kyun implement kiya?**
A: Delivery logistics ke liye. Ek order ek restaurant se aata hai. Multiple restaurants se items mix karna delivery complicated bana deta.

**Q: Cart caching kyun kiya?**
A: Checkout flow mein cart frequently access hota hai. Cache se fast response milta hai. 30 min TTL kyunki cart data dynamic hai.

**Q: Agar menu item price change ho jaye cart mein add karne ke baad?**
A: Cart mein `pricePerItem` store hota hai add time pe. Price lock ho jata hai. Order place karte waqt fresh price fetch kar sakte hain for verification.

**Q: Cart clear kab hota hai?**
A: Manual clear, Order place hone ke baad, ya TTL expire (inactive cart).

**Q: Multiple devices se same user cart access kare toh?**
A: Same cart milega (userId based). Cache consistent rehta hai. Real-time sync ke liye WebSocket add kar sakte hain.

---

## Best Practices

1. **Restaurant Lock** - One cart = One restaurant
2. **Price Snapshot** - Store price at add time
3. **Validate Items** - Check availability before add
4. **Cache User-wise** - Granular cache invalidation
5. **Recalculate Totals** - Always recalculate on changes
