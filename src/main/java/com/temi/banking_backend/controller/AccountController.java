package com.temi.banking_backend.controller;

import com.temi.banking_backend.dto.account.AccountResponse;
import com.temi.banking_backend.dto.account.CreateAccountRequest;
import com.temi.banking_backend.entity.User;
import com.temi.banking_backend.security.SecurityUtil;
import com.temi.banking_backend.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;
    private final SecurityUtil securityUtil;

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        User currentUser = securityUtil.getCurrentUser();
        AccountResponse response = accountService.createAccount(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<List<AccountResponse>> getMyAccounts() {
        User currentUser = securityUtil.getCurrentUser();
        List<AccountResponse> accounts = accountService.getMyAccounts(currentUser);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountResponse> getAccountById(@PathVariable String accountId) {
        User currentUser = securityUtil.getCurrentUser();
        AccountResponse response = accountService.getAccountById(accountId, currentUser);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{accountId}/freeze")
    public ResponseEntity<AccountResponse> freezeAccount(@PathVariable String accountId) {
        User currentUser = securityUtil.getCurrentUser();
        AccountResponse response = accountService.freezeAccount(accountId, currentUser);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{accountId}/close")
    public ResponseEntity<AccountResponse> closeAccount(@PathVariable String accountId) {
        User currentUser = securityUtil.getCurrentUser();
        AccountResponse response = accountService.closeAccount(accountId, currentUser);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{accountId}/reactivate")
    public ResponseEntity<AccountResponse> reactivateAccount(@PathVariable String accountId) {
        User currentUser = securityUtil.getCurrentUser();
        AccountResponse response = accountService.reactivateAccount(accountId, currentUser);
        return ResponseEntity.ok(response);
    }
}
