package com.temi.banking_backend.service;

import com.temi.banking_backend.config.PhoneNumberUtil;
import com.temi.banking_backend.dto.user.ChangePasswordRequest;
import com.temi.banking_backend.dto.user.ChangePinRequest;
import com.temi.banking_backend.dto.user.UpdateProfileRequest;
import com.temi.banking_backend.dto.user.UserResponse;
import com.temi.banking_backend.entity.User;
import com.temi.banking_backend.entity.enums.Role;
import com.temi.banking_backend.exception.*;
import com.temi.banking_backend.repository.AccountRepository;
import com.temi.banking_backend.repository.TransactionRepository;
import com.temi.banking_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;
    private final PhoneNumberUtil phoneNumberUtil;

    public User getUserEntityByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
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

    private void assertIsStaff(User requestingUser) {
        if (requestingUser.getRole() != Role.TELLER && requestingUser.getRole() != Role.ADMIN) {
            throw new UnauthorizedAccountAccessException();
        }
    }

    public UserResponse getUserForStaff(String identifier, User requestingUser) {
        assertIsStaff(requestingUser);

        User user = userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByPhoneNumber(identifier))
                .or(() -> userRepository.findById(identifier))
                .orElseThrow(() -> new UserNotFoundException(identifier));

        return UserResponse.fromEntity(user);
    }

    private void assertIsAdmin(User requestingUser) {
        if (requestingUser.getRole() != Role.ADMIN) {
            throw new UnauthorizedAccountAccessException();
        }
    }

    public Page<UserResponse> getAllUsers(Pageable pageable, User requestingUser) {
        assertIsAdmin(requestingUser);

        return userRepository.findAll(pageable).map(UserResponse::fromEntity);
    }

    @Transactional
    public void deleteUser(String userId, User requestingUser) {
        assertIsAdmin(requestingUser);

        User userToDelete = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (userToDelete.getId().equals(requestingUser.getId())) {
            throw new UserDeletionNotAllowedException("you cannot delete your own account");
        }

        if (accountRepository.existsByOwnerId(userId)) {
            throw new UserDeletionNotAllowedException("this user owns one or more accounts");
        }

        if (transactionRepository.existsByPerformedById(userId)) {
            throw new UserDeletionNotAllowedException("this user has performed transactions on record");
        }

        userRepository.delete(userToDelete);
    }
}
