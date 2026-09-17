package com.temi.banking_backend.service;

import com.temi.banking_backend.dto.user.LoginRequest;
import com.temi.banking_backend.dto.user.RegisterUserRequest;
import com.temi.banking_backend.dto.user.VerifyOtpRequest;
import com.temi.banking_backend.dto.user.AuthResponse;
import com.temi.banking_backend.dto.user.LoginInitiatedResponse;
import com.temi.banking_backend.dto.user.UserResponse;
import com.temi.banking_backend.entity.User;
import com.temi.banking_backend.entity.enums.Role;
import com.temi.banking_backend.exception.EmailAlreadyExistsException;
import com.temi.banking_backend.exception.InvalidCredentialsException;
import com.temi.banking_backend.exception.InvalidOtpException;
import com.temi.banking_backend.exception.PhoneNumberAlreadyExistsException;
import com.temi.banking_backend.exception.UserNotFoundException;
import com.temi.banking_backend.repository.UserRepository;
import com.temi.banking_backend.security.JwtService;
import com.temi.banking_backend.security.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final JwtService jwtService;
    private final UserService userService;

    @Transactional
    public UserResponse register(RegisterUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        String normalizedPhoneNumber = normalizePhoneNumber(request.getPhoneNumber());

        if (userRepository.existsByPhoneNumber(normalizedPhoneNumber)) {
            throw new PhoneNumberAlreadyExistsException(normalizedPhoneNumber);
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(normalizedPhoneNumber);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.CUSTOMER);

        User savedUser = userRepository.save(user);

        return UserResponse.fromEntity(savedUser);
    }

    public LoginInitiatedResponse login(LoginRequest request) {
        User user = getUserOrThrowInvalidCredentials(request.getEmail());

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        String otp = otpService.generateOtp(user.getEmail());

        // TODO: send otp via email once mail sending is set up.
        System.out.println("OTP for " + user.getEmail() + ": " + otp);

        return new LoginInitiatedResponse(
                "OTP sent to your registered email",
                user.getEmail()
        );
    }

    public AuthResponse verifyOtp(VerifyOtpRequest request) {
        boolean isValid = otpService.verifyOtp(request.getEmail(), request.getCode());

        if (!isValid) {
            throw new InvalidOtpException();
        }

        User user = getUserOrThrowInvalidCredentials(request.getEmail());
        String token = jwtService.generateToken(user);

        return new AuthResponse(token, "Bearer", UserResponse.fromEntity(user));
    }

    private User getUserOrThrowInvalidCredentials(String email) {
        try {
            return userService.getUserEntityByEmail(email);
        } catch (UserNotFoundException e) {
            throw new InvalidCredentialsException();
        }
    }

    private String normalizePhoneNumber(String rawPhoneNumber) {
        String cleaned = rawPhoneNumber.trim();

        if (cleaned.startsWith("+")) {
            return cleaned;
        }

        if (cleaned.startsWith("0")) {
            cleaned = cleaned.substring(1);
        }
        return "+234" + cleaned;
    }
}