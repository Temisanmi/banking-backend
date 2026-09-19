package com.temi.banking_backend.exception;

public class TransactionNotReversibleException extends RuntimeException {
    public TransactionNotReversibleException(String currentStatus) {
        super("Cannot reverse a transaction with status: " + currentStatus);
    }
}
