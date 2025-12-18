package com.microServiceTut.admin_service.repository;

import com.microServiceTut.admin_service.model.PlatformStats;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlatformStatsRepository extends JpaRepository<PlatformStats, UUID> {
    Optional<PlatformStats> findByStatsDate(LocalDate date);
    List<PlatformStats> findByStatsDateBetweenOrderByStatsDateDesc(LocalDate start, LocalDate end);
}
