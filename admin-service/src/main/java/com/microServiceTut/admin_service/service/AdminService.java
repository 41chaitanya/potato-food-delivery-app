package com.microServiceTut.admin_service.service;

import com.microServiceTut.admin_service.dto.*;
import java.util.List;
import java.util.UUID;

public interface AdminService {

    // User Management
    List<UserResponse> getAllUsers();
    List<UserResponse> getUsersByRole(String role);
    UserResponse blockUser(UUID userId);
    UserResponse unblockUser(UUID userId);

    // Restaurant Approval
    List<RestaurantResponse> getAllRestaurants();
    List<RestaurantResponse> getPendingRestaurants();
    RestaurantResponse approveRestaurant(UUID restaurantId);
    RestaurantResponse rejectRestaurant(UUID restaurantId);

    // Platform Analytics
    PlatformAnalyticsResponse getPlatformAnalytics();

    // Commission Management
    List<CommissionResponse> getAllCommissions();
    CommissionResponse getCommission(String configKey);
    CommissionResponse createCommission(CommissionRequest request);
    CommissionResponse updateCommission(UUID id, CommissionRequest request);
    void deleteCommission(UUID id);
}
