package com.barangay.application.usecases;

import com.barangay.application.services.SessionManager;
import com.barangay.domain.entities.User;
import com.barangay.domain.entities.UserRole;
import com.barangay.domain.entities.VoterApplication;
import com.barangay.domain.exceptions.UnauthorizedOperationException;
import com.barangay.domain.repositories.IVoterApplicationRepository;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Use Case: Schedule COMELEC Verification
 */
public class ScheduleVerificationUseCase {
    private final IVoterApplicationRepository applicationRepository;
    private final SessionManager sessionManager;

    public ScheduleVerificationUseCase(IVoterApplicationRepository applicationRepository,
            SessionManager sessionManager) {
        this.applicationRepository = applicationRepository;
        this.sessionManager = sessionManager;
    }

    public void execute(String applicationId, LocalDateTime appointmentDateTime, String venue) {
        // Check authorization
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null) {
            throw new UnauthorizedOperationException("No user logged in");
        }

        UserRole role = currentUser.getRole();
        if (role != UserRole.CLERK && role != UserRole.ADMIN && role != UserRole.SUPER_ADMIN) {
            throw new UnauthorizedOperationException("You are not authorized to schedule verification");
        }

        // Find application
        VoterApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        // Generate appointment slip reference
        String slipReference = "AS-" + LocalDateTime.now().getYear() + "-" +
                UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // Schedule verification
        application.schedule(appointmentDateTime, venue, slipReference);
        applicationRepository.save(application);
    }
}
