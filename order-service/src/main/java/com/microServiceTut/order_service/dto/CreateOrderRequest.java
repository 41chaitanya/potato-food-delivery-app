package com.microServiceTut.order_service.dto;

import lombok.Data;

@Data
public class CreateOrderRequest {
    private String customerName;
    private String restaurantName;
    private Double totalAmount;
}
