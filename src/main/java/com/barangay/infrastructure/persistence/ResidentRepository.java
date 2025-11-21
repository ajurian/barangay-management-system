package com.barangay.infrastructure.persistence;

import com.barangay.domain.entities.*;
import com.barangay.domain.repositories.IResidentRepository;
import com.barangay.domain.valueobjects.Address;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * SQLite implementation of IResidentRepository.
 */
public class ResidentRepository implements IResidentRepository {

    @Override
    public void save(Resident resident) {
        String sql = "INSERT OR REPLACE INTO residents " +
                "(id, first_name, middle_name, last_name, suffix, birth_date, birth_place, " +
                "gender, civil_status, nationality, contact, house_number, street, purok, " +
                "barangay, city, province, occupation, employment, income_bracket, " +
                "education_level, is_voter, is_active, deactivation_reason, " +
                "registered_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, resident.getId().getValue());
            pstmt.setString(2, resident.getFirstName());
            pstmt.setString(3, resident.getMiddleName());
            pstmt.setString(4, resident.getLastName());
            pstmt.setString(5, resident.getSuffix());
            pstmt.setString(6, resident.getBirthDate().toString());
            pstmt.setString(7, resident.getBirthPlace());
            pstmt.setString(8, resident.getGender().name());
            pstmt.setString(9, resident.getCivilStatus() != null ? resident.getCivilStatus().name() : null);
            pstmt.setString(10, resident.getNationality());
            pstmt.setString(11, resident.getContact());

            Address address = resident.getAddress();
            if (address != null) {
                pstmt.setString(12, address.getHouseNumber());
                pstmt.setString(13, address.getStreet());
                pstmt.setString(14, address.getPurok());
                pstmt.setString(15, address.getBarangay());
                pstmt.setString(16, address.getCity());
                pstmt.setString(17, address.getProvince());
            } else {
                pstmt.setString(12, null);
                pstmt.setString(13, null);
                pstmt.setString(14, null);
                pstmt.setString(15, null);
                pstmt.setString(16, null);
                pstmt.setString(17, null);
            }

            pstmt.setString(18, resident.getOccupation());
            pstmt.setString(19, resident.getEmployment());
            pstmt.setString(20, resident.getIncomeBracket() != null ? resident.getIncomeBracket().name() : null);
            pstmt.setString(21, resident.getEducationLevel() != null ? resident.getEducationLevel().name() : null);
            pstmt.setInt(22, resident.isVoter() ? 1 : 0);
            pstmt.setInt(23, resident.isActive() ? 1 : 0);
            pstmt.setString(24, resident.getDeactivationReason());
            pstmt.setString(25, resident.getRegisteredAt().toString());
            pstmt.setString(26, resident.getUpdatedAt().toString());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save resident", e);
        }
    }

    @Override
    public Optional<Resident> findById(ResidentId id) {
        String sql = "SELECT * FROM residents WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id.getValue());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToResident(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find resident by ID", e);
        }

        return Optional.empty();
    }

    @Override
    public List<Resident> findAll() {
        String sql = "SELECT * FROM residents ORDER BY last_name, first_name";
        return executeQuery(sql);
    }

    @Override
    public List<Resident> findActiveResidents() {
        String sql = "SELECT * FROM residents WHERE is_active = 1 ORDER BY last_name, first_name";
        return executeQuery(sql);
    }

    @Override
    public List<Resident> searchByName(String name) {
        String sql = "SELECT * FROM residents WHERE " +
                "(first_name LIKE ? OR middle_name LIKE ? OR last_name LIKE ?) " +
                "AND is_active = 1 ORDER BY last_name, first_name";

        List<Resident> residents = new ArrayList<>();
        String searchPattern = "%" + name + "%";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                residents.add(mapResultSetToResident(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to search residents by name", e);
        }

        return residents;
    }

    @Override
    public List<Resident> findByGender(Gender gender) {
        String sql = "SELECT * FROM residents WHERE gender = ? AND is_active = 1 " +
                "ORDER BY last_name, first_name";

        List<Resident> residents = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, gender.name());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                residents.add(mapResultSetToResident(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find residents by gender", e);
        }

        return residents;
    }

    @Override
    public List<Resident> findByAgeRange(int minAge, int maxAge) {
        // Calculate birth date range
        LocalDate maxBirthDate = LocalDate.now().minusYears(minAge);
        LocalDate minBirthDate = LocalDate.now().minusYears(maxAge + 1);

        String sql = "SELECT * FROM residents WHERE birth_date >= ? AND birth_date <= ? " +
                "AND is_active = 1 ORDER BY last_name, first_name";

        List<Resident> residents = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, minBirthDate.toString());
            pstmt.setString(2, maxBirthDate.toString());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                residents.add(mapResultSetToResident(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find residents by age range", e);
        }

        return residents;
    }

    @Override
    public List<Resident> findPotentialDuplicates(String firstName, String lastName, LocalDate birthDate) {
        String sql = "SELECT * FROM residents WHERE first_name = ? AND last_name = ? AND birth_date = ?";

        List<Resident> residents = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            pstmt.setString(3, birthDate.toString());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                residents.add(mapResultSetToResident(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find potential duplicates", e);
        }

        return residents;
    }

    @Override
    public int countTotal() {
        return executeCount("SELECT COUNT(*) FROM residents WHERE is_active = 1");
    }

    @Override
    public int countByGender(Gender gender) {
        String sql = "SELECT COUNT(*) FROM residents WHERE gender = ? AND is_active = 1";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, gender.name());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to count residents by gender", e);
        }

        return 0;
    }

    @Override
    public int countVoters() {
        return executeCount("SELECT COUNT(*) FROM residents WHERE is_voter = 1 AND is_active = 1");
    }

    @Override
    public ResidentId generateNextId() {
        String year = String.valueOf(LocalDate.now().getYear());
        String sql = "SELECT MAX(CAST(SUBSTR(id, LENGTH(id) - 9, 10) AS INTEGER)) FROM residents WHERE id LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "BR-" + year + "-%");
            ResultSet rs = pstmt.executeQuery();

            int nextSequence = 1;
            if (rs.next()) {
                nextSequence = rs.getInt(1) + 1;
            }

            String id = String.format("BR-%s-%010d", year, nextSequence);
            return ResidentId.fromString(id);

        } catch (SQLException e) {
            throw new RuntimeException("Failed to generate resident ID", e);
        }
    }

    @Override
    public List<Resident> findWithPagination(int offset, int limit) {
        String sql = "SELECT * FROM residents WHERE is_active = 1 " +
                "ORDER BY last_name, first_name LIMIT ? OFFSET ?";

        List<Resident> residents = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);
            pstmt.setInt(2, offset);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                residents.add(mapResultSetToResident(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find residents with pagination", e);
        }

        return residents;
    }

    private List<Resident> executeQuery(String sql) {
        List<Resident> residents = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                residents.add(mapResultSetToResident(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to execute query", e);
        }

        return residents;
    }

    private int executeCount(String sql) {
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to execute count", e);
        }

        return 0;
    }

    private Resident mapResultSetToResident(ResultSet rs) throws SQLException {
        ResidentId id = ResidentId.fromString(rs.getString("id"));
        String firstName = rs.getString("first_name");
        String lastName = rs.getString("last_name");
        LocalDate birthDate = LocalDate.parse(rs.getString("birth_date"));
        Gender gender = Gender.valueOf(rs.getString("gender"));

        Resident resident = new Resident(id, firstName, lastName, birthDate, gender);

        resident.setMiddleName(rs.getString("middle_name"));
        resident.setSuffix(rs.getString("suffix"));
        resident.setBirthPlace(rs.getString("birth_place"));

        String civilStatusStr = rs.getString("civil_status");
        if (civilStatusStr != null && !civilStatusStr.isEmpty()) {
            resident.setCivilStatus(CivilStatus.valueOf(civilStatusStr));
        }

        resident.setNationality(rs.getString("nationality"));
        resident.setContact(rs.getString("contact"));

        // Build address
        String houseNumber = rs.getString("house_number");
        String street = rs.getString("street");
        String purok = rs.getString("purok");
        String barangay = rs.getString("barangay");
        String city = rs.getString("city");
        String province = rs.getString("province");

        if (barangay != null || city != null || province != null) {
            Address address = new Address(houseNumber, street, purok, barangay, city, province);
            resident.setAddress(address);
        }

        resident.setOccupation(rs.getString("occupation"));
        resident.setEmployment(rs.getString("employment"));

        String incomeBracketStr = rs.getString("income_bracket");
        if (incomeBracketStr != null && !incomeBracketStr.isEmpty()) {
            resident.setIncomeBracket(IncomeBracket.valueOf(incomeBracketStr));
        }

        String educationLevelStr = rs.getString("education_level");
        if (educationLevelStr != null && !educationLevelStr.isEmpty()) {
            resident.setEducationLevel(EducationLevel.valueOf(educationLevelStr));
        }

        resident.setVoter(rs.getInt("is_voter") == 1);

        boolean isActive = rs.getInt("is_active") == 1;
        if (!isActive) {
            resident.deactivate(rs.getString("deactivation_reason"));
        }

        return resident;
    }
}
