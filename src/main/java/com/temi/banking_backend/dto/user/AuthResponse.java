package com.temi.banking_backend.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class AuthResponse {
    private String token;
    private String tokenType;
    private UserResponse user;
}
