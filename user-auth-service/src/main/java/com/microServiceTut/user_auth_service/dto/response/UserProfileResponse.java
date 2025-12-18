package com.microServiceTut.user_auth_service.dto.response;

import com.microServiceTut.user_auth_service.model.Role;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileResponse {
    private UUID userId;
    private String name;
    private String email;
    private String phone;
    private String address;
    private Role role;
    private LocalDateTime createdAt;
}
