package com.temi.banking_backend.exception;

public class AccountNotActiveException extends RuntimeException {
    public AccountNotActiveException(String status) {
        super("Account is not active (current status: " + status + ")");
    }
}
