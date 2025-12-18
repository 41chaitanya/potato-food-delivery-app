package com.microServiceTut.admin_service.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "platform_stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlatformStats {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private LocalDate statsDate;

    private long totalUsers;
    private long totalRestaurants;
    private long totalRiders;
    private long totalOrders;
    private long completedOrders;
    private long cancelledOrders;

    @Column(precision = 15, scale = 2)
    private BigDecimal totalRevenue;

    @Column(precision = 15, scale = 2)
    private BigDecimal totalCommission;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
