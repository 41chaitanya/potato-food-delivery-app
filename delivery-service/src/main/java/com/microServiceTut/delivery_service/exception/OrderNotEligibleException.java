package com.microServiceTut.delivery_service.exception;

import java.util.UUID;

public class OrderNotEligibleException extends RuntimeException {

    public OrderNotEligibleException(UUID orderId, String reason) {
        super("Order " + orderId + " is not eligible for delivery: " + reason);
    }
}
