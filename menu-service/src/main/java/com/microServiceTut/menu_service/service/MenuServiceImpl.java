package com.microServiceTut.menu_service.service;

import com.microServiceTut.menu_service.client.RestaurantClient;
import com.microServiceTut.menu_service.client.dto.RestaurantInternalResponse;
import com.microServiceTut.menu_service.dto.request.CreateMenuItemRequest;
import com.microServiceTut.menu_service.dto.request.UpdateMenuItemRequest;
import com.microServiceTut.menu_service.dto.response.MenuItemResponse;
import com.microServiceTut.menu_service.exception.MenuItemNotFoundException;
import com.microServiceTut.menu_service.exception.RestaurantNotActiveException;
import com.microServiceTut.menu_service.exception.RestaurantNotFoundException;
import com.microServiceTut.menu_service.mapper.MenuMapper;
import com.microServiceTut.menu_service.model.MealType;
import com.microServiceTut.menu_service.model.MenuItem;
import com.microServiceTut.menu_service.model.MenuStatus;
import com.microServiceTut.menu_service.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuServiceImpl implements MenuService {

    private final MenuItemRepository menuItemRepository;
    private final RestaurantClient restaurantClient;

    @Override
    @Transactional
    public MenuItemResponse createMenuItem(CreateMenuItemRequest request) {
        validateRestaurant(request.getRestaurantId());

        MenuItem menuItem = MenuMapper.toEntity(request);
        MenuItem saved = menuItemRepository.save(menuItem);
        return MenuMapper.toResponse(saved);
    }

    @Override
    public MenuItemResponse getMenuItemById(UUID menuItemId) {
        MenuItem menuItem = findMenuItemOrThrow(menuItemId);
        return MenuMapper.toResponse(menuItem);
    }

    @Override
    public List<MenuItemResponse> getMenuByRestaurant(UUID restaurantId) {
        return menuItemRepository.findByRestaurantIdAndStatus(restaurantId, MenuStatus.ACTIVE)
                .stream()
                .map(MenuMapper::toResponse)
                .toList();
    }

    @Override
    public List<MenuItemResponse> getMenuByRestaurantAndMealType(UUID restaurantId, MealType mealType) {
        return menuItemRepository.findByRestaurantIdAndMealTypeAndStatus(restaurantId, mealType, MenuStatus.ACTIVE)
                .stream()
                .map(MenuMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public MenuItemResponse updateMenuItem(UUID menuItemId, UpdateMenuItemRequest request) {
        MenuItem menuItem = findMenuItemOrThrow(menuItemId);
        MenuMapper.updateEntity(menuItem, request);
        MenuItem updated = menuItemRepository.save(menuItem);
        return MenuMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void softDeleteMenuItem(UUID menuItemId) {
        MenuItem menuItem = findMenuItemOrThrow(menuItemId);
        menuItem.setStatus(MenuStatus.INACTIVE);
        menuItem.setAvailable(false);
        menuItemRepository.save(menuItem);
    }

    private void validateRestaurant(UUID restaurantId) {
        try {
            RestaurantInternalResponse restaurant = restaurantClient.getRestaurantInternal(restaurantId);

            if (!restaurant.active()) {
                throw new RestaurantNotActiveException(restaurantId);
            }
        } catch (WebClientResponseException.NotFound e) {
            throw new RestaurantNotFoundException(restaurantId);
        }
    }

    private MenuItem findMenuItemOrThrow(UUID menuItemId) {
        return menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new MenuItemNotFoundException(menuItemId));
    }
}
