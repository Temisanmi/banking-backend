package com.temi.banking_backend.controller;

import com.temi.banking_backend.dto.transaction.TransactionResponse;
import com.temi.banking_backend.entity.User;
import com.temi.banking_backend.security.SecurityUtil;
import com.temi.banking_backend.service.AccountService;
import com.temi.banking_backend.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;
    private final AccountService accountService;
    private final SecurityUtil securityUtil;

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
