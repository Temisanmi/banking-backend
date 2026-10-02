package com.temi.banking_backend.exception;

public class TransactionNotReversibleException extends RuntimeException {
    public TransactionNotReversibleException(String reason) {
        super("Cannot reverse this transaction: " + reason);
    }
}
