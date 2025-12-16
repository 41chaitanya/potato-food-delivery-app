package com.microServiceTut.menu_service.service;

import com.microServiceTut.menu_service.dto.request.CreateMenuItemRequest;
import com.microServiceTut.menu_service.dto.request.UpdateMenuItemRequest;
import com.microServiceTut.menu_service.dto.response.MenuItemResponse;
import com.microServiceTut.menu_service.mapper.MenuMapper;
import com.microServiceTut.menu_service.model.MenuItem;
import com.microServiceTut.menu_service.model.MenuStatus;
import com.microServiceTut.menu_service.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService{
    @Autowired
    private MenuItemRepository repository;

    @Override
    public MenuItemResponse createMenuItem(CreateMenuItemRequest request) {
        var entity = MenuMapper.toEntity(request);
        var saved = repository.save(entity);
        return MenuMapper.toResponse(saved);
    }

    @Override
    public MenuItemResponse getMenuItemById(UUID menuId) {
        MenuItem item = repository.findById(menuId)
                .orElseThrow(() -> new RuntimeException("Menu item not found"));
        return MenuMapper.toResponse(item);
    }

    @Override
    public List<MenuItemResponse> getMenuByRestaurant(UUID restaurantId) {
        return repository
                .findByRestaurantIdAndStatus(restaurantId, MenuStatus.ACTIVE)
                .stream()
                .map(MenuMapper::toResponse)
                .toList();
    }

    @Override
    public MenuItemResponse updateMenuItem(UUID menuId, UpdateMenuItemRequest request) {

        MenuItem existing = repository.findById(menuId)
                .orElseThrow(() -> new RuntimeException("Menu item not found"));

        MenuMapper.updateEntity(existing, request);
        MenuItem updated = repository.save(existing);

        return MenuMapper.toResponse(updated);
    }

    @Override
    public void deactivateMenuItem(UUID menuId) {

        MenuItem item = repository.findById(menuId)
                .orElseThrow(() -> new RuntimeException("Menu item not found"));

        item.setStatus(MenuStatus.INACTIVE);
        item.setAvailable(false);

        repository.save(item);
    }


}
