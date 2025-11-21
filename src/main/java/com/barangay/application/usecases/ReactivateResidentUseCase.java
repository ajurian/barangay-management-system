package com.barangay.application.usecases;

import com.barangay.application.services.SessionManager;
import com.barangay.domain.entities.Resident;
import com.barangay.domain.entities.ResidentId;
import com.barangay.domain.entities.User;
import com.barangay.domain.entities.UserRole;
import com.barangay.domain.exceptions.ResidentNotFoundException;
import com.barangay.domain.exceptions.UnauthorizedOperationException;
import com.barangay.domain.repositories.IResidentRepository;

/**
 * Use Case: Reactivate Archived Resident
 */
public class ReactivateResidentUseCase {
    private final IResidentRepository residentRepository;
    private final SessionManager sessionManager;

    public ReactivateResidentUseCase(IResidentRepository residentRepository, SessionManager sessionManager) {
        this.residentRepository = residentRepository;
        this.sessionManager = sessionManager;
    }

    public void execute(String residentIdStr) {
        // Check authorization
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null) {
            throw new UnauthorizedOperationException("No user logged in");
        }

        UserRole role = currentUser.getRole();
        if (role != UserRole.ADMIN && role != UserRole.SUPER_ADMIN) {
            throw new UnauthorizedOperationException("Only ADMIN and SUPER_ADMIN can reactivate residents");
        }

        // Find resident
        ResidentId residentId = ResidentId.fromString(residentIdStr);
        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new ResidentNotFoundException("Resident not found"));

        // Reactivate
        resident.reactivate();
        residentRepository.save(resident);
    }
}
