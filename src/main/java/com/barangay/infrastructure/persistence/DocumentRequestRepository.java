package com.barangay.infrastructure.persistence;

import com.barangay.domain.entities.DocumentRequest;
import com.barangay.domain.entities.DocumentRequestStatus;
import com.barangay.domain.entities.DocumentType;
import com.barangay.domain.entities.ResidentId;
import com.barangay.domain.repositories.IDocumentRequestRepository;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * SQLite implementation of IDocumentRequestRepository.
 */
public class DocumentRequestRepository implements IDocumentRequestRepository {

    @Override
    public void save(DocumentRequest request) {
        String sql = "INSERT INTO document_requests (id, resident_id, document_type, purpose, requested_valid_until, " +
                "notes, additional_info, status, staff_notes, handled_by, linked_document_reference, created_at, updated_at) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, request.getId());
            pstmt.setString(2, request.getResidentId().getValue());
            pstmt.setString(3, request.getDocumentType().name());
            pstmt.setString(4, request.getPurpose());
            pstmt.setString(5,
                    request.getRequestedValidUntil() != null ? request.getRequestedValidUntil().toString() : null);
            pstmt.setString(6, request.getResidentNotes());
            pstmt.setString(7, request.getAdditionalInfo());
            pstmt.setString(8, request.getStatus().name());
            pstmt.setString(9, request.getStaffNotes());
            pstmt.setString(10, request.getHandledBy());
            pstmt.setString(11, request.getLinkedDocumentReference());
            pstmt.setString(12, request.getCreatedAt().toString());
            pstmt.setString(13, request.getUpdatedAt().toString());
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to save document request", ex);
        }
    }

    @Override
    public void update(DocumentRequest request) {
        String sql = "UPDATE document_requests SET purpose = ?, requested_valid_until = ?, notes = ?, additional_info = ?, "
                +
                "status = ?, staff_notes = ?, handled_by = ?, linked_document_reference = ?, updated_at = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, request.getPurpose());
            pstmt.setString(2,
                    request.getRequestedValidUntil() != null ? request.getRequestedValidUntil().toString() : null);
            pstmt.setString(3, request.getResidentNotes());
            pstmt.setString(4, request.getAdditionalInfo());
            pstmt.setString(5, request.getStatus().name());
            pstmt.setString(6, request.getStaffNotes());
            pstmt.setString(7, request.getHandledBy());
            pstmt.setString(8, request.getLinkedDocumentReference());
            pstmt.setString(9, request.getUpdatedAt().toString());
            pstmt.setString(10, request.getId());
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to update document request", ex);
        }
    }

    @Override
    public Optional<DocumentRequest> findById(String id) {
        String sql = "SELECT * FROM document_requests WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to find document request", ex);
        }
        return Optional.empty();
    }

    @Override
    public List<DocumentRequest> findAll() {
        return query("SELECT * FROM document_requests ORDER BY created_at DESC");
    }

    @Override
    public List<DocumentRequest> findByStatus(DocumentRequestStatus status) {
        String sql = "SELECT * FROM document_requests WHERE status = ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status.name());
            ResultSet rs = pstmt.executeQuery();
            List<DocumentRequest> results = new ArrayList<>();
            while (rs.next()) {
                results.add(mapRow(rs));
            }
            return results;
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to find document requests by status", ex);
        }
    }

    @Override
    public List<DocumentRequest> findByResidentId(ResidentId residentId) {
        String sql = "SELECT * FROM document_requests WHERE resident_id = ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, residentId.getValue());
            ResultSet rs = pstmt.executeQuery();
            List<DocumentRequest> results = new ArrayList<>();
            while (rs.next()) {
                results.add(mapRow(rs));
            }
            return results;
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to find document requests by resident", ex);
        }
    }

    @Override
    public List<DocumentRequest> search(String term) {
        String sql = "SELECT * FROM document_requests WHERE id LIKE ? OR resident_id LIKE ? OR purpose LIKE ? ORDER BY created_at DESC";
        String like = "%" + term + "%";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, like);
            pstmt.setString(2, like);
            pstmt.setString(3, like);
            ResultSet rs = pstmt.executeQuery();
            List<DocumentRequest> results = new ArrayList<>();
            while (rs.next()) {
                results.add(mapRow(rs));
            }
            return results;
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to search document requests", ex);
        }
    }

    @Override
    public int countByStatuses(DocumentRequestStatus... statuses) {
        if (statuses == null || statuses.length == 0) {
            return 0;
        }
        String placeholders = Arrays.stream(statuses).map(s -> "?").collect(Collectors.joining(","));
        String sql = "SELECT COUNT(*) FROM document_requests WHERE status IN (" + placeholders + ")";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < statuses.length; i++) {
                pstmt.setString(i + 1, statuses[i].name());
            }
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to count document requests", ex);
        }
        return 0;
    }

    @Override
    public String generateNextId() {
        String year = String.valueOf(LocalDate.now().getYear());
        String sql = "SELECT MAX(CAST(SUBSTR(id, LENGTH(id) - 9, 10) AS INTEGER)) FROM document_requests WHERE id LIKE ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "DR-" + year + "-%");
            ResultSet rs = pstmt.executeQuery();
            int next = 1;
            if (rs.next()) {
                next = rs.getInt(1) + 1;
            }
            return String.format("DR-%s-%010d", year, next);
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to generate next document request id", ex);
        }
    }

    private List<DocumentRequest> query(String sql) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            List<DocumentRequest> results = new ArrayList<>();
            while (rs.next()) {
                results.add(mapRow(rs));
            }
            return results;
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to query document requests", ex);
        }
    }

    private DocumentRequest mapRow(ResultSet rs) throws SQLException {
        ResidentId residentId = ResidentId.fromString(rs.getString("resident_id"));
        DocumentType type = DocumentType.valueOf(rs.getString("document_type"));
        String purpose = rs.getString("purpose");
        String requestedValidUntil = rs.getString("requested_valid_until");
        LocalDate validUntil = requestedValidUntil != null ? LocalDate.parse(requestedValidUntil) : null;
        String notes = rs.getString("notes");
        String additionalInfo = rs.getString("additional_info");
        DocumentRequestStatus status = DocumentRequestStatus.valueOf(rs.getString("status"));
        String staffNotes = rs.getString("staff_notes");
        String handledBy = rs.getString("handled_by");
        String linkedRef = rs.getString("linked_document_reference");
        LocalDateTime createdAt = LocalDateTime.parse(rs.getString("created_at"));
        LocalDateTime updatedAt = LocalDateTime.parse(rs.getString("updated_at"));

        return new DocumentRequest(
                rs.getString("id"),
                residentId,
                type,
                purpose,
                validUntil,
                notes,
                additionalInfo,
                status,
                staffNotes,
                handledBy,
                linkedRef,
                createdAt,
                updatedAt);
    }
}
