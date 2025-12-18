package com.microServiceTut.cart_service.controller;

import com.microServiceTut.cart_service.dto.request.AddCartItemRequest;
import com.microServiceTut.cart_service.dto.response.CartResponse;
import com.microServiceTut.cart_service.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public CartResponse addItemToCart(@Valid @RequestBody AddCartItemRequest request) {
        return cartService.addItemToCart(request);
    }

    @GetMapping("/{userId}")
    public CartResponse getCartByUserId(@PathVariable UUID userId) {
        return cartService.getCartByUserId(userId);
    }

    @PatchMapping("/items/{cartItemId}")
    public CartResponse updateCartItemQuantity(
            @PathVariable UUID cartItemId,
            @RequestParam int quantity) {
        return cartService.updateCartItemQuantity(cartItemId, quantity);
    }

    @DeleteMapping("/items/{cartItemId}")
    public CartResponse removeItemFromCart(@PathVariable UUID cartItemId) {
        return cartService.removeItemFromCart(cartItemId);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearCart(@PathVariable UUID userId) {
        cartService.clearCart(userId);
    }
}
