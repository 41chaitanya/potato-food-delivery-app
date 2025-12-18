package com.microServiceTut.admin_service.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommissionRequest {

    @NotBlank(message = "Config key is required")
    private String configKey;

    @NotNull(message = "Commission percentage is required")
    @DecimalMin(value = "0.0", message = "Commission must be >= 0")
    @DecimalMax(value = "100.0", message = "Commission must be <= 100")
    private BigDecimal commissionPercentage;

    private String description;
}
