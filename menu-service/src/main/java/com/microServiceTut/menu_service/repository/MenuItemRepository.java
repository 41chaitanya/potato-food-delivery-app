package com.microServiceTut.menu_service.repository;

import com.microServiceTut.menu_service.model.MenuItem;
import com.microServiceTut.menu_service.model.MenuStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MenuItemRepository extends JpaRepository<MenuItem, UUID> {
    List<MenuItem> findByRestaurantIdAndStatus(UUID restaurantId, MenuStatus status);

}
