package com.microServiceTut.payment_service.repository;

import com.microServiceTut.payment_service.dto.PaymentResponse;
import com.microServiceTut.payment_service.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findPaymentAdminByOrderId(UUID orderId);
}
