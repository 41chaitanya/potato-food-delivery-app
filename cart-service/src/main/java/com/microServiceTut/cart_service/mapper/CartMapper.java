package com.microServiceTut.cart_service.mapper;

import com.microServiceTut.cart_service.dto.response.CartItemResponse;
import com.microServiceTut.cart_service.dto.response.CartResponse;
import com.microServiceTut.cart_service.model.Cart;
import com.microServiceTut.cart_service.model.CartItem;

import java.util.List;

public final class CartMapper {

    private CartMapper() {}

    public static CartResponse toResponse(Cart cart) {
        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(CartMapper::toItemResponse)
                .toList();

        return CartResponse.builder()
                .id(cart.getId())
                .userId(cart.getUserId())
                .restaurantId(cart.getRestaurantId())
                .totalAmount(cart.getTotalAmount())
                .items(itemResponses)
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }

    public static CartItemResponse toItemResponse(CartItem item) {
        return CartItemResponse.builder()
                .id(item.getId())
                .menuItemId(item.getMenuItemId())
                .quantity(item.getQuantity())
                .pricePerItem(item.getPricePerItem())
                .totalPrice(item.getTotalPrice())
                .build();
    }
}
