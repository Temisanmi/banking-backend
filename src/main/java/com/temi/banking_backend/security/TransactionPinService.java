package com.temi.banking_backend.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.temi.banking_backend.exception.InvalidPinException;
import com.temi.banking_backend.exception.PinLockedException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TransactionPinService {
    private final PasswordEncoder passwordEncoder;

    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCKOUT_MINUTES = 15;

    private final Cache<String, Integer> attemptCache = Caffeine.newBuilder()
            .expireAfterWrite(LOCKOUT_MINUTES, TimeUnit.MINUTES)
            .maximumSize(10_000)
            .build();

    public void verifyPin(String userId, String submittedPin, String hashedPin) {
        Integer attempts = attemptCache.getIfPresent(userId);

        if (attempts != null && attempts >= MAX_ATTEMPTS) {
            throw new PinLockedException(LOCKOUT_MINUTES);
        }

        if (!passwordEncoder.matches(submittedPin, hashedPin)) {
            int newAttempts = (attempts == null ? 0 : attempts) + 1;
            attemptCache.put(userId, newAttempts);
            throw new InvalidPinException();
        }

        attemptCache.invalidate(userId);
    }
}
