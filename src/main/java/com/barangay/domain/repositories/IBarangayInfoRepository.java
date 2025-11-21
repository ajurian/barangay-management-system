package com.barangay.domain.repositories;

import com.barangay.domain.entities.BarangayInfo;
import java.util.Optional;

/**
 * Repository interface for BarangayInfo entity.
 * Should only have one record.
 */
public interface IBarangayInfoRepository {
    /**
     * Save or update barangay information
     */
    void save(BarangayInfo info);

    /**
     * Get barangay information
     */
    Optional<BarangayInfo> get();
}
