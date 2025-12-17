package com.microServiceTut.delivery_service.client;

import com.microServiceTut.delivery_service.client.dto.OrderBasicResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

/**
 * WebClient for Order Service communication.
 * Fetches order details for delivery validation.
 */
@Component
@Slf4j
public class OrderClient {

    private final WebClient webClient;

    public OrderClient(WebClient.Builder builder) {
        this.webClient = builder
                .baseUrl("http://ORDER-SERVICE")
                .build();
    }

    public OrderBasicResponse getOrderBasic(UUID orderId) {
        log.debug("Fetching order details for orderId: {}", orderId);
        
        return webClient.get()
                .uri("/api/orders/internal/{orderId}", orderId)
                .retrieve()
                .bodyToMono(OrderBasicResponse.class)
                .block();
    }
}
