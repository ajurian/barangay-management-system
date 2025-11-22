package com.barangay.application.usecases;

import com.barangay.application.dto.IssueDocumentInputDto;
import com.barangay.application.services.SessionManager;
import com.barangay.domain.entities.*;
import com.barangay.domain.exceptions.ResidentNotFoundException;
import com.barangay.domain.exceptions.UnauthorizedOperationException;
import com.barangay.domain.repositories.IDocumentRepository;
import com.barangay.domain.repositories.IDocumentRequestRepository;
import com.barangay.domain.repositories.IResidentRepository;
import com.barangay.domain.valueobjects.DocumentReference;

import java.time.LocalDate;

/**
 * Use Case: Issue Document
 * Following SRP: Handles only document issuance logic.
 */
public class IssueDocumentUseCase {
    private final IDocumentRepository documentRepository;
    private final IResidentRepository residentRepository;
    private final SessionManager sessionManager;
    private final IDocumentRequestRepository documentRequestRepository;

    public IssueDocumentUseCase(IDocumentRepository documentRepository,
            IResidentRepository residentRepository,
            SessionManager sessionManager,
            IDocumentRequestRepository documentRequestRepository) {
        this.documentRepository = documentRepository;
        this.residentRepository = residentRepository;
        this.sessionManager = sessionManager;
        this.documentRequestRepository = documentRequestRepository;
    }

    public String execute(IssueDocumentInputDto input) {
        // Check authorization
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null) {
            throw new UnauthorizedOperationException("No user logged in");
        }

        UserRole role = currentUser.getRole();
        if (role != UserRole.CLERK && role != UserRole.ADMIN && role != UserRole.SUPER_ADMIN) {
            throw new UnauthorizedOperationException("You are not authorized to issue documents");
        }

        // Validate resident exists and is active
        ResidentId residentId = ResidentId.fromString(input.getResidentId());
        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new ResidentNotFoundException("Resident not found"));

        if (!resident.isActive()) {
            throw new IllegalStateException("Cannot issue document to inactive resident");
        }

        // Ensure linked document request (if any) is valid
        DocumentRequest linkedRequest = null;
        if (input.getRequestId() != null && !input.getRequestId().isBlank()) {
            linkedRequest = documentRequestRepository.findById(input.getRequestId().trim())
                    .orElseThrow(() -> new IllegalArgumentException("Document request not found"));

            if (!linkedRequest.getResidentId().equals(residentId)) {
                throw new IllegalStateException("Request does not belong to the selected resident");
            }

            if (linkedRequest.getStatus() != DocumentRequestStatus.APPROVED) {
                throw new IllegalStateException("Only approved requests can be issued");
            }
        }

        // Generate document reference
        DocumentReference reference = documentRepository.generateNextReference(input.getDocumentType());

        // Create document
        Document document = new Document(
                reference,
                residentId,
                input.getDocumentType(),
                input.getPurpose(),
                LocalDate.now(),
                input.getValidUntil(),
                currentUser.getUsername());

        if (input.getAdditionalInfo() != null && !input.getAdditionalInfo().isEmpty()) {
            document.setAdditionalInfo(input.getAdditionalInfo());
        }

        if (linkedRequest != null) {
            document.setOriginRequestId(linkedRequest.getId());
        }

        if (input.getPhotoPath() != null && !input.getPhotoPath().isBlank()) {
            document.setPhotoPath(input.getPhotoPath());
        }

        // Save document
        documentRepository.save(document);

        if (linkedRequest != null) {
            linkedRequest.markIssued(currentUser.getUsername(), reference.getValue());
            documentRequestRepository.update(linkedRequest);
        }

        return reference.getValue();
    }
}
