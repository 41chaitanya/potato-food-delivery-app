package com.microServiceTut.user_auth_service.security;

import com.microServiceTut.user_auth_service.model.Role;
import com.microServiceTut.user_auth_service.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * JWT utility class for token generation and validation.
 * Uses HS256 algorithm with secret key from environment.
 */
@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long expiration;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expiration) {
        // Create signing key from secret - must be at least 256 bits for HS256
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    /**
     * Generate JWT token containing userId, email, and role.
     * Token is signed with HS256 algorithm.
     */
    public String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Validate token and return claims if valid.
     * Returns null if token is invalid or expired.
     */
    public Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Extract user ID from token.
     */
    public UUID getUserIdFromToken(String token) {
        Claims claims = validateToken(token);
        return claims != null ? UUID.fromString(claims.getSubject()) : null;
    }

    /**
     * Extract email from token.
     */
    public String getEmailFromToken(String token) {
        Claims claims = validateToken(token);
        return claims != null ? claims.get("email", String.class) : null;
    }

    /**
     * Extract role from token.
     */
    public Role getRoleFromToken(String token) {
        Claims claims = validateToken(token);
        if (claims != null) {
            String roleStr = claims.get("role", String.class);
            return Role.valueOf(roleStr);
        }
        return null;
    }

    /**
     * Check if token is valid (not expired and properly signed).
     */
    public boolean isTokenValid(String token) {
        return validateToken(token) != null;
    }
}
