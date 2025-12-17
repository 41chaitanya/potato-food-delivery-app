package com.microServiceTut.menu_service.dto.request;

import com.microServiceTut.menu_service.model.MealType;
import com.microServiceTut.menu_service.model.MenuStatus;
import com.microServiceTut.menu_service.model.OccasionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateMenuItemRequest {

    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    private MealType mealType;

    private OccasionType occasionType;

    private MenuStatus status;

    private Boolean available;
}
