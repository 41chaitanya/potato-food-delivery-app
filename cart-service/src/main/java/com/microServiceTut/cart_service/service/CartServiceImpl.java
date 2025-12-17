package com.microServiceTut.cart_service.service;

import com.microServiceTut.cart_service.client.MenuClient;
import com.microServiceTut.cart_service.client.dto.MenuItemInternalResponse;
import com.microServiceTut.cart_service.dto.request.AddCartItemRequest;
import com.microServiceTut.cart_service.dto.response.CartResponse;
import com.microServiceTut.cart_service.exception.*;
import com.microServiceTut.cart_service.mapper.CartMapper;
import com.microServiceTut.cart_service.model.Cart;
import com.microServiceTut.cart_service.model.CartItem;
import com.microServiceTut.cart_service.repository.CartItemRepository;
import com.microServiceTut.cart_service.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MenuClient menuClient;

    @Override
    @Transactional
    public CartResponse addItemToCart(AddCartItemRequest request) {
        // 1. Validate menu item via Menu Service
        MenuItemInternalResponse menuItem = fetchAndValidateMenuItem(request.getMenuItemId());

        // 2. Get or create cart
        Cart cart = getOrCreateCart(request.getUserId(), menuItem.restaurantId());

        // 3. Validate restaurant match
        if (!cart.getRestaurantId().equals(menuItem.restaurantId())) {
            throw new RestaurantMismatchException();
        }

        // 4. Check if item already exists in cart
        Optional<CartItem> existingItem = cartItemRepository
                .findByCartIdAndMenuItemId(cart.getId(), request.getMenuItemId());

        if (existingItem.isPresent()) {
            // Update quantity
            CartItem item = existingItem.get();
            item.updateQuantity(item.getQuantity() + request.getQuantity());
            cartItemRepository.save(item);
        } else {
            // Add new item
            CartItem newItem = CartItem.builder()
                    .menuItemId(request.getMenuItemId())
                    .quantity(request.getQuantity())
                    .pricePerItem(menuItem.price())
                    .totalPrice(menuItem.price())
                    .build();
            newItem.calculateTotalPrice();
            cart.addItem(newItem);
        }

        cart.recalculateTotalAmount();
        Cart savedCart = cartRepository.save(cart);

        return CartMapper.toResponse(savedCart);
    }

    @Override
    public CartResponse getCartByUserId(UUID userId) {
        Cart cart = cartRepository.findActiveCartByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException(userId));
        return CartMapper.toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse removeItemFromCart(UUID cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new CartItemNotFoundException(cartItemId));

        Cart cart = cartItem.getCart();
        cart.removeItem(cartItem);
        cartItemRepository.delete(cartItem);

        // If cart is empty, deactivate it
        if (cart.getItems().isEmpty()) {
            cart.setActive(false);
        }

        Cart savedCart = cartRepository.save(cart);
        return CartMapper.toResponse(savedCart);
    }

    @Override
    @Transactional
    public void clearCart(UUID userId) {
        Cart cart = cartRepository.findByUserIdAndActiveTrue(userId)
                .orElseThrow(() -> new CartNotFoundException(userId));

        cart.getItems().clear();
        cart.recalculateTotalAmount();
        cart.setActive(false);
        cartRepository.save(cart);
    }

    private MenuItemInternalResponse fetchAndValidateMenuItem(UUID menuItemId) {
        try {
            MenuItemInternalResponse menuItem = menuClient.getMenuItemInternal(menuItemId);

            if (!menuItem.available()) {
                throw new MenuItemUnavailableException(menuItemId);
            }

            return menuItem;
        } catch (WebClientResponseException.NotFound e) {
            throw new MenuItemNotFoundException(menuItemId);
        }
    }

    private Cart getOrCreateCart(UUID userId, UUID restaurantId) {
        return cartRepository.findByUserIdAndActiveTrue(userId)
                .orElseGet(() -> Cart.builder()
                        .userId(userId)
                        .restaurantId(restaurantId)
                        .active(true)
                        .build());
    }
}
