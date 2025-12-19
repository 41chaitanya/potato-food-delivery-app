# 16. DTO & Mapper Patterns - Complete Guide

## 📋 Overview

DTOs (Data Transfer Objects) aur Mappers use karte hain for:
- Entity aur API response separation
- Data transformation
- Security (sensitive fields hide karna)
- API versioning support

---

## 🏗️ Architecture Pattern

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           CLIENT                                         │
└─────────────────────────────────┬───────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                          CONTROLLER                                      │
│                    (Accepts/Returns DTOs)                                │
└─────────────────────────────────┬───────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                           SERVICE                                        │
│              (Uses Mapper to convert Entity ↔ DTO)                       │
└─────────────────────────────────┬───────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                          REPOSITORY                                      │
│                      (Works with Entities)                               │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 📦 DTO Types

### 1. Request DTOs (Input)

```java
// Create Request
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRestaurantRequest {
    
    @NotBlank(message = "Restaurant name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2-100 characters")
    private String name;
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
    
    @NotBlank(message = "Cuisine type is required")
    private String cuisineType;
    
    @NotBlank(message = "Address is required")
    private String address;
    
    @NotBlank(message = "City is required")
    private String city;
    
    private String phone;
    
    @Email(message = "Invalid email format")
    private String email;
    
    @DecimalMin(value = "0.0", message = "Minimum order cannot be negative")
    private BigDecimal minimumOrder;
    
    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", 
             message = "Invalid time format (HH:mm)")
    private String openingTime;
    
    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", 
             message = "Invalid time format (HH:mm)")
    private String closingTime;
}

// Update Request (Partial update support)
@Data
public class UpdateRestaurantRequest {
    
    @Size(min = 2, max = 100)
    private String name;  // Optional
    
    private String description;  // Optional
    
    private String cuisineType;  // Optional
    
    private Boolean isOpen;  // Optional
    
    private BigDecimal minimumOrder;  // Optional
}
```

### 2. Response DTOs (Output)

```java
// Basic Response
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantDTO {
    private Long id;
    private String name;
    private String description;
    private String cuisineType;
    private String address;
    private String city;
    private String phone;
    private Double rating;
    private Integer totalReviews;
    private Boolean isOpen;
    private String openingTime;
    private String closingTime;
    private BigDecimal minimumOrder;
    private Integer avgDeliveryTime;
    private LocalDateTime createdAt;
}

// Summary Response (List view)
@Data
@Builder
public class RestaurantSummaryDTO {
    private Long id;
    private String name;
    private String cuisineType;
    private String city;
    private Double rating;
    private Boolean isOpen;
    private Integer avgDeliveryTime;
    private String imageUrl;
}

// Detailed Response (Single view with nested data)
@Data
@Builder
public class RestaurantDetailDTO {
    private Long id;
    private String name;
    private String description;
    private String cuisineType;
    private String address;
    private String city;
    private Double latitude;
    private Double longitude;
    private String phone;
    private String email;
    private Double rating;
    private Integer totalReviews;
    private Boolean isOpen;
    private String openingTime;
    private String closingTime;
    private BigDecimal minimumOrder;
    private Integer deliveryRadiusKm;
    private Integer avgDeliveryTime;
    private List<String> images;
    private List<CategoryDTO> categories;  // Nested
    private OwnerDTO owner;  // Nested
}
```

### 3. Nested DTOs

```java
@Data
@Builder
public class OrderDTO {
    private Long id;
    private String orderNumber;
    private String status;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    
    // Nested DTOs
    private UserSummaryDTO user;
    private RestaurantSummaryDTO restaurant;
    private List<OrderItemDTO> items;
    private DeliveryDTO delivery;
    private PaymentDTO payment;
}

@Data
@Builder
public class OrderItemDTO {
    private Long id;
    private String menuItemName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private String specialInstructions;
    private List<AddonDTO> addons;
}

@Data
@Builder
public class UserSummaryDTO {
    private Long id;
    private String name;
    private String phone;
    // Note: Email, password excluded for security
}
```

---

## 🔄 Mapper Implementation

### Option 1: MapStruct (Recommended)

```java
// Dependency in pom.xml
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>1.5.5.Final</version>
</dependency>
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct-processor</artifactId>
    <version>1.5.5.Final</version>
    <scope>provided</scope>
</dependency>
```

```java
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RestaurantMapper {
    
    // Entity to DTO
    RestaurantDTO toDTO(Restaurant restaurant);
    
    // DTO to Entity
    Restaurant toEntity(CreateRestaurantRequest request);
    
    // List conversion
    List<RestaurantDTO> toDTOList(List<Restaurant> restaurants);
    
    // Summary DTO
    @Mapping(target = "imageUrl", source = "primaryImageUrl")
    RestaurantSummaryDTO toSummaryDTO(Restaurant restaurant);
    
    // Update entity from DTO (partial update)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDTO(UpdateRestaurantRequest request, @MappingTarget Restaurant restaurant);
    
    // Custom mapping
    @Mapping(target = "fullAddress", expression = "java(restaurant.getAddress() + \", \" + restaurant.getCity())")
    @Mapping(target = "isCurrentlyOpen", expression = "java(isRestaurantOpen(restaurant))")
    RestaurantDetailDTO toDetailDTO(Restaurant restaurant);
    
    // Helper method
    default boolean isRestaurantOpen(Restaurant restaurant) {
        if (!restaurant.getIsOpen()) return false;
        LocalTime now = LocalTime.now();
        return now.isAfter(restaurant.getOpeningTime()) && 
               now.isBefore(restaurant.getClosingTime());
    }
}
```

### Option 2: Manual Mapper

```java
@Component
public class RestaurantMapper {
    
    public RestaurantDTO toDTO(Restaurant restaurant) {
        if (restaurant == null) return null;
        
        return RestaurantDTO.builder()
            .id(restaurant.getId())
            .name(restaurant.getName())
            .description(restaurant.getDescription())
            .cuisineType(restaurant.getCuisineType())
            .address(restaurant.getAddress())
            .city(restaurant.getCity())
            .phone(restaurant.getPhone())
            .rating(restaurant.getRating())
            .totalReviews(restaurant.getTotalReviews())
            .isOpen(restaurant.getIsOpen())
            .openingTime(formatTime(restaurant.getOpeningTime()))
            .closingTime(formatTime(restaurant.getClosingTime()))
            .minimumOrder(restaurant.getMinimumOrder())
            .avgDeliveryTime(restaurant.getAvgDeliveryTime())
            .createdAt(restaurant.getCreatedAt())
            .build();
    }
    
    public Restaurant toEntity(CreateRestaurantRequest request) {
        if (request == null) return null;
        
        return Restaurant.builder()
            .name(request.getName())
            .description(request.getDescription())
            .cuisineType(request.getCuisineType())
            .address(request.getAddress())
            .city(request.getCity())
            .phone(request.getPhone())
            .email(request.getEmail())
            .minimumOrder(request.getMinimumOrder())
            .openingTime(parseTime(request.getOpeningTime()))
            .closingTime(parseTime(request.getClosingTime()))
            .isOpen(true)
            .isActive(true)
            .rating(0.0)
            .totalReviews(0)
            .build();
    }
    
    public void updateEntityFromDTO(UpdateRestaurantRequest request, Restaurant restaurant) {
        if (request.getName() != null) {
            restaurant.setName(request.getName());
        }
        if (request.getDescription() != null) {
            restaurant.setDescription(request.getDescription());
        }
        if (request.getCuisineType() != null) {
            restaurant.setCuisineType(request.getCuisineType());
        }
        if (request.getIsOpen() != null) {
            restaurant.setIsOpen(request.getIsOpen());
        }
        if (request.getMinimumOrder() != null) {
            restaurant.setMinimumOrder(request.getMinimumOrder());
        }
    }
    
    public List<RestaurantDTO> toDTOList(List<Restaurant> restaurants) {
        return restaurants.stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }
    
    private String formatTime(LocalTime time) {
        return time != null ? time.format(DateTimeFormatter.ofPattern("HH:mm")) : null;
    }
    
    private LocalTime parseTime(String time) {
        return time != null ? LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm")) : null;
    }
}
```

---

## 🛒 Complex Mapping Examples

### Order Mapper with Nested Objects

```java
@Mapper(componentModel = "spring", 
        uses = {OrderItemMapper.class, UserMapper.class, RestaurantMapper.class})
public interface OrderMapper {
    
    @Mapping(target = "user", source = "userId", qualifiedByName = "mapUser")
    @Mapping(target = "restaurant", source = "restaurantId", qualifiedByName = "mapRestaurant")
    @Mapping(target = "statusDisplay", source = "status", qualifiedByName = "formatStatus")
    OrderDTO toDTO(Order order);
    
    @Named("formatStatus")
    default String formatStatus(OrderStatus status) {
        return switch (status) {
            case PENDING -> "Order Placed";
            case CONFIRMED -> "Order Confirmed";
            case PREPARING -> "Preparing Your Food";
            case READY -> "Ready for Pickup";
            case OUT_FOR_DELIVERY -> "Out for Delivery";
            case DELIVERED -> "Delivered";
            case CANCELLED -> "Cancelled";
        };
    }
}

@Mapper(componentModel = "spring")
public interface OrderItemMapper {
    
    @Mapping(target = "menuItemName", source = "menuItem.name")
    @Mapping(target = "totalPrice", expression = "java(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))")
    OrderItemDTO toDTO(OrderItem item);
    
    List<OrderItemDTO> toDTOList(List<OrderItem> items);
}
```

### Cart Mapper

```java
@Component
@RequiredArgsConstructor
public class CartMapper {
    
    private final MenuClient menuClient;
    
    public CartDTO toDTO(Cart cart) {
        CartDTO dto = CartDTO.builder()
            .id(cart.getId())
            .userId(cart.getUserId())
            .restaurantId(cart.getRestaurantId())
            .totalAmount(cart.getTotalAmount())
            .itemCount(cart.getItems().size())
            .items(mapCartItems(cart.getItems()))
            .build();
        
        // Enrich with restaurant info
        if (cart.getRestaurantId() != null) {
            try {
                RestaurantDTO restaurant = restaurantClient.getRestaurantById(cart.getRestaurantId());
                dto.setRestaurantName(restaurant.getName());
                dto.setRestaurantImage(restaurant.getImageUrl());
            } catch (Exception e) {
                // Log and continue without restaurant info
            }
        }
        
        return dto;
    }
    
    private List<CartItemDTO> mapCartItems(List<CartItem> items) {
        return items.stream()
            .map(this::mapCartItem)
            .collect(Collectors.toList());
    }
    
    private CartItemDTO mapCartItem(CartItem item) {
        return CartItemDTO.builder()
            .id(item.getId())
            .menuItemId(item.getMenuItemId())
            .menuItemName(item.getMenuItemName())
            .quantity(item.getQuantity())
            .unitPrice(item.getUnitPrice())
            .totalPrice(item.getTotalPrice())
            .specialInstructions(item.getSpecialInstructions())
            .addons(mapAddons(item.getAddons()))
            .build();
    }
}
```

---

## 📄 Pagination Response DTO

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;
    private boolean hasNext;
    private boolean hasPrevious;
    
    public static <T> PageResponse<T> from(Page<T> page) {
        return PageResponse.<T>builder()
            .content(page.getContent())
            .pageNumber(page.getNumber())
            .pageSize(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .first(page.isFirst())
            .last(page.isLast())
            .hasNext(page.hasNext())
            .hasPrevious(page.hasPrevious())
            .build();
    }
    
    public static <T, E> PageResponse<T> from(Page<E> page, Function<E, T> mapper) {
        List<T> content = page.getContent().stream()
            .map(mapper)
            .collect(Collectors.toList());
        
        return PageResponse.<T>builder()
            .content(content)
            .pageNumber(page.getNumber())
            .pageSize(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .first(page.isFirst())
            .last(page.isLast())
            .hasNext(page.hasNext())
            .hasPrevious(page.hasPrevious())
            .build();
    }
}

// Usage in Controller
@GetMapping
public PageResponse<RestaurantSummaryDTO> getAllRestaurants(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "rating") String sortBy) {
    
    Page<Restaurant> restaurants = restaurantService.getAllRestaurants(
        PageRequest.of(page, size, Sort.by(sortBy).descending())
    );
    
    return PageResponse.from(restaurants, restaurantMapper::toSummaryDTO);
}
```

---

## 🔒 Security Considerations

### Sensitive Data Exclusion

```java
// Entity (has sensitive data)
@Entity
public class User {
    private Long id;
    private String email;
    private String password;      // Sensitive
    private String firstName;
    private String lastName;
    private String phone;
    private String panNumber;     // Sensitive
    private String aadhaarNumber; // Sensitive
    private LocalDateTime createdAt;
}

// Public DTO (excludes sensitive data)
@Data
@Builder
public class UserDTO {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    // password, panNumber, aadhaarNumber excluded
}

// Admin DTO (includes more data)
@Data
@Builder
public class UserAdminDTO {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String panNumberMasked;  // "XXXXX1234X"
    private Boolean isVerified;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
}
```

### Mapper with Role-based Fields

```java
@Component
public class UserMapper {
    
    public UserDTO toDTO(User user) {
        return UserDTO.builder()
            .id(user.getId())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .phone(maskPhone(user.getPhone()))
            .build();
    }
    
    public UserAdminDTO toAdminDTO(User user) {
        return UserAdminDTO.builder()
            .id(user.getId())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .phone(user.getPhone())
            .panNumberMasked(maskPan(user.getPanNumber()))
            .isVerified(user.getIsVerified())
            .createdAt(user.getCreatedAt())
            .lastLogin(user.getLastLogin())
            .build();
    }
    
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) return phone;
        return "XXXXXX" + phone.substring(phone.length() - 4);
    }
    
    private String maskPan(String pan) {
        if (pan == null || pan.length() != 10) return null;
        return "XXXXX" + pan.substring(5);
    }
}
```

---

## 📊 Interview Questions

### Q1: DTO kyu use karte hain?
**Answer:**
- **Separation of concerns:** Entity aur API response alag
- **Security:** Sensitive fields hide kar sakte hain
- **Flexibility:** API response customize kar sakte hain
- **Versioning:** Different DTOs for different API versions
- **Performance:** Only required fields transfer

### Q2: MapStruct vs Manual Mapping?
**Answer:**
| MapStruct | Manual Mapping |
|-----------|----------------|
| Compile-time code generation | Runtime execution |
| Less boilerplate | More code |
| Type-safe | Error-prone |
| Better performance | Slightly slower |
| Learning curve | Simple to understand |

### Q3: Circular reference kaise handle karte ho?
**Answer:**
```java
// Problem: Order has User, User has Orders
// Solution 1: Different DTOs
@Data
public class OrderDTO {
    private UserSummaryDTO user;  // Summary, not full
}

@Data
public class UserSummaryDTO {
    private Long id;
    private String name;
    // No orders field
}

// Solution 2: @JsonIgnore
@Data
public class UserDTO {
    @JsonIgnore
    private List<OrderDTO> orders;
}

// Solution 3: @JsonManagedReference / @JsonBackReference
```

### Q4: Partial update kaise implement karte ho?
**Answer:**
```java
// Request DTO with optional fields
@Data
public class UpdateRequest {
    private String name;      // null means don't update
    private String email;     // null means don't update
}

// Mapper
@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
void updateEntity(UpdateRequest request, @MappingTarget Entity entity);

// Manual approach
if (request.getName() != null) {
    entity.setName(request.getName());
}
```

### Q5: Nested objects kaise map karte ho?
**Answer:**
```java
// MapStruct - uses other mappers
@Mapper(componentModel = "spring", uses = {ItemMapper.class, UserMapper.class})
public interface OrderMapper {
    OrderDTO toDTO(Order order);
}

// Manual - call other mappers
public OrderDTO toDTO(Order order) {
    return OrderDTO.builder()
        .id(order.getId())
        .items(itemMapper.toDTOList(order.getItems()))
        .user(userMapper.toSummaryDTO(order.getUser()))
        .build();
}
```

---

## ⚠️ Best Practices

1. **Separate Request/Response DTOs** - Don't use same DTO for both
2. **Use Builder pattern** - Cleaner object creation
3. **Validate Request DTOs** - Use Bean Validation annotations
4. **Don't expose entities** - Always use DTOs in controllers
5. **Keep DTOs simple** - No business logic
6. **Use meaningful names** - `CreateOrderRequest`, `OrderSummaryDTO`
7. **Document DTOs** - Use Swagger annotations
