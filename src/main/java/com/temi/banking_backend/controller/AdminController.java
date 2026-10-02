package com.temi.banking_backend.controller;

import com.temi.banking_backend.dto.transaction.TransactionResponse;
import com.temi.banking_backend.dto.user.CreateStaffRequest;
import com.temi.banking_backend.dto.user.UserResponse;
import com.temi.banking_backend.entity.User;
import com.temi.banking_backend.security.SecurityUtil;
import com.temi.banking_backend.service.AuthService;
import com.temi.banking_backend.service.TransactionReversalService;
import com.temi.banking_backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AuthService authService;
    private final UserService userService;
    private final TransactionReversalService reversalService;
    private final SecurityUtil securityUtil;

    @PostMapping("/staff")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createStaffUser(@Valid @RequestBody CreateStaffRequest request) {
        UserResponse response = authService.createStaffUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/staff/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteStaffUser(@PathVariable String userId) {
        User currentUser = securityUtil.getCurrentUser();
        userService.deleteUser(userId, currentUser);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/transactions/{transactionId}/reverse")
    public ResponseEntity<TransactionResponse> reverseTransaction(@PathVariable String transactionId) {
        User currentUser = securityUtil.getCurrentUser();
        TransactionResponse response = reversalService.reverseTransaction(transactionId, currentUser);
        return ResponseEntity.ok(response);
    }
}
