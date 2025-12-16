package com.microServiceTut.payment_service.controller;

import com.microServiceTut.payment_service.dto.PaymentRequest;
import com.microServiceTut.payment_service.dto.PaymentResponse;
import com.microServiceTut.payment_service.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    @Autowired
    private PaymentService paymentService;

    @PostMapping
    public PaymentResponse makePayment(@RequestBody PaymentRequest paymentRequest) {
        return paymentService.processPayment(paymentRequest);
    }
    @GetMapping("/order/{orderId}")
    public PaymentResponse getPaymentAdmin(@PathVariable UUID orderId) {
        return paymentService.getPaymentAdminByOrderId(orderId);
    }


}
