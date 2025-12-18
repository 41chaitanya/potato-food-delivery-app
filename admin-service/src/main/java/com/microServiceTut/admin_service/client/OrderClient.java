package com.microServiceTut.admin_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "ORDER-SERVICE")
public interface OrderClient {

    @GetMapping("/api/orders/admin/stats")
    OrderStatsResponse getOrderStats();

    record OrderStatsResponse(long totalOrders, long completedOrders, long cancelledOrders,
                              long pendingOrders, double totalRevenue) {}
}
