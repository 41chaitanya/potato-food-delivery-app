package com.microServiceTut.user_auth_service.service;

import com.microServiceTut.user_auth_service.dto.request.LoginRequest;
import com.microServiceTut.user_auth_service.dto.request.RegisterRequest;
import com.microServiceTut.user_auth_service.dto.response.AuthResponse;
import com.microServiceTut.user_auth_service.dto.response.TokenValidationResponse;
import com.microServiceTut.user_auth_service.exception.InvalidCredentialsException;
import com.microServiceTut.user_auth_service.exception.UserAlreadyExistsException;
import com.microServiceTut.user_auth_service.exception.UserNotActiveException;
import com.microServiceTut.user_auth_service.mapper.UserMapper;
import com.microServiceTut.user_auth_service.model.Role;
import com.microServiceTut.user_auth_service.model.User;
import com.microServiceTut.user_auth_service.repository.UserRepository;
import com.microServiceTut.user_auth_service.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(request.getEmail());
        }

        // Hash password using BCrypt
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // Create user entity
        User user = UserMapper.toEntity(request, encodedPassword);
        User savedUser = userRepository.save(user);

        // Generate JWT token
        String token = jwtUtil.generateToken(savedUser);

        return UserMapper.toAuthResponse(savedUser, token);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(InvalidCredentialsException::new);

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        // Check if user is active
        if (!user.isActive()) {
            throw new UserNotActiveException(user.getEmail());
        }

        // Generate JWT token
        String token = jwtUtil.generateToken(user);

        return UserMapper.toAuthResponse(user, token);
    }

    @Override
    public TokenValidationResponse validateToken(String token) {
        // Validate token and extract claims
        if (!jwtUtil.isTokenValid(token)) {
            return TokenValidationResponse.builder()
                    .valid(false)
                    .build();
        }

        UUID userId = jwtUtil.getUserIdFromToken(token);
        String email = jwtUtil.getEmailFromToken(token);
        Role role = jwtUtil.getRoleFromToken(token);

        return TokenValidationResponse.builder()
                .valid(true)
                .userId(userId)
                .email(email)
                .role(role)
                .build();
    }
}
