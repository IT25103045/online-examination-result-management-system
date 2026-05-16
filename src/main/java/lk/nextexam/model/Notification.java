package lk.nextexam.model;

import lk.nextexam.dao.FileUtil;

/**
 * Notification model stores user/role-based notification records.
 *
 * Storage format:
 * notificationId|targetUserId|targetRole|title|message|type|status|createdAt|readAt|targetUrl
 *
 * Responsible Member:
 * IT25103045 - De Silva H.L.D.C.P.C
 */
public class Notification {

    public static final String STATUS_UNREAD = "Unread";
    public static final String STATUS_READ = "Read";
    public static final String STATUS_ARCHIVED = "Archived";

    public static final String TYPE_RESULT = "Result";
    public static final String TYPE_APPEAL = "Appeal";
    public static final String TYPE_DOCUMENT = "Document";
    public static final String TYPE_FEEDBACK = "Feedback";
    public static final String TYPE_EXAM = "Exam";
    public static final String TYPE_NOTICE = "Notice";
    public static final String TYPE_SYSTEM = "System";

    private String notificationId;
    private String targetUserId;
    private String targetRole;
    private String title;
    private String message;
    private String type;
    private String status;
    private String createdAt;
    private String readAt;
    private String targetUrl;

    public Notification() {
    }

    public Notification(String notificationId,
                        String targetUserId,
                        String targetRole,
                        String title,
                        String message,
                        String type,
                        String status,
                        String createdAt,
                        String readAt,
                        String targetUrl) {
        this.notificationId = notificationId;
        this.targetUserId = targetUserId;
        this.targetRole = targetRole;
        this.title = title;
        this.message = message;
        this.type = type;
        this.status = status;
        this.createdAt = createdAt;
        this.readAt = readAt;
        this.targetUrl = targetUrl;
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

    public String getReadAt() {
        return safe(readAt);
    }

    public void setReadAt(String readAt) {
        this.readAt = readAt;
    }

    public String getTargetUrl() {
        return safe(targetUrl);
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public boolean isUnread() {
        return STATUS_UNREAD.equalsIgnoreCase(getStatus());
    }

    public boolean isRead() {
        return STATUS_READ.equalsIgnoreCase(getStatus());
    }

    public boolean isArchived() {
        return STATUS_ARCHIVED.equalsIgnoreCase(getStatus());
    }

    public boolean isValidStatus() {
        return isUnread() || isRead() || isArchived();
    }

    public boolean isTargetedToUser(String userId, String role) {
        String cleanUserId = safe(userId);
        String cleanRole = safe(role);

        boolean userMatch = !getTargetUserId().isEmpty()
                && getTargetUserId().equalsIgnoreCase(cleanUserId);

        boolean roleMatch = !getTargetRole().isEmpty()
                && getTargetRole().equalsIgnoreCase(cleanRole);

        boolean allMatch = "All".equalsIgnoreCase(getTargetRole());

        return userMatch || roleMatch || allMatch;
    }

    public String getTypeIcon() {
        if (TYPE_RESULT.equalsIgnoreCase(getType())) {
            return "bi-bar-chart-fill";
        }

        if (TYPE_APPEAL.equalsIgnoreCase(getType())) {
            return "bi-arrow-repeat";
        }

        if (TYPE_DOCUMENT.equalsIgnoreCase(getType())) {
            return "bi-folder-check";
        }

        if (TYPE_FEEDBACK.equalsIgnoreCase(getType())) {
            return "bi-chat-dots-fill";
        }

        if (TYPE_EXAM.equalsIgnoreCase(getType())) {
            return "bi-journal-check";
        }

        if (TYPE_NOTICE.equalsIgnoreCase(getType())) {
            return "bi-megaphone-fill";
        }

        return "bi-info-circle-fill";
    }

    public String getTypeBadgeClass() {
        if (TYPE_RESULT.equalsIgnoreCase(getType())) {
            return "badge-soft-success";
        }

        if (TYPE_APPEAL.equalsIgnoreCase(getType())) {
            return "badge-soft-warning";
        }

        if (TYPE_DOCUMENT.equalsIgnoreCase(getType())) {
            return "badge-soft-info";
        }

        if (TYPE_FEEDBACK.equalsIgnoreCase(getType())) {
            return "badge-soft-primary";
        }

        if (TYPE_EXAM.equalsIgnoreCase(getType())) {
            return "badge-soft-danger";
        }

        if (TYPE_NOTICE.equalsIgnoreCase(getType())) {
            return "badge-soft-primary";
        }

        return "badge-soft-secondary";
    }

    public String getStatusBadgeClass() {
        if (isUnread()) {
            return "badge-soft-warning";
        }

        if (isRead()) {
            return "badge-soft-success";
        }

        if (isArchived()) {
            return "badge-soft-secondary";
        }

        return "badge-soft-secondary";
    }

    public boolean isCompleteForSave() {
        return !getNotificationId().isEmpty()
                && !getTitle().isEmpty()
                && !getMessage().isEmpty()
                && !getType().isEmpty()
                && isValidStatus()
                && !getCreatedAt().isEmpty()
                && (!getTargetUserId().isEmpty() || !getTargetRole().isEmpty());
    }

    public String toFileString() {
        return FileUtil.clean(getNotificationId()) + "|"
                + FileUtil.clean(getTargetUserId()) + "|"
                + FileUtil.clean(getTargetRole()) + "|"
                + FileUtil.clean(getTitle()) + "|"
                + FileUtil.clean(getMessage()) + "|"
                + FileUtil.clean(getType()) + "|"
                + FileUtil.clean(getStatus()) + "|"
                + FileUtil.clean(getCreatedAt()) + "|"
                + FileUtil.clean(getReadAt()) + "|"
                + FileUtil.clean(getTargetUrl());
    }

    public static Notification fromFileString(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }

        String[] data = FileUtil.splitRecord(line);

        if (data.length < 10) {
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
                data[7],
                data[8],
                data[9]
        );
    }

    private String normalizeStatus(String value) {
        String clean = safe(value);

        if (clean.isEmpty()) {
            return STATUS_UNREAD;
        }

        if (STATUS_UNREAD.equalsIgnoreCase(clean)) {
            return STATUS_UNREAD;
        }

        if (STATUS_READ.equalsIgnoreCase(clean)) {
            return STATUS_READ;
        }

        if (STATUS_ARCHIVED.equalsIgnoreCase(clean)) {
            return STATUS_ARCHIVED;
        }

        return clean;
    }

    private String normalizeType(String value) {
        String clean = safe(value);

        if (clean.isEmpty()) {
            return TYPE_SYSTEM;
        }

        if (TYPE_RESULT.equalsIgnoreCase(clean)) {
            return TYPE_RESULT;
        }

        if (TYPE_APPEAL.equalsIgnoreCase(clean)) {
            return TYPE_APPEAL;
        }

        if (TYPE_DOCUMENT.equalsIgnoreCase(clean)) {
            return TYPE_DOCUMENT;
        }

        if (TYPE_FEEDBACK.equalsIgnoreCase(clean)) {
            return TYPE_FEEDBACK;
        }

        if (TYPE_EXAM.equalsIgnoreCase(clean)) {
            return TYPE_EXAM;
        }

        if (TYPE_NOTICE.equalsIgnoreCase(clean)) {
            return TYPE_NOTICE;
        }

        if (TYPE_SYSTEM.equalsIgnoreCase(clean)) {
            return TYPE_SYSTEM;
        }

        return clean;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}