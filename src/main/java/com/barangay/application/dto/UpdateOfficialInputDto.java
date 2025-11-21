package com.barangay.application.dto;

import java.time.LocalDate;

/**
 * DTO for updating barangay official information
 * Module 6: Barangay Officials Management
 */
public class UpdateOfficialInputDto {
    private final String officialId;
    private final LocalDate termStart;
    private final LocalDate termEnd;
    private final boolean isCurrent;

    public UpdateOfficialInputDto(String officialId, LocalDate termStart,
            LocalDate termEnd, boolean isCurrent) {
        this.officialId = officialId;
        this.termStart = termStart;
        this.termEnd = termEnd;
        this.isCurrent = isCurrent;
    }

    public String getOfficialId() {
        return officialId;
    }

    public LocalDate getTermStart() {
        return termStart;
    }

    public LocalDate getTermEnd() {
        return termEnd;
    }

    public boolean isCurrent() {
        return isCurrent;
    }
}
