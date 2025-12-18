package com.microServiceTut.api_gateway.security;

import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Validates routes and determines access rules.
 * Defines which endpoints are public and role-based access.
 */
@Component
public class RouteValidator {

    // Public endpoints - no authentication required
    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/auth/register",
            "/auth/login",
            "/auth/validate",
            "/actuator/health"
    );

    // Role-based access rules
    private static final Map<String, Set<String>> ROLE_ACCESS = Map.of(
            "USER", Set.of(
                    "/auth/profile",   // User profile
                    "/api/cart",
                    "/api/orders",
                    "/api/payments",   // View payments
                    "/api/menus",      // Read only
                    "/api/restaurants" // Read only
            ),
            "ADMIN", Set.of(
                    "/auth/profile",   // Admin profile
                    "/auth/admin",     // Admin user management
                    "/api/admin",      // Admin service
                    "/api/restaurants",
                    "/api/menus",
                    "/api/orders",
                    "/api/payments",   // View payments
                    "/api/delivery"    // Admin can assign deliveries
            ),
            "RIDER", Set.of(
                    "/auth/profile",   // Rider profile
                    "/api/delivery",
                    "/api/orders"
            )
    );

    // Endpoints where USER can only read (GET)
    private static final Set<String> USER_READ_ONLY = Set.of(
            "/api/restaurants",
            "/api/menus"
    );

    /**
     * Check if the request is to a public endpoint.
     */
    public Predicate<ServerHttpRequest> isSecured = request -> 
            PUBLIC_ENDPOINTS.stream()
                    .noneMatch(uri -> request.getURI().getPath().startsWith(uri));

    /**
     * Check if user role has access to the requested endpoint.
     */
    public boolean hasAccess(String role, String path, HttpMethod method) {
        if (role == null) return false;

        Set<String> allowedPaths = ROLE_ACCESS.get(role);
        if (allowedPaths == null) return false;

        // Check if path matches any allowed path
        boolean pathAllowed = allowedPaths.stream()
                .anyMatch(path::startsWith);

        if (!pathAllowed) return false;

        // For USER role, check read-only restrictions
        if ("USER".equals(role) && USER_READ_ONLY.stream().anyMatch(path::startsWith)) {
            // USER can only GET on restaurants and menus
            return method == HttpMethod.GET;
        }

        return true;
    }
}
