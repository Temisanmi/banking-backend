package com.temi.banking_backend.service;

import com.temi.banking_backend.entity.Account;
import com.temi.banking_backend.entity.Transaction;
import com.temi.banking_backend.entity.User;
import com.temi.banking_backend.entity.enums.TransactionStatus;
import com.temi.banking_backend.entity.enums.TransactionType;
import com.temi.banking_backend.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class FailedTransactionService {
    private final TransactionBuilder transactionBuilder;
    private final TransactionRepository transactionRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Transaction recordFailedTransaction(
            TransactionType type,
            BigDecimal amount,
            Account fromAccount,
            Account toAccount,
            User performedBy,
            String description,
            String failureReason
    ) {
        String failedDescription;

        if (description == null || description.isBlank()) {
            failedDescription = "Failed: " + failureReason;
        } else {
            failedDescription = description + " | Failed: " + failureReason;
        }

        Transaction transaction = transactionBuilder.build(
                type,
                amount,
                fromAccount,
                toAccount,
                performedBy,
                failedDescription,
                TransactionStatus.FAILED
        );

        return transactionRepository.save(transaction);
    }
}