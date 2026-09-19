package com.temi.banking_backend.exception;

public class InvalidPinException extends RuntimeException {
    public InvalidPinException() {
        super("Invalid transaction PIN!");
    }
}
