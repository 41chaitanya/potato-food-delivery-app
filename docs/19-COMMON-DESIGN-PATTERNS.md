# 19. Common Design Patterns - Complete Guide

## 📋 Overview

Is project mein use hone wale design patterns jo interview mein frequently puche jaate hain.

---

## 🏗️ Creational Patterns

### 1. Builder Pattern

**Use Case:** Complex objects create karna with many optional parameters

```java
// Without Builder - Telescoping Constructor Problem
public class Order {
    public Order(Long userId) { }
    public Order(Long userId, Long restaurantId) { }
    public Order(Long userId, Long restaurantId, String address) { }
    public Order(Long userId, Long restaurantId, String address, BigDecimal amount) { }
    // ... more constructors
}

// With Builder Pattern
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private Long id;
    private Long userId;
    private Long restaurantId;
    private String deliveryAddress;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private List<OrderItem> items;
    private LocalDateTime createdAt;
}

// Usage
Order order = Order.builder()
    .userId(123L)
    .restaurantId(456L)
    .deliveryAddress("123 Main St")
    .totalAmount(new BigDecimal("599.00"))
    .status(OrderStatus.PENDING)
    .items(orderItems)
    .createdAt(LocalDateTime.now())
    .build();
```

**Project mein kahan use hua:**
- All DTOs (RestaurantDTO, OrderDTO, UserDTO)
- Entity classes
- Response objects

---

### 2. Factory Pattern

**Use Case:** Object creation logic encapsulate karna

```java
// Payment Processor Factory
public interface PaymentProcessor {
    PaymentResult process(PaymentRequest request);
}

@Component
public class UPIPaymentProcessor implements PaymentProcessor {
    @Override
    public PaymentResult process(PaymentRequest request) {
        // UPI specific logic
    }
}

@Component
public class CardPaymentProcessor implements PaymentProcessor {
    @Override
    public PaymentResult process(PaymentRequest request) {
        // Card specific logic
    }
}

@Component
public class PaymentProcessorFactory {
    
    private final Map<PaymentMethod, PaymentProcessor> processors;
    
    public PaymentProcessorFactory(
            UPIPaymentProcessor upiProcessor,
            CardPaymentProcessor cardProcessor,
            WalletPaymentProcessor walletProcessor) {
        
        processors = Map.of(
            PaymentMethod.UPI, upiProcessor,
            PaymentMethod.CARD, cardProcessor,
            PaymentMethod.WALLET, walletProcessor
        );
    }
    
    public PaymentProcessor getProcessor(PaymentMethod method) {
        PaymentProcessor processor = processors.get(method);
        if (processor == null) {
            throw new UnsupportedPaymentMethodException(method);
        }
        return processor;
    }
}

// Usage
@Service
public class PaymentService {
    
    @Autowired
    private PaymentProcessorFactory factory;
    
    public PaymentResult processPayment(PaymentRequest request) {
        PaymentProcessor processor = factory.getProcessor(request.getMethod());
        return processor.process(request);
    }
}
```

---

### 3. Singleton Pattern

**Use Case:** Single instance throughout application

```java
// Spring Beans are Singleton by default
@Service  // Singleton scope
public class OrderService {
    // Only one instance created
}

@Component
@Scope("prototype")  // New instance every time
public class OrderValidator {
    // New instance for each injection
}

// Traditional Singleton (without Spring)
public class ConfigurationManager {
    
    private static volatile ConfigurationManager instance;
    private final Properties config;
    
    private ConfigurationManager() {
        config = loadConfiguration();
    }
    
    public static ConfigurationManager getInstance() {
        if (instance == null) {
            synchronized (ConfigurationManager.class) {
                if (instance == null) {
                    instance = new ConfigurationManager();
                }
            }
        }
        return instance;
    }
    
    public String getProperty(String key) {
        return config.getProperty(key);
    }
}
```

---

## 🔧 Structural Patterns

### 4. Adapter Pattern

**Use Case:** Incompatible interfaces ko compatible banana

```java
// External Payment Gateway Response
public class RazorpayResponse {
    private String razorpay_payment_id;
    private String razorpay_order_id;
    private String razorpay_signature;
    private int amount_paid;  // in paise
    private String status;
}

// Our Internal Payment Response
public class PaymentResponse {
    private String paymentId;
    private String orderId;
    private BigDecimal amount;  // in rupees
    private PaymentStatus status;
}

// Adapter
@Component
public class RazorpayAdapter {
    
    public PaymentResponse adapt(RazorpayResponse razorpayResponse) {
        return PaymentResponse.builder()
            .paymentId(razorpayResponse.getRazorpay_payment_id())
            .orderId(razorpayResponse.getRazorpay_order_id())
            .amount(convertPaiseToRupees(razorpayResponse.getAmount_paid()))
            .status(mapStatus(razorpayResponse.getStatus()))
            .build();
    }
    
    private BigDecimal convertPaiseToRupees(int paise) {
        return new BigDecimal(paise).divide(new BigDecimal(100));
    }
    
    private PaymentStatus mapStatus(String razorpayStatus) {
        return switch (razorpayStatus) {
            case "captured" -> PaymentStatus.SUCCESS;
            case "failed" -> PaymentStatus.FAILED;
            case "refunded" -> PaymentStatus.REFUNDED;
            default -> PaymentStatus.PENDING;
        };
    }
}
```

---

### 5. Decorator Pattern

**Use Case:** Object mein dynamically behavior add karna

```java
// Base Interface
public interface NotificationService {
    void send(Notification notification);
}

// Base Implementation
@Component
@Primary
public class EmailNotificationService implements NotificationService {
    @Override
    public void send(Notification notification) {
        // Send email
    }
}

// Decorator - Adds logging
@Component
public class LoggingNotificationDecorator implements NotificationService {
    
    private final NotificationService delegate;
    
    public LoggingNotificationDecorator(
            @Qualifier("emailNotificationService") NotificationService delegate) {
        this.delegate = delegate;
    }
    
    @Override
    public void send(Notification notification) {
        log.info("Sending notification: {}", notification);
        delegate.send(notification);
        log.info("Notification sent successfully");
    }
}

// Decorator - Adds retry
@Component
public class RetryNotificationDecorator implements NotificationService {
    
    private final NotificationService delegate;
    
    @Override
    public void send(Notification notification) {
        int attempts = 0;
        while (attempts < 3) {
            try {
                delegate.send(notification);
                return;
            } catch (Exception e) {
                attempts++;
                if (attempts >= 3) throw e;
            }
        }
    }
}
```

---

### 6. Facade Pattern

**Use Case:** Complex subsystem ko simple interface provide karna

```java
// Complex subsystems
@Service
public class InventoryService {
    public boolean checkAvailability(Long itemId, int quantity) { }
    public void reserveItems(Long itemId, int quantity) { }
    public void releaseItems(Long itemId, int quantity) { }
}

@Service
public class PaymentService {
    public PaymentResult processPayment(PaymentRequest request) { }
    public void refundPayment(String paymentId) { }
}

@Service
public class NotificationService {
    public void sendOrderConfirmation(Order order) { }
    public void sendPaymentReceipt(Payment payment) { }
}

@Service
public class DeliveryService {
    public void scheduleDelivery(Order order) { }
}

// Facade - Simplifies order placement
@Service
@RequiredArgsConstructor
public class OrderFacade {
    
    private final InventoryService inventoryService;
    private final PaymentService paymentService;
    private final NotificationService notificationService;
    private final DeliveryService deliveryService;
    private final OrderRepository orderRepository;
    
    @Transactional
    public OrderResult placeOrder(OrderRequest request) {
        // 1. Check inventory
        for (OrderItem item : request.getItems()) {
            if (!inventoryService.checkAvailability(item.getItemId(), item.getQuantity())) {
                throw new InsufficientInventoryException(item.getItemId());
            }
        }
        
        // 2. Reserve items
        for (OrderItem item : request.getItems()) {
            inventoryService.reserveItems(item.getItemId(), item.getQuantity());
        }
        
        // 3. Create order
        Order order = createOrder(request);
        
        // 4. Process payment
        PaymentResult paymentResult = paymentService.processPayment(
            PaymentRequest.from(order)
        );
        
        if (!paymentResult.isSuccess()) {
            // Rollback inventory
            for (OrderItem item : request.getItems()) {
                inventoryService.releaseItems(item.getItemId(), item.getQuantity());
            }
            throw new PaymentFailedException(paymentResult.getMessage());
        }
        
        // 5. Update order status
        order.setStatus(OrderStatus.CONFIRMED);
        order.setPaymentId(paymentResult.getPaymentId());
        orderRepository.save(order);
        
        // 6. Schedule delivery
        deliveryService.scheduleDelivery(order);
        
        // 7. Send notifications
        notificationService.sendOrderConfirmation(order);
        notificationService.sendPaymentReceipt(paymentResult.getPayment());
        
        return OrderResult.success(order);
    }
}

// Client code is simple
@RestController
public class OrderController {
    
    @Autowired
    private OrderFacade orderFacade;
    
    @PostMapping("/orders")
    public OrderResult placeOrder(@RequestBody OrderRequest request) {
        return orderFacade.placeOrder(request);  // One simple call
    }
}
```

---

## 🔄 Behavioral Patterns

### 7. Strategy Pattern

**Use Case:** Algorithm ko runtime pe select karna

```java
// Strategy Interface
public interface DeliveryFeeStrategy {
    BigDecimal calculate(Order order);
}

// Concrete Strategies
@Component("standardDelivery")
public class StandardDeliveryStrategy implements DeliveryFeeStrategy {
    @Override
    public BigDecimal calculate(Order order) {
        // Base fee + distance based
        return new BigDecimal("40.00")
            .add(order.getDistance().multiply(new BigDecimal("5.00")));
    }
}

@Component("expressDelivery")
public class ExpressDeliveryStrategy implements DeliveryFeeStrategy {
    @Override
    public BigDecimal calculate(Order order) {
        // Higher fee for faster delivery
        return new BigDecimal("80.00")
            .add(order.getDistance().multiply(new BigDecimal("10.00")));
    }
}

@Component("freeDelivery")
public class FreeDeliveryStrategy implements DeliveryFeeStrategy {
    @Override
    public BigDecimal calculate(Order order) {
        return BigDecimal.ZERO;
    }
}

// Context
@Service
public class DeliveryFeeCalculator {
    
    private final Map<DeliveryType, DeliveryFeeStrategy> strategies;
    
    public DeliveryFeeCalculator(
            @Qualifier("standardDelivery") DeliveryFeeStrategy standard,
            @Qualifier("expressDelivery") DeliveryFeeStrategy express,
            @Qualifier("freeDelivery") DeliveryFeeStrategy free) {
        
        strategies = Map.of(
            DeliveryType.STANDARD, standard,
            DeliveryType.EXPRESS, express,
            DeliveryType.FREE, free
        );
    }
    
    public BigDecimal calculateFee(Order order, DeliveryType type) {
        DeliveryFeeStrategy strategy = strategies.get(type);
        return strategy.calculate(order);
    }
}
```

---

### 8. Observer Pattern (Event-Driven)

**Use Case:** State change pe multiple objects ko notify karna

```java
// Event
public class OrderPlacedEvent {
    private final Order order;
    private final LocalDateTime timestamp;
    
    public OrderPlacedEvent(Order order) {
        this.order = order;
        this.timestamp = LocalDateTime.now();
    }
}

// Publisher
@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final ApplicationEventPublisher eventPublisher;
    
    public Order createOrder(CreateOrderRequest request) {
        Order order = processOrder(request);
        
        // Publish event
        eventPublisher.publishEvent(new OrderPlacedEvent(order));
        
        return order;
    }
}

// Observers (Listeners)
@Component
public class InventoryEventListener {
    
    @EventListener
    public void handleOrderPlaced(OrderPlacedEvent event) {
        // Update inventory
        log.info("Updating inventory for order: {}", event.getOrder().getId());
    }
}

@Component
public class NotificationEventListener {
    
    @EventListener
    @Async  // Non-blocking
    public void handleOrderPlaced(OrderPlacedEvent event) {
        // Send notification
        log.info("Sending notification for order: {}", event.getOrder().getId());
    }
}

@Component
public class AnalyticsEventListener {
    
    @EventListener
    @Async
    public void handleOrderPlaced(OrderPlacedEvent event) {
        // Track analytics
        log.info("Tracking analytics for order: {}", event.getOrder().getId());
    }
}
```

---

### 9. Template Method Pattern

**Use Case:** Algorithm ka skeleton define karna, steps subclass implement kare

```java
// Abstract Template
public abstract class OrderProcessor {
    
    // Template method - defines the algorithm
    public final OrderResult process(Order order) {
        validateOrder(order);
        
        if (!checkInventory(order)) {
            return OrderResult.failed("Insufficient inventory");
        }
        
        reserveInventory(order);
        
        PaymentResult payment = processPayment(order);
        if (!payment.isSuccess()) {
            releaseInventory(order);
            return OrderResult.failed("Payment failed");
        }
        
        finalizeOrder(order);
        sendNotification(order);
        
        return OrderResult.success(order);
    }
    
    // Common implementation
    protected void validateOrder(Order order) {
        if (order.getItems().isEmpty()) {
            throw new InvalidOrderException("Order has no items");
        }
    }
    
    // Abstract methods - subclass must implement
    protected abstract boolean checkInventory(Order order);
    protected abstract void reserveInventory(Order order);
    protected abstract PaymentResult processPayment(Order order);
    protected abstract void releaseInventory(Order order);
    
    // Hook methods - optional override
    protected void finalizeOrder(Order order) {
        order.setStatus(OrderStatus.CONFIRMED);
    }
    
    protected void sendNotification(Order order) {
        // Default: do nothing
    }
}

// Concrete Implementation
@Service
public class FoodOrderProcessor extends OrderProcessor {
    
    @Autowired
    private InventoryService inventoryService;
    
    @Autowired
    private PaymentService paymentService;
    
    @Override
    protected boolean checkInventory(Order order) {
        return inventoryService.checkAvailability(order.getItems());
    }
    
    @Override
    protected void reserveInventory(Order order) {
        inventoryService.reserve(order.getItems());
    }
    
    @Override
    protected PaymentResult processPayment(Order order) {
        return paymentService.process(order.getPaymentDetails());
    }
    
    @Override
    protected void releaseInventory(Order order) {
        inventoryService.release(order.getItems());
    }
    
    @Override
    protected void sendNotification(Order order) {
        // Send SMS and Email
        notificationService.sendOrderConfirmation(order);
    }
}
```

---

### 10. Chain of Responsibility Pattern

**Use Case:** Request ko chain of handlers se pass karna

```java
// Handler Interface
public interface OrderValidator {
    void setNext(OrderValidator next);
    void validate(Order order);
}

// Abstract Handler
public abstract class AbstractOrderValidator implements OrderValidator {
    
    protected OrderValidator next;
    
    @Override
    public void setNext(OrderValidator next) {
        this.next = next;
    }
    
    protected void validateNext(Order order) {
        if (next != null) {
            next.validate(order);
        }
    }
}

// Concrete Handlers
@Component
public class ItemAvailabilityValidator extends AbstractOrderValidator {
    
    @Override
    public void validate(Order order) {
        for (OrderItem item : order.getItems()) {
            if (!isAvailable(item)) {
                throw new ValidationException("Item not available: " + item.getName());
            }
        }
        validateNext(order);
    }
}

@Component
public class MinimumOrderValidator extends AbstractOrderValidator {
    
    @Override
    public void validate(Order order) {
        if (order.getTotalAmount().compareTo(new BigDecimal("100")) < 0) {
            throw new ValidationException("Minimum order amount is ₹100");
        }
        validateNext(order);
    }
}

@Component
public class DeliveryAddressValidator extends AbstractOrderValidator {
    
    @Override
    public void validate(Order order) {
        if (order.getDeliveryAddress() == null || order.getDeliveryAddress().isEmpty()) {
            throw new ValidationException("Delivery address is required");
        }
        validateNext(order);
    }
}

@Component
public class RestaurantOpenValidator extends AbstractOrderValidator {
    
    @Override
    public void validate(Order order) {
        if (!isRestaurantOpen(order.getRestaurantId())) {
            throw new ValidationException("Restaurant is currently closed");
        }
        validateNext(order);
    }
}

// Chain Configuration
@Configuration
public class ValidatorChainConfig {
    
    @Bean
    public OrderValidator orderValidatorChain(
            ItemAvailabilityValidator itemValidator,
            MinimumOrderValidator minOrderValidator,
            DeliveryAddressValidator addressValidator,
            RestaurantOpenValidator restaurantValidator) {
        
        // Build chain
        itemValidator.setNext(minOrderValidator);
        minOrderValidator.setNext(addressValidator);
        addressValidator.setNext(restaurantValidator);
        
        return itemValidator;  // Return first in chain
    }
}

// Usage
@Service
public class OrderService {
    
    @Autowired
    @Qualifier("orderValidatorChain")
    private OrderValidator validator;
    
    public Order createOrder(CreateOrderRequest request) {
        Order order = mapToOrder(request);
        
        // Validate through chain
        validator.validate(order);
        
        // If we reach here, all validations passed
        return orderRepository.save(order);
    }
}
```

---

## 🏛️ Architectural Patterns

### 11. Repository Pattern

**Use Case:** Data access logic ko abstract karna

```java
// Repository Interface
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    // Spring Data JPA generates implementation
    List<Order> findByUserId(Long userId);
    
    List<Order> findByStatus(OrderStatus status);
    
    @Query("SELECT o FROM Order o WHERE o.restaurantId = :restaurantId AND o.createdAt >= :date")
    List<Order> findRecentOrdersByRestaurant(
        @Param("restaurantId") Long restaurantId,
        @Param("date") LocalDateTime date
    );
    
    @Query("SELECT o FROM Order o WHERE o.userId = :userId ORDER BY o.createdAt DESC")
    Page<Order> findUserOrdersWithPagination(
        @Param("userId") Long userId, 
        Pageable pageable
    );
}

// Custom Repository for complex queries
public interface OrderRepositoryCustom {
    List<Order> findOrdersWithFilters(OrderSearchCriteria criteria);
}

@Repository
public class OrderRepositoryCustomImpl implements OrderRepositoryCustom {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Override
    public List<Order> findOrdersWithFilters(OrderSearchCriteria criteria) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Order> query = cb.createQuery(Order.class);
        Root<Order> order = query.from(Order.class);
        
        List<Predicate> predicates = new ArrayList<>();
        
        if (criteria.getUserId() != null) {
            predicates.add(cb.equal(order.get("userId"), criteria.getUserId()));
        }
        if (criteria.getStatus() != null) {
            predicates.add(cb.equal(order.get("status"), criteria.getStatus()));
        }
        if (criteria.getFromDate() != null) {
            predicates.add(cb.greaterThanOrEqualTo(order.get("createdAt"), criteria.getFromDate()));
        }
        
        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.desc(order.get("createdAt")));
        
        return entityManager.createQuery(query).getResultList();
    }
}
```

---

### 12. Service Layer Pattern

**Use Case:** Business logic ko separate layer mein organize karna

```java
// Controller - Only handles HTTP
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    
    private final OrderService orderService;
    
    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderDTO order = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }
}

// Service - Contains business logic
@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final RestaurantClient restaurantClient;
    private final OrderMapper orderMapper;
    
    public OrderDTO createOrder(CreateOrderRequest request) {
        // Business logic here
        validateRestaurant(request.getRestaurantId());
        Cart cart = cartService.getCartByUserId(request.getUserId());
        validateCart(cart);
        
        Order order = buildOrder(request, cart);
        order = orderRepository.save(order);
        
        cartService.clearCart(request.getUserId());
        
        return orderMapper.toDTO(order);
    }
    
    public OrderDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        return orderMapper.toDTO(order);
    }
    
    private void validateRestaurant(Long restaurantId) {
        RestaurantDTO restaurant = restaurantClient.getRestaurantById(restaurantId);
        if (!restaurant.getIsOpen()) {
            throw new BusinessException("Restaurant is closed");
        }
    }
}

// Repository - Data access only
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    // Data access methods
}
```

---

## 📊 Interview Questions on Patterns

### Q1: Builder Pattern kab use karte ho?
**Answer:**
- Object mein bahut saare parameters hon
- Optional parameters hon
- Immutable objects banana ho
- Telescoping constructor problem avoid karna ho

### Q2: Factory vs Abstract Factory?
**Answer:**
| Factory | Abstract Factory |
|---------|------------------|
| Single product | Family of products |
| One create method | Multiple create methods |
| Simple | Complex |

### Q3: Strategy vs Template Method?
**Answer:**
| Strategy | Template Method |
|----------|-----------------|
| Composition | Inheritance |
| Runtime selection | Compile-time |
| Entire algorithm changes | Steps change |
| Interface-based | Abstract class |

### Q4: Observer Pattern real-world example?
**Answer:**
- Order placed → Notify inventory, payment, delivery
- User registered → Send welcome email, create profile
- Payment received → Update order, send receipt

### Q5: Singleton ke problems?
**Answer:**
- Testing difficult (mocking)
- Hidden dependencies
- Global state
- Thread safety issues
- Violates Single Responsibility

**Solution:** Use Spring's dependency injection instead of manual singleton.
