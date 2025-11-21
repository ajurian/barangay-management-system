package com.barangay.application.usecases;

import com.barangay.application.dto.ResidentStatisticsDto;
import com.barangay.domain.entities.Gender;
import com.barangay.domain.entities.Resident;
import com.barangay.domain.repositories.IResidentRepository;
import java.util.List;

/**
 * Use Case: Get resident statistics
 * Module 7: Reports & Analytics
 * 
 * SOLID Principles:
 * - SRP: Single responsibility - calculating resident statistics
 * - DIP: Depends on repository abstraction
 */
public class GetResidentStatisticsUseCase {
    private final IResidentRepository residentRepository;

    public GetResidentStatisticsUseCase(IResidentRepository residentRepository) {
        this.residentRepository = residentRepository;
    }

    public ResidentStatisticsDto execute() {
        List<Resident> allResidents = residentRepository.findAll();

        long activeResidents = allResidents.stream()
                .filter(Resident::isActive)
                .count();

        long maleCount = allResidents.stream()
                .filter(Resident::isActive)
                .filter(r -> r.getGender() == Gender.MALE)
                .count();

        long femaleCount = allResidents.stream()
                .filter(Resident::isActive)
                .filter(r -> r.getGender() == Gender.FEMALE)
                .count();

        long registeredVoters = allResidents.stream()
                .filter(Resident::isActive)
                .filter(Resident::isVoter)
                .count();

        return new ResidentStatisticsDto(
                allResidents.size(),
                (int) activeResidents,
                (int) maleCount,
                (int) femaleCount,
                (int) registeredVoters);
    }
}
