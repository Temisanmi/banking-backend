package com.temi.banking_backend.service;

import com.temi.banking_backend.entity.Account;
import com.temi.banking_backend.entity.Transaction;
import com.temi.banking_backend.entity.enums.AccountStatus;
import com.temi.banking_backend.entity.enums.AccountType;
import com.temi.banking_backend.entity.enums.TransactionType;
import com.temi.banking_backend.repository.AccountRepository;
import com.temi.banking_backend.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InterestService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionBuilder transactionBuilder;

    private static final BigDecimal DAYS_IN_YEAR = BigDecimal.valueOf(365);

    @Value("${app.savings.annual-interest-rate}")
    private BigDecimal annualInterestRate;

    @Scheduled(cron = "${app.savings.interest-schedule}")
    @Transactional
    public void applyDailyInterest() {
        List<Account> savingsAccounts = accountRepository.findAllByTypeAndStatus(
                AccountType.SAVINGS, AccountStatus.ACTIVE
        );

        for (Account account : savingsAccounts) {
            applyInterestToAccount(account);
        }
    }

    private void applyInterestToAccount(Account account) {
        BigDecimal dailyInterest = account.getBalance()
                .multiply(annualInterestRate)
                .divide(DAYS_IN_YEAR, new MathContext(10, RoundingMode.HALF_UP))
                .setScale(4, RoundingMode.HALF_UP);

        if (dailyInterest.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        account.setBalance(account.getBalance().add(dailyInterest));
        accountRepository.save(account);

        Transaction transaction = transactionBuilder.build(
                TransactionType.INTEREST, dailyInterest, null, account,
                account.getOwner(), "Daily interest accrual"
        );
        transactionRepository.save(transaction);
    }
}