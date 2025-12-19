# Config Server (Port: 8089)

## Ye Service Kya Karti Hai?

Config Server **centralized configuration management** provide karta hai. Sab services apni configuration yahan se fetch karti hain instead of apne local application.yml se.

---

## Kyun Zaroori Hai?

**Problem Without Config Server:**
- Har service mein database credentials duplicate
- Config change karne ke liye har service restart
- Environment-specific config manage karna mushkil

**Solution With Config Server:**
- Ek jagah sab config
- Runtime pe config refresh ho sakti hai
- Git-backed config possible (version control)

---

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Config Server (8089)                  │
│                                                          │
│   ┌─────────────────────────────────────────────────┐   │
│   │              config-repo/ folder                 │   │
│   │                                                  │   │
│   │  ├── application.yml      (shared config)       │   │
│   │  ├── user-auth-service.yml                      │   │
│   │  ├── restaurant-service.yml                     │   │
│   │  ├── menu-service.yml                           │   │
│   │  ├── cart-service.yml                           │   │
│   │  ├── order-service.yml                          │   │
│   │  └── ... (other services)                       │   │
│   └─────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
                           │
                           ▼
        ┌──────────────────┼──────────────────┐
        │                  │                  │
        ▼                  ▼                  ▼
   ┌─────────┐       ┌─────────┐       ┌─────────┐
   │ Service │       │ Service │       │ Service │
   │    A    │       │    B    │       │    C    │
   └─────────┘       └─────────┘       └─────────┘
```

---

## Tech Stack

| Technology | Purpose |
|------------|---------|
| Spring Cloud Config Server | Config serving |
| Native Profile | Local file-based config |
| Git Profile (optional) | Git-backed config |

---

## Key Files

### 1. Main Application
```java
@SpringBootApplication
@EnableConfigServer  // ← Ye annotation config server enable karta hai
public class ConfigServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
```

### 2. application.yml
```yaml
server:
  port: 8089

spring:
  application:
    name: CONFIG-SERVER
  profiles:
    active: native  # Local files use karo
  cloud:
    config:
      server:
        native:
          search-locations: file:../config-repo  # Config files yahan hain
```

---

## Config Files Structure (config-repo/)

### application.yml (Shared Config)
```yaml
# Ye config SARI services ko milti hai
spring:
  datasource:
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka
```

### Service-Specific Config (e.g., menu-service.yml)
```yaml
server:
  port: 8084

spring:
  application:
    name: MENU-SERVICE
  datasource:
    url: jdbc:postgresql://${DB_HOST}:${DB_PORT}/${MENU_DB}
  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT}
      password: ${REDIS_PASSWORD}
```

---

## Client Side Configuration

Har service mein ye dependency aur config honi chahiye:

### pom.xml
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-config</artifactId>
</dependency>
```

### application.yml (Client Service)
```yaml
spring:
  application:
    name: MENU-SERVICE  # Isi naam se config fetch hogi
  config:
    import: optional:configserver:http://localhost:8089
```

---

## Config Fetch Flow

```
1. Service Start Hoti Hai
        │
        ▼
2. Config Server ko request: GET /MENU-SERVICE/default
        │
        ▼
3. Config Server check karta hai:
   - config-repo/menu-service.yml (service specific)
   - config-repo/application.yml (shared)
        │
        ▼
4. Merged config return hoti hai
        │
        ▼
5. Service apni config apply karti hai
```

---

## API Endpoints

| Endpoint | Description |
|----------|-------------|
| GET `/{application}/{profile}` | Get config for app |
| GET `/{application}-{profile}.yml` | Get YAML format |
| GET `/actuator/health` | Health check |

**Example:**
```bash
# Menu service ki config fetch karo
curl http://localhost:8089/MENU-SERVICE/default

# YAML format mein
curl http://localhost:8089/menu-service.yml
```

---

## Interview Questions

**Q: Config Server kyun use kiya?**
A: Centralized configuration ke liye. Ek jagah se sab services ki config manage hoti hai. Environment change karna easy hai.

**Q: Native vs Git profile mein kya difference hai?**
A: Native local files se config serve karta hai. Git profile remote Git repository se config fetch karta hai - production ke liye better hai.

**Q: Config refresh kaise karte ho?**
A: `/actuator/refresh` endpoint call karke ya Spring Cloud Bus use karke.

**Q: Agar Config Server down ho jaye toh?**
A: Services apni cached config use karti hain. `optional:` prefix lagane se service fail nahi hoti.

---

## Best Practices

1. **Sensitive data** - Environment variables use karo, hardcode mat karo
2. **Profiles** - dev, staging, prod ke liye alag configs
3. **Encryption** - Secrets encrypt karo
4. **Health check** - Config server ki health monitor karo
5. **Fallback** - `optional:` prefix use karo
