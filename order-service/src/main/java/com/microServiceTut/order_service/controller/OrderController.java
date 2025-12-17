package com.microServiceTut.order_service.controller;

import com.microServiceTut.order_service.dto.CreateOrderRequest;
import com.microServiceTut.order_service.dto.OrderBasicResponse;
import com.microServiceTut.order_service.dto.OrderResponse;
import com.microServiceTut.order_service.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
}
