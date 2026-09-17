package com.temi.banking_backend.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
public class OtpService {
    private static final int OTP_LENGTH = 6;
    private static final int MAX_ATTEMPTS = 3;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final Cache<String, OtpEntry> otpCache = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(10_000)
            .build();

    public String generateOtp(String email) {
        String code = String.valueOf(100000 + RANDOM.nextInt(900000));
        otpCache.put(email, new OtpEntry(code, 0));
        return code;
    }

    public boolean verifyOtp(String email, String submittedCode) {
        OtpEntry entry = otpCache.getIfPresent(email);

        if (entry == null) {
            return false;
        }

        if (entry.attempts() >= MAX_ATTEMPTS) {
            otpCache.invalidate(email);
            return false;
        }

        if (entry.code().equals(submittedCode)) {
            otpCache.invalidate(email);
            return true;
        }

        otpCache.put(email, new OtpEntry(entry.code(), entry.attempts() + 1));
        return false;
    }
}
