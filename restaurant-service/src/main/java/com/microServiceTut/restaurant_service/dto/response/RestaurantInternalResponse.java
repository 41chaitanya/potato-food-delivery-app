package com.microServiceTut.restaurant_service.dto.response;

import com.microServiceTut.restaurant_service.model.CuisineType;

import java.util.UUID;

public record RestaurantInternalResponse(
        UUID restaurantId,
        boolean active,
        CuisineType cuisineType
) {}
