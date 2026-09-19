package com.temi.banking_backend.dto.transaction;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TransferRequest {
    @NotBlank(message = "Sender account ID is required")
    private String fromAccountId;

    private String toAccountNumber;

    private String toPhoneNumber;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotBlank(message = "Transaction PIN is required")
    @Pattern(regexp = "^\\d{4}$", message = "PIN must be exactly 4 digits")
    private String pin;

    private String description;
}
