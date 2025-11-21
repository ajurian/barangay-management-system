package com.barangay.application.usecases;

import com.barangay.application.dto.RegisterOfficialInputDto;
import com.barangay.application.services.SessionManager;
import com.barangay.domain.entities.BarangayOfficial;
import com.barangay.domain.entities.Resident;
import com.barangay.domain.entities.ResidentId;
import com.barangay.domain.entities.UserRole;
import com.barangay.domain.exceptions.UnauthorizedException;
import com.barangay.domain.repositories.IOfficialRepository;
import com.barangay.domain.repositories.IResidentRepository;

/**
 * Use Case: Register a new barangay official
 * Module 6: Barangay Officials Management
 * 
 * SOLID Principles:
 * - SRP: Single responsibility - registering officials only
 * - DIP: Depends on repository abstractions
 * - OCP: Extensible through repository implementations
 */
public class RegisterOfficialUseCase {
    private final IOfficialRepository officialRepository;
    private final IResidentRepository residentRepository;
    private final SessionManager sessionManager;

    public RegisterOfficialUseCase(IOfficialRepository officialRepository,
            IResidentRepository residentRepository,
            SessionManager sessionManager) {
        this.officialRepository = officialRepository;
        this.residentRepository = residentRepository;
        this.sessionManager = sessionManager;
    }

    public String execute(RegisterOfficialInputDto input) {
        // Authorization check
        UserRole currentRole = sessionManager.getCurrentUserRole();
        if (currentRole != UserRole.ADMIN && currentRole != UserRole.SUPER_ADMIN) {
            throw new UnauthorizedException("Only ADMIN or SUPER_ADMIN can register officials");
        }

        // Validate resident exists
        ResidentId residentId = ResidentId.fromString(input.getResidentId());
        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new IllegalArgumentException("Resident not found: " + input.getResidentId()));

        if (!resident.isActive()) {
            throw new IllegalArgumentException("Cannot assign official position to inactive resident");
        }

        // Check if position is already occupied by current official
        if (input.isCurrent()) {
            BarangayOfficial existingCurrent = officialRepository.findCurrentByPosition(input.getPosition());
            if (existingCurrent != null) {
                throw new IllegalArgumentException("Position " + input.getPosition() +
                        " is already occupied by " + existingCurrent.getOfficialName());
            }
        }

        // Validate term dates
        if (input.getTermEnd().isBefore(input.getTermStart())) {
            throw new IllegalArgumentException("Term end date must be after term start date");
        }

        // Create official
        BarangayOfficial official = new BarangayOfficial(
                officialRepository.generateNextId(),
                residentId,
                resident.getFullName(),
                input.getPosition(),
                input.getTermStart(),
                input.getTermEnd(),
                input.isCurrent());

        officialRepository.save(official);

        return official.getOfficialId();
    }
}
