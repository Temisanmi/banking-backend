package com.temi.banking_backend.dto.transaction;

import com.temi.banking_backend.entity.Transaction;
import com.temi.banking_backend.entity.enums.TransactionStatus;
import com.temi.banking_backend.entity.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Setter
public class TransactionResponse {
    private String id;
    private String reference;
    private TransactionType type;
    private BigDecimal amount;
    private String fromAccountNumber;
    private String toAccountNumber;
    private String performedByName;
    private TransactionStatus status;
    private String description;
    private LocalDateTime initiatedAt;
    private LocalDateTime completedAt;

    public static TransactionResponse fromEntity(Transaction transaction){
        return new TransactionResponse(
                transaction.getId(),
                transaction.getReference(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getFromAccount() != null ? transaction.getFromAccount().getAccountNumber() : null,
                transaction.getToAccount() != null ? transaction.getToAccount().getAccountNumber() : null,
                transaction.getPerformedBy().getFullName(),
                transaction.getStatus(),
                transaction.getDescription(),
                transaction.getInitiatedAt(),
                transaction.getCompletedAt()
        );
    }
}
