package com.barangay.infrastructure.services;

import com.barangay.application.ports.IDocumentGenerator;
import com.barangay.domain.entities.Document;
import com.barangay.domain.entities.Resident;

/**
 * Strategy implementation for Barangay ID generation.
 * Following OCP: New document types can be added without modifying existing
 * code.
 */
public class BarangayIDGenerator implements IDocumentGenerator {

    @Override
    public String generateContent(Document document, Resident resident) {
        StringBuilder content = new StringBuilder();
        content.append("=================================\n");
        content.append("      BARANGAY IDENTIFICATION    \n");
        content.append("=================================\n\n");
        content.append("ID Number: ").append(document.getReference().getValue()).append("\n\n");
        content.append("Name: ").append(resident.getFullName()).append("\n");
        content.append("Birth Date: ").append(resident.getBirthDate()).append("\n");
        content.append("Gender: ").append(resident.getGender()).append("\n");
        content.append("Address: ")
                .append(resident.getAddress() != null ? resident.getAddress().getFullAddress() : "N/A").append("\n\n");

        if (document.getAdditionalInfo() != null) {
            content.append("Emergency Contact: ").append(document.getAdditionalInfo()).append("\n");
        }

        content.append("\nIssued: ").append(document.getIssuedDate()).append("\n");
        if (document.getValidUntil() != null) {
            content.append("Valid Until: ").append(document.getValidUntil()).append("\n");
        }
        content.append("\nIssued By: ").append(document.getIssuedBy()).append("\n");

        return content.toString();
    }

    @Override
    public boolean validate(Document document, Resident resident) {
        return resident.isActive();
    }
}
