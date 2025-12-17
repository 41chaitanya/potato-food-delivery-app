package com.microServiceTut.user_auth_service.mapper;

import com.microServiceTut.user_auth_service.dto.request.RegisterRequest;
import com.microServiceTut.user_auth_service.dto.response.AuthResponse;
import com.microServiceTut.user_auth_service.dto.response.UserResponse;
import com.microServiceTut.user_auth_service.model.Role;
import com.microServiceTut.user_auth_service.model.User;

public final class UserMapper {

    private UserMapper() {}

    public static User toEntity(RegisterRequest request, String encodedPassword) {
        return User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(encodedPassword)
                .role(request.getRole() != null ? request.getRole() : Role.USER)
                .active(true)
                .build();
    }

    public static UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public static AuthResponse toAuthResponse(User user, String token) {
        return AuthResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .token(token)
                .tokenType("Bearer")
                .build();
    }
}
