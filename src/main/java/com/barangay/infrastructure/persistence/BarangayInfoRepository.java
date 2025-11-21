package com.barangay.infrastructure.persistence;

import com.barangay.domain.entities.BarangayInfo;
import com.barangay.domain.repositories.IBarangayInfoRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * SQLite implementation of {@link IBarangayInfoRepository}.
 */
public class BarangayInfoRepository implements IBarangayInfoRepository {

    private static final String INFO_ID = "BRGY_INFO";

    @Override
    public void save(BarangayInfo info) {
        if (info == null) {
            throw new IllegalArgumentException("Barangay info cannot be null");
        }

        String sql = "INSERT INTO barangay_info " +
                "(id, barangay_name, city, province, region, address, contact_number, email, seal_path, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT(id) DO UPDATE SET " +
                "barangay_name = excluded.barangay_name, " +
                "city = excluded.city, " +
                "province = excluded.province, " +
                "region = excluded.region, " +
                "address = excluded.address, " +
                "contact_number = excluded.contact_number, " +
                "email = excluded.email, " +
                "seal_path = excluded.seal_path, " +
                "updated_at = excluded.updated_at";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String id = info.getId() != null ? info.getId() : INFO_ID;
            pstmt.setString(1, id);
            pstmt.setString(2, info.getBarangayName());
            pstmt.setString(3, info.getCity());
            pstmt.setString(4, info.getProvince());
            pstmt.setString(5, info.getRegion());
            pstmt.setString(6, info.getAddress());
            pstmt.setString(7, info.getContactNumber());
            pstmt.setString(8, info.getEmail());
            pstmt.setString(9, info.getSealPath());
            LocalDateTime updatedAt = info.getUpdatedAt() != null ? info.getUpdatedAt() : LocalDateTime.now();
            pstmt.setString(10, updatedAt.toString());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save barangay info", e);
        }
    }

    @Override
    public Optional<BarangayInfo> get() {
        String sql = "SELECT * FROM barangay_info WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, INFO_ID);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load barangay info", e);
        }

        return Optional.empty();
    }

    private BarangayInfo mapRow(ResultSet rs) throws SQLException {
        return new BarangayInfo(
                rs.getString("id"),
                rs.getString("barangay_name"),
                rs.getString("city"),
                rs.getString("province"),
                rs.getString("region"),
                rs.getString("address"),
                rs.getString("contact_number"),
                rs.getString("email"),
                rs.getString("seal_path"),
                parseDateTime(rs.getString("updated_at")));
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDateTime.parse(value);
    }
}
