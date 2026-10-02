package com.temi.banking_backend.controller;

import com.temi.banking_backend.dto.transaction.DepositRequest;
import com.temi.banking_backend.dto.transaction.TransactionResponse;
import com.temi.banking_backend.dto.transaction.TransferRequest;
import com.temi.banking_backend.dto.transaction.WithdrawRequest;
import com.temi.banking_backend.entity.User;
import com.temi.banking_backend.security.SecurityUtil;
import com.temi.banking_backend.service.AccountService;
import com.temi.banking_backend.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;
    private final AccountService accountService;
    private final SecurityUtil securityUtil;

    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponse> deposit(@Valid @RequestBody DepositRequest request) {
        User currentUser = securityUtil.getCurrentUser();
        TransactionResponse response = transactionService.deposit(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(@Valid @RequestBody WithdrawRequest request) {
        User currentUser = securityUtil.getCurrentUser();
        TransactionResponse response = transactionService.withdraw(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(@Valid @RequestBody TransferRequest request) {
        User currentUser = securityUtil.getCurrentUser();
        TransactionResponse response = transactionService.transfer(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/statement/{accountId}")
    public ResponseEntity<Page<TransactionResponse>> getStatement(
            @PathVariable String accountId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        User currentUser = securityUtil.getCurrentUser();

        accountService.getAccountById(accountId, currentUser);

        LocalDateTime effectiveFrom = from != null ? from : LocalDateTime.now().minusYears(10);
        LocalDateTime effectiveTo = to != null ? to : LocalDateTime.now();

        Pageable pageable = PageRequest.of(page, size);
        Page<TransactionResponse> statement = transactionService.getAccountStatement(
                accountId, effectiveFrom, effectiveTo, pageable
        );

        return ResponseEntity.ok(statement);
    }
}
