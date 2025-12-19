# 🍕 Food Delivery Microservices Platform

A production-ready food delivery backend built with Spring Boot Microservices architecture.

---

## 🏗️ Architecture Overview

```
                                    ┌─────────────────┐
                                    │  Service        │
                                    │  Registry       │
                                    │  (Eureka)       │
                                    │  Port: 8761     │
                                    └────────┬────────┘
                                             │
                    ┌────────────────────────┼────────────────────────┐
                    │                        │                        │
                    ▼                        ▼                        ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                           API Gateway (Port: 8080)                          │
│                    JWT Authentication + Route Management                     │
└─────────────────────────────────────────────────────────────────────────────┘
                    │
    ┌───────────────┼───────────────┬───────────────┬───────────────┐
    │               │               │               │               │
    ▼               ▼               ▼               ▼               ▼
┌────────┐    ┌────────┐    ┌────────┐    ┌────────┐    ┌────────┐
│  User  │    │Restaurant│   │  Menu  │    │  Cart  │    │ Order  │
│  Auth  │    │ Service │    │Service │    │Service │    │Service │
│  8086  │    │  8083   │    │  8084  │    │  8085  │    │  8081  │
└────────┘    └────────┘    └────────┘    └────────┘    └───┬────┘
                                                            │
                                          ┌─────────────────┼─────────────────┐
                                          ▼                                   ▼
                                    ┌────────┐                          ┌────────┐
                                    │Payment │                          │Delivery│
                                    │Service │                          │Service │
                                    │  8082  │                          │  8087  │
                                    └────────┘                          └────────┘
```

---

## 🛠️ Tech Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 21 | Programming Language |
| Spring Boot | 3.5.x | Application Framework |
| Spring Cloud | 2025.0.0 | Microservices Tools |
| PostgreSQL | 12+ | Database |
| Redis Cloud | 8.x | Caching & JWT Blacklist |
| Netflix Eureka | - | Service Discovery |
| Spring Cloud Gateway | - | API Gateway |
| Spring Cloud Config | - | Centralized Configuration |
| JWT | - | Authentication |
| Resilience4j | - | Circuit Breaker |
| Grafana Loki | - | Centralized Logging |
| Zipkin/Tempo | - | Distributed Tracing |
| Lombok | 1.18.34 | Boilerplate Reduction |
| Maven | 3.9+ | Build Tool |

---

## 📦 Services

| Service | Port | Description | Status |
|---------|------|-------------|--------|
| Config Server | 8089 | Centralized Configuration | ✅ Active |
| Service Registry | 8761 | Eureka Server - Service Discovery | ✅ Active |
| API Gateway | 8080 | Entry point, JWT validation, routing | ✅ Active |
| User Auth Service | 8086 | User registration, login, JWT tokens | ✅ Active |
| Restaurant Service | 8083 | Restaurant CRUD operations | ✅ Active |
| Menu Service | 8084 | Menu items management | ✅ Active |
| Cart Service | 8085 | Shopping cart management | ✅ Active |
| Order Service | 8081 | Order processing with payment | ✅ Active |
| Payment Service | 8082 | Payment processing (mock) | ✅ Active |
| Delivery Service | 8087 | Delivery & rider management | ✅ Active |
| Admin Service | 8088 | Platform analytics & management | ✅ Active |

---

## 🚀 Getting Started

### Prerequisites

- Java 21 or higher
- Maven 3.9+
- PostgreSQL 12+
- Git

### Clone the Repository

```bash
git clone https://github.com/your-username/food-delivery-microservices.git
cd food-delivery-microservices
```

### Database Setup

Create the database in PostgreSQL:

```sql
CREATE DATABASE microst;
```

---

## ⚙️ Environment Setup

### 1. Create Environment File

```bash
cp .env.example .env
```

### 2. Update `.env` with your credentials

```properties
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=microst
DB_USERNAME=postgres
DB_PASSWORD=your_password_here

# JWT (Change in production!)
JWT_SECRET=your_super_secret_key_minimum_32_characters_long
JWT_EXPIRATION=86400000
```

### 3. Configure IntelliJ IDEA

For each service:
1. Open **Run Configuration**
2. Go to **Environment Variables**
3. Click **"..."** → **Load from file**
4. Select `.env` file from project root

---

## ▶️ Running the Project

### Start Order (Important!)

Start services in this order:

```bash
# 1. Config Server (Start First!)
cd config-server
./mvnw spring-boot:run

# 2. Service Registry
cd service-registry
./mvnw spring-boot:run

# 3. Other Services (Any Order)
cd user-auth-service && ./mvnw spring-boot:run
cd restaurant-service && ./mvnw spring-boot:run
cd menu-service && ./mvnw spring-boot:run
cd cart-service && ./mvnw spring-boot:run
cd payment-service && ./mvnw spring-boot:run
cd order-service && ./mvnw spring-boot:run
cd delivery-service && ./mvnw spring-boot:run
cd admin-service && ./mvnw spring-boot:run

# 4. API Gateway (Start Last!)
cd api-gateway && ./mvnw spring-boot:run
```

### Verify Services

- Config Server: http://localhost:8089/actuator/health
- Eureka Dashboard: http://localhost:8761
- API Gateway: http://localhost:8080

---

## 📡 API Documentation

### Authentication

All endpoints (except `/auth/*`) require JWT token in header:
```
Authorization: Bearer <your_jwt_token>
```

### Role-Based Access

| Role | Access |
|------|--------|
| `ADMIN` | Restaurant, Menu, Delivery assignment |
| `USER` | Cart, Orders, View restaurants/menus |
| `RIDER` | Delivery pickup & deliver |

### API Endpoints Summary

#### Auth Service
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/auth/register` | Register new user |
| POST | `/auth/login` | Login user |
| GET | `/auth/profile/{userId}` | Get user profile |
| PATCH | `/auth/profile/{userId}` | Update profile (name, phone, address) |

#### Restaurant Service
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/restaurants` | Create restaurant |
| GET | `/api/restaurants` | Get all active restaurants |
| GET | `/api/restaurants/{id}` | Get restaurant by ID |
| PATCH | `/api/restaurants/{id}` | Update restaurant |
| PATCH | `/api/restaurants/{id}/toggle-status` | Open/Close restaurant |
| DELETE | `/api/restaurants/{id}` | Soft delete restaurant |

#### Menu Service
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/menus` | Create menu item |
| GET | `/api/menus/{id}` | Get menu item |
| GET | `/api/menus/restaurant/{restaurantId}` | Get menu by restaurant |
| PATCH | `/api/menus/{id}` | Update menu item |
| PATCH | `/api/menus/{id}/toggle-availability` | Toggle item availability |
| DELETE | `/api/menus/{id}` | Soft delete menu item |

#### Cart Service
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/cart/items` | Add item to cart |
| GET | `/api/cart/{userId}` | Get user's cart |
| PATCH | `/api/cart/items/{itemId}?quantity=N` | Update item quantity |
| DELETE | `/api/cart/items/{itemId}` | Remove item from cart |
| DELETE | `/api/cart/{userId}` | Clear cart |

#### Order Service
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/orders` | Create order |
| GET | `/api/orders/{orderId}` | Get order by ID |
| GET | `/api/orders/user/{userId}` | Get order history |
| PATCH | `/api/orders/{orderId}/cancel` | Cancel order |
| PATCH | `/api/orders/{orderId}/status?status=X` | Update order status |

#### Delivery Service
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/delivery/assign/{orderId}` | Assign delivery (ADMIN) |
| GET | `/api/delivery/rider` | Get rider's deliveries |
| PUT | `/api/delivery/{id}/pickup` | Pickup order (RIDER) |
| PUT | `/api/delivery/{id}/deliver` | Deliver order (RIDER) |

### Base URLs

| Service | Direct URL | Via Gateway |
|---------|------------|-------------|
| Auth | http://localhost:8086 | http://localhost:8080/auth |
| Restaurant | http://localhost:8083 | http://localhost:8080/api/restaurants |
| Menu | http://localhost:8084 | http://localhost:8080/api/menus |
| Cart | http://localhost:8085 | http://localhost:8080/api/cart |
| Order | http://localhost:8081 | http://localhost:8080/api/orders |
| Delivery | http://localhost:8087 | http://localhost:8080/api/delivery |

---

## 🚀 Redis Caching

Redis Cloud is used for caching frequently accessed data and JWT token blacklist.

| Service | Cache Use Case | TTL |
|---------|---------------|-----|
| Menu Service | Menu items by restaurant | 15 min |
| Restaurant Service | Active restaurants list | 10 min |
| Cart Service | User cart data | 30 min |
| User Auth Service | JWT Token Blacklist (logout) | Token expiry |

### Performance Improvement

| Operation | Without Cache | With Cache | Improvement |
|-----------|--------------|------------|-------------|
| Get Menu | ~1000ms | ~60ms | ~16x faster |
| Get Restaurants | ~800ms | ~65ms | ~12x faster |
| Get Cart | ~900ms | ~60ms | ~15x faster |

---

## 📊 Observability

### Centralized Logging (Grafana Loki)
All services send logs to Grafana Cloud Loki with trace correlation.

### Distributed Tracing (Zipkin/Tempo)
Request tracing across services with unique trace IDs.

### Health Monitoring
Each service exposes `/actuator/health` endpoint.

---

## 📁 Project Structure

```
food-delivery-microservices/
├── .env.example              # Environment template
├── .env                      # Environment variables (git ignored)
├── .gitignore                # Git ignore rules
├── README.md                 # This file
│
├── config-server/            # Spring Cloud Config Server
├── config-repo/              # Configuration files for all services
├── service-registry/         # Eureka Server
├── api-gateway/              # API Gateway + JWT
├── user-auth-service/        # Authentication + JWT Blacklist (Redis)
├── restaurant-service/       # Restaurant management + Caching (Redis)
├── menu-service/             # Menu management + Caching (Redis)
├── cart-service/             # Shopping cart + Caching (Redis)
├── order-service/            # Order processing + Circuit Breaker
├── payment-service/          # Payment processing
├── delivery-service/         # Delivery management
├── admin-service/            # Platform analytics
└── observability/            # Monitoring configs
```

### Service Structure (Each Service)

```
service-name/
├── src/main/java/com/microServiceTut/service_name/
│   ├── controller/           # REST Controllers
│   ├── service/              # Business Logic
│   ├── repository/           # Data Access
│   ├── model/                # Entity Classes
│   ├── dto/
│   │   ├── request/          # Request DTOs
│   │   └── response/         # Response DTOs
│   ├── mapper/               # Entity-DTO Mappers
│   ├── exception/            # Custom Exceptions
│   ├── config/               # Configuration
│   └── client/               # Feign/WebClient
├── src/main/resources/
│   └── application.yaml      # Configuration
└── pom.xml                   # Dependencies
```

---

## 🤝 Contributing

### Setup for Contributors

1. Fork the repository
2. Clone your fork:
   ```bash
   git clone https://github.com/your-username/food-delivery-microservices.git
   ```
3. Create `.env` file from `.env.example`
4. Create a feature branch:
   ```bash
   git checkout -b feature/your-feature-name
   ```
5. Make changes and commit:
   ```bash
   git commit -m "feat: add your feature"
   ```
6. Push and create Pull Request

### Coding Standards

- Use constructor injection (no `@Autowired` on fields)
- Return DTOs from controllers (never entities)
- Use `@Transactional` for database operations
- Follow existing package structure
- Add proper logging

---

## 🔧 Key Features

- **Microservices Architecture** - 11 independent services
- **Service Discovery** - Netflix Eureka for dynamic service registration
- **API Gateway** - Single entry point with JWT authentication
- **Centralized Config** - Spring Cloud Config Server
- **Redis Caching** - High-performance caching with Redis Cloud
- **JWT Blacklist** - Secure logout with Redis-based token blacklist
- **Circuit Breaker** - Resilience4j for fault tolerance
- **Centralized Logging** - Grafana Loki integration
- **Distributed Tracing** - Zipkin/Tempo for request tracing
- **Role-Based Access** - ADMIN, USER, RIDER roles

---

## 📄 License

This project is for educational purposes.

---

## 👨‍💻 Author

Built with ❤️ for learning microservices architecture.
