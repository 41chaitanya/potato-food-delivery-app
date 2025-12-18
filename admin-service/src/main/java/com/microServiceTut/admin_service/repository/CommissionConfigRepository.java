package com.microServiceTut.admin_service.repository;

import com.microServiceTut.admin_service.model.CommissionConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface CommissionConfigRepository extends JpaRepository<CommissionConfig, UUID> {
    Optional<CommissionConfig> findByConfigKey(String configKey);
    Optional<CommissionConfig> findByConfigKeyAndActiveTrue(String configKey);
}
