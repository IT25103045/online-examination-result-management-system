package lk.nextexam.model;

import lk.nextexam.dao.FileUtil;

/**
 * Notification model represents a system notification for a user or role.
 *
 * Storage format:
 * notificationId|targetUserId|targetRole|title|message|type|status|createdAt
 *
 * Responsible Member:
 * IT25103045 - De Silva H.L.D.C.P.C
 */
public class Notification {

    public static final String STATUS_UNREAD = "Unread";
    public static final String STATUS_READ = "Read";

    public static final String TYPE_RESULT = "RESULT";
    public static final String TYPE_DOCUMENT = "DOCUMENT";
    public static final String TYPE_NOTICE = "NOTICE";
    public static final String TYPE_FEEDBACK = "FEEDBACK";
    public static final String TYPE_EXAM = "EXAM";
    public static final String TYPE_SYSTEM = "SYSTEM";

    private String notificationId;
    private String targetUserId;
    private String targetRole;
    private String title;
    private String message;
    private String type;
    private String status;
    private String createdAt;

    public Notification() {
    }

    public Notification(String notificationId,
                        String targetUserId,
                        String targetRole,
                        String title,
                        String message,
                        String type,
                        String status,
                        String createdAt) {
        this.notificationId = notificationId;
        this.targetUserId = targetUserId;
        this.targetRole = targetRole;
        this.title = title;
        this.message = message;
        this.type = type;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getNotificationId() {
        return safe(notificationId);
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getTargetUserId() {
        return safe(targetUserId);
    }

    public void setTargetUserId(String targetUserId) {
        this.targetUserId = targetUserId;
    }

    public String getTargetRole() {
        return safe(targetRole);
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }

    public String getTitle() {
        return safe(title);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return safe(message);
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return normalizeType(type);
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return normalizeStatus(status);
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return safe(createdAt);
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isUnread() {
        return STATUS_UNREAD.equalsIgnoreCase(getStatus());
    }

    public boolean isRead() {
        return STATUS_READ.equalsIgnoreCase(getStatus());
    }

    public String getTypeIcon() {
        if (TYPE_RESULT.equalsIgnoreCase(getType())) {
            return "bi-bar-chart-fill";
        }

        if (TYPE_DOCUMENT.equalsIgnoreCase(getType())) {
            return "bi-folder-check";
        }

        if (TYPE_NOTICE.equalsIgnoreCase(getType())) {
            return "bi-megaphone-fill";
        }

        if (TYPE_FEEDBACK.equalsIgnoreCase(getType())) {
            return "bi-chat-dots-fill";
        }

        if (TYPE_EXAM.equalsIgnoreCase(getType())) {
            return "bi-journal-check";
        }

        return "bi-bell-fill";
    }

    public String getTypeBadgeClass() {
        if (TYPE_RESULT.equalsIgnoreCase(getType())) {
            return "badge-soft-primary";
        }

        if (TYPE_DOCUMENT.equalsIgnoreCase(getType())) {
            return "badge-soft-info";
        }

        if (TYPE_NOTICE.equalsIgnoreCase(getType())) {
            return "badge-soft-warning";
        }

        if (TYPE_FEEDBACK.equalsIgnoreCase(getType())) {
            return "badge-soft-success";
        }

        if (TYPE_EXAM.equalsIgnoreCase(getType())) {
            return "badge-soft-primary";
        }

        return "badge-soft-secondary";
    }

    public String getStatusBadgeClass() {
        return isUnread() ? "badge-soft-danger" : "badge-soft-secondary";
    }

    public boolean isCompleteForSave() {
        return !getNotificationId().isEmpty()
                && !getTitle().isEmpty()
                && !getMessage().isEmpty()
                && !getType().isEmpty()
                && !getStatus().isEmpty()
                && !getCreatedAt().isEmpty();
    }

    public String toFileString() {
        return FileUtil.clean(getNotificationId()) + "|"
                + FileUtil.clean(getTargetUserId()) + "|"
                + FileUtil.clean(getTargetRole()) + "|"
                + FileUtil.clean(getTitle()) + "|"
                + FileUtil.clean(getMessage()) + "|"
                + FileUtil.clean(getType()) + "|"
                + FileUtil.clean(getStatus()) + "|"
                + FileUtil.clean(getCreatedAt());
    }

    public static Notification fromFileString(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }

        String[] data = FileUtil.splitRecord(line);

        if (data.length < 8) {
            return null;
        }

        return new Notification(
                data[0],
                data[1],
                data[2],
                data[3],
                data[4],
                data[5],
                data[6],
                data[7]
        );
    }

    private String normalizeStatus(String value) {
        String statusValue = safe(value);

        if (STATUS_READ.equalsIgnoreCase(statusValue)) {
            return STATUS_READ;
        }

        return STATUS_UNREAD;
    }

    private String normalizeType(String value) {
        String typeValue = safe(value).toUpperCase();

        if (TYPE_RESULT.equalsIgnoreCase(typeValue)) {
            return TYPE_RESULT;
        }

        if (TYPE_DOCUMENT.equalsIgnoreCase(typeValue)) {
            return TYPE_DOCUMENT;
        }

        if (TYPE_NOTICE.equalsIgnoreCase(typeValue)) {
            return TYPE_NOTICE;
        }

        if (TYPE_FEEDBACK.equalsIgnoreCase(typeValue)) {
            return TYPE_FEEDBACK;
        }

        if (TYPE_EXAM.equalsIgnoreCase(typeValue)) {
            return TYPE_EXAM;
        }

        return TYPE_SYSTEM;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}