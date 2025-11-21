package com.barangay.application.usecases;

import com.barangay.application.services.SessionManager;
import com.barangay.domain.entities.DocumentRequest;
import com.barangay.domain.entities.ResidentId;
import com.barangay.domain.entities.User;
import com.barangay.domain.entities.UserRole;
import com.barangay.domain.exceptions.UnauthorizedOperationException;
import com.barangay.domain.repositories.IDocumentRequestRepository;
import java.util.Optional;

/**
 * Use Case: Fetch a specific document request with role-based access.
 */
public class GetDocumentRequestUseCase {
    private final IDocumentRequestRepository documentRequestRepository;
    private final SessionManager sessionManager;

    public GetDocumentRequestUseCase(IDocumentRequestRepository documentRequestRepository,
            SessionManager sessionManager) {
        this.documentRequestRepository = documentRequestRepository;
        this.sessionManager = sessionManager;
    }

    public Optional<DocumentRequest> execute(String requestId) {
        if (requestId == null || requestId.isBlank()) {
            throw new IllegalArgumentException("Request id is required");
        }

        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null) {
            throw new UnauthorizedOperationException("No user logged in");
        }

        Optional<DocumentRequest> result = documentRequestRepository.findById(requestId.trim());
        if (result.isEmpty()) {
            return Optional.empty();
        }

        DocumentRequest request = result.get();
        if (currentUser.getRole() == UserRole.RESIDENT) {
            ResidentId linkedResidentId = currentUser.getLinkedResidentId();
            if (linkedResidentId == null || !linkedResidentId.equals(request.getResidentId())) {
                throw new UnauthorizedOperationException("You cannot view another resident's request");
            }
        }

        return Optional.of(request);
    }
}
