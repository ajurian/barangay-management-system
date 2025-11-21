package com.barangay.application.dto;

import com.barangay.domain.entities.UserRole;

/**
 * DTO for creating a new user
 */
public class CreateUserInputDto {
    private final String username;
    private final String password;
    private final UserRole role;
    private final String linkedResidentId;

    public CreateUserInputDto(String username, String password, UserRole role,
            String linkedResidentId) {
        this.username = username != null ? username.trim() : null;
        this.password = password;
        this.role = role;
        this.linkedResidentId = linkedResidentId != null ? linkedResidentId.trim() : null;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public UserRole getRole() {
        return role;
    }

    public String getLinkedResidentId() {
        return linkedResidentId;
    }
}
