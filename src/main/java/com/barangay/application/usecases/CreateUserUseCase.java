package com.barangay.application.usecases;

import com.barangay.application.dto.CreateUserInputDto;
import com.barangay.application.ports.IPasswordHasher;
import com.barangay.application.services.PasswordValidator;
import com.barangay.application.services.SessionManager;
import com.barangay.domain.entities.Resident;
import com.barangay.domain.entities.ResidentId;
import com.barangay.domain.entities.User;
import com.barangay.domain.exceptions.UnauthorizedOperationException;
import com.barangay.domain.repositories.IResidentRepository;
import com.barangay.domain.repositories.IUserRepository;
import com.barangay.domain.valueobjects.UserId;

/**
 * Use Case: Create User Account
 * Following SRP: Handles only user creation logic.
 * Following business rules for role-based account creation.
 */
public class CreateUserUseCase {
    private final IUserRepository userRepository;
    private final IResidentRepository residentRepository;
    private final IPasswordHasher passwordHasher;
    private final PasswordValidator passwordValidator;
    private final SessionManager sessionManager;

    public CreateUserUseCase(IUserRepository userRepository,
            IResidentRepository residentRepository,
            IPasswordHasher passwordHasher,
            PasswordValidator passwordValidator,
            SessionManager sessionManager) {
        this.userRepository = userRepository;
        this.residentRepository = residentRepository;
        this.passwordHasher = passwordHasher;
        this.passwordValidator = passwordValidator;
        this.sessionManager = sessionManager;
    }

    public String execute(CreateUserInputDto input) {
        // Get current user
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null) {
            throw new UnauthorizedOperationException("No user logged in");
        }

        // Check if current user can create this role
        if (!currentUser.canCreateRole(input.getRole())) {
            throw new UnauthorizedOperationException(
                    "You are not authorized to create " + input.getRole() + " accounts");
        }

        // Validate password
        if (!passwordValidator.isValid(input.getPassword())) {
            throw new IllegalArgumentException(passwordValidator.getValidationMessage());
        }

        // Validate username
        if (input.getUsername() == null || input.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }

        // Check if username already exists
        if (userRepository.findByUsername(input.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Validate linked resident id
        String linkedResidentIdValue = input.getLinkedResidentId();
        if (linkedResidentIdValue == null || linkedResidentIdValue.isEmpty()) {
            throw new IllegalArgumentException("Linked resident ID is required for user accounts.");
        }

        ResidentId residentId = ResidentId.fromString(linkedResidentIdValue);
        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Resident not found. Please register the resident before creating the account."));
        if (!resident.isActive()) {
            throw new IllegalArgumentException("Cannot link to an inactive resident record.");
        }

        // Create new user
        String passwordHash = passwordHasher.hash(input.getPassword());
        User newUser = new User(
                UserId.generate(),
                input.getUsername(),
                passwordHash,
                input.getRole());

        newUser.setLinkedResidentId(residentId);

        // Save user
        userRepository.save(newUser);

        return newUser.getId().getValue();
    }
}
