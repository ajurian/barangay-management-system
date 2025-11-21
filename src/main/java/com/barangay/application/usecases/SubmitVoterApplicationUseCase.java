package com.barangay.application.usecases;

import com.barangay.application.dto.SubmitVoterApplicationInputDto;
import com.barangay.application.services.SessionManager;
import com.barangay.domain.entities.*;
import com.barangay.domain.exceptions.ResidentNotFoundException;
import com.barangay.domain.exceptions.UnauthorizedOperationException;
import com.barangay.domain.repositories.IResidentRepository;
import com.barangay.domain.repositories.IVoterApplicationRepository;

/**
 * Use Case: Submit Voter Application
 * RESIDENT can submit their own voter application
 */
public class SubmitVoterApplicationUseCase {
    private final IVoterApplicationRepository applicationRepository;
    private final IResidentRepository residentRepository;
    private final SessionManager sessionManager;

    public SubmitVoterApplicationUseCase(IVoterApplicationRepository applicationRepository,
            IResidentRepository residentRepository,
            SessionManager sessionManager) {
        this.applicationRepository = applicationRepository;
        this.residentRepository = residentRepository;
        this.sessionManager = sessionManager;
    }

    public String execute(SubmitVoterApplicationInputDto input) {
        // Check if user is logged in
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null) {
            throw new UnauthorizedOperationException("No user logged in");
        }

        // Verify resident exists
        ResidentId residentId = ResidentId.fromString(input.getResidentId());
        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new ResidentNotFoundException("Resident not found"));

        if (!resident.isActive()) {
            throw new IllegalStateException("Cannot submit application for inactive resident");
        }

        // Validate required fields
        if (input.getValidIdFrontPath() == null || input.getValidIdFrontPath().isEmpty()) {
            throw new IllegalArgumentException("Valid ID (front) is required");
        }
        if (input.getValidIdBackPath() == null || input.getValidIdBackPath().isEmpty()) {
            throw new IllegalArgumentException("Valid ID (back) is required");
        }

        // Generate application ID
        String applicationId = applicationRepository.generateNextId();

        // Create voter application
        VoterApplication application = new VoterApplication(
                applicationId,
                residentId,
                input.getApplicationType());

        application.setValidIdFrontPath(input.getValidIdFrontPath());
        application.setValidIdBackPath(input.getValidIdBackPath());

        if (input.getApplicationType() == ApplicationType.TRANSFER &&
                input.getCurrentRegistrationDetails() != null) {
            application.setCurrentRegistrationDetails(input.getCurrentRegistrationDetails());
        }

        // Save application
        applicationRepository.save(application);

        return applicationId;
    }
}
