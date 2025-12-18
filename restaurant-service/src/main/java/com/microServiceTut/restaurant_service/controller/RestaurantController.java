package com.microServiceTut.restaurant_service.controller;

import com.microServiceTut.restaurant_service.dto.request.CreateRestaurantRequest;
import com.microServiceTut.restaurant_service.dto.request.UpdateRestaurantRequest;
import com.microServiceTut.restaurant_service.dto.response.RestaurantInternalResponse;
import com.microServiceTut.restaurant_service.dto.response.RestaurantResponse;
import com.microServiceTut.restaurant_service.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RestaurantResponse createRestaurant(@Valid @RequestBody CreateRestaurantRequest request) {
        return restaurantService.createRestaurant(request);
    }

    @GetMapping("/{restaurantId}")
    public RestaurantResponse getRestaurantById(@PathVariable UUID restaurantId) {
        return restaurantService.getRestaurantById(restaurantId);
    }

    @GetMapping
    public List<RestaurantResponse> getAllActiveRestaurants() {
        return restaurantService.getAllActiveRestaurants();
    }

    @PatchMapping("/{restaurantId}")
    public RestaurantResponse updateRestaurant(
            @PathVariable UUID restaurantId,
            @Valid @RequestBody UpdateRestaurantRequest request) {
        return restaurantService.updateRestaurant(restaurantId, request);
    }

    @DeleteMapping("/{restaurantId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void softDeleteRestaurant(@PathVariable UUID restaurantId) {
        restaurantService.softDeleteRestaurant(restaurantId);
    }

    /**
     * Toggle restaurant open/close status
     */
    @PatchMapping("/{restaurantId}/toggle-status")
    public RestaurantResponse toggleRestaurantStatus(@PathVariable UUID restaurantId) {
        return restaurantService.toggleRestaurantStatus(restaurantId);
    }

    // Internal API for Menu Service
    @GetMapping("/internal/{restaurantId}")
    public RestaurantInternalResponse getRestaurantInternal(@PathVariable UUID restaurantId) {
        return restaurantService.getRestaurantInternal(restaurantId);
    }

    // ==================== ADMIN ENDPOINTS ====================

    @GetMapping("/admin/all")
    public List<RestaurantResponse> getAllRestaurants() {
        return restaurantService.getAllRestaurants();
    }

    @GetMapping("/admin/pending")
    public List<RestaurantResponse> getPendingRestaurants() {
        return restaurantService.getPendingRestaurants();
    }

    @PatchMapping("/admin/{restaurantId}/approve")
    public RestaurantResponse approveRestaurant(@PathVariable UUID restaurantId) {
        return restaurantService.approveRestaurant(restaurantId);
    }

    @PatchMapping("/admin/{restaurantId}/reject")
    public RestaurantResponse rejectRestaurant(@PathVariable UUID restaurantId) {
        return restaurantService.rejectRestaurant(restaurantId);
    }

    @GetMapping("/admin/stats")
    public RestaurantService.RestaurantStatsResponse getRestaurantStats() {
        return restaurantService.getRestaurantStats();
    }
}
