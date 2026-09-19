package com.temi.banking_backend.config;

import org.springframework.stereotype.Component;

@Component
public class PhoneNumberUtil {
    public String normalize(String rawPhoneNumber) {
        String cleaned = rawPhoneNumber.trim();

        if (cleaned.startsWith("+")){
            return cleaned;
        }

        if (cleaned.startsWith("0")){
            cleaned = cleaned.substring(1);
        }

        return "+234" + cleaned;
    }
}
