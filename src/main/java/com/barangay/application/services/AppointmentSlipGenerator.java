package com.barangay.application.services;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Generates a PDF appointment slip using iText.
 */
public class AppointmentSlipGenerator {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMMM d, yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("h:mm a");

    public byte[] generate(AppointmentSlipData data) {
        Objects.requireNonNull(data, "Appointment slip data is required");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PdfWriter writer = new PdfWriter(out);
                PdfDocument pdf = new PdfDocument(writer);
                Document document = new Document(pdf)) {
            document.setMargins(40, 40, 40, 40);

            addHeader(document, data);
            addApplicantSection(document, data);
            addAppointmentSection(document, data);
            addReminders(document, data);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to generate appointment slip PDF", ex);
        }
        return out.toByteArray();
    }

    private void addHeader(Document document, AppointmentSlipData data) {
        document.add(new Paragraph(safeValue(data.getBarangayName(), "Barangay Management Office"))
                .setTextAlignment(TextAlignment.CENTER)
                .setBold()
                .setFontSize(16));
        StringBuilder addressLine = new StringBuilder();
        if (data.getBarangayAddress() != null) {
            addressLine.append(data.getBarangayAddress());
        }
        if (data.getBarangayContact() != null && !data.getBarangayContact().isBlank()) {
            if (addressLine.length() > 0) {
                addressLine.append(" | ");
            }
            addressLine.append("Tel: ").append(data.getBarangayContact());
        }
        if (data.getBarangayEmail() != null && !data.getBarangayEmail().isBlank()) {
            if (addressLine.length() > 0) {
                addressLine.append(" | ");
            }
            addressLine.append(data.getBarangayEmail());
        }
        if (addressLine.length() > 0) {
            document.add(new Paragraph(addressLine.toString()).setTextAlignment(TextAlignment.CENTER));
        }
        document.add(new Paragraph("APPOINTMENT SLIP")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(13)
                .setBold());
        document.add(new Paragraph("Slip Reference: " + safeValue(data.getSlipReference(), "N/A"))
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(15)
                .setBold());
    }

    private void addApplicantSection(Document document, AppointmentSlipData data) {
        document.add(new Paragraph("Applicant Information").setBold());
        Table table = new Table(UnitValue.createPercentArray(new float[] { 35, 65 }))
                .setWidth(UnitValue.createPercentValue(100));
        addRow(table, "Application ID", data.getApplicationId());
        addRow(table, "Application Type", data.getApplicationType());
        addRow(table, "Resident ID", data.getResidentId());
        addRow(table, "Applicant Name", data.getApplicantName());
        addRow(table, "Contact", data.getApplicantContact());
        addRow(table, "Address", data.getApplicantAddress());
        table.setMarginBottom(12);
        document.add(table);
    }

    private void addAppointmentSection(Document document, AppointmentSlipData data) {
        document.add(new Paragraph("Appointment Details").setBold());
        Table table = new Table(UnitValue.createPercentArray(new float[] { 35, 65 }))
                .setWidth(UnitValue.createPercentValue(100));
        LocalDateTime schedule = data.getAppointmentDateTime();
        addRow(table, "Date", schedule != null ? DATE_FORMAT.format(schedule) : "--");
        addRow(table, "Time", schedule != null ? TIME_FORMAT.format(schedule) : "--");
        addRow(table, "Venue", data.getAppointmentVenue());
        addRow(table, "Slip Reference", data.getSlipReference());
        table.setMarginBottom(12);
        document.add(table);
    }

    private void addReminders(Document document, AppointmentSlipData data) {
        List<String> reminders = data.getReminders() != null ? data.getReminders() : Collections.emptyList();
        if (reminders.isEmpty()) {
            return;
        }
        document.add(new Paragraph("Reminders").setBold());
        for (String reminder : reminders) {
            document.add(new Paragraph("• " + reminder).setMarginLeft(12));
        }
    }

    private void addRow(Table table, String label, String value) {
        table.addCell(new Cell().add(new Paragraph(label)).setBorder(Border.NO_BORDER).setBold());
        table.addCell(new Cell().add(new Paragraph(safeValue(value, "--"))).setBorder(Border.NO_BORDER));
    }

    private String safeValue(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    /**
     * Immutable data required to craft the slip layout.
     */
    public static class AppointmentSlipData {
        private final String barangayName;
        private final String barangayAddress;
        private final String barangayContact;
        private final String barangayEmail;
        private final String applicationId;
        private final String applicationType;
        private final String residentId;
        private final String applicantName;
        private final String applicantContact;
        private final String applicantAddress;
        private final LocalDateTime appointmentDateTime;
        private final String appointmentVenue;
        private final String slipReference;
        private final List<String> reminders;

        public AppointmentSlipData(String barangayName, String barangayAddress, String barangayContact,
                String barangayEmail, String applicationId, String applicationType, String residentId,
                String applicantName, String applicantContact, String applicantAddress,
                LocalDateTime appointmentDateTime, String appointmentVenue, String slipReference,
                List<String> reminders) {
            this.barangayName = barangayName;
            this.barangayAddress = barangayAddress;
            this.barangayContact = barangayContact;
            this.barangayEmail = barangayEmail;
            this.applicationId = applicationId;
            this.applicationType = applicationType;
            this.residentId = residentId;
            this.applicantName = applicantName;
            this.applicantContact = applicantContact;
            this.applicantAddress = applicantAddress;
            this.appointmentDateTime = appointmentDateTime;
            this.appointmentVenue = appointmentVenue;
            this.slipReference = slipReference;
            this.reminders = reminders;
        }

        public String getBarangayName() {
            return barangayName;
        }

        public String getBarangayAddress() {
            return barangayAddress;
        }

        public String getBarangayContact() {
            return barangayContact;
        }

        public String getBarangayEmail() {
            return barangayEmail;
        }

        public String getApplicationId() {
            return applicationId;
        }

        public String getApplicationType() {
            return applicationType;
        }

        public String getResidentId() {
            return residentId;
        }

        public String getApplicantName() {
            return applicantName;
        }

        public String getApplicantContact() {
            return applicantContact;
        }

        public String getApplicantAddress() {
            return applicantAddress;
        }

        public LocalDateTime getAppointmentDateTime() {
            return appointmentDateTime;
        }

        public String getAppointmentVenue() {
            return appointmentVenue;
        }

        public String getSlipReference() {
            return slipReference;
        }

        public List<String> getReminders() {
            return reminders;
        }
    }
}
