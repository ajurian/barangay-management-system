package com.barangay.application.dto;

/**
 * Simple aggregate for document request counters.
 */
public class DocumentRequestCountsDto {
    private final int pending;
    private final int underReview;
    private final int approved;

    public DocumentRequestCountsDto(int pending, int underReview, int approved) {
        this.pending = pending;
        this.underReview = underReview;
        this.approved = approved;
    }

    public int getPending() {
        return pending;
    }

    public int getUnderReview() {
        return underReview;
    }

    public int getApproved() {
        return approved;
    }
}
