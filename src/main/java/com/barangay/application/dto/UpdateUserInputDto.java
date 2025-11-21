package com.barangay.application.dto;

/**
 * DTO for updating user information
 */
public class UpdateUserInputDto {
    private final String userId;

    public UpdateUserInputDto(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
}
