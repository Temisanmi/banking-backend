package com.temi.banking_backend.service;

import com.temi.banking_backend.config.PhoneNumberUtil;
import com.temi.banking_backend.dto.user.*;
import com.temi.banking_backend.entity.User;
import com.temi.banking_backend.entity.enums.Role;
import com.temi.banking_backend.exception.*;
import com.temi.banking_backend.repository.UserRepository;
import com.temi.banking_backend.security.JwtService;
import com.temi.banking_backend.security.LoginAttemptService;
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
    private final EmailService emailService;
    private final PhoneNumberUtil phoneNumberUtil;
    private final LoginAttemptService loginAttemptService;
    private final PasswordResetService passwordResetService;

    @Transactional
    public UserResponse register(RegisterUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        String normalizedPhoneNumber = phoneNumberUtil.normalize(request.getPhoneNumber());

        if (userRepository.existsByPhoneNumber(normalizedPhoneNumber)) {
            throw new PhoneNumberAlreadyExistsException(normalizedPhoneNumber);
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(normalizedPhoneNumber);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setTransactionPin(passwordEncoder.encode(request.getTransactionPin()));
        user.setRole(Role.CUSTOMER);

        User savedUser = userRepository.save(user);

        return UserResponse.fromEntity(savedUser);
    }

    private User getUserOrThrowInvalidCredentials(String email) {
        try {
            return userService.getUserEntityByEmail(email);
        } catch (UserNotFoundException e) {
            throw new InvalidCredentialsException();
        }
    }

    public LoginInitiatedResponse login(LoginRequest request) {
        loginAttemptService.assertNotLocked(request.getEmail());

        User user = getUserOrThrowInvalidCredentials(request.getEmail());

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            loginAttemptService.recordFailure(request.getEmail());

            throw new InvalidCredentialsException();
        }

        loginAttemptService.recordSuccess(request.getEmail());

        String otp = otpService.generateOtp(user.getEmail());

        emailService.sendOtpEmail(user.getEmail(), otp);

        return new LoginInitiatedResponse(
                "An OTP has been sent to your registered email",
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

    @Transactional
    public UserResponse createStaffUser(CreateStaffRequest request) {
        if (request.getRole() == Role.CUSTOMER) {
            throw new InvalidStaffRoleException();
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        String normalizedPhoneNumber = phoneNumberUtil.normalize(request.getPhoneNumber());

        if (userRepository.existsByPhoneNumber(normalizedPhoneNumber)) {
            throw new PhoneNumberAlreadyExistsException(normalizedPhoneNumber);
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(normalizedPhoneNumber);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setTransactionPin(passwordEncoder.encode(request.getTransactionPin()));
        user.setRole(request.getRole());

        User savedUser = userRepository.save(user);

        return UserResponse.fromEntity(savedUser);
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            String code = passwordResetService.generateResetCode(user.getEmail());

            emailService.sendPasswordResetEmail(user.getEmail(), code);
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        boolean isValid = passwordResetService.verifyResetCode(request.getEmail(), request.getCode());

        if (!isValid) {
            throw new InvalidResetCodeException();
        }

        User user = getUserOrThrowInvalidCredentials(request.getEmail());

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        userRepository.save(user);
    }
}
