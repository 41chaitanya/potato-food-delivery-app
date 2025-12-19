# 🍕 Food Delivery Microservices - Project Overview

## Ye Project Kya Hai?

Ye ek **Food Delivery Platform** hai jo **Microservices Architecture** pe bana hai - jaise Zomato/Swiggy ka backend hota hai. Isme 11 independent services hain jo milke ek complete food ordering system banate hain.

---

## Architecture Diagram

```
                         ┌─────────────────────┐
                         │   Config Server     │ ← Sab services ki config yahan se aati hai
                         │      (8089)         │
                         └──────────┬──────────┘
                                    │
                         ┌──────────▼──────────┐
                         │  Service Registry   │ ← Sab services yahan register hoti hain
                         │   Eureka (8761)     │
                         └──────────┬──────────┘
                                    │
┌───────────────────────────────────▼───────────────────────────────────┐
│                        API Gateway (8080)                              │
│            JWT Validation + Routing + Load Balancing                   │
└───────────────────────────────────┬───────────────────────────────────┘
                                    │
    ┌───────────┬───────────┬───────┴───────┬───────────┬───────────┐
    ▼           ▼           ▼               ▼           ▼           ▼
┌───────┐  ┌───────┐  ┌───────┐       ┌───────┐  ┌───────┐  ┌───────┐
│ User  │  │ Rest- │  │ Menu  │       │ Cart  │  │ Order │  │Delivery│
│ Auth  │  │aurant │  │Service│       │Service│  │Service│  │Service │
│ 8086  │  │ 8083  │  │ 8084  │       │ 8085  │  │ 8081  │  │ 8087   │
└───┬───┘  └───┬───┘  └───┬───┘       └───┬───┘  └───┬───┘  └───┬───┘
    │          │          │               │          │          │
    └──────────┴──────────┴───────┬───────┴──────────┴──────────┘
                                  │
                    ┌─────────────▼─────────────┐
                    │      Redis Cloud          │ ← Caching + JWT Blacklist
                    │   (redis-13107.c212...)   │
                    └─────────────┬─────────────┘
                                  │
                    ┌─────────────▼─────────────┐
                    │      PostgreSQL           │ ← Database
                    │   (Each service ka DB)    │
                    └───────────────────────────┘
```

---

## Tech Stack - Detailed Explanation

### 1. Core Framework
| Technology | Version | Kya Karta Hai |
|------------|---------|---------------|
| **Java 21** | 21 | Latest LTS version, Virtual Threads support |
| **Spring Boot 3.5** | 3.5.x | Application framework - auto-configuration, embedded server |
| **Spring Cloud 2025** | 2025.0.0 | Microservices tools - config, discovery, gateway |
| **Maven** | 3.9+ | Build tool - dependency management |

### 2. Database & Caching
| Technology | Purpose | Kahan Use Hua |
|------------|---------|---------------|
| **PostgreSQL** | Primary Database | Sab services mein data store |
| **Redis Cloud** | Caching + Blacklist | Menu, Restaurant, Cart cache + JWT logout |
| **Spring Data JPA** | ORM | Database operations |
| **Hibernate** | JPA Implementation | Entity mapping |

### 3. Service Discovery & Config
| Technology | Purpose | Port |
|------------|---------|------|
| **Netflix Eureka** | Service Registry | 8761 |
| **Spring Cloud Config** | Centralized Config | 8089 |
| **Spring Cloud Gateway** | API Gateway | 8080 |

### 4. Security
| Technology | Purpose |
|------------|---------|
| **JWT (JSON Web Token)** | Stateless authentication |
| **Spring Security** | Security framework |
| **BCrypt** | Password hashing |
| **Redis JWT Blacklist** | Logout support |

### 5. Resilience & Communication
| Technology | Purpose |
|------------|---------|
| **Resilience4j** | Circuit Breaker, Retry, Rate Limiter |
| **WebClient** | Non-blocking HTTP calls |
| **RestTemplate** | Blocking HTTP calls |

### 6. Development Tools
| Technology | Purpose |
|------------|---------|
| **Lombok** | Boilerplate code reduction |
| **spring-dotenv** | .env file support |
| **Jackson** | JSON serialization |

---

## Services Overview

| # | Service | Port | Main Responsibility |
|---|---------|------|---------------------|
| 1 | Config Server | 8089 | Sab services ki configuration serve karta hai |
| 2 | Service Registry | 8761 | Services ko register aur discover karta hai |
| 3 | API Gateway | 8080 | Single entry point, JWT validation, routing |
| 4 | User Auth | 8086 | Registration, Login, JWT tokens, Logout |
| 5 | Restaurant | 8083 | Restaurant CRUD, approval workflow |
| 6 | Menu | 8084 | Menu items management |
| 7 | Cart | 8085 | Shopping cart operations |
| 8 | Order | 8081 | Order creation, payment integration |
| 9 | Payment | 8082 | Payment processing (mock) |
| 10 | Delivery | 8087 | Delivery assignment, rider management |
| 11 | Admin | 8088 | Platform analytics, commission management |

---

## Request Flow Example

**User Order Place Karta Hai:**

```
1. User → API Gateway (8080)
   └── JWT Token validate hota hai

2. API Gateway → Order Service (8081)
   └── Order create request

3. Order Service → Cart Service (8085)
   └── Cart data fetch (Redis cache se)

4. Order Service → Payment Service (8082)
   └── Payment process (Circuit Breaker protected)

5. Payment Success → Order Status Update
   └── Order CONFIRMED

6. Admin → Delivery Service (8087)
   └── Rider assign

7. Rider → Delivery Service
   └── Pickup → Deliver → Complete
```

---

## Key Design Patterns Used

| Pattern | Kahan Use Hua | Kyun |
|---------|---------------|------|
| **API Gateway** | api-gateway | Single entry point, cross-cutting concerns |
| **Service Registry** | Eureka | Dynamic service discovery |
| **Config Server** | config-server | Externalized configuration |
| **Circuit Breaker** | Order→Payment | Fault tolerance |
| **Cache Aside** | Menu, Restaurant, Cart | Performance improvement |
| **JWT Blacklist** | User Auth | Stateless logout |
| **Saga Pattern** | Order flow | Distributed transactions |

---

## Database Design

Har service ka apna database hai (Database per Service pattern):

| Service | Database Name | Main Tables |
|---------|--------------|-------------|
| User Auth | user_auth_db | users |
| Restaurant | restaurant_db | restaurants |
| Menu | menu_db | menu_items |
| Cart | cart_db | carts, cart_items |
| Order | order_db | orders, order_items |
| Payment | payment_db | payments |
| Delivery | delivery_db | deliveries |
| Admin | admin_db | commission_config, platform_stats |

---

## Interview Questions Ye Project Se

1. **Microservices vs Monolith** - Kyun microservices choose kiya?
2. **Service Discovery** - Eureka kaise kaam karta hai?
3. **API Gateway** - Kyun zaroori hai?
4. **JWT Authentication** - Stateless auth kaise kaam karta hai?
5. **Redis Caching** - Cache invalidation kaise handle kiya?
6. **Circuit Breaker** - Resilience4j kaise use kiya?
7. **Distributed Tracing** - Request trace kaise karte ho?
8. **Config Server** - Centralized config ke benefits?

---

## Project Run Karne Ka Order

```bash
1. Config Server (8089)     ← Pehle config ready ho
2. Service Registry (8761)  ← Phir discovery ready ho
3. All Other Services       ← Ab services start ho
4. API Gateway (8080)       ← Last mein gateway
```

---

## Environment Variables

```properties
# Database
DB_HOST, DB_PORT, DB_USERNAME, DB_PASSWORD

# JWT
JWT_SECRET, JWT_EXPIRATION

# Redis
REDIS_HOST, REDIS_PORT, REDIS_PASSWORD
```
