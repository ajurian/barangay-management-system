package com.barangay.domain.repositories;

import com.barangay.domain.entities.VoterApplication;
import com.barangay.domain.entities.ApplicationStatus;
import com.barangay.domain.entities.ResidentId;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for VoterApplication entity.
 */
public interface IVoterApplicationRepository {
    /**
     * Save a new application or update existing
     */
    void save(VoterApplication application);

    /**
     * Find application by ID
     */
    Optional<VoterApplication> findById(String id);

    /**
     * Find applications by resident
     */
    List<VoterApplication> findByResidentId(ResidentId residentId);

    /**
     * Find applications by status
     */
    List<VoterApplication> findByStatus(ApplicationStatus status);

    /**
     * Find all applications
     */
    List<VoterApplication> findAll();

    /**
     * Count pending applications
     */
    int countPending();

    /**
     * Generate next application ID
     */
    String generateNextId();
}
