package com.microServiceTut.cart_service.repository;

import com.microServiceTut.cart_service.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    Optional<CartItem> findByCartIdAndMenuItemId(UUID cartId, UUID menuItemId);
}
