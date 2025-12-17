package com.microServiceTut.restaurant_service.dto.response;

import com.microServiceTut.restaurant_service.model.CuisineType;
import com.microServiceTut.restaurant_service.model.RestaurantStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantResponse {

    private UUID id;
    private String name;
    private String address;
    private String phone;
    private CuisineType cuisineType;
    private RestaurantStatus status;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
