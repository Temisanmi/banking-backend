package com.temi.banking_backend.exception;

public class UnauthorizedAccountAccessException extends RuntimeException {
    public UnauthorizedAccountAccessException() {
        super("You do not have permission to access this account");
    }
}
