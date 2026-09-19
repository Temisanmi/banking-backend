package com.temi.banking_backend.controller;

import com.temi.banking_backend.dto.user.CreateStaffRequest;
import com.temi.banking_backend.dto.user.UserResponse;
import com.temi.banking_backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AuthService authService;

    @PostMapping("/staff")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createStaffUser(@Valid @RequestBody CreateStaffRequest request) {
        UserResponse response = authService.createStaffUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
