package com.microServiceTut.order_service.dto;

import java.util.UUID;

/**
 * Internal response for Delivery Service.
 * Contains minimal order info needed for delivery assignment.
 */
public record OrderBasicResponse(
        UUID orderId,
        String orderStatus,
        UUID restaurantId
) {}
