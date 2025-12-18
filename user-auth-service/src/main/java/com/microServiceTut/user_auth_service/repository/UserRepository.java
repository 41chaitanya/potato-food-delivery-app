package com.microServiceTut.user_auth_service.repository;

import com.microServiceTut.user_auth_service.model.Role;
import com.microServiceTut.user_auth_service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRole(Role role);

    long countByActiveTrue();

    long countByActiveFalse();

    long countByRole(Role role);
}
