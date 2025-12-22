package com.microServiceTut.user_auth_service.service;

public interface TokenBlacklistServiceInterface {
    void blacklistToken(String tokenId, long expirationTimeInMs);
    boolean isTokenBlacklisted(String tokenId);
    void removeFromBlacklist(String tokenId);
}
