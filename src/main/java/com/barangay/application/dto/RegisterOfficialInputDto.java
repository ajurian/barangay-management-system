package com.barangay.application.dto;

import com.barangay.domain.entities.OfficialPosition;
import java.time.LocalDate;

/**
 * DTO for registering a new barangay official
 * Module 6: Barangay Officials Management
 */
public class RegisterOfficialInputDto {
    private final String residentId;
    private final OfficialPosition position;
    private final LocalDate termStart;
    private final LocalDate termEnd;
    private final boolean isCurrent;

    public RegisterOfficialInputDto(String residentId, OfficialPosition position,
            LocalDate termStart, LocalDate termEnd,
            boolean isCurrent) {
        this.residentId = residentId;
        this.position = position;
        this.termStart = termStart;
        this.termEnd = termEnd;
        this.isCurrent = isCurrent;
    }

    public String getResidentId() {
        return residentId;
    }

    public OfficialPosition getPosition() {
        return position;
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
