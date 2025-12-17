package com.microServiceTut.user_auth_service.dto.response;

import com.microServiceTut.user_auth_service.model.Role;
import lombok.*;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private UUID userId;
    private String name;
    private String email;
    private Role role;
    private String token;
    private String tokenType;
}
