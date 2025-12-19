package com.microServiceTut.payment_service.service;

import com.microServiceTut.payment_service.dto.PaymentRequest;
import com.microServiceTut.payment_service.dto.PaymentResponse;
import com.microServiceTut.payment_service.exception.PaymentNotFoundException;
import com.microServiceTut.payment_service.model.Payment;
import com.microServiceTut.payment_service.model.PaymentStatus;
import com.microServiceTut.payment_service.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    
    private final PaymentRepository paymentRepository;

    public PaymentResponse processPayment(PaymentRequest paymentRequest) {
        log.info("Processing payment for orderId: {}, amount: {}", 
                paymentRequest.getOrderId(), paymentRequest.getAmount());
        
        Payment payment = new Payment();
        payment.setOrderId(paymentRequest.getOrderId());
        payment.setAmount(paymentRequest.getAmount());
        payment.setPaymentTime(LocalDateTime.now());

        try {
            boolean paymentResult = simulatePaymentGateway();
            if (paymentResult) {
                payment.setStatus(PaymentStatus.SUCCESS);
                log.info("Payment successful for orderId: {}", paymentRequest.getOrderId());
            } else {
                payment.setStatus(PaymentStatus.FAILED);
                log.warn("Payment failed for orderId: {}", paymentRequest.getOrderId());
            }
        } catch (Exception e) {
            log.error("Payment processing error for orderId: {}", paymentRequest.getOrderId(), e);
            payment.setStatus(PaymentStatus.FAILED);
        }
        
        Payment savedPayment = paymentRepository.save(payment);

        return new PaymentResponse(
                savedPayment.getId(),
                savedPayment.getStatus()
        );
    }

    /**
     * Simulates payment gateway - 70% success rate
     */
    private boolean simulatePaymentGateway() {
        return Math.random() > 0.3;
    }

    public PaymentResponse getPaymentByOrderId(UUID orderId) {
        log.debug("Fetching payment for orderId: {}", orderId);
        Payment payment = paymentRepository.findPaymentAdminByOrderId(orderId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for orderId: " + orderId));
        
        return new PaymentResponse(payment.getId(), payment.getStatus());
    }
}
