package com.microServiceTut.order_service.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class CreateOrderRequest {
    private UUID userId;
    private String customerName;
    private String restaurantName;
    private Double totalAmount;
}
