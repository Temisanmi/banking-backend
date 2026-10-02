package com.temi.banking_backend.controller;

import com.temi.banking_backend.dto.account.AccountResponse;
import com.temi.banking_backend.dto.account.StaffAccountResponse;
import com.temi.banking_backend.dto.user.UserResponse;
import com.temi.banking_backend.entity.User;
import com.temi.banking_backend.security.SecurityUtil;
import com.temi.banking_backend.service.AccountService;
import com.temi.banking_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/staff")
@PreAuthorize("hasAnyRole('TELLER', 'ADMIN')")
@RequiredArgsConstructor
public class StaffController {
    private final UserService userService;
    private final AccountService accountService;
    private final SecurityUtil securityUtil;

    @GetMapping("/users/lookup")
    public ResponseEntity<UserResponse> lookupUser(@RequestParam String identifier) {
        User currentUser = securityUtil.getCurrentUser();
        UserResponse response = userService.getUserForStaff(identifier, currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users")
    public ResponseEntity<Page<UserResponse>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        User currentUser = securityUtil.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);
        Page<UserResponse> users = userService.getAllUsers(pageable, currentUser);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/accounts/lookup")
    public ResponseEntity<StaffAccountResponse> lookupAccount(@RequestParam String accountNumber) {
        User currentUser = securityUtil.getCurrentUser();
        StaffAccountResponse response = accountService.getAccountByNumber(accountNumber, currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{customerId}/accounts")
    public ResponseEntity<List<StaffAccountResponse>> getCustomerAccounts(@PathVariable String customerId) {
        User currentUser = securityUtil.getCurrentUser();
        List<StaffAccountResponse> accounts = accountService.getAccountsForCustomer(customerId, currentUser);
        return ResponseEntity.ok(accounts);
    }
}
