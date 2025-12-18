package com.microServiceTut.menu_service.service;

import com.microServiceTut.menu_service.dto.request.CreateMenuItemRequest;
import com.microServiceTut.menu_service.dto.request.UpdateMenuItemRequest;
import com.microServiceTut.menu_service.dto.response.MenuItemInternalResponse;
import com.microServiceTut.menu_service.dto.response.MenuItemResponse;
import com.microServiceTut.menu_service.model.MealType;

import java.util.List;
import java.util.UUID;

public interface MenuService {

    MenuItemResponse createMenuItem(CreateMenuItemRequest request);

    MenuItemResponse getMenuItemById(UUID menuItemId);

    List<MenuItemResponse> getMenuByRestaurant(UUID restaurantId);

    List<MenuItemResponse> getMenuByRestaurantAndMealType(UUID restaurantId, MealType mealType);

    MenuItemResponse updateMenuItem(UUID menuItemId, UpdateMenuItemRequest request);

    void softDeleteMenuItem(UUID menuItemId);

    MenuItemResponse toggleAvailability(UUID menuItemId);

    MenuItemInternalResponse getMenuItemInternal(UUID menuItemId);
}
