package com.barangay.application.dto;

import com.barangay.domain.entities.DocumentType;
import java.time.LocalDate;

/**
 * DTO for creating a resident document request.
 */
public class SubmitDocumentRequestInputDto {
    private final DocumentType documentType;
    private final String purpose;
    private final LocalDate requestedValidUntil;
    private final String notes;
    private final String additionalInfo;

    public SubmitDocumentRequestInputDto(DocumentType documentType,
            String purpose,
            LocalDate requestedValidUntil,
            String notes,
            String additionalInfo) {
        this.documentType = documentType;
        this.purpose = purpose;
        this.requestedValidUntil = requestedValidUntil;
        this.notes = notes;
        this.additionalInfo = additionalInfo;
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

    public String getNotes() {
        return notes;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }
}
