package com.temi.banking_backend.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email")
    private String email;

    @NotBlank(message = "Reset code is required")
    private String code;

    @NotBlank(message = "Enter new password")
    @Size(min = 6, max = 72, message = "Invalid password length")
    private String newPassword;
}
