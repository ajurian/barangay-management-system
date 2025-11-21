package com.barangay.application.ports;

import com.barangay.domain.entities.Document;
import com.barangay.domain.entities.Resident;

/**
 * Port interface for document generation strategies.
 * Following Strategy Pattern and ISP.
 */
public interface IDocumentGenerator {
    /**
     * Generate document content for printing/preview
     */
    String generateContent(Document document, Resident resident);

    /**
     * Validate document-specific requirements
     */
    boolean validate(Document document, Resident resident);
}
