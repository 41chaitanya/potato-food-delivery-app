package com.microServiceTut.user_auth_service.exception;

public class UserNotActiveException extends RuntimeException {

    public UserNotActiveException(String email) {
        super("User account is deactivated: " + email);
    }
}
