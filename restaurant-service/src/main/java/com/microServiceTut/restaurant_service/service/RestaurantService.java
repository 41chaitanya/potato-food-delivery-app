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

    RestaurantInternalResponse getRestaurantInternal(UUID restaurantId);
}
