package com.microServiceTut.admin_service.client;

import com.microServiceTut.admin_service.dto.RestaurantResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@FeignClient(name = "RESTAURANT-SERVICE")
public interface RestaurantClient {

    @GetMapping("/api/restaurants/admin/all")
    List<RestaurantResponse> getAllRestaurants();

    @GetMapping("/api/restaurants/admin/pending")
    List<RestaurantResponse> getPendingRestaurants();

    @PatchMapping("/api/restaurants/admin/{restaurantId}/approve")
    RestaurantResponse approveRestaurant(@PathVariable UUID restaurantId);

    @PatchMapping("/api/restaurants/admin/{restaurantId}/reject")
    RestaurantResponse rejectRestaurant(@PathVariable UUID restaurantId);

    @GetMapping("/api/restaurants/admin/stats")
    RestaurantStatsResponse getRestaurantStats();

    record RestaurantStatsResponse(long totalRestaurants, long activeRestaurants,
                                   long pendingRestaurants, long rejectedRestaurants) {}
}
