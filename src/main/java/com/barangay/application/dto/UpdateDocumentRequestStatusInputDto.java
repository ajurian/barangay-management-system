package com.barangay.application.dto;

import com.barangay.domain.entities.DocumentRequestStatus;

/**
 * DTO for reviewing document requests.
 */
public class UpdateDocumentRequestStatusInputDto {
    private final String requestId;
    private final DocumentRequestStatus targetStatus;
    private final String staffNotes;

    public UpdateDocumentRequestStatusInputDto(String requestId,
            DocumentRequestStatus targetStatus,
            String staffNotes) {
        this.requestId = requestId;
        this.targetStatus = targetStatus;
        this.staffNotes = staffNotes;
    }

    public String getRequestId() {
        return requestId;
    }

    public DocumentRequestStatus getTargetStatus() {
        return targetStatus;
    }

    public String getStaffNotes() {
        return staffNotes;
    }
}
