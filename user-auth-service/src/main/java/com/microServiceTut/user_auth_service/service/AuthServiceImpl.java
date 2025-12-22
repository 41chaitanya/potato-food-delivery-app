package com.microServiceTut.user_auth_service.service;

import com.microServiceTut.user_auth_service.dto.request.LoginRequest;
import com.microServiceTut.user_auth_service.dto.request.RegisterRequest;
import com.microServiceTut.user_auth_service.dto.request.UpdateProfileRequest;
import com.microServiceTut.user_auth_service.dto.response.AuthResponse;
import com.microServiceTut.user_auth_service.dto.response.TokenValidationResponse;
import com.microServiceTut.user_auth_service.dto.response.UserProfileResponse;
import com.microServiceTut.user_auth_service.exception.InvalidCredentialsException;
import com.microServiceTut.user_auth_service.exception.UserAlreadyExistsException;
import com.microServiceTut.user_auth_service.exception.UserNotActiveException;
import com.microServiceTut.user_auth_service.exception.UserNotFoundException;
import com.microServiceTut.user_auth_service.mapper.UserMapper;
import com.microServiceTut.user_auth_service.model.Role;
import com.microServiceTut.user_auth_service.model.User;
import com.microServiceTut.user_auth_service.repository.UserRepository;
import com.microServiceTut.user_auth_service.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistServiceInterface tokenBlacklistService;

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
        // First check if token is blacklisted (user logged out)
        if (isTokenBlacklisted(token)) {
            log.warn("Token validation failed: token is blacklisted");
            return TokenValidationResponse.builder()
                    .valid(false)
                    .build();
        }

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

    /**
     * Logout user by adding their token to Redis blacklist
     * Token will be rejected on subsequent requests until it expires
     */
    @Override
    public void logout(String token) {
        String tokenId = jwtUtil.getTokenId(token);
        long remainingTime = jwtUtil.getRemainingExpirationTime(token);
        
        tokenBlacklistService.blacklistToken(tokenId, remainingTime);
        log.info("User logged out, token blacklisted: {}", tokenId);
    }

    /**
     * Check if token is in blacklist (user has logged out)
     */
    @Override
    public boolean isTokenBlacklisted(String token) {
        String tokenId = jwtUtil.getTokenId(token);
        return tokenBlacklistService.isTokenBlacklisted(tokenId);
    }

    @Override
    public UserProfileResponse getProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));
        return UserMapper.toProfileResponse(user);
    }

    @Override
    @Transactional
    public UserProfileResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));

        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }

        User updated = userRepository.save(user);
        return UserMapper.toProfileResponse(updated);
    }

    // ==================== ADMIN ENDPOINTS ====================

    @Override
    public java.util.List<UserProfileResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::toProfileResponse)
                .toList();
    }

    @Override
    public java.util.List<UserProfileResponse> getUsersByRole(String role) {
        return userRepository.findByRole(Role.valueOf(role.toUpperCase())).stream()
                .map(UserMapper::toProfileResponse)
                .toList();
    }

    @Override
    @Transactional
    public UserProfileResponse blockUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));
        user.setActive(false);
        return UserMapper.toProfileResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserProfileResponse unblockUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));
        user.setActive(true);
        return UserMapper.toProfileResponse(userRepository.save(user));
    }

    @Override
    public UserStatsResponse getUserStats() {
        long total = userRepository.count();
        long active = userRepository.countByActiveTrue();
        long blocked = userRepository.countByActiveFalse();
        long admins = userRepository.countByRole(Role.ADMIN);
        long riders = userRepository.countByRole(Role.RIDER);
        return new UserStatsResponse(total, active, blocked, admins, riders);
    }
}
