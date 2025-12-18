package com.microServiceTut.admin_service.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommissionResponse {
    private UUID id;
    private String configKey;
    private BigDecimal commissionPercentage;
    private String description;
    private boolean active;
}
