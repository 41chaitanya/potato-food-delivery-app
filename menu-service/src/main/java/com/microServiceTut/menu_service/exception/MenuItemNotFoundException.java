package com.microServiceTut.menu_service.exception;

import java.util.UUID;

public class MenuItemNotFoundException extends RuntimeException {

    public MenuItemNotFoundException(UUID menuItemId) {
        super("Menu item not found with id: " + menuItemId);
    }
}
