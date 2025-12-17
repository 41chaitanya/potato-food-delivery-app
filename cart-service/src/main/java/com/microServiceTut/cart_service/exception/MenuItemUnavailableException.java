package com.microServiceTut.cart_service.exception;

import java.util.UUID;

public class MenuItemUnavailableException extends RuntimeException {

    public MenuItemUnavailableException(UUID menuItemId) {
        super("Menu item is not available: " + menuItemId);
    }
}
