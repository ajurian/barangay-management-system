package com.barangay.application.usecases;

import com.barangay.domain.entities.BarangayOfficial;
import com.barangay.domain.entities.OfficialPosition;
import com.barangay.domain.repositories.IOfficialRepository;
import java.util.List;

/**
 * Use Case: List barangay officials
 * Module 6: Barangay Officials Management
 * 
 * SOLID Principles:
 * - SRP: Single responsibility - listing officials
 * - DIP: Depends on repository abstraction
 * - OCP: Can be extended with different filtering strategies
 */
public class ListOfficialsUseCase {
    private final IOfficialRepository officialRepository;

    public ListOfficialsUseCase(IOfficialRepository officialRepository) {
        this.officialRepository = officialRepository;
    }

    /**
     * Get all current officials
     */
    public List<BarangayOfficial> getCurrentOfficials() {
        return officialRepository.findCurrentOfficials();
    }

    /**
     * Get all officials (including past)
     */
    public List<BarangayOfficial> getAllOfficials() {
        return officialRepository.findAll();
    }

    /**
     * Get officials by position (including history)
     */
    public List<BarangayOfficial> getOfficialsByPosition(OfficialPosition position) {
        return officialRepository.findByPosition(position);
    }
}
