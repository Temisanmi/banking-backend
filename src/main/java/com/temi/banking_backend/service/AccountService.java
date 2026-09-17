package com.temi.banking_backend.service;

import com.temi.banking_backend.dto.account.AccountResponse;
import com.temi.banking_backend.dto.account.CreateAccountRequest;
import com.temi.banking_backend.entity.Account;
import com.temi.banking_backend.entity.User;
import com.temi.banking_backend.entity.enums.AccountStatus;
import com.temi.banking_backend.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int ACCOUNT_NUMBER_LENGTH = 10;

    private String generateRandomAccountNumber(){
        StringBuilder stringBuilder = new StringBuilder(ACCOUNT_NUMBER_LENGTH);
        for(int i = 0; i < ACCOUNT_NUMBER_LENGTH; i++){
            stringBuilder.append(RANDOM.nextInt(10));
        }
        return stringBuilder.toString();
    }
    private String generateUniqueAccountNumber(){
        String accountNumber;
        do{
            accountNumber = generateRandomAccountNumber();
        } while (accountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
    }

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request, User owner){
        Account account = new Account();
        account.setAccountNumber(generateUniqueAccountNumber());
        account.setType(request.getType());
        account.setCurrency(request.getCurrency());
        account.setStatus(AccountStatus.ACTIVE);
        account.setOwner(owner);

        Account savedAccount = accountRepository.save(account);
        return AccountResponse.fromEntity(savedAccount);
    }
}
