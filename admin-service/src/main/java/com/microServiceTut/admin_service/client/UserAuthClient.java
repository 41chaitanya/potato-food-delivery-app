package com.microServiceTut.admin_service.client;

import com.microServiceTut.admin_service.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@FeignClient(name = "USER-AUTH-SERVICE")
public interface UserAuthClient {

    @GetMapping("/auth/admin/users")
    List<UserResponse> getAllUsers();

    @GetMapping("/auth/admin/users/{userId}")
    UserResponse getUserById(@PathVariable UUID userId);

    @PatchMapping("/auth/admin/users/{userId}/block")
    UserResponse blockUser(@PathVariable UUID userId);

    @PatchMapping("/auth/admin/users/{userId}/unblock")
    UserResponse unblockUser(@PathVariable UUID userId);

    @GetMapping("/auth/admin/users/role/{role}")
    List<UserResponse> getUsersByRole(@PathVariable String role);

    @GetMapping("/auth/admin/stats")
    UserStatsResponse getUserStats();

    record UserStatsResponse(long totalUsers, long activeUsers, long blockedUsers,
                             long totalAdmins, long totalRiders) {}
}
