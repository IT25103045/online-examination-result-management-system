package lk.nextexam.dao;

import jakarta.servlet.ServletContext;
import lk.nextexam.model.ActivityLog;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * ActivityLogDAO manages activity log file handling operations.
 *
 * This DAO uses FileUtil to store and retrieve activity records from
 * activity_logs.txt. It separates file handling logic from servlets and JSP pages.
 *
 * Responsible Member:
 * IT25103045 - De Silva H.L.D.C.P.C
 */
public class ActivityLogDAO {

    private static final String FILE_NAME = "activity_logs.txt";

    private static final DateTimeFormatter DISPLAY_DATE_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Creates and saves a new activity log record.
     */
    public boolean addLog(ServletContext context,
                          String userId,
                          String userRole,
                          String action,
                          String description) {

        String id = FileUtil.generateId("LOG");
        String createdAt = LocalDateTime.now().format(DISPLAY_DATE_TIME);

        ActivityLog log = new ActivityLog(
                id,
                FileUtil.clean(userId),
                FileUtil.clean(userRole),
                FileUtil.clean(action).toUpperCase(),
                FileUtil.clean(description),
                createdAt
        );

        return FileUtil.appendLine(context, FILE_NAME, log.toFileString());
    }

    /**
     * Reads all activity log records from the text file.
     */
    public List<ActivityLog> getAllLogs(ServletContext context) {
        List<String> lines = FileUtil.readLines(context, FILE_NAME);
        List<ActivityLog> logs = new ArrayList<>();

        for (String line : lines) {
            ActivityLog log = ActivityLog.fromFileString(line);

            if (log != null) {
                logs.add(log);
            }
        }

        return logs;
    }

    /**
     * Returns the latest activity logs.
     * Since new records are appended, the latest items are at the end of the file.
     */
    public List<ActivityLog> getLatestLogs(ServletContext context, int limit) {
        List<ActivityLog> allLogs = getAllLogs(context);
        List<ActivityLog> latestLogs = new ArrayList<>();

        if (limit <= 0) {
            limit = 5;
        }

        for (int i = allLogs.size() - 1; i >= 0 && latestLogs.size() < limit; i--) {
            latestLogs.add(allLogs.get(i));
        }

        return latestLogs;
    }

    /**
     * Counts all activity log records.
     */
    public int countLogs(ServletContext context) {
        return FileUtil.countLines(context, FILE_NAME);
    }

    /**
     * Deletes an activity log record by log ID.
     */
    public boolean deleteLog(ServletContext context, String logId) {
        return FileUtil.deleteLineById(context, FILE_NAME, logId);
    }
}