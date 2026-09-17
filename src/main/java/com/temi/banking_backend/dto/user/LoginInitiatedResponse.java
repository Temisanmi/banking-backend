package com.temi.banking_backend.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class LoginInitiatedResponse {
    private String message;
    private String email;
}
