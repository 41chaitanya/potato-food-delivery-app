package com.microServiceTut.cart_service.exception;

public class RestaurantMismatchException extends RuntimeException {

    public RestaurantMismatchException() {
        super("Cannot add items from different restaurants to the same cart. Please clear your cart first.");
    }
}
