package com.microServiceTut.cart_service.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemResponse {

    private UUID id;
    private UUID menuItemId;
    private int quantity;
    private BigDecimal pricePerItem;
    private BigDecimal totalPrice;
}
