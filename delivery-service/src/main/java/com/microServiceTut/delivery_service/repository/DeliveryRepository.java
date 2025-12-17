package com.microServiceTut.delivery_service.repository;

import com.microServiceTut.delivery_service.model.Delivery;
import com.microServiceTut.delivery_service.model.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {

    Optional<Delivery> findByOrderId(UUID orderId);

    boolean existsByOrderId(UUID orderId);

    List<Delivery> findByRiderId(UUID riderId);

    List<Delivery> findByRiderIdAndStatusIn(UUID riderId, List<DeliveryStatus> statuses);

    long countByRiderIdAndStatusIn(UUID riderId, List<DeliveryStatus> activeStatuses);
}
