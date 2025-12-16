package com.microServiceTut.menu_service.service;

import com.microServiceTut.menu_service.dto.request.CreateMenuItemRequest;
import com.microServiceTut.menu_service.dto.request.UpdateMenuItemRequest;
import com.microServiceTut.menu_service.dto.response.MenuItemResponse;

import java.util.List;
import java.util.UUID;

public interface MenuService {
    MenuItemResponse createMenuItem(CreateMenuItemRequest request);

    MenuItemResponse getMenuItemById(UUID menuId);


    List<MenuItemResponse> getMenuByRestaurant(UUID restaurantId);

    MenuItemResponse updateMenuItem(UUID menuId, UpdateMenuItemRequest request);

    void deactivateMenuItem(UUID menuId);
}
