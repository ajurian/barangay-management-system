package com.barangay.application.usecases;

import com.barangay.application.services.SessionManager;
import com.barangay.domain.entities.User;
import com.barangay.domain.entities.UserRole;
import com.barangay.domain.exceptions.UnauthorizedOperationException;
import com.barangay.domain.exceptions.UserNotFoundException;
import com.barangay.domain.repositories.IUserRepository;
import com.barangay.domain.valueobjects.UserId;

/**
 * Use Case: Change User Role
 */
public class ChangeUserRoleUseCase {
    private final IUserRepository userRepository;
    private final SessionManager sessionManager;

    public ChangeUserRoleUseCase(IUserRepository userRepository, SessionManager sessionManager) {
        this.userRepository = userRepository;
        this.sessionManager = sessionManager;
    }

    public void execute(String targetUserId, UserRole newRole) {
        if (targetUserId == null || targetUserId.trim().isEmpty()) {
            throw new IllegalArgumentException("Target user ID is required");
        }
        if (newRole == null) {
            throw new IllegalArgumentException("New role is required");
        }
        String sanitizedId = targetUserId.trim();

        User actingUser = sessionManager.getCurrentUser();
        if (actingUser == null) {
            throw new UnauthorizedOperationException("No user logged in");
        }

        User targetUser = userRepository.findById(UserId.fromString(sanitizedId))
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (actingUser.getId().equals(targetUser.getId())) {
            throw new UnauthorizedOperationException("You cannot change your own role");
        }

        if (!actingUser.canManage(targetUser)) {
            throw new UnauthorizedOperationException("You can only change roles of lower-level accounts");
        }

        if (!actingUser.canCreateRole(newRole)) {
            throw new UnauthorizedOperationException("You cannot assign role " + newRole);
        }

        if (targetUser.getRole() == newRole) {
            throw new IllegalArgumentException("User already has role " + newRole);
        }

        targetUser.updateRole(newRole);
        userRepository.save(targetUser);
    }
}
