package com.barangay.domain.repositories;

import com.barangay.domain.entities.ResidentId;
import com.barangay.domain.entities.User;
import com.barangay.domain.entities.UserRole;
import com.barangay.domain.valueobjects.UserId;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entity.
 * Following DIP: High-level domain code depends on this abstraction,
 * not on concrete database implementations.
 */
public interface IUserRepository {
    /**
     * Save a new user or update existing user
     */
    void save(User user);

    /**
     * Find user by ID
     */
    Optional<User> findById(UserId id);

    /**
     * Find user by username
     */
    Optional<User> findByUsername(String username);

    /**
     * Find all users
     */
    List<User> findAll();

    /**
     * Find users by role
     */
    List<User> findByRole(UserRole role);

    /**
     * Find user linked to a resident ID
     */
    Optional<User> findByLinkedResidentId(ResidentId residentId);

    /**
     * Find active users
     */
    List<User> findActiveUsers();

    /**
     * Check if any super admin exists
     */
    boolean hasSuperAdmin();

    /**
     * Count users by role
     */
    int countByRole(UserRole role);

    /**
     * Delete user (soft delete by setting isActive = false)
     */
    void delete(UserId id);
}
