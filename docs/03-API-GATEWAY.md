# API Gateway (Port: 8080)

## Ye Service Kya Karti Hai?

API Gateway **single entry point** hai poore system ka. Sab requests pehle yahan aati hain, phir appropriate service ko forward hoti hain. Ye JWT validation, routing, aur load balancing handle karta hai.

---

## Kyun Zaroori Hai?

**Without API Gateway:**
```
Client ko har service ka address pata hona chahiye:
- User Auth: http://localhost:8086
- Menu: http://localhost:8084
- Order: http://localhost:8081

Problems:
- Multiple endpoints expose
- Har service pe security lagani padegi
- CORS issues
- No centralized logging
```

**With API Gateway:**
```
Client sirf ek address jaanta hai: http://localhost:8080

Gateway internally route karta hai:
- /auth/* → User Auth Service
- /api/menus/* → Menu Service
- /api/orders/* → Order Service
```

---

## Architecture

```
                              Client
                                │
                                ▼
┌─────────────────────────────────────────────────────────┐
│                    API Gateway (8080)                    │
│                                                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │
│  │   Routing    │  │     JWT      │  │    Load      │   │
│  │   Rules      │  │  Validation  │  │  Balancing   │   │
│  └──────────────┘  └──────────────┘  └──────────────┘   │
│                                                          │
│  Routes:                                                 │
│  /auth/**        → USER-AUTH-SERVICE                    │
│  /api/restaurants/** → RESTAURANT-SERVICE               │
│  /api/menus/**   → MENU-SERVICE                         │
│  /api/cart/**    → CART-SERVICE                         │
│  /api/orders/**  → ORDER-SERVICE                        │
│  /api/delivery/** → DELIVERY-SERVICE                    │
└─────────────────────────────────────────────────────────┘
                                │
        ┌───────────────────────┼───────────────────────┐
        ▼                       ▼                       ▼
   ┌─────────┐            ┌─────────┐            ┌─────────┐
   │  User   │            │  Menu   │            │  Order  │
   │  Auth   │            │ Service │            │ Service │
   └─────────┘            └─────────┘            └─────────┘
```

---

## Tech Stack

| Technology | Purpose |
|------------|---------|
| Spring Cloud Gateway | Reactive API Gateway |
| JWT (jjwt) | Token validation |
| Eureka Client | Service discovery |
| WebFlux | Reactive programming |

---

## Key Components

### 1. Route Configuration (application.yml)
```yaml
spring:
  cloud:
    gateway:
      routes:
        # Auth Service - Public endpoints
        - id: auth-service
          uri: lb://USER-AUTH-SERVICE  # lb = load balanced
          predicates:
            - Path=/auth/**
          filters:
            - RewritePath=/auth/(?<segment>.*), /api/auth/${segment}

        # Menu Service - Protected
        - id: menu-service
          uri: lb://MENU-SERVICE
          predicates:
            - Path=/api/menus/**
          filters:
            - AuthenticationFilter  # JWT check

        # Order Service - Protected
        - id: order-service
          uri: lb://ORDER-SERVICE
          predicates:
            - Path=/api/orders/**
          filters:
            - AuthenticationFilter
```

### 2. JWT Authentication Filter
```java
@Component
public class AuthenticationFilter implements GatewayFilter {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1. Authorization header check
        String authHeader = exchange.getRequest()
            .getHeaders()
            .getFirst("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }
        
        // 2. Token extract
        String token = authHeader.substring(7);
        
        // 3. Token validate
        if (!jwtUtil.isTokenValid(token)) {
            return unauthorized(exchange);
        }
        
        // 4. User info headers mein add (downstream services ke liye)
        ServerHttpRequest request = exchange.getRequest().mutate()
            .header("X-User-Id", jwtUtil.getUserId(token))
            .header("X-User-Role", jwtUtil.getRole(token))
            .build();
        
        return chain.filter(exchange.mutate().request(request).build());
    }
}
```

### 3. Route Validator (Public vs Protected)
```java
@Component
public class RouteValidator {
    
    // Ye endpoints public hain - JWT nahi chahiye
    public static final List<String> openEndpoints = List.of(
        "/auth/register",
        "/auth/login",
        "/actuator/health"
    );
    
    public boolean isSecured(ServerHttpRequest request) {
        return openEndpoints.stream()
            .noneMatch(uri -> request.getURI().getPath().contains(uri));
    }
}
```

---

## Request Flow

```
1. Client Request: POST /api/orders
   Headers: Authorization: Bearer eyJhbGc...
        │
        ▼
2. Gateway receives request
        │
        ▼
3. Route Matching: /api/orders/** → ORDER-SERVICE
        │
        ▼
4. Filter Chain Execute:
   a. RouteValidator: Is this protected? → YES
   b. AuthenticationFilter: 
      - Extract token
      - Validate signature
      - Check expiry
      - Add X-User-Id, X-User-Role headers
        │
        ▼
5. Service Discovery: ORDER-SERVICE kahan hai?
   Eureka se address milta hai
        │
        ▼
6. Load Balancing: Multiple instances mein se ek choose
        │
        ▼
7. Forward Request to Order Service
        │
        ▼
8. Response back to Client
```

---

## Routing Rules

| Client Request | Gateway Route | Target Service |
|----------------|---------------|----------------|
| POST /auth/login | /auth/** | USER-AUTH-SERVICE |
| GET /api/restaurants | /api/restaurants/** | RESTAURANT-SERVICE |
| GET /api/menus/restaurant/{id} | /api/menus/** | MENU-SERVICE |
| POST /api/cart/items | /api/cart/** | CART-SERVICE |
| POST /api/orders | /api/orders/** | ORDER-SERVICE |
| GET /api/delivery/rider | /api/delivery/** | DELIVERY-SERVICE |

---

## JWT Validation

```
Token Structure:
┌─────────────────────────────────────────────────────────┐
│ Header.Payload.Signature                                 │
│                                                          │
│ Header: { "alg": "HS512", "typ": "JWT" }                │
│                                                          │
│ Payload: {                                               │
│   "sub": "user-uuid",                                   │
│   "email": "user@example.com",                          │
│   "role": "USER",                                       │
│   "exp": 1703001600                                     │
│ }                                                        │
│                                                          │
│ Signature: HMACSHA512(header + payload, secret)         │
└─────────────────────────────────────────────────────────┘

Validation Steps:
1. Signature verify (secret key se)
2. Expiry check (exp claim)
3. Claims extract (userId, role)
```

---

## Interview Questions

**Q: API Gateway kyun use kiya?**
A: Single entry point, centralized authentication, routing, load balancing, cross-cutting concerns ek jagah handle hote hain.

**Q: Spring Cloud Gateway vs Zuul?**
A: Gateway reactive hai (WebFlux), better performance. Zuul blocking hai, Netflix ne deprecate kar diya.

**Q: JWT validation Gateway pe kyun?**
A: Centralized security. Har service pe JWT logic duplicate nahi karna padta. Invalid requests services tak pahunchti hi nahi.

**Q: Load Balancing kaise kaam karta hai?**
A: `lb://SERVICE-NAME` use karte hain. Gateway Eureka se instances fetch karta hai aur round-robin ya random algorithm se distribute karta hai.

**Q: Agar Gateway down ho jaye?**
A: Poora system inaccessible ho jayega. Production mein multiple Gateway instances run karo with load balancer (nginx/HAProxy) in front.

---

## Best Practices

1. **Rate Limiting** - Abuse se bachne ke liye
2. **Circuit Breaker** - Downstream failures handle karo
3. **Request/Response Logging** - Debugging ke liye
4. **CORS Configuration** - Frontend integration ke liye
5. **Multiple Instances** - High availability ke liye
6. **Health Checks** - Monitoring ke liye
