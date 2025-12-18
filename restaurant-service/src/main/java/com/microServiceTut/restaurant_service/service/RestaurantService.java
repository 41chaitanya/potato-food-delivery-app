package com.microServiceTut.restaurant_service.service;

import com.microServiceTut.restaurant_service.dto.request.CreateRestaurantRequest;
import com.microServiceTut.restaurant_service.dto.request.UpdateRestaurantRequest;
import com.microServiceTut.restaurant_service.dto.response.RestaurantInternalResponse;
import com.microServiceTut.restaurant_service.dto.response.RestaurantResponse;

import java.util.List;
import java.util.UUID;

public interface RestaurantService {

    RestaurantResponse createRestaurant(CreateRestaurantRequest request);

    RestaurantResponse getRestaurantById(UUID restaurantId);

    List<RestaurantResponse> getAllActiveRestaurants();

    RestaurantResponse updateRestaurant(UUID restaurantId, UpdateRestaurantRequest request);

    void softDeleteRestaurant(UUID restaurantId);

    RestaurantResponse toggleRestaurantStatus(UUID restaurantId);

    RestaurantInternalResponse getRestaurantInternal(UUID restaurantId);

    // Admin endpoints
    List<RestaurantResponse> getAllRestaurants();

    List<RestaurantResponse> getPendingRestaurants();

    RestaurantResponse approveRestaurant(UUID restaurantId);

    RestaurantResponse rejectRestaurant(UUID restaurantId);

    RestaurantStatsResponse getRestaurantStats();

    record RestaurantStatsResponse(long totalRestaurants, long activeRestaurants,
                                   long pendingRestaurants, long rejectedRestaurants) {}
}
