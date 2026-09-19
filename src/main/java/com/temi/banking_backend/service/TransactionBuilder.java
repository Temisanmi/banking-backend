package com.temi.banking_backend.service;

import com.temi.banking_backend.entity.Account;
import com.temi.banking_backend.entity.Transaction;
import com.temi.banking_backend.entity.User;
import com.temi.banking_backend.entity.enums.TransactionStatus;
import com.temi.banking_backend.entity.enums.TransactionType;
import com.temi.banking_backend.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class TransactionBuilder {
    private final TransactionRepository transactionRepository;

    private static final SecureRandom RANDOM = new SecureRandom();

    public Transaction build(TransactionType type, BigDecimal amount,
                             Account fromAccount, Account toAccount,
                             User performedBy, String description) {
        Transaction transaction = new Transaction();
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);
        transaction.setPerformedBy(performedBy);
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setDescription(description);
        transaction.setReference(generateUniqueReference());
        transaction.setCompletedAt(LocalDateTime.now());
        return transaction;
    }

    public String generateUniqueReference() {
        String reference;
        do {
            reference = "TXN-" + (100000000 + RANDOM.nextInt(900000000));
        } while (transactionRepository.existsByReference(reference));
        return reference;
    }
}