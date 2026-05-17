package lk.nextexam.dao;

import jakarta.servlet.ServletContext;
import lk.nextexam.model.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Professional MySQL DAO for Student Management.
 *
 * MySQL table:
 * students
 *
 * Columns:
 * student_id, name, email, course, batch, contact, exam_status
 *
 * Responsible Member:
 * IT25103045 - De Silva H.L.D.C.P.C
 */
public class StudentDAO {

    public List<Student> getAllStudents(ServletContext context) {
        List<Student> students = new ArrayList<>();

        String sql = "SELECT student_id, name, email, course, batch, contact, exam_status " +
                "FROM students " +
                "ORDER BY student_id ASC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                students.add(mapResultSetToStudent(resultSet));
            }

        } catch (SQLException e) {
            System.out.println("STUDENTDAO ERROR -> getAllStudents failed");
            e.printStackTrace();
        }

        students.sort(Comparator.comparing(Student::getStudentId, String.CASE_INSENSITIVE_ORDER));
        return students;
    }

    public Student getStudentById(ServletContext context, String studentId) {
        String cleanStudentId = FileUtil.clean(studentId);

        if (cleanStudentId.isEmpty()) {
            return null;
        }

        String sql = "SELECT student_id, name, email, course, batch, contact, exam_status " +
                "FROM students " +
                "WHERE LOWER(TRIM(student_id)) = LOWER(TRIM(?)) " +
                "LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanStudentId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToStudent(resultSet);
                }
            }

        } catch (SQLException e) {
            System.out.println("STUDENTDAO ERROR -> getStudentById failed for " + cleanStudentId);
            e.printStackTrace();
        }

        return null;
    }

    public Student getStudentByEmail(ServletContext context, String email) {
        String cleanEmail = FileUtil.clean(email);

        if (cleanEmail.isEmpty()) {
            return null;
        }

        String sql = "SELECT student_id, name, email, course, batch, contact, exam_status " +
                "FROM students " +
                "WHERE LOWER(TRIM(email)) = LOWER(TRIM(?)) " +
                "LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanEmail);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToStudent(resultSet);
                }
            }

        } catch (SQLException e) {
            System.out.println("STUDENTDAO ERROR -> getStudentByEmail failed for " + cleanEmail);
            e.printStackTrace();
        }

        return null;
    }

    public List<Student> getStudentsByBatch(ServletContext context, String batch) {
        List<Student> selectedStudents = new ArrayList<>();
        String cleanBatch = normalizeBatchInput(batch);

        if (cleanBatch.isEmpty()) {
            return selectedStudents;
        }

        String sql = "SELECT student_id, name, email, course, batch, contact, exam_status " +
                "FROM students " +
                "WHERE LOWER(TRIM(batch)) = LOWER(TRIM(?)) " +
                "ORDER BY name ASC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanBatch);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    selectedStudents.add(mapResultSetToStudent(resultSet));
                }
            }

        } catch (SQLException e) {
            System.out.println("STUDENTDAO ERROR -> getStudentsByBatch failed for " + cleanBatch);
            e.printStackTrace();
        }

        selectedStudents.sort(Comparator.comparing(Student::getName, String.CASE_INSENSITIVE_ORDER));
        return selectedStudents;
    }

    public List<Student> getStudentsByStatus(ServletContext context, String examStatus) {
        List<Student> selectedStudents = new ArrayList<>();
        String cleanStatus = normalizeStatusInput(examStatus);

        if (cleanStatus.isEmpty()) {
            return selectedStudents;
        }

        String sql = "SELECT student_id, name, email, course, batch, contact, exam_status " +
                "FROM students " +
                "WHERE LOWER(TRIM(exam_status)) = LOWER(TRIM(?)) " +
                "ORDER BY name ASC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanStatus);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    selectedStudents.add(mapResultSetToStudent(resultSet));
                }
            }

        } catch (SQLException e) {
            System.out.println("STUDENTDAO ERROR -> getStudentsByStatus failed for " + cleanStatus);
            e.printStackTrace();
        }

        selectedStudents.sort(Comparator.comparing(Student::getName, String.CASE_INSENSITIVE_ORDER));
        return selectedStudents;
    }

    public List<Student> getEligibleStudents(ServletContext context) {
        return getStudentsByStatus(context, Student.STATUS_ELIGIBLE);
    }

    public List<Student> getPendingStudents(ServletContext context) {
        return getStudentsByStatus(context, Student.STATUS_PENDING);
    }

    public List<Student> getBlockedStudents(ServletContext context) {
        return getStudentsByStatus(context, Student.STATUS_BLOCKED);
    }

    public boolean addStudent(ServletContext context, Student student) {
        if (!isValidForCreate(context, student)) {
            return false;
        }

        String sql = "INSERT INTO students " +
                "(student_id, name, email, course, batch, contact, exam_status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            fillStudentStatement(statement, student);
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("STUDENTDAO ERROR -> addStudent failed");
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateStudent(ServletContext context, Student student) {
        if (!isValidForUpdate(context, student)) {
            return false;
        }

        String sql = "UPDATE students SET " +
                "name = ?, " +
                "email = ?, " +
                "course = ?, " +
                "batch = ?, " +
                "contact = ?, " +
                "exam_status = ? " +
                "WHERE student_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, student.getName());
            statement.setString(2, student.getEmail());
            statement.setString(3, student.getCourse());
            statement.setString(4, student.getBatch());
            statement.setString(5, student.getContact());
            statement.setString(6, student.getExamStatus());
            statement.setString(7, student.getStudentId());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("STUDENTDAO ERROR -> updateStudent failed for " +
                    (student != null ? student.getStudentId() : ""));
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteStudent(ServletContext context, String studentId) {
        String cleanStudentId = FileUtil.clean(studentId);

        if (cleanStudentId.isEmpty()) {
            return false;
        }

        if (!existsById(context, cleanStudentId)) {
            return false;
        }

        String sql = "DELETE FROM students WHERE LOWER(TRIM(student_id)) = LOWER(TRIM(?))";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanStudentId);
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("STUDENTDAO ERROR -> deleteStudent failed for " + cleanStudentId);
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateStudentStatus(ServletContext context, String studentId, String examStatus) {
        String cleanStudentId = FileUtil.clean(studentId);
        String cleanStatus = normalizeStatusInput(examStatus);

        if (cleanStudentId.isEmpty() || cleanStatus.isEmpty()) {
            return false;
        }

        Student existingStudent = getStudentById(context, cleanStudentId);

        if (existingStudent == null) {
            return false;
        }

        existingStudent.setExamStatus(cleanStatus);

        if (!existingStudent.isValidExamStatus()) {
            return false;
        }

        String sql = "UPDATE students SET exam_status = ? " +
                "WHERE LOWER(TRIM(student_id)) = LOWER(TRIM(?))";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanStatus);
            statement.setString(2, cleanStudentId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("STUDENTDAO ERROR -> updateStudentStatus failed for " + cleanStudentId);
            e.printStackTrace();
            return false;
        }
    }

    public boolean markEligible(ServletContext context, String studentId) {
        return updateStudentStatus(context, studentId, Student.STATUS_ELIGIBLE);
    }

    public boolean markPending(ServletContext context, String studentId) {
        return updateStudentStatus(context, studentId, Student.STATUS_PENDING);
    }

    public boolean markBlocked(ServletContext context, String studentId) {
        return updateStudentStatus(context, studentId, Student.STATUS_BLOCKED);
    }

    public boolean canStudentAttemptExam(ServletContext context, String studentId) {
        Student student = getStudentById(context, studentId);

        if (student == null) {
            return false;
        }

        return student.canAttemptExam();
    }

    public String getStudentAttemptValidationMessage(ServletContext context, String studentId) {
        Student student = getStudentById(context, studentId);

        if (student == null) {
            return "Student record not found.";
        }

        if (student.isBlocked()) {
            return "This student is blocked from attempting exams.";
        }

        if (student.isPending()) {
            return "This student is still pending eligibility review.";
        }

        if (!student.isEligible()) {
            return "This student is not eligible for online exams.";
        }

        return "OK";
    }

    public int countAllStudents(ServletContext context) {
        return countByQuery("SELECT COUNT(*) FROM students");
    }

    public int countEligibleStudents(ServletContext context) {
        return countByStatus(context, Student.STATUS_ELIGIBLE);
    }

    public int countPendingStudents(ServletContext context) {
        return countByStatus(context, Student.STATUS_PENDING);
    }

    public int countBlockedStudents(ServletContext context) {
        return countByStatus(context, Student.STATUS_BLOCKED);
    }

    public int countByBatch(ServletContext context, String batch) {
        String cleanBatch = normalizeBatchInput(batch);

        if (cleanBatch.isEmpty()) {
            return 0;
        }

        String sql = "SELECT COUNT(*) FROM students WHERE LOWER(TRIM(batch)) = LOWER(TRIM(?))";
        return countBySingleParameterQuery(sql, cleanBatch);
    }

    public int countYearOneStudents(ServletContext context) {
        return countByBatch(context, Student.BATCH_Y1S1)
                + countByBatch(context, Student.BATCH_Y1S2);
    }

    public int countYearTwoStudents(ServletContext context) {
        return countByBatch(context, Student.BATCH_Y2S1)
                + countByBatch(context, Student.BATCH_Y2S2);
    }

    public int countYearThreeStudents(ServletContext context) {
        return countByBatch(context, Student.BATCH_Y3S1)
                + countByBatch(context, Student.BATCH_Y3S2);
    }

    public int countYearFourStudents(ServletContext context) {
        return countByBatch(context, Student.BATCH_Y4S1)
                + countByBatch(context, Student.BATCH_Y4S2);
    }

    public int calculateEligibilityRate(ServletContext context) {
        int total = countAllStudents(context);

        if (total == 0) {
            return 0;
        }

        return (countEligibleStudents(context) * 100) / total;
    }

    public boolean existsById(ServletContext context, String studentId) {
        String cleanStudentId = FileUtil.clean(studentId);

        if (cleanStudentId.isEmpty()) {
            return false;
        }

        String sql = "SELECT student_id FROM students " +
                "WHERE LOWER(TRIM(student_id)) = LOWER(TRIM(?)) LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanStudentId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException e) {
            System.out.println("STUDENTDAO ERROR -> existsById failed for " + cleanStudentId);
            e.printStackTrace();
            return false;
        }
    }

    public boolean existsByEmail(ServletContext context, String email) {
        return getStudentByEmail(context, email) != null;
    }

    private boolean isValidForCreate(ServletContext context, Student student) {
        if (!isStudentObjectValid(student)) {
            return false;
        }

        if (existsById(context, student.getStudentId())) {
            return false;
        }

        return getStudentByEmail(context, student.getEmail()) == null;
    }

    private boolean isValidForUpdate(ServletContext context, Student student) {
        if (!isStudentObjectValid(student)) {
            return false;
        }

        Student existingStudent = getStudentById(context, student.getStudentId());

        if (existingStudent == null) {
            return false;
        }

        Student duplicateEmailStudent = getStudentByEmail(context, student.getEmail());

        if (duplicateEmailStudent != null
                && !duplicateEmailStudent.getStudentId().equalsIgnoreCase(student.getStudentId())) {
            return false;
        }

        return true;
    }

    private boolean isStudentObjectValid(Student student) {
        if (student == null) {
            return false;
        }

        return student.isCompleteForSave();
    }

    private int countByStatus(ServletContext context, String status) {
        String cleanStatus = normalizeStatusInput(status);

        if (cleanStatus.isEmpty()) {
            return 0;
        }

        String sql = "SELECT COUNT(*) FROM students WHERE LOWER(TRIM(exam_status)) = LOWER(TRIM(?))";
        return countBySingleParameterQuery(sql, cleanStatus);
    }

    private int countByQuery(String sql) {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

        } catch (SQLException e) {
            System.out.println("STUDENTDAO ERROR -> countByQuery failed");
            e.printStackTrace();
        }

        return 0;
    }

    private int countBySingleParameterQuery(String sql, String parameter) {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, parameter);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }

        } catch (SQLException e) {
            System.out.println("STUDENTDAO ERROR -> countBySingleParameterQuery failed");
            e.printStackTrace();
        }

        return 0;
    }

    private void fillStudentStatement(PreparedStatement statement, Student student) throws SQLException {
        statement.setString(1, student.getStudentId());
        statement.setString(2, student.getName());
        statement.setString(3, student.getEmail());
        statement.setString(4, student.getCourse());
        statement.setString(5, student.getBatch());
        statement.setString(6, student.getContact());
        statement.setString(7, student.getExamStatus());
    }

    private Student mapResultSetToStudent(ResultSet resultSet) throws SQLException {
        return new Student(
                safe(resultSet.getString("student_id")),
                safe(resultSet.getString("name")),
                safe(resultSet.getString("email")),
                safe(resultSet.getString("course")),
                normalizeBatchInput(resultSet.getString("batch")),
                safe(resultSet.getString("contact")),
                normalizeStatusInput(resultSet.getString("exam_status"))
        );
    }

    private String normalizeBatchInput(String value) {
        String batchValue = safe(value).toUpperCase();

        if (batchValue.equalsIgnoreCase(Student.BATCH_Y1S1)) {
            return Student.BATCH_Y1S1;
        }

        if (batchValue.equalsIgnoreCase(Student.BATCH_Y1S2)) {
            return Student.BATCH_Y1S2;
        }

        if (batchValue.equalsIgnoreCase(Student.BATCH_Y2S1)) {
            return Student.BATCH_Y2S1;
        }

        if (batchValue.equalsIgnoreCase(Student.BATCH_Y2S2)) {
            return Student.BATCH_Y2S2;
        }

        if (batchValue.equalsIgnoreCase(Student.BATCH_Y3S1)) {
            return Student.BATCH_Y3S1;
        }

        if (batchValue.equalsIgnoreCase(Student.BATCH_Y3S2)) {
            return Student.BATCH_Y3S2;
        }

        if (batchValue.equalsIgnoreCase(Student.BATCH_Y4S1)) {
            return Student.BATCH_Y4S1;
        }

        if (batchValue.equalsIgnoreCase(Student.BATCH_Y4S2)) {
            return Student.BATCH_Y4S2;
        }

        return batchValue;
    }

    private String normalizeStatusInput(String value) {
        String statusValue = safe(value);

        if (statusValue.equalsIgnoreCase(Student.STATUS_ELIGIBLE)) {
            return Student.STATUS_ELIGIBLE;
        }

        if (statusValue.equalsIgnoreCase(Student.STATUS_PENDING)) {
            return Student.STATUS_PENDING;
        }

        if (statusValue.equalsIgnoreCase(Student.STATUS_BLOCKED)) {
            return Student.STATUS_BLOCKED;
        }

        return statusValue;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}