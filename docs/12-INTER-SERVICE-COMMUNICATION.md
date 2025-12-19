# 12. Inter-Service Communication - Complete Guide

## 📡 Overview

Is project mein services ek dusre se communicate karti hain using:
1. **Synchronous Communication** - OpenFeign Client
2. **Service Discovery** - Eureka based routing
3. **Load Balancing** - Client-side load balancing

---

## 🔗 Communication Patterns Used

### 1. OpenFeign Clients

#### Menu Service → Restaurant Service
```java
@FeignClient(name = "restaurant-service")
public interface RestaurantClient {
    
    @GetMapping("/api/restaurants/{id}")
    RestaurantDTO getRestaurantById(@PathVariable("id") Long id);
    
    @GetMapping("/api/restaurants/{id}/exists")
    Boolean checkRestaurantExists(@PathVariable("id") Long id);
}
```

#### Cart Service → Menu Service
```java
@FeignClient(name = "menu-service")
public interface MenuClient {
    
    @GetMapping("/api/menu-items/{id}")
    MenuItemDTO getMenuItemById(@PathVariable("id") Long id);
    
    @GetMapping("/api/menu-items/{id}/price")
    BigDecimal getMenuItemPrice(@PathVariable("id") Long id);
}
```

#### Order Service → Multiple Services
```java
@FeignClient(name = "cart-service")
public interface CartClient {
    @GetMapping("/api/cart/user/{userId}")
    CartDTO getCartByUserId(@PathVariable("userId") Long userId);
    
    @DeleteMapping("/api/cart/user/{userId}/clear")
    void clearCart(@PathVariable("userId") Long userId);
}

@FeignClient(name = "user-auth-service")
public interface UserClient {
    @GetMapping("/api/users/{id}")
    UserDTO getUserById(@PathVariable("id") Long id);
}

@FeignClient(name = "restaurant-service")
public interface RestaurantClient {
    @GetMapping("/api/restaurants/{id}")
    RestaurantDTO getRestaurantById(@PathVariable("id") Long id);
}
```

#### Payment Service → Order Service
```java
@FeignClient(name = "order-service")
public interface OrderClient {
    @GetMapping("/api/orders/{id}")
    OrderDTO getOrderById(@PathVariable("id") Long id);
    
    @PutMapping("/api/orders/{id}/status")
    void updateOrderStatus(@PathVariable("id") Long id, 
                          @RequestParam("status") String status);
}
```

#### Delivery Service → Order & User Service
```java
@FeignClient(name = "order-service")
public interface OrderClient {
    @GetMapping("/api/orders/{id}")
    OrderDTO getOrderById(@PathVariable("id") Long id);
    
    @PutMapping("/api/orders/{id}/status")
    void updateOrderStatus(@PathVariable("id") Long id,
                          @RequestParam("status") String status);
}

@FeignClient(name = "user-auth-service")
public interface UserClient {
    @GetMapping("/api/users/{id}/address")
    AddressDTO getUserAddress(@PathVariable("id") Long id);
}
```

---

## 🔄 Service Dependency Graph

```
┌─────────────────────────────────────────────────────────────────┐
│                        API GATEWAY                               │
│                      (Entry Point)                               │
└─────────────────────────┬───────────────────────────────────────┘
                          │
    ┌─────────────────────┼─────────────────────┐
    │                     │                     │
    ▼                     ▼                     ▼
┌─────────┐        ┌─────────────┐        ┌─────────────┐
│  USER   │        │ RESTAURANT  │        │   ADMIN     │
│ SERVICE │        │  SERVICE    │        │  SERVICE    │
└────┬────┘        └──────┬──────┘        └─────────────┘
     │                    │
     │                    ▼
     │             ┌─────────────┐
     │             │    MENU     │
     │             │  SERVICE    │
     │             └──────┬──────┘
     │                    │
     │                    ▼
     │             ┌─────────────┐
     └────────────►│    CART     │
                   │  SERVICE    │
                   └──────┬──────┘
                          │
                          ▼
                   ┌─────────────┐
                   │   ORDER     │◄────────────┐
                   │  SERVICE    │             │
                   └──────┬──────┘             │
                          │                    │
            ┌─────────────┼─────────────┐      │
            │             │             │      │
            ▼             ▼             ▼      │
     ┌─────────────┐ ┌─────────────┐ ┌─────────────┐
     │  PAYMENT    │ │  DELIVERY   │ │   (User)    │
     │  SERVICE    │ │  SERVICE    │ │  SERVICE    │
     └─────────────┘ └─────────────┘ └─────────────┘
```

---

## ⚙️ Feign Configuration

### Basic Configuration
```yaml
# application.yml
spring:
  cloud:
    openfeign:
      client:
        config:
          default:
            connectTimeout: 5000
            readTimeout: 5000
            loggerLevel: basic
```

### With Circuit Breaker (Resilience4j)
```yaml
spring:
  cloud:
    openfeign:
      circuitbreaker:
        enabled: true

resilience4j:
  circuitbreaker:
    instances:
      restaurant-service:
        registerHealthIndicator: true
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        permittedNumberOfCallsInHalfOpenState: 3
        automaticTransitionFromOpenToHalfOpenEnabled: true
        waitDurationInOpenState: 5s
        failureRateThreshold: 50
```

### Feign with Fallback
```java
@FeignClient(name = "restaurant-service", 
             fallback = RestaurantClientFallback.class)
public interface RestaurantClient {
    @GetMapping("/api/restaurants/{id}")
    RestaurantDTO getRestaurantById(@PathVariable("id") Long id);
}

@Component
public class RestaurantClientFallback implements RestaurantClient {
    @Override
    public RestaurantDTO getRestaurantById(Long id) {
        // Return default/cached response
        return RestaurantDTO.builder()
            .id(id)
            .name("Service Unavailable")
            .build();
    }
}
```

---

## 🔐 Security in Inter-Service Communication

### JWT Token Propagation
```java
@Component
public class FeignClientInterceptor implements RequestInterceptor {
    
    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attributes = 
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String authHeader = request.getHeader("Authorization");
            
            if (authHeader != null) {
                template.header("Authorization", authHeader);
            }
        }
    }
}
```

### Internal Service Authentication
```java
@Configuration
public class FeignConfig {
    
    @Value("${internal.service.secret}")
    private String internalSecret;
    
    @Bean
    public RequestInterceptor internalAuthInterceptor() {
        return template -> {
            template.header("X-Internal-Auth", internalSecret);
        };
    }
}
```

---

## 📊 Interview Questions

### Q1: Synchronous vs Asynchronous Communication?
**Answer:**
| Aspect | Synchronous (Feign) | Asynchronous (Kafka/RabbitMQ) |
|--------|---------------------|-------------------------------|
| Response | Immediate | Eventually |
| Coupling | Tight | Loose |
| Use Case | Real-time data | Event-driven |
| Failure | Cascading possible | Isolated |
| Example | Get restaurant details | Order placed notification |

### Q2: How does Feign Client work internally?
**Answer:**
1. Interface define karte ho with annotations
2. Spring Cloud proxy create karta hai
3. Eureka se service location resolve hoti hai
4. HTTP request banta hai
5. Response deserialize hota hai

### Q3: Circuit Breaker kyu use karte hain?
**Answer:**
- **Problem:** Agar ek service down hai, toh calling service bhi hang ho sakti hai
- **Solution:** Circuit breaker pattern
- **States:** CLOSED → OPEN → HALF_OPEN → CLOSED
- **Benefit:** Fast failure, system stability

### Q4: Load Balancing kaise hoti hai?
**Answer:**
```
Client Request
      │
      ▼
┌─────────────┐
│ Feign Client│
└──────┬──────┘
       │
       ▼
┌─────────────┐
│Load Balancer│ (Spring Cloud LoadBalancer)
└──────┬──────┘
       │
       ├──────► Instance 1 (localhost:8081)
       ├──────► Instance 2 (localhost:8082)
       └──────► Instance 3 (localhost:8083)
```
- Round Robin (default)
- Random
- Weighted

### Q5: Service Discovery ke bina kya problem hoti?
**Answer:**
- Hardcoded URLs maintain karni padti
- Dynamic scaling impossible
- Manual configuration for each environment
- No health checking

---

## 🛠️ Debugging Tips

### Enable Feign Logging
```yaml
logging:
  level:
    com.microServiceTut: DEBUG
    feign: DEBUG
```

### Check Eureka Registration
```bash
curl http://localhost:8761/eureka/apps
```

### Test Individual Service
```bash
# Direct call
curl http://localhost:8082/api/restaurants/1

# Through Gateway
curl http://localhost:8080/restaurant-service/api/restaurants/1
```

---

## ⚠️ Common Issues & Solutions

| Issue | Cause | Solution |
|-------|-------|----------|
| Connection refused | Service not running | Check Eureka dashboard |
| 404 Not Found | Wrong path | Verify @GetMapping path |
| Timeout | Slow response | Increase timeout config |
| Load balancer error | No instances | Check service registration |
| Serialization error | DTO mismatch | Sync DTOs across services |
