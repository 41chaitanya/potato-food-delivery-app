package com.microServiceTut.menu_service.exception;

import java.util.UUID;

public class RestaurantNotActiveException extends RuntimeException {

    public RestaurantNotActiveException(UUID restaurantId) {
        super("Restaurant is not active: " + restaurantId);
    }
}
