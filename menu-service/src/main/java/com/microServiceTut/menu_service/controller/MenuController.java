package com.microServiceTut.menu_service.controller;

import com.microServiceTut.menu_service.dto.request.CreateMenuItemRequest;
import com.microServiceTut.menu_service.dto.request.UpdateMenuItemRequest;
import com.microServiceTut.menu_service.dto.response.MenuItemInternalResponse;
import com.microServiceTut.menu_service.dto.response.MenuItemResponse;
import com.microServiceTut.menu_service.model.MealType;
import com.microServiceTut.menu_service.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MenuItemResponse createMenuItem(@Valid @RequestBody CreateMenuItemRequest request) {
        return menuService.createMenuItem(request);
    }

    @GetMapping("/{menuItemId}")
    public MenuItemResponse getMenuItemById(@PathVariable UUID menuItemId) {
        return menuService.getMenuItemById(menuItemId);
    }

    @GetMapping("/restaurant/{restaurantId}")
    public List<MenuItemResponse> getMenuByRestaurant(@PathVariable UUID restaurantId) {
        return menuService.getMenuByRestaurant(restaurantId);
    }

    @GetMapping("/restaurant/{restaurantId}/meal-type/{mealType}")
    public List<MenuItemResponse> getMenuByRestaurantAndMealType(
            @PathVariable UUID restaurantId,
            @PathVariable MealType mealType) {
        return menuService.getMenuByRestaurantAndMealType(restaurantId, mealType);
    }

    @PatchMapping("/{menuItemId}")
    public MenuItemResponse updateMenuItem(
            @PathVariable UUID menuItemId,
            @Valid @RequestBody UpdateMenuItemRequest request) {
        return menuService.updateMenuItem(menuItemId, request);
    }

    @DeleteMapping("/{menuItemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void softDeleteMenuItem(@PathVariable UUID menuItemId) {
        menuService.softDeleteMenuItem(menuItemId);
    }

    // Internal API for Cart Service
    @GetMapping("/internal/{menuItemId}")
    public MenuItemInternalResponse getMenuItemInternal(@PathVariable UUID menuItemId) {
        return menuService.getMenuItemInternal(menuItemId);
    }
}
