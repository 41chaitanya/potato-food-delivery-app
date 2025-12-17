package com.microServiceTut.delivery_service.exception;

import java.util.UUID;

public class DeliveryNotFoundException extends RuntimeException {

    public DeliveryNotFoundException(UUID deliveryId) {
        super("Delivery not found: " + deliveryId);
    }
}
