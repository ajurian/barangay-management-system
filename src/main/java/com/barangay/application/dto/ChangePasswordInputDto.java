package com.barangay.application.dto;

/**
 * DTO for changing user password
 * Module 10: Profile Management
 */
public class ChangePasswordInputDto {
    private final String userId;
    private final String currentPassword;
    private final String newPassword;

    public ChangePasswordInputDto(String userId, String currentPassword, String newPassword) {
        this.userId = userId;
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }

    public String getUserId() {
        return userId;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }
}
