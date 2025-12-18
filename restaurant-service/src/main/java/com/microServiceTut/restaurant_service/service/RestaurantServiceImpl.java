package com.microServiceTut.restaurant_service.service;

import com.microServiceTut.restaurant_service.dto.request.CreateRestaurantRequest;
import com.microServiceTut.restaurant_service.dto.request.UpdateRestaurantRequest;
import com.microServiceTut.restaurant_service.dto.response.RestaurantInternalResponse;
import com.microServiceTut.restaurant_service.dto.response.RestaurantResponse;
import com.microServiceTut.restaurant_service.exception.RestaurantNotFoundException;
import com.microServiceTut.restaurant_service.mapper.RestaurantMapper;
import com.microServiceTut.restaurant_service.model.Restaurant;
import com.microServiceTut.restaurant_service.model.RestaurantStatus;
import com.microServiceTut.restaurant_service.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;

    @Override
    @Transactional
    public RestaurantResponse createRestaurant(CreateRestaurantRequest request) {
        Restaurant restaurant = RestaurantMapper.toEntity(request);
        Restaurant saved = restaurantRepository.save(restaurant);
        return RestaurantMapper.toResponse(saved);
    }

    @Override
    public RestaurantResponse getRestaurantById(UUID restaurantId) {
        Restaurant restaurant = findRestaurantOrThrow(restaurantId);
        return RestaurantMapper.toResponse(restaurant);
    }

    @Override
    public List<RestaurantResponse> getAllActiveRestaurants() {
        return restaurantRepository.findByActiveTrue()
                .stream()
                .map(RestaurantMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public RestaurantResponse updateRestaurant(UUID restaurantId, UpdateRestaurantRequest request) {
        Restaurant restaurant = findRestaurantOrThrow(restaurantId);
        RestaurantMapper.updateEntity(restaurant, request);
        Restaurant updated = restaurantRepository.save(restaurant);
        return RestaurantMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void softDeleteRestaurant(UUID restaurantId) {
        Restaurant restaurant = findRestaurantOrThrow(restaurantId);
        restaurant.setActive(false);
        restaurant.setStatus(RestaurantStatus.CLOSED);
        restaurantRepository.save(restaurant);
    }

    @Override
    @Transactional
    public RestaurantResponse toggleRestaurantStatus(UUID restaurantId) {
        Restaurant restaurant = findRestaurantOrThrow(restaurantId);
        
        if (restaurant.getStatus() == RestaurantStatus.ACTIVE) {
            restaurant.setStatus(RestaurantStatus.CLOSED);
            restaurant.setActive(false);
        } else {
            restaurant.setStatus(RestaurantStatus.ACTIVE);
            restaurant.setActive(true);
        }
        
        Restaurant updated = restaurantRepository.save(restaurant);
        return RestaurantMapper.toResponse(updated);
    }

    @Override
    public RestaurantInternalResponse getRestaurantInternal(UUID restaurantId) {
        Restaurant restaurant = findRestaurantOrThrow(restaurantId);
        return RestaurantMapper.toInternalResponse(restaurant);
    }

    private Restaurant findRestaurantOrThrow(UUID restaurantId) {
        return restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));
    }

    // ==================== ADMIN ENDPOINTS ====================

    @Override
    public List<RestaurantResponse> getAllRestaurants() {
        return restaurantRepository.findAll().stream()
                .map(RestaurantMapper::toResponse)
                .toList();
    }

    @Override
    public List<RestaurantResponse> getPendingRestaurants() {
        return restaurantRepository.findByStatus(RestaurantStatus.PENDING).stream()
                .map(RestaurantMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public RestaurantResponse approveRestaurant(UUID restaurantId) {
        Restaurant restaurant = findRestaurantOrThrow(restaurantId);
        restaurant.setStatus(RestaurantStatus.ACTIVE);
        restaurant.setActive(true);
        return RestaurantMapper.toResponse(restaurantRepository.save(restaurant));
    }

    @Override
    @Transactional
    public RestaurantResponse rejectRestaurant(UUID restaurantId) {
        Restaurant restaurant = findRestaurantOrThrow(restaurantId);
        restaurant.setStatus(RestaurantStatus.REJECTED);
        restaurant.setActive(false);
        return RestaurantMapper.toResponse(restaurantRepository.save(restaurant));
    }

    @Override
    public RestaurantStatsResponse getRestaurantStats() {
        long total = restaurantRepository.count();
        long active = restaurantRepository.countByStatus(RestaurantStatus.ACTIVE);
        long pending = restaurantRepository.countByStatus(RestaurantStatus.PENDING);
        long rejected = restaurantRepository.countByStatus(RestaurantStatus.REJECTED);
        return new RestaurantStatsResponse(total, active, pending, rejected);
    }
}
