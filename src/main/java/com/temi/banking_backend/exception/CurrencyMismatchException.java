package com.temi.banking_backend.exception;

public class CurrencyMismatchException extends RuntimeException {
    public CurrencyMismatchException() {
        super("Cannot transfer between accounts with different currencies");
    }
}
