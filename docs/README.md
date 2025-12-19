# 📚 Food Delivery Microservices - Interview Documentation

## 🎯 Purpose
Ye documentation interview preparation ke liye banai gayi hai. Isme project ka complete technical overview, design decisions, aur commonly asked interview questions covered hain.

---

## 📑 Table of Contents

### 🏗️ Architecture & Overview
| # | Document | Description |
|---|----------|-------------|
| 00 | [Project Overview](./00-PROJECT-OVERVIEW.md) | High-level architecture, tech stack, service overview |
| 01 | [Config Server](./01-CONFIG-SERVER.md) | Centralized configuration management |
| 02 | [Service Registry](./02-SERVICE-REGISTRY.md) | Eureka service discovery |
| 03 | [API Gateway](./03-API-GATEWAY.md) | Spring Cloud Gateway, routing, filters |

### 🔧 Business Services
| # | Document | Description |
|---|----------|-------------|
| 04 | [User Auth Service](./04-USER-AUTH-SERVICE.md) | Authentication, JWT, user management |
| 05 | [Restaurant Service](./05-RESTAURANT-SERVICE.md) | Restaurant CRUD, search, ratings |
| 06 | [Menu Service](./06-MENU-SERVICE.md) | Menu items, categories, pricing |
| 07 | [Cart Service](./07-CART-SERVICE.md) | Shopping cart management |
| 08 | [Order Service](./08-ORDER-SERVICE.md) | Order processing, status management |
| 09 | [Payment Service](./09-PAYMENT-SERVICE.md) | Payment processing, refunds |
| 10 | [Delivery Service](./10-DELIVERY-SERVICE.md) | Delivery assignment, tracking |
| 11 | [Admin Service](./11-ADMIN-SERVICE.md) | Admin operations, analytics |
| 12 | [Notification Service](./12-NOTIFICATION-SERVICE.md) | Event-driven notifications (Kafka) |

### 🔗 Technical Deep Dives
| # | Document | Description |
|---|----------|-------------|
| 13 | [Inter-Service Communication](./12-INTER-SERVICE-COMMUNICATION.md) | Feign clients, service calls |
| 13 | [Database Design](./13-DATABASE-DESIGN.md) | Schema design, relationships |
| 14 | [Security Implementation](./14-SECURITY-IMPLEMENTATION.md) | JWT, Spring Security, RBAC |
| 15 | [Exception Handling](./15-EXCEPTION-HANDLING.md) | Global exception handling |
| 16 | [DTO & Mapper Patterns](./16-DTO-MAPPER-PATTERNS.md) | Data transfer objects, MapStruct |


### 📝 Interview Preparation
| # | Document | Description |
|---|----------|-------------|
| 18 | [Interview Q&A](./18-INTERVIEW-QA.md) | Common interview questions with answers |
| 19 | [Design Patterns](./19-COMMON-DESIGN-PATTERNS.md) | Patterns used in the project |
| 20 | [Project Setup Guide](./20-PROJECT-SETUP-GUIDE.md) | How to run the project |

### 📖 Additional Resources
| Document | Description |
|----------|-------------|
| [Config Server Guide](./CONFIG-SERVER-GUIDE.md) | Detailed config server setup |
| [Deployment Guide](./DEPLOYMENT-GUIDE.md) | Production deployment |
| [Testing Guide](./testing.md) | Testing strategies |

---

## 🚀 Quick Start

```bash
# 1. Clone repository
git clone <repo-url>

# 2. Setup databases
mysql -u root -p < scripts/init-db.sql

# 3. Start services in order
cd config-server && mvn spring-boot:run
cd service-registry && mvn spring-boot:run
cd api-gateway && mvn spring-boot:run
# ... start other services
```

See [Project Setup Guide](./20-PROJECT-SETUP-GUIDE.md) for detailed instructions.

---

## 🏛️ Architecture Diagram

```
                                    ┌─────────────────┐
                                    │  Config Server  │
                                    │     (8888)      │
                                    └────────┬────────┘
                                             │
                                    ┌────────▼────────┐
                                    │Service Registry │
                                    │  Eureka (8761)  │
                                    └────────┬────────┘
                                             │
┌──────────┐                        ┌────────▼────────┐
│  Client  │───────────────────────>│   API Gateway   │
│  (Web/   │                        │     (8080)      │
│  Mobile) │<───────────────────────│                 │
└──────────┘                        └────────┬────────┘
                                             │
        ┌────────────────────────────────────┼────────────────────────────────────┐
        │                                    │                                    │
        ▼                                    ▼                                    ▼
┌───────────────┐                   ┌───────────────┐                   ┌───────────────┐
│  User Auth    │                   │  Restaurant   │                   │    Menu       │
│   Service     │                   │   Service     │                   │   Service     │
│    (8081)     │                   │    (8082)     │                   │    (8083)     │
└───────────────┘                   └───────────────┘                   └───────────────┘
        │                                    │                                    │
        ▼                                    ▼                                    ▼
┌───────────────┐                   ┌───────────────┐                   ┌───────────────┐
│    Cart       │                   │    Order      │                   │   Payment     │
│   Service     │                   │   Service     │                   │   Service     │
│    (8084)     │                   │    (8085)     │                   │    (8086)     │
└───────────────┘                   └───────────────┘                   └───────────────┘
                                             │
                        ┌────────────────────┼────────────────────┐
                        ▼                                        ▼
               ┌───────────────┐                        ┌───────────────┐
               │   Delivery    │                        │    Admin      │
               │   Service     │                        │   Service     │
               │    (8087)     │                        │    (8088)     │
               └───────────────┘                        └───────────────┘
```

---

## 🛠️ Tech Stack Summary

| Category | Technology |
|----------|------------|
| Language | Java 17 |
| Framework | Spring Boot 3.x |
| Cloud | Spring Cloud 2023.x |
| Database | MySQL 8.0 |
| Security | Spring Security + JWT |
| Build | Maven |
| Containerization | Docker |

---

## 📊 Key Concepts Covered

### Microservices Patterns
- ✅ Service Discovery (Eureka)
- ✅ API Gateway Pattern
- ✅ Config Server (Externalized Configuration)
- ✅ Circuit Breaker (Resilience4j)
- ✅ Client-side Load Balancing
- ✅ Database per Service
- ✅ Event-Driven Architecture (Kafka)

### Design Patterns
- ✅ Builder Pattern
- ✅ Factory Pattern
- ✅ Strategy Pattern
- ✅ Observer Pattern (Events)
- ✅ Repository Pattern
- ✅ DTO Pattern

### Security
- ✅ JWT Authentication
- ✅ Role-Based Access Control
- ✅ Method-level Security
- ✅ Password Encryption (BCrypt)

### Best Practices
- ✅ Exception Handling
- ✅ Logging & Monitoring
- ✅ API Documentation
- ✅ Input Validation

---

## 💡 Interview Tips

1. **Architecture samjho** - Har service ka purpose aur dependencies
2. **Trade-offs discuss karo** - Microservices vs Monolith pros/cons
3. **Real examples do** - Project se specific examples
4. **Diagrams draw karo** - Visual explanation powerful hai
5. **Code explain karo** - Key implementations samjhao

---

## 📞 Quick Reference

### Service URLs (Local)
```
Config Server:     http://localhost:8888
Eureka Dashboard:  http://localhost:8761
API Gateway:       http://localhost:8080
```

### Common API Endpoints
```
POST /api/auth/register    - User registration
POST /api/auth/login       - User login
GET  /api/restaurants      - List restaurants
GET  /api/menu-items       - Get menu
POST /api/cart/items       - Add to cart
POST /api/orders           - Place order
POST /api/payments         - Make payment
```

---

**Good luck with your interview! 🎯**
