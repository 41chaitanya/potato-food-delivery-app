package com.microServiceTut.delivery_service.exception;

public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String requiredRole) {
        super("Access denied. Required role: " + requiredRole);
    }
}
