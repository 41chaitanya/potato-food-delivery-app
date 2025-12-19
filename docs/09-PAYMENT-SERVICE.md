# Payment Service (Port: 8082)

## Ye Service Kya Karti Hai?

Payment Service **payment processing** handle karti hai. Ye ek **mock service** hai - real payment gateway (Razorpay, Stripe) integrate nahi hai. Order Service se payment request aati hai aur success/failure response jaata hai.

---

## Features

| Feature | Description |
|---------|-------------|
| Process Payment | Payment request handle |
| Payment Status | SUCCESS, FAILED, PENDING |
| Mock Implementation | Real gateway nahi |
| Payment History | Payment records |

---

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                  Payment Service (8082)                  │
│                                                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │
│  │  Controller  │  │   Service    │  │  Repository  │   │
│  │              │  │              │  │              │   │
│  │/api/payments │→ │PaymentService│→ │ PaymentRepo  │   │
│  └──────────────┘  └──────────────┘  └──────────────┘   │
│                                              │           │
│                                              ▼           │
│                                       ┌──────────────┐   │
│                                       │  PostgreSQL  │   │
│                                       │  payment_db  │   │
│                                       └──────────────┘   │
└─────────────────────────────────────────────────────────┘
                         ▲
                         │
                  Order Service
                  (Circuit Breaker)
```

---

## Tech Stack

| Technology | Purpose |
|------------|---------|
| Spring Data JPA | Database operations |
| PostgreSQL | Payment records storage |

---

## Database Schema

```sql
CREATE TABLE payments (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(20),  -- CARD, UPI, WALLET, COD
    status VARCHAR(20),          -- SUCCESS, FAILED, PENDING
    transaction_id VARCHAR(100),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

### Payment Status
```java
public enum PaymentStatus {
    PENDING,   // Payment initiated
    SUCCESS,   // Payment successful
    FAILED     // Payment failed
}
```

---

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/payments | Process payment |
| GET | /api/payments/{paymentId} | Get payment by ID |
| GET | /api/payments/order/{orderId} | Get payment by order |

---

## Key Flow

### Payment Processing Flow
```
Order Service: POST /api/payments
{
  "orderId": "order-uuid",
  "amount": 794.00,
  "paymentMethod": "CARD"
}
        │
        ▼
1. Validate request
2. Create payment record (status = PENDING)
3. Mock payment processing
   - Random success/failure (for testing)
   - Or always success (for demo)
4. Update payment status
5. Generate transaction ID
        │
        ▼
Response:
{
  "id": "payment-uuid",
  "orderId": "order-uuid",
  "amount": 794.00,
  "status": "SUCCESS",
  "transactionId": "TXN_1703001600_abc123"
}
```

---

## Mock Implementation

```java
@Service
public class PaymentService {

    public PaymentResponse processPayment(PaymentRequest request) {
        // Create payment record
        Payment payment = Payment.builder()
            .orderId(request.getOrderId())
            .amount(request.getAmount())
            .paymentMethod(request.getPaymentMethod())
            .status(PaymentStatus.PENDING)
            .build();
        
        // Mock processing (simulate real gateway)
        boolean success = mockPaymentGateway(request);
        
        if (success) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setTransactionId(generateTransactionId());
        } else {
            payment.setStatus(PaymentStatus.FAILED);
        }
        
        paymentRepository.save(payment);
        return mapToResponse(payment);
    }

    private boolean mockPaymentGateway(PaymentRequest request) {
        // For demo: always success
        // For testing: random success/failure
        return true;
    }

    private String generateTransactionId() {
        return "TXN_" + System.currentTimeMillis() + "_" + 
               UUID.randomUUID().toString().substring(0, 8);
    }
}
```

---

## Real Payment Gateway Integration (Future)

```
Current (Mock):
Order Service → Payment Service → Mock Success

Future (Real):
Order Service → Payment Service → Razorpay/Stripe API
                                        │
                                        ▼
                                  Real Payment
                                        │
                                        ▼
                                  Webhook callback
                                        │
                                        ▼
                                  Update status
```

### Razorpay Integration Example
```java
// Future implementation
public PaymentResponse processPayment(PaymentRequest request) {
    // 1. Create Razorpay order
    RazorpayClient razorpay = new RazorpayClient(keyId, keySecret);
    JSONObject orderRequest = new JSONObject();
    orderRequest.put("amount", request.getAmount() * 100); // paise
    orderRequest.put("currency", "INR");
    
    Order razorpayOrder = razorpay.orders.create(orderRequest);
    
    // 2. Return order ID to frontend
    // 3. Frontend completes payment
    // 4. Webhook receives confirmation
    // 5. Update payment status
}
```

---

## Interview Questions

**Q: Ye mock service kyun hai?**
A: Learning project hai. Real payment gateway (Razorpay, Stripe) integrate karne ke liye merchant account chahiye. Mock se flow samajh aata hai.

**Q: Real integration mein kya different hoga?**
A: 
1. Payment gateway SDK use hoga
2. Webhook endpoint banana padega
3. Signature verification karni padegi
4. Refund handling add karni padegi

**Q: Payment failure kaise handle kiya?**
A: Order Service mein Circuit Breaker hai. Payment fail hone pe order PENDING rehta hai. User retry kar sakta hai.

**Q: Idempotency kaise ensure karoge?**
A: Order ID unique hai. Same order ID se duplicate payment nahi hogi. Transaction ID bhi unique generate hota hai.

**Q: COD (Cash on Delivery) kaise handle karoge?**
A: Payment method = COD. Payment status = PENDING until delivery. Delivery complete pe status = SUCCESS.

---

## Best Practices

1. **Idempotency** - Duplicate payments prevent karo
2. **Transaction ID** - Unique identifier for tracking
3. **Webhook** - Async payment confirmation
4. **Retry Logic** - Temporary failures handle karo
5. **Audit Trail** - All payment attempts log karo
6. **PCI Compliance** - Card data securely handle karo
