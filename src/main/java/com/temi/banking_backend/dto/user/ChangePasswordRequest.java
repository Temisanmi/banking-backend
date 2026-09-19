package com.temi.banking_backend.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordRequest {
    @NotBlank(message = "Insert current password")
    private String currentPassword;

    @NotBlank(message = "Insert new password")
    @Size(min = 6, max = 72, message = "Invalid password length")
    private String newPassword;
}
