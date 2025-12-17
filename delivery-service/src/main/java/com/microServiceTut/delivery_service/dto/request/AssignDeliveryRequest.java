package com.microServiceTut.delivery_service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignDeliveryRequest {

    @NotNull(message = "Rider ID is required")
    private UUID riderId;
}
