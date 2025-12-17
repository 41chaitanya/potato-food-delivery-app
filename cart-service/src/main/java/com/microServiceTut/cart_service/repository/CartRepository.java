package com.microServiceTut.cart_service.repository;

import com.microServiceTut.cart_service.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CartRepository extends JpaRepository<Cart, UUID> {

    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.userId = :userId AND c.active = true")
    Optional<Cart> findActiveCartByUserId(@Param("userId") UUID userId);

    Optional<Cart> findByUserIdAndActiveTrue(UUID userId);
}
