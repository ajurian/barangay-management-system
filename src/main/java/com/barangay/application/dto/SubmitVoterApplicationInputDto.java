package com.barangay.application.dto;

import com.barangay.domain.entities.ApplicationType;

/**
 * DTO for submitting voter application
 */
public class SubmitVoterApplicationInputDto {
    private final String residentId;
    private final ApplicationType applicationType;
    private final String currentRegistrationDetails;
    private final String validIdFrontPath;
    private final String validIdBackPath;

    public SubmitVoterApplicationInputDto(String residentId, ApplicationType applicationType,
            String currentRegistrationDetails,
            String validIdFrontPath, String validIdBackPath) {
        this.residentId = residentId;
        this.applicationType = applicationType;
        this.currentRegistrationDetails = currentRegistrationDetails;
        this.validIdFrontPath = validIdFrontPath;
        this.validIdBackPath = validIdBackPath;
    }

    public String getResidentId() {
        return residentId;
    }

    public ApplicationType getApplicationType() {
        return applicationType;
    }

    public String getCurrentRegistrationDetails() {
        return currentRegistrationDetails;
    }

    public String getValidIdFrontPath() {
        return validIdFrontPath;
    }

    public String getValidIdBackPath() {
        return validIdBackPath;
    }
}
