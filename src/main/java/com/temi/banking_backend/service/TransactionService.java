package com.temi.banking_backend.service;

import com.temi.banking_backend.dto.transaction.DepositRequest;
import com.temi.banking_backend.dto.transaction.TransactionResponse;
import com.temi.banking_backend.dto.transaction.TransferRequest;
import com.temi.banking_backend.dto.transaction.WithdrawRequest;
import com.temi.banking_backend.entity.Account;
import com.temi.banking_backend.entity.Transaction;
import com.temi.banking_backend.entity.User;
import com.temi.banking_backend.entity.enums.AccountStatus;
import com.temi.banking_backend.entity.enums.TransactionType;
import com.temi.banking_backend.exception.AccountNotActiveException;
import com.temi.banking_backend.exception.AccountNotFoundException;
import com.temi.banking_backend.exception.CurrencyMismatchException;
import com.temi.banking_backend.exception.InsufficientFundsException;
import com.temi.banking_backend.exception.InvalidTransferRecipientException;
import com.temi.banking_backend.repository.AccountRepository;
import com.temi.banking_backend.repository.TransactionRepository;
import com.temi.banking_backend.security.TransactionPinService;
import com.temi.banking_backend.config.PhoneNumberUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionPinService transactionPinService;
    private final PhoneNumberUtil phoneNumberUtil;
    private final TransactionBuilder transactionBuilder;

    private void assertAccountActive(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException(account.getStatus().name());
        }
    }

    @Transactional
    public TransactionResponse deposit(DepositRequest request, User performedBy) {
        Account account = accountRepository.findByIdForUpdate(request.getAccountId())
                .orElseThrow(() -> new AccountNotFoundException(request.getAccountId()));

        assertAccountActive(account);

        account.setBalance(account.getBalance().add(request.getAmount()));

        accountRepository.save(account);

        Transaction transaction = transactionBuilder.build(
                TransactionType.DEPOSIT, request.getAmount(), null, account,
                performedBy, request.getDescription()
        );
        return TransactionResponse.fromEntity(transactionRepository.save(transaction));
    }

    private void assertSufficientFunds(Account account, BigDecimal amount) {
        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException();
        }
    }

    @Transactional
    public TransactionResponse withdraw(WithdrawRequest request, User performedBy) {
        Account account = accountRepository.findByIdForUpdate(request.getAccountId())
                .orElseThrow(() -> new AccountNotFoundException(request.getAccountId()));

        assertAccountActive(account);

        transactionPinService.verifyPin(performedBy.getId(), request.getPin(), performedBy.getTransactionPin());

        assertSufficientFunds(account, request.getAmount());

        account.setBalance(account.getBalance().subtract(request.getAmount()));

        accountRepository.save(account);

        Transaction transaction = transactionBuilder.build(
                TransactionType.WITHDRAWAL, request.getAmount(), account, null,
                performedBy, request.getDescription()
        );

        return TransactionResponse.fromEntity(transactionRepository.save(transaction));
    }

    private Account resolveRecipientAccount(TransferRequest request) {
        boolean hasAccountNumber = request.getToAccountNumber() != null && !request.getToAccountNumber().isBlank();
        boolean hasPhoneNumber = request.getToPhoneNumber() != null && !request.getToPhoneNumber().isBlank();

        if (hasAccountNumber == hasPhoneNumber) {
            throw new InvalidTransferRecipientException(
                    "Provide either account number or phone number");
        }

        if (hasAccountNumber) {
            return accountRepository.findByAccountNumber(request.getToAccountNumber())
                    .orElseThrow(() -> new AccountNotFoundException(request.getToAccountNumber()));
        }

        String normalizedPhone = phoneNumberUtil.normalize(request.getToPhoneNumber());

        return accountRepository.findCheckingAccountByOwnerPhoneNumber(normalizedPhone)
                .orElseThrow(() -> new AccountNotFoundException(normalizedPhone));
    }

    @Transactional
    public TransactionResponse transfer(TransferRequest request, User performedBy) {
        Account fromAccount = accountRepository.findByIdForUpdate(request.getFromAccountId())
                .orElseThrow(() -> new AccountNotFoundException(request.getFromAccountId()));

        Account toAccountLookup = resolveRecipientAccount(request);

        Account toAccount = accountRepository.findByIdForUpdate(toAccountLookup.getId())
                .orElseThrow(() -> new AccountNotFoundException(toAccountLookup.getId()));

        assertAccountActive(fromAccount);

        assertAccountActive(toAccount);

        transactionPinService.verifyPin(performedBy.getId(), request.getPin(), performedBy.getTransactionPin());

        assertSufficientFunds(fromAccount, request.getAmount());

        if (fromAccount.getCurrency() != toAccount.getCurrency()) {
            throw new CurrencyMismatchException();
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(request.getAmount()));

        toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        Transaction transaction = transactionBuilder.build(
                TransactionType.TRANSFER, request.getAmount(), fromAccount, toAccount,
                performedBy, request.getDescription()
        );
        return TransactionResponse.fromEntity(transactionRepository.save(transaction));
    }

    public Page<TransactionResponse> getAccountStatement(String accountId, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        Page<Transaction> transactions = transactionRepository.findStatementForAccount(accountId, from, to, pageable);
        return transactions.map(TransactionResponse::fromEntity);
    }
}