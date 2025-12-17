package com.microServiceTut.menu_service.exception;

import java.util.UUID;

public class RestaurantNotFoundException extends RuntimeException {

    public RestaurantNotFoundException(UUID restaurantId) {
        super("Restaurant not found with id: " + restaurantId);
    }
}
