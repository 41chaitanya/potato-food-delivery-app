package com.microServiceTut.menu_service.service;

import com.microServiceTut.menu_service.client.RestaurantClient;
import com.microServiceTut.menu_service.client.dto.RestaurantInternalResponse;
import com.microServiceTut.menu_service.config.CacheConstants;
import com.microServiceTut.menu_service.dto.request.CreateMenuItemRequest;
import com.microServiceTut.menu_service.dto.request.UpdateMenuItemRequest;
import com.microServiceTut.menu_service.dto.response.MenuItemInternalResponse;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MenuServiceImpl implements MenuService {

    private final MenuItemRepository menuItemRepository;
    private final RestaurantClient restaurantClient;

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = CacheConstants.MENU_BY_RESTAURANT, key = "#request.restaurantId"),
        @CacheEvict(value = CacheConstants.MENU_BY_RESTAURANT_MEAL, allEntries = true)
    })
    public MenuItemResponse createMenuItem(CreateMenuItemRequest request) {
        log.info("Creating menu item for restaurant: {}", request.getRestaurantId());
        validateRestaurant(request.getRestaurantId());
        MenuItem menuItem = MenuMapper.toEntity(request);
        MenuItem saved = menuItemRepository.save(menuItem);
        log.info("Menu item created: {}, cache evicted for restaurant: {}", saved.getId(), request.getRestaurantId());
        return MenuMapper.toResponse(saved);
    }

    @Override
    public MenuItemResponse getMenuItemById(UUID menuItemId) {
        MenuItem menuItem = findMenuItemOrThrow(menuItemId);
        return MenuMapper.toResponse(menuItem);
    }

    @Override
    @Cacheable(value = CacheConstants.MENU_BY_RESTAURANT, key = "#restaurantId", unless = "#result.isEmpty()")
    public List<MenuItemResponse> getMenuByRestaurant(UUID restaurantId) {
        log.info("Fetching menu from DATABASE for restaurant: {} (cache miss)", restaurantId);
        return menuItemRepository.findByRestaurantIdAndStatus(restaurantId, MenuStatus.ACTIVE)
                .stream()
                .map(MenuMapper::toResponse)
                .toList();
    }

    @Override
    @Cacheable(value = CacheConstants.MENU_BY_RESTAURANT_MEAL, key = "#restaurantId + ':' + #mealType", unless = "#result.isEmpty()")
    public List<MenuItemResponse> getMenuByRestaurantAndMealType(UUID restaurantId, MealType mealType) {
        log.info("Fetching menu from DATABASE for restaurant: {}, mealType: {} (cache miss)", restaurantId, mealType);
        return menuItemRepository.findByRestaurantIdAndMealTypeAndStatus(restaurantId, mealType, MenuStatus.ACTIVE)
                .stream()
                .map(MenuMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public MenuItemResponse updateMenuItem(UUID menuItemId, UpdateMenuItemRequest request) {
        MenuItem menuItem = findMenuItemOrThrow(menuItemId);
        UUID restaurantId = menuItem.getRestaurantId();
        MenuMapper.updateEntity(menuItem, request);
        MenuItem updated = menuItemRepository.save(menuItem);
        evictMenuCache(restaurantId);
        log.info("Menu item updated: {}, cache evicted for restaurant: {}", menuItemId, restaurantId);
        return MenuMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void softDeleteMenuItem(UUID menuItemId) {
        MenuItem menuItem = findMenuItemOrThrow(menuItemId);
        UUID restaurantId = menuItem.getRestaurantId();
        menuItem.setStatus(MenuStatus.INACTIVE);
        menuItem.setAvailable(false);
        menuItemRepository.save(menuItem);
        evictMenuCache(restaurantId);
        log.info("Menu item soft deleted: {}, cache evicted for restaurant: {}", menuItemId, restaurantId);
    }

    @Override
    @Transactional
    public MenuItemResponse toggleAvailability(UUID menuItemId) {
        MenuItem menuItem = findMenuItemOrThrow(menuItemId);
        UUID restaurantId = menuItem.getRestaurantId();
        menuItem.setAvailable(!menuItem.isAvailable());
        MenuItem updated = menuItemRepository.save(menuItem);
        evictMenuCache(restaurantId);
        log.info("Menu item availability toggled: {}, cache evicted for restaurant: {}", menuItemId, restaurantId);
        return MenuMapper.toResponse(updated);
    }

    @Override
    public MenuItemInternalResponse getMenuItemInternal(UUID menuItemId) {
        MenuItem menuItem = findMenuItemOrThrow(menuItemId);
        return MenuMapper.toInternalResponse(menuItem);
    }

    @Caching(evict = {
        @CacheEvict(value = CacheConstants.MENU_BY_RESTAURANT, key = "#restaurantId"),
        @CacheEvict(value = CacheConstants.MENU_BY_RESTAURANT_MEAL, allEntries = true)
    })
    public void evictMenuCache(UUID restaurantId) {
        log.debug("Evicting menu cache for restaurant: {}", restaurantId);
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
