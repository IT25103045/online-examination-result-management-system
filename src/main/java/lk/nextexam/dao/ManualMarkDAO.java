package lk.nextexam.dao;

import jakarta.servlet.ServletContext;
import lk.nextexam.model.ManualMark;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * ManualMarkDAO manages MySQL essay/manual marking records.
 *
 * MySQL table:
 * manual_marks
 *
 * Columns:
 * mark_id, submission_id, exam_id, student_id, question_id,
 * marks_awarded, feedback, marked_by, marked_at
 *
 * Responsible Member:
 * IT25103045 - De Silva H.L.D.C.P.C
 */
public class ManualMarkDAO {

    private static final DateTimeFormatter STORAGE_DATE_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public List<ManualMark> getAllMarks(ServletContext context) {
        List<ManualMark> marks = new ArrayList<>();

        String sql = "SELECT mark_id, submission_id, exam_id, student_id, question_id, " +
                "marks_awarded, feedback, marked_by, marked_at " +
                "FROM manual_marks " +
                "ORDER BY marked_at DESC, mark_id DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                marks.add(mapResultSetToManualMark(resultSet));
            }

        } catch (SQLException e) {
            System.out.println("MANUALMARKDAO ERROR -> getAllMarks failed");
            e.printStackTrace();
        }

        return marks;
    }

    public ManualMark getMarkById(ServletContext context, String markId) {
        String cleanMarkId = FileUtil.clean(markId);

        if (cleanMarkId.isEmpty()) {
            return null;
        }

        String sql = "SELECT mark_id, submission_id, exam_id, student_id, question_id, " +
                "marks_awarded, feedback, marked_by, marked_at " +
                "FROM manual_marks " +
                "WHERE LOWER(TRIM(mark_id)) = LOWER(TRIM(?)) " +
                "LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanMarkId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToManualMark(resultSet);
                }
            }

        } catch (SQLException e) {
            System.out.println("MANUALMARKDAO ERROR -> getMarkById failed for " + cleanMarkId);
            e.printStackTrace();
        }

        return null;
    }

    public List<ManualMark> getMarksBySubmission(ServletContext context, String submissionId) {
        List<ManualMark> marks = new ArrayList<>();
        String cleanSubmissionId = FileUtil.clean(submissionId);

        if (cleanSubmissionId.isEmpty()) {
            return marks;
        }

        String sql = "SELECT mark_id, submission_id, exam_id, student_id, question_id, " +
                "marks_awarded, feedback, marked_by, marked_at " +
                "FROM manual_marks " +
                "WHERE LOWER(TRIM(submission_id)) = LOWER(TRIM(?)) " +
                "ORDER BY question_id ASC, marked_at DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanSubmissionId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    marks.add(mapResultSetToManualMark(resultSet));
                }
            }

        } catch (SQLException e) {
            System.out.println("MANUALMARKDAO ERROR -> getMarksBySubmission failed for " + cleanSubmissionId);
            e.printStackTrace();
        }

        return marks;
    }

    public ManualMark getMarkBySubmissionAndQuestion(ServletContext context,
                                                     String submissionId,
                                                     String questionId) {
        String cleanSubmissionId = FileUtil.clean(submissionId);
        String cleanQuestionId = FileUtil.clean(questionId);

        if (cleanSubmissionId.isEmpty() || cleanQuestionId.isEmpty()) {
            return null;
        }

        String sql = "SELECT mark_id, submission_id, exam_id, student_id, question_id, " +
                "marks_awarded, feedback, marked_by, marked_at " +
                "FROM manual_marks " +
                "WHERE LOWER(TRIM(submission_id)) = LOWER(TRIM(?)) " +
                "AND LOWER(TRIM(question_id)) = LOWER(TRIM(?)) " +
                "LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanSubmissionId);
            statement.setString(2, cleanQuestionId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToManualMark(resultSet);
                }
            }

        } catch (SQLException e) {
            System.out.println("MANUALMARKDAO ERROR -> getMarkBySubmissionAndQuestion failed");
            e.printStackTrace();
        }

        return null;
    }

    public boolean saveOrUpdateMark(ServletContext context, ManualMark mark) {
        if (mark == null) {
            return false;
        }

        if (mark.getMarkedAt().isEmpty()) {
            mark.setMarkedAt(now());
        }

        ManualMark existingMark = getMarkBySubmissionAndQuestion(
                context,
                mark.getSubmissionId(),
                mark.getQuestionId()
        );

        if (existingMark != null) {
            mark.setMarkId(existingMark.getMarkId());
            return updateMark(context, mark);
        }

        return addMark(context, mark);
    }

    public boolean addMark(ServletContext context, ManualMark mark) {
        if (mark == null) {
            return false;
        }

        if (mark.getMarkId().isEmpty()) {
            mark.setMarkId(FileUtil.generateId("MM"));
        }

        if (mark.getMarkedAt().isEmpty()) {
            mark.setMarkedAt(now());
        }

        if (!isValidForSave(mark)) {
            return false;
        }

        if (existsById(mark.getMarkId())) {
            mark.setMarkId(FileUtil.generateId("MM"));
        }

        String sql = "INSERT INTO manual_marks " +
                "(mark_id, submission_id, exam_id, student_id, question_id, marks_awarded, feedback, marked_by, marked_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            fillManualMarkStatement(statement, mark);
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("MANUALMARKDAO ERROR -> addMark failed");
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateMark(ServletContext context, ManualMark mark) {
        if (!isValidForSave(mark)) {
            return false;
        }

        String sql = "UPDATE manual_marks SET " +
                "submission_id = ?, " +
                "exam_id = ?, " +
                "student_id = ?, " +
                "question_id = ?, " +
                "marks_awarded = ?, " +
                "feedback = ?, " +
                "marked_by = ?, " +
                "marked_at = ? " +
                "WHERE LOWER(TRIM(mark_id)) = LOWER(TRIM(?))";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, mark.getSubmissionId());
            statement.setString(2, mark.getExamId());
            statement.setString(3, mark.getStudentId());
            statement.setString(4, mark.getQuestionId());
            statement.setDouble(5, mark.getMarksAwardedAsDouble());
            statement.setString(6, mark.getFeedback());
            statement.setString(7, mark.getMarkedBy());
            statement.setTimestamp(8, toTimestamp(mark.getMarkedAt()));
            statement.setString(9, mark.getMarkId());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("MANUALMARKDAO ERROR -> updateMark failed for " +
                    (mark != null ? mark.getMarkId() : ""));
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteMark(ServletContext context, String markId) {
        String cleanMarkId = FileUtil.clean(markId);

        if (cleanMarkId.isEmpty()) {
            return false;
        }

        String sql = "DELETE FROM manual_marks WHERE LOWER(TRIM(mark_id)) = LOWER(TRIM(?))";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanMarkId);
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("MANUALMARKDAO ERROR -> deleteMark failed for " + cleanMarkId);
            e.printStackTrace();
            return false;
        }
    }

    public double getTotalAwardedMarksBySubmission(ServletContext context, String submissionId) {
        String cleanSubmissionId = FileUtil.clean(submissionId);

        if (cleanSubmissionId.isEmpty()) {
            return 0.0;
        }

        String sql = "SELECT COALESCE(SUM(marks_awarded), 0) AS total_awarded " +
                "FROM manual_marks " +
                "WHERE LOWER(TRIM(submission_id)) = LOWER(TRIM(?))";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanSubmissionId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getDouble("total_awarded");
                }
            }

        } catch (SQLException e) {
            System.out.println("MANUALMARKDAO ERROR -> getTotalAwardedMarksBySubmission failed");
            e.printStackTrace();
        }

        return 0.0;
    }

    public int countMarksBySubmission(ServletContext context, String submissionId) {
        String cleanSubmissionId = FileUtil.clean(submissionId);

        if (cleanSubmissionId.isEmpty()) {
            return 0;
        }

        String sql = "SELECT COUNT(*) FROM manual_marks " +
                "WHERE LOWER(TRIM(submission_id)) = LOWER(TRIM(?))";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanSubmissionId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }

        } catch (SQLException e) {
            System.out.println("MANUALMARKDAO ERROR -> countMarksBySubmission failed");
            e.printStackTrace();
        }

        return 0;
    }

    public String now() {
        return LocalDateTime.now().format(STORAGE_DATE_TIME);
    }

    private boolean isValidForSave(ManualMark mark) {
        return mark != null && mark.isCompleteForSave();
    }

    private boolean existsById(String markId) {
        String cleanMarkId = FileUtil.clean(markId);

        if (cleanMarkId.isEmpty()) {
            return false;
        }

        String sql = "SELECT mark_id FROM manual_marks " +
                "WHERE LOWER(TRIM(mark_id)) = LOWER(TRIM(?)) " +
                "LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanMarkId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException e) {
            System.out.println("MANUALMARKDAO ERROR -> existsById failed for " + cleanMarkId);
            e.printStackTrace();
            return false;
        }
    }

    private void fillManualMarkStatement(PreparedStatement statement,
                                         ManualMark mark) throws SQLException {
        statement.setString(1, mark.getMarkId());
        statement.setString(2, mark.getSubmissionId());
        statement.setString(3, mark.getExamId());
        statement.setString(4, mark.getStudentId());
        statement.setString(5, mark.getQuestionId());
        statement.setDouble(6, mark.getMarksAwardedAsDouble());
        statement.setString(7, mark.getFeedback());
        statement.setString(8, mark.getMarkedBy());
        statement.setTimestamp(9, toTimestamp(mark.getMarkedAt()));
    }

    private ManualMark mapResultSetToManualMark(ResultSet resultSet) throws SQLException {
        return new ManualMark(
                safe(resultSet.getString("mark_id")),
                safe(resultSet.getString("submission_id")),
                safe(resultSet.getString("exam_id")),
                safe(resultSet.getString("student_id")),
                safe(resultSet.getString("question_id")),
                formatNumber(resultSet.getDouble("marks_awarded")),
                safe(resultSet.getString("feedback")),
                safe(resultSet.getString("marked_by")),
                fromTimestamp(resultSet.getTimestamp("marked_at"))
        );
    }

    private Timestamp toTimestamp(String value) {
        LocalDateTime dateTime = parseDateTime(value);

        if (dateTime == null) {
            dateTime = LocalDateTime.now();
        }

        return Timestamp.valueOf(dateTime);
    }

    private String fromTimestamp(Timestamp timestamp) {
        if (timestamp == null) {
            return "";
        }

        return timestamp.toLocalDateTime().format(STORAGE_DATE_TIME);
    }

    private LocalDateTime parseDateTime(String value) {
        String cleanValue = FileUtil.clean(value);

        if (cleanValue.isEmpty()) {
            return null;
        }

        try {
            return LocalDateTime.parse(cleanValue, STORAGE_DATE_TIME);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private String formatNumber(double value) {
        if (value == Math.floor(value)) {
            return String.valueOf((int) value);
        }

        return String.format("%.2f", value);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}