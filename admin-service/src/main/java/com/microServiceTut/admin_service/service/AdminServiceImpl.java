package com.microServiceTut.admin_service.service;

import com.microServiceTut.admin_service.client.*;
import com.microServiceTut.admin_service.dto.*;
import com.microServiceTut.admin_service.model.CommissionConfig;
import com.microServiceTut.admin_service.repository.CommissionConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AdminServiceImpl implements AdminService {

    private final UserAuthClient userAuthClient;
    private final RestaurantClient restaurantClient;
    private final OrderClient orderClient;
    private final CommissionConfigRepository commissionRepo;

    @Override
    public List<UserResponse> getAllUsers() {
        return userAuthClient.getAllUsers();
    }

    @Override
    public List<UserResponse> getUsersByRole(String role) {
        return userAuthClient.getUsersByRole(role);
    }

    @Override
    public UserResponse blockUser(UUID userId) {
        return userAuthClient.blockUser(userId);
    }

    @Override
    public UserResponse unblockUser(UUID userId) {
        return userAuthClient.unblockUser(userId);
    }


    @Override
    public List<RestaurantResponse> getAllRestaurants() {
        return restaurantClient.getAllRestaurants();
    }

    @Override
    public List<RestaurantResponse> getPendingRestaurants() {
        return restaurantClient.getPendingRestaurants();
    }

    @Override
    public RestaurantResponse approveRestaurant(UUID restaurantId) {
        return restaurantClient.approveRestaurant(restaurantId);
    }

    @Override
    public RestaurantResponse rejectRestaurant(UUID restaurantId) {
        return restaurantClient.rejectRestaurant(restaurantId);
    }

    @Override
    public PlatformAnalyticsResponse getPlatformAnalytics() {
        var userStats = userAuthClient.getUserStats();
        var restaurantStats = restaurantClient.getRestaurantStats();
        var orderStats = orderClient.getOrderStats();

        BigDecimal commission = commissionRepo.findByConfigKeyAndActiveTrue("DEFAULT")
                .map(CommissionConfig::getCommissionPercentage)
                .orElse(BigDecimal.valueOf(10));

        BigDecimal totalRevenue = BigDecimal.valueOf(orderStats.totalRevenue());
        BigDecimal totalCommission = totalRevenue.multiply(commission).divide(BigDecimal.valueOf(100));

        return PlatformAnalyticsResponse.builder()
                .totalUsers(userStats.totalUsers())
                .activeUsers(userStats.activeUsers())
                .blockedUsers(userStats.blockedUsers())
                .totalAdmins(userStats.totalAdmins())
                .totalRiders(userStats.totalRiders())
                .totalRestaurants(restaurantStats.totalRestaurants())
                .activeRestaurants(restaurantStats.activeRestaurants())
                .pendingRestaurants(restaurantStats.pendingRestaurants())
                .totalOrders(orderStats.totalOrders())
                .completedOrders(orderStats.completedOrders())
                .cancelledOrders(orderStats.cancelledOrders())
                .pendingOrders(orderStats.pendingOrders())
                .totalRevenue(totalRevenue)
                .totalCommission(totalCommission)
                .commissionPercentage(commission)
                .build();
    }


    @Override
    public List<CommissionResponse> getAllCommissions() {
        return commissionRepo.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public CommissionResponse getCommission(String configKey) {
        return commissionRepo.findByConfigKey(configKey)
                .map(this::toResponse)
                .orElseThrow(() -> new RuntimeException("Commission not found: " + configKey));
    }

    @Override
    @Transactional
    public CommissionResponse createCommission(CommissionRequest request) {
        CommissionConfig config = CommissionConfig.builder()
                .configKey(request.getConfigKey())
                .commissionPercentage(request.getCommissionPercentage())
                .description(request.getDescription())
                .active(true)
                .build();
        return toResponse(commissionRepo.save(config));
    }

    @Override
    @Transactional
    public CommissionResponse updateCommission(UUID id, CommissionRequest request) {
        CommissionConfig config = commissionRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Commission not found: " + id));
        config.setCommissionPercentage(request.getCommissionPercentage());
        config.setDescription(request.getDescription());
        return toResponse(commissionRepo.save(config));
    }

    @Override
    @Transactional
    public void deleteCommission(UUID id) {
        commissionRepo.deleteById(id);
    }

    private CommissionResponse toResponse(CommissionConfig config) {
        return CommissionResponse.builder()
                .id(config.getId())
                .configKey(config.getConfigKey())
                .commissionPercentage(config.getCommissionPercentage())
                .description(config.getDescription())
                .active(config.isActive())
                .build();
    }
}
