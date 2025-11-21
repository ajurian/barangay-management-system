package com.barangay.application.usecases;

import com.barangay.application.dto.LoginInputDto;
import com.barangay.application.dto.LoginOutputDto;
import com.barangay.application.ports.IPasswordHasher;
import com.barangay.domain.entities.User;
import com.barangay.domain.exceptions.InvalidCredentialsException;
import com.barangay.domain.repositories.IUserRepository;

/**
 * Use Case: User Login
 * Following SRP: Handles only the login business logic.
 * Following DIP: Depends on abstractions (interfaces), not concrete
 * implementations.
 */
public class LoginUseCase {
    private final IUserRepository userRepository;
    private final IPasswordHasher passwordHasher;

    // Constructor injection (DIP)
    public LoginUseCase(IUserRepository userRepository, IPasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    /**
     * Execute login use case
     */
    public LoginOutputDto execute(LoginInputDto input) {
        // Find user by username
        User user = userRepository.findByUsername(input.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

        // Check if user is active
        if (!user.isActive()) {
            throw new InvalidCredentialsException("Account is deactivated");
        }

        // Verify password
        if (!passwordHasher.verify(input.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        // Record login
        user.recordLogin();
        userRepository.save(user);

        // Return success response
        return new LoginOutputDto(
                user.getId().getValue(),
                user.getUsername(),
                user.getRole(),
                true,
                "Login successful");
    }
}
