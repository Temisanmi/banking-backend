package com.temi.banking_backend.controller;

import com.temi.banking_backend.dto.user.ChangePasswordRequest;
import com.temi.banking_backend.dto.user.ChangePinRequest;
import com.temi.banking_backend.dto.user.UpdateProfileRequest;
import com.temi.banking_backend.dto.user.UserResponse;
import com.temi.banking_backend.entity.User;
import com.temi.banking_backend.security.SecurityUtil;
import com.temi.banking_backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final SecurityUtil securityUtil;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile() {
        User currentUser = securityUtil.getCurrentUser();
        return ResponseEntity.ok(UserResponse.fromEntity(currentUser));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        User currentUser = securityUtil.getCurrentUser();
        UserResponse response = userService.updateProfile(request, currentUser);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        User currentUser = securityUtil.getCurrentUser();
        userService.changePassword(request, currentUser);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/me/pin")
    public ResponseEntity<Void> changePin(@Valid @RequestBody ChangePinRequest request) {
        User currentUser = securityUtil.getCurrentUser();
        userService.changePin(request, currentUser);
        return ResponseEntity.noContent().build();
    }
}
