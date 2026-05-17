package lk.nextexam.dao;

import jakarta.servlet.ServletContext;
import lk.nextexam.model.ExamIntegrityLog;

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
 * ExamIntegrityLogDAO manages MySQL exam integrity log records.
 *
 * MySQL table:
 * exam_integrity_logs
 *
 * Columns:
 * log_id, student_id, exam_id, event_type, description, created_at
 *
 * Responsible Member:
 * IT25103045 - De Silva H.L.D.C.P.C
 */
public class ExamIntegrityLogDAO {

    private static final DateTimeFormatter STORAGE_DATE_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public boolean addLog(ServletContext context,
                          String studentId,
                          String examId,
                          String eventType,
                          String description) {

        ExamIntegrityLog log = new ExamIntegrityLog(
                FileUtil.generateId("INT"),
                FileUtil.clean(studentId),
                FileUtil.clean(examId),
                normalizeEventTypeInput(eventType),
                FileUtil.clean(description),
                now()
        );

        return addLog(context, log);
    }

    public boolean addLog(ServletContext context, ExamIntegrityLog log) {
        if (log == null) {
            return false;
        }

        if (log.getLogId().isEmpty()) {
            log.setLogId(FileUtil.generateId("INT"));
        }

        if (log.getCreatedAt().isEmpty()) {
            log.setCreatedAt(now());
        }

        if (!log.isCompleteForSave()) {
            return false;
        }

        if (existsById(log.getLogId())) {
            log.setLogId(FileUtil.generateId("INT"));
        }

        String sql = "INSERT INTO exam_integrity_logs " +
                "(log_id, student_id, exam_id, event_type, description, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            fillIntegrityLogStatement(statement, log);
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("EXAMINTEGRITYLOGDAO ERROR -> addLog failed");
            e.printStackTrace();
            return false;
        }
    }

    public List<ExamIntegrityLog> getAllLogs(ServletContext context) {
        List<ExamIntegrityLog> logs = new ArrayList<>();

        String sql = "SELECT log_id, student_id, exam_id, event_type, description, created_at " +
                "FROM exam_integrity_logs " +
                "ORDER BY created_at DESC, log_id DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                logs.add(mapResultSetToIntegrityLog(resultSet));
            }

        } catch (SQLException e) {
            System.out.println("EXAMINTEGRITYLOGDAO ERROR -> getAllLogs failed");
            e.printStackTrace();
        }

        return logs;
    }

    public ExamIntegrityLog getLogById(ServletContext context, String logId) {
        String cleanLogId = FileUtil.clean(logId);

        if (cleanLogId.isEmpty()) {
            return null;
        }

        String sql = "SELECT log_id, student_id, exam_id, event_type, description, created_at " +
                "FROM exam_integrity_logs " +
                "WHERE LOWER(TRIM(log_id)) = LOWER(TRIM(?)) " +
                "LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanLogId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToIntegrityLog(resultSet);
                }
            }

        } catch (SQLException e) {
            System.out.println("EXAMINTEGRITYLOGDAO ERROR -> getLogById failed for " + cleanLogId);
            e.printStackTrace();
        }

        return null;
    }

    public List<ExamIntegrityLog> getLogsByStudentAndExam(ServletContext context,
                                                          String studentId,
                                                          String examId) {
        List<ExamIntegrityLog> selectedLogs = new ArrayList<>();

        String cleanStudentId = FileUtil.clean(studentId);
        String cleanExamId = FileUtil.clean(examId);

        if (cleanStudentId.isEmpty() || cleanExamId.isEmpty()) {
            return selectedLogs;
        }

        String sql = "SELECT log_id, student_id, exam_id, event_type, description, created_at " +
                "FROM exam_integrity_logs " +
                "WHERE LOWER(TRIM(student_id)) = LOWER(TRIM(?)) " +
                "AND LOWER(TRIM(exam_id)) = LOWER(TRIM(?)) " +
                "ORDER BY created_at DESC, log_id DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanStudentId);
            statement.setString(2, cleanExamId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    selectedLogs.add(mapResultSetToIntegrityLog(resultSet));
                }
            }

        } catch (SQLException e) {
            System.out.println("EXAMINTEGRITYLOGDAO ERROR -> getLogsByStudentAndExam failed");
            e.printStackTrace();
        }

        return selectedLogs;
    }

    public List<ExamIntegrityLog> getLogsByStudent(ServletContext context, String studentId) {
        List<ExamIntegrityLog> selectedLogs = new ArrayList<>();
        String cleanStudentId = FileUtil.clean(studentId);

        if (cleanStudentId.isEmpty()) {
            return selectedLogs;
        }

        String sql = "SELECT log_id, student_id, exam_id, event_type, description, created_at " +
                "FROM exam_integrity_logs " +
                "WHERE LOWER(TRIM(student_id)) = LOWER(TRIM(?)) " +
                "ORDER BY created_at DESC, log_id DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanStudentId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    selectedLogs.add(mapResultSetToIntegrityLog(resultSet));
                }
            }

        } catch (SQLException e) {
            System.out.println("EXAMINTEGRITYLOGDAO ERROR -> getLogsByStudent failed for " + cleanStudentId);
            e.printStackTrace();
        }

        return selectedLogs;
    }

    public List<ExamIntegrityLog> getLogsByExam(ServletContext context, String examId) {
        List<ExamIntegrityLog> selectedLogs = new ArrayList<>();
        String cleanExamId = FileUtil.clean(examId);

        if (cleanExamId.isEmpty()) {
            return selectedLogs;
        }

        String sql = "SELECT log_id, student_id, exam_id, event_type, description, created_at " +
                "FROM exam_integrity_logs " +
                "WHERE LOWER(TRIM(exam_id)) = LOWER(TRIM(?)) " +
                "ORDER BY created_at DESC, log_id DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanExamId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    selectedLogs.add(mapResultSetToIntegrityLog(resultSet));
                }
            }

        } catch (SQLException e) {
            System.out.println("EXAMINTEGRITYLOGDAO ERROR -> getLogsByExam failed for " + cleanExamId);
            e.printStackTrace();
        }

        return selectedLogs;
    }

    public List<ExamIntegrityLog> getLogsByEventType(ServletContext context, String eventType) {
        List<ExamIntegrityLog> selectedLogs = new ArrayList<>();
        String cleanEventType = normalizeEventTypeInput(eventType);

        if (cleanEventType.isEmpty()) {
            return selectedLogs;
        }

        String sql = "SELECT log_id, student_id, exam_id, event_type, description, created_at " +
                "FROM exam_integrity_logs " +
                "WHERE LOWER(TRIM(event_type)) = LOWER(TRIM(?)) " +
                "ORDER BY created_at DESC, log_id DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanEventType);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    selectedLogs.add(mapResultSetToIntegrityLog(resultSet));
                }
            }

        } catch (SQLException e) {
            System.out.println("EXAMINTEGRITYLOGDAO ERROR -> getLogsByEventType failed for " + cleanEventType);
            e.printStackTrace();
        }

        return selectedLogs;
    }

    public int countLogsByStudentAndExam(ServletContext context,
                                         String studentId,
                                         String examId) {
        String cleanStudentId = FileUtil.clean(studentId);
        String cleanExamId = FileUtil.clean(examId);

        if (cleanStudentId.isEmpty() || cleanExamId.isEmpty()) {
            return 0;
        }

        String sql = "SELECT COUNT(*) FROM exam_integrity_logs " +
                "WHERE LOWER(TRIM(student_id)) = LOWER(TRIM(?)) " +
                "AND LOWER(TRIM(exam_id)) = LOWER(TRIM(?))";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanStudentId);
            statement.setString(2, cleanExamId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }

        } catch (SQLException e) {
            System.out.println("EXAMINTEGRITYLOGDAO ERROR -> countLogsByStudentAndExam failed");
            e.printStackTrace();
        }

        return 0;
    }

    public int countAllLogs(ServletContext context) {
        return countByQuery("SELECT COUNT(*) FROM exam_integrity_logs");
    }

    public int countLogsByEventType(ServletContext context, String eventType) {
        String cleanEventType = normalizeEventTypeInput(eventType);

        if (cleanEventType.isEmpty()) {
            return 0;
        }

        String sql = "SELECT COUNT(*) FROM exam_integrity_logs " +
                "WHERE LOWER(TRIM(event_type)) = LOWER(TRIM(?))";

        return countBySingleParameterQuery(sql, cleanEventType);
    }

    public boolean deleteLog(ServletContext context, String logId) {
        String cleanLogId = FileUtil.clean(logId);

        if (cleanLogId.isEmpty()) {
            return false;
        }

        String sql = "DELETE FROM exam_integrity_logs " +
                "WHERE LOWER(TRIM(log_id)) = LOWER(TRIM(?))";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanLogId);
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("EXAMINTEGRITYLOGDAO ERROR -> deleteLog failed for " + cleanLogId);
            e.printStackTrace();
            return false;
        }
    }

    public String now() {
        return LocalDateTime.now().format(STORAGE_DATE_TIME);
    }

    private int countByQuery(String sql) {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

        } catch (SQLException e) {
            System.out.println("EXAMINTEGRITYLOGDAO ERROR -> countByQuery failed");
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
            System.out.println("EXAMINTEGRITYLOGDAO ERROR -> countBySingleParameterQuery failed");
            e.printStackTrace();
        }

        return 0;
    }

    private void fillIntegrityLogStatement(PreparedStatement statement,
                                           ExamIntegrityLog log) throws SQLException {
        statement.setString(1, log.getLogId());
        statement.setString(2, log.getStudentId());
        statement.setString(3, log.getExamId());
        statement.setString(4, log.getEventType());
        statement.setString(5, log.getDescription());
        statement.setTimestamp(6, toTimestamp(log.getCreatedAt()));
    }

    private ExamIntegrityLog mapResultSetToIntegrityLog(ResultSet resultSet) throws SQLException {
        return new ExamIntegrityLog(
                safe(resultSet.getString("log_id")),
                safe(resultSet.getString("student_id")),
                safe(resultSet.getString("exam_id")),
                normalizeEventTypeInput(resultSet.getString("event_type")),
                safe(resultSet.getString("description")),
                fromTimestamp(resultSet.getTimestamp("created_at"))
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

    private String normalizeEventTypeInput(String value) {
        return FileUtil.clean(value).toUpperCase();
    }

    private boolean existsById(String logId) {
        String cleanLogId = FileUtil.clean(logId);

        if (cleanLogId.isEmpty()) {
            return false;
        }

        String sql = "SELECT log_id FROM exam_integrity_logs " +
                "WHERE LOWER(TRIM(log_id)) = LOWER(TRIM(?)) " +
                "LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanLogId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException e) {
            System.out.println("EXAMINTEGRITYLOGDAO ERROR -> existsById failed for " + cleanLogId);
            e.printStackTrace();
            return false;
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}