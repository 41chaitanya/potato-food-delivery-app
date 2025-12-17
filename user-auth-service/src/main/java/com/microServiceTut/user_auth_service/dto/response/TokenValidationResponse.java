package com.microServiceTut.user_auth_service.dto.response;

import com.microServiceTut.user_auth_service.model.Role;
import lombok.*;

import java.util.UUID;

/**
 * Response DTO for internal token validation endpoint.
 * Used by API Gateway to validate JWT and extract user info.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenValidationResponse {

    private boolean valid;
    private UUID userId;
    private String email;
    private Role role;
}
