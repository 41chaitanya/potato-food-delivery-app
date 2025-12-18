package com.microServiceTut.admin_service.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlatformAnalyticsResponse {
    // User Stats
    private long totalUsers;
    private long activeUsers;
    private long blockedUsers;
    private long totalAdmins;
    private long totalRiders;

    // Restaurant Stats
    private long totalRestaurants;
    private long activeRestaurants;
    private long pendingRestaurants;

    // Order Stats
    private long totalOrders;
    private long completedOrders;
    private long cancelledOrders;
    private long pendingOrders;

    // Revenue Stats
    private BigDecimal totalRevenue;
    private BigDecimal totalCommission;
    private BigDecimal commissionPercentage;
}
