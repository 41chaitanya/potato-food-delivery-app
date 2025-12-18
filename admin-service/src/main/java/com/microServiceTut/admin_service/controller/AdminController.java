package com.microServiceTut.admin_service.controller;

import com.microServiceTut.admin_service.dto.*;
import com.microServiceTut.admin_service.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // ==================== USER MANAGEMENT ====================

    @GetMapping("/users")
    public List<UserResponse> getAllUsers() {
        return adminService.getAllUsers();
    }

    @GetMapping("/users/role/{role}")
    public List<UserResponse> getUsersByRole(@PathVariable String role) {
        return adminService.getUsersByRole(role);
    }

    @PatchMapping("/users/{userId}/block")
    public UserResponse blockUser(@PathVariable UUID userId) {
        return adminService.blockUser(userId);
    }

    @PatchMapping("/users/{userId}/unblock")
    public UserResponse unblockUser(@PathVariable UUID userId) {
        return adminService.unblockUser(userId);
    }

    // ==================== RESTAURANT APPROVAL ====================

    @GetMapping("/restaurants")
    public List<RestaurantResponse> getAllRestaurants() {
        return adminService.getAllRestaurants();
    }

    @GetMapping("/restaurants/pending")
    public List<RestaurantResponse> getPendingRestaurants() {
        return adminService.getPendingRestaurants();
    }

    @PatchMapping("/restaurants/{restaurantId}/approve")
    public RestaurantResponse approveRestaurant(@PathVariable UUID restaurantId) {
        return adminService.approveRestaurant(restaurantId);
    }

    @PatchMapping("/restaurants/{restaurantId}/reject")
    public RestaurantResponse rejectRestaurant(@PathVariable UUID restaurantId) {
        return adminService.rejectRestaurant(restaurantId);
    }


    // ==================== PLATFORM ANALYTICS ====================

    @GetMapping("/analytics")
    public PlatformAnalyticsResponse getPlatformAnalytics() {
        return adminService.getPlatformAnalytics();
    }

    // ==================== COMMISSION MANAGEMENT ====================

    @GetMapping("/commissions")
    public List<CommissionResponse> getAllCommissions() {
        return adminService.getAllCommissions();
    }

    @GetMapping("/commissions/{configKey}")
    public CommissionResponse getCommission(@PathVariable String configKey) {
        return adminService.getCommission(configKey);
    }

    @PostMapping("/commissions")
    @ResponseStatus(HttpStatus.CREATED)
    public CommissionResponse createCommission(@Valid @RequestBody CommissionRequest request) {
        return adminService.createCommission(request);
    }

    @PatchMapping("/commissions/{id}")
    public CommissionResponse updateCommission(@PathVariable UUID id, @Valid @RequestBody CommissionRequest request) {
        return adminService.updateCommission(id, request);
    }

    @DeleteMapping("/commissions/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCommission(@PathVariable UUID id) {
        adminService.deleteCommission(id);
    }
}
