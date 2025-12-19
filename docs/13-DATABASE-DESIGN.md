# 13. Database Design & Schema - Complete Guide

## 📊 Overview

Har microservice ka apna dedicated database hai (Database per Service pattern).

---

## 🗄️ Database Distribution

| Service | Database | Port | Schema |
|---------|----------|------|--------|
| User Auth Service | MySQL | 3306 | user_auth_db |
| Restaurant Service | MySQL | 3306 | restaurant_db |
| Menu Service | MySQL | 3306 | menu_db |
| Cart Service | MySQL | 3306 | cart_db |
| Order Service | MySQL | 3306 | order_db |
| Payment Service | MySQL | 3306 | payment_db |
| Delivery Service | MySQL | 3306 | delivery_db |
| Admin Service | MySQL | 3306 | admin_db |

---

## 📋 Entity Relationship Diagrams

### 1. User Auth Service Schema

```sql
-- Users Table
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone VARCHAR(20),
    role ENUM('CUSTOMER', 'RESTAURANT_OWNER', 'DELIVERY_PARTNER', 'ADMIN'),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- User Addresses Table
CREATE TABLE user_addresses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100),
    postal_code VARCHAR(20),
    country VARCHAR(100) DEFAULT 'India',
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    is_default BOOLEAN DEFAULT FALSE,
    address_type ENUM('HOME', 'WORK', 'OTHER'),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Refresh Tokens Table
CREATE TABLE refresh_tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token VARCHAR(500) NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

```
┌─────────────────┐       ┌─────────────────────┐
│     USERS       │       │   USER_ADDRESSES    │
├─────────────────┤       ├─────────────────────┤
│ id (PK)         │──────<│ user_id (FK)        │
│ email           │       │ id (PK)             │
│ password        │       │ address_line1       │
│ first_name      │       │ city                │
│ last_name       │       │ postal_code         │
│ phone           │       │ latitude            │
│ role            │       │ longitude           │
│ is_active       │       │ is_default          │
└─────────────────┘       └─────────────────────┘
         │
         │
         ▼
┌─────────────────┐
│ REFRESH_TOKENS  │
├─────────────────┤
│ id (PK)         │
│ user_id (FK)    │
│ token           │
│ expiry_date     │
└─────────────────┘
```

### 2. Restaurant Service Schema

```sql
-- Restaurants Table
CREATE TABLE restaurants (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    owner_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    cuisine_type VARCHAR(100),
    address VARCHAR(500),
    city VARCHAR(100),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    phone VARCHAR(20),
    email VARCHAR(255),
    rating DECIMAL(2, 1) DEFAULT 0.0,
    total_reviews INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    is_open BOOLEAN DEFAULT TRUE,
    opening_time TIME,
    closing_time TIME,
    minimum_order DECIMAL(10, 2) DEFAULT 0.00,
    delivery_radius_km INT DEFAULT 10,
    avg_delivery_time INT DEFAULT 30,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Restaurant Images Table
CREATE TABLE restaurant_images (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    restaurant_id BIGINT NOT NULL,
    image_url VARCHAR(500),
    is_primary BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (restaurant_id) REFERENCES restaurants(id)
);
```

```
┌─────────────────────┐       ┌─────────────────────┐
│    RESTAURANTS      │       │  RESTAURANT_IMAGES  │
├─────────────────────┤       ├─────────────────────┤
│ id (PK)             │──────<│ restaurant_id (FK)  │
│ owner_id            │       │ id (PK)             │
│ name                │       │ image_url           │
│ description         │       │ is_primary          │
│ cuisine_type        │       └─────────────────────┘
│ address             │
│ rating              │
│ is_active           │
│ is_open             │
│ opening_time        │
│ closing_time        │
└─────────────────────┘
```

### 3. Menu Service Schema

```sql
-- Categories Table
CREATE TABLE categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    restaurant_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    display_order INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE
);

-- Menu Items Table
CREATE TABLE menu_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    restaurant_id BIGINT NOT NULL,
    category_id BIGINT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    discounted_price DECIMAL(10, 2),
    image_url VARCHAR(500),
    is_vegetarian BOOLEAN DEFAULT FALSE,
    is_vegan BOOLEAN DEFAULT FALSE,
    is_available BOOLEAN DEFAULT TRUE,
    preparation_time INT DEFAULT 15,
    calories INT,
    spice_level ENUM('MILD', 'MEDIUM', 'HOT', 'EXTRA_HOT'),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id)
);

-- Item Addons Table
CREATE TABLE item_addons (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    menu_item_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    is_available BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (menu_item_id) REFERENCES menu_items(id)
);
```

```
┌─────────────────┐       ┌─────────────────┐       ┌─────────────────┐
│   CATEGORIES    │       │   MENU_ITEMS    │       │  ITEM_ADDONS    │
├─────────────────┤       ├─────────────────┤       ├─────────────────┤
│ id (PK)         │──────<│ category_id(FK) │       │ id (PK)         │
│ restaurant_id   │       │ id (PK)         │──────<│ menu_item_id(FK)│
│ name            │       │ restaurant_id   │       │ name            │
│ description     │       │ name            │       │ price           │
│ display_order   │       │ price           │       │ is_available    │
│ is_active       │       │ is_vegetarian   │       └─────────────────┘
└─────────────────┘       │ is_available    │
                          │ preparation_time│
                          └─────────────────┘
```

### 4. Cart Service Schema

```sql
-- Carts Table
CREATE TABLE carts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    restaurant_id BIGINT,
    total_amount DECIMAL(10, 2) DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Cart Items Table
CREATE TABLE cart_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    cart_id BIGINT NOT NULL,
    menu_item_id BIGINT NOT NULL,
    menu_item_name VARCHAR(255),
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(10, 2) NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    special_instructions TEXT,
    FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE
);

-- Cart Item Addons Table
CREATE TABLE cart_item_addons (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    cart_item_id BIGINT NOT NULL,
    addon_id BIGINT NOT NULL,
    addon_name VARCHAR(100),
    addon_price DECIMAL(10, 2),
    FOREIGN KEY (cart_item_id) REFERENCES cart_items(id) ON DELETE CASCADE
);
```

```
┌─────────────────┐       ┌─────────────────┐       ┌─────────────────────┐
│     CARTS       │       │   CART_ITEMS    │       │  CART_ITEM_ADDONS   │
├─────────────────┤       ├─────────────────┤       ├─────────────────────┤
│ id (PK)         │──────<│ cart_id (FK)    │──────<│ cart_item_id (FK)   │
│ user_id (UK)    │       │ id (PK)         │       │ id (PK)             │
│ restaurant_id   │       │ menu_item_id    │       │ addon_id            │
│ total_amount    │       │ quantity        │       │ addon_name          │
│ created_at      │       │ unit_price      │       │ addon_price         │
│ updated_at      │       │ total_price     │       └─────────────────────┘
└─────────────────┘       └─────────────────┘
```

### 5. Order Service Schema

```sql
-- Orders Table
CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_number VARCHAR(50) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    restaurant_id BIGINT NOT NULL,
    delivery_address_id BIGINT,
    status ENUM('PENDING', 'CONFIRMED', 'PREPARING', 'READY', 
                'OUT_FOR_DELIVERY', 'DELIVERED', 'CANCELLED') DEFAULT 'PENDING',
    subtotal DECIMAL(10, 2) NOT NULL,
    tax_amount DECIMAL(10, 2) DEFAULT 0.00,
    delivery_fee DECIMAL(10, 2) DEFAULT 0.00,
    discount_amount DECIMAL(10, 2) DEFAULT 0.00,
    total_amount DECIMAL(10, 2) NOT NULL,
    payment_method ENUM('CASH', 'CARD', 'UPI', 'WALLET'),
    payment_status ENUM('PENDING', 'PAID', 'FAILED', 'REFUNDED') DEFAULT 'PENDING',
    special_instructions TEXT,
    estimated_delivery_time TIMESTAMP,
    actual_delivery_time TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Order Items Table
CREATE TABLE order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    menu_item_id BIGINT NOT NULL,
    menu_item_name VARCHAR(255),
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    special_instructions TEXT,
    FOREIGN KEY (order_id) REFERENCES orders(id)
);

-- Order Status History Table
CREATE TABLE order_status_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    notes TEXT,
    changed_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id)
);
```

```
┌─────────────────────┐       ┌─────────────────┐
│      ORDERS         │       │   ORDER_ITEMS   │
├─────────────────────┤       ├─────────────────┤
│ id (PK)             │──────<│ order_id (FK)   │
│ order_number (UK)   │       │ id (PK)         │
│ user_id             │       │ menu_item_id    │
│ restaurant_id       │       │ menu_item_name  │
│ status              │       │ quantity        │
│ subtotal            │       │ unit_price      │
│ total_amount        │       │ total_price     │
│ payment_status      │       └─────────────────┘
│ created_at          │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────────┐
│  ORDER_STATUS_HISTORY   │
├─────────────────────────┤
│ id (PK)                 │
│ order_id (FK)           │
│ status                  │
│ notes                   │
│ changed_by              │
│ created_at              │
└─────────────────────────┘
```

### 6. Payment Service Schema

```sql
-- Payments Table
CREATE TABLE payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    payment_id VARCHAR(100) UNIQUE NOT NULL,
    order_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(10) DEFAULT 'INR',
    payment_method ENUM('CASH', 'CARD', 'UPI', 'WALLET', 'NET_BANKING'),
    status ENUM('INITIATED', 'PROCESSING', 'SUCCESS', 'FAILED', 'REFUNDED'),
    gateway_transaction_id VARCHAR(255),
    gateway_response TEXT,
    failure_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Refunds Table
CREATE TABLE refunds (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    refund_id VARCHAR(100) UNIQUE NOT NULL,
    payment_id BIGINT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    reason TEXT,
    status ENUM('INITIATED', 'PROCESSING', 'SUCCESS', 'FAILED'),
    gateway_refund_id VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (payment_id) REFERENCES payments(id)
);
```

### 7. Delivery Service Schema

```sql
-- Delivery Partners Table
CREATE TABLE delivery_partners (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    vehicle_type ENUM('BIKE', 'SCOOTER', 'CAR', 'BICYCLE'),
    vehicle_number VARCHAR(50),
    license_number VARCHAR(100),
    is_available BOOLEAN DEFAULT TRUE,
    is_verified BOOLEAN DEFAULT FALSE,
    current_latitude DECIMAL(10, 8),
    current_longitude DECIMAL(11, 8),
    rating DECIMAL(2, 1) DEFAULT 5.0,
    total_deliveries INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Deliveries Table
CREATE TABLE deliveries (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    delivery_number VARCHAR(50) UNIQUE NOT NULL,
    order_id BIGINT NOT NULL,
    partner_id BIGINT,
    pickup_address TEXT,
    delivery_address TEXT,
    pickup_latitude DECIMAL(10, 8),
    pickup_longitude DECIMAL(11, 8),
    delivery_latitude DECIMAL(10, 8),
    delivery_longitude DECIMAL(11, 8),
    status ENUM('PENDING', 'ASSIGNED', 'PICKED_UP', 'IN_TRANSIT', 
                'DELIVERED', 'CANCELLED') DEFAULT 'PENDING',
    distance_km DECIMAL(5, 2),
    estimated_time INT,
    actual_pickup_time TIMESTAMP,
    actual_delivery_time TIMESTAMP,
    delivery_fee DECIMAL(10, 2),
    tip_amount DECIMAL(10, 2) DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (partner_id) REFERENCES delivery_partners(id)
);

-- Delivery Tracking Table
CREATE TABLE delivery_tracking (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    delivery_id BIGINT NOT NULL,
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    status VARCHAR(50),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (delivery_id) REFERENCES deliveries(id)
);
```

---

## 🔗 Cross-Service Data References

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         DATA FLOW DIAGRAM                                │
└─────────────────────────────────────────────────────────────────────────┘

User Service (user_id) ──────────────────────────────────────────────────┐
       │                                                                  │
       ├──► Restaurant Service (owner_id references user_id)              │
       │           │                                                      │
       │           └──► Menu Service (restaurant_id)                      │
       │                       │                                          │
       │                       ▼                                          │
       ├──────────────► Cart Service (user_id, menu_item_id)              │
       │                       │                                          │
       │                       ▼                                          │
       ├──────────────► Order Service (user_id, restaurant_id)            │
       │                       │                                          │
       │                       ├──► Payment Service (order_id, user_id)   │
       │                       │                                          │
       │                       └──► Delivery Service (order_id)           │
       │                                   │                              │
       └───────────────────────────────────┘                              │
                    (delivery_partner references user_id)                 │
                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 📊 Interview Questions

### Q1: Database per Service pattern kyu use kiya?
**Answer:**
- **Loose Coupling:** Services independent hain
- **Technology Freedom:** Har service apna DB choose kar sakti hai
- **Scaling:** Individual database scale kar sakte hain
- **Failure Isolation:** Ek DB down hone se doosri services affect nahi hoti

### Q2: Data Consistency kaise maintain karte ho?
**Answer:**
- **Eventual Consistency:** Accept karte hain ki data immediately consistent nahi hoga
- **Saga Pattern:** Distributed transactions ke liye
- **Event Sourcing:** State changes as events store karte hain
- **Compensating Transactions:** Failure pe rollback

### Q3: Foreign Keys across services kaise handle karte ho?
**Answer:**
- Foreign keys physically nahi hote across services
- **Logical References:** IDs store karte hain
- **Validation:** API call karke verify karte hain
- **Caching:** Frequently accessed data cache karte hain

### Q4: Reporting/Analytics ke liye data kaise aggregate karte ho?
**Answer:**
```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  Service 1  │     │  Service 2  │     │  Service 3  │
│     DB      │     │     DB      │     │     DB      │
└──────┬──────┘     └──────┬──────┘     └──────┬──────┘
       │                   │                   │
       └───────────────────┼───────────────────┘
                           │
                           ▼
                  ┌─────────────────┐
                  │   ETL Process   │
                  └────────┬────────┘
                           │
                           ▼
                  ┌─────────────────┐
                  │  Data Warehouse │
                  │   (Analytics)   │
                  └─────────────────┘
```

### Q5: Indexing strategy kya hai?
**Answer:**
```sql
-- Primary lookups
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_orders_user ON orders(user_id);
CREATE INDEX idx_orders_restaurant ON orders(restaurant_id);
CREATE INDEX idx_orders_status ON orders(status);

-- Composite indexes for common queries
CREATE INDEX idx_orders_user_status ON orders(user_id, status);
CREATE INDEX idx_menu_restaurant_category ON menu_items(restaurant_id, category_id);

-- Full-text search
CREATE FULLTEXT INDEX idx_restaurant_search ON restaurants(name, description, cuisine_type);
```

---

## 🛠️ JPA Entity Examples

### User Entity
```java
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    private String firstName;
    private String lastName;
    private String phone;
    
    @Enumerated(EnumType.STRING)
    private UserRole role;
    
    private Boolean isActive = true;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserAddress> addresses;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

### Order Entity with Relationships
```java
@Entity
@Table(name = "orders")
@Data
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String orderNumber;
    
    private Long userId;
    private Long restaurantId;
    
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal deliveryFee;
    private BigDecimal totalAmount;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderStatusHistory> statusHistory = new ArrayList<>();
}
```
