package com.microServiceTut.user_auth_service.controller;

import com.microServiceTut.user_auth_service.dto.request.LoginRequest;
import com.microServiceTut.user_auth_service.dto.request.RegisterRequest;
import com.microServiceTut.user_auth_service.dto.response.AuthResponse;
import com.microServiceTut.user_auth_service.dto.response.TokenValidationResponse;
import com.microServiceTut.user_auth_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Register a new user.
     * Default role is USER if not specified.
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    /**
     * Login with email and password.
     * Returns JWT token on success.
     */
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    /**
     * Internal endpoint for API Gateway to validate JWT tokens.
     * Extracts token from Authorization header.
     */
    @GetMapping("/validate")
    public TokenValidationResponse validateToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return TokenValidationResponse.builder()
                    .valid(false)
                    .build();
        }

        String token = authHeader.substring(7);
        return authService.validateToken(token);
    }
}
