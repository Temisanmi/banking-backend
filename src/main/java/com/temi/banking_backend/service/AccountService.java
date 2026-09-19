package com.temi.banking_backend.service;

import com.temi.banking_backend.dto.account.AccountResponse;
import com.temi.banking_backend.dto.account.CreateAccountRequest;
import com.temi.banking_backend.entity.Account;
import com.temi.banking_backend.entity.User;
import com.temi.banking_backend.entity.enums.AccountStatus;
import com.temi.banking_backend.entity.enums.Role;
import com.temi.banking_backend.exception.AccountNotFoundException;
import com.temi.banking_backend.exception.InvalidAccountStatusTransitionException;
import com.temi.banking_backend.exception.UnauthorizedAccountAccessException;
import com.temi.banking_backend.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int ACCOUNT_NUMBER_LENGTH = 10;

    private String generateRandomAccountNumber() {
        StringBuilder stringBuilder = new StringBuilder(ACCOUNT_NUMBER_LENGTH);
        for(int i = 0; i < ACCOUNT_NUMBER_LENGTH; i++){
            stringBuilder.append(RANDOM.nextInt(10));
        }
        return stringBuilder.toString();
    }

    private String generateUniqueAccountNumber() {
        String accountNumber;
        do{
            accountNumber = generateRandomAccountNumber();
        } while (accountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
    }

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request, User owner) {
        Account account = new Account();
        account.setAccountNumber(generateUniqueAccountNumber());
        account.setType(request.getType());
        account.setCurrency(request.getCurrency());
        account.setStatus(AccountStatus.ACTIVE);
        account.setOwner(owner);

        Account savedAccount = accountRepository.save(account);
        return AccountResponse.fromEntity(savedAccount);
    }

    private void assertCanView(Account account, User requestingUser) {
        boolean isOwner = account.getOwner().getId().equals(requestingUser.getId());
        boolean isStaff = requestingUser.getRole() == Role.TELLER || requestingUser.getRole() == Role.ADMIN;

        if (!isOwner && !isStaff){
            throw new UnauthorizedAccountAccessException();
        }
    }

    public AccountResponse getAccountById(String accountId, User requestingUser) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        assertCanView(account, requestingUser);
        return AccountResponse.fromEntity(account);
    }

    public List<AccountResponse> getMyAccounts(User owner) {
        return accountRepository.findAllByOwnerId(owner.getId())
                .stream()
                .map(AccountResponse::fromEntity)
                .toList();
    }

    private void assertIsTellerOrAdmin(User actor) {
        if (actor.getRole() != Role.TELLER && actor.getRole() != Role.ADMIN){
            throw new UnauthorizedAccountAccessException();
        }
    }

    private void assertCanFreeze(Account account, User actor){
        boolean isOwner = account.getOwner().getId().equals(actor.getId());
        boolean isStaff = actor.getRole() == Role.TELLER || actor.getRole() == Role.ADMIN;

        if (!isOwner && !isStaff) {
            throw new UnauthorizedAccountAccessException();
        }
    }

    private void assertIsAdmin(User actor) {
        if (actor.getRole() != Role.ADMIN) {
            throw new UnauthorizedAccountAccessException();
        }
    }

    private void assertTransitionAllowed(AccountStatus from, AccountStatus to){
        boolean allowed = switch (from) {
            case ACTIVE-> to == AccountStatus.FROZEN || to == AccountStatus.CLOSED;
            case FROZEN -> to == AccountStatus.ACTIVE || to == AccountStatus.CLOSED;
            case CLOSED -> false;
        };
        if (!allowed){
            throw new InvalidAccountStatusTransitionException(from.name(), to.name());
        }
    }

    @Transactional
    public AccountResponse freezeAccount(String accountId, User actor) {
        Account account = accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        assertCanFreeze(account, actor);

        assertTransitionAllowed(account.getStatus(), AccountStatus.FROZEN);

        account.setStatus(AccountStatus.FROZEN);

        Account savedAccount = accountRepository.save(account);

        return AccountResponse.fromEntity(savedAccount);
    }

    @Transactional
    public AccountResponse closeAccount(String accountId, User actor){
        assertIsAdmin(actor);

        Account account = accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        assertTransitionAllowed(account.getStatus(), AccountStatus.CLOSED);

        account.setStatus(AccountStatus.CLOSED);

        Account savedAccount = accountRepository.save(account);

        return AccountResponse.fromEntity(savedAccount);
    }

    @Transactional
    public AccountResponse reactivateAccount(String accountId, User actor){
        assertIsAdmin(actor);

        Account account = accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        assertTransitionAllowed(account.getStatus(), AccountStatus.ACTIVE);

        account.setStatus(AccountStatus.ACTIVE);

        Account savedAccount = accountRepository.save(account);

        return AccountResponse.fromEntity(savedAccount);
    }
}
