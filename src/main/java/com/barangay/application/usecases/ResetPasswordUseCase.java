package com.barangay.application.usecases;

import com.barangay.application.ports.IPasswordHasher;
import com.barangay.application.services.PasswordValidator;
import com.barangay.application.services.SessionManager;
import com.barangay.domain.entities.User;
import com.barangay.domain.exceptions.UnauthorizedOperationException;
import com.barangay.domain.exceptions.UserNotFoundException;
import com.barangay.domain.repositories.IUserRepository;
import com.barangay.domain.valueobjects.UserId;

/**
 * Use Case: Reset User Password
 * SUPER_ADMIN can reset passwords for ADMIN, CLERK, RESIDENT
 * ADMIN can reset passwords for CLERK, RESIDENT
 * CLERK can reset passwords for RESIDENT
 */
public class ResetPasswordUseCase {
    private final IUserRepository userRepository;
    private final IPasswordHasher passwordHasher;
    private final PasswordValidator passwordValidator;
    private final SessionManager sessionManager;

    public ResetPasswordUseCase(IUserRepository userRepository,
            IPasswordHasher passwordHasher,
            PasswordValidator passwordValidator,
            SessionManager sessionManager) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.passwordValidator = passwordValidator;
        this.sessionManager = sessionManager;
    }

    public void execute(String targetUserId, String newPassword) {
        // Get current user
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null) {
            throw new UnauthorizedOperationException("No user logged in");
        }

        // Find target user
        User targetUser = userRepository.findById(UserId.fromString(targetUserId))
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Check if current user can manage target user
        if (!currentUser.canManage(targetUser)) {
            throw new UnauthorizedOperationException("You are not authorized to reset this user's password");
        }

        // Validate new password
        if (!passwordValidator.isValid(newPassword)) {
            throw new IllegalArgumentException(passwordValidator.getValidationMessage());
        }

        // Reset password
        String newPasswordHash = passwordHasher.hash(newPassword);
        targetUser.setPasswordHash(newPasswordHash);
        userRepository.save(targetUser);
    }
}
