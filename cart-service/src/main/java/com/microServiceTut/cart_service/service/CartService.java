package com.microServiceTut.cart_service.service;

import com.microServiceTut.cart_service.dto.request.AddCartItemRequest;
import com.microServiceTut.cart_service.dto.response.CartResponse;

import java.util.UUID;

public interface CartService {

    CartResponse addItemToCart(AddCartItemRequest request);

    CartResponse getCartByUserId(UUID userId);

    CartResponse updateCartItemQuantity(UUID cartItemId, int quantity);

    CartResponse removeItemFromCart(UUID cartItemId);

    void clearCart(UUID userId);
}
