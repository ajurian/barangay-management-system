package com.barangay.domain.entities;

import com.barangay.domain.valueobjects.DocumentReference;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Domain Entity: Document
 * Represents issued barangay documents.
 * Following SRP: Handles only document entity data.
 */
public class Document {
    private final DocumentReference reference;
    private final ResidentId residentId;
    private final DocumentType type;
    private final String purpose;
    private final LocalDate issuedDate;
    private final LocalDate validUntil;
    private final String issuedBy; // Username of issuer
    private String additionalInfo; // JSON or key-value pairs for document-specific data
    private final LocalDateTime createdAt;
    private String originRequestId;

    public Document(DocumentReference reference, ResidentId residentId,
            DocumentType type, String purpose, LocalDate issuedDate,
            LocalDate validUntil, String issuedBy) {
        if (reference == null || residentId == null || type == null ||
                issuedDate == null || issuedBy == null) {
            throw new IllegalArgumentException("Required fields cannot be null");
        }
        this.reference = reference;
        this.residentId = residentId;
        this.type = type;
        this.purpose = purpose;
        this.issuedDate = issuedDate;
        this.validUntil = validUntil;
        this.issuedBy = issuedBy;
        this.createdAt = LocalDateTime.now();
    }

    // Getters
    public DocumentReference getReference() {
        return reference;
    }

    public ResidentId getResidentId() {
        return residentId;
    }

    public DocumentType getType() {
        return type;
    }

    public String getPurpose() {
        return purpose;
    }

    public LocalDate getIssuedDate() {
        return issuedDate;
    }

    public LocalDate getValidUntil() {
        return validUntil;
    }

    public String getIssuedBy() {
        return issuedBy;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getOriginRequestId() {
        return originRequestId;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public void setOriginRequestId(String originRequestId) {
        this.originRequestId = originRequestId;
    }

    /**
     * Business rule: Check if document is still valid
     */
    public boolean isValid() {
        if (validUntil == null) {
            return true; // No expiry
        }
        return LocalDate.now().isBefore(validUntil) || LocalDate.now().isEqual(validUntil);
    }

    /**
     * Business rule: Check if document is expired
     */
    public boolean isExpired() {
        return !isValid();
    }
}
