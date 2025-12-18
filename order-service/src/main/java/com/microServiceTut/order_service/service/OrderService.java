package com.microServiceTut.order_service.service;

import com.microServiceTut.order_service.client.PaymentClient;
import com.microServiceTut.order_service.dto.*;
import com.microServiceTut.order_service.model.Order;
import com.microServiceTut.order_service.model.OrderStatus;
import com.microServiceTut.order_service.model.PaymentStatus;
import com.microServiceTut.order_service.repository.OrderRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentClient paymentClient;

    @CircuitBreaker(name = "paymentCB", fallbackMethod = "paymentFallback")
    public OrderResponse createOrder(CreateOrderRequest createOrderRequest) {

        // 1. Create order
        Order order = new Order();
        order.setUserId(createOrderRequest.getUserId());
        order.setCustomerName(createOrderRequest.getCustomerName());
        order.setRestaurantName(createOrderRequest.getRestaurantName());
        order.setTotalAmount(createOrderRequest.getTotalAmount());
        order.setStatus(OrderStatus.PAYMENT_PENDING);
        order.setCreatedAt(LocalDateTime.now());

        // 2. Save order FIRST (ID generated here)
        Order savedOrder = orderRepository.save(order);

        // 3. Call payment service

            PaymentResponse paymentResponse =
                    paymentClient.makePayment(
                            new PaymentRequest(
                                    savedOrder.getId(),
                                    savedOrder.getTotalAmount()
                            )
                    );

            if (PaymentStatus.SUCCESS.equals(paymentResponse.getStatus())) {
                savedOrder.setStatus(OrderStatus.PAID);
            } else {
                savedOrder.setStatus(OrderStatus.PAYMENT_FAILED);
            }


        // 4. Save UPDATED status
        orderRepository.save(savedOrder);

        // 5. Return response
        return new OrderResponse(
                savedOrder.getId(),
                savedOrder.getStatus(),
                savedOrder.getTotalAmount()
        );
    }
//    fall back
public OrderResponse paymentFallback(
        CreateOrderRequest request,
        Throwable ex) {

    System.out.println("🔥 FALLBACK TRIGGERED due to: " + ex.getMessage());

    Order order = new Order();
    order.setCustomerName(request.getCustomerName());
    order.setRestaurantName(request.getRestaurantName());
    order.setTotalAmount(request.getTotalAmount());
    order.setStatus(OrderStatus.PAYMENT_PENDING);
    order.setCreatedAt(LocalDateTime.now());

    Order savedOrder = orderRepository.save(order);

    return new OrderResponse(
            savedOrder.getId(),
            savedOrder.getStatus(),
            savedOrder.getTotalAmount()
    );
}

    /**
     * Internal API for Delivery Service.
     * Returns minimal order info needed for delivery assignment.
     */
    public OrderBasicResponse getOrderBasic(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found: " + orderId));

        return new OrderBasicResponse(
                order.getId(),
                order.getStatus().name(),
                null  // restaurantId not available in current schema
        );
    }

    /**
     * Update order status
     */
    public OrderBasicResponse updateOrderStatus(UUID orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found: " + orderId));

        order.setStatus(OrderStatus.valueOf(status));
        orderRepository.save(order);

        return new OrderBasicResponse(
                order.getId(),
                order.getStatus().name(),
                null
        );
    }

    /**
     * Get order by ID
     */
    public OrderDetailResponse getOrderById(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found: " + orderId));

        return toDetailResponse(order);
    }

    /**
     * Get order history for user
     */
    public List<OrderDetailResponse> getOrderHistory(UUID userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDetailResponse)
                .toList();
    }

    /**
     * Cancel order
     */
    public OrderDetailResponse cancelOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found: " + orderId));

        // Can only cancel if not delivered
        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot cancel delivered order");
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order saved = orderRepository.save(order);

        return toDetailResponse(saved);
    }

    private OrderDetailResponse toDetailResponse(Order order) {
        return OrderDetailResponse.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .customerName(order.getCustomerName())
                .restaurantName(order.getRestaurantName())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    /**
     * Get order stats for admin
     */
    public OrderStatsResponse getOrderStats() {
        long total = orderRepository.count();
        long completed = orderRepository.countByStatus(OrderStatus.DELIVERED);
        long cancelled = orderRepository.countByStatus(OrderStatus.CANCELLED);
        long pending = orderRepository.countByStatus(OrderStatus.PAYMENT_PENDING);
        double revenue = orderRepository.sumTotalRevenueByStatus(OrderStatus.DELIVERED);
        return new OrderStatsResponse(total, completed, cancelled, pending, revenue);
    }

    public record OrderStatsResponse(long totalOrders, long completedOrders, long cancelledOrders,
                                     long pendingOrders, double totalRevenue) {}
}
