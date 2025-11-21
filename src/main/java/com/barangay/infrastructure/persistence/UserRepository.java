package com.barangay.infrastructure.persistence;

import com.barangay.domain.entities.User;
import com.barangay.domain.entities.UserRole;
import com.barangay.domain.entities.ResidentId;
import com.barangay.domain.repositories.IUserRepository;
import com.barangay.domain.valueobjects.UserId;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * SQLite implementation of IUserRepository.
 * Following DIP: Implements the repository interface from domain layer.
 * Following SRP: Handles only User entity persistence.
 */
public class UserRepository implements IUserRepository {

    @Override
    public void save(User user) {
        String sql = "INSERT OR REPLACE INTO users " +
                "(id, username, password_hash, role, " +
                "linked_resident_id, is_active, created_at, last_login_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getId().getValue());
            pstmt.setString(2, user.getUsername());
            pstmt.setString(3, user.getPasswordHash());
            pstmt.setString(4, user.getRole().name());
            pstmt.setString(5, user.getLinkedResidentId() != null ? user.getLinkedResidentId().getValue() : null);
            pstmt.setInt(6, user.isActive() ? 1 : 0);
            pstmt.setString(7, user.getCreatedAt().toString());
            pstmt.setString(8, user.getLastLoginAt() != null ? user.getLastLoginAt().toString() : null);
            pstmt.setString(9, user.getUpdatedAt().toString());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save user", e);
        }
    }

    @Override
    public Optional<User> findById(UserId id) {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id.getValue());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToUser(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user by ID", e);
        }

        return Optional.empty();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToUser(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user by username", e);
        }

        return Optional.empty();
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM users ORDER BY created_at DESC";
        List<User> users = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all users", e);
        }

        return users;
    }

    @Override
    public List<User> findByRole(UserRole role) {
        String sql = "SELECT * FROM users WHERE role = ? ORDER BY created_at DESC";
        List<User> users = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, role.name());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find users by role", e);
        }

        return users;
    }

    @Override
    public Optional<User> findByLinkedResidentId(ResidentId residentId) {
        String sql = "SELECT * FROM users WHERE linked_resident_id = ? LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, residentId.getValue());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToUser(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user by linked resident ID", e);
        }

        return Optional.empty();
    }

    @Override
    public List<User> findActiveUsers() {
        String sql = "SELECT * FROM users WHERE is_active = 1 ORDER BY username";
        List<User> users = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find active users", e);
        }

        return users;
    }

    @Override
    public boolean hasSuperAdmin() {
        String sql = "SELECT COUNT(*) FROM users WHERE role = 'SUPER_ADMIN'";

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to check for super admin", e);
        }

        return false;
    }

    @Override
    public int countByRole(UserRole role) {
        String sql = "SELECT COUNT(*) FROM users WHERE role = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, role.name());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to count users by role", e);
        }

        return 0;
    }

    @Override
    public void delete(UserId id) {
        String sql = "UPDATE users SET is_active = 0, updated_at = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, LocalDateTime.now().toString());
            pstmt.setString(2, id.getValue());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete user", e);
        }
    }

    /**
     * Helper method to map ResultSet to User entity
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        UserId id = UserId.fromString(rs.getString("id"));
        String username = rs.getString("username");
        String passwordHash = rs.getString("password_hash");
        UserRole role = UserRole.valueOf(rs.getString("role"));

        User user = new User(id, username, passwordHash, role);

        String linkedResidentId = rs.getString("linked_resident_id");
        if (linkedResidentId != null && !linkedResidentId.isEmpty()) {
            user.setLinkedResidentId(ResidentId.fromString(linkedResidentId));
        }

        boolean isActive = rs.getInt("is_active") == 1;
        if (!isActive) {
            user.deactivate();
        }

        return user;
    }
}
