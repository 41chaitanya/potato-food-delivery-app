package com.microServiceTut.payment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;
@Data
@AllArgsConstructor
public class PaymentRequest {
    private UUID orderId;
    private Double amount;
}
