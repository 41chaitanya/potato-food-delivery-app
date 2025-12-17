package com.microServiceTut.cart_service.client.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record MenuItemInternalResponse(
        UUID menuItemId,
        UUID restaurantId,
        BigDecimal price,
        boolean available
) {}
