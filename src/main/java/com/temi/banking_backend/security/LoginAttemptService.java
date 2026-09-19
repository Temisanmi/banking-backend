package com.temi.banking_backend.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.temi.banking_backend.exception.LoginLockedException;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
public class LoginAttemptService {
    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCKOUT_MINUTES = 15;

    private final Cache<String, Integer> attemptCache = Caffeine.newBuilder()
            .expireAfterWrite(LOCKOUT_MINUTES, TimeUnit.MINUTES)
            .maximumSize(10_000)
            .build();

    public void assertNotLocked(String email) {
        Integer attempts = attemptCache.getIfPresent(email);
        if (attempts != null && attempts >= MAX_ATTEMPTS) {
            throw new LoginLockedException(LOCKOUT_MINUTES);
        }
    }

    public void recordFailure(String email) {
        int attempts = attemptCache.getIfPresent(email) != null ? attemptCache.getIfPresent(email) : 0;
        attemptCache.put(email, attempts + 1);
    }

    public void recordSuccess(String email) {
        attemptCache.invalidate(email);
    }
}