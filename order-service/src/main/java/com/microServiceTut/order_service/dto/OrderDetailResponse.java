package com.microServiceTut.order_service.dto;

import com.microServiceTut.order_service.model.OrderStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDetailResponse {
    private UUID orderId;
    private UUID userId;
    private String customerName;
    private String restaurantName;
    private Double totalAmount;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
