package com.barangay.application.usecases;

import com.barangay.application.dto.UpdateOfficialInputDto;
import com.barangay.application.services.SessionManager;
import com.barangay.domain.entities.BarangayOfficial;
import com.barangay.domain.entities.UserRole;
import com.barangay.domain.exceptions.UnauthorizedException;
import com.barangay.domain.repositories.IOfficialRepository;

/**
 * Use Case: Update barangay official information
 * Module 6: Barangay Officials Management
 * 
 * SOLID Principles:
 * - SRP: Single responsibility - updating official details
 * - DIP: Depends on repository abstraction
 */
public class UpdateOfficialUseCase {
    private final IOfficialRepository officialRepository;
    private final SessionManager sessionManager;

    public UpdateOfficialUseCase(IOfficialRepository officialRepository,
            SessionManager sessionManager) {
        this.officialRepository = officialRepository;
        this.sessionManager = sessionManager;
    }

    public void execute(UpdateOfficialInputDto input) {
        // Authorization check
        UserRole currentRole = sessionManager.getCurrentUserRole();
        if (currentRole != UserRole.ADMIN && currentRole != UserRole.SUPER_ADMIN) {
            throw new UnauthorizedException("Only ADMIN or SUPER_ADMIN can update officials");
        }

        // Find official
        BarangayOfficial official = officialRepository.findById(input.getOfficialId())
                .orElseThrow(() -> new IllegalArgumentException("Official not found: " + input.getOfficialId()));

        // Validate term dates
        if (input.getTermEnd().isBefore(input.getTermStart())) {
            throw new IllegalArgumentException("Term end date must be after term start date");
        }

        // Check if setting as current while position has reached max
        if (input.isCurrent() && !official.isCurrent()) {
            int currentCount = officialRepository.countCurrentByPosition(official.getPosition());
            int maxAllowed = official.getPosition().getMaxAllowed();
            if (currentCount >= maxAllowed) {
                if (maxAllowed == 1) {
                    BarangayOfficial existingCurrent = officialRepository.findCurrentByPosition(official.getPosition());
                    throw new IllegalArgumentException("Position " + official.getPosition() +
                            " is already occupied by " + existingCurrent.getOfficialName());
                } else {
                    throw new IllegalArgumentException("Position " + official.getPosition() +
                            " has reached maximum of " + maxAllowed + " officials");
                }
            }
        }

        // Update fields
        official.setTermStart(input.getTermStart());
        official.setTermEnd(input.getTermEnd());
        official.setCurrent(input.isCurrent());

        officialRepository.update(official);
    }
}
