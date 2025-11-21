package com.barangay.application.usecases;

import com.barangay.application.dto.DocumentRequestCountsDto;
import com.barangay.application.services.SessionManager;
import com.barangay.domain.entities.DocumentRequestStatus;
import com.barangay.domain.entities.User;
import com.barangay.domain.entities.UserRole;
import com.barangay.domain.exceptions.UnauthorizedOperationException;
import com.barangay.domain.repositories.IDocumentRequestRepository;
import java.util.EnumSet;

/**
 * Use Case: Aggregate document request counters for staff dashboards.
 */
public class GetDocumentRequestCountsUseCase {
    private static final EnumSet<UserRole> STAFF_ROLES = EnumSet.of(UserRole.CLERK, UserRole.ADMIN,
            UserRole.SUPER_ADMIN);

    private final IDocumentRequestRepository documentRequestRepository;
    private final SessionManager sessionManager;

    public GetDocumentRequestCountsUseCase(IDocumentRequestRepository documentRequestRepository,
            SessionManager sessionManager) {
        this.documentRequestRepository = documentRequestRepository;
        this.sessionManager = sessionManager;
    }

    public DocumentRequestCountsDto execute() {
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null) {
            throw new UnauthorizedOperationException("No user logged in");
        }
        if (!STAFF_ROLES.contains(currentUser.getRole())) {
            throw new UnauthorizedOperationException("Only staff users can view request counters");
        }

        int pending = documentRequestRepository.countByStatuses(DocumentRequestStatus.PENDING);
        int underReview = documentRequestRepository.countByStatuses(DocumentRequestStatus.UNDER_REVIEW);
        int approved = documentRequestRepository.countByStatuses(DocumentRequestStatus.APPROVED);

        return new DocumentRequestCountsDto(pending, underReview, approved);
    }
}
