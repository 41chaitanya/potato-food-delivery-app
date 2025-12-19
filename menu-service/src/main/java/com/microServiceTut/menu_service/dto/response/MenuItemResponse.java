package com.microServiceTut.menu_service.dto.response;

import com.microServiceTut.menu_service.model.MealType;
import com.microServiceTut.menu_service.model.MenuStatus;
import com.microServiceTut.menu_service.model.OccasionType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuItemResponse {

    private UUID id;
    private UUID restaurantId;
    private String name;
    private String description;
    private BigDecimal price;
    private MealType mealType;
    private OccasionType occasionType;
    private MenuStatus status;
    private boolean available;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
