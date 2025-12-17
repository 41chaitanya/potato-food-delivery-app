package com.microServiceTut.delivery_service.mapper;

import com.microServiceTut.delivery_service.dto.response.DeliveryResponse;
import com.microServiceTut.delivery_service.model.Delivery;

public final class DeliveryMapper {

    private DeliveryMapper() {}

    public static DeliveryResponse toResponse(Delivery delivery) {
        return DeliveryResponse.builder()
                .id(delivery.getId())
                .orderId(delivery.getOrderId())
                .riderId(delivery.getRiderId())
                .status(delivery.getStatus())
                .assignedAt(delivery.getAssignedAt())
                .pickedUpAt(delivery.getPickedUpAt())
                .deliveredAt(delivery.getDeliveredAt())
                .build();
    }
}
