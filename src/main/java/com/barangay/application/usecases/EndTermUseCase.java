package com.barangay.application.usecases;

import com.barangay.application.services.SessionManager;
import com.barangay.domain.entities.BarangayOfficial;
import com.barangay.domain.entities.UserRole;
import com.barangay.domain.exceptions.UnauthorizedException;
import com.barangay.domain.repositories.IOfficialRepository;
import java.time.LocalDate;

/**
 * Use Case: End a barangay official's term
 * Module 6: Barangay Officials Management
 * 
 * SOLID Principles:
 * - SRP: Single responsibility - ending official terms
 * - DIP: Depends on repository abstraction
 */
public class EndTermUseCase {
    private final IOfficialRepository officialRepository;
    private final SessionManager sessionManager;

    public EndTermUseCase(IOfficialRepository officialRepository,
            SessionManager sessionManager) {
        this.officialRepository = officialRepository;
        this.sessionManager = sessionManager;
    }

    public void execute(String officialId) {
        // Authorization check
        UserRole currentRole = sessionManager.getCurrentUserRole();
        if (currentRole != UserRole.ADMIN && currentRole != UserRole.SUPER_ADMIN) {
            throw new UnauthorizedException("Only ADMIN or SUPER_ADMIN can end official terms");
        }

        // Find official
        BarangayOfficial official = officialRepository.findById(officialId)
                .orElseThrow(() -> new IllegalArgumentException("Official not found: " + officialId));

        if (!official.isCurrent()) {
            throw new IllegalArgumentException("Official is not currently serving");
        }

        // End term
        official.setCurrent(false);
        official.setTermEnd(LocalDate.now());

        officialRepository.update(official);
    }
}
