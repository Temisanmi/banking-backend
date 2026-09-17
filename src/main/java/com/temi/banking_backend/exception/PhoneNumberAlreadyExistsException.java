package com.temi.banking_backend.exception;

public class PhoneNumberAlreadyExistsException extends RuntimeException {
    public PhoneNumberAlreadyExistsException(String phoneNumber) {
        super("Phone number already registered: " + phoneNumber);
    }
}
