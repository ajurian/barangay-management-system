package com.barangay.application.usecases;

import com.barangay.domain.entities.BarangayInfo;
import com.barangay.domain.repositories.IBarangayInfoRepository;

/**
 * Use Case: Get barangay information
 * Module 8: System Administration
 * 
 * SOLID Principles:
 * - SRP: Single responsibility - retrieving barangay info
 * - DIP: Depends on repository abstraction
 */
public class GetBarangayInfoUseCase {
    private final IBarangayInfoRepository barangayInfoRepository;

    public GetBarangayInfoUseCase(IBarangayInfoRepository barangayInfoRepository) {
        this.barangayInfoRepository = barangayInfoRepository;
    }

    public BarangayInfo execute() {
        return barangayInfoRepository.get().orElseThrow(
                () -> new IllegalStateException("Barangay information not found"));
    }
}
