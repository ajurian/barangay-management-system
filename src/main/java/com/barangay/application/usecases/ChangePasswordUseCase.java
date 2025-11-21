package com.barangay.application.usecases;

import com.barangay.application.dto.ChangePasswordInputDto;
import com.barangay.application.ports.IPasswordHasher;
import com.barangay.application.services.PasswordValidator;
import com.barangay.application.services.SessionManager;
import com.barangay.domain.entities.User;
import com.barangay.domain.valueobjects.UserId;
import com.barangay.domain.repositories.IUserRepository;

import java.util.Optional;

/**
 * Use Case: Change user password
 * Module 10: Profile Management
 * 
 * SOLID Principles:
 * - SRP: Single responsibility - changing password
 * - DIP: Depends on repository and hasher abstractions
 */
public class ChangePasswordUseCase {
    private final IUserRepository userRepository;
    private final IPasswordHasher passwordHasher;
    private final PasswordValidator passwordValidator;
    private final SessionManager sessionManager;

    public ChangePasswordUseCase(IUserRepository userRepository,
            IPasswordHasher passwordHasher,
            PasswordValidator passwordValidator,
            SessionManager sessionManager) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.passwordValidator = passwordValidator;
        this.sessionManager = sessionManager;
    }

    public void execute(ChangePasswordInputDto input) {
        // Verify user can only change their own password
        UserId currentUserId = sessionManager.getCurrentUser().getId();
        if (!currentUserId.getValue().equals(input.getUserId())) {
            throw new IllegalArgumentException("Cannot change another user's password");
        }

        // Find user
        UserId userId = UserId.fromString(input.getUserId());
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new IllegalArgumentException("User not found");
        }

        User user = userOpt.get();

        // Verify current password
        if (!passwordHasher.verify(input.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // Validate new password
        if (!passwordValidator.isValid(input.getNewPassword())) {
            throw new IllegalArgumentException("New password does not meet requirements");
        }

        // Hash and update password
        String newHash = passwordHasher.hash(input.getNewPassword());
        user.setPasswordHash(newHash);

        userRepository.save(user);
    }
}
