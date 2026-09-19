package com.temi.banking_backend.service;

import com.temi.banking_backend.dto.transaction.TransactionResponse;
import com.temi.banking_backend.entity.Account;
import com.temi.banking_backend.entity.Transaction;
import com.temi.banking_backend.entity.User;
import com.temi.banking_backend.entity.enums.Role;
import com.temi.banking_backend.entity.enums.TransactionStatus;
import com.temi.banking_backend.entity.enums.TransactionType;
import com.temi.banking_backend.exception.AccountNotFoundException;
import com.temi.banking_backend.exception.InsufficientFundsException;
import com.temi.banking_backend.exception.TransactionNotFoundException;
import com.temi.banking_backend.exception.TransactionNotReversibleException;
import com.temi.banking_backend.exception.UnauthorizedAccountAccessException;
import com.temi.banking_backend.repository.AccountRepository;
import com.temi.banking_backend.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TransactionReversalService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionBuilder transactionBuilder;

    private void assertIsAdmin(User actor) {
        if (actor.getRole() != Role.ADMIN) {
            throw new UnauthorizedAccountAccessException();
        }
    }

    private void assertSufficientFunds(Account account, BigDecimal amount) {
        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException();
        }
    }

    private Transaction reverseTransfer(Transaction original, User actor) {
        Account fromAccount = accountRepository.findByIdForUpdate(original.getFromAccount().getId())
                .orElseThrow(() -> new AccountNotFoundException(original.getFromAccount().getId()));

        Account toAccount = accountRepository.findByIdForUpdate(original.getToAccount().getId())
                .orElseThrow(() -> new AccountNotFoundException(original.getToAccount().getId()));

        assertSufficientFunds(toAccount, original.getAmount());

        toAccount.setBalance(toAccount.getBalance().subtract(original.getAmount()));

        fromAccount.setBalance(fromAccount.getBalance().add(original.getAmount()));

        accountRepository.save(toAccount);
        accountRepository.save(fromAccount);

        Transaction reversal = transactionBuilder.build(
                TransactionType.TRANSFER, original.getAmount(), toAccount, fromAccount,
                actor, "Reversal of " + original.getReference()
        );
        reversal.setReversalOf(original);

        return transactionRepository.save(reversal);
    }

    private Transaction reverseWithdrawal(Transaction original, User actor) {
        Account account = accountRepository.findByIdForUpdate(original.getFromAccount().getId())
                .orElseThrow(() -> new AccountNotFoundException(original.getFromAccount().getId()));

        account.setBalance(account.getBalance().add(original.getAmount()));

        accountRepository.save(account);

        Transaction reversal = transactionBuilder.build(
                TransactionType.WITHDRAWAL, original.getAmount(), null, account,
                actor, "Reversal of " + original.getReference()
        );
        reversal.setReversalOf(original);

        return transactionRepository.save(reversal);
    }

    private Transaction reverseDeposit(Transaction original, User actor) {
        Account account = accountRepository.findByIdForUpdate(original.getToAccount().getId())
                .orElseThrow(() -> new AccountNotFoundException(original.getToAccount().getId()));

        assertSufficientFunds(account, original.getAmount());

        account.setBalance(account.getBalance().subtract(original.getAmount()));

        accountRepository.save(account);

        Transaction reversal = transactionBuilder.build(
                TransactionType.DEPOSIT, original.getAmount(), account, null,
                actor, "Reversal of " + original.getReference()
        );
        reversal.setReversalOf(original);

        return transactionRepository.save(reversal);
    }

    @Transactional
    public TransactionResponse reverseTransaction(String transactionId, User actor) {
        assertIsAdmin(actor);

        Transaction original = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException(transactionId));

        if (original.getStatus() != TransactionStatus.COMPLETED) {
            throw new TransactionNotReversibleException(original.getStatus().name());
        }

        Transaction reversal = switch (original.getType()) {
            case DEPOSIT -> reverseDeposit(original, actor);
            case WITHDRAWAL -> reverseWithdrawal(original, actor);
            case TRANSFER -> reverseTransfer(original, actor);
        };

        original.setStatus(TransactionStatus.REVERSED);

        transactionRepository.save(original);

        return TransactionResponse.fromEntity(reversal);
    }
}
