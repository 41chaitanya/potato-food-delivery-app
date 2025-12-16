package com.microServiceTut.menu_service.mapper;

import com.microServiceTut.menu_service.dto.request.CreateMenuItemRequest;
import com.microServiceTut.menu_service.dto.request.UpdateMenuItemRequest;
import com.microServiceTut.menu_service.dto.response.MenuItemResponse;
import com.microServiceTut.menu_service.model.MenuItem;
import com.microServiceTut.menu_service.model.MenuStatus;

public class MenuMapper {

    public static MenuItem toEntity(CreateMenuItemRequest request) {
        return MenuItem.builder()
                .restaurantId(request.getRestaurantId())
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .available(true)
                .status(MenuStatus.ACTIVE)
                .build();
    }

    public static MenuItemResponse toResponse(MenuItem item) {
        return MenuItemResponse.builder()
                .id(item.getId())
                .restaurantId(item.getRestaurantId())
                .name(item.getName())
                .description(item.getDescription())
                .price(item.getPrice())
                .available(item.isAvailable())
                .status(item.getStatus())
                .createdAt(item.getCreatedAt())
                .build();
    }
    public static void updateEntity(MenuItem entity, UpdateMenuItemRequest request) {

        if (request.getName() != null) {
            entity.setName(request.getName());
        }

        if (request.getDescription() != null) {
            entity.setDescription(request.getDescription());
        }

        if (request.getPrice() != null) {
            entity.setPrice(request.getPrice());
        }


    }

}