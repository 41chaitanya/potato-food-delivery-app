package com.microServiceTut.restaurant_service.service;

import com.microServiceTut.restaurant_service.config.CacheConstants;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;

    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.RESTAURANTS_ACTIVE_LIST, allEntries = true)
    public RestaurantResponse createRestaurant(CreateRestaurantRequest request) {
        log.info("Creating restaurant: {}", request.getName());
        Restaurant restaurant = RestaurantMapper.toEntity(request);
        Restaurant saved = restaurantRepository.save(restaurant);
        log.info("Restaurant created: {}, cache evicted", saved.getId());
        return RestaurantMapper.toResponse(saved);
    }

    @Override
    public RestaurantResponse getRestaurantById(UUID restaurantId) {
        Restaurant restaurant = findRestaurantOrThrow(restaurantId);
        return RestaurantMapper.toResponse(restaurant);
    }

    @Override
    @Cacheable(value = CacheConstants.RESTAURANTS_ACTIVE_LIST, unless = "#result.isEmpty()")
    public List<RestaurantResponse> getAllActiveRestaurants() {
        log.info("Fetching active restaurants from DATABASE (cache miss)");
        return restaurantRepository.findByActiveTrue()
                .stream()
                .map(RestaurantMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.RESTAURANTS_ACTIVE_LIST, allEntries = true)
    public RestaurantResponse updateRestaurant(UUID restaurantId, UpdateRestaurantRequest request) {
        log.info("Updating restaurant: {}", restaurantId);
        Restaurant restaurant = findRestaurantOrThrow(restaurantId);
        RestaurantMapper.updateEntity(restaurant, request);
        Restaurant updated = restaurantRepository.save(restaurant);
        log.info("Restaurant updated: {}, cache evicted", restaurantId);
        return RestaurantMapper.toResponse(updated);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.RESTAURANTS_ACTIVE_LIST, allEntries = true)
    public void softDeleteRestaurant(UUID restaurantId) {
        log.info("Soft deleting restaurant: {}", restaurantId);
        Restaurant restaurant = findRestaurantOrThrow(restaurantId);
        restaurant.setActive(false);
        restaurant.setStatus(RestaurantStatus.CLOSED);
        restaurantRepository.save(restaurant);
        log.info("Restaurant soft deleted: {}, cache evicted", restaurantId);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.RESTAURANTS_ACTIVE_LIST, allEntries = true)
    public RestaurantResponse toggleRestaurantStatus(UUID restaurantId) {
        log.info("Toggling restaurant status: {}", restaurantId);
        Restaurant restaurant = findRestaurantOrThrow(restaurantId);
        if (restaurant.getStatus() == RestaurantStatus.ACTIVE) {
            restaurant.setStatus(RestaurantStatus.CLOSED);
            restaurant.setActive(false);
        } else {
            restaurant.setStatus(RestaurantStatus.ACTIVE);
            restaurant.setActive(true);
        }
        Restaurant updated = restaurantRepository.save(restaurant);
        log.info("Restaurant status toggled: {}, cache evicted", restaurantId);
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
    @CacheEvict(value = CacheConstants.RESTAURANTS_ACTIVE_LIST, allEntries = true)
    public RestaurantResponse approveRestaurant(UUID restaurantId) {
        log.info("Approving restaurant: {}", restaurantId);
        Restaurant restaurant = findRestaurantOrThrow(restaurantId);
        restaurant.setStatus(RestaurantStatus.ACTIVE);
        restaurant.setActive(true);
        log.info("Restaurant approved: {}, cache evicted", restaurantId);
        return RestaurantMapper.toResponse(restaurantRepository.save(restaurant));
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.RESTAURANTS_ACTIVE_LIST, allEntries = true)
    public RestaurantResponse rejectRestaurant(UUID restaurantId) {
        log.info("Rejecting restaurant: {}", restaurantId);
        Restaurant restaurant = findRestaurantOrThrow(restaurantId);
        restaurant.setStatus(RestaurantStatus.REJECTED);
        restaurant.setActive(false);
        log.info("Restaurant rejected: {}, cache evicted", restaurantId);
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
