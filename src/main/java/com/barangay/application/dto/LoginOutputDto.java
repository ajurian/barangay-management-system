package com.barangay.application.dto;

import com.barangay.domain.entities.UserRole;

/**
 * DTO for user login output
 */
public class LoginOutputDto {
    private final String userId;
    private final String username;
    private final UserRole role;
    private final boolean success;
    private final String message;

    public LoginOutputDto(String userId, String username, UserRole role, boolean success, String message) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.success = success;
        this.message = message;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public UserRole getRole() {
        return role;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
