package com.barangay.application.dto;

/**
 * DTO representing the generated appointment slip PDF document.
 */
public class AppointmentSlipOutputDto {
    private final String suggestedFileName;
    private final byte[] fileContent;

    public AppointmentSlipOutputDto(String suggestedFileName, byte[] fileContent) {
        if (suggestedFileName == null || suggestedFileName.isBlank()) {
            throw new IllegalArgumentException("Suggested file name is required");
        }
        if (fileContent == null || fileContent.length == 0) {
            throw new IllegalArgumentException("PDF content cannot be empty");
        }
        this.suggestedFileName = suggestedFileName;
        this.fileContent = fileContent;
    }

    public String getSuggestedFileName() {
        return suggestedFileName;
    }

    public byte[] getFileContent() {
        return fileContent;
    }
}
