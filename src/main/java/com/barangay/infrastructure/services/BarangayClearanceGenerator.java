package com.barangay.infrastructure.services;

import com.barangay.application.ports.IDocumentGenerator;
import com.barangay.domain.entities.Document;
import com.barangay.domain.entities.Resident;

/**
 * Strategy implementation for Barangay Clearance generation.
 */
public class BarangayClearanceGenerator implements IDocumentGenerator {

    @Override
    public String generateContent(Document document, Resident resident) {
        StringBuilder content = new StringBuilder();
        content.append("=================================\n");
        content.append("      BARANGAY CLEARANCE         \n");
        content.append("=================================\n\n");
        content.append("Reference No: ").append(document.getReference().getValue()).append("\n\n");
        content.append("TO WHOM IT MAY CONCERN:\n\n");
        content.append("This is to certify that ").append(resident.getFullName()).append(", ");
        content.append("of legal age, ");
        content.append(resident.getGender() == com.barangay.domain.entities.Gender.MALE ? "male" : "female");
        content.append(", Filipino, and a resident of ");
        content.append(resident.getAddress() != null ? resident.getAddress().getFullAddress() : "this barangay");
        content.append(" is personally known to me and is of good moral character.\n\n");

        if (document.getPurpose() != null && !document.getPurpose().isEmpty()) {
            content.append("This clearance is being issued upon request of the interested party ");
            content.append("for ").append(document.getPurpose()).append(".\n\n");
        }

        content.append("Issued this ").append(document.getIssuedDate()).append(".\n");
        if (document.getValidUntil() != null) {
            content.append("Valid Until: ").append(document.getValidUntil()).append("\n");
        }
        content.append("\n\nIssued By: ").append(document.getIssuedBy()).append("\n");
        content.append("\n_______________________\n");
        content.append("Authorized Signature\n");

        return content.toString();
    }

    @Override
    public boolean validate(Document document, Resident resident) {
        return resident.isActive() && document.getPurpose() != null;
    }
}
