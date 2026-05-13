package lk.nextexam.model;

import lk.nextexam.dao.FileUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Feedback model for NextExamLK.
 *
 * Storage format:
 * feedbackId|studentId|category|message|date|status
 *
 * Categories:
 * - Exam
 * - Result
 * - Technical
 * - Account
 * - General
 *
 * Status:
 * - New
 * - In Review
 * - Resolved
 * - Closed
 */
public class Feedback {

    public static final String CATEGORY_EXAM = "Exam";
    public static final String CATEGORY_RESULT = "Result";
    public static final String CATEGORY_TECHNICAL = "Technical";
    public static final String CATEGORY_ACCOUNT = "Account";
    public static final String CATEGORY_GENERAL = "General";

    public static final String STATUS_NEW = "New";
    public static final String STATUS_IN_REVIEW = "In Review";
    public static final String STATUS_RESOLVED = "Resolved";
    public static final String STATUS_CLOSED = "Closed";

    private static final DateTimeFormatter STORAGE_DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DISPLAY_DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private String feedbackId;
    private String studentId;
    private String category;
    private String message;
    private String date;
    private String status;

    public Feedback() {
    }

    public Feedback(String feedbackId,
                    String studentId,
                    String category,
                    String message,
                    String date,
                    String status) {
        this.feedbackId = feedbackId;
        this.studentId = studentId;
        this.category = category;
        this.message = message;
        this.date = date;
        this.status = status;
    }

    public String getFeedbackId() {
        return safe(feedbackId);
    }

    public void setFeedbackId(String feedbackId) {
        this.feedbackId = feedbackId;
    }

    public String getStudentId() {
        return safe(studentId);
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getCategory() {
        return normalizeCategory(category);
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getMessage() {
        return safe(message);
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDate() {
        return safe(date);
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStatus() {
        return normalizeStatus(status);
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /*
     * Category helpers
     */
    public boolean isExamCategory() {
        return CATEGORY_EXAM.equalsIgnoreCase(getCategory());
    }

    public boolean isResultCategory() {
        return CATEGORY_RESULT.equalsIgnoreCase(getCategory());
    }

    public boolean isTechnicalCategory() {
        return CATEGORY_TECHNICAL.equalsIgnoreCase(getCategory());
    }

    public boolean isAccountCategory() {
        return CATEGORY_ACCOUNT.equalsIgnoreCase(getCategory());
    }

    public boolean isGeneralCategory() {
        return CATEGORY_GENERAL.equalsIgnoreCase(getCategory());
    }

    public boolean isValidCategory() {
        return isExamCategory()
                || isResultCategory()
                || isTechnicalCategory()
                || isAccountCategory()
                || isGeneralCategory();
    }

    /*
     * Status helpers
     */
    public boolean isNew() {
        return STATUS_NEW.equalsIgnoreCase(getStatus());
    }

    public boolean isInReview() {
        return STATUS_IN_REVIEW.equalsIgnoreCase(getStatus());
    }

    public boolean isResolved() {
        return STATUS_RESOLVED.equalsIgnoreCase(getStatus());
    }

    public boolean isClosed() {
        return STATUS_CLOSED.equalsIgnoreCase(getStatus());
    }

    public boolean isOpen() {
        return isNew() || isInReview();
    }

    public boolean isCompleted() {
        return isResolved() || isClosed();
    }

    public boolean isValidStatus() {
        return isNew() || isInReview() || isResolved() || isClosed();
    }

    public boolean canEditByStudent(String currentStudentId) {
        return isNew() && getStudentId().equalsIgnoreCase(FileUtil.clean(currentStudentId));
    }

    public boolean canDeleteByStudent(String currentStudentId) {
        return isNew() && getStudentId().equalsIgnoreCase(FileUtil.clean(currentStudentId));
    }

    public boolean canBeReviewedByStaff() {
        return isNew() || isInReview();
    }

    /*
     * Date helpers
     */
    public LocalDate getFeedbackLocalDate() {
        String value = getDate();

        if (value.isEmpty()) {
            return null;
        }

        try {
            return LocalDate.parse(value, STORAGE_DATE_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public boolean isValidDate() {
        return getFeedbackLocalDate() != null;
    }

    public String getDisplayDate() {
        LocalDate feedbackDate = getFeedbackLocalDate();

        if (feedbackDate == null) {
            return getDate().isEmpty() ? "Not dated" : getDate();
        }

        return feedbackDate.format(DISPLAY_DATE_FORMAT);
    }

    public boolean isToday() {
        LocalDate feedbackDate = getFeedbackLocalDate();
        return feedbackDate != null && feedbackDate.equals(LocalDate.now());
    }

    /*
     * UI helpers
     */
    public String getCategoryBadgeClass() {
        if (isExamCategory()) {
            return "badge-soft-primary";
        }

        if (isResultCategory()) {
            return "badge-soft-success";
        }

        if (isTechnicalCategory()) {
            return "badge-soft-danger";
        }

        if (isAccountCategory()) {
            return "badge-soft-warning";
        }

        if (isGeneralCategory()) {
            return "badge-soft-secondary";
        }

        return "badge-soft-secondary";
    }

    public String getStatusBadgeClass() {
        if (isNew()) {
            return "badge-soft-primary";
        }

        if (isInReview()) {
            return "badge-soft-warning";
        }

        if (isResolved()) {
            return "badge-soft-success";
        }

        if (isClosed()) {
            return "badge-soft-secondary";
        }

        return "badge-soft-secondary";
    }

    public String getPriorityLabel() {
        if (isTechnicalCategory()) {
            return "High";
        }

        if (isExamCategory() || isResultCategory()) {
            return "Medium";
        }

        return "Normal";
    }

    public String getPriorityBadgeClass() {
        if ("High".equalsIgnoreCase(getPriorityLabel())) {
            return "badge-soft-danger";
        }

        if ("Medium".equalsIgnoreCase(getPriorityLabel())) {
            return "badge-soft-warning";
        }

        return "badge-soft-secondary";
    }

    public String getShortMessage() {
        String text = getMessage();

        if (text.length() <= 120) {
            return text;
        }

        return text.substring(0, 120) + "...";
    }

    public String getProgressLabel() {
        if (isNew()) {
            return "Waiting for review";
        }

        if (isInReview()) {
            return "Being reviewed";
        }

        if (isResolved()) {
            return "Resolved";
        }

        if (isClosed()) {
            return "Closed";
        }

        return "Unknown";
    }

    /*
     * Validation
     */
    public boolean isCompleteForSave() {
        return !getFeedbackId().isEmpty()
                && !getStudentId().isEmpty()
                && isValidCategory()
                && !getMessage().isEmpty()
                && isValidDate()
                && isValidStatus();
    }

    /*
     * File serialization
     */
    public String toFileString() {
        return FileUtil.clean(getFeedbackId()) + "|"
                + FileUtil.clean(getStudentId()) + "|"
                + FileUtil.clean(getCategory()) + "|"
                + FileUtil.clean(getMessage()) + "|"
                + FileUtil.clean(getDate()) + "|"
                + FileUtil.clean(getStatus());
    }

    public static Feedback fromFileString(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }

        String[] data = FileUtil.splitRecord(line);

        if (data.length < 6) {
            return null;
        }

        return new Feedback(
                data[0],
                data[1],
                data[2],
                data[3],
                data[4],
                data[5]
        );
    }

    private String normalizeCategory(String value) {
        String categoryValue = safe(value);

        if (CATEGORY_EXAM.equalsIgnoreCase(categoryValue)) {
            return CATEGORY_EXAM;
        }

        if (CATEGORY_RESULT.equalsIgnoreCase(categoryValue)) {
            return CATEGORY_RESULT;
        }

        if (CATEGORY_TECHNICAL.equalsIgnoreCase(categoryValue)) {
            return CATEGORY_TECHNICAL;
        }

        if (CATEGORY_ACCOUNT.equalsIgnoreCase(categoryValue)) {
            return CATEGORY_ACCOUNT;
        }

        if (CATEGORY_GENERAL.equalsIgnoreCase(categoryValue)) {
            return CATEGORY_GENERAL;
        }

        return categoryValue;
    }

    private String normalizeStatus(String value) {
        String statusValue = safe(value);

        if (STATUS_NEW.equalsIgnoreCase(statusValue)) {
            return STATUS_NEW;
        }

        if (STATUS_IN_REVIEW.equalsIgnoreCase(statusValue)
                || "InReview".equalsIgnoreCase(statusValue)
                || "Review".equalsIgnoreCase(statusValue)) {
            return STATUS_IN_REVIEW;
        }

        if (STATUS_RESOLVED.equalsIgnoreCase(statusValue)) {
            return STATUS_RESOLVED;
        }

        if (STATUS_CLOSED.equalsIgnoreCase(statusValue)) {
            return STATUS_CLOSED;
        }

        return statusValue;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}