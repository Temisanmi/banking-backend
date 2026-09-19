package com.temi.banking_backend.exception;

public class InvalidStaffRoleException extends RuntimeException {
    public InvalidStaffRoleException() {
        super("Role must be TELLER or ADMIN when creating a staff account");
    }
}
