package com.barangay.application.usecases;

import com.barangay.application.services.SessionManager;
import com.barangay.domain.entities.Resident;
import com.barangay.domain.entities.User;
import com.barangay.domain.entities.UserRole;
import com.barangay.domain.entities.VoterApplication;
import com.barangay.domain.exceptions.ResidentNotFoundException;
import com.barangay.domain.exceptions.UnauthorizedOperationException;
import com.barangay.domain.repositories.IResidentRepository;
import com.barangay.domain.repositories.IVoterApplicationRepository;

/**
 * Use Case: Mark Application as Verified
 * Updates both application status and resident voter status
 */
public class VerifyVoterApplicationUseCase {
    private final IVoterApplicationRepository applicationRepository;
    private final IResidentRepository residentRepository;
    private final SessionManager sessionManager;

    public VerifyVoterApplicationUseCase(IVoterApplicationRepository applicationRepository,
            IResidentRepository residentRepository,
            SessionManager sessionManager) {
        this.applicationRepository = applicationRepository;
        this.residentRepository = residentRepository;
        this.sessionManager = sessionManager;
    }

    public void execute(String applicationId) {
        // Check authorization
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null) {
            throw new UnauthorizedOperationException("No user logged in");
        }

        UserRole role = currentUser.getRole();
        if (role != UserRole.CLERK && role != UserRole.ADMIN && role != UserRole.SUPER_ADMIN) {
            throw new UnauthorizedOperationException("You are not authorized to verify applications");
        }

        // Find application
        VoterApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        // Mark as verified
        application.markAsVerified();
        applicationRepository.save(application);

        // Update resident voter status
        Resident resident = residentRepository.findById(application.getResidentId())
                .orElseThrow(() -> new ResidentNotFoundException("Resident not found"));

        resident.setVoter(true);
        residentRepository.save(resident);
    }
}
