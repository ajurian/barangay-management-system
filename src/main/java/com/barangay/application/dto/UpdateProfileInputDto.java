package com.barangay.application.dto;

/**
 * DTO for updating user profile
 * Module 10: Profile Management
 */
public class UpdateProfileInputDto {
    private final String userId;
    private final String linkedResidentId;

    public UpdateProfileInputDto(String userId, String linkedResidentId) {
        this.userId = userId;
        this.linkedResidentId = linkedResidentId != null ? linkedResidentId.trim() : null;
    }

    public String getUserId() {
        return userId;
    }

    public String getLinkedResidentId() {
        return linkedResidentId;
    }
}
