package com.temi.banking_backend.service;

import com.temi.banking_backend.config.PhoneNumberUtil;
import com.temi.banking_backend.dto.user.ChangePasswordRequest;
import com.temi.banking_backend.dto.user.ChangePinRequest;
import com.temi.banking_backend.dto.user.UpdateProfileRequest;
import com.temi.banking_backend.dto.user.UserResponse;
import com.temi.banking_backend.entity.User;
import com.temi.banking_backend.exception.InvalidCredentialsException;
import com.temi.banking_backend.exception.InvalidPinException;
import com.temi.banking_backend.exception.PhoneNumberAlreadyExistsException;
import com.temi.banking_backend.exception.UserNotFoundException;
import com.temi.banking_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PhoneNumberUtil phoneNumberUtil;

    public User getUserEntityByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
    }

    public UserResponse findByEmail(String email) {
        User user = getUserEntityByEmail(email);
        return UserResponse.fromEntity(user);
    }

    @Transactional
    public UserResponse updateProfile(UpdateProfileRequest request, User currentUser) {
        String normalizedPhoneNumber = phoneNumberUtil.normalize(request.getPhoneNumber());

        boolean phoneChanged = !normalizedPhoneNumber.equals(currentUser.getPhoneNumber());

        if (phoneChanged && userRepository.existsByPhoneNumber(normalizedPhoneNumber)) {
            throw new PhoneNumberAlreadyExistsException(normalizedPhoneNumber);
        }

        currentUser.setFirstName(request.getFirstName());

        currentUser.setLastName(request.getLastName());

        currentUser.setPhoneNumber(normalizedPhoneNumber);

        User savedUser = userRepository.save(currentUser);
        return UserResponse.fromEntity(savedUser);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request, User currentUser) {
        if (!passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPassword())) {
            throw new InvalidCredentialsException();
        }

        currentUser.setPassword(passwordEncoder.encode(request.getNewPassword()));

        userRepository.save(currentUser);
    }

    @Transactional
    public void changePin(ChangePinRequest request, User currentUser) {
        if (!passwordEncoder.matches(request.getCurrentPin(), currentUser.getTransactionPin())) {
            throw new InvalidPinException();
        }

        currentUser.setTransactionPin(passwordEncoder.encode(request.getNewPin()));

        userRepository.save(currentUser);
    }
}
