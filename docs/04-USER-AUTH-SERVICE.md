# User Auth Service (Port: 8086)

## Ye Service Kya Karti Hai?

User Auth Service **authentication aur authorization** handle karti hai. User registration, login, JWT token generation, aur logout (token blacklist) - sab yahan hota hai.

---

## Features

| Feature | Description |
|---------|-------------|
| User Registration | New user create with BCrypt password |
| Login | Email/password verify, JWT token return |
| JWT Token | Stateless authentication token |
| Logout | Token blacklist in Redis |
| Profile Management | View/update user profile |
| Role-Based Access | ADMIN, USER, RIDER roles |

---

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                 User Auth Service (8086)                 │
│                                                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │
│  │  Controller  │  │   Service    │  │  Repository  │   │
│  │              │  │              │  │              │   │
│  │ /api/auth/*  │→ │ AuthService  │→ │UserRepository│   │
│  └──────────────┘  └──────────────┘  └──────────────┘   │
│         │                 │                  │           │
│         │                 ▼                  ▼           │
│         │         ┌──────────────┐   ┌──────────────┐   │
│         │         │   JwtUtil    │   │  PostgreSQL  │   │
│         │         │              │   │  user_auth_db│   │
│         │         └──────────────┘   └──────────────┘   │
│         │                 │                              │
│         │                 ▼                              │
│         │         ┌──────────────┐                      │
│         └────────→│    Redis     │ ← JWT Blacklist      │
│                   │   (Cloud)    │                      │
│                   └──────────────┘                      │
└─────────────────────────────────────────────────────────┘
```

---

## Tech Stack

| Technology | Purpose |
|------------|---------|
| Spring Security | Security framework |
| JWT (jjwt 0.12.6) | Token generation/validation |
| BCrypt | Password hashing |
| Redis | JWT Blacklist for logout |
| PostgreSQL | User data storage |

---

## Database Schema

```sql
CREATE TABLE users (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,  -- BCrypt hashed
    role VARCHAR(20) DEFAULT 'USER', -- ADMIN, USER, RIDER
    phone VARCHAR(15),
    address TEXT,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

---

## API Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | /api/auth/register | New user registration | ❌ |
| POST | /api/auth/login | User login, get JWT | ❌ |
| POST | /api/auth/logout | Logout, blacklist token | ✅ |
| GET | /api/auth/validate | Validate JWT token | ✅ |
| GET | /api/auth/profile/{userId} | Get user profile | ✅ |
| PATCH | /api/auth/profile/{userId} | Update profile | ✅ |

### Admin Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/auth/admin/users | Get all users |
| GET | /api/auth/admin/users/role/{role} | Get users by role |
| PATCH | /api/auth/admin/users/{id}/block | Block user |
| PATCH | /api/auth/admin/users/{id}/unblock | Unblock user |

---

## Key Flows

### 1. Registration Flow
```
Client: POST /api/auth/register
{
  "name": "Rahul Sharma",
  "email": "rahul@gmail.com",
  "password": "password123",
  "phone": "9876543210"
}
        │
        ▼
1. Email duplicate check
2. Password BCrypt hash
3. User save to DB
4. JWT token generate
        │
        ▼
Response:
{
  "userId": "uuid",
  "name": "Rahul Sharma",
  "email": "rahul@gmail.com",
  "role": "USER",
  "token": "eyJhbGciOiJIUzUxMiJ9..."
}
```

### 2. Login Flow
```
Client: POST /api/auth/login
{
  "email": "rahul@gmail.com",
  "password": "password123"
}
        │
        ▼
1. User find by email
2. Password verify (BCrypt match)
3. Check if user active
4. JWT token generate
        │
        ▼
Response:
{
  "userId": "uuid",
  "token": "eyJhbGciOiJIUzUxMiJ9..."
}
```

### 3. Logout Flow (JWT Blacklist)
```
Client: POST /api/auth/logout
Headers: Authorization: Bearer eyJhbGc...
        │
        ▼
1. Token se JTI (JWT ID) extract
2. Token ki remaining expiry calculate
3. Redis mein store: blacklist:jwt:{jti} = "true"
   TTL = remaining expiry time
        │
        ▼
Response: { "message": "Logged out successfully" }

Next Request with same token:
1. Gateway validates token
2. Auth Service checks Redis blacklist
3. Token found in blacklist → REJECTED
```

---

## JWT Token Structure

```
Header:
{
  "alg": "HS512",
  "typ": "JWT"
}

Payload:
{
  "jti": "unique-token-id",      // For blacklist
  "sub": "user-uuid",            // User ID
  "email": "rahul@gmail.com",
  "role": "USER",
  "iat": 1703001600,             // Issued at
  "exp": 1703088000              // Expiry (24 hours)
}

Signature:
HMACSHA512(
  base64(header) + "." + base64(payload),
  secret_key
)
```

---

## Redis JWT Blacklist

```
Purpose: Stateless JWT mein logout support

Problem:
- JWT stateless hai, server pe session nahi
- Token valid hai tab tak use ho sakta hai
- Logout kaise karein?

Solution:
- Logout pe token ID Redis mein store karo
- Har request pe check karo - token blacklisted hai?
- TTL = token expiry (automatic cleanup)

Redis Key: blacklist:jwt:{token-id}
Value: "true"
TTL: Remaining token expiry time
```

---

## Security Configuration

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> 
                session.sessionCreationPolicy(STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .anyRequest().authenticated()
            );
        return http.build();
    }
}
```

---

## Interview Questions

**Q: JWT kyun use kiya, session kyun nahi?**
A: Microservices mein stateless better hai. JWT self-contained hai, har service validate kar sakti hai. Session sticky sessions ya shared session store chahiye.

**Q: JWT logout kaise implement kiya?**
A: Redis mein token blacklist maintain karte hain. Logout pe token ID store hota hai with TTL = remaining expiry. Har request pe blacklist check hota hai.

**Q: BCrypt kyun use kiya?**
A: BCrypt slow hashing algorithm hai, brute force attacks se protect karta hai. Salt automatically handle karta hai.

**Q: Token expiry kitni rakhi aur kyun?**
A: 24 hours (86400000 ms). Balance between security aur user experience. Short expiry = frequent login, Long expiry = security risk.

**Q: Refresh token implement kiya?**
A: Is project mein nahi kiya. Production mein access token (short lived) + refresh token (long lived) pattern use karna chahiye.

---

## Best Practices

1. **Strong Secret** - JWT secret minimum 256 bits
2. **Short Expiry** - Access tokens short lived
3. **HTTPS Only** - Tokens encrypted transit mein
4. **Blacklist Cleanup** - Redis TTL automatic cleanup
5. **Rate Limiting** - Login attempts limit karo
6. **Password Policy** - Strong password enforce karo
