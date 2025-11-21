package com.barangay.domain.repositories;

import com.barangay.domain.entities.DocumentRequest;
import com.barangay.domain.entities.DocumentRequestStatus;
import com.barangay.domain.entities.ResidentId;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for document requests.
 */
public interface IDocumentRequestRepository {
    void save(DocumentRequest request);

    void update(DocumentRequest request);

    Optional<DocumentRequest> findById(String id);

    List<DocumentRequest> findAll();

    List<DocumentRequest> findByStatus(DocumentRequestStatus status);

    List<DocumentRequest> findByResidentId(ResidentId residentId);

    List<DocumentRequest> search(String term);

    int countByStatuses(DocumentRequestStatus... statuses);

    String generateNextId();
}
