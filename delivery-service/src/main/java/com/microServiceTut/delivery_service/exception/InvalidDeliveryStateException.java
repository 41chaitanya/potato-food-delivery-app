package com.microServiceTut.delivery_service.exception;

import com.microServiceTut.delivery_service.model.DeliveryStatus;

public class InvalidDeliveryStateException extends RuntimeException {

    public InvalidDeliveryStateException(DeliveryStatus current, DeliveryStatus expected) {
        super("Invalid delivery state. Current: " + current + ", Expected: " + expected);
    }
}
