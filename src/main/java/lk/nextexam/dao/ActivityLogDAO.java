package lk.nextexam.dao;

import jakarta.servlet.ServletContext;
import lk.nextexam.model.ActivityLog;

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
 * ActivityLogDAO manages MySQL activity log records.
 *
 * MySQL table:
 * activity_logs
 *
 * Columns:
 * id, user_id, user_role, action, description, created_at
 *
 * Responsible Member:
 * IT25103045 - De Silva H.L.D.C.P.C
 */
public class ActivityLogDAO {

    private static final DateTimeFormatter STORAGE_DATE_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Creates and saves a new activity log record.
     */
    public boolean addLog(ServletContext context,
                          String userId,
                          String userRole,
                          String action,
                          String description) {

        ActivityLog log = new ActivityLog(
                FileUtil.generateId("LOG"),
                FileUtil.clean(userId),
                FileUtil.clean(userRole),
                FileUtil.clean(action).toUpperCase(),
                FileUtil.clean(description),
                now()
        );

        return addLog(context, log);
    }

    /**
     * Saves a prepared ActivityLog object.
     */
    public boolean addLog(ServletContext context, ActivityLog log) {
        if (log == null) {
            return false;
        }

        if (log.getId().isEmpty()) {
            log.setId(FileUtil.generateId("LOG"));
        }

        if (log.getCreatedAt().isEmpty()) {
            log.setCreatedAt(now());
        }

        if (log.getAction().isEmpty() || log.getDescription().isEmpty()) {
            return false;
        }

        if (existsById(log.getId())) {
            log.setId(FileUtil.generateId("LOG"));
        }

        String sql = "INSERT INTO activity_logs " +
                "(id, user_id, user_role, action, description, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, log.getId());
            statement.setString(2, log.getUserId());
            statement.setString(3, log.getUserRole());
            statement.setString(4, log.getAction());
            statement.setString(5, log.getDescription());
            statement.setTimestamp(6, toTimestamp(log.getCreatedAt()));

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("ACTIVITYLOGDAO ERROR -> addLog failed");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Reads all activity log records.
     */
    public List<ActivityLog> getAllLogs(ServletContext context) {
        List<ActivityLog> logs = new ArrayList<>();

        String sql = "SELECT id, user_id, user_role, action, description, created_at " +
                "FROM activity_logs " +
                "ORDER BY created_at DESC, id DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                logs.add(mapResultSetToActivityLog(resultSet));
            }

        } catch (SQLException e) {
            System.out.println("ACTIVITYLOGDAO ERROR -> getAllLogs failed");
            e.printStackTrace();
        }

        return logs;
    }

    /**
     * Returns latest activity logs.
     */
    public List<ActivityLog> getLatestLogs(ServletContext context, int limit) {
        List<ActivityLog> logs = new ArrayList<>();

        int safeLimit = limit <= 0 ? 5 : limit;

        String sql = "SELECT id, user_id, user_role, action, description, created_at " +
                "FROM activity_logs " +
                "ORDER BY created_at DESC, id DESC " +
                "LIMIT ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, safeLimit);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    logs.add(mapResultSetToActivityLog(resultSet));
                }
            }

        } catch (SQLException e) {
            System.out.println("ACTIVITYLOGDAO ERROR -> getLatestLogs failed");
            e.printStackTrace();
        }

        return logs;
    }

    /**
     * Compatibility method for older dashboard.jsp calls.
     */
    public List<ActivityLog> getRecentLogs(ServletContext context, int limit) {
        return getLatestLogs(context, limit);
    }

    /**
     * Compatibility method for possible older code.
     */
    public List<ActivityLog> getRecentActivityLogs(ServletContext context, int limit) {
        return getLatestLogs(context, limit);
    }

    public List<ActivityLog> getLogsByUserId(ServletContext context, String userId) {
        List<ActivityLog> logs = new ArrayList<>();
        String cleanUserId = FileUtil.clean(userId);

        if (cleanUserId.isEmpty()) {
            return logs;
        }

        String sql = "SELECT id, user_id, user_role, action, description, created_at " +
                "FROM activity_logs " +
                "WHERE LOWER(TRIM(user_id)) = LOWER(TRIM(?)) " +
                "ORDER BY created_at DESC, id DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanUserId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    logs.add(mapResultSetToActivityLog(resultSet));
                }
            }

        } catch (SQLException e) {
            System.out.println("ACTIVITYLOGDAO ERROR -> getLogsByUserId failed for " + cleanUserId);
            e.printStackTrace();
        }

        return logs;
    }

    public List<ActivityLog> getLogsByRole(ServletContext context, String userRole) {
        List<ActivityLog> logs = new ArrayList<>();
        String cleanRole = FileUtil.clean(userRole);

        if (cleanRole.isEmpty()) {
            return logs;
        }

        String sql = "SELECT id, user_id, user_role, action, description, created_at " +
                "FROM activity_logs " +
                "WHERE LOWER(TRIM(user_role)) = LOWER(TRIM(?)) " +
                "ORDER BY created_at DESC, id DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanRole);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    logs.add(mapResultSetToActivityLog(resultSet));
                }
            }

        } catch (SQLException e) {
            System.out.println("ACTIVITYLOGDAO ERROR -> getLogsByRole failed for " + cleanRole);
            e.printStackTrace();
        }

        return logs;
    }

    public List<ActivityLog> getLogsByAction(ServletContext context, String action) {
        List<ActivityLog> logs = new ArrayList<>();
        String cleanAction = FileUtil.clean(action).toUpperCase();

        if (cleanAction.isEmpty()) {
            return logs;
        }

        String sql = "SELECT id, user_id, user_role, action, description, created_at " +
                "FROM activity_logs " +
                "WHERE LOWER(TRIM(action)) = LOWER(TRIM(?)) " +
                "ORDER BY created_at DESC, id DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanAction);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    logs.add(mapResultSetToActivityLog(resultSet));
                }
            }

        } catch (SQLException e) {
            System.out.println("ACTIVITYLOGDAO ERROR -> getLogsByAction failed for " + cleanAction);
            e.printStackTrace();
        }

        return logs;
    }

    /**
     * Counts all activity log records.
     */
    public int countLogs(ServletContext context) {
        return countByQuery("SELECT COUNT(*) FROM activity_logs");
    }

    public int countAllLogs(ServletContext context) {
        return countLogs(context);
    }

    public int countLogsByUserId(ServletContext context, String userId) {
        String cleanUserId = FileUtil.clean(userId);

        if (cleanUserId.isEmpty()) {
            return 0;
        }

        String sql = "SELECT COUNT(*) FROM activity_logs " +
                "WHERE LOWER(TRIM(user_id)) = LOWER(TRIM(?))";

        return countBySingleParameterQuery(sql, cleanUserId);
    }

    public int countLogsByRole(ServletContext context, String userRole) {
        String cleanRole = FileUtil.clean(userRole);

        if (cleanRole.isEmpty()) {
            return 0;
        }

        String sql = "SELECT COUNT(*) FROM activity_logs " +
                "WHERE LOWER(TRIM(user_role)) = LOWER(TRIM(?))";

        return countBySingleParameterQuery(sql, cleanRole);
    }

    public int countLogsByAction(ServletContext context, String action) {
        String cleanAction = FileUtil.clean(action).toUpperCase();

        if (cleanAction.isEmpty()) {
            return 0;
        }

        String sql = "SELECT COUNT(*) FROM activity_logs " +
                "WHERE LOWER(TRIM(action)) = LOWER(TRIM(?))";

        return countBySingleParameterQuery(sql, cleanAction);
    }

    /**
     * Deletes an activity log record by log ID.
     */
    public boolean deleteLog(ServletContext context, String logId) {
        String cleanLogId = FileUtil.clean(logId);

        if (cleanLogId.isEmpty()) {
            return false;
        }

        String sql = "DELETE FROM activity_logs " +
                "WHERE LOWER(TRIM(id)) = LOWER(TRIM(?))";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanLogId);
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("ACTIVITYLOGDAO ERROR -> deleteLog failed for " + cleanLogId);
            e.printStackTrace();
            return false;
        }
    }

    public boolean clearAllLogs(ServletContext context) {
        String sql = "DELETE FROM activity_logs";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println("ACTIVITYLOGDAO ERROR -> clearAllLogs failed");
            e.printStackTrace();
            return false;
        }
    }

    public boolean existsById(String logId) {
        String cleanLogId = FileUtil.clean(logId);

        if (cleanLogId.isEmpty()) {
            return false;
        }

        String sql = "SELECT id FROM activity_logs " +
                "WHERE LOWER(TRIM(id)) = LOWER(TRIM(?)) " +
                "LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanLogId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException e) {
            System.out.println("ACTIVITYLOGDAO ERROR -> existsById failed for " + cleanLogId);
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
            System.out.println("ACTIVITYLOGDAO ERROR -> countByQuery failed");
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
            System.out.println("ACTIVITYLOGDAO ERROR -> countBySingleParameterQuery failed");
            e.printStackTrace();
        }

        return 0;
    }

    private ActivityLog mapResultSetToActivityLog(ResultSet resultSet) throws SQLException {
        return new ActivityLog(
                safe(resultSet.getString("id")),
                safe(resultSet.getString("user_id")),
                safe(resultSet.getString("user_role")),
                safe(resultSet.getString("action")),
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

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}