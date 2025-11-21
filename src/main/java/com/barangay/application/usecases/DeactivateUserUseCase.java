package com.barangay.application.usecases;

import com.barangay.application.services.SessionManager;
import com.barangay.domain.entities.User;
import com.barangay.domain.exceptions.UnauthorizedOperationException;
import com.barangay.domain.exceptions.UserNotFoundException;
import com.barangay.domain.repositories.IUserRepository;
import com.barangay.domain.valueobjects.UserId;

/**
 * Use Case: Deactivate User Account
 */
public class DeactivateUserUseCase {
    private final IUserRepository userRepository;
    private final SessionManager sessionManager;

    public DeactivateUserUseCase(IUserRepository userRepository, SessionManager sessionManager) {
        this.userRepository = userRepository;
        this.sessionManager = sessionManager;
    }

    public void execute(String targetUserId) {
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
            throw new UnauthorizedOperationException("You are not authorized to deactivate this user");
        }

        // Deactivate user
        targetUser.deactivate();
        userRepository.save(targetUser);
    }
}
