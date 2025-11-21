package com.barangay.infrastructure.services;

import com.barangay.application.ports.IDocumentGenerator;
import com.barangay.domain.entities.DocumentType;

/**
 * Factory for creating document generators.
 * Following Factory Pattern and OCP.
 */
public class DocumentGeneratorFactory {

    public static IDocumentGenerator create(DocumentType type) {
        switch (type) {
            case BARANGAY_ID:
                return new BarangayIDGenerator();
            case BARANGAY_CLEARANCE:
                return new BarangayClearanceGenerator();
            case CERTIFICATE_OF_RESIDENCY:
                return new CertificateOfResidencyGenerator();
            default:
                throw new IllegalArgumentException("Unknown document type: " + type);
        }
    }
}
