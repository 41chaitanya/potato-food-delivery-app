package com.microServiceTut.order_service.controller;

import com.microServiceTut.order_service.dto.*;
import com.microServiceTut.order_service.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping
    public OrderResponse createOrder(@RequestBody CreateOrderRequest createOrderRequest) {
        return orderService.createOrder(createOrderRequest);
    }

    /**
     * Get order by ID
     */
    @GetMapping("/{orderId}")
    public OrderDetailResponse getOrderById(@PathVariable UUID orderId) {
        return orderService.getOrderById(orderId);
    }

    /**
     * Get order history for user
     */
    @GetMapping("/user/{userId}")
    public List<OrderDetailResponse> getOrderHistory(@PathVariable UUID userId) {
        return orderService.getOrderHistory(userId);
    }

    /**
     * Cancel order
     */
    @PatchMapping("/{orderId}/cancel")
    public OrderDetailResponse cancelOrder(@PathVariable UUID orderId) {
        return orderService.cancelOrder(orderId);
    }

    /**
     * Internal endpoint for Delivery Service to fetch order details.
     */
    @GetMapping("/internal/{orderId}")
    public OrderBasicResponse getOrderBasic(@PathVariable UUID orderId) {
        return orderService.getOrderBasic(orderId);
    }

    /**
     * Update order status - for testing/admin purposes
     */
    @PatchMapping("/{orderId}/status")
    public OrderBasicResponse updateOrderStatus(@PathVariable UUID orderId, @RequestParam String status) {
        return orderService.updateOrderStatus(orderId, status);
    }

    /**
     * Admin stats endpoint
     */
    @GetMapping("/admin/stats")
    public OrderService.OrderStatsResponse getOrderStats() {
        return orderService.getOrderStats();
    }
}
