package lk.nextexam.model;

import lk.nextexam.dao.FileUtil;

/**
 * ActivityLog model represents an important user/system action in Nextexam.
 *
 * This class demonstrates encapsulation by storing activity log details
 * in private fields and exposing them through getter and setter methods.
 *
 * File format:
 * id|userId|userRole|action|description|createdAt
 *
 * Responsible Member:
 * IT25103045 - De Silva H.L.D.C.P.C
 */
public class ActivityLog {

    private String id;
    private String userId;
    private String userRole;
    private String action;
    private String description;
    private String createdAt;

    public ActivityLog() {
    }

    public ActivityLog(String id,
                       String userId,
                       String userRole,
                       String action,
                       String description,
                       String createdAt) {
        this.id = id;
        this.userId = userId;
        this.userRole = userRole;
        this.action = action;
        this.description = description;
        this.createdAt = createdAt;
    }

    public String getId() {
        return safe(id);
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return safe(userId);
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserRole() {
        return safe(userRole);
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public String getAction() {
        return safe(action);
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDescription() {
        return safe(description);
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatedAt() {
        return safe(createdAt);
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Converts an ActivityLog object into a pipe-separated text-file record.
     */
    public String toFileString() {
        return FileUtil.clean(getId()) + "|"
                + FileUtil.clean(getUserId()) + "|"
                + FileUtil.clean(getUserRole()) + "|"
                + FileUtil.clean(getAction()) + "|"
                + FileUtil.clean(getDescription()) + "|"
                + FileUtil.clean(getCreatedAt());
    }

    /**
     * Converts a pipe-separated text-file record into an ActivityLog object.
     */
    public static ActivityLog fromFileString(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }

        String[] data = FileUtil.splitRecord(line);

        if (data.length < 6) {
            return null;
        }

        return new ActivityLog(
                data[0],
                data[1],
                data[2],
                data[3],
                data[4],
                data[5]
        );
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}