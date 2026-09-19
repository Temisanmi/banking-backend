package com.temi.banking_backend.exception;

public class PinLockedException extends RuntimeException {
    public PinLockedException(int lockoutMinutes) {
        super("Too many incorrect PIN attempts. Try again in " + lockoutMinutes + " minutes.");
    }
}
