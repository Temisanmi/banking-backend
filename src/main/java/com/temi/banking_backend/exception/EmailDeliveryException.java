package com.temi.banking_backend.exception;

public class EmailDeliveryException extends RuntimeException {
    public EmailDeliveryException(String recipient, Throwable cause) {
        super("Failed to send email to: " + recipient, cause);
    }
}
