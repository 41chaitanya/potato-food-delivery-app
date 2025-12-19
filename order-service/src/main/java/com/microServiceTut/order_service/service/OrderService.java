package com.microServiceTut.order_service.service;

import com.microServiceTut.order_service.client.PaymentClient;
import com.microServiceTut.order_service.dto.*;
import com.microServiceTut.order_service.exception.InvalidOrderStateException;
import com.microServiceTut.order_service.exception.OrderNotFoundException;
import com.microServiceTut.order_service.model.Order;
import com.microServiceTut.order_service.model.OrderStatus;
import com.microServiceTut.order_service.model.PaymentStatus;
import com.microServiceTut.order_service.repository.OrderRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final PaymentClient paymentClient;

    @CircuitBreaker(name = "paymentCB", fallbackMethod = "paymentFallback")
    public OrderResponse createOrder(CreateOrderRequest createOrderRequest) {
        log.info("Creating order for user: {}, restaurant: {}", 
                createOrderRequest.getUserId(), createOrderRequest.getRestaurantName());

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
        log.debug("Order saved with ID: {}", savedOrder.getId());

        // 3. Call payment service
        PaymentResponse paymentResponse = paymentClient.makePayment(
                new PaymentRequest(savedOrder.getId(), savedOrder.getTotalAmount())
        );

        if (PaymentStatus.SUCCESS.equals(paymentResponse.getStatus())) {
            savedOrder.setStatus(OrderStatus.PAID);
            log.info("Order {} payment successful", savedOrder.getId());
        } else {
            savedOrder.setStatus(OrderStatus.PAYMENT_FAILED);
            log.warn("Order {} payment failed", savedOrder.getId());
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
    /**
     * Fallback method when payment service is unavailable
     */
    public OrderResponse paymentFallback(CreateOrderRequest request, Throwable ex) {
        log.warn("Payment service unavailable, order saved with PAYMENT_PENDING status. Reason: {}", ex.getMessage());

        Order order = new Order();
        order.setUserId(request.getUserId());
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
        log.debug("Fetching basic order info for orderId: {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));

        return new OrderBasicResponse(
                order.getId(),
                order.getStatus().name(),
                null
        );
    }

    /**
     * Update order status
     */
    public OrderBasicResponse updateOrderStatus(UUID orderId, String status) {
        log.info("Updating order {} status to {}", orderId, status);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));

        try {
            order.setStatus(OrderStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new InvalidOrderStateException("Invalid order status: " + status);
        }
        
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
        log.debug("Fetching order details for orderId: {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));

        return toDetailResponse(order);
    }

    /**
     * Get order history for user
     */
    public List<OrderDetailResponse> getOrderHistory(UUID userId) {
        log.debug("Fetching order history for userId: {}", userId);
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDetailResponse)
                .toList();
    }

    /**
     * Cancel order
     */
    public OrderDetailResponse cancelOrder(UUID orderId) {
        log.info("Cancelling order: {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));

        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new InvalidOrderStateException("Cannot cancel delivered order");
        }
        
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new InvalidOrderStateException("Order is already cancelled");
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order saved = orderRepository.save(order);
        log.info("Order {} cancelled successfully", orderId);

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
