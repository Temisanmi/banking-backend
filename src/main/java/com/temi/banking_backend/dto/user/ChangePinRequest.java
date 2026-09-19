package com.temi.banking_backend.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePinRequest {
    @NotBlank(message = "Insert current PIN")
    private String currentPin;

    @NotBlank(message = "Insert new PIN")
    @Pattern(regexp = "^\\d{4}$", message = "PIN must be exactly 4 digits")
    private String newPin;
}
