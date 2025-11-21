package com.barangay.application.usecases;

import com.barangay.application.dto.SetupInputDto;
import com.barangay.application.ports.IPasswordHasher;
import com.barangay.application.services.PasswordValidator;
import com.barangay.domain.entities.User;
import com.barangay.domain.entities.UserRole;
import com.barangay.domain.repositories.IUserRepository;
import com.barangay.domain.valueobjects.UserId;

/**
 * Use Case: First-Run System Setup
 * Creates the initial super admin account.
 * Following SRP: Handles only system setup logic.
 */
public class SystemSetupUseCase {
    private final IUserRepository userRepository;
    private final IPasswordHasher passwordHasher;
    private final PasswordValidator passwordValidator;

    public SystemSetupUseCase(IUserRepository userRepository,
            IPasswordHasher passwordHasher,
            PasswordValidator passwordValidator) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.passwordValidator = passwordValidator;
    }

    /**
     * Execute system setup
     */
    public void execute(SetupInputDto input) {
        // Check if super admin already exists
        if (userRepository.hasSuperAdmin()) {
            throw new IllegalStateException("System is already set up");
        }

        // Validate password
        if (!passwordValidator.isValid(input.getPassword())) {
            throw new IllegalArgumentException(passwordValidator.getValidationMessage());
        }

        // Validate required fields
        if (input.getUsername() == null || input.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }

        // Create super admin user
        String passwordHash = passwordHasher.hash(input.getPassword());
        User superAdmin = new User(
                UserId.generate(),
                input.getUsername(),
                passwordHash,
                UserRole.SUPER_ADMIN);

        // Save user
        userRepository.save(superAdmin);
    }

    /**
     * Check if system needs setup
     */
    public boolean needsSetup() {
        return !userRepository.hasSuperAdmin();
    }
}
