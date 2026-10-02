package com.temi.banking_backend.dto.account;

import com.temi.banking_backend.entity.Account;
import com.temi.banking_backend.entity.enums.AccountStatus;
import com.temi.banking_backend.entity.enums.AccountType;
import com.temi.banking_backend.entity.enums.Currency;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Setter
public class StaffAccountResponse {
    private String id;
    private String accountNumber;
    private AccountType type;
    private AccountStatus status;
    private BigDecimal balance;
    private Currency currency;
    private LocalDateTime createdAt;
    private String ownerId;
    private String ownerFullName;

    public static StaffAccountResponse fromEntity(Account account) {
        return new StaffAccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getType(),
                account.getStatus(),
                account.getBalance(),
                account.getCurrency(),
                account.getCreatedAt(),
                account.getOwner().getId(),
                account.getOwner().getFullName()
        );
    }
}
