package com.temi.banking_backend.exception;

public class InvalidAccountStatusTransitionException extends RuntimeException {
    public InvalidAccountStatusTransitionException(String from, String to) {
        super("Cannot transition account from " + from + " to " + to);
    }
}
