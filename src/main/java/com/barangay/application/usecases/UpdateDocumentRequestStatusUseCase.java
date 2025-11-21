package com.barangay.application.usecases;

import com.barangay.application.dto.UpdateDocumentRequestStatusInputDto;
import com.barangay.application.services.SessionManager;
import com.barangay.domain.entities.DocumentRequest;
import com.barangay.domain.entities.DocumentRequestStatus;
import com.barangay.domain.entities.User;
import com.barangay.domain.entities.UserRole;
import com.barangay.domain.exceptions.UnauthorizedOperationException;
import com.barangay.domain.repositories.IDocumentRequestRepository;
import java.util.EnumSet;

/**
 * Use Case: Staff review and update document request statuses.
 */
public class UpdateDocumentRequestStatusUseCase {
    private static final EnumSet<UserRole> STAFF_ROLES = EnumSet.of(UserRole.CLERK, UserRole.ADMIN,
            UserRole.SUPER_ADMIN);

    private final IDocumentRequestRepository documentRequestRepository;
    private final SessionManager sessionManager;

    public UpdateDocumentRequestStatusUseCase(IDocumentRequestRepository documentRequestRepository,
            SessionManager sessionManager) {
        this.documentRequestRepository = documentRequestRepository;
        this.sessionManager = sessionManager;
    }

    public void execute(UpdateDocumentRequestStatusInputDto input) {
        if (input == null) {
            throw new IllegalArgumentException("Input is required");
        }

        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null) {
            throw new UnauthorizedOperationException("No user logged in");
        }
        if (!STAFF_ROLES.contains(currentUser.getRole())) {
            throw new UnauthorizedOperationException("You are not allowed to review document requests");
        }

        DocumentRequest request = documentRequestRepository.findById(input.getRequestId())
                .orElseThrow(() -> new IllegalArgumentException("Document request not found"));

        DocumentRequestStatus targetStatus = input.getTargetStatus();
        if (targetStatus == null) {
            throw new IllegalArgumentException("Target status is required");
        }

        switch (targetStatus) {
            case UNDER_REVIEW:
                request.markUnderReview(currentUser.getUsername(), input.getStaffNotes());
                break;
            case APPROVED:
                request.approve(currentUser.getUsername(), input.getStaffNotes());
                break;
            case REJECTED:
                request.reject(currentUser.getUsername(), input.getStaffNotes());
                break;
            default:
                throw new IllegalArgumentException("Unsupported status transition");
        }

        documentRequestRepository.update(request);
    }
}
