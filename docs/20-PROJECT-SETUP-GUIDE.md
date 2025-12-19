# 20. Project Setup & Running Guide

## 📋 Prerequisites

### Required Software
| Software | Version | Purpose |
|----------|---------|---------|
| Java | 17+ | Runtime |
| Maven | 3.8+ | Build tool |
| MySQL | 8.0+ | Database |
| Git | Latest | Version control |
| Docker | Latest | Containerization (optional) |

### IDE Setup
- IntelliJ IDEA (Recommended) or VS Code
- Lombok plugin installed
- Spring Boot plugin

---

## 🗄️ Database Setup

### 1. Create Databases
```sql
-- Connect to MySQL
mysql -u root -p

-- Create databases for each service
CREATE DATABASE user_auth_db;
CREATE DATABASE restaurant_db;
CREATE DATABASE menu_db;
CREATE DATABASE cart_db;
CREATE DATABASE order_db;
CREATE DATABASE payment_db;
CREATE DATABASE delivery_db;
CREATE DATABASE admin_db;

-- Create user (optional)
CREATE USER 'foodapp'@'localhost' IDENTIFIED BY 'password123';
GRANT ALL PRIVILEGES ON *.* TO 'foodapp'@'localhost';
FLUSH PRIVILEGES;
```

### 2. Environment Variables
Create `.env` file in root directory:
```env
# Database
DB_HOST=localhost
DB_PORT=3306
DB_USERNAME=root
DB_PASSWORD=your_password

# JWT
JWT_SECRET=your-256-bit-secret-key-here-make-it-long-and-random
JWT_EXPIRATION=86400000

# Eureka
EUREKA_HOST=localhost
EUREKA_PORT=8761

# Config Server
CONFIG_SERVER_HOST=localhost
CONFIG_SERVER_PORT=8888
```

---

## 🚀 Service Startup Order

Services ko specific order mein start karna zaroori hai:

```
┌─────────────────────────────────────────────────────────────────┐
│                    STARTUP ORDER                                 │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  Step 1: Infrastructure Services                                 │
│  ┌─────────────────┐    ┌─────────────────┐                     │
│  │  Config Server  │───>│ Service Registry│                     │
│  │    (8888)       │    │    (8761)       │                     │
│  └─────────────────┘    └─────────────────┘                     │
│                                                                  │
│  Step 2: API Gateway                                             │
│  ┌─────────────────┐                                            │
│  │   API Gateway   │                                            │
│  │    (8080)       │                                            │
│  └─────────────────┘                                            │
│                                                                  │
│  Step 3: Business Services (Any Order)                          │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐               │
│  │  User   │ │Restaurant│ │  Menu   │ │  Cart   │               │
│  │ (8081)  │ │ (8082)  │ │ (8083)  │ │ (8084)  │               │
│  └─────────┘ └─────────┘ └─────────┘ └─────────┘               │
│                                                                  │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐               │
│  │  Order  │ │ Payment │ │Delivery │ │  Admin  │               │
│  │ (8085)  │ │ (8086)  │ │ (8087)  │ │ (8088)  │               │
│  └─────────┘ └─────────┘ └─────────┘ └─────────┘               │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 📦 Running Services

### Option 1: Using Maven (Development)

```bash
# Terminal 1: Config Server
cd config-server
mvn spring-boot:run

# Terminal 2: Service Registry (wait for config server)
cd service-registry
mvn spring-boot:run

# Terminal 3: API Gateway (wait for eureka)
cd api-gateway
mvn spring-boot:run

# Terminal 4-11: Business Services
cd user-auth-service
mvn spring-boot:run

cd restaurant-service
mvn spring-boot:run

cd menu-service
mvn spring-boot:run

cd cart-service
mvn spring-boot:run

cd order-service
mvn spring-boot:run

cd payment-service
mvn spring-boot:run

cd delivery-service
mvn spring-boot:run

cd admin-service
mvn spring-boot:run
```

### Option 2: Using JAR Files

```bash
# Build all services
mvn clean package -DskipTests

# Run services
java -jar config-server/target/config-server-0.0.1-SNAPSHOT.jar
java -jar service-registry/target/service-registry-0.0.1-SNAPSHOT.jar
java -jar api-gateway/target/api-gateway-0.0.1-SNAPSHOT.jar
java -jar user-auth-service/target/user-auth-service-0.0.1-SNAPSHOT.jar
# ... and so on
```

### Option 3: Using Docker Compose

```yaml
# docker-compose.yml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root123
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql
      - ./init-db.sql:/docker-entrypoint-initdb.d/init.sql

  config-server:
    build: ./config-server
    ports:
      - "8888:8888"
    environment:
      - SPRING_PROFILES_ACTIVE=docker

  service-registry:
    build: ./service-registry
    ports:
      - "8761:8761"
    depends_on:
      - config-server
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - CONFIG_SERVER_URL=http://config-server:8888

  api-gateway:
    build: ./api-gateway
    ports:
      - "8080:8080"
    depends_on:
      - service-registry
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://service-registry:8761/eureka/

  user-auth-service:
    build: ./user-auth-service
    ports:
      - "8081:8081"
    depends_on:
      - mysql
      - service-registry
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/user_auth_db

  # ... other services

volumes:
  mysql-data:
```

```bash
# Start all services
docker-compose up -d

# Check logs
docker-compose logs -f

# Stop all services
docker-compose down
```

---

## ✅ Verification Steps

### 1. Check Config Server
```bash
curl http://localhost:8888/user-auth-service/default
# Should return configuration
```

### 2. Check Eureka Dashboard
Open browser: http://localhost:8761
- All services should be registered

### 3. Check API Gateway Health
```bash
curl http://localhost:8080/actuator/health
# Should return {"status":"UP"}
```

### 4. Test User Registration
```bash
curl -X POST http://localhost:8080/user-auth-service/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test@123",
    "firstName": "Test",
    "lastName": "User",
    "phone": "9876543210"
  }'
```

### 5. Test Login
```bash
curl -X POST http://localhost:8080/user-auth-service/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test@123"
  }'
# Should return JWT token
```

---

## 🔧 Common Issues & Solutions

### Issue 1: Config Server Connection Refused
```
Error: Connection refused to localhost:8888
```
**Solution:**
- Ensure Config Server is running first
- Check if port 8888 is available
- Verify config server URL in bootstrap.yml

### Issue 2: Eureka Registration Failed
```
Error: Cannot execute request on any known server
```
**Solution:**
- Ensure Service Registry is running
- Check eureka.client.serviceUrl.defaultZone
- Wait for Eureka to fully start (30-60 seconds)

### Issue 3: Database Connection Failed
```
Error: Communications link failure
```
**Solution:**
- Verify MySQL is running
- Check database credentials
- Ensure database exists
- Check firewall settings

### Issue 4: Port Already in Use
```
Error: Port 8080 is already in use
```
**Solution:**
```bash
# Find process using port
netstat -ano | findstr :8080

# Kill process (Windows)
taskkill /PID <pid> /F

# Or change port in application.yml
server:
  port: 8090
```

### Issue 5: Feign Client Timeout
```
Error: Read timed out executing GET
```
**Solution:**
```yaml
# Increase timeout in application.yml
spring:
  cloud:
    openfeign:
      client:
        config:
          default:
            connectTimeout: 10000
            readTimeout: 10000
```

---

## 📊 Service Ports Reference

| Service | Port | Actuator |
|---------|------|----------|
| Config Server | 8888 | /actuator |
| Service Registry | 8761 | /actuator |
| API Gateway | 8080 | /actuator |
| User Auth Service | 8081 | /actuator |
| Restaurant Service | 8082 | /actuator |
| Menu Service | 8083 | /actuator |
| Cart Service | 8084 | /actuator |
| Order Service | 8085 | /actuator |
| Payment Service | 8086 | /actuator |
| Delivery Service | 8087 | /actuator |
| Admin Service | 8088 | /actuator |

---

## 🧪 Testing the Application

### Postman Collection
Import the Postman collection from `docs/postman/` folder.

### Sample API Calls

```bash
# 1. Register User
POST http://localhost:8080/user-auth-service/api/auth/register

# 2. Login
POST http://localhost:8080/user-auth-service/api/auth/login

# 3. Get Restaurants (with token)
GET http://localhost:8080/restaurant-service/api/restaurants
Header: Authorization: Bearer <token>

# 4. Get Menu
GET http://localhost:8080/menu-service/api/menu-items/restaurant/1

# 5. Add to Cart
POST http://localhost:8080/cart-service/api/cart/items

# 6. Place Order
POST http://localhost:8080/order-service/api/orders

# 7. Make Payment
POST http://localhost:8080/payment-service/api/payments
```

---

## 🐛 Debugging Tips

### Enable Debug Logging
```yaml
logging:
  level:
    com.microServiceTut: DEBUG
    org.springframework.cloud: DEBUG
    feign: DEBUG
```

### Check Service Health
```bash
# Individual service
curl http://localhost:8081/actuator/health

# Through gateway
curl http://localhost:8080/user-auth-service/actuator/health
```

### View Registered Services
```bash
curl http://localhost:8761/eureka/apps
```

### Check Configuration
```bash
curl http://localhost:8888/user-auth-service/default
```
