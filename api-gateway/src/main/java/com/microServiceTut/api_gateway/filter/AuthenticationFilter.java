package com.microServiceTut.api_gateway.filter;

import com.microServiceTut.api_gateway.security.JwtUtil;
import com.microServiceTut.api_gateway.security.RouteValidator;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Global authentication filter for API Gateway.
 * 
 * Responsibilities:
 * 1. Extract JWT from Authorization header
 * 2. Validate JWT signature and expiry
 * 3. Check role-based access
 * 4. Add X-USER-ID and X-USER-ROLE headers for downstream services
 * 5. Reject unauthorized requests
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;
    private final RouteValidator routeValidator;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // Skip authentication for public endpoints
        if (!routeValidator.isSecured.test(request)) {
            log.debug("Public endpoint accessed: {}", path);
            return chain.filter(exchange);
        }

        // Check for Authorization header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header for: {}", path);
            return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
        }

        // Extract and validate token
        String token = authHeader.substring(7);
        Claims claims = jwtUtil.validateToken(token);

        if (claims == null) {
            log.warn("Invalid JWT token for: {}", path);
            return onError(exchange, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
        }

        // Extract user info from token
        String userId = jwtUtil.getUserId(claims);
        String role = jwtUtil.getRole(claims);
        String email = jwtUtil.getEmail(claims);

        // Check role-based access
        if (!routeValidator.hasAccess(role, path, request.getMethod())) {
            log.warn("Access denied for user {} with role {} to: {} {}", 
                    email, role, request.getMethod(), path);
            return onError(exchange, "Access denied. Insufficient permissions.", HttpStatus.FORBIDDEN);
        }

        // Add user info headers for downstream services
        // Services can trust these headers as Gateway has validated the token
        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-USER-ID", userId)
                .header("X-USER-ROLE", role)
                .header("X-USER-EMAIL", email)
                .build();

        log.debug("Authenticated request: {} {} by user {} ({})", 
                request.getMethod(), path, email, role);

        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    @Override
    public int getOrder() {
        // Run before other filters
        return -1;
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add("Content-Type", "application/json");
        
        String body = String.format(
                "{\"status\":%d,\"error\":\"%s\",\"message\":\"%s\"}",
                status.value(), status.getReasonPhrase(), message
        );
        
        return response.writeWith(
                Mono.just(response.bufferFactory().wrap(body.getBytes()))
        );
    }
}
