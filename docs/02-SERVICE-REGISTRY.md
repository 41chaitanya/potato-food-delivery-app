# Service Registry - Eureka Server (Port: 8761)

## Ye Service Kya Karti Hai?

Service Registry ek **phone directory** jaisi hai microservices ke liye. Jab koi service start hoti hai, wo apna address yahan register karti hai. Jab kisi service ko dusri service se baat karni ho, toh yahan se address milta hai.

---

## Kyun Zaroori Hai?

**Problem Without Service Registry:**
```
Order Service ko Payment Service call karni hai
Order Service mein hardcode: http://localhost:8082/api/payments

Agar Payment Service ka port change ho gaya → Code change karna padega
Agar multiple instances hain → Load balancing kaise?
```

**Solution With Service Registry:**
```
Order Service: "Mujhe PAYMENT-SERVICE chahiye"
Eureka: "Ye lo address: 192.168.1.10:8082, 192.168.1.11:8082"
Order Service: Load balance karke call karta hai
```

---

## Architecture

```
                    ┌─────────────────────────────┐
                    │     Eureka Server (8761)    │
                    │                             │
                    │  Registry:                  │
                    │  ┌───────────────────────┐  │
                    │  │ USER-AUTH-SERVICE     │  │
                    │  │   → localhost:8086    │  │
                    │  │                       │  │
                    │  │ MENU-SERVICE          │  │
                    │  │   → localhost:8084    │  │
                    │  │   → 192.168.1.5:8084  │  │
                    │  │                       │  │
                    │  │ ORDER-SERVICE         │  │
                    │  │   → localhost:8081    │  │
                    │  └───────────────────────┘  │
                    └─────────────────────────────┘
                                 ▲
                    ┌────────────┼────────────┐
                    │            │            │
              Register      Register     Register
                    │            │            │
               ┌────┴────┐ ┌────┴────┐ ┌────┴────┐
               │ Service │ │ Service │ │ Service │
               │    A    │ │    B    │ │    C    │
               └─────────┘ └─────────┘ └─────────┘
```

---

## Tech Stack

| Technology | Purpose |
|------------|---------|
| Netflix Eureka Server | Service Registry |
| Spring Cloud Netflix | Eureka integration |

---

## Key Files

### 1. Main Application
```java
@SpringBootApplication
@EnableEurekaServer  // ← Ye annotation Eureka Server enable karta hai
public class ServiceRegistryApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceRegistryApplication.class, args);
    }
}
```

### 2. application.yml
```yaml
server:
  port: 8761

spring:
  application:
    name: SERVICE-REGISTRY

eureka:
  client:
    register-with-eureka: false  # Khud ko register mat karo
    fetch-registry: false        # Registry fetch mat karo
  server:
    enable-self-preservation: false  # Development ke liye
```

---

## Client Side Configuration

Har service jo register honi chahiye:

### pom.xml
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

### application.yml
```yaml
eureka:
  client:
    register-with-eureka: true
    fetch-registry: true
    service-url:
      defaultZone: http://localhost:8761/eureka
  instance:
    prefer-ip-address: true
```

---

## Registration Flow

```
1. Service Start Hoti Hai
        │
        ▼
2. Eureka Client apna info collect karta hai:
   - Application Name (MENU-SERVICE)
   - IP Address (192.168.1.5)
   - Port (8084)
   - Health URL (/actuator/health)
        │
        ▼
3. POST request to Eureka Server
   POST /eureka/apps/MENU-SERVICE
        │
        ▼
4. Eureka Server registry mein add karta hai
        │
        ▼
5. Heartbeat har 30 seconds (default)
   - Agar 3 heartbeats miss → Service remove
```

---

## Service Discovery Flow

```
1. Order Service ko Payment Service chahiye
        │
        ▼
2. Eureka Client se poochta hai:
   GET /eureka/apps/PAYMENT-SERVICE
        │
        ▼
3. Eureka Server response:
   {
     "instances": [
       { "host": "192.168.1.10", "port": 8082 },
       { "host": "192.168.1.11", "port": 8082 }
     ]
   }
        │
        ▼
4. Load Balancer (Ribbon/LoadBalancer) ek instance choose karta hai
        │
        ▼
5. Request us instance ko jaati hai
```

---

## Eureka Dashboard

**URL:** http://localhost:8761

Dashboard pe dikhta hai:
- Registered services list
- Instance count
- Status (UP/DOWN)
- Last heartbeat time

```
┌─────────────────────────────────────────────────────────┐
│                 Eureka Dashboard                         │
├─────────────────────────────────────────────────────────┤
│ Application          AMIs        Availability Zones     │
│ ─────────────────────────────────────────────────────── │
│ USER-AUTH-SERVICE    n/a         (1)                    │
│ MENU-SERVICE         n/a         (2)                    │
│ ORDER-SERVICE        n/a         (1)                    │
│ PAYMENT-SERVICE      n/a         (1)                    │
│ CART-SERVICE         n/a         (1)                    │
└─────────────────────────────────────────────────────────┘
```

---

## Key Concepts

### 1. Self-Preservation Mode
```
Jab network issues hote hain, Eureka services ko remove nahi karta
- Production mein useful hai
- Development mein disable kar sakte ho
```

### 2. Heartbeat
```
Har service 30 seconds mein heartbeat bhejti hai
- "Main alive hoon"
- 3 miss = Service removed
```

### 3. Registry Fetch
```
Clients har 30 seconds mein registry fetch karte hain
- Local cache maintain karte hain
- Eureka down bhi ho toh kaam chalta hai
```

---

## Interview Questions

**Q: Eureka kya hai aur kyun use kiya?**
A: Eureka ek Service Registry hai. Services dynamically register hoti hain aur discover hoti hain. Hardcoded URLs ki zaroorat nahi.

**Q: Agar Eureka Server down ho jaye toh?**
A: Clients apni local cache use karte hain. Existing connections kaam karti hain. Naye services register nahi ho paayengi.

**Q: Self-preservation mode kya hai?**
A: Jab network issues hote hain, Eureka services ko remove nahi karta. Ye false positives se bachata hai.

**Q: Heartbeat kya hai?**
A: Har 30 seconds mein service Eureka ko "I'm alive" message bhejti hai. 3 miss pe service unhealthy mark hoti hai.

**Q: Eureka vs Consul vs Zookeeper?**
A: Eureka - Netflix, AP system (Availability + Partition tolerance)
   Consul - HashiCorp, CP system, more features
   Zookeeper - Apache, CP system, complex

---

## Best Practices

1. **Multiple Eureka Servers** - Production mein cluster banao
2. **Health Checks** - Actuator health endpoint expose karo
3. **Prefer IP** - Hostname issues se bachne ke liye
4. **Timeouts** - Appropriate timeouts set karo
5. **Monitoring** - Dashboard regularly check karo
