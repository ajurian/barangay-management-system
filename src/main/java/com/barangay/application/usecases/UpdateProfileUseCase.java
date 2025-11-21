package com.barangay.application.usecases;

import com.barangay.application.dto.UpdateProfileInputDto;
import com.barangay.application.services.SessionManager;
import com.barangay.domain.entities.Resident;
import com.barangay.domain.entities.ResidentId;
import com.barangay.domain.entities.User;
import com.barangay.domain.entities.UserRole;
import com.barangay.domain.exceptions.UnauthorizedOperationException;
import com.barangay.domain.repositories.IResidentRepository;
import com.barangay.domain.repositories.IUserRepository;
import com.barangay.domain.valueobjects.UserId;

import java.util.Optional;

/**
 * Use Case: Update user profile
 * Module 10: Profile Management
 * 
 * SOLID Principles:
 * - SRP: Single responsibility - updating user profile
 * - DIP: Depends on repository abstractions
 */
public class UpdateProfileUseCase {
    private final IUserRepository userRepository;
    private final IResidentRepository residentRepository;
    private final SessionManager sessionManager;

    public UpdateProfileUseCase(IUserRepository userRepository,
            IResidentRepository residentRepository,
            SessionManager sessionManager) {
        this.userRepository = userRepository;
        this.residentRepository = residentRepository;
        this.sessionManager = sessionManager;
    }

    public void execute(UpdateProfileInputDto input) {
        // Verify user can only update their own profile
        User currentUser = sessionManager.getCurrentUser();
        UserId currentUserId = currentUser.getId();
        if (!currentUserId.getValue().equals(input.getUserId())) {
            throw new IllegalArgumentException("Cannot update another user's profile");
        }

        // Find user
        UserId userId = UserId.fromString(input.getUserId());
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new IllegalArgumentException("User not found");
        }

        User user = userOpt.get();

        if (input.getLinkedResidentId() != null) {
            if (currentUser.getRole() != UserRole.SUPER_ADMIN) {
                throw new UnauthorizedOperationException(
                        "Only the super admin can link a resident record to this account.");
            }

            String trimmedId = input.getLinkedResidentId().trim();
            if (trimmedId.isEmpty()) {
                throw new IllegalArgumentException("Linked resident ID cannot be blank.");
            }

            ResidentId residentId = ResidentId.fromString(trimmedId);
            Resident resident = residentRepository.findById(residentId)
                    .orElseThrow(() -> new IllegalArgumentException("Resident not found for the provided ID."));

            if (!resident.isActive()) {
                throw new IllegalArgumentException("Cannot link to an inactive resident record.");
            }

            userRepository.findByLinkedResidentId(residentId)
                    .filter(linkedUser -> !linkedUser.getId().equals(userId))
                    .ifPresent(linkedUser -> {
                        throw new IllegalArgumentException(
                                "Another user is already linked to this resident record.");
                    });

            user.setLinkedResidentId(residentId);
        }

        userRepository.save(user);
    }
}
