# 18. Interview Questions & Answers - Complete Guide

## 📋 Table of Contents
1. [Microservices Architecture](#microservices-architecture)
2. [Spring Boot & Spring Cloud](#spring-boot--spring-cloud)
3. [Service Discovery & API Gateway](#service-discovery--api-gateway)
4. [Inter-Service Communication](#inter-service-communication)
5. [Database & Data Management](#database--data-management)
6. [Security](#security)
7. [Resilience & Fault Tolerance](#resilience--fault-tolerance)
8. [DevOps & Deployment](#devops--deployment)
9. [Project-Specific Questions](#project-specific-questions)
10. [Scenario-Based Questions](#scenario-based-questions)

---

## 🏗️ Microservices Architecture

### Q1: Microservices kya hain? Monolith se kaise different hain?

**Answer:**
```
Monolith:
┌─────────────────────────────────────┐
│           Single Application         │
│  ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐   │
│  │User │ │Order│ │Menu │ │Pay  │   │
│  └─────┘ └─────┘ └─────┘ └─────┘   │
│         Single Database              │
│         Single Deployment            │
└─────────────────────────────────────┘

Microservices:
┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐
│  User   │ │  Order  │ │  Menu   │ │ Payment │
│ Service │ │ Service │ │ Service │ │ Service │
│   DB    │ │   DB    │ │   DB    │ │   DB    │
└─────────┘ └─────────┘ └─────────┘ └─────────┘
```

| Aspect | Monolith | Microservices |
|--------|----------|---------------|
| Deployment | Single unit | Independent |
| Scaling | Entire app | Individual service |
| Technology | Single stack | Polyglot |
| Team | Large team | Small teams |
| Failure | Entire app down | Isolated failure |
| Development | Slower | Faster iterations |

---

### Q2: Microservices ke advantages aur disadvantages?

**Advantages:**
1. **Independent Deployment** - Ek service update karne se doosri affect nahi hoti
2. **Technology Freedom** - Har service different tech use kar sakti hai
3. **Scalability** - Sirf busy services scale karo
4. **Fault Isolation** - Ek service fail hone se poora system down nahi hota
5. **Team Autonomy** - Small teams independently kaam kar sakti hain

**Disadvantages:**
1. **Complexity** - Distributed system manage karna mushkil
2. **Network Latency** - Service-to-service calls slow
3. **Data Consistency** - Distributed transactions complex
4. **Testing** - Integration testing challenging
5. **Operational Overhead** - Multiple services monitor karna

---

### Q3: Service decomposition kaise karte ho?

**Answer:**
1. **Domain-Driven Design (DDD):**
   - Bounded contexts identify karo
   - Each context = potential microservice
   
2. **Business Capabilities:**
   - User Management
   - Order Processing
   - Payment Handling
   - Delivery Management

3. **Single Responsibility:**
   - Ek service = ek business function

**Is Project mein:**
```
Food Delivery Domain
├── User Context → User Auth Service
├── Restaurant Context → Restaurant Service
├── Menu Context → Menu Service
├── Cart Context → Cart Service
├── Order Context → Order Service
├── Payment Context → Payment Service
├── Delivery Context → Delivery Service
└── Admin Context → Admin Service
```

---

## 🍃 Spring Boot & Spring Cloud

### Q4: Spring Boot kya hai? Features batao.

**Answer:**
Spring Boot = Spring framework ka opinionated version jo production-ready applications quickly banane mein help karta hai.

**Key Features:**
1. **Auto-configuration** - Automatic bean configuration
2. **Starter Dependencies** - Pre-configured dependencies
3. **Embedded Server** - Tomcat/Jetty built-in
4. **Actuator** - Production-ready features (health, metrics)
5. **No XML** - Java-based configuration

```java
// Traditional Spring
@Configuration
@EnableWebMvc
@ComponentScan
public class AppConfig {
    @Bean
    public DataSource dataSource() { ... }
    @Bean
    public EntityManagerFactory entityManagerFactory() { ... }
    // 50+ more beans...
}

// Spring Boot
@SpringBootApplication  // That's it!
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

---

### Q5: Spring Cloud components explain karo.

**Answer:**
```
┌─────────────────────────────────────────────────────────────────┐
│                      SPRING CLOUD ECOSYSTEM                      │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────────────┐    ┌─────────────────┐                     │
│  │  Config Server  │    │ Service Registry│                     │
│  │ (Centralized    │    │    (Eureka)     │                     │
│  │  Configuration) │    │                 │                     │
│  └─────────────────┘    └─────────────────┘                     │
│                                                                  │
│  ┌─────────────────┐    ┌─────────────────┐                     │
│  │   API Gateway   │    │  Load Balancer  │                     │
│  │ (Spring Cloud   │    │ (Spring Cloud   │                     │
│  │    Gateway)     │    │  LoadBalancer)  │                     │
│  └─────────────────┘    └─────────────────┘                     │
│                                                                  │
│  ┌─────────────────┐    ┌─────────────────┐                     │
│  │  Circuit Breaker│    │   Feign Client  │                     │
│  │  (Resilience4j) │    │ (Declarative    │                     │
│  │                 │    │  REST Client)   │                     │
│  └─────────────────┘    └─────────────────┘                     │
│                                                                  │
│  ┌─────────────────┐    ┌─────────────────┐                     │
│  │ Distributed     │    │     Sleuth      │                     │
│  │   Tracing       │    │   (Tracing)     │                     │
│  │   (Zipkin)      │    │                 │                     │
│  └─────────────────┘    └─────────────────┘                     │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

### Q6: @SpringBootApplication annotation kya karta hai?

**Answer:**
```java
@SpringBootApplication = @Configuration + @EnableAutoConfiguration + @ComponentScan

@Configuration       // Java-based configuration
@EnableAutoConfiguration  // Auto-configure beans based on classpath
@ComponentScan      // Scan for @Component, @Service, @Repository, @Controller
```

---

## 🔍 Service Discovery & API Gateway

### Q7: Service Discovery kya hai? Eureka kaise kaam karta hai?

**Answer:**
**Problem:** Microservices dynamically scale hoti hain, IP addresses change hote hain.

**Solution:** Service Registry (Eureka)

```
┌─────────────────────────────────────────────────────────────────┐
│                        EUREKA SERVER                             │
│                    (Service Registry)                            │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  Service Name    │  Instances                            │    │
│  ├───────────────────────────────────────────────────────────    │
│  │  order-service   │  192.168.1.10:8085, 192.168.1.11:8085 │    │
│  │  user-service    │  192.168.1.20:8081                    │    │
│  │  menu-service    │  192.168.1.30:8083, 192.168.1.31:8083 │    │
│  └─────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘

Flow:
1. Service starts → Registers with Eureka
2. Client needs service → Asks Eureka for address
3. Eureka returns available instances
4. Client calls service directly
5. Heartbeat every 30 seconds
6. No heartbeat for 90 seconds → Instance removed
```

---

### Q8: API Gateway kya hai? Kyu use karte hain?

**Answer:**
**API Gateway = Single entry point for all client requests**

```
Without Gateway:
Client → User Service (8081)
Client → Order Service (8085)
Client → Menu Service (8083)
(Client ko sab URLs pata hone chahiye)

With Gateway:
Client → API Gateway (8080) → Routes to appropriate service
(Client sirf gateway URL jaanta hai)
```

**Responsibilities:**
1. **Routing** - Request ko correct service pe bhejo
2. **Load Balancing** - Multiple instances mein distribute karo
3. **Authentication** - JWT validate karo
4. **Rate Limiting** - Too many requests block karo
5. **Logging** - Request/Response log karo
6. **CORS** - Cross-origin requests handle karo

---

### Q9: Client-side vs Server-side Load Balancing?

**Answer:**
```
Server-side (Traditional):
┌────────┐     ┌──────────────┐     ┌──────────┐
│ Client │────>│ Load Balancer│────>│ Service  │
└────────┘     │   (Nginx)    │     │ Instance │
               └──────────────┘     └──────────┘

Client-side (Spring Cloud):
┌────────────────────────────┐     ┌──────────┐
│         Client             │     │ Service  │
│  ┌──────────────────────┐  │────>│ Instance │
│  │ Load Balancer        │  │     └──────────┘
│  │ (Spring Cloud LB)    │  │     ┌──────────┐
│  │ - Gets instances     │  │────>│ Service  │
│  │   from Eureka        │  │     │ Instance │
│  │ - Chooses one        │  │     └──────────┘
│  └──────────────────────┘  │
└────────────────────────────┘
```

| Server-side | Client-side |
|-------------|-------------|
| Centralized | Distributed |
| Single point of failure | No SPOF |
| Extra hop | Direct call |
| Hardware/Software LB | Library-based |

---

## 🔗 Inter-Service Communication

### Q10: Synchronous vs Asynchronous Communication?

**Answer:**
```
Synchronous (Feign/REST):
┌────────┐  Request   ┌────────┐
│Order   │───────────>│ User   │
│Service │            │Service │
│        │<───────────│        │
└────────┘  Response  └────────┘
(Waits for response)

Asynchronous (Kafka/RabbitMQ):
┌────────┐  Publish   ┌─────────┐  Subscribe  ┌────────┐
│Order   │───────────>│ Message │<────────────│Delivery│
│Service │            │  Queue  │             │Service │
└────────┘            └─────────┘             └────────┘
(Fire and forget)
```

| Synchronous | Asynchronous |
|-------------|--------------|
| Immediate response | Eventually |
| Tight coupling | Loose coupling |
| Simple | Complex |
| Cascading failures | Isolated |
| REST/gRPC | Kafka/RabbitMQ |

---

### Q11: Feign Client kaise kaam karta hai?

**Answer:**
```java
// 1. Define interface
@FeignClient(name = "restaurant-service")
public interface RestaurantClient {
    @GetMapping("/api/restaurants/{id}")
    RestaurantDTO getRestaurantById(@PathVariable("id") Long id);
}

// 2. Spring creates proxy at runtime
// 3. When called:
//    - Looks up "restaurant-service" in Eureka
//    - Gets available instances
//    - Load balances (Round Robin)
//    - Makes HTTP call
//    - Deserializes response

// 4. Usage
@Service
public class OrderService {
    @Autowired
    private RestaurantClient restaurantClient;
    
    public void createOrder(Long restaurantId) {
        RestaurantDTO restaurant = restaurantClient.getRestaurantById(restaurantId);
        // Use restaurant data
    }
}
```

---

### Q12: Circuit Breaker Pattern explain karo.

**Answer:**
**Problem:** Agar downstream service slow/down hai, toh calling service bhi hang ho jayegi.

**Solution:** Circuit Breaker

```
States:
┌─────────┐     Failures > Threshold    ┌─────────┐
│ CLOSED  │ ─────────────────────────> │  OPEN   │
│(Normal) │                             │ (Fail   │
└─────────┘                             │  Fast)  │
     ▲                                  └────┬────┘
     │                                       │
     │      Success                    Wait timeout
     │                                       │
     │         ┌─────────────┐               │
     └─────────│ HALF-OPEN   │<──────────────┘
               │(Test calls) │
               └─────────────┘
```

```java
@FeignClient(name = "restaurant-service", fallback = RestaurantFallback.class)
public interface RestaurantClient {
    @GetMapping("/api/restaurants/{id}")
    RestaurantDTO getRestaurantById(@PathVariable Long id);
}

@Component
public class RestaurantFallback implements RestaurantClient {
    @Override
    public RestaurantDTO getRestaurantById(Long id) {
        // Return cached/default response
        return RestaurantDTO.builder()
            .id(id)
            .name("Service Unavailable")
            .build();
    }
}
```

---

## 💾 Database & Data Management

### Q13: Database per Service pattern kya hai?

**Answer:**
```
┌─────────────┐  ┌─────────────┐  ┌─────────────┐
│ User Service│  │Order Service│  │Menu Service │
└──────┬──────┘  └──────┬──────┘  └──────┬──────┘
       │                │                │
       ▼                ▼                ▼
┌─────────────┐  ┌─────────────┐  ┌─────────────┐
│  User DB    │  │  Order DB   │  │  Menu DB    │
│  (MySQL)    │  │  (MySQL)    │  │  (MongoDB)  │
└─────────────┘  └─────────────┘  └─────────────┘
```

**Benefits:**
- Loose coupling
- Independent scaling
- Technology freedom
- Failure isolation

**Challenges:**
- Data consistency
- Cross-service queries
- Distributed transactions

---

### Q14: Distributed Transactions kaise handle karte ho?

**Answer:**
**Saga Pattern:**
```
Order Creation Saga:

Step 1: Create Order (Order Service)
    ↓ Success
Step 2: Reserve Items (Inventory Service)
    ↓ Success
Step 3: Process Payment (Payment Service)
    ↓ Failure!
Step 4: Compensate - Refund Payment
Step 5: Compensate - Release Items
Step 6: Compensate - Cancel Order
```

**Types:**
1. **Choreography:** Services listen to events and react
2. **Orchestration:** Central coordinator manages saga

---

### Q15: CQRS Pattern kya hai?

**Answer:**
**CQRS = Command Query Responsibility Segregation**

```
Traditional:
┌─────────────────────────────────┐
│           Service               │
│  Read + Write → Same Model      │
│  Same Database                  │
└─────────────────────────────────┘

CQRS:
┌─────────────────┐  ┌─────────────────┐
│  Command Side   │  │   Query Side    │
│  (Write Model)  │  │  (Read Model)   │
│  - Create       │  │  - Optimized    │
│  - Update       │  │    for reads    │
│  - Delete       │  │  - Denormalized │
└────────┬────────┘  └────────┬────────┘
         │                    │
         ▼                    ▼
┌─────────────────┐  ┌─────────────────┐
│   Write DB      │  │    Read DB      │
│   (MySQL)       │  │  (Elasticsearch)│
└─────────────────┘  └─────────────────┘
```

---

## 🔐 Security

### Q16: JWT Authentication flow explain karo.

**Answer:**
```
1. Login Request:
   Client → POST /api/auth/login {email, password}
   
2. Server validates credentials
   
3. Server generates JWT:
   Header: {"alg": "HS256", "typ": "JWT"}
   Payload: {"sub": "user@email.com", "roles": ["CUSTOMER"], "exp": 1703088000}
   Signature: HMACSHA256(header + payload, secret)
   
4. Server returns token:
   {accessToken: "eyJhbG...", refreshToken: "eyJhbG..."}
   
5. Client stores token (localStorage/cookie)

6. Subsequent requests:
   Client → GET /api/orders
   Header: Authorization: Bearer eyJhbG...
   
7. Server validates token:
   - Check signature
   - Check expiry
   - Extract user info
   
8. If valid → Process request
   If invalid → 401 Unauthorized
```

---

### Q17: OAuth2 vs JWT?

**Answer:**
| OAuth2 | JWT |
|--------|-----|
| Authorization framework | Token format |
| Defines flows (Auth Code, Client Credentials) | Defines token structure |
| Can use JWT as token | Self-contained token |
| Third-party authorization | Direct authentication |
| Google/Facebook login | Custom auth |

**OAuth2 + JWT = Best of both worlds**

---

### Q18: Role-Based Access Control (RBAC) kaise implement kiya?

**Answer:**
```java
// 1. Define Roles
public enum UserRole {
    CUSTOMER,
    RESTAURANT_OWNER,
    DELIVERY_PARTNER,
    ADMIN
}

// 2. Store role in JWT
claims.put("roles", user.getRoles());

// 3. Configure Security
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/admin/**").hasRole("ADMIN")
    .requestMatchers("/api/restaurant/**").hasRole("RESTAURANT_OWNER")
    .anyRequest().authenticated()
)

// 4. Method-level security
@PreAuthorize("hasRole('ADMIN')")
public void deleteUser(Long id) { }

@PreAuthorize("hasRole('RESTAURANT_OWNER') and @restaurantSecurity.isOwner(#id)")
public void updateRestaurant(Long id) { }
```

---

## 🛡️ Resilience & Fault Tolerance

### Q19: Retry Pattern kaise implement karte ho?

**Answer:**
```java
// Using Resilience4j
@Retry(name = "restaurantService", fallbackMethod = "getRestaurantFallback")
public RestaurantDTO getRestaurant(Long id) {
    return restaurantClient.getRestaurantById(id);
}

public RestaurantDTO getRestaurantFallback(Long id, Exception e) {
    return getCachedRestaurant(id);
}

// Configuration
resilience4j:
  retry:
    instances:
      restaurantService:
        maxAttempts: 3
        waitDuration: 1s
        retryExceptions:
          - java.io.IOException
          - feign.FeignException.ServiceUnavailable
```

---

### Q20: Rate Limiting kyu aur kaise?

**Answer:**
**Why:** DDoS protection, fair usage, resource protection

```java
// API Gateway rate limiting
spring:
  cloud:
    gateway:
      routes:
        - id: order-service
          uri: lb://order-service
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 10
                redis-rate-limiter.burstCapacity: 20
                key-resolver: "#{@userKeyResolver}"

@Bean
public KeyResolver userKeyResolver() {
    return exchange -> Mono.just(
        exchange.getRequest().getHeaders().getFirst("X-User-Id")
    );
}
```

---

## 🚀 DevOps & Deployment

### Q21: Docker kya hai? Dockerfile explain karo.

**Answer:**
```dockerfile
# Multi-stage build
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Environment variables
ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

---

### Q22: Docker Compose vs Kubernetes?

**Answer:**
| Docker Compose | Kubernetes |
|----------------|------------|
| Single host | Multi-host cluster |
| Development/Testing | Production |
| Simple orchestration | Advanced orchestration |
| docker-compose.yml | Deployments, Services, Pods |
| Manual scaling | Auto-scaling |
| No self-healing | Self-healing |

---

### Q23: CI/CD Pipeline explain karo.

**Answer:**
```
┌─────────┐    ┌─────────┐    ┌─────────┐    ┌─────────┐    ┌─────────┐
│  Code   │───>│  Build  │───>│  Test   │───>│ Deploy  │───>│ Monitor │
│  Push   │    │         │    │         │    │         │    │         │
└─────────┘    └─────────┘    └─────────┘    └─────────┘    └─────────┘
     │              │              │              │              │
     ▼              ▼              ▼              ▼              ▼
  GitHub        Maven          JUnit         Docker         Prometheus
  Commit        Build          Tests         Push           Grafana
                               Sonar         K8s Deploy
```

---

## 🍕 Project-Specific Questions

### Q24: Is project mein services ka flow explain karo.

**Answer:**
```
Customer Order Flow:

1. Customer Login
   Client → API Gateway → User Auth Service
   ← JWT Token

2. Browse Restaurants
   Client → API Gateway → Restaurant Service
   ← Restaurant List

3. View Menu
   Client → API Gateway → Menu Service → Restaurant Service (validate)
   ← Menu Items

4. Add to Cart
   Client → API Gateway → Cart Service → Menu Service (get price)
   ← Updated Cart

5. Place Order
   Client → API Gateway → Order Service
   Order Service → Cart Service (get cart)
   Order Service → User Service (get address)
   Order Service → Restaurant Service (validate)
   ← Order Created

6. Make Payment
   Client → API Gateway → Payment Service → Order Service (update status)
   ← Payment Confirmed

7. Delivery Assignment
   Delivery Service → Order Service (get order)
   Delivery Service → User Service (get address)
   ← Delivery Assigned

8. Track Order
   Client → API Gateway → Order Service
   ← Order Status
```

---

### Q25: Agar ek service down ho jaye toh kya hoga?

**Answer:**
```
Scenario: Restaurant Service Down

1. Menu Service calls Restaurant Service
   ↓
2. Feign Client timeout (5 seconds)
   ↓
3. Circuit Breaker opens (after 5 failures)
   ↓
4. Fallback method called
   - Return cached data
   - Return default response
   - Throw user-friendly error
   ↓
5. Circuit stays open for 30 seconds
   ↓
6. Half-open state - test calls
   ↓
7. If successful → Circuit closes
   If fails → Circuit stays open
```

---

## 🎯 Scenario-Based Questions

### Q26: High traffic handle kaise karoge?

**Answer:**
1. **Horizontal Scaling:** Multiple instances of busy services
2. **Caching:** Redis for frequently accessed data
3. **Database Optimization:** Read replicas, connection pooling
4. **Async Processing:** Queue heavy operations
5. **CDN:** Static content delivery
6. **Rate Limiting:** Protect from abuse

---

### Q27: Data consistency kaise ensure karoge across services?

**Answer:**
1. **Eventual Consistency:** Accept that data won't be immediately consistent
2. **Saga Pattern:** Compensating transactions for failures
3. **Event Sourcing:** Store events, not just current state
4. **Idempotency:** Same request = same result
5. **Distributed Locks:** For critical sections

---

### Q28: Service versioning kaise karoge?

**Answer:**
```
Option 1: URL Versioning
/api/v1/orders
/api/v2/orders

Option 2: Header Versioning
Accept: application/vnd.myapp.v1+json

Option 3: Query Parameter
/api/orders?version=1

Best Practice:
- Support multiple versions simultaneously
- Deprecation notice before removing old version
- Backward compatible changes when possible
```

---

### Q29: Debugging kaise karoge distributed system mein?

**Answer:**
1. **Distributed Tracing:** Zipkin/Jaeger - trace request across services
2. **Centralized Logging:** ELK Stack - all logs in one place
3. **Correlation ID:** Same ID across all service logs
4. **Health Endpoints:** /actuator/health for each service
5. **Metrics:** Prometheus + Grafana dashboards

---

### Q30: Production mein koi issue aaye toh approach kya hoga?

**Answer:**
```
1. Identify
   - Check alerts (Prometheus/Grafana)
   - Check error logs (Kibana)
   - Check traces (Zipkin)

2. Isolate
   - Which service affected?
   - Which endpoint?
   - Since when?

3. Mitigate
   - Scale up if load issue
   - Rollback if recent deployment
   - Enable circuit breaker if downstream issue

4. Fix
   - Root cause analysis
   - Code fix
   - Test thoroughly

5. Deploy
   - Canary deployment
   - Monitor closely

6. Post-mortem
   - Document incident
   - Preventive measures
   - Update runbooks
```

---

## 💡 Tips for Interview

1. **Project ko deeply samjho** - Har service ka purpose, flow, dependencies
2. **Trade-offs discuss karo** - Koi bhi decision ke pros/cons batao
3. **Real examples do** - "Humne ye problem face kiya, ye solution implement kiya"
4. **Diagrams draw karo** - Visual explanation powerful hai
5. **Honest raho** - Agar kuch nahi pata, bol do "I'll need to look into that"
6. **Questions pucho** - Interviewer se clarification lo

---

## 📚 Additional Resources

- Spring Cloud Documentation
- Microservices Patterns by Chris Richardson
- Building Microservices by Sam Newman
- 12-Factor App Methodology
