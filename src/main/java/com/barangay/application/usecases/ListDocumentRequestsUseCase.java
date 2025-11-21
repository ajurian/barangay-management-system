package com.barangay.application.usecases;

import com.barangay.application.services.SessionManager;
import com.barangay.domain.entities.DocumentRequest;
import com.barangay.domain.entities.DocumentRequestStatus;
import com.barangay.domain.entities.ResidentId;
import com.barangay.domain.entities.User;
import com.barangay.domain.entities.UserRole;
import com.barangay.domain.exceptions.UnauthorizedOperationException;
import com.barangay.domain.repositories.IDocumentRequestRepository;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Use Case: List document requests.
 * Residents always receive their own records; staff can filter by status or
 * search term.
 */
public class ListDocumentRequestsUseCase {
    private final IDocumentRequestRepository documentRequestRepository;
    private final SessionManager sessionManager;

    public ListDocumentRequestsUseCase(IDocumentRequestRepository documentRequestRepository,
            SessionManager sessionManager) {
        this.documentRequestRepository = documentRequestRepository;
        this.sessionManager = sessionManager;
    }

    public List<DocumentRequest> execute(DocumentRequestStatus statusFilter, String searchTerm) {
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null) {
            throw new UnauthorizedOperationException("No user logged in");
        }

        if (currentUser.getRole() == UserRole.RESIDENT) {
            ResidentId residentId = currentUser.getLinkedResidentId();
            if (residentId == null) {
                throw new IllegalStateException("Resident user is not linked to a resident profile");
            }
            List<DocumentRequest> requests = documentRequestRepository.findByResidentId(residentId);
            return filterRequests(requests, statusFilter, searchTerm);
        }

        if (searchTerm != null && !searchTerm.isBlank()) {
            return documentRequestRepository.search(searchTerm.trim());
        }

        if (statusFilter != null) {
            return documentRequestRepository.findByStatus(statusFilter);
        }

        return documentRequestRepository.findAll();
    }

    private List<DocumentRequest> filterRequests(List<DocumentRequest> source,
            DocumentRequestStatus statusFilter,
            String searchTerm) {
        return source.stream()
                .filter(request -> statusFilter == null || request.getStatus() == statusFilter)
                .filter(request -> matchesSearch(request, searchTerm))
                .collect(Collectors.toList());
    }

    private boolean matchesSearch(DocumentRequest request, String term) {
        if (term == null || term.isBlank()) {
            return true;
        }
        String normalizedTerm = term.toLowerCase(Locale.ROOT);
        return request.getId().toLowerCase(Locale.ROOT).contains(normalizedTerm) ||
                (request.getPurpose() != null && request.getPurpose().toLowerCase(Locale.ROOT).contains(normalizedTerm))
                ||
                request.getDocumentType().name().toLowerCase(Locale.ROOT).contains(normalizedTerm) ||
                request.getStatus().name().toLowerCase(Locale.ROOT).contains(normalizedTerm);
    }
}
