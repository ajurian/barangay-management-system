package com.barangay.application.usecases;

import com.barangay.domain.entities.User;
import com.barangay.domain.entities.UserRole;
import com.barangay.domain.repositories.IUserRepository;

import java.util.List;

/**
 * Use Case: List Users
 */
public class ListUsersUseCase {
    private final IUserRepository userRepository;

    public ListUsersUseCase(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> execute() {
        return userRepository.findAll();
    }

    public List<User> executeByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    public List<User> executeActiveOnly() {
        return userRepository.findActiveUsers();
    }
}
