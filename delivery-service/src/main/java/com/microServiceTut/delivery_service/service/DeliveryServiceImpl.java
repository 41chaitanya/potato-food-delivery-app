package com.microServiceTut.delivery_service.service;

import com.microServiceTut.delivery_service.client.OrderClient;
import com.microServiceTut.delivery_service.client.dto.OrderBasicResponse;
import com.microServiceTut.delivery_service.dto.request.AssignDeliveryRequest;
import com.microServiceTut.delivery_service.dto.response.DeliveryResponse;
import com.microServiceTut.delivery_service.exception.*;
import com.microServiceTut.delivery_service.mapper.DeliveryMapper;
import com.microServiceTut.delivery_service.model.Delivery;
import com.microServiceTut.delivery_service.model.DeliveryStatus;
import com.microServiceTut.delivery_service.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final OrderClient orderClient;

    @Value("${delivery.max-active-per-rider}")
    private int maxActiveDeliveriesPerRider;

    private static final List<DeliveryStatus> ACTIVE_STATUSES = List.of(
            DeliveryStatus.ASSIGNED,
            DeliveryStatus.PICKED_UP
    );

    @Override
    @Transactional
    public DeliveryResponse assignDelivery(UUID orderId, AssignDeliveryRequest request) {
        log.info("Assigning order {} to rider {}", orderId, request.getRiderId());

        // Check if order already has a delivery
        if (deliveryRepository.existsByOrderId(orderId)) {
            throw new OrderNotEligibleException(orderId, "Order already has a delivery assigned");
        }

        // Validate order via Order Service
        OrderBasicResponse order = orderClient.getOrderBasic(orderId);
        validateOrderForDelivery(order);

        // Check rider capacity
        validateRiderCapacity(request.getRiderId());

        // Create delivery
        Delivery delivery = Delivery.builder()
                .orderId(orderId)
                .riderId(request.getRiderId())
                .status(DeliveryStatus.ASSIGNED)
                .assignedAt(LocalDateTime.now())
                .build();

        Delivery saved = deliveryRepository.save(delivery);
        log.info("Delivery {} created for order {}", saved.getId(), orderId);

        return DeliveryMapper.toResponse(saved);
    }

    @Override
    public List<DeliveryResponse> getRiderDeliveries(UUID riderId) {
        log.debug("Fetching deliveries for rider {}", riderId);

        return deliveryRepository.findByRiderId(riderId)
                .stream()
                .map(DeliveryMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public DeliveryResponse pickupDelivery(UUID deliveryId, UUID riderId) {
        log.info("Rider {} picking up delivery {}", riderId, deliveryId);

        Delivery delivery = findDeliveryOrThrow(deliveryId);

        // Verify rider ownership
        validateRiderOwnership(delivery, riderId);

        // Verify status
        if (delivery.getStatus() != DeliveryStatus.ASSIGNED) {
            throw new InvalidDeliveryStateException(delivery.getStatus(), DeliveryStatus.ASSIGNED);
        }

        delivery.setStatus(DeliveryStatus.PICKED_UP);
        delivery.setPickedUpAt(LocalDateTime.now());

        Delivery updated = deliveryRepository.save(delivery);
        log.info("Delivery {} picked up by rider {}", deliveryId, riderId);

        return DeliveryMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public DeliveryResponse deliverOrder(UUID deliveryId, UUID riderId) {
        log.info("Rider {} delivering order for delivery {}", riderId, deliveryId);

        Delivery delivery = findDeliveryOrThrow(deliveryId);

        // Verify rider ownership
        validateRiderOwnership(delivery, riderId);

        // Verify status
        if (delivery.getStatus() != DeliveryStatus.PICKED_UP) {
            throw new InvalidDeliveryStateException(delivery.getStatus(), DeliveryStatus.PICKED_UP);
        }

        delivery.setStatus(DeliveryStatus.DELIVERED);
        delivery.setDeliveredAt(LocalDateTime.now());

        Delivery updated = deliveryRepository.save(delivery);
        log.info("Delivery {} completed by rider {}", deliveryId, riderId);

        return DeliveryMapper.toResponse(updated);
    }

    private void validateOrderForDelivery(OrderBasicResponse order) {
        // Order must be CONFIRMED or PAID to be assigned for delivery
        if (!"CONFIRMED".equals(order.orderStatus()) && !"PAID".equals(order.orderStatus())) {
            throw new OrderNotEligibleException(
                    order.orderId(),
                    "Order status must be CONFIRMED or PAID. Current: " + order.orderStatus()
            );
        }
    }

    private void validateRiderCapacity(UUID riderId) {
        long activeCount = deliveryRepository.countByRiderIdAndStatusIn(riderId, ACTIVE_STATUSES);

        if (activeCount >= maxActiveDeliveriesPerRider) {
            throw new RiderCapacityExceededException(riderId, maxActiveDeliveriesPerRider);
        }
    }

    private void validateRiderOwnership(Delivery delivery, UUID riderId) {
        if (!delivery.getRiderId().equals(riderId)) {
            throw new UnauthorizedRiderException(riderId, delivery.getId());
        }
    }

    private Delivery findDeliveryOrThrow(UUID deliveryId) {
        return deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new DeliveryNotFoundException(deliveryId));
    }
}
