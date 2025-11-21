package com.barangay.application.usecases;

import com.barangay.application.dto.SubmitDocumentRequestInputDto;
import com.barangay.application.services.SessionManager;
import com.barangay.domain.entities.DocumentRequest;
import com.barangay.domain.entities.DocumentType;
import com.barangay.domain.entities.Resident;
import com.barangay.domain.entities.ResidentId;
import com.barangay.domain.entities.User;
import com.barangay.domain.entities.UserRole;
import com.barangay.domain.exceptions.ResidentNotFoundException;
import com.barangay.domain.exceptions.UnauthorizedOperationException;
import com.barangay.domain.repositories.IDocumentRequestRepository;
import com.barangay.domain.repositories.IResidentRepository;

/**
 * Use Case: Resident submits an online document request (Module 5).
 */
public class SubmitDocumentRequestUseCase {
    private final IDocumentRequestRepository documentRequestRepository;
    private final IResidentRepository residentRepository;
    private final SessionManager sessionManager;

    public SubmitDocumentRequestUseCase(IDocumentRequestRepository documentRequestRepository,
            IResidentRepository residentRepository,
            SessionManager sessionManager) {
        this.documentRequestRepository = documentRequestRepository;
        this.residentRepository = residentRepository;
        this.sessionManager = sessionManager;
    }

    public String execute(SubmitDocumentRequestInputDto input) {
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null) {
            throw new UnauthorizedOperationException("No user logged in");
        }
        if (currentUser.getRole() != UserRole.RESIDENT) {
            throw new UnauthorizedOperationException("Only residents can submit document requests");
        }
        if (input == null) {
            throw new IllegalArgumentException("Input is required");
        }
        if (input.getDocumentType() == null) {
            throw new IllegalArgumentException("Document type is required");
        }

        ResidentId linkedResidentId = currentUser.getLinkedResidentId();
        if (linkedResidentId == null) {
            throw new IllegalStateException("Resident account is not linked to a resident profile");
        }

        ResidentId residentId = linkedResidentId;
        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new ResidentNotFoundException("Resident profile not found"));

        if (!resident.isActive()) {
            throw new IllegalStateException("Cannot request documents for inactive resident profiles");
        }

        DocumentType documentType = input.getDocumentType();
        String requestId = documentRequestRepository.generateNextId();

        DocumentRequest request = new DocumentRequest(
                requestId,
                residentId,
                documentType,
                input.getPurpose(),
                input.getRequestedValidUntil(),
                input.getNotes(),
                input.getAdditionalInfo());

        documentRequestRepository.save(request);
        return requestId;
    }
}
