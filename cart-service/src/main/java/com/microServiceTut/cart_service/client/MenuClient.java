package com.microServiceTut.cart_service.client;

import com.microServiceTut.cart_service.client.dto.MenuItemInternalResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

@Component
public class MenuClient {

    private final WebClient webClient;

    public MenuClient(WebClient.Builder builder) {
        this.webClient = builder
                .baseUrl("http://MENU-SERVICE")
                .build();
    }

    public MenuItemInternalResponse getMenuItemInternal(UUID menuItemId) {
        return webClient.get()
                .uri("/api/menus/internal/{menuItemId}", menuItemId)
                .retrieve()
                .bodyToMono(MenuItemInternalResponse.class)
                .block();
    }
}
