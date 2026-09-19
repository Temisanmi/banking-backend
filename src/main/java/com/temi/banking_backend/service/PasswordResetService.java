package com.temi.banking_backend.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
public class PasswordResetService {
    private static final int CODE_LENGTH_BOUND = 900000;
    private static final int CODE_MIN = 100000;
    private static final int MAX_ATTEMPTS = 5;
    private static final SecureRandom RANDOM = new SecureRandom();

    private record ResetEntry(String code, int attempts){}

    private final Cache<String, ResetEntry> resetCache = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(10_000)
            .build();

    public String generateResetCode(String email) {
        String code = String.valueOf(CODE_MIN + RANDOM.nextInt(CODE_LENGTH_BOUND));
        resetCache.put(email, new ResetEntry(code, 0));
        return code;
    }

    public boolean verifyResetCode(String email, String submittedCode) {
        ResetEntry entry = resetCache.getIfPresent(email);

        if (entry == null) {
            return false;
        }

        if (entry.attempts() >= MAX_ATTEMPTS) {
            resetCache.invalidate(email);
            return false;
        }

        if (entry.code().equals(submittedCode)) {
            resetCache.invalidate(email);
            return true;
        }

        resetCache.put(email, new ResetEntry(entry.code(), entry.attempts() + 1));
        return false;
    }
}
