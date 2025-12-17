package com.microServiceTut.order_service.model;

public enum OrderStatus {
    CREATED,
    PAYMENT_PENDING,
    PAID,
    CONFIRMED,
    PAYMENT_FAILED,
    CANCELLED,
    DELIVERED
}
