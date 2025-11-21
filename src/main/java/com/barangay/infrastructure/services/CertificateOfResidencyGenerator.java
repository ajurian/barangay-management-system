package com.barangay.infrastructure.services;

import com.barangay.application.ports.IDocumentGenerator;
import com.barangay.domain.entities.Document;
import com.barangay.domain.entities.Resident;

import java.time.LocalDate;
import java.time.Period;

/**
 * Strategy implementation for Certificate of Residency generation.
 */
public class CertificateOfResidencyGenerator implements IDocumentGenerator {

    @Override
    public String generateContent(Document document, Resident resident) {
        StringBuilder content = new StringBuilder();
        content.append("=================================\n");
        content.append("   CERTIFICATE OF RESIDENCY      \n");
        content.append("=================================\n\n");
        content.append("Reference No: ").append(document.getReference().getValue()).append("\n\n");
        content.append("TO WHOM IT MAY CONCERN:\n\n");
        content.append("This is to certify that ").append(resident.getFullName()).append(", ");
        content.append(resident.getAge()).append(" years old, ");
        content.append(resident.getGender() == com.barangay.domain.entities.Gender.MALE ? "male" : "female");
        content.append(", ");

        if (resident.getCivilStatus() != null) {
            content.append(resident.getCivilStatus().toString().toLowerCase()).append(", ");
        }

        content.append("is a bona fide resident of ");
        content.append(resident.getAddress() != null ? resident.getAddress().getFullAddress() : "this barangay");
        content.append(".\n\n");

        // Calculate residency duration
        Period residencyPeriod = Period.between(resident.getRegisteredAt().toLocalDate(), LocalDate.now());
        int years = residencyPeriod.getYears();
        int months = residencyPeriod.getMonths();

        content.append("The above-named person has been a resident of this barangay for ");
        if (years > 0) {
            content.append(years).append(years == 1 ? " year" : " years");
            if (months > 0) {
                content.append(" and ").append(months).append(months == 1 ? " month" : " months");
            }
        } else if (months > 0) {
            content.append(months).append(months == 1 ? " month" : " months");
        } else {
            content.append("less than a month");
        }
        content.append(".\n\n");

        if (document.getPurpose() != null && !document.getPurpose().isEmpty()) {
            content.append("This certification is issued upon request of the interested party ");
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
        return resident.isActive();
    }
}
