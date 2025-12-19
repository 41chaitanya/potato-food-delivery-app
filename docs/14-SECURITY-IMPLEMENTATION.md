# 14. Security Implementation - Complete Guide

## 🔐 Overview

Is project mein security implement ki gayi hai using:
1. **JWT (JSON Web Tokens)** - Authentication
2. **Spring Security** - Authorization
3. **BCrypt** - Password Encoding
4. **Role-Based Access Control (RBAC)**

---

## 🏗️ Security Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           CLIENT REQUEST                                 │
└─────────────────────────────────┬───────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                            API GATEWAY                                   │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │                    JWT Validation Filter                         │    │
│  │  - Token presence check                                          │    │
│  │  - Token signature validation                                    │    │
│  │  - Token expiry check                                            │    │
│  │  - Extract user details                                          │    │
│  └─────────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────┬───────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                         MICROSERVICES                                    │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │                   Spring Security Filter                         │    │
│  │  - Role-based authorization                                      │    │
│  │  - Method-level security                                         │    │
│  │  - CORS configuration                                            │    │
│  └─────────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 🔑 JWT Implementation

### JWT Configuration
```java
@Configuration
public class JwtConfig {
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.expiration}")
    private Long jwtExpiration;  // 24 hours in milliseconds
    
    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;  // 7 days
    
    // Getters...
}
```

### JWT Utility Class
```java
@Component
public class JwtUtils {
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.expiration}")
    private Long jwtExpiration;
    
    // Generate Access Token
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", userDetails.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList()));
        
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(userDetails.getUsername())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
    }
    
    // Generate Refresh Token
    public String generateRefreshToken(UserDetails userDetails) {
        return Jwts.builder()
            .setSubject(userDetails.getUsername())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration))
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
    }
    
    // Validate Token
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    
    // Extract Username
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    // Extract Expiration
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    // Check if Token Expired
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
    }
    
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
```

### JWT Token Structure
```
Header.Payload.Signature

Header:
{
  "alg": "HS256",
  "typ": "JWT"
}

Payload:
{
  "sub": "user@example.com",
  "roles": ["ROLE_CUSTOMER"],
  "iat": 1703001600,
  "exp": 1703088000
}

Signature:
HMACSHA256(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  secret
)
```

---

## 🛡️ Spring Security Configuration

### Security Filter Chain
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthFilter;
    
    @Autowired
    private UserDetailsService userDetailsService;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                
                // Role-based access
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/restaurant-owner/**").hasRole("RESTAURANT_OWNER")
                .requestMatchers("/api/delivery/**").hasRole("DELIVERY_PARTNER")
                
                // Authenticated endpoints
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) 
            throws Exception {
        return config.getAuthenticationManager();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

### JWT Authentication Filter
```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Autowired
    private JwtUtils jwtUtils;
    
    @Autowired
    private UserDetailsService userDetailsService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) 
            throws ServletException, IOException {
        
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;
        
        // Check if Authorization header exists and starts with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        jwt = authHeader.substring(7);
        
        try {
            userEmail = jwtUtils.extractUsername(jwt);
            
            if (userEmail != null && 
                SecurityContextHolder.getContext().getAuthentication() == null) {
                
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
                
                if (jwtUtils.validateToken(jwt) && !jwtUtils.isTokenExpired(jwt)) {
                    UsernamePasswordAuthenticationToken authToken = 
                        new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                        );
                    
                    authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e.getMessage());
        }
        
        filterChain.doFilter(request, response);
    }
}
```

---

## 👤 User Roles & Permissions

### Role Hierarchy
```java
public enum UserRole {
    CUSTOMER,           // Basic user - can order food
    RESTAURANT_OWNER,   // Can manage restaurant & menu
    DELIVERY_PARTNER,   // Can accept & deliver orders
    ADMIN               // Full system access
}
```

### Permission Matrix

| Endpoint | CUSTOMER | RESTAURANT_OWNER | DELIVERY_PARTNER | ADMIN |
|----------|----------|------------------|------------------|-------|
| View Restaurants | ✅ | ✅ | ✅ | ✅ |
| View Menu | ✅ | ✅ | ✅ | ✅ |
| Add to Cart | ✅ | ❌ | ❌ | ✅ |
| Place Order | ✅ | ❌ | ❌ | ✅ |
| Manage Restaurant | ❌ | ✅ | ❌ | ✅ |
| Manage Menu | ❌ | ✅ | ❌ | ✅ |
| Accept Delivery | ❌ | ❌ | ✅ | ✅ |
| Update Delivery Status | ❌ | ❌ | ✅ | ✅ |
| View All Orders | ❌ | ❌ | ❌ | ✅ |
| Manage Users | ❌ | ❌ | ❌ | ✅ |

### Method-Level Security
```java
@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {
    
    @GetMapping
    public List<RestaurantDTO> getAllRestaurants() {
        // Public - anyone can view
        return restaurantService.getAllRestaurants();
    }
    
    @PostMapping
    @PreAuthorize("hasRole('RESTAURANT_OWNER') or hasRole('ADMIN')")
    public RestaurantDTO createRestaurant(@RequestBody RestaurantDTO dto) {
        return restaurantService.createRestaurant(dto);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("@restaurantSecurity.isOwner(#id) or hasRole('ADMIN')")
    public RestaurantDTO updateRestaurant(@PathVariable Long id, 
                                          @RequestBody RestaurantDTO dto) {
        return restaurantService.updateRestaurant(id, dto);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteRestaurant(@PathVariable Long id) {
        restaurantService.deleteRestaurant(id);
    }
}
```

### Custom Security Expression
```java
@Component("restaurantSecurity")
public class RestaurantSecurityService {
    
    @Autowired
    private RestaurantRepository restaurantRepository;
    
    public boolean isOwner(Long restaurantId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = auth.getName();
        
        return restaurantRepository.findById(restaurantId)
            .map(restaurant -> restaurant.getOwnerEmail().equals(currentUserEmail))
            .orElse(false);
    }
}
```

---

## 🔄 Authentication Flow

### Login Flow
```
┌──────────┐     ┌─────────────┐     ┌──────────────┐     ┌──────────┐
│  Client  │     │ API Gateway │     │ Auth Service │     │    DB    │
└────┬─────┘     └──────┬──────┘     └──────┬───────┘     └────┬─────┘
     │                  │                   │                  │
     │ POST /api/auth/login                 │                  │
     │ {email, password}│                   │                  │
     │─────────────────>│                   │                  │
     │                  │                   │                  │
     │                  │ Forward request   │                  │
     │                  │──────────────────>│                  │
     │                  │                   │                  │
     │                  │                   │ Find user by email
     │                  │                   │─────────────────>│
     │                  │                   │                  │
     │                  │                   │<─────────────────│
     │                  │                   │   User data      │
     │                  │                   │                  │
     │                  │                   │ Validate password│
     │                  │                   │ (BCrypt compare) │
     │                  │                   │                  │
     │                  │                   │ Generate JWT     │
     │                  │                   │ Generate Refresh │
     │                  │                   │                  │
     │                  │<──────────────────│                  │
     │                  │ {accessToken,     │                  │
     │                  │  refreshToken}    │                  │
     │<─────────────────│                   │                  │
     │ Response         │                   │                  │
     │                  │                   │                  │
```

### Token Refresh Flow
```java
@PostMapping("/refresh-token")
public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
    String refreshToken = request.getRefreshToken();
    
    // Validate refresh token
    if (!jwtUtils.validateToken(refreshToken)) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body("Invalid refresh token");
    }
    
    // Check if refresh token exists in DB
    RefreshToken storedToken = refreshTokenRepository
        .findByToken(refreshToken)
        .orElseThrow(() -> new TokenRefreshException("Refresh token not found"));
    
    // Check expiry
    if (storedToken.getExpiryDate().isBefore(Instant.now())) {
        refreshTokenRepository.delete(storedToken);
        throw new TokenRefreshException("Refresh token expired");
    }
    
    // Generate new access token
    User user = storedToken.getUser();
    String newAccessToken = jwtUtils.generateToken(user);
    
    return ResponseEntity.ok(new TokenRefreshResponse(newAccessToken, refreshToken));
}
```

---

## 🔒 Password Security

### Password Encoding
```java
@Service
public class AuthService {
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public User registerUser(RegisterRequest request) {
        // Encode password before saving
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        
        User user = User.builder()
            .email(request.getEmail())
            .password(encodedPassword)  // Stored as BCrypt hash
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .role(UserRole.CUSTOMER)
            .build();
        
        return userRepository.save(user);
    }
    
    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
```

### Password Validation Rules
```java
public class PasswordValidator {
    
    private static final String PASSWORD_PATTERN = 
        "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";
    
    public static boolean isValid(String password) {
        return password != null && password.matches(PASSWORD_PATTERN);
    }
    
    /*
     * Password must contain:
     * - At least 8 characters
     * - At least one digit
     * - At least one lowercase letter
     * - At least one uppercase letter
     * - At least one special character (@#$%^&+=)
     * - No whitespace
     */
}
```

---

## 🌐 API Gateway Security

### Gateway Filter Configuration
```java
@Component
public class JwtAuthenticationGatewayFilter implements GatewayFilter {
    
    @Autowired
    private JwtUtils jwtUtils;
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Skip authentication for public endpoints
        if (isPublicEndpoint(request.getPath().toString())) {
            return chain.filter(exchange);
        }
        
        // Check Authorization header
        if (!request.getHeaders().containsKey("Authorization")) {
            return onError(exchange, "Missing Authorization header", HttpStatus.UNAUTHORIZED);
        }
        
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, "Invalid Authorization header", HttpStatus.UNAUTHORIZED);
        }
        
        String token = authHeader.substring(7);
        
        try {
            if (!jwtUtils.validateToken(token)) {
                return onError(exchange, "Invalid token", HttpStatus.UNAUTHORIZED);
            }
            
            // Add user info to headers for downstream services
            String username = jwtUtils.extractUsername(token);
            ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-User-Email", username)
                .build();
            
            return chain.filter(exchange.mutate().request(modifiedRequest).build());
            
        } catch (Exception e) {
            return onError(exchange, "Token validation failed", HttpStatus.UNAUTHORIZED);
        }
    }
    
    private boolean isPublicEndpoint(String path) {
        return path.contains("/api/auth/") || 
               path.contains("/api/public/") ||
               path.contains("/actuator/");
    }
    
    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        return response.setComplete();
    }
}
```

---

## 📊 Interview Questions

### Q1: JWT vs Session-based Authentication?
**Answer:**
| Aspect | JWT | Session |
|--------|-----|---------|
| Storage | Client-side (token) | Server-side (session store) |
| Scalability | Stateless, easy to scale | Requires session replication |
| Performance | No DB lookup needed | Session lookup required |
| Security | Token can be stolen | Session ID can be stolen |
| Microservices | Perfect fit | Complex to implement |
| Revocation | Difficult (need blacklist) | Easy (delete session) |

### Q2: JWT Token mein kya store karte ho?
**Answer:**
```json
{
  "sub": "user@example.com",    // Subject (user identifier)
  "roles": ["ROLE_CUSTOMER"],   // User roles
  "userId": 123,                // User ID (optional)
  "iat": 1703001600,            // Issued at
  "exp": 1703088000             // Expiration
}
```
**Note:** Sensitive data (password, PII) kabhi store nahi karte!

### Q3: Token expiry handle kaise karte ho?
**Answer:**
1. **Access Token:** Short-lived (15-60 minutes)
2. **Refresh Token:** Long-lived (7-30 days)
3. **Flow:**
   - Access token expire → Use refresh token
   - Refresh token expire → Re-login required
4. **Implementation:** Refresh endpoint + token rotation

### Q4: CORS kya hai aur kyu zaroori hai?
**Answer:**
- **CORS:** Cross-Origin Resource Sharing
- **Problem:** Browser blocks requests to different origins
- **Solution:** Server specifies allowed origins
- **Configuration:**
  - Allowed Origins: `http://localhost:3000`
  - Allowed Methods: `GET, POST, PUT, DELETE`
  - Allowed Headers: `Authorization, Content-Type`

### Q5: BCrypt kyu use karte ho?
**Answer:**
- **Salted:** Har password ka unique salt
- **Slow:** Brute force attacks difficult
- **Adaptive:** Work factor increase kar sakte ho
- **One-way:** Decrypt nahi ho sakta

### Q6: Method-level security kaise implement ki?
**Answer:**
```java
@EnableMethodSecurity  // Enable annotations

@PreAuthorize("hasRole('ADMIN')")  // Before method execution
@PostAuthorize("returnObject.owner == authentication.name")  // After execution
@Secured("ROLE_ADMIN")  // Simple role check
```

---

## ⚠️ Security Best Practices

1. **Never expose JWT secret** - Environment variable mein rakho
2. **Use HTTPS** - Production mein mandatory
3. **Validate all inputs** - SQL injection, XSS prevention
4. **Rate limiting** - Brute force protection
5. **Token blacklisting** - Logout pe token invalidate
6. **Audit logging** - Security events track karo
7. **Regular key rotation** - JWT secret periodically change karo
