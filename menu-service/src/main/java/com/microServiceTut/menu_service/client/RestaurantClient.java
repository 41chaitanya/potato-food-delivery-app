package com.microServiceTut.menu_service.client;

import com.microServiceTut.menu_service.client.dto.RestaurantInternalResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

@Component
public class RestaurantClient {

    private final WebClient webClient;

    public RestaurantClient(WebClient.Builder builder) {
        this.webClient = builder
                .baseUrl("http://RESTAURANT-SERVICE")
                .build();
    }

    public RestaurantInternalResponse getRestaurantInternal(UUID restaurantId) {
        return webClient.get()
                .uri("/api/restaurants/internal/{restaurantId}", restaurantId)
                .retrieve()
                .bodyToMono(RestaurantInternalResponse.class)
                .block();
    }
}
