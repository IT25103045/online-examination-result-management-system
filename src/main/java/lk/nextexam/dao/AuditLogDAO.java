package lk.nextexam.dao;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lk.nextexam.model.AuditLog;

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
 * AuditLogDAO manages MySQL audit log records.
 *
 * MySQL table:
 * audit_logs
 *
 * Columns:
 * audit_id, user_id, user_role, action, module, description, status, ip_address, created_at
 *
 * Responsible Member:
 * IT25103045 - De Silva H.L.D.C.P.C
 */
public class AuditLogDAO {

    private static final DateTimeFormatter STORAGE_DATE_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public List<AuditLog> getAllLogs(ServletContext context) {
        List<AuditLog> logs = new ArrayList<>();

        String sql = "SELECT audit_id, user_id, user_role, action, module, description, status, ip_address, created_at " +
                "FROM audit_logs " +
                "ORDER BY created_at DESC, audit_id DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                logs.add(mapResultSetToAuditLog(resultSet));
            }

        } catch (SQLException e) {
            System.out.println("AUDITLOGDAO ERROR -> getAllLogs failed");
            e.printStackTrace();
        }

        return logs;
    }

    public List<AuditLog> getRecentLogs(ServletContext context, int limit) {
        List<AuditLog> logs = new ArrayList<>();
        int safeLimit = limit <= 0 ? 10 : limit;

        String sql = "SELECT audit_id, user_id, user_role, action, module, description, status, ip_address, created_at " +
                "FROM audit_logs " +
                "ORDER BY created_at DESC, audit_id DESC " +
                "LIMIT ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, safeLimit);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    logs.add(mapResultSetToAuditLog(resultSet));
                }
            }

        } catch (SQLException e) {
            System.out.println("AUDITLOGDAO ERROR -> getRecentLogs failed");
            e.printStackTrace();
        }

        return logs;
    }

    public AuditLog getLogById(ServletContext context, String auditId) {
        String cleanAuditId = FileUtil.clean(auditId);

        if (cleanAuditId.isEmpty()) {
            return null;
        }

        String sql = "SELECT audit_id, user_id, user_role, action, module, description, status, ip_address, created_at " +
                "FROM audit_logs " +
                "WHERE LOWER(TRIM(audit_id)) = LOWER(TRIM(?)) " +
                "LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanAuditId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToAuditLog(resultSet);
                }
            }

        } catch (SQLException e) {
            System.out.println("AUDITLOGDAO ERROR -> getLogById failed for " + cleanAuditId);
            e.printStackTrace();
        }

        return null;
    }

    public List<AuditLog> getLogsByStatus(ServletContext context, String status) {
        List<AuditLog> logs = new ArrayList<>();
        String cleanStatus = normalizeStatusInput(status);

        if (cleanStatus.isEmpty()) {
            return logs;
        }

        String sql = "SELECT audit_id, user_id, user_role, action, module, description, status, ip_address, created_at " +
                "FROM audit_logs " +
                "WHERE LOWER(TRIM(status)) = LOWER(TRIM(?)) " +
                "ORDER BY created_at DESC, audit_id DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanStatus);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    logs.add(mapResultSetToAuditLog(resultSet));
                }
            }

        } catch (SQLException e) {
            System.out.println("AUDITLOGDAO ERROR -> getLogsByStatus failed for " + cleanStatus);
            e.printStackTrace();
        }

        return logs;
    }

    public List<AuditLog> getLogsByModule(ServletContext context, String module) {
        List<AuditLog> logs = new ArrayList<>();
        String cleanModule = normalizeModuleInput(module);

        if (cleanModule.isEmpty()) {
            return logs;
        }

        String sql = "SELECT audit_id, user_id, user_role, action, module, description, status, ip_address, created_at " +
                "FROM audit_logs " +
                "WHERE LOWER(TRIM(module)) = LOWER(TRIM(?)) " +
                "ORDER BY created_at DESC, audit_id DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanModule);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    logs.add(mapResultSetToAuditLog(resultSet));
                }
            }

        } catch (SQLException e) {
            System.out.println("AUDITLOGDAO ERROR -> getLogsByModule failed for " + cleanModule);
            e.printStackTrace();
        }

        return logs;
    }

    public int countAll(ServletContext context) {
        return countByQuery("SELECT COUNT(*) FROM audit_logs");
    }

    public int countByStatus(ServletContext context, String status) {
        String cleanStatus = normalizeStatusInput(status);

        if (cleanStatus.isEmpty()) {
            return 0;
        }

        String sql = "SELECT COUNT(*) FROM audit_logs WHERE LOWER(TRIM(status)) = LOWER(TRIM(?))";
        return countBySingleParameterQuery(sql, cleanStatus);
    }

    public int countByModule(ServletContext context, String module) {
        String cleanModule = normalizeModuleInput(module);

        if (cleanModule.isEmpty()) {
            return 0;
        }

        String sql = "SELECT COUNT(*) FROM audit_logs WHERE LOWER(TRIM(module)) = LOWER(TRIM(?))";
        return countBySingleParameterQuery(sql, cleanModule);
    }

    public int countToday(ServletContext context) {
        return countByQuery("SELECT COUNT(*) FROM audit_logs WHERE DATE(created_at) = CURRENT_DATE()");
    }

    public boolean addLog(ServletContext context, AuditLog log) {
        if (log == null) {
            return false;
        }

        if (log.getAuditId().isEmpty()) {
            log.setAuditId(FileUtil.generateId("AL"));
        }

        if (log.getCreatedAt().isEmpty()) {
            log.setCreatedAt(now());
        }

        if (!log.isCompleteForSave()) {
            return false;
        }

        if (existsById(log.getAuditId())) {
            log.setAuditId(FileUtil.generateId("AL"));
        }

        String sql = "INSERT INTO audit_logs " +
                "(audit_id, user_id, user_role, action, module, description, status, ip_address, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            fillAuditLogStatement(statement, log);
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("AUDITLOGDAO ERROR -> addLog failed");
            e.printStackTrace();
            return false;
        }
    }

    public boolean logAction(ServletContext context,
                             String userId,
                             String userRole,
                             String action,
                             String module,
                             String description,
                             String status,
                             String ipAddress) {

        AuditLog log = new AuditLog(
                FileUtil.generateId("AL"),
                FileUtil.clean(userId),
                FileUtil.clean(userRole),
                FileUtil.clean(action),
                normalizeModuleInput(module),
                FileUtil.clean(description),
                normalizeStatusInput(status),
                FileUtil.clean(ipAddress),
                now()
        );

        return addLog(context, log);
    }

    public boolean logAction(ServletContext context,
                             HttpServletRequest request,
                             String action,
                             String module,
                             String description,
                             String status) {

        HttpSession session = request != null ? request.getSession(false) : null;

        String userId = "SYSTEM";
        String userRole = "System";

        if (session != null) {
            Object sessionUserId = session.getAttribute("userId");
            Object sessionUserRole = session.getAttribute("userRole");

            if (sessionUserId != null) {
                userId = String.valueOf(sessionUserId);
            }

            if (sessionUserRole != null) {
                userRole = String.valueOf(sessionUserRole);
            }
        }

        return logAction(
                context,
                userId,
                userRole,
                action,
                module,
                description,
                status,
                getClientIp(request)
        );
    }

    public boolean deleteLog(ServletContext context, String auditId) {
        String cleanAuditId = FileUtil.clean(auditId);

        if (cleanAuditId.isEmpty()) {
            return false;
        }

        String sql = "DELETE FROM audit_logs WHERE LOWER(TRIM(audit_id)) = LOWER(TRIM(?))";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanAuditId);
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("AUDITLOGDAO ERROR -> deleteLog failed for " + cleanAuditId);
            e.printStackTrace();
            return false;
        }
    }

    public boolean clearAllLogs(ServletContext context) {
        String sql = "DELETE FROM audit_logs";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println("AUDITLOGDAO ERROR -> clearAllLogs failed");
            e.printStackTrace();
            return false;
        }
    }

    public boolean existsById(String auditId) {
        String cleanAuditId = FileUtil.clean(auditId);

        if (cleanAuditId.isEmpty()) {
            return false;
        }

        String sql = "SELECT audit_id FROM audit_logs " +
                "WHERE LOWER(TRIM(audit_id)) = LOWER(TRIM(?)) " +
                "LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanAuditId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException e) {
            System.out.println("AUDITLOGDAO ERROR -> existsById failed for " + cleanAuditId);
            e.printStackTrace();
            return false;
        }
    }

    public String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "Unknown";
        }

        String forwardedFor = request.getHeader("X-Forwarded-For");

        if (forwardedFor != null && !forwardedFor.trim().isEmpty()) {
            return forwardedFor.split(",")[0].trim();
        }

        String realIp = request.getHeader("X-Real-IP");

        if (realIp != null && !realIp.trim().isEmpty()) {
            return realIp.trim();
        }

        return request.getRemoteAddr() == null ? "Unknown" : request.getRemoteAddr();
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
            System.out.println("AUDITLOGDAO ERROR -> countByQuery failed");
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
            System.out.println("AUDITLOGDAO ERROR -> countBySingleParameterQuery failed");
            e.printStackTrace();
        }

        return 0;
    }

    private void fillAuditLogStatement(PreparedStatement statement, AuditLog log) throws SQLException {
        statement.setString(1, log.getAuditId());
        statement.setString(2, log.getUserId());
        statement.setString(3, log.getUserRole());
        statement.setString(4, log.getAction());
        statement.setString(5, log.getModule());
        statement.setString(6, log.getDescription());
        statement.setString(7, log.getStatus());
        statement.setString(8, log.getIpAddress());
        statement.setTimestamp(9, toTimestamp(log.getCreatedAt()));
    }

    private AuditLog mapResultSetToAuditLog(ResultSet resultSet) throws SQLException {
        return new AuditLog(
                safe(resultSet.getString("audit_id")),
                safe(resultSet.getString("user_id")),
                safe(resultSet.getString("user_role")),
                safe(resultSet.getString("action")),
                normalizeModuleInput(resultSet.getString("module")),
                safe(resultSet.getString("description")),
                normalizeStatusInput(resultSet.getString("status")),
                safe(resultSet.getString("ip_address")),
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

    private String normalizeStatusInput(String value) {
        String statusValue = safe(value);

        if (statusValue.isEmpty()) {
            return AuditLog.STATUS_SUCCESS;
        }

        if (statusValue.equalsIgnoreCase(AuditLog.STATUS_SUCCESS)) {
            return AuditLog.STATUS_SUCCESS;
        }

        if (statusValue.equalsIgnoreCase(AuditLog.STATUS_FAILED)) {
            return AuditLog.STATUS_FAILED;
        }

        if (statusValue.equalsIgnoreCase(AuditLog.STATUS_WARNING)) {
            return AuditLog.STATUS_WARNING;
        }

        if (statusValue.equalsIgnoreCase(AuditLog.STATUS_DENIED)) {
            return AuditLog.STATUS_DENIED;
        }

        return statusValue;
    }

    private String normalizeModuleInput(String value) {
        String moduleValue = safe(value);

        if (moduleValue.isEmpty()) {
            return AuditLog.MODULE_SYSTEM;
        }

        if (moduleValue.equalsIgnoreCase(AuditLog.MODULE_AUTHENTICATION)) {
            return AuditLog.MODULE_AUTHENTICATION;
        }

        if (moduleValue.equalsIgnoreCase(AuditLog.MODULE_STUDENTS)) {
            return AuditLog.MODULE_STUDENTS;
        }

        if (moduleValue.equalsIgnoreCase(AuditLog.MODULE_USERS)) {
            return AuditLog.MODULE_USERS;
        }

        if (moduleValue.equalsIgnoreCase(AuditLog.MODULE_EXAMS)) {
            return AuditLog.MODULE_EXAMS;
        }

        if (moduleValue.equalsIgnoreCase(AuditLog.MODULE_QUESTIONS)) {
            return AuditLog.MODULE_QUESTIONS;
        }

        if (moduleValue.equalsIgnoreCase(AuditLog.MODULE_SUBMISSIONS)) {
            return AuditLog.MODULE_SUBMISSIONS;
        }

        if (moduleValue.equalsIgnoreCase(AuditLog.MODULE_MANUAL_MARKING)) {
            return AuditLog.MODULE_MANUAL_MARKING;
        }

        if (moduleValue.equalsIgnoreCase(AuditLog.MODULE_RESULTS)) {
            return AuditLog.MODULE_RESULTS;
        }

        if (moduleValue.equalsIgnoreCase(AuditLog.MODULE_RESULT_APPEALS)) {
            return AuditLog.MODULE_RESULT_APPEALS;
        }

        if (moduleValue.equalsIgnoreCase(AuditLog.MODULE_REPORTS)) {
            return AuditLog.MODULE_REPORTS;
        }

        if (moduleValue.equalsIgnoreCase(AuditLog.MODULE_NOTIFICATIONS)) {
            return AuditLog.MODULE_NOTIFICATIONS;
        }

        if (moduleValue.equalsIgnoreCase(AuditLog.MODULE_DOCUMENTS)) {
            return AuditLog.MODULE_DOCUMENTS;
        }

        if (moduleValue.equalsIgnoreCase(AuditLog.MODULE_FEEDBACK)) {
            return AuditLog.MODULE_FEEDBACK;
        }

        if (moduleValue.equalsIgnoreCase(AuditLog.MODULE_SYSTEM)) {
            return AuditLog.MODULE_SYSTEM;
        }

        return moduleValue;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}