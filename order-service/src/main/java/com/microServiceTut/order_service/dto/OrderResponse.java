package com.microServiceTut.order_service.dto;

import com.microServiceTut.order_service.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;
@Data
@AllArgsConstructor
public class OrderResponse {
    private UUID orderId;
    private OrderStatus status;
    private Double totalAmount;
}

