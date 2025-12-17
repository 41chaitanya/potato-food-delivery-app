package com.microServiceTut.delivery_service.service;

import com.microServiceTut.delivery_service.dto.request.AssignDeliveryRequest;
import com.microServiceTut.delivery_service.dto.response.DeliveryResponse;

import java.util.List;
import java.util.UUID;

public interface DeliveryService {

    DeliveryResponse assignDelivery(UUID orderId, AssignDeliveryRequest request);

    List<DeliveryResponse> getRiderDeliveries(UUID riderId);

    DeliveryResponse pickupDelivery(UUID deliveryId, UUID riderId);

    DeliveryResponse deliverOrder(UUID deliveryId, UUID riderId);
}
