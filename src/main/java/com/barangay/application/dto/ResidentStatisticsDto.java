package com.barangay.application.dto;

/**
 * DTO for resident statistics
 * Module 7: Reports & Analytics
 */
public class ResidentStatisticsDto {
    private final int totalResidents;
    private final int activeResidents;
    private final int maleCount;
    private final int femaleCount;
    private final int registeredVoters;

    public ResidentStatisticsDto(int totalResidents, int activeResidents,
            int maleCount, int femaleCount, int registeredVoters) {
        this.totalResidents = totalResidents;
        this.activeResidents = activeResidents;
        this.maleCount = maleCount;
        this.femaleCount = femaleCount;
        this.registeredVoters = registeredVoters;
    }

    public int getTotalResidents() {
        return totalResidents;
    }

    public int getActiveResidents() {
        return activeResidents;
    }

    public int getMaleCount() {
        return maleCount;
    }

    public int getFemaleCount() {
        return femaleCount;
    }

    public int getRegisteredVoters() {
        return registeredVoters;
    }
}
