package com.microServiceTut.menu_service.dto.request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;
@Data
@AllArgsConstructor
@Builder
public class CreateMenuItemRequest {
    private UUID restaurantId;

    private String name;

    private String description;

    private double price;
}
