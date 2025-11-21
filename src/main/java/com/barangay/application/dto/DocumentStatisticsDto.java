package com.barangay.application.dto;

/**
 * DTO for document statistics
 * Module 7: Reports & Analytics
 */
public class DocumentStatisticsDto {
    private final int totalDocuments;
    private final int documentsToday;
    private final int documentsThisMonth;
    private final int barangayIDs;
    private final int clearances;
    private final int residencyCertificates;

    public DocumentStatisticsDto(int totalDocuments, int documentsToday,
            int documentsThisMonth, int barangayIDs,
            int clearances, int residencyCertificates) {
        this.totalDocuments = totalDocuments;
        this.documentsToday = documentsToday;
        this.documentsThisMonth = documentsThisMonth;
        this.barangayIDs = barangayIDs;
        this.clearances = clearances;
        this.residencyCertificates = residencyCertificates;
    }

    public int getTotalDocuments() {
        return totalDocuments;
    }

    public int getDocumentsToday() {
        return documentsToday;
    }

    public int getDocumentsThisMonth() {
        return documentsThisMonth;
    }

    public int getBarangayIDs() {
        return barangayIDs;
    }

    public int getClearances() {
        return clearances;
    }

    public int getResidencyCertificates() {
        return residencyCertificates;
    }
}
