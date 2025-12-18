package com.microServiceTut.user_auth_service.service;

import com.microServiceTut.user_auth_service.dto.request.LoginRequest;
import com.microServiceTut.user_auth_service.dto.request.RegisterRequest;
import com.microServiceTut.user_auth_service.dto.request.UpdateProfileRequest;
import com.microServiceTut.user_auth_service.dto.response.AuthResponse;
import com.microServiceTut.user_auth_service.dto.response.TokenValidationResponse;
import com.microServiceTut.user_auth_service.dto.response.UserProfileResponse;

import java.util.List;
import java.util.UUID;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    TokenValidationResponse validateToken(String token);

    UserProfileResponse getProfile(UUID userId);

    UserProfileResponse updateProfile(UUID userId, UpdateProfileRequest request);

    // Admin endpoints
    List<UserProfileResponse> getAllUsers();

    List<UserProfileResponse> getUsersByRole(String role);

    UserProfileResponse blockUser(UUID userId);

    UserProfileResponse unblockUser(UUID userId);

    UserStatsResponse getUserStats();

    record UserStatsResponse(long totalUsers, long activeUsers, long blockedUsers,
                             long totalAdmins, long totalRiders) {}
}
