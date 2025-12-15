package com.microServiceTut.payment_service.service;

import com.microServiceTut.payment_service.dto.PaymentRequest;
import com.microServiceTut.payment_service.dto.PaymentResponse;
import com.microServiceTut.payment_service.model.Payment;
import com.microServiceTut.payment_service.model.PaymentStatus;
import com.microServiceTut.payment_service.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PaymentService {
    @Autowired
    private PaymentRepository paymentRepository;

    public PaymentResponse processPayment(PaymentRequest paymentRequest) {
        Payment  payment = new Payment();

        payment.setOrderId(paymentRequest.getOrderId());
        payment.setAmount(paymentRequest.getAmount());
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaymentTime(LocalDateTime.now());

        try{
            boolean paymentResult = simulatePaymentGateway();
            if (paymentResult) {
                payment.setStatus(PaymentStatus.SUCCESS);
            } else {
                payment.setStatus(PaymentStatus.FAILED);
            }

        }catch(Exception e){
            payment.setStatus(PaymentStatus.FAILED);
        }
        Payment savePayment = paymentRepository.save(payment);

        return new PaymentResponse(
                savePayment.getId(),
                savePayment.getStatus()
        );

    }
    private boolean simulatePaymentGateway() {
//       to fake payment failure 30 %
        return Math.random() > 0.3;
    }
}
