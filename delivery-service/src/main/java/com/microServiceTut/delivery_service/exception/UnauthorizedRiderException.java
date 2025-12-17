package com.microServiceTut.delivery_service.exception;

import java.util.UUID;

public class UnauthorizedRiderException extends RuntimeException {

    public UnauthorizedRiderException(UUID riderId, UUID deliveryId) {
        super("Rider " + riderId + " is not authorized to access delivery " + deliveryId);
    }
}
