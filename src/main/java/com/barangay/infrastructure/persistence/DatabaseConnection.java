package com.barangay.infrastructure.persistence;

import com.barangay.domain.entities.BarangayInfo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

/**
 * Database connection manager.
 * Following SRP: Handles only database connection and initialization.
 */
public class DatabaseConnection {
    private static final String DB_URL = "jdbc:sqlite:barangay.db";
    private static Connection connection;

    /**
     * Get database connection (singleton pattern)
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
        }
        return connection;
    }

    /**
     * Initialize database schema
     */
    public static void initializeDatabase() {
        try (Connection conn = getConnection()) {
            ensureUsersTableWithoutContact(conn);
            ensureUsersTableWithoutEmail(conn);

            try (Statement stmt = conn.createStatement()) {

                // Users table
                stmt.execute(
                        "CREATE TABLE IF NOT EXISTS users (" +
                                "id TEXT PRIMARY KEY, " +
                                "username TEXT UNIQUE NOT NULL, " +
                                "password_hash TEXT NOT NULL, " +
                                "role TEXT NOT NULL, " +
                                "linked_resident_id TEXT, " +
                                "is_active INTEGER DEFAULT 1, " +
                                "created_at TEXT NOT NULL, " +
                                "last_login_at TEXT, " +
                                "updated_at TEXT NOT NULL)");

                // Residents table
                stmt.execute(
                        "CREATE TABLE IF NOT EXISTS residents (" +
                                "id TEXT PRIMARY KEY, " +
                                "first_name TEXT NOT NULL, " +
                                "middle_name TEXT, " +
                                "last_name TEXT NOT NULL, " +
                                "suffix TEXT, " +
                                "birth_date TEXT NOT NULL, " +
                                "birth_place TEXT, " +
                                "gender TEXT NOT NULL, " +
                                "civil_status TEXT, " +
                                "nationality TEXT, " +
                                "contact TEXT, " +
                                "house_number TEXT, " +
                                "street TEXT, " +
                                "purok TEXT, " +
                                "barangay TEXT, " +
                                "city TEXT, " +
                                "province TEXT, " +
                                "occupation TEXT, " +
                                "employment TEXT, " +
                                "income_bracket TEXT, " +
                                "education_level TEXT, " +
                                "is_voter INTEGER DEFAULT 0, " +
                                "is_active INTEGER DEFAULT 1, " +
                                "deactivation_reason TEXT, " +
                                "registered_at TEXT NOT NULL, " +
                                "updated_at TEXT NOT NULL)");

                // Document Requests table
                stmt.execute(
                        "CREATE TABLE IF NOT EXISTS document_requests (" +
                                "id TEXT PRIMARY KEY, " +
                                "resident_id TEXT NOT NULL, " +
                                "document_type TEXT NOT NULL, " +
                                "purpose TEXT, " +
                                "requested_valid_until TEXT, " +
                                "notes TEXT, " +
                                "additional_info TEXT, " +
                                "status TEXT NOT NULL, " +
                                "staff_notes TEXT, " +
                                "handled_by TEXT, " +
                                "linked_document_reference TEXT, " +
                                "created_at TEXT NOT NULL, " +
                                "updated_at TEXT NOT NULL, " +
                                "FOREIGN KEY (resident_id) REFERENCES residents(id))");

                // Documents table
                stmt.execute(
                        "CREATE TABLE IF NOT EXISTS documents (" +
                                "reference TEXT PRIMARY KEY, " +
                                "resident_id TEXT NOT NULL, " +
                                "type TEXT NOT NULL, " +
                                "purpose TEXT, " +
                                "issued_date TEXT NOT NULL, " +
                                "valid_until TEXT, " +
                                "issued_by TEXT NOT NULL, " +
                                "additional_info TEXT, " +
                                "request_id TEXT, " +
                                "created_at TEXT NOT NULL, " +
                                "FOREIGN KEY (resident_id) REFERENCES residents(id), " +
                                "FOREIGN KEY (request_id) REFERENCES document_requests(id))");

                ensureDocumentsRequestColumn(stmt);

                // Voter Applications table
                stmt.execute(
                        "CREATE TABLE IF NOT EXISTS voter_applications (" +
                                "id TEXT PRIMARY KEY, " +
                                "resident_id TEXT NOT NULL, " +
                                "application_type TEXT NOT NULL, " +
                                "current_registration_details TEXT, " +
                                "valid_id_front_path TEXT, " +
                                "valid_id_back_path TEXT, " +
                                "status TEXT NOT NULL, " +
                                "review_notes TEXT, " +
                                "reviewed_by TEXT, " +
                                "appointment_datetime TEXT, " +
                                "appointment_venue TEXT, " +
                                "appointment_slip_reference TEXT, " +
                                "submitted_at TEXT NOT NULL, " +
                                "reviewed_at TEXT, " +
                                "updated_at TEXT NOT NULL, " +
                                "FOREIGN KEY (resident_id) REFERENCES residents(id))");

                // Barangay Officials table
                stmt.execute(
                        "CREATE TABLE IF NOT EXISTS barangay_officials (" +
                                "id TEXT PRIMARY KEY, " +
                                "resident_id TEXT NOT NULL, " +
                                "official_name TEXT NOT NULL, " +
                                "position TEXT NOT NULL, " +
                                "term_start TEXT NOT NULL, " +
                                "term_end TEXT NOT NULL, " +
                                "is_current INTEGER DEFAULT 1, " +
                                "created_at TEXT NOT NULL, " +
                                "updated_at TEXT NOT NULL, " +
                                "FOREIGN KEY (resident_id) REFERENCES residents(id))");

                stmt.execute(
                        "CREATE TABLE IF NOT EXISTS barangay_info (" +
                                "id TEXT PRIMARY KEY, " +
                                "barangay_name TEXT, " +
                                "city TEXT, " +
                                "province TEXT, " +
                                "region TEXT, " +
                                "address TEXT, " +
                                "contact_number TEXT, " +
                                "email TEXT, " +
                                "seal_path TEXT, " +
                                "updated_at TEXT)");

                ensureBarangayInfoSeeded(conn);

                // Create indexes for better performance
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_users_username ON users(username)");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_users_role ON users(role)");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_residents_name ON residents(last_name, first_name)");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_residents_active ON residents(is_active)");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_documents_resident ON documents(resident_id)");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_documents_type ON documents(type)");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_documents_request ON documents(request_id)");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_voter_apps_status ON voter_applications(status)");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_document_requests_status ON document_requests(status)");
                stmt.execute(
                        "CREATE INDEX IF NOT EXISTS idx_document_requests_resident ON document_requests(resident_id)");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    private static void ensureUsersTableWithoutContact(Connection conn) throws SQLException {
        if (!columnExists(conn, "users", "contact")) {
            return;
        }

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys=off;");
            stmt.execute("BEGIN TRANSACTION;");
            try {
                stmt.execute(
                        "CREATE TABLE users_migrate (" +
                                "id TEXT PRIMARY KEY, " +
                                "username TEXT UNIQUE NOT NULL, " +
                                "password_hash TEXT NOT NULL, " +
                                "role TEXT NOT NULL, " +
                                "linked_resident_id TEXT, " +
                                "is_active INTEGER DEFAULT 1, " +
                                "created_at TEXT NOT NULL, " +
                                "last_login_at TEXT, " +
                                "updated_at TEXT NOT NULL)");

                stmt.execute(
                        "INSERT INTO users_migrate (id, username, password_hash, role, " +
                                "linked_resident_id, is_active, created_at, last_login_at, updated_at) " +
                                "SELECT id, username, password_hash, role, linked_resident_id, " +
                                "is_active, created_at, last_login_at, updated_at FROM users");

                stmt.execute("DROP TABLE users;");
                stmt.execute("ALTER TABLE users_migrate RENAME TO users;");

                stmt.execute("COMMIT;");
            } catch (SQLException migrationEx) {
                stmt.execute("ROLLBACK;");
                throw migrationEx;
            } finally {
                stmt.execute("PRAGMA foreign_keys=on;");
            }
        }
    }

    private static void ensureUsersTableWithoutEmail(Connection conn) throws SQLException {
        if (!columnExists(conn, "users", "email")) {
            return;
        }

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys=off;");
            stmt.execute("BEGIN TRANSACTION;");
            try {
                stmt.execute(
                        "CREATE TABLE users_migrate_email (" +
                                "id TEXT PRIMARY KEY, " +
                                "username TEXT UNIQUE NOT NULL, " +
                                "password_hash TEXT NOT NULL, " +
                                "role TEXT NOT NULL, " +
                                "linked_resident_id TEXT, " +
                                "is_active INTEGER DEFAULT 1, " +
                                "created_at TEXT NOT NULL, " +
                                "last_login_at TEXT, " +
                                "updated_at TEXT NOT NULL)");

                stmt.execute(
                        "INSERT INTO users_migrate_email (id, username, password_hash, role, " +
                                "linked_resident_id, is_active, created_at, last_login_at, updated_at) " +
                                "SELECT id, username, password_hash, role, linked_resident_id, " +
                                "is_active, created_at, last_login_at, updated_at FROM users");

                stmt.execute("DROP TABLE users;");
                stmt.execute("ALTER TABLE users_migrate_email RENAME TO users;");

                stmt.execute("COMMIT;");
            } catch (SQLException migrationEx) {
                stmt.execute("ROLLBACK;");
                throw migrationEx;
            } finally {
                stmt.execute("PRAGMA foreign_keys=on;");
            }
        }
    }

    private static boolean columnExists(Connection conn, String tableName, String columnName) throws SQLException {
        String query = String.format("PRAGMA table_info(%s)", tableName);
        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                String name = rs.getString("name");
                if (name != null && name.equalsIgnoreCase(columnName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void ensureDocumentsRequestColumn(Statement stmt) throws SQLException {
        try {
            stmt.execute("ALTER TABLE documents ADD COLUMN request_id TEXT");
        } catch (SQLException e) {
            String message = e.getMessage();
            if (message == null || !message.toLowerCase().contains("duplicate column name")) {
                throw e;
            }
        }
    }

    private static void ensureBarangayInfoSeeded(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM barangay_info")) {
            if (rs.next() && rs.getInt(1) > 0) {
                return;
            }
        }

        BarangayInfo defaultInfo = DefaultBarangayInfo.getInfo();
        String insertSql = "INSERT INTO barangay_info " +
                "(id, barangay_name, city, province, region, address, contact_number, email, seal_path, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
            pstmt.setString(1, defaultInfo.getId());
            pstmt.setString(2, defaultInfo.getBarangayName());
            pstmt.setString(3, defaultInfo.getCity());
            pstmt.setString(4, defaultInfo.getProvince());
            pstmt.setString(5, defaultInfo.getRegion());
            pstmt.setString(6, defaultInfo.getAddress());
            pstmt.setString(7, defaultInfo.getContactNumber());
            pstmt.setString(8, defaultInfo.getEmail());
            pstmt.setString(9, defaultInfo.getSealPath());
            LocalDateTime updatedAt = defaultInfo.getUpdatedAt() != null
                    ? defaultInfo.getUpdatedAt()
                    : LocalDateTime.now();
            pstmt.setString(10, updatedAt.toString());
            pstmt.executeUpdate();
        }
    }

    /**
     * Close database connection
     */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
