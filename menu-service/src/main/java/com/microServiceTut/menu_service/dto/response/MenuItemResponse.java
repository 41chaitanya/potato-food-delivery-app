package com.microServiceTut.menu_service.dto.response;

import com.microServiceTut.menu_service.model.MenuStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class MenuItemResponse {
    private UUID id;
    private UUID restaurantId;
    private String name;
    private String description;
    private double price;
    private boolean available;
    private MenuStatus status;
    private LocalDateTime createdAt;
}
