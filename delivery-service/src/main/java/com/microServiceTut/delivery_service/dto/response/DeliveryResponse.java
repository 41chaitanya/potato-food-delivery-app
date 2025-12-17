package com.microServiceTut.delivery_service.dto.response;

import com.microServiceTut.delivery_service.model.DeliveryStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryResponse {

    private UUID id;
    private UUID orderId;
    private UUID riderId;
    private DeliveryStatus status;
    private LocalDateTime assignedAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveredAt;
}
