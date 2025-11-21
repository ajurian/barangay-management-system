package com.barangay.application.usecases;

import com.barangay.domain.entities.Gender;
import com.barangay.domain.entities.Resident;
import com.barangay.domain.repositories.IResidentRepository;

import java.util.List;

/**
 * Use Case: Search and Filter Residents
 */
public class SearchResidentsUseCase {
    private final IResidentRepository residentRepository;

    public SearchResidentsUseCase(IResidentRepository residentRepository) {
        this.residentRepository = residentRepository;
    }

    public List<Resident> searchByName(String name) {
        return residentRepository.searchByName(name);
    }

    public List<Resident> filterByGender(Gender gender) {
        return residentRepository.findByGender(gender);
    }

    public List<Resident> filterByAgeRange(int minAge, int maxAge) {
        return residentRepository.findByAgeRange(minAge, maxAge);
    }

    public List<Resident> getAllActive() {
        return residentRepository.findActiveResidents();
    }

    public List<Resident> getWithPagination(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return residentRepository.findWithPagination(offset, pageSize);
    }
}
