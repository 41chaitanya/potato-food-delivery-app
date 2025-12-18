package com.microServiceTut.admin_service.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantResponse {
    private UUID id;
    private String name;
    private String address;
    private String phone;
    private String cuisineType;
    private String status;
    private boolean active;
    private LocalDateTime createdAt;
}
