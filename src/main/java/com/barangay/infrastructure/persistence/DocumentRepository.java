package com.barangay.infrastructure.persistence;

import com.barangay.domain.entities.Document;
import com.barangay.domain.entities.DocumentType;
import com.barangay.domain.entities.ResidentId;
import com.barangay.domain.repositories.IDocumentRepository;
import com.barangay.domain.valueobjects.DocumentReference;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * SQLite implementation of IDocumentRepository.
 */
public class DocumentRepository implements IDocumentRepository {

    @Override
    public void save(Document document) {
        String sql = "INSERT INTO documents " +
            "(reference, resident_id, type, purpose, issued_date, valid_until, " +
            "issued_by, additional_info, photo_path, request_id, created_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, document.getReference().getValue());
            pstmt.setString(2, document.getResidentId().getValue());
            pstmt.setString(3, document.getType().name());
            pstmt.setString(4, document.getPurpose());
            pstmt.setString(5, document.getIssuedDate().toString());
            pstmt.setString(6, document.getValidUntil() != null ? document.getValidUntil().toString() : null);
            pstmt.setString(7, document.getIssuedBy());
            pstmt.setString(8, document.getAdditionalInfo());
            pstmt.setString(9, document.getPhotoPath());
            pstmt.setString(10, document.getOriginRequestId());
            pstmt.setString(11, document.getCreatedAt().toString());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save document", e);
        }
    }

    @Override
    public Optional<Document> findByReference(DocumentReference reference) {
        String sql = "SELECT * FROM documents WHERE reference = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, reference.getValue());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToDocument(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find document by reference", e);
        }

        return Optional.empty();
    }

    @Override
    public List<Document> findByResidentId(ResidentId residentId) {
        String sql = "SELECT * FROM documents WHERE resident_id = ? ORDER BY issued_date DESC";
        List<Document> documents = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, residentId.getValue());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                documents.add(mapResultSetToDocument(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find documents by resident ID", e);
        }

        return documents;
    }

    @Override
    public List<Document> findByType(DocumentType type) {
        String sql = "SELECT * FROM documents WHERE type = ? ORDER BY issued_date DESC";
        List<Document> documents = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, type.name());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                documents.add(mapResultSetToDocument(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find documents by type", e);
        }

        return documents;
    }

    @Override
    public List<Document> findByIssuedDate(LocalDate date) {
        String sql = "SELECT * FROM documents WHERE issued_date = ? ORDER BY created_at DESC";
        List<Document> documents = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, date.toString());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                documents.add(mapResultSetToDocument(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find documents by issued date", e);
        }

        return documents;
    }

    @Override
    public List<Document> findByDateRange(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT * FROM documents WHERE issued_date >= ? AND issued_date <= ? " +
                "ORDER BY issued_date DESC";
        List<Document> documents = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, startDate.toString());
            pstmt.setString(2, endDate.toString());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                documents.add(mapResultSetToDocument(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find documents by date range", e);
        }

        return documents;
    }

    @Override
    public List<Document> search(String query) {
        String sql = "SELECT * FROM documents WHERE reference LIKE ? OR purpose LIKE ? " +
                "ORDER BY issued_date DESC";
        List<Document> documents = new ArrayList<>();
        String searchPattern = "%" + query + "%";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                documents.add(mapResultSetToDocument(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to search documents", e);
        }

        return documents;
    }

    @Override
    public int countIssuedToday() {
        String today = LocalDate.now().toString();
        String sql = "SELECT COUNT(*) FROM documents WHERE issued_date = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, today);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to count documents issued today", e);
        }

        return 0;
    }

    @Override
    public int countIssuedThisMonth() {
        LocalDate now = LocalDate.now();
        String startOfMonth = now.withDayOfMonth(1).toString();
        String endOfMonth = now.withDayOfMonth(now.lengthOfMonth()).toString();

        String sql = "SELECT COUNT(*) FROM documents WHERE issued_date >= ? AND issued_date <= ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, startOfMonth);
            pstmt.setString(2, endOfMonth);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to count documents issued this month", e);
        }

        return 0;
    }

    @Override
    public int countByType(DocumentType type) {
        String sql = "SELECT COUNT(*) FROM documents WHERE type = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, type.name());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to count documents by type", e);
        }

        return 0;
    }

    @Override
    public DocumentReference generateNextReference(DocumentType type) {
        String prefix;
        switch (type) {
            case BARANGAY_ID:
                prefix = "BID";
                break;
            case BARANGAY_CLEARANCE:
                prefix = "BC";
                break;
            case CERTIFICATE_OF_RESIDENCY:
                prefix = "CR";
                break;
            default:
                throw new IllegalArgumentException("Unknown document type");
        }

        String year = String.valueOf(LocalDate.now().getYear());
        String sql = "SELECT MAX(CAST(SUBSTR(reference, LENGTH(reference) - 9, 10) AS INTEGER)) FROM documents " +
                "WHERE reference LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, prefix + "-" + year + "-%");
            ResultSet rs = pstmt.executeQuery();

            int nextSequence = 1;
            if (rs.next()) {
                nextSequence = rs.getInt(1) + 1;
            }

            String reference = String.format("%s-%s-%010d", prefix, year, nextSequence);
            return DocumentReference.fromString(reference);

        } catch (SQLException e) {
            throw new RuntimeException("Failed to generate document reference", e);
        }
    }

    private Document mapResultSetToDocument(ResultSet rs) throws SQLException {
        DocumentReference reference = DocumentReference.fromString(rs.getString("reference"));
        ResidentId residentId = ResidentId.fromString(rs.getString("resident_id"));
        DocumentType type = DocumentType.valueOf(rs.getString("type"));
        String purpose = rs.getString("purpose");
        LocalDate issuedDate = LocalDate.parse(rs.getString("issued_date"));

        String validUntilStr = rs.getString("valid_until");
        LocalDate validUntil = validUntilStr != null ? LocalDate.parse(validUntilStr) : null;

        String issuedBy = rs.getString("issued_by");

        Document document = new Document(
                reference,
                residentId,
                type,
                purpose,
                issuedDate,
                validUntil,
                issuedBy);

        document.setAdditionalInfo(rs.getString("additional_info"));
        document.setOriginRequestId(rs.getString("request_id"));
        document.setPhotoPath(rs.getString("photo_path"));

        return document;
    }
}
