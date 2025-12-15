package com.microServiceTut.payment_service.dto;

import com.microServiceTut.payment_service.model.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor

public class PaymentResponse {

    private UUID paymentId;
    private PaymentStatus status;



}
