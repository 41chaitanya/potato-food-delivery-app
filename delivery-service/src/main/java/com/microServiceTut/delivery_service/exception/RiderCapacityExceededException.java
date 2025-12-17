package com.microServiceTut.delivery_service.exception;

import java.util.UUID;

public class RiderCapacityExceededException extends RuntimeException {

    public RiderCapacityExceededException(UUID riderId, int maxCapacity) {
        super("Rider " + riderId + " has reached maximum active deliveries limit: " + maxCapacity);
    }
}
