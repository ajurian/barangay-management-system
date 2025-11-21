package com.barangay.application.usecases;

import com.barangay.application.dto.DocumentStatisticsDto;
import com.barangay.domain.entities.Document;
import com.barangay.domain.entities.DocumentType;
import com.barangay.domain.repositories.IDocumentRepository;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * Use Case: Get document statistics
 * Module 7: Reports & Analytics
 * 
 * SOLID Principles:
 * - SRP: Single responsibility - calculating document statistics
 * - DIP: Depends on repository abstraction
 */
public class GetDocumentStatisticsUseCase {
    private final IDocumentRepository documentRepository;

    public GetDocumentStatisticsUseCase(IDocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public DocumentStatisticsDto execute() {
        // Get all documents by searching with empty query
        List<Document> allDocuments = documentRepository.search("");
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.now();

        long documentsToday = allDocuments.stream()
                .filter(d -> d.getCreatedAt().toLocalDate().equals(today))
                .count();

        long documentsThisMonth = allDocuments.stream()
                .filter(d -> {
                    LocalDate issuedDate = d.getCreatedAt().toLocalDate();
                    return issuedDate.getYear() == currentMonth.getYear() &&
                            issuedDate.getMonthValue() == currentMonth.getMonthValue();
                })
                .count();

        long barangayIDs = allDocuments.stream()
                .filter(d -> d.getType() == DocumentType.BARANGAY_ID)
                .count();

        long clearances = allDocuments.stream()
                .filter(d -> d.getType() == DocumentType.BARANGAY_CLEARANCE)
                .count();

        long residencyCerts = allDocuments.stream()
                .filter(d -> d.getType() == DocumentType.CERTIFICATE_OF_RESIDENCY)
                .count();

        return new DocumentStatisticsDto(
                allDocuments.size(),
                (int) documentsToday,
                (int) documentsThisMonth,
                (int) barangayIDs,
                (int) clearances,
                (int) residencyCerts);
    }
}
