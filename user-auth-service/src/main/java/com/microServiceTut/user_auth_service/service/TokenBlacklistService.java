package com.microServiceTut.user_auth_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Primary
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService implements TokenBlacklistServiceInterface {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String BLACKLIST_PREFIX = "blacklist:jwt:";
    private static final String BLACKLISTED_VALUE = "true";

    @Override
    public void blacklistToken(String tokenId, long expirationTimeInMs) {
        String key = BLACKLIST_PREFIX + tokenId;
        redisTemplate.opsForValue().set(key, BLACKLISTED_VALUE, expirationTimeInMs, TimeUnit.MILLISECONDS);
        log.info("Token blacklisted in Redis: {} (TTL: {} ms)", tokenId, expirationTimeInMs);
    }

    @Override
    public boolean isTokenBlacklisted(String tokenId) {
        String key = BLACKLIST_PREFIX + tokenId;
        Boolean exists = redisTemplate.hasKey(key);
        if (Boolean.TRUE.equals(exists)) {
            log.debug("Token is blacklisted: {}", tokenId);
            return true;
        }
        return false;
    }

    @Override
    public void removeFromBlacklist(String tokenId) {
        String key = BLACKLIST_PREFIX + tokenId;
        redisTemplate.delete(key);
        log.info("Token removed from blacklist: {}", tokenId);
    }
}
