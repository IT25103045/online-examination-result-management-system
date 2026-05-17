package lk.nextexam.model;

import lk.nextexam.dao.FileUtil;

/**
 * AuditLog model stores important system activity records.
 *
 * Storage format:
 * auditId|userId|userRole|action|module|description|status|ipAddress|createdAt
 *
 * Responsible Member:
 * IT25103045 - De Silva H.L.D.C.P.C
 */
public class AuditLog {

    public static final String STATUS_SUCCESS = "Success";
    public static final String STATUS_FAILED = "Failed";
    public static final String STATUS_WARNING = "Warning";
    public static final String STATUS_DENIED = "Denied";

    public static final String MODULE_AUTHENTICATION = "Authentication";
    public static final String MODULE_STUDENTS = "Students";
    public static final String MODULE_USERS = "Users";
    public static final String MODULE_EXAMS = "Exams";
    public static final String MODULE_QUESTIONS = "Questions";
    public static final String MODULE_SUBMISSIONS = "Submissions";
    public static final String MODULE_MANUAL_MARKING = "Manual Marking";
    public static final String MODULE_RESULTS = "Results";
    public static final String MODULE_RESULT_APPEALS = "Result Appeals";
    public static final String MODULE_REPORTS = "Reports";
    public static final String MODULE_NOTIFICATIONS = "Notifications";
    public static final String MODULE_DOCUMENTS = "Documents";
    public static final String MODULE_FEEDBACK = "Feedback";
    public static final String MODULE_SYSTEM = "System";

    private String auditId;
    private String userId;
    private String userRole;
    private String action;
    private String module;
    private String description;
    private String status;
    private String ipAddress;
    private String createdAt;

    public AuditLog() {
    }

    public AuditLog(String auditId,
                    String userId,
                    String userRole,
                    String action,
                    String module,
                    String description,
                    String status,
                    String ipAddress,
                    String createdAt) {
        this.auditId = auditId;
        this.userId = userId;
        this.userRole = userRole;
        this.action = action;
        this.module = module;
        this.description = description;
        this.status = status;
        this.ipAddress = ipAddress;
        this.createdAt = createdAt;
    }

    public String getAuditId() {
        return safe(auditId);
    }

    public void setAuditId(String auditId) {
        this.auditId = auditId;
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

    public String getModule() {
        return safe(module);
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getDescription() {
        return safe(description);
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        String clean = safe(status);

        if (clean.isEmpty()) {
            return STATUS_SUCCESS;
        }

        if (STATUS_SUCCESS.equalsIgnoreCase(clean)) {
            return STATUS_SUCCESS;
        }

        if (STATUS_FAILED.equalsIgnoreCase(clean)) {
            return STATUS_FAILED;
        }

        if (STATUS_WARNING.equalsIgnoreCase(clean)) {
            return STATUS_WARNING;
        }

        if (STATUS_DENIED.equalsIgnoreCase(clean)) {
            return STATUS_DENIED;
        }

        return clean;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIpAddress() {
        return safe(ipAddress);
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getCreatedAt() {
        return safe(createdAt);
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isSuccess() {
        return STATUS_SUCCESS.equalsIgnoreCase(getStatus());
    }

    public boolean isFailed() {
        return STATUS_FAILED.equalsIgnoreCase(getStatus());
    }

    public boolean isWarning() {
        return STATUS_WARNING.equalsIgnoreCase(getStatus());
    }

    public boolean isDenied() {
        return STATUS_DENIED.equalsIgnoreCase(getStatus());
    }

    public String getStatusBadgeClass() {
        if (isSuccess()) {
            return "badge-soft-success";
        }

        if (isFailed()) {
            return "badge-soft-danger";
        }

        if (isWarning()) {
            return "badge-soft-warning";
        }

        if (isDenied()) {
            return "badge-soft-danger";
        }

        return "badge-soft-secondary";
    }

    public String getModuleBadgeClass() {
        if (MODULE_RESULTS.equalsIgnoreCase(getModule())) {
            return "badge-soft-success";
        }

        if (MODULE_RESULT_APPEALS.equalsIgnoreCase(getModule())) {
            return "badge-soft-warning";
        }

        if (MODULE_REPORTS.equalsIgnoreCase(getModule())) {
            return "badge-soft-primary";
        }

        if (MODULE_NOTIFICATIONS.equalsIgnoreCase(getModule())) {
            return "badge-soft-info";
        }

        if (MODULE_MANUAL_MARKING.equalsIgnoreCase(getModule())) {
            return "badge-soft-warning";
        }

        if (MODULE_SYSTEM.equalsIgnoreCase(getModule())) {
            return "badge-soft-secondary";
        }

        return "badge-soft-primary";
    }

    public boolean isCompleteForSave() {
        return !getAuditId().isEmpty()
                && !getUserId().isEmpty()
                && !getUserRole().isEmpty()
                && !getAction().isEmpty()
                && !getModule().isEmpty()
                && !getDescription().isEmpty()
                && !getStatus().isEmpty()
                && !getCreatedAt().isEmpty();
    }

    public String toFileString() {
        return FileUtil.clean(getAuditId()) + "|"
                + FileUtil.clean(getUserId()) + "|"
                + FileUtil.clean(getUserRole()) + "|"
                + FileUtil.clean(getAction()) + "|"
                + FileUtil.clean(getModule()) + "|"
                + FileUtil.clean(getDescription()) + "|"
                + FileUtil.clean(getStatus()) + "|"
                + FileUtil.clean(getIpAddress()) + "|"
                + FileUtil.clean(getCreatedAt());
    }

    public static AuditLog fromFileString(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }

        String[] data = FileUtil.splitRecord(line);

        if (data.length < 9) {
            return null;
        }

        return new AuditLog(
                data[0],
                data[1],
                data[2],
                data[3],
                data[4],
                data[5],
                data[6],
                data[7],
                data[8]
        );
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}