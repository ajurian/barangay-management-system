package com.barangay.domain.entities;

import com.barangay.domain.valueobjects.UserId;
import java.time.LocalDateTime;

/**
 * Domain Entity: User
 * Represents a system user with role-based access control.
 * Following SRP: Handles only user entity data and basic validation.
 */
public class User {
    private final UserId id;
    private String username;
    private String passwordHash;
    private UserRole role;
    private ResidentId linkedResidentId; // Optional link to Resident entity
    private boolean isActive;
    private final LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    private LocalDateTime updatedAt;

    public User(UserId id, String username, String passwordHash, UserRole role) {
        if (id == null || username == null || passwordHash == null || role == null) {
            throw new IllegalArgumentException("Required fields cannot be null");
        }
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters
    public UserId getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public ResidentId getLinkedResidentId() {
        return linkedResidentId;
    }

    public boolean isActive() {
        return isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Setters with business rules
    public void setPasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.isEmpty()) {
            throw new IllegalArgumentException("Password hash cannot be null or empty");
        }
        this.passwordHash = passwordHash;
        this.updatedAt = LocalDateTime.now();
    }

    public void setLinkedResidentId(ResidentId linkedResidentId) {
        this.linkedResidentId = linkedResidentId;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void reactivate() {
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void recordLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    public void updateRole(UserRole newRole) {
        if (newRole == null) {
            throw new IllegalArgumentException("User role cannot be null");
        }
        this.role = newRole;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Business rule: Check if user can manage another user based on role hierarchy
     */
    public boolean canManage(User otherUser) {
        switch (this.role) {
            case SUPER_ADMIN:
                return otherUser.role == UserRole.ADMIN ||
                        otherUser.role == UserRole.CLERK ||
                        otherUser.role == UserRole.RESIDENT;
            case ADMIN:
                return otherUser.role == UserRole.CLERK ||
                        otherUser.role == UserRole.RESIDENT;
            case CLERK:
                return otherUser.role == UserRole.RESIDENT;
            default:
                return false;
        }
    }

    /**
     * Business rule: Check if user can create accounts of a specific role
     */
    public boolean canCreateRole(UserRole targetRole) {
        switch (this.role) {
            case SUPER_ADMIN:
                return targetRole == UserRole.ADMIN ||
                        targetRole == UserRole.CLERK ||
                        targetRole == UserRole.RESIDENT;
            case ADMIN:
                return targetRole == UserRole.CLERK || targetRole == UserRole.RESIDENT;
            case CLERK:
                return targetRole == UserRole.RESIDENT;
            default:
                return false;
        }
    }
}
