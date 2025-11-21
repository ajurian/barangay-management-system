package com.barangay.domain.repositories;

import com.barangay.domain.entities.Document;
import com.barangay.domain.entities.DocumentType;
import com.barangay.domain.entities.ResidentId;
import com.barangay.domain.valueobjects.DocumentReference;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Document entity.
 */
public interface IDocumentRepository {
    /**
     * Save a new document
     */
    void save(Document document);

    /**
     * Find document by reference
     */
    Optional<Document> findByReference(DocumentReference reference);

    /**
     * Find all documents issued to a resident
     */
    List<Document> findByResidentId(ResidentId residentId);

    /**
     * Find documents by type
     */
    List<Document> findByType(DocumentType type);

    /**
     * Find documents issued on a specific date
     */
    List<Document> findByIssuedDate(LocalDate date);

    /**
     * Find documents issued within a date range
     */
    List<Document> findByDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * Search documents by reference or resident name
     */
    List<Document> search(String query);

    /**
     * Count documents issued today
     */
    int countIssuedToday();

    /**
     * Count documents issued this month
     */
    int countIssuedThisMonth();

    /**
     * Count documents by type
     */
    int countByType(DocumentType type);

    /**
     * Generate next document reference
     */
    DocumentReference generateNextReference(DocumentType type);
}
