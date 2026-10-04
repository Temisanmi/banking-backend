package com.temi.banking_backend.exception;

public class AccountBalanceNotZeroException extends RuntimeException {
    public AccountBalanceNotZeroException() {
        super("Account balance must be zero before the account can be closed");
    }
}
