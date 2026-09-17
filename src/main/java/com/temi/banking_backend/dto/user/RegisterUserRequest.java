package com.temi.banking_backend.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterUserRequest {
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "Invalid first name length")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Invalid last name length")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 72, message = "Invalid password length")
    private String password;
}
