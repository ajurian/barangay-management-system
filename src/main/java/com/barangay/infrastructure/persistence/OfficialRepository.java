package com.barangay.infrastructure.persistence;

import com.barangay.domain.entities.*;
import com.barangay.domain.repositories.IOfficialRepository;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * SQLite implementation of IOfficialRepository.
 */
public class OfficialRepository implements IOfficialRepository {

    @Override
    public void save(BarangayOfficial official) {
        String sql = "INSERT OR REPLACE INTO barangay_officials " +
                "(id, resident_id, official_name, position, term_start, term_end, is_current, " +
                "photo_path, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, official.getId());
            pstmt.setString(2, official.getResidentId().getValue());
            pstmt.setString(3, official.getOfficialName());
            pstmt.setString(4, official.getPosition().name());
            pstmt.setString(5, official.getTermStart().toString());
            pstmt.setString(6, official.getTermEnd().toString());
            pstmt.setInt(7, official.isCurrent() ? 1 : 0);
            pstmt.setString(8, official.getPhotoPath());
            pstmt.setString(9, official.getCreatedAt().toString());
            pstmt.setString(10, official.getUpdatedAt().toString());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save official", e);
        }
    }

    @Override
    public void update(BarangayOfficial official) {
        String sql = "UPDATE barangay_officials SET " +
                "official_name = ?, position = ?, term_start = ?, term_end = ?, " +
                "is_current = ?, photo_path = ?, updated_at = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, official.getOfficialName());
            pstmt.setString(2, official.getPosition().name());
            pstmt.setString(3, official.getTermStart().toString());
            pstmt.setString(4, official.getTermEnd().toString());
            pstmt.setInt(5, official.isCurrent() ? 1 : 0);
            pstmt.setString(6, official.getPhotoPath());
            pstmt.setString(7, LocalDateTime.now().toString());
            pstmt.setString(8, official.getId());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update official", e);
        }
    }

    @Override
    public Optional<BarangayOfficial> findById(String id) {
        String sql = "SELECT * FROM barangay_officials WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToOfficial(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find official", e);
        }

        return Optional.empty();
    }

    @Override
    public List<BarangayOfficial> findCurrentOfficials() {
        String sql = "SELECT * FROM barangay_officials WHERE is_current = 1 ORDER BY position";
        List<BarangayOfficial> officials = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                officials.add(mapResultSetToOfficial(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find current officials", e);
        }

        return officials;
    }

    @Override
    public BarangayOfficial findCurrentByPosition(OfficialPosition position) {
        String sql = "SELECT * FROM barangay_officials WHERE position = ? AND is_current = 1";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, position.name());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToOfficial(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find official by position", e);
        }

        return null;
    }

    @Override
    public List<BarangayOfficial> findCurrentOfficialsByPosition(OfficialPosition position) {
        String sql = "SELECT * FROM barangay_officials WHERE position = ? AND is_current = 1 ORDER BY term_start DESC";
        List<BarangayOfficial> officials = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, position.name());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                officials.add(mapResultSetToOfficial(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find current officials by position", e);
        }

        return officials;
    }

    @Override
    public int countCurrentByPosition(OfficialPosition position) {
        String sql = "SELECT COUNT(*) FROM barangay_officials WHERE position = ? AND is_current = 1";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, position.name());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to count current officials by position", e);
        }

        return 0;
    }

    @Override
    public List<BarangayOfficial> findByPosition(OfficialPosition position) {
        String sql = "SELECT * FROM barangay_officials WHERE position = ? ORDER BY term_start DESC";
        List<BarangayOfficial> officials = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, position.name());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                officials.add(mapResultSetToOfficial(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find officials by position", e);
        }

        return officials;
    }

    @Override
    public List<BarangayOfficial> findByResidentId(ResidentId residentId) {
        String sql = "SELECT * FROM barangay_officials WHERE resident_id = ? ORDER BY term_start DESC";
        List<BarangayOfficial> officials = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, residentId.getValue());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                officials.add(mapResultSetToOfficial(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find officials by resident", e);
        }

        return officials;
    }

    @Override
    public List<BarangayOfficial> findAll() {
        String sql = "SELECT * FROM barangay_officials ORDER BY term_start DESC";
        List<BarangayOfficial> officials = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                officials.add(mapResultSetToOfficial(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all officials", e);
        }

        return officials;
    }

    @Override
    public String generateNextId() {
        return "OFF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private BarangayOfficial mapResultSetToOfficial(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        ResidentId residentId = ResidentId.fromString(rs.getString("resident_id"));
        String officialName = rs.getString("official_name");
        OfficialPosition position = OfficialPosition.valueOf(rs.getString("position"));
        LocalDate termStart = LocalDate.parse(rs.getString("term_start"));
        LocalDate termEnd = LocalDate.parse(rs.getString("term_end"));
        boolean isCurrent = rs.getInt("is_current") == 1;
        String photoPath = rs.getString("photo_path");
        return new BarangayOfficial(id, residentId, officialName, position,
                termStart, termEnd, isCurrent, photoPath);
    }
}
