package com.microServiceTut.user_auth_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "simple")
@Slf4j
public class InMemoryTokenBlacklistService implements TokenBlacklistServiceInterface {

    private final Map<String, Long> blacklistedTokens = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static final String BLACKLIST_PREFIX = "blacklist:jwt:";

    public InMemoryTokenBlacklistService() {
        // Cleanup expired tokens every 5 minutes
        scheduler.scheduleAtFixedRate(this::cleanupExpiredTokens, 5, 5, TimeUnit.MINUTES);
        log.info("InMemoryTokenBlacklistService initialized (Redis disabled)");
    }

    @Override
    public void blacklistToken(String tokenId, long expirationTimeInMs) {
        String key = BLACKLIST_PREFIX + tokenId;
        long expiryTime = System.currentTimeMillis() + expirationTimeInMs;
        blacklistedTokens.put(key, expiryTime);
        log.info("Token blacklisted (in-memory): {} (TTL: {} ms)", tokenId, expirationTimeInMs);
    }

    @Override
    public boolean isTokenBlacklisted(String tokenId) {
        String key = BLACKLIST_PREFIX + tokenId;
        Long expiryTime = blacklistedTokens.get(key);
        if (expiryTime != null) {
            if (System.currentTimeMillis() < expiryTime) {
                log.debug("Token is blacklisted: {}", tokenId);
                return true;
            } else {
                blacklistedTokens.remove(key);
            }
        }
        return false;
    }

    @Override
    public void removeFromBlacklist(String tokenId) {
        String key = BLACKLIST_PREFIX + tokenId;
        blacklistedTokens.remove(key);
        log.info("Token removed from blacklist: {}", tokenId);
    }

    private void cleanupExpiredTokens() {
        long now = System.currentTimeMillis();
        blacklistedTokens.entrySet().removeIf(entry -> entry.getValue() < now);
    }
}
