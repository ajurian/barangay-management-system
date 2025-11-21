package com.barangay.domain.entities;

import java.time.LocalDateTime;

/**
 * Domain Entity: VoterApplication
 * Represents a resident's application for voter registration.
 */
public class VoterApplication {
    private final String id;
    private final ResidentId residentId;
    private ApplicationType applicationType;
    private String currentRegistrationDetails; // For transfer type
    private String validIdFrontPath;
    private String validIdBackPath;
    private ApplicationStatus status;
    private String reviewNotes;
    private String reviewedBy;
    private LocalDateTime appointmentDateTime;
    private String appointmentVenue;
    private String appointmentSlipReference;
    private final LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private LocalDateTime updatedAt;

    public VoterApplication(String id, ResidentId residentId, ApplicationType applicationType) {
        this(id, residentId, applicationType, null, null, null, ApplicationStatus.PENDING, null, null, null, null,
                null, LocalDateTime.now(), null, LocalDateTime.now());
    }

    private VoterApplication(String id, ResidentId residentId, ApplicationType applicationType,
            String currentRegistrationDetails,
            String validIdFrontPath, String validIdBackPath, ApplicationStatus status, String reviewNotes,
            String reviewedBy,
            LocalDateTime appointmentDateTime, String appointmentVenue, String appointmentSlipReference,
            LocalDateTime submittedAt,
            LocalDateTime reviewedAt, LocalDateTime updatedAt) {
        if (id == null || residentId == null || applicationType == null) {
            throw new IllegalArgumentException("Required fields cannot be null");
        }
        this.id = id;
        this.residentId = residentId;
        this.applicationType = applicationType;
        this.currentRegistrationDetails = currentRegistrationDetails;
        this.validIdFrontPath = validIdFrontPath;
        this.validIdBackPath = validIdBackPath;
        this.status = status != null ? status : ApplicationStatus.PENDING;
        this.reviewNotes = reviewNotes;
        this.reviewedBy = reviewedBy;
        this.appointmentDateTime = appointmentDateTime;
        this.appointmentVenue = appointmentVenue;
        this.appointmentSlipReference = appointmentSlipReference;
        LocalDateTime submittedAtValue = submittedAt != null ? submittedAt : LocalDateTime.now();
        this.submittedAt = submittedAtValue;
        this.reviewedAt = reviewedAt;
        this.updatedAt = updatedAt != null ? updatedAt : submittedAtValue;
    }

    public static VoterApplication restoreFromPersistence(String id, ResidentId residentId,
            ApplicationType applicationType,
            String currentRegistrationDetails, String validIdFrontPath, String validIdBackPath,
            ApplicationStatus status,
            String reviewNotes, String reviewedBy, LocalDateTime appointmentDateTime, String appointmentVenue,
            String appointmentSlipReference, LocalDateTime submittedAt, LocalDateTime reviewedAt,
            LocalDateTime updatedAt) {
        return new VoterApplication(id, residentId, applicationType, currentRegistrationDetails, validIdFrontPath,
                validIdBackPath, status, reviewNotes, reviewedBy, appointmentDateTime, appointmentVenue,
                appointmentSlipReference, submittedAt, reviewedAt, updatedAt);
    }

    // Getters
    public String getId() {
        return id;
    }

    public ResidentId getResidentId() {
        return residentId;
    }

    public ApplicationType getApplicationType() {
        return applicationType;
    }

    public String getCurrentRegistrationDetails() {
        return currentRegistrationDetails;
    }

    public String getValidIdFrontPath() {
        return validIdFrontPath;
    }

    public String getValidIdBackPath() {
        return validIdBackPath;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public String getReviewNotes() {
        return reviewNotes;
    }

    public String getReviewedBy() {
        return reviewedBy;
    }

    public LocalDateTime getAppointmentDateTime() {
        return appointmentDateTime;
    }

    public String getAppointmentVenue() {
        return appointmentVenue;
    }

    public String getAppointmentSlipReference() {
        return appointmentSlipReference;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Setters
    public void setCurrentRegistrationDetails(String currentRegistrationDetails) {
        this.currentRegistrationDetails = currentRegistrationDetails;
        this.updatedAt = LocalDateTime.now();
    }

    public void setValidIdFrontPath(String validIdFrontPath) {
        this.validIdFrontPath = validIdFrontPath;
        this.updatedAt = LocalDateTime.now();
    }

    public void setValidIdBackPath(String validIdBackPath) {
        this.validIdBackPath = validIdBackPath;
        this.updatedAt = LocalDateTime.now();
    }

    public void approve(String reviewedBy, String notes) {
        this.status = ApplicationStatus.APPROVED;
        this.reviewedBy = reviewedBy;
        this.reviewNotes = notes;
        this.reviewedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void reject(String reviewedBy, String notes) {
        this.status = ApplicationStatus.REJECTED;
        this.reviewedBy = reviewedBy;
        this.reviewNotes = notes;
        this.reviewedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void schedule(LocalDateTime appointmentDateTime, String venue, String slipReference) {
        if (this.status != ApplicationStatus.APPROVED) {
            throw new IllegalStateException("Can only schedule approved applications");
        }
        this.status = ApplicationStatus.SCHEDULED;
        this.appointmentDateTime = appointmentDateTime;
        this.appointmentVenue = venue;
        this.appointmentSlipReference = slipReference;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsVerified() {
        if (this.status != ApplicationStatus.SCHEDULED) {
            throw new IllegalStateException("Can only verify scheduled applications");
        }
        this.status = ApplicationStatus.VERIFIED;
        this.updatedAt = LocalDateTime.now();
    }

    public void setUnderReview() {
        if (this.status != ApplicationStatus.PENDING) {
            throw new IllegalStateException("Can only review pending applications");
        }
        this.status = ApplicationStatus.UNDER_REVIEW;
        this.updatedAt = LocalDateTime.now();
    }
}
