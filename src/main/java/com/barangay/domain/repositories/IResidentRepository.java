package com.barangay.domain.repositories;

import com.barangay.domain.entities.Gender;
import com.barangay.domain.entities.Resident;
import com.barangay.domain.entities.ResidentId;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Resident entity.
 */
public interface IResidentRepository {
    /**
     * Save a new resident or update existing resident
     */
    void save(Resident resident);

    /**
     * Find resident by ID
     */
    Optional<Resident> findById(ResidentId id);

    /**
     * Find all residents
     */
    List<Resident> findAll();

    /**
     * Find active residents
     */
    List<Resident> findActiveResidents();

    /**
     * Search residents by name
     */
    List<Resident> searchByName(String name);

    /**
     * Filter residents by gender
     */
    List<Resident> findByGender(Gender gender);

    /**
     * Filter residents by age range
     */
    List<Resident> findByAgeRange(int minAge, int maxAge);

    /**
     * Find potential duplicates (same name and birth date)
     */
    List<Resident> findPotentialDuplicates(String firstName, String lastName, LocalDate birthDate);

    /**
     * Count total residents
     */
    int countTotal();

    /**
     * Count residents by gender
     */
    int countByGender(Gender gender);

    /**
     * Count registered voters
     */
    int countVoters();

    /**
     * Generate next resident ID
     */
    ResidentId generateNextId();

    /**
     * Find residents with pagination
     */
    List<Resident> findWithPagination(int offset, int limit);
}
