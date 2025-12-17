package com.microServiceTut.menu_service.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record MenuItemInternalResponse(
        UUID menuItemId,
        UUID restaurantId,
        BigDecimal price,
        boolean available
) {}
