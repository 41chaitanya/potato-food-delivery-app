package com.microServiceTut.menu_service.mapper;

import com.microServiceTut.menu_service.dto.request.CreateMenuItemRequest;
import com.microServiceTut.menu_service.dto.request.UpdateMenuItemRequest;
import com.microServiceTut.menu_service.dto.response.MenuItemInternalResponse;
import com.microServiceTut.menu_service.dto.response.MenuItemResponse;
import com.microServiceTut.menu_service.model.MenuItem;
import com.microServiceTut.menu_service.model.MenuStatus;
import com.microServiceTut.menu_service.model.OccasionType;

public final class MenuMapper {

    private MenuMapper() {}

    public static MenuItem toEntity(CreateMenuItemRequest request) {
        return MenuItem.builder()
                .restaurantId(request.getRestaurantId())
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .mealType(request.getMealType())
                .occasionType(request.getOccasionType() != null ? request.getOccasionType() : OccasionType.REGULAR)
                .status(MenuStatus.ACTIVE)
                .available(true)
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
        if (request.getMealType() != null) {
            entity.setMealType(request.getMealType());
        }
        if (request.getOccasionType() != null) {
            entity.setOccasionType(request.getOccasionType());
        }
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }
        if (request.getAvailable() != null) {
            entity.setAvailable(request.getAvailable());
        }
    }

    public static MenuItemResponse toResponse(MenuItem item) {
        return MenuItemResponse.builder()
                .id(item.getId())
                .restaurantId(item.getRestaurantId())
                .name(item.getName())
                .description(item.getDescription())
                .price(item.getPrice())
                .mealType(item.getMealType())
                .occasionType(item.getOccasionType())
                .status(item.getStatus())
                .available(item.isAvailable())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }

    public static MenuItemInternalResponse toInternalResponse(MenuItem item) {
        return new MenuItemInternalResponse(
                item.getId(),
                item.getRestaurantId(),
                item.getPrice(),
                item.isAvailable()
        );
    }
}
