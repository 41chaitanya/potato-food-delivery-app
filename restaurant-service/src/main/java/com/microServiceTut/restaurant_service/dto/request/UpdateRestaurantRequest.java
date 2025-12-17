package com.microServiceTut.restaurant_service.dto.request;

import com.microServiceTut.restaurant_service.model.CuisineType;
import com.microServiceTut.restaurant_service.model.RestaurantStatus;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateRestaurantRequest {

    @Size(max = 120, message = "Name must not exceed 120 characters")
    private String name;

    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    private CuisineType cuisineType;

    private RestaurantStatus status;
}
