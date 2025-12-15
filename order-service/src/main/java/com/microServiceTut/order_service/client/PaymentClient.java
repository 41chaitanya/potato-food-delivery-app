package com.microServiceTut.order_service.client;

import com.microServiceTut.order_service.dto.PaymentRequest;
import com.microServiceTut.order_service.dto.PaymentResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
@Component
public class PaymentClient {
    private final WebClient webClient;
    public PaymentClient(WebClient.Builder builder) {
        this.webClient = builder
                .baseUrl("http://PAYMENT-SERVICE")
                .build();
    }

    public PaymentResponse makePayment(PaymentRequest request) {
        return webClient.post()
                .uri("/api/payments")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PaymentResponse.class)
                .block(); // sync call
    }
}
