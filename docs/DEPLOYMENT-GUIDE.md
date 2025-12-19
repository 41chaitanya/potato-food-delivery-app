# Deployment Guide

## Environment Variables

Sabhi services environment variables se configure hoti hain. Local mein default values use hoti hain, production mein set karo.

### Required Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_HOST` | localhost | Database host |
| `DB_PORT` | 5432 | Database port |
| `DB_USERNAME` | postgres | Database username |
| `DB_PASSWORD` | 1122334455 | Database password |
| `EUREKA_URI` | http://localhost:8761/eureka | Eureka server URL |
| `CONFIG_SERVER_URI` | http://localhost:8089 | Config server URL |
| `JWT_SECRET` | (default key) | JWT signing key (min 256 bits) |

### Database Variables (per service)
| Variable | Default |
|----------|---------|
| `USER_AUTH_DB` | user_auth_db |
| `RESTAURANT_DB` | restaurant_db |
| `MENU_DB` | menu_db |
| `CART_DB` | cart_db |
| `ORDER_DB` | order_db |
| `PAYMENT_DB` | payment_db |
| `DELIVERY_DB` | delivery_db |
| `ADMIN_DB` | admin_db |

---

## Local Development

```bash
# 1. Start PostgreSQL with databases
# 2. Start services in order:
cd service-registry && mvnw spring-boot:run
cd config-server && mvnw spring-boot:run
cd api-gateway && mvnw spring-boot:run
# ... other services
```

---

## Docker Deployment

```yaml
# docker-compose.yml example
services:
  config-server:
    environment:
      - EUREKA_URI=http://eureka:8761/eureka
      - SPRING_PROFILES_ACTIVE=git
      - CONFIG_GIT_URI=https://github.com/your-org/config.git
      - CONFIG_GIT_USERNAME=${GIT_USER}
      - CONFIG_GIT_PASSWORD=${GIT_TOKEN}

  order-service:
    environment:
      - CONFIG_SERVER_URI=http://config-server:8089
      - EUREKA_URI=http://eureka:8761/eureka
      - DB_HOST=postgres
      - ORDER_DB=order_db
```

---

## AWS/Cloud Deployment

1. **Set environment variables** in ECS Task Definition / EKS ConfigMap
2. **Use RDS** for PostgreSQL - set `DB_HOST` to RDS endpoint
3. **Use Secrets Manager** for sensitive values (DB_PASSWORD, JWT_SECRET)
4. **Config Server**: Use Git profile with GitHub/CodeCommit

---

## Startup Order

1. **SERVICE-REGISTRY** (Eureka) - Port 8761
2. **CONFIG-SERVER** - Port 8089
3. **All other services** - After config server is healthy

---

## Health Check URLs

| Service | Health URL |
|---------|------------|
| Eureka | http://localhost:8761/actuator/health |
| Config Server | http://localhost:8089/actuator/health |
| API Gateway | http://localhost:8080/actuator/health |
| Order Service | http://localhost:8081/actuator/health |

---

## Quick Production Checklist

- [ ] Strong JWT_SECRET generated
- [ ] Database credentials secured
- [ ] Config Server using Git profile
- [ ] All services pointing to correct EUREKA_URI
- [ ] Health checks configured
- [ ] Logging configured for production
