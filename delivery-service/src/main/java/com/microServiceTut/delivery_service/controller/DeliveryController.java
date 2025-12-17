package com.microServiceTut.delivery_service.controller;

import com.microServiceTut.delivery_service.dto.request.AssignDeliveryRequest;
import com.microServiceTut.delivery_service.dto.response.DeliveryResponse;
import com.microServiceTut.delivery_service.exception.AccessDeniedException;
import com.microServiceTut.delivery_service.service.DeliveryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/delivery")
@RequiredArgsConstructor
@Slf4j
public class DeliveryController {

    private final DeliveryService deliveryService;

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_RIDER = "RIDER";

    /**
     * Assign order to rider - ADMIN only
     */
    @PostMapping("/assign/{orderId}")
    @ResponseStatus(HttpStatus.CREATED)
    public DeliveryResponse assignDelivery(
            @PathVariable UUID orderId,
            @Valid @RequestBody AssignDeliveryRequest request,
            @RequestHeader("X-USER-ROLE") String userRole) {

        validateRole(userRole, ROLE_ADMIN);
        log.info("Admin assigning order {} to rider {}", orderId, request.getRiderId());

        return deliveryService.assignDelivery(orderId, request);
    }

    /**
     * Get rider's assigned deliveries - RIDER only
     */
    @GetMapping("/rider")
    public List<DeliveryResponse> getRiderDeliveries(
            @RequestHeader("X-USER-ID") String userId,
            @RequestHeader("X-USER-ROLE") String userRole) {

        validateRole(userRole, ROLE_RIDER);
        UUID riderId = UUID.fromString(userId);
        log.debug("Rider {} fetching their deliveries", riderId);

        return deliveryService.getRiderDeliveries(riderId);
    }

    /**
     * Pickup order - RIDER only
     */
    @PutMapping("/{deliveryId}/pickup")
    public DeliveryResponse pickupDelivery(
            @PathVariable UUID deliveryId,
            @RequestHeader("X-USER-ID") String userId,
            @RequestHeader("X-USER-ROLE") String userRole) {

        validateRole(userRole, ROLE_RIDER);
        UUID riderId = UUID.fromString(userId);
        log.info("Rider {} picking up delivery {}", riderId, deliveryId);

        return deliveryService.pickupDelivery(deliveryId, riderId);
    }

    /**
     * Deliver order - RIDER only
     */
    @PutMapping("/{deliveryId}/deliver")
    public DeliveryResponse deliverOrder(
            @PathVariable UUID deliveryId,
            @RequestHeader("X-USER-ID") String userId,
            @RequestHeader("X-USER-ROLE") String userRole) {

        validateRole(userRole, ROLE_RIDER);
        UUID riderId = UUID.fromString(userId);
        log.info("Rider {} delivering order for delivery {}", riderId, deliveryId);

        return deliveryService.deliverOrder(deliveryId, riderId);
    }

    private void validateRole(String userRole, String requiredRole) {
        if (!requiredRole.equals(userRole)) {
            throw new AccessDeniedException(requiredRole);
        }
    }
}
