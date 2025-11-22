package com.barangay.application.dto;

import com.barangay.domain.entities.DocumentType;
import java.time.LocalDate;

/**
 * DTO for issuing a document
 */
public class IssueDocumentInputDto {
    private final String residentId;
    private final DocumentType documentType;
    private final String purpose;
    private final LocalDate validUntil;
    private final String additionalInfo; // JSON string for document-specific data
    private final String requestId; // Optional link to a document request
    private final String photoPath;

    public IssueDocumentInputDto(String residentId, DocumentType documentType,
            String purpose, LocalDate validUntil, String additionalInfo) {
        this(residentId, documentType, purpose, validUntil, additionalInfo, null, null);
    }

    public IssueDocumentInputDto(String residentId, DocumentType documentType,
            String purpose, LocalDate validUntil, String additionalInfo, String requestId) {
        this(residentId, documentType, purpose, validUntil, additionalInfo, requestId, null);
    }

    public IssueDocumentInputDto(String residentId, DocumentType documentType,
            String purpose, LocalDate validUntil, String additionalInfo,
            String requestId, String photoPath) {
        this.residentId = residentId;
        this.documentType = documentType;
        this.purpose = purpose;
        this.validUntil = validUntil;
        this.additionalInfo = additionalInfo;
        this.requestId = requestId;
        this.photoPath = photoPath;
    }

    public String getResidentId() {
        return residentId;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public String getPurpose() {
        return purpose;
    }

    public LocalDate getValidUntil() {
        return validUntil;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getPhotoPath() {
        return photoPath;
    }
}
