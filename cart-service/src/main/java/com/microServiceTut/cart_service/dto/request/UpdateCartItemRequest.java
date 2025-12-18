package com.microServiceTut.cart_service.dto.request;

import jakarta.validation.constraints.Min;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCartItemRequest {

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;
}
