package com.microServiceTut.order_service.dto;

import com.microServiceTut.order_service.model.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;
@Data
@AllArgsConstructor
public class PaymentResponse {
    private UUID  paymentId;
    private PaymentStatus status;
}
