package com.barangay.application.usecases;

import com.barangay.application.services.SessionManager;
import com.barangay.domain.entities.User;
import com.barangay.domain.entities.UserRole;
import com.barangay.domain.entities.VoterApplication;
import com.barangay.domain.exceptions.UnauthorizedOperationException;
import com.barangay.domain.repositories.IVoterApplicationRepository;

/**
 * Use Case: Review Voter Application
 * CLERK and ADMIN can review applications
 */
public class ReviewVoterApplicationUseCase {
    private final IVoterApplicationRepository applicationRepository;
    private final SessionManager sessionManager;

    public ReviewVoterApplicationUseCase(IVoterApplicationRepository applicationRepository,
            SessionManager sessionManager) {
        this.applicationRepository = applicationRepository;
        this.sessionManager = sessionManager;
    }

    public void approve(String applicationId, String notes) {
        // Check authorization
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null) {
            throw new UnauthorizedOperationException("No user logged in");
        }

        UserRole role = currentUser.getRole();
        if (role != UserRole.CLERK && role != UserRole.ADMIN && role != UserRole.SUPER_ADMIN) {
            throw new UnauthorizedOperationException("You are not authorized to review applications");
        }

        // Find application
        VoterApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        // Approve application
        application.approve(currentUser.getUsername(), notes);
        applicationRepository.save(application);
    }

    public void reject(String applicationId, String notes) {
        // Check authorization
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null) {
            throw new UnauthorizedOperationException("No user logged in");
        }

        UserRole role = currentUser.getRole();
        if (role != UserRole.CLERK && role != UserRole.ADMIN && role != UserRole.SUPER_ADMIN) {
            throw new UnauthorizedOperationException("You are not authorized to review applications");
        }

        // Find application
        VoterApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        // Reject application
        application.reject(currentUser.getUsername(), notes);
        applicationRepository.save(application);
    }

    public void setUnderReview(String applicationId) {
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null) {
            throw new UnauthorizedOperationException("No user logged in");
        }

        UserRole role = currentUser.getRole();
        if (role != UserRole.CLERK && role != UserRole.ADMIN && role != UserRole.SUPER_ADMIN) {
            throw new UnauthorizedOperationException("You are not authorized to review applications");
        }

        VoterApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        application.setUnderReview();
        applicationRepository.save(application);
    }
}
