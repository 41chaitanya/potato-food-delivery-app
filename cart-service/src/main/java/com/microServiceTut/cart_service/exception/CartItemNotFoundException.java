package com.microServiceTut.cart_service.exception;

import java.util.UUID;

public class CartItemNotFoundException extends RuntimeException {

    public CartItemNotFoundException(UUID cartItemId) {
        super("Cart item not found: " + cartItemId);
    }
}
