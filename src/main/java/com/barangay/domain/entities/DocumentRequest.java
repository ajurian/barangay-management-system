package com.barangay.domain.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Domain Entity: DocumentRequest
 * Represents a resident-initiated document request in Module 5.
 */
public class DocumentRequest {
    private final String id;
    private final ResidentId residentId;
    private final DocumentType documentType;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String purpose;
    private LocalDate requestedValidUntil;
    private String residentNotes;
    private String additionalInfo;
    private DocumentRequestStatus status;
    private String staffNotes;
    private String handledBy;
    private String linkedDocumentReference;

    public DocumentRequest(String id,
            ResidentId residentId,
            DocumentType documentType,
            String purpose,
            LocalDate requestedValidUntil,
            String residentNotes,
            String additionalInfo) {
        if (id == null || residentId == null || documentType == null) {
            throw new IllegalArgumentException("Request id, resident, and document type are required");
        }
        this.id = id;
        this.residentId = residentId;
        this.documentType = documentType;
        this.purpose = purpose;
        this.requestedValidUntil = requestedValidUntil;
        this.residentNotes = residentNotes;
        this.additionalInfo = additionalInfo;
        this.status = DocumentRequestStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public DocumentRequest(String id,
            ResidentId residentId,
            DocumentType documentType,
            String purpose,
            LocalDate requestedValidUntil,
            String residentNotes,
            String additionalInfo,
            DocumentRequestStatus status,
            String staffNotes,
            String handledBy,
            String linkedDocumentReference,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.id = id;
        this.residentId = residentId;
        this.documentType = documentType;
        this.purpose = purpose;
        this.requestedValidUntil = requestedValidUntil;
        this.residentNotes = residentNotes;
        this.additionalInfo = additionalInfo;
        this.status = status;
        this.staffNotes = staffNotes;
        this.handledBy = handledBy;
        this.linkedDocumentReference = linkedDocumentReference;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public ResidentId getResidentId() {
        return residentId;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public String getPurpose() {
        return purpose;
    }

    public LocalDate getRequestedValidUntil() {
        return requestedValidUntil;
    }

    public String getResidentNotes() {
        return residentNotes;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public DocumentRequestStatus getStatus() {
        return status;
    }

    public String getStaffNotes() {
        return staffNotes;
    }

    public String getHandledBy() {
        return handledBy;
    }

    public String getLinkedDocumentReference() {
        return linkedDocumentReference;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void updateResidentNotes(String notes, LocalDate requestedValidUntil, String additionalInfo) {
        this.residentNotes = notes;
        this.requestedValidUntil = requestedValidUntil;
        this.additionalInfo = additionalInfo;
        touch();
    }

    public void markUnderReview(String username, String notes) {
        transitionTo(DocumentRequestStatus.UNDER_REVIEW, username, notes);
    }

    public void approve(String username, String notes) {
        transitionTo(DocumentRequestStatus.APPROVED, username, notes);
    }

    public void reject(String username, String notes) {
        transitionTo(DocumentRequestStatus.REJECTED, username, notes);
    }

    public void markIssued(String username, String documentReference) {
        if (status != DocumentRequestStatus.APPROVED) {
            throw new IllegalStateException("Only approved requests can be marked as issued");
        }
        this.status = DocumentRequestStatus.ISSUED;
        this.handledBy = username;
        this.linkedDocumentReference = documentReference;
        touch();
    }

    private void transitionTo(DocumentRequestStatus targetStatus, String username, String notes) {
        if (!status.canTransitionTo(targetStatus)) {
            throw new IllegalStateException(
                    String.format("Cannot move request %s from %s to %s", id, status, targetStatus));
        }
        this.status = targetStatus;
        this.handledBy = username;
        this.staffNotes = notes;
        touch();
    }

    private void touch() {
        this.updatedAt = LocalDateTime.now();
    }
}
