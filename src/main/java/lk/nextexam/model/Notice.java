package lk.nextexam.model;

import lk.nextexam.dao.FileUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Notice model for NextExamLK.
 *
 * Storage format:
 * noticeId|title|description|noticeDate|targetGroup|priority|status
 *
 * Target groups:
 * - All
 * - Admin
 * - Lecturer
 * - Student
 *
 * Priority:
 * - Low
 * - Normal
 * - High
 * - Urgent
 *
 * Status:
 * - Draft
 * - Published
 * - Archived
 */
public class Notice {

    public static final String TARGET_ALL = "All";
    public static final String TARGET_ADMIN = "Admin";
    public static final String TARGET_LECTURER = "Lecturer";
    public static final String TARGET_STUDENT = "Student";

    public static final String PRIORITY_LOW = "Low";
    public static final String PRIORITY_NORMAL = "Normal";
    public static final String PRIORITY_HIGH = "High";
    public static final String PRIORITY_URGENT = "Urgent";

    public static final String STATUS_DRAFT = "Draft";
    public static final String STATUS_PUBLISHED = "Published";
    public static final String STATUS_ARCHIVED = "Archived";

    private static final DateTimeFormatter STORAGE_DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DISPLAY_DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private String noticeId;
    private String title;
    private String description;
    private String noticeDate;
    private String targetGroup;
    private String priority;
    private String status;

    public Notice() {
    }

    public Notice(String noticeId,
                  String title,
                  String description,
                  String noticeDate,
                  String targetGroup,
                  String priority,
                  String status) {
        this.noticeId = noticeId;
        this.title = title;
        this.description = description;
        this.noticeDate = noticeDate;
        this.targetGroup = targetGroup;
        this.priority = priority;
        this.status = status;
    }

    public String getNoticeId() {
        return safe(noticeId);
    }

    public void setNoticeId(String noticeId) {
        this.noticeId = noticeId;
    }

    public String getTitle() {
        return safe(title);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return safe(description);
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNoticeDate() {
        return safe(noticeDate);
    }

    public void setNoticeDate(String noticeDate) {
        this.noticeDate = noticeDate;
    }

    public String getTargetGroup() {
        return normalizeTargetGroup(targetGroup);
    }

    public void setTargetGroup(String targetGroup) {
        this.targetGroup = targetGroup;
    }

    public String getPriority() {
        return normalizePriority(priority);
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return normalizeStatus(status);
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /*
     * Target helpers
     */
    public boolean isForAll() {
        return TARGET_ALL.equalsIgnoreCase(getTargetGroup());
    }

    public boolean isForAdmin() {
        return TARGET_ADMIN.equalsIgnoreCase(getTargetGroup());
    }

    public boolean isForLecturer() {
        return TARGET_LECTURER.equalsIgnoreCase(getTargetGroup());
    }

    public boolean isForStudent() {
        return TARGET_STUDENT.equalsIgnoreCase(getTargetGroup());
    }

    public boolean isVisibleForRole(String role) {
        String cleanRole = FileUtil.clean(role);

        if (cleanRole.isEmpty()) {
            return false;
        }

        if (isForAll()) {
            return true;
        }

        return getTargetGroup().equalsIgnoreCase(cleanRole);
    }

    public boolean isValidTargetGroup() {
        return isForAll() || isForAdmin() || isForLecturer() || isForStudent();
    }

    /*
     * Priority helpers
     */
    public boolean isLowPriority() {
        return PRIORITY_LOW.equalsIgnoreCase(getPriority());
    }

    public boolean isNormalPriority() {
        return PRIORITY_NORMAL.equalsIgnoreCase(getPriority());
    }

    public boolean isHighPriority() {
        return PRIORITY_HIGH.equalsIgnoreCase(getPriority());
    }

    public boolean isUrgentPriority() {
        return PRIORITY_URGENT.equalsIgnoreCase(getPriority());
    }

    public boolean isValidPriority() {
        return isLowPriority() || isNormalPriority() || isHighPriority() || isUrgentPriority();
    }

    /*
     * Status helpers
     */
    public boolean isDraft() {
        return STATUS_DRAFT.equalsIgnoreCase(getStatus());
    }

    public boolean isPublished() {
        return STATUS_PUBLISHED.equalsIgnoreCase(getStatus());
    }

    public boolean isArchived() {
        return STATUS_ARCHIVED.equalsIgnoreCase(getStatus());
    }

    public boolean isValidStatus() {
        return isDraft() || isPublished() || isArchived();
    }

    public boolean canEdit() {
        return !isArchived();
    }

    public boolean canDelete() {
        return !isPublished();
    }

    /*
     * Date helpers
     */
    public LocalDate getNoticeLocalDate() {
        String value = getNoticeDate();

        if (value.isEmpty()) {
            return null;
        }

        try {
            return LocalDate.parse(value, STORAGE_DATE_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public boolean isValidNoticeDate() {
        return getNoticeLocalDate() != null;
    }

    public String getDisplayNoticeDate() {
        LocalDate date = getNoticeLocalDate();

        if (date == null) {
            return getNoticeDate().isEmpty() ? "Not dated" : getNoticeDate();
        }

        return date.format(DISPLAY_DATE_FORMAT);
    }

    public boolean isToday() {
        LocalDate date = getNoticeLocalDate();
        return date != null && date.equals(LocalDate.now());
    }

    /*
     * UI helpers
     */
    public String getPriorityBadgeClass() {
        if (isUrgentPriority()) {
            return "badge-soft-danger";
        }

        if (isHighPriority()) {
            return "badge-soft-warning";
        }

        if (isNormalPriority()) {
            return "badge-soft-primary";
        }

        if (isLowPriority()) {
            return "badge-soft-secondary";
        }

        return "badge-soft-secondary";
    }

    public String getStatusBadgeClass() {
        if (isPublished()) {
            return "badge-soft-success";
        }

        if (isDraft()) {
            return "badge-soft-warning";
        }

        if (isArchived()) {
            return "badge-soft-secondary";
        }

        return "badge-soft-secondary";
    }

    public String getTargetBadgeClass() {
        if (isForAll()) {
            return "badge-soft-primary";
        }

        if (isForStudent()) {
            return "badge-soft-success";
        }

        if (isForLecturer()) {
            return "badge-soft-info";
        }

        if (isForAdmin()) {
            return "badge-soft-danger";
        }

        return "badge-soft-secondary";
    }

    public String getShortDescription() {
        String text = getDescription();

        if (text.length() <= 120) {
            return text;
        }

        return text.substring(0, 120) + "...";
    }

    public String getVisibilityLabel() {
        if (isPublished()) {
            return "Visible";
        }

        if (isDraft()) {
            return "Draft";
        }

        if (isArchived()) {
            return "Archived";
        }

        return "Hidden";
    }

    public String getVisibilityBadgeClass() {
        if (isPublished()) {
            return "badge-soft-success";
        }

        if (isDraft()) {
            return "badge-soft-warning";
        }

        if (isArchived()) {
            return "badge-soft-secondary";
        }

        return "badge-soft-danger";
    }

    /*
     * Validation
     */
    public boolean isCompleteForSave() {
        return !getNoticeId().isEmpty()
                && !getTitle().isEmpty()
                && !getDescription().isEmpty()
                && isValidNoticeDate()
                && isValidTargetGroup()
                && isValidPriority()
                && isValidStatus();
    }

    /*
     * File serialization
     */
    public String toFileString() {
        return FileUtil.clean(getNoticeId()) + "|"
                + FileUtil.clean(getTitle()) + "|"
                + FileUtil.clean(getDescription()) + "|"
                + FileUtil.clean(getNoticeDate()) + "|"
                + FileUtil.clean(getTargetGroup()) + "|"
                + FileUtil.clean(getPriority()) + "|"
                + FileUtil.clean(getStatus());
    }

    public static Notice fromFileString(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }

        String[] data = FileUtil.splitRecord(line);

        if (data.length < 7) {
            return null;
        }

        return new Notice(
                data[0],
                data[1],
                data[2],
                data[3],
                data[4],
                data[5],
                data[6]
        );
    }

    private String normalizeTargetGroup(String value) {
        String target = safe(value);

        if (TARGET_ALL.equalsIgnoreCase(target)) {
            return TARGET_ALL;
        }

        if (TARGET_ADMIN.equalsIgnoreCase(target)) {
            return TARGET_ADMIN;
        }

        if (TARGET_LECTURER.equalsIgnoreCase(target)) {
            return TARGET_LECTURER;
        }

        if (TARGET_STUDENT.equalsIgnoreCase(target)) {
            return TARGET_STUDENT;
        }

        return target;
    }

    private String normalizePriority(String value) {
        String priorityValue = safe(value);

        if (PRIORITY_LOW.equalsIgnoreCase(priorityValue)) {
            return PRIORITY_LOW;
        }

        if (PRIORITY_NORMAL.equalsIgnoreCase(priorityValue)) {
            return PRIORITY_NORMAL;
        }

        if (PRIORITY_HIGH.equalsIgnoreCase(priorityValue)) {
            return PRIORITY_HIGH;
        }

        if (PRIORITY_URGENT.equalsIgnoreCase(priorityValue)) {
            return PRIORITY_URGENT;
        }

        return priorityValue;
    }

    private String normalizeStatus(String value) {
        String statusValue = safe(value);

        if (STATUS_DRAFT.equalsIgnoreCase(statusValue)) {
            return STATUS_DRAFT;
        }

        if (STATUS_PUBLISHED.equalsIgnoreCase(statusValue)) {
            return STATUS_PUBLISHED;
        }

        if (STATUS_ARCHIVED.equalsIgnoreCase(statusValue)) {
            return STATUS_ARCHIVED;
        }

        return statusValue;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}