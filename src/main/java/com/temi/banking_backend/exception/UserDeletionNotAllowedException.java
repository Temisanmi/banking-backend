package com.temi.banking_backend.exception;

public class UserDeletionNotAllowedException extends RuntimeException {
    public UserDeletionNotAllowedException(String reason) {
        super("Cannot delete this user: " + reason);
    }
}