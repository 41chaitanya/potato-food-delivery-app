package com.microServiceTut.payment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequest {
    private UUID orderId;
    private Double amount;
}
