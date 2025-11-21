package com.barangay.application.usecases;

import com.barangay.domain.entities.Document;
import com.barangay.domain.entities.DocumentType;
import com.barangay.domain.entities.ResidentId;
import com.barangay.domain.repositories.IDocumentRepository;
import com.barangay.domain.valueobjects.DocumentReference;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Use Case: Search and Manage Documents
 */
public class SearchDocumentsUseCase {
    private final IDocumentRepository documentRepository;

    public SearchDocumentsUseCase(IDocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public Optional<Document> findByReference(String reference) {
        return documentRepository.findByReference(DocumentReference.fromString(reference));
    }

    public List<Document> findByResident(String residentId) {
        return documentRepository.findByResidentId(ResidentId.fromString(residentId));
    }

    public List<Document> findByType(DocumentType type) {
        return documentRepository.findByType(type);
    }

    public List<Document> findByDate(LocalDate date) {
        return documentRepository.findByIssuedDate(date);
    }

    public List<Document> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return documentRepository.findByDateRange(startDate, endDate);
    }

    public List<Document> search(String query) {
        return documentRepository.search(query);
    }
}
