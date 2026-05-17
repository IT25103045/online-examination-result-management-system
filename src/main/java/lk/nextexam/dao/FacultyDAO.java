package lk.nextexam.dao;

import jakarta.servlet.ServletContext;
import lk.nextexam.model.Faculty;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * FacultyDAO manages MySQL faculty records.
 *
 * MySQL table:
 * faculties
 *
 * Columns:
 * faculty_id, faculty_name, dean_name, contact_email, status
 */
public class FacultyDAO {

    public List<Faculty> getAllFaculties(ServletContext context) {
        List<Faculty> faculties = new ArrayList<>();

        String sql = "SELECT faculty_id, faculty_name, dean_name, contact_email, status " +
                "FROM faculties " +
                "ORDER BY faculty_id ASC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                faculties.add(mapResultSetToFaculty(resultSet));
            }

        } catch (SQLException e) {
            System.out.println("FACULTYDAO ERROR -> getAllFaculties failed");
            e.printStackTrace();
        }

        faculties.sort(Comparator.comparing(Faculty::getFacultyId, String.CASE_INSENSITIVE_ORDER));
        return faculties;
    }

    public Faculty getFacultyById(ServletContext context, String facultyId) {
        String cleanFacultyId = FileUtil.clean(facultyId);

        if (cleanFacultyId.isEmpty()) {
            return null;
        }

        String sql = "SELECT faculty_id, faculty_name, dean_name, contact_email, status " +
                "FROM faculties " +
                "WHERE LOWER(TRIM(faculty_id)) = LOWER(TRIM(?)) " +
                "LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanFacultyId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToFaculty(resultSet);
                }
            }

        } catch (SQLException e) {
            System.out.println("FACULTYDAO ERROR -> getFacultyById failed for " + cleanFacultyId);
            e.printStackTrace();
        }

        return null;
    }

    public boolean addFaculty(ServletContext context, Faculty faculty) {
        if (!isValidForSave(faculty)) {
            return false;
        }

        if (existsById(faculty.getFacultyId())) {
            return false;
        }

        String sql = "INSERT INTO faculties " +
                "(faculty_id, faculty_name, dean_name, contact_email, status) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            fillFacultyStatement(statement, faculty);
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("FACULTYDAO ERROR -> addFaculty failed");
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateFaculty(ServletContext context, Faculty faculty) {
        if (!isValidForSave(faculty)) {
            return false;
        }

        if (!existsById(faculty.getFacultyId())) {
            return false;
        }

        String sql = "UPDATE faculties SET " +
                "faculty_name = ?, " +
                "dean_name = ?, " +
                "contact_email = ?, " +
                "status = ? " +
                "WHERE LOWER(TRIM(faculty_id)) = LOWER(TRIM(?))";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, faculty.getFacultyName());
            statement.setString(2, faculty.getDeanName());
            statement.setString(3, faculty.getContactEmail());
            statement.setString(4, normalizeStatus(faculty.getStatus()));
            statement.setString(5, faculty.getFacultyId());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("FACULTYDAO ERROR -> updateFaculty failed for " +
                    (faculty != null ? faculty.getFacultyId() : ""));
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteFaculty(ServletContext context, String facultyId) {
        String cleanFacultyId = FileUtil.clean(facultyId);

        if (cleanFacultyId.isEmpty()) {
            return false;
        }

        String sql = "DELETE FROM faculties " +
                "WHERE LOWER(TRIM(faculty_id)) = LOWER(TRIM(?))";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanFacultyId);
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("FACULTYDAO ERROR -> deleteFaculty failed for " + cleanFacultyId);
            e.printStackTrace();
            return false;
        }
    }

    public int countAllFaculties(ServletContext context) {
        String sql = "SELECT COUNT(*) FROM faculties";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

        } catch (SQLException e) {
            System.out.println("FACULTYDAO ERROR -> countAllFaculties failed");
            e.printStackTrace();
        }

        return 0;
    }

    public boolean existsById(String facultyId) {
        String cleanFacultyId = FileUtil.clean(facultyId);

        if (cleanFacultyId.isEmpty()) {
            return false;
        }

        String sql = "SELECT faculty_id FROM faculties " +
                "WHERE LOWER(TRIM(faculty_id)) = LOWER(TRIM(?)) " +
                "LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanFacultyId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException e) {
            System.out.println("FACULTYDAO ERROR -> existsById failed for " + cleanFacultyId);
            e.printStackTrace();
            return false;
        }
    }

    private void fillFacultyStatement(PreparedStatement statement, Faculty faculty) throws SQLException {
        statement.setString(1, faculty.getFacultyId());
        statement.setString(2, faculty.getFacultyName());
        statement.setString(3, faculty.getDeanName());
        statement.setString(4, faculty.getContactEmail());
        statement.setString(5, normalizeStatus(faculty.getStatus()));
    }

    private Faculty mapResultSetToFaculty(ResultSet resultSet) throws SQLException {
        return new Faculty(
                safe(resultSet.getString("faculty_id")),
                safe(resultSet.getString("faculty_name")),
                safe(resultSet.getString("dean_name")),
                safe(resultSet.getString("contact_email")),
                normalizeStatus(resultSet.getString("status"))
        );
    }

    private boolean isValidForSave(Faculty faculty) {
        return faculty != null
                && !FileUtil.clean(faculty.getFacultyId()).isEmpty()
                && !FileUtil.clean(faculty.getFacultyName()).isEmpty()
                && !FileUtil.clean(faculty.getDeanName()).isEmpty()
                && isValidEmail(faculty.getContactEmail())
                && !FileUtil.clean(faculty.getStatus()).isEmpty();
    }

    private boolean isValidEmail(String email) {
        String cleanEmail = FileUtil.clean(email);
        return cleanEmail.contains("@")
                && cleanEmail.contains(".")
                && cleanEmail.length() >= 6
                && !cleanEmail.contains(" ");
    }

    private String normalizeStatus(String value) {
        String status = safe(value);

        if (status.equalsIgnoreCase("Active")) {
            return "Active";
        }

        if (status.equalsIgnoreCase("Inactive")) {
            return "Inactive";
        }

        if (status.equalsIgnoreCase("Archived")) {
            return "Archived";
        }

        return status.isEmpty() ? "Active" : status;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}