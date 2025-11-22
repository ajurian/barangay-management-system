package com.barangay.domain.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Domain Entity: BarangayOfficial
 * Represents barangay government officials.
 */
public class BarangayOfficial {
    private final String id;
    private final ResidentId residentId;
    private String officialName;
    private OfficialPosition position;
    private LocalDate termStart;
    private LocalDate termEnd;
    private boolean isCurrent;
    private String photoPath;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public BarangayOfficial(String id, ResidentId residentId, OfficialPosition position,
            LocalDate termStart, LocalDate termEnd) {
        if (id == null || residentId == null || position == null ||
                termStart == null || termEnd == null) {
            throw new IllegalArgumentException("Required fields cannot be null");
        }
        this.id = id;
        this.residentId = residentId;
        this.position = position;
        this.termStart = termStart;
        this.termEnd = termEnd;
        this.isCurrent = LocalDate.now().isBefore(termEnd) &&
                LocalDate.now().isAfter(termStart);
        this.photoPath = null;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Constructor with all fields (for repository reconstruction)
    public BarangayOfficial(String id, ResidentId residentId, String officialName,
            OfficialPosition position, LocalDate termStart, LocalDate termEnd,
            boolean isCurrent) {
        this(id, residentId, officialName, position, termStart, termEnd, isCurrent, null);
    }

    public BarangayOfficial(String id, ResidentId residentId, String officialName,
            OfficialPosition position, LocalDate termStart, LocalDate termEnd,
            boolean isCurrent, String photoPath) {
        if (id == null || residentId == null || position == null ||
                termStart == null || termEnd == null) {
            throw new IllegalArgumentException("Required fields cannot be null");
        }
        this.id = id;
        this.residentId = residentId;
        this.officialName = officialName;
        this.position = position;
        this.termStart = termStart;
        this.termEnd = termEnd;
        this.isCurrent = isCurrent;
        this.photoPath = photoPath;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getOfficialId() {
        return id;
    }

    public ResidentId getResidentId() {
        return residentId;
    }

    public String getOfficialName() {
        return officialName;
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

    public String getPhotoPath() {
        return photoPath;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Setters
    public void setOfficialName(String officialName) {
        this.officialName = officialName;
        this.updatedAt = LocalDateTime.now();
    }

    public void setPosition(OfficialPosition position) {
        this.position = position;
        this.updatedAt = LocalDateTime.now();
    }

    public void setTermStart(LocalDate termStart) {
        this.termStart = termStart;
        this.updatedAt = LocalDateTime.now();
    }

    public void setTermEnd(LocalDate termEnd) {
        this.termEnd = termEnd;
        this.isCurrent = LocalDate.now().isBefore(termEnd) &&
                LocalDate.now().isAfter(termStart);
        this.updatedAt = LocalDateTime.now();
    }

    public void setCurrent(boolean isCurrent) {
        this.isCurrent = isCurrent;
        this.updatedAt = LocalDateTime.now();
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = (photoPath == null || photoPath.isBlank()) ? null : photoPath;
        this.updatedAt = LocalDateTime.now();
    }

    public void endTerm() {
        this.isCurrent = false;
        this.termEnd = LocalDate.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Business rule: Check if term is active
     */
    public boolean isTermActive() {
        LocalDate now = LocalDate.now();
        return (now.isAfter(termStart) || now.isEqual(termStart)) &&
                (now.isBefore(termEnd) || now.isEqual(termEnd));
    }

    public boolean hasPhoto() {
        return photoPath != null && !photoPath.isBlank();
    }
}
