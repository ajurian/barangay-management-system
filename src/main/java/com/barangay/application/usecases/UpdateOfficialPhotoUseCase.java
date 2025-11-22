package com.barangay.application.usecases;

import com.barangay.application.services.SessionManager;
import com.barangay.domain.entities.BarangayOfficial;
import com.barangay.domain.entities.UserRole;
import com.barangay.domain.exceptions.UnauthorizedException;
import com.barangay.domain.repositories.IOfficialRepository;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Use Case: Update a barangay official's photo path reference.
 */
public class UpdateOfficialPhotoUseCase {
    private final IOfficialRepository officialRepository;
    private final SessionManager sessionManager;

    public UpdateOfficialPhotoUseCase(IOfficialRepository officialRepository,
            SessionManager sessionManager) {
        this.officialRepository = officialRepository;
        this.sessionManager = sessionManager;
    }

    public void execute(String officialId, String photoPath) {
        UserRole currentRole = sessionManager.getCurrentUserRole();
        if (currentRole != UserRole.ADMIN && currentRole != UserRole.SUPER_ADMIN) {
            throw new UnauthorizedException("Only ADMIN or SUPER_ADMIN can update official photos");
        }

        BarangayOfficial official = officialRepository.findById(officialId)
                .orElseThrow(() -> new IllegalArgumentException("Official not found: " + officialId));

        String sanitizedPath = sanitizePath(photoPath);
        official.setPhotoPath(sanitizedPath);
        officialRepository.update(official);
    }

    private String sanitizePath(String photoPath) {
        if (photoPath == null || photoPath.isBlank()) {
            return null;
        }
        try {
            Path candidate = Paths.get(photoPath);
            if (!Files.exists(candidate)) {
                throw new IllegalArgumentException("Selected photo could not be found");
            }
            return candidate.toAbsolutePath().toString();
        } catch (InvalidPathException ex) {
            throw new IllegalArgumentException("Invalid photo location selected", ex);
        }
    }
}
