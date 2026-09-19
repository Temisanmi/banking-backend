package com.temi.banking_backend.exception;

public class LoginLockedException extends RuntimeException {
    public LoginLockedException(int lockoutMinutes) {
        super("Too many failed login attempts. Try again in " + lockoutMinutes + " minutes.");
    }
}