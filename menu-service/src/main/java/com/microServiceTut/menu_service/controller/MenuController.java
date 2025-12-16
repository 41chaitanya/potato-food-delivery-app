package com.microServiceTut.menu_service.controller;

import com.microServiceTut.menu_service.dto.request.CreateMenuItemRequest;
import com.microServiceTut.menu_service.dto.request.UpdateMenuItemRequest;
import com.microServiceTut.menu_service.dto.response.MenuItemResponse;
import com.microServiceTut.menu_service.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/menus")

public class MenuController {
    @Autowired
    private MenuService menuService;

    @PostMapping
    public MenuItemResponse createMenu(@RequestBody CreateMenuItemRequest createMenuItemRequest) {
        return menuService.createMenuItem(createMenuItemRequest);
    }
    @GetMapping("/restaurant/{restaurantId}")
    public List<MenuItemResponse> getMenuItems(@PathVariable UUID restaurantId) {
        return menuService.getMenuByRestaurant(restaurantId);
    }
    @PutMapping("/{menuId}")
    public MenuItemResponse updateMenuItem(
            @PathVariable UUID menuId,
            @RequestBody UpdateMenuItemRequest request) {
        return menuService.updateMenuItem(menuId, request);
    }
    @DeleteMapping("/{menuId}")
    public void deactivateMenuItem(
            @PathVariable UUID menuId) {
        menuService.deactivateMenuItem(menuId);
    }
}