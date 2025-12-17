package com.microServiceTut.delivery_service.client.dto;

import java.util.UUID;

/**
 * Response from Order Service internal API.
 * Contains minimal order info needed for delivery assignment.
 */
public record OrderBasicResponse(
        UUID orderId,
        String orderStatus,
        UUID restaurantId
) {}
