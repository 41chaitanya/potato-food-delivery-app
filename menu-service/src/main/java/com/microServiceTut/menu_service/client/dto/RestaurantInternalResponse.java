package com.microServiceTut.menu_service.client.dto;

import java.util.UUID;

public record RestaurantInternalResponse(
        UUID restaurantId,
        boolean active,
        String cuisineType
) {}
