# Spring Cloud Config Server Guide

## Overview

Config Server centralized configuration provide karta hai sabhi microservices ke liye. Ye development mein local files use karta hai aur production mein Git repository.

---

## Configuration Types

### 1. GLOBAL Configuration (`config-repo/application.yml`)

Ye configuration **SABHI SERVICES** pe apply hoti hai automatically.

| Property | Description |
|----------|-------------|
| Database HikariCP settings | Connection pool size, timeouts |
| JPA/Hibernate settings | DDL auto, dialect, SQL logging |
| Eureka client settings | Service discovery registration |
| Actuator endpoints | Health, info exposure |
| Common logging levels | Root, Hibernate logging |

```yaml
# Shared across ALL services
spring:
  datasource:
    hikari:
      maximum-pool-size: 10
  jpa:
    hibernate:
      ddl-auto: update
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka
```

---

### 2. SERVICE-SPECIFIC Configuration

Har service ki apni config file hai jo **sirf usi service** pe apply hoti hai.

| File | Service | Port | Database |
|------|---------|------|----------|
| `user-auth-service.yml` | USER-AUTH-SERVICE | 8086 | user_auth_db |
| `restaurant-service.yml` | RESTAURANT-SERVICE | 8083 | restaurant_db |
| `menu-service.yml` | MENU-SERVICE | 8084 | menu_db |
| `cart-service.yml` | CART-SERVICE | 8085 | cart_db |
| `order-service.yml` | ORDER-SERVICE | 8081 | order_db |
| `payment-service.yml` | PAYMENT-SERVICE | 8082 | payment_db |
| `delivery-service.yml` | DELIVERY-SERVICE | 8087 | delivery_db |
| `admin-service.yml` | ADMIN-SERVICE | 8088 | admin_db |
| `api-gateway.yml` | API-GATEWAY | 8080 | - |
| `service-registry.yml` | SERVICE-REGISTRY | 8761 | - |

---

## Config Priority (Override Order)

```
1. Service-specific config (highest priority)
   ↓
2. application.yml (global/shared)
   ↓
3. Service's local application.yaml (lowest priority)
```

**Example:** Agar `order-service.yml` mein `server.port=8081` hai aur `application.yml` mein `server.port=9000` hai, toh **8081** use hoga.

---

## Folder Structure

```
config-repo/
├── application.yml           # GLOBAL - All services
├── api-gateway.yml           # API Gateway specific
├── order-service.yml         # Order Service specific
├── payment-service.yml       # Payment Service specific
├── menu-service.yml          # Menu Service specific
├── restaurant-service.yml    # Restaurant Service specific
├── cart-service.yml          # Cart Service specific
├── user-auth-service.yml     # User Auth Service specific
├── delivery-service.yml      # Delivery Service specific
├── admin-service.yml         # Admin Service specific
└── service-registry.yml      # Eureka Server specific
```

---

## Service Port Mapping

| Service | Port | Description |
|---------|------|-------------|
| SERVICE-REGISTRY | 8761 | Eureka Server |
| API-GATEWAY | 8080 | Entry point for all requests |
| ORDER-SERVICE | 8081 | Order management |
| PAYMENT-SERVICE | 8082 | Payment processing |
| RESTAURANT-SERVICE | 8083 | Restaurant CRUD |
| MENU-SERVICE | 8084 | Menu items management |
| CART-SERVICE | 8085 | Shopping cart |
| USER-AUTH-SERVICE | 8086 | Authentication & JWT |
| DELIVERY-SERVICE | 8087 | Delivery tracking |
| ADMIN-SERVICE | 8088 | Admin dashboard |
| CONFIG-SERVER | 8089 | Configuration server |

---

## Testing Config Server

### 1. Start Config Server
```bash
cd config-server
mvnw spring-boot:run
```

### 2. Test Endpoints

| URL | Description |
|-----|-------------|
| `http://localhost:8089/application/default` | Global config |
| `http://localhost:8089/order-service/default` | Order service config |
| `http://localhost:8089/user-auth-service/default` | User auth config |
| `http://localhost:8089/api-gateway/default` | API Gateway config |

### 3. Response Format
```json
{
  "name": "order-service",
  "profiles": ["default"],
  "propertySources": [
    {
      "name": "file:./config-repo/order-service.yml",
      "source": {
        "server.port": 8081,
        "spring.datasource.url": "jdbc:postgresql://..."
      }
    },
    {
      "name": "file:./config-repo/application.yml",
      "source": {
        "spring.jpa.hibernate.ddl-auto": "update"
      }
    }
  ]
}
```

---

## Environment Profiles

### Development (Native/Local)
```yaml
spring:
  profiles:
    active: native
  cloud:
    config:
      server:
        native:
          search-locations: file:./config-repo
```

### Production (Git)
```yaml
spring:
  profiles:
    active: git
  cloud:
    config:
      server:
        git:
          uri: https://github.com/your-org/config-repo.git
          default-label: main
```

---

## Client Service Integration

Har service ko config server se connect karne ke liye `bootstrap.yml` add karo:

```yaml
# src/main/resources/bootstrap.yml
spring:
  application:
    name: ORDER-SERVICE  # Must match config file name
  cloud:
    config:
      uri: http://localhost:8089
      fail-fast: true
```

**Dependencies required:**
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-config</artifactId>
</dependency>
```

---

## Quick Reference

| What | Where | Scope |
|------|-------|-------|
| Database pool settings | `application.yml` | Global |
| Service port | `{service-name}.yml` | Service-specific |
| Database URL | `{service-name}.yml` | Service-specific |
| Eureka settings | `application.yml` | Global |
| JWT secret | `user-auth-service.yml` | Service-specific |
| Gateway routes | `api-gateway.yml` | Service-specific |
| Resilience4j | `order-service.yml` | Service-specific |

---

## Startup Order

1. **SERVICE-REGISTRY** (8761) - First
2. **CONFIG-SERVER** (8089) - Second
3. **All other services** - After config server is ready
