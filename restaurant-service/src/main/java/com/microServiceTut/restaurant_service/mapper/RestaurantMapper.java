package com.microServiceTut.restaurant_service.mapper;

import com.microServiceTut.restaurant_service.dto.request.CreateRestaurantRequest;
import com.microServiceTut.restaurant_service.dto.request.UpdateRestaurantRequest;
import com.microServiceTut.restaurant_service.dto.response.RestaurantInternalResponse;
import com.microServiceTut.restaurant_service.dto.response.RestaurantResponse;
import com.microServiceTut.restaurant_service.model.Restaurant;
import com.microServiceTut.restaurant_service.model.RestaurantStatus;

public final class RestaurantMapper {

    private RestaurantMapper() {}

    public static Restaurant toEntity(CreateRestaurantRequest request) {
        return Restaurant.builder()
                .name(request.getName())
                .address(request.getAddress())
                .phone(request.getPhone())
                .cuisineType(request.getCuisineType())
                .status(RestaurantStatus.ACTIVE)
                .active(true)
                .build();
    }

    public static void updateEntity(Restaurant restaurant, UpdateRestaurantRequest request) {
        if (request.getName() != null) {
            restaurant.setName(request.getName());
        }
        if (request.getAddress() != null) {
            restaurant.setAddress(request.getAddress());
        }
        if (request.getPhone() != null) {
            restaurant.setPhone(request.getPhone());
        }
        if (request.getCuisineType() != null) {
            restaurant.setCuisineType(request.getCuisineType());
        }
        if (request.getStatus() != null) {
            restaurant.setStatus(request.getStatus());
            restaurant.setActive(request.getStatus() == RestaurantStatus.ACTIVE);
        }
    }

    public static RestaurantResponse toResponse(Restaurant restaurant) {
        return RestaurantResponse.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .address(restaurant.getAddress())
                .phone(restaurant.getPhone())
                .cuisineType(restaurant.getCuisineType())
                .status(restaurant.getStatus())
                .active(restaurant.isActive())
                .createdAt(restaurant.getCreatedAt())
                .updatedAt(restaurant.getUpdatedAt())
                .build();
    }

    public static RestaurantInternalResponse toInternalResponse(Restaurant restaurant) {
        return new RestaurantInternalResponse(
                restaurant.getId(),
                restaurant.isActive(),
                restaurant.getCuisineType()
        );
    }
}
