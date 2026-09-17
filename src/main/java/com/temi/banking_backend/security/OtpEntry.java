package com.temi.banking_backend.security;


public record OtpEntry(String code, int attempts) {
}
