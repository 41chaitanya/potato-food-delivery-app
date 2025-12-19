# 15. Exception Handling - Complete Guide

## 🎯 Overview

Centralized exception handling using `@ControllerAdvice` for consistent error responses across all microservices.

---

## 🏗️ Exception Handling Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         CLIENT REQUEST                                   │
└─────────────────────────────────┬───────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                           CONTROLLER                                     │
│                    (Throws Exception)                                    │
└─────────────────────────────────┬───────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                    GLOBAL EXCEPTION HANDLER                              │
│                     (@ControllerAdvice)                                  │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │  - Catches all exceptions                                        │    │
│  │  - Maps to appropriate HTTP status                               │    │
│  │  - Creates standardized error response                           │    │
│  │  - Logs error details                                            │    │
│  └─────────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────┬───────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                      STANDARDIZED ERROR RESPONSE                         │
│  {                                                                       │
│    "timestamp": "2024-12-19T10:30:00",                                   │
│    "status": 404,                                                        │
│    "error": "Not Found",                                                 │
│    "message": "Restaurant not found with id: 123",                       │
│    "path": "/api/restaurants/123"                                        │
│  }                                                                       │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 📋 Custom Exception Classes

### Base Exception
```java
public abstract class BaseException extends RuntimeException {
    private final String errorCode;
    private final HttpStatus status;
    
    public BaseException(String message, String errorCode, HttpStatus status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }
    
    // Getters
}
```

### Resource Not Found Exception
```java
public class ResourceNotFoundException extends BaseException {
    
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(
            String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue),
            "RESOURCE_NOT_FOUND",
            HttpStatus.NOT_FOUND
        );
    }
    
    public ResourceNotFoundException(String message) {
        super(message, "RESOURCE_NOT_FOUND", HttpStatus.NOT_FOUND);
    }
}

// Usage
throw new ResourceNotFoundException("Restaurant", "id", restaurantId);
// Message: "Restaurant not found with id: '123'"
```

### Bad Request Exception
```java
public class BadRequestException extends BaseException {
    
    public BadRequestException(String message) {
        super(message, "BAD_REQUEST", HttpStatus.BAD_REQUEST);
    }
}

// Usage
throw new BadRequestException("Invalid order status transition");
```

### Duplicate Resource Exception
```java
public class DuplicateResourceException extends BaseException {
    
    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
        super(
            String.format("%s already exists with %s: '%s'", resourceName, fieldName, fieldValue),
            "DUPLICATE_RESOURCE",
            HttpStatus.CONFLICT
        );
    }
}

// Usage
throw new DuplicateResourceException("User", "email", email);
```

### Unauthorized Exception
```java
public class UnauthorizedException extends BaseException {
    
    public UnauthorizedException(String message) {
        super(message, "UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
    }
    
    public UnauthorizedException() {
        super("Authentication required", "UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
    }
}
```

### Forbidden Exception
```java
public class ForbiddenException extends BaseException {
    
    public ForbiddenException(String message) {
        super(message, "FORBIDDEN", HttpStatus.FORBIDDEN);
    }
    
    public ForbiddenException() {
        super("Access denied", "FORBIDDEN", HttpStatus.FORBIDDEN);
    }
}
```

### Business Logic Exception
```java
public class BusinessException extends BaseException {
    
    public BusinessException(String message) {
        super(message, "BUSINESS_ERROR", HttpStatus.UNPROCESSABLE_ENTITY);
    }
}

// Usage
throw new BusinessException("Cannot cancel order after it's been delivered");
throw new BusinessException("Insufficient balance for payment");
throw new BusinessException("Restaurant is currently closed");
```

### Service Unavailable Exception
```java
public class ServiceUnavailableException extends BaseException {
    
    public ServiceUnavailableException(String serviceName) {
        super(
            String.format("%s is currently unavailable", serviceName),
            "SERVICE_UNAVAILABLE",
            HttpStatus.SERVICE_UNAVAILABLE
        );
    }
}

// Usage (Feign fallback)
throw new ServiceUnavailableException("Restaurant Service");
```

---

## 📝 Error Response DTO

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String errorCode;
    private String message;
    private String path;
    private List<FieldError> fieldErrors;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldError {
        private String field;
        private String message;
        private Object rejectedValue;
    }
}
```

---

## 🎛️ Global Exception Handler

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    // Handle Custom Base Exceptions
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(
            BaseException ex, WebRequest request) {
        
        log.error("BaseException: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(ex.getStatus().value())
            .error(ex.getStatus().getReasonPhrase())
            .errorCode(ex.getErrorCode())
            .message(ex.getMessage())
            .path(extractPath(request))
            .build();
        
        return new ResponseEntity<>(error, ex.getStatus());
    }
    
    // Handle Resource Not Found
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, WebRequest request) {
        
        log.error("Resource not found: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.NOT_FOUND.value())
            .error("Not Found")
            .errorCode("RESOURCE_NOT_FOUND")
            .message(ex.getMessage())
            .path(extractPath(request))
            .build();
        
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
    
    // Handle Validation Errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> ErrorResponse.FieldError.builder()
                .field(error.getField())
                .message(error.getDefaultMessage())
                .rejectedValue(error.getRejectedValue())
                .build())
            .collect(Collectors.toList());
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Validation Failed")
            .errorCode("VALIDATION_ERROR")
            .message("Input validation failed")
            .path(extractPath(request))
            .fieldErrors(fieldErrors)
            .build();
        
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    
    // Handle Constraint Violation (Bean Validation)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, WebRequest request) {
        
        List<ErrorResponse.FieldError> fieldErrors = ex.getConstraintViolations()
            .stream()
            .map(violation -> ErrorResponse.FieldError.builder()
                .field(violation.getPropertyPath().toString())
                .message(violation.getMessage())
                .rejectedValue(violation.getInvalidValue())
                .build())
            .collect(Collectors.toList());
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Constraint Violation")
            .errorCode("CONSTRAINT_VIOLATION")
            .message("Validation constraints violated")
            .path(extractPath(request))
            .fieldErrors(fieldErrors)
            .build();
        
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    
    // Handle Data Integrity Violation (Duplicate entries, FK violations)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, WebRequest request) {
        
        String message = "Database constraint violation";
        if (ex.getCause() instanceof ConstraintViolationException) {
            message = "Duplicate entry or constraint violation";
        }
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.CONFLICT.value())
            .error("Data Integrity Violation")
            .errorCode("DATA_INTEGRITY_ERROR")
            .message(message)
            .path(extractPath(request))
            .build();
        
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }
    
    // Handle Feign Client Exceptions
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponse> handleFeignException(
            FeignException ex, WebRequest request) {
        
        log.error("Feign client error: {}", ex.getMessage());
        
        HttpStatus status = HttpStatus.valueOf(ex.status());
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(status.value())
            .error("Service Communication Error")
            .errorCode("FEIGN_ERROR")
            .message("Error communicating with downstream service")
            .path(extractPath(request))
            .build();
        
        return new ResponseEntity<>(error, status);
    }
    
    // Handle Access Denied
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex, WebRequest request) {
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.FORBIDDEN.value())
            .error("Forbidden")
            .errorCode("ACCESS_DENIED")
            .message("You don't have permission to access this resource")
            .path(extractPath(request))
            .build();
        
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }
    
    // Handle Authentication Exceptions
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex, WebRequest request) {
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.UNAUTHORIZED.value())
            .error("Unauthorized")
            .errorCode("AUTHENTICATION_FAILED")
            .message(ex.getMessage())
            .path(extractPath(request))
            .build();
        
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }
    
    // Handle HTTP Message Not Readable (Invalid JSON)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, WebRequest request) {
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Bad Request")
            .errorCode("INVALID_JSON")
            .message("Invalid JSON format in request body")
            .path(extractPath(request))
            .build();
        
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    
    // Handle Method Not Allowed
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException ex, WebRequest request) {
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.METHOD_NOT_ALLOWED.value())
            .error("Method Not Allowed")
            .errorCode("METHOD_NOT_ALLOWED")
            .message(String.format("Method '%s' is not supported for this endpoint", 
                ex.getMethod()))
            .path(extractPath(request))
            .build();
        
        return new ResponseEntity<>(error, HttpStatus.METHOD_NOT_ALLOWED);
    }
    
    // Handle All Other Exceptions (Fallback)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(
            Exception ex, WebRequest request) {
        
        log.error("Unexpected error occurred", ex);
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error("Internal Server Error")
            .errorCode("INTERNAL_ERROR")
            .message("An unexpected error occurred. Please try again later.")
            .path(extractPath(request))
            .build();
        
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    private String extractPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
}
```

---

## 📊 HTTP Status Code Mapping

| Exception Type | HTTP Status | Error Code |
|----------------|-------------|------------|
| ResourceNotFoundException | 404 | RESOURCE_NOT_FOUND |
| BadRequestException | 400 | BAD_REQUEST |
| DuplicateResourceException | 409 | DUPLICATE_RESOURCE |
| UnauthorizedException | 401 | UNAUTHORIZED |
| ForbiddenException | 403 | FORBIDDEN |
| BusinessException | 422 | BUSINESS_ERROR |
| ServiceUnavailableException | 503 | SERVICE_UNAVAILABLE |
| MethodArgumentNotValidException | 400 | VALIDATION_ERROR |
| DataIntegrityViolationException | 409 | DATA_INTEGRITY_ERROR |
| Exception (fallback) | 500 | INTERNAL_ERROR |

---

## 🔄 Service Layer Exception Usage

```java
@Service
@Slf4j
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private RestaurantClient restaurantClient;
    
    public OrderDTO getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        
        return orderMapper.toDTO(order);
    }
    
    public OrderDTO createOrder(CreateOrderRequest request) {
        // Validate restaurant exists
        try {
            RestaurantDTO restaurant = restaurantClient.getRestaurantById(request.getRestaurantId());
            if (!restaurant.getIsOpen()) {
                throw new BusinessException("Restaurant is currently closed");
            }
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Restaurant", "id", request.getRestaurantId());
        } catch (FeignException e) {
            throw new ServiceUnavailableException("Restaurant Service");
        }
        
        // Validate minimum order
        if (request.getTotalAmount().compareTo(restaurant.getMinimumOrder()) < 0) {
            throw new BusinessException(
                String.format("Minimum order amount is ₹%.2f", restaurant.getMinimumOrder())
            );
        }
        
        // Create order...
    }
    
    public OrderDTO updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        
        // Validate status transition
        if (!isValidStatusTransition(order.getStatus(), newStatus)) {
            throw new BadRequestException(
                String.format("Cannot change order status from %s to %s", 
                    order.getStatus(), newStatus)
            );
        }
        
        order.setStatus(newStatus);
        return orderMapper.toDTO(orderRepository.save(order));
    }
    
    public void cancelOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        
        // Check ownership
        if (!order.getUserId().equals(userId)) {
            throw new ForbiddenException("You can only cancel your own orders");
        }
        
        // Check if cancellable
        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new BusinessException("Cannot cancel a delivered order");
        }
        
        if (order.getStatus() == OrderStatus.OUT_FOR_DELIVERY) {
            throw new BusinessException("Cannot cancel order that is out for delivery");
        }
        
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }
}
```

---

## 📊 Interview Questions

### Q1: @ControllerAdvice kya karta hai?
**Answer:**
- Global exception handling provide karta hai
- Saare controllers ke exceptions ek jagah handle hote hain
- Consistent error response format milta hai
- Code duplication kam hoti hai

### Q2: @ExceptionHandler vs @ControllerAdvice?
**Answer:**
| @ExceptionHandler | @ControllerAdvice |
|-------------------|-------------------|
| Single controller | All controllers |
| Local scope | Global scope |
| Controller mein define | Separate class |
| Specific handling | Centralized handling |

### Q3: Custom Exception kyu banate hain?
**Answer:**
- **Meaningful errors:** Generic exceptions se better
- **HTTP status mapping:** Automatic status code
- **Error codes:** Client-side handling easy
- **Logging:** Better debugging

### Q4: Validation errors kaise handle karte ho?
**Answer:**
```java
// DTO with validation
public class CreateOrderRequest {
    @NotNull(message = "Restaurant ID is required")
    private Long restaurantId;
    
    @NotEmpty(message = "Order items cannot be empty")
    private List<OrderItemRequest> items;
    
    @Min(value = 1, message = "Total amount must be positive")
    private BigDecimal totalAmount;
}

// Controller
@PostMapping
public OrderDTO createOrder(@Valid @RequestBody CreateOrderRequest request) {
    // If validation fails, MethodArgumentNotValidException thrown
    // GlobalExceptionHandler catches and returns proper error response
}
```

### Q5: Feign exceptions kaise handle karte ho?
**Answer:**
```java
// Option 1: In Service Layer
try {
    restaurantClient.getRestaurantById(id);
} catch (FeignException.NotFound e) {
    throw new ResourceNotFoundException("Restaurant", "id", id);
} catch (FeignException e) {
    throw new ServiceUnavailableException("Restaurant Service");
}

// Option 2: Feign Error Decoder
@Component
public class CustomFeignErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String methodKey, Response response) {
        switch (response.status()) {
            case 404:
                return new ResourceNotFoundException("Resource not found");
            case 503:
                return new ServiceUnavailableException("Service unavailable");
            default:
                return new Exception("Unknown error");
        }
    }
}
```

---

## ⚠️ Best Practices

1. **Never expose stack traces** to clients in production
2. **Log all exceptions** with proper context
3. **Use specific exceptions** instead of generic ones
4. **Include error codes** for client-side handling
5. **Validate early** - fail fast principle
6. **Document error responses** in API documentation
