package com.microServiceTut.cart_service.controller;

import com.microServiceTut.cart_service.dto.request.AddCartItemRequest;
import com.microServiceTut.cart_service.dto.response.CartResponse;
import com.microServiceTut.cart_service.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public CartResponse addItemToCart(@Valid @RequestBody AddCartItemRequest request) {
        log.info("Adding item to cart for user: {}, menuItemId: {}", request.getUserId(), request.getMenuItemId());
        CartResponse response = cartService.addItemToCart(request);
        log.debug("Item added successfully, cart now has {} items", response.getItems().size());
        return response;
    }

    @GetMapping("/{userId}")
    public CartResponse getCartByUserId(@PathVariable UUID userId) {
        log.info("Fetching cart for user: {}", userId);
        return cartService.getCartByUserId(userId);
    }

    @PatchMapping("/items/{cartItemId}")
    public CartResponse updateCartItemQuantity(
            @PathVariable UUID cartItemId,
            @RequestParam int quantity) {
        log.info("Updating cart item: {} to quantity: {}", cartItemId, quantity);
        return cartService.updateCartItemQuantity(cartItemId, quantity);
    }

    @DeleteMapping("/items/{cartItemId}")
    public CartResponse removeItemFromCart(@PathVariable UUID cartItemId) {
        log.info("Removing item from cart: {}", cartItemId);
        return cartService.removeItemFromCart(cartItemId);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearCart(@PathVariable UUID userId) {
        log.info("Clearing cart for user: {}", userId);
        cartService.clearCart(userId);
        log.debug("Cart cleared successfully for user: {}", userId);
    }
}
