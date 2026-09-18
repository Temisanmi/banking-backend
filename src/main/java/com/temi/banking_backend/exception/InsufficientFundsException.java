package com.temi.banking_backend.exception;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException() {
        super("Insufficient funds for this operation");
    }
}
