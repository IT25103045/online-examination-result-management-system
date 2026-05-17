package lk.nextexam.dao;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lk.nextexam.model.AuditLog;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * AuditLogDAO manages file-based audit log records.
 *
 * Storage file:
 * audit_logs.txt
 *
 * Responsible Member:
 * IT25103045 - De Silva H.L.D.C.P.C
 */
public class AuditLogDAO {

    private static final String FILE_NAME = "audit_logs.txt";

    private static final DateTimeFormatter STORAGE_DATE_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public List<AuditLog> getAllLogs(ServletContext context) {
        List<AuditLog> logs = new ArrayList<>();
        List<String> lines = FileUtil.readLines(context, FILE_NAME);

        for (String line : lines) {
            AuditLog log = AuditLog.fromFileString(line);

            if (log != null && !log.getAuditId().isEmpty()) {
                logs.add(log);
            }
        }

        return logs;
    }

    public List<AuditLog> getRecentLogs(ServletContext context, int limit) {
        List<AuditLog> allLogs = getAllLogs(context);
        List<AuditLog> recentLogs = new ArrayList<>();

        int safeLimit = Math.max(limit, 0);

        for (int i = allLogs.size() - 1; i >= 0 && recentLogs.size() < safeLimit; i--) {
            recentLogs.add(allLogs.get(i));
        }

        return recentLogs;
    }

    public AuditLog getLogById(ServletContext context, String auditId) {
        String cleanAuditId = FileUtil.clean(auditId);

        if (cleanAuditId.isEmpty()) {
            return null;
        }

        for (AuditLog log : getAllLogs(context)) {
            if (log.getAuditId().equalsIgnoreCase(cleanAuditId)) {
                return log;
            }
        }

        return null;
    }

    public List<AuditLog> getLogsByStatus(ServletContext context, String status) {
        List<AuditLog> selectedLogs = new ArrayList<>();
        String cleanStatus = FileUtil.clean(status);

        if (cleanStatus.isEmpty()) {
            return selectedLogs;
        }

        for (AuditLog log : getAllLogs(context)) {
            if (log.getStatus().equalsIgnoreCase(cleanStatus)) {
                selectedLogs.add(log);
            }
        }

        return selectedLogs;
    }

    public List<AuditLog> getLogsByModule(ServletContext context, String module) {
        List<AuditLog> selectedLogs = new ArrayList<>();
        String cleanModule = FileUtil.clean(module);

        if (cleanModule.isEmpty()) {
            return selectedLogs;
        }

        for (AuditLog log : getAllLogs(context)) {
            if (log.getModule().equalsIgnoreCase(cleanModule)) {
                selectedLogs.add(log);
            }
        }

        return selectedLogs;
    }

    public int countAll(ServletContext context) {
        return getAllLogs(context).size();
    }

    public int countByStatus(ServletContext context, String status) {
        return getLogsByStatus(context, status).size();
    }

    public int countByModule(ServletContext context, String module) {
        return getLogsByModule(context, module).size();
    }

    public int countToday(ServletContext context) {
        int count = 0;
        String today = LocalDate.now().toString();

        for (AuditLog log : getAllLogs(context)) {
            if (log.getCreatedAt().startsWith(today)) {
                count++;
            }
        }

        return count;
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

        return FileUtil.appendLine(context, FILE_NAME, log.toFileString());
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
                FileUtil.clean(module),
                FileUtil.clean(description),
                FileUtil.clean(status),
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

    public String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "Unknown";
        }

        String forwardedFor = request.getHeader("X-Forwarded-For");

        if (forwardedFor != null && !forwardedFor.trim().isEmpty()) {
            return forwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr() == null ? "Unknown" : request.getRemoteAddr();
    }

    public String now() {
        return LocalDateTime.now().format(STORAGE_DATE_TIME);
    }
}