package com.microServiceTut.cart_service.service;

import com.microServiceTut.cart_service.client.MenuClient;
import com.microServiceTut.cart_service.client.dto.MenuItemInternalResponse;
import com.microServiceTut.cart_service.config.CacheConstants;
import com.microServiceTut.cart_service.dto.request.AddCartItemRequest;
import com.microServiceTut.cart_service.dto.response.CartResponse;
import com.microServiceTut.cart_service.exception.*;
import com.microServiceTut.cart_service.mapper.CartMapper;
import com.microServiceTut.cart_service.model.Cart;
import com.microServiceTut.cart_service.model.CartItem;
import com.microServiceTut.cart_service.repository.CartItemRepository;
import com.microServiceTut.cart_service.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MenuClient menuClient;

    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.CART_BY_USER, key = "#request.userId")
    public CartResponse addItemToCart(AddCartItemRequest request) {
        log.info("Adding item to cart for user: {}, cache will be evicted", request.getUserId());
        MenuItemInternalResponse menuItem = fetchAndValidateMenuItem(request.getMenuItemId());
        Cart cart = getOrCreateCart(request.getUserId(), menuItem.restaurantId());

        if (!cart.getRestaurantId().equals(menuItem.restaurantId())) {
            throw new RestaurantMismatchException();
        }

        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndMenuItemId(cart.getId(), request.getMenuItemId());

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.updateQuantity(item.getQuantity() + request.getQuantity());
            cartItemRepository.save(item);
        } else {
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
    @Cacheable(value = CacheConstants.CART_BY_USER, key = "#userId")
    public CartResponse getCartByUserId(UUID userId) {
        log.info("Fetching cart from DATABASE for user: {} (cache miss)", userId);
        Cart cart = cartRepository.findActiveCartByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException(userId));
        return CartMapper.toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse updateCartItemQuantity(UUID cartItemId, int quantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new CartItemNotFoundException(cartItemId));
        cartItem.updateQuantity(quantity);
        cartItemRepository.save(cartItem);

        Cart cart = cartItem.getCart();
        cart.recalculateTotalAmount();
        Cart savedCart = cartRepository.save(cart);
        evictCartCache(cart.getUserId());
        log.info("Cart item quantity updated: {}, cache evicted for user: {}", cartItemId, cart.getUserId());
        return CartMapper.toResponse(savedCart);
    }

    @Override
    @Transactional
    public CartResponse removeItemFromCart(UUID cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new CartItemNotFoundException(cartItemId));

        Cart cart = cartItem.getCart();
        UUID userId = cart.getUserId();
        cart.removeItem(cartItem);
        cartItemRepository.delete(cartItem);

        if (cart.getItems().isEmpty()) {
            cart.setActive(false);
        }

        Cart savedCart = cartRepository.save(cart);
        evictCartCache(userId);
        log.info("Cart item removed: {}, cache evicted for user: {}", cartItemId, userId);
        return CartMapper.toResponse(savedCart);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.CART_BY_USER, key = "#userId")
    public void clearCart(UUID userId) {
        log.info("Clearing cart for user: {}, cache will be evicted", userId);
        Cart cart = cartRepository.findByUserIdAndActiveTrue(userId)
                .orElseThrow(() -> new CartNotFoundException(userId));
        cart.getItems().clear();
        cart.recalculateTotalAmount();
        cart.setActive(false);
        cartRepository.save(cart);
    }

    @CacheEvict(value = CacheConstants.CART_BY_USER, key = "#userId")
    public void evictCartCache(UUID userId) {
        log.debug("Evicting cart cache for user: {}", userId);
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
