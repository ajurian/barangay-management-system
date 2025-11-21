package com.barangay.infrastructure.persistence;

import com.barangay.domain.entities.*;
import com.barangay.domain.repositories.IVoterApplicationRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * SQLite implementation of IVoterApplicationRepository.
 */
public class VoterApplicationRepository implements IVoterApplicationRepository {

    @Override
    public void save(VoterApplication application) {
        String sql = "INSERT OR REPLACE INTO voter_applications " +
                "(id, resident_id, application_type, current_registration_details, " +
                "valid_id_front_path, valid_id_back_path, status, review_notes, reviewed_by, " +
                "appointment_datetime, appointment_venue, appointment_slip_reference, " +
                "submitted_at, reviewed_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, application.getId());
            pstmt.setString(2, application.getResidentId().getValue());
            pstmt.setString(3, application.getApplicationType().name());
            pstmt.setString(4, application.getCurrentRegistrationDetails());
            pstmt.setString(5, application.getValidIdFrontPath());
            pstmt.setString(6, application.getValidIdBackPath());
            pstmt.setString(7, application.getStatus().name());
            pstmt.setString(8, application.getReviewNotes());
            pstmt.setString(9, application.getReviewedBy());
            pstmt.setString(10,
                    application.getAppointmentDateTime() != null ? application.getAppointmentDateTime().toString()
                            : null);
            pstmt.setString(11, application.getAppointmentVenue());
            pstmt.setString(12, application.getAppointmentSlipReference());
            pstmt.setString(13, application.getSubmittedAt().toString());
            pstmt.setString(14, application.getReviewedAt() != null ? application.getReviewedAt().toString() : null);
            pstmt.setString(15, application.getUpdatedAt().toString());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save voter application", e);
        }
    }

    @Override
    public Optional<VoterApplication> findById(String id) {
        String sql = "SELECT * FROM voter_applications WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToApplication(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find voter application", e);
        }

        return Optional.empty();
    }

    @Override
    public List<VoterApplication> findByResidentId(ResidentId residentId) {
        String sql = "SELECT * FROM voter_applications WHERE resident_id = ? ORDER BY submitted_at DESC";
        List<VoterApplication> applications = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, residentId.getValue());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                applications.add(mapResultSetToApplication(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find applications by resident", e);
        }

        return applications;
    }

    @Override
    public List<VoterApplication> findByStatus(ApplicationStatus status) {
        String sql = "SELECT * FROM voter_applications WHERE status = ? ORDER BY submitted_at DESC";
        List<VoterApplication> applications = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status.name());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                applications.add(mapResultSetToApplication(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find applications by status", e);
        }

        return applications;
    }

    @Override
    public List<VoterApplication> findAll() {
        String sql = "SELECT * FROM voter_applications ORDER BY submitted_at DESC";
        List<VoterApplication> applications = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                applications.add(mapResultSetToApplication(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all applications", e);
        }

        return applications;
    }

    @Override
    public int countPending() {
        String sql = "SELECT COUNT(*) FROM voter_applications WHERE status = 'PENDING'";

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to count pending applications", e);
        }

        return 0;
    }

    @Override
    public String generateNextId() {
        String year = String.valueOf(LocalDateTime.now().getYear());
        String sql = "SELECT MAX(CAST(SUBSTR(id, LENGTH(id) - 9, 10) AS INTEGER)) FROM voter_applications WHERE id LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "VA-" + year + "-%");
            ResultSet rs = pstmt.executeQuery();

            int nextSequence = 1;
            if (rs.next()) {
                nextSequence = rs.getInt(1) + 1;
            }

            return String.format("VA-%s-%010d", year, nextSequence);

        } catch (SQLException e) {
            throw new RuntimeException("Failed to generate application ID", e);
        }
    }

    private VoterApplication mapResultSetToApplication(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        ResidentId residentId = ResidentId.fromString(rs.getString("resident_id"));
        ApplicationType applicationType = ApplicationType.valueOf(rs.getString("application_type"));

        ApplicationStatus status = ApplicationStatus.valueOf(rs.getString("status"));

        LocalDateTime appointmentDateTime = parseDateTime(rs.getString("appointment_datetime"));
        LocalDateTime submittedAt = parseDateTime(rs.getString("submitted_at"));
        LocalDateTime reviewedAt = parseDateTime(rs.getString("reviewed_at"));
        LocalDateTime updatedAt = parseDateTime(rs.getString("updated_at"));

        return VoterApplication.restoreFromPersistence(
                id,
                residentId,
                applicationType,
                rs.getString("current_registration_details"),
                rs.getString("valid_id_front_path"),
                rs.getString("valid_id_back_path"),
                status,
                rs.getString("review_notes"),
                rs.getString("reviewed_by"),
                appointmentDateTime,
                rs.getString("appointment_venue"),
                rs.getString("appointment_slip_reference"),
                submittedAt,
                reviewedAt,
                updatedAt);
    }

    private LocalDateTime parseDateTime(String value) {
        return value == null ? null : LocalDateTime.parse(value);
    }
}
