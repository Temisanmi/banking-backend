package com.temi.banking_backend.exception;

public class InvalidTransferRecipientException extends RuntimeException {
    public InvalidTransferRecipientException(String message) {
        super(message);
    }
}
