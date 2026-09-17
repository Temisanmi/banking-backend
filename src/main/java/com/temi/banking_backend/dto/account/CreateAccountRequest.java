package com.temi.banking_backend.dto.account;

import com.temi.banking_backend.entity.enums.AccountType;
import com.temi.banking_backend.entity.enums.Currency;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAccountRequest {
    @NotNull(message = "Account type is required")
    private AccountType type;

    @NotNull(message = "Account currency is required")
    private Currency currency;
}
