package com.barangay.domain.repositories;

import com.barangay.domain.entities.BarangayOfficial;
import com.barangay.domain.entities.OfficialPosition;
import com.barangay.domain.entities.ResidentId;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for BarangayOfficial entity.
 */
public interface IOfficialRepository {
    /**
     * Save a new official or update existing
     */
    void save(BarangayOfficial official);

    /**
     * Find official by ID
     */
    Optional<BarangayOfficial> findById(String id);

    /**
     * Find current officials
     */
    List<BarangayOfficial> findCurrentOfficials();

    /**
     * Find current official by position
     */
    BarangayOfficial findCurrentByPosition(OfficialPosition position);

    /**
     * Find all officials by position (including past)
     */
    List<BarangayOfficial> findByPosition(OfficialPosition position);

    /**
     * Find all officials for a resident
     */
    List<BarangayOfficial> findByResidentId(ResidentId residentId);

    /**
     * Find all officials (including past terms)
     */
    List<BarangayOfficial> findAll();

    /**
     * Update existing official
     */
    void update(BarangayOfficial official);

    /**
     * Generate next official ID
     */
    String generateNextId();
}
