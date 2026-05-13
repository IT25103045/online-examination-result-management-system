package lk.nextexam.model;

import lk.nextexam.dao.FileUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Exam model for NextExamLK.
 *
 * Current storage format:
 * examId|subject|examDate|duration|totalMarks|status
 *
 * Notes:
 * - examDate currently stores a date only: yyyy-MM-dd
 * - duration stores minutes
 * - Future versions can extend this model with startTime/endTime without breaking DAO structure
 */
public class Exam {

    public static final String STATUS_DRAFT = "Draft";
    public static final String STATUS_SCHEDULED = "Scheduled";
    public static final String STATUS_ACTIVE = "Active";
    public static final String STATUS_ONGOING = "Ongoing";
    public static final String STATUS_COMPLETED = "Completed";
    public static final String STATUS_PUBLISHED = "Published";
    public static final String STATUS_CANCELLED = "Cancelled";
    public static final String STATUS_INACTIVE = "Inactive";

    private static final int DEFAULT_DURATION_MINUTES = 60;
    private static final int MIN_DURATION_MINUTES = 1;
    private static final int MAX_DURATION_MINUTES = 360;

    private static final double MIN_TOTAL_MARKS = 1.0;
    private static final double MAX_TOTAL_MARKS = 1000.0;

    private static final DateTimeFormatter STORAGE_DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DISPLAY_DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private String examId;
    private String subject;
    private String examDate;
    private String duration;
    private String totalMarks;
    private String status;

    public Exam() {
    }

    public Exam(String examId,
                String subject,
                String examDate,
                String duration,
                String totalMarks,
                String status) {
        this.examId = examId;
        this.subject = subject;
        this.examDate = examDate;
        this.duration = duration;
        this.totalMarks = totalMarks;
        this.status = status;
    }

    public String getExamId() {
        return safe(examId);
    }

    public void setExamId(String examId) {
        this.examId = examId;
    }

    public String getSubject() {
        return safe(subject);
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getExamDate() {
        return safe(examDate);
    }

    public void setExamDate(String examDate) {
        this.examDate = examDate;
    }

    public String getDuration() {
        return safe(duration);
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getTotalMarks() {
        return safe(totalMarks);
    }

    public void setTotalMarks(String totalMarks) {
        this.totalMarks = totalMarks;
    }

    public String getStatus() {
        return normalizeStatus(status);
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /*
     * Lifecycle helpers
     */
    public boolean isDraft() {
        return STATUS_DRAFT.equalsIgnoreCase(getStatus());
    }

    public boolean isScheduled() {
        return STATUS_SCHEDULED.equalsIgnoreCase(getStatus());
    }

    public boolean isActive() {
        return STATUS_ACTIVE.equalsIgnoreCase(getStatus());
    }

    public boolean isOngoing() {
        return STATUS_ONGOING.equalsIgnoreCase(getStatus());
    }

    public boolean isCompleted() {
        return STATUS_COMPLETED.equalsIgnoreCase(getStatus());
    }

    public boolean isPublished() {
        return STATUS_PUBLISHED.equalsIgnoreCase(getStatus());
    }

    public boolean isCancelled() {
        return STATUS_CANCELLED.equalsIgnoreCase(getStatus());
    }

    public boolean isInactive() {
        return STATUS_INACTIVE.equalsIgnoreCase(getStatus());
    }

    public boolean isClosed() {
        return isCompleted() || isPublished() || isCancelled() || isInactive();
    }

    public boolean isAttemptableStatus() {
        return isScheduled() || isActive() || isOngoing();
    }

    /**
     * Student attempt rule.
     *
     * Current project rule:
     * Students can attempt exams with Scheduled, Active, or Ongoing status.
     *
     * Later professional rule:
     * Check date/time window from server side.
     */
    public boolean canStudentAttempt() {
        return isAttemptableStatus();
    }

    public boolean canManageQuestions() {
        return !isCancelled() && !isPublished();
    }

    public boolean canPublishResults() {
        return isCompleted() || isOngoing();
    }

    public boolean canEditExam() {
        return !isPublished();
    }

    public boolean isValidStatus() {
        return isDraft()
                || isScheduled()
                || isActive()
                || isOngoing()
                || isCompleted()
                || isPublished()
                || isCancelled()
                || isInactive();
    }

    /*
     * Duration helpers
     */
    public int getDurationMinutes() {
        String value = getDuration();

        if (value.isEmpty()) {
            return DEFAULT_DURATION_MINUTES;
        }

        try {
            int parsedDuration = Integer.parseInt(value.replaceAll("[^0-9]", ""));

            if (parsedDuration < MIN_DURATION_MINUTES) {
                return DEFAULT_DURATION_MINUTES;
            }

            if (parsedDuration > MAX_DURATION_MINUTES) {
                return MAX_DURATION_MINUTES;
            }

            return parsedDuration;

        } catch (Exception e) {
            return DEFAULT_DURATION_MINUTES;
        }
    }

    public boolean isValidDuration() {
        int minutes = getDurationMinutes();

        try {
            int rawMinutes = Integer.parseInt(getDuration().replaceAll("[^0-9]", ""));
            return rawMinutes >= MIN_DURATION_MINUTES && rawMinutes <= MAX_DURATION_MINUTES && minutes == rawMinutes;
        } catch (Exception e) {
            return false;
        }
    }

    public String getDisplayDuration() {
        int minutes = getDurationMinutes();

        if (minutes == 1) {
            return "1 minute";
        }

        if (minutes < 60) {
            return minutes + " minutes";
        }

        int hours = minutes / 60;
        int remainingMinutes = minutes % 60;

        if (remainingMinutes == 0) {
            return hours == 1 ? "1 hour" : hours + " hours";
        }

        String hourText = hours == 1 ? "1 hour" : hours + " hours";
        String minuteText = remainingMinutes == 1 ? "1 minute" : remainingMinutes + " minutes";

        return hourText + " " + minuteText;
    }

    /*
     * Marks helpers
     */
    public double getTotalMarksAsDouble() {
        try {
            return Double.parseDouble(getTotalMarks());
        } catch (Exception e) {
            return 0.0;
        }
    }

    public boolean isValidTotalMarks() {
        double marks = getTotalMarksAsDouble();
        return marks >= MIN_TOTAL_MARKS && marks <= MAX_TOTAL_MARKS;
    }

    public String getDisplayTotalMarks() {
        double marks = getTotalMarksAsDouble();

        if (marks == Math.floor(marks)) {
            return String.valueOf((int) marks);
        }

        return String.format("%.2f", marks);
    }

    /*
     * Date helpers
     */
    public LocalDate getExamLocalDate() {
        String value = getExamDate();

        if (value.isEmpty()) {
            return null;
        }

        try {
            return LocalDate.parse(value, STORAGE_DATE_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public boolean isValidExamDate() {
        return getExamLocalDate() != null;
    }

    public String getDisplayExamDate() {
        LocalDate date = getExamLocalDate();

        if (date == null) {
            return getExamDate().isEmpty() ? "Not scheduled" : getExamDate();
        }

        return date.format(DISPLAY_DATE_FORMAT);
    }

    public boolean isToday() {
        LocalDate date = getExamLocalDate();
        return date != null && date.equals(LocalDate.now());
    }

    public boolean isPastDate() {
        LocalDate date = getExamLocalDate();
        return date != null && date.isBefore(LocalDate.now());
    }

    public boolean isFutureDate() {
        LocalDate date = getExamLocalDate();
        return date != null && date.isAfter(LocalDate.now());
    }

    /*
     * UI helpers
     */
    public String getDisplayStatusClass() {
        if (isActive() || isOngoing()) {
            return "badge-soft-success";
        }

        if (isScheduled()) {
            return "badge-soft-primary";
        }

        if (isCompleted() || isPublished()) {
            return "badge-soft-info";
        }

        if (isCancelled() || isInactive()) {
            return "badge-soft-danger";
        }

        if (isDraft()) {
            return "badge-soft-warning";
        }

        return "badge-soft-secondary";
    }

    public String getAccessLabel() {
        if (canStudentAttempt()) {
            return "Student Access";
        }

        if (isDraft()) {
            return "Draft";
        }

        if (isClosed()) {
            return "Closed";
        }

        return "Locked";
    }

    public String getAccessBadgeClass() {
        if (canStudentAttempt()) {
            return "badge-soft-success";
        }

        if (isDraft()) {
            return "badge-soft-warning";
        }

        if (isClosed()) {
            return "badge-soft-secondary";
        }

        return "badge-soft-danger";
    }

    public String getLifecycleHint() {
        if (isDraft()) {
            return "Exam is being prepared and is not visible to students.";
        }

        if (isScheduled()) {
            return "Exam is scheduled and visible to eligible students.";
        }

        if (isActive()) {
            return "Exam is active and students can access it.";
        }

        if (isOngoing()) {
            return "Exam is currently ongoing.";
        }

        if (isCompleted()) {
            return "Exam has ended and is ready for marking.";
        }

        if (isPublished()) {
            return "Exam results have been published.";
        }

        if (isCancelled()) {
            return "Exam has been cancelled.";
        }

        if (isInactive()) {
            return "Exam is inactive.";
        }

        return "Unknown exam state.";
    }

    /*
     * Validation helper
     */
    public boolean isCompleteForSave() {
        return !getExamId().isEmpty()
                && !getSubject().isEmpty()
                && isValidExamDate()
                && isValidDuration()
                && isValidTotalMarks()
                && isValidStatus();
    }

    /*
     * File serialization
     */
    public String toFileString() {
        return FileUtil.clean(getExamId()) + "|"
                + FileUtil.clean(getSubject()) + "|"
                + FileUtil.clean(getExamDate()) + "|"
                + FileUtil.clean(String.valueOf(getDurationMinutes())) + "|"
                + FileUtil.clean(getDisplayTotalMarks()) + "|"
                + FileUtil.clean(getStatus());
    }

    public static Exam fromFileString(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }

        String[] data = FileUtil.splitRecord(line);

        if (data.length < 6) {
            return null;
        }

        return new Exam(
                data[0],
                data[1],
                data[2],
                data[3],
                data[4],
                data[5]
        );
    }

    private String normalizeStatus(String value) {
        String statusValue = safe(value);

        if (STATUS_DRAFT.equalsIgnoreCase(statusValue)) {
            return STATUS_DRAFT;
        }

        if (STATUS_SCHEDULED.equalsIgnoreCase(statusValue)) {
            return STATUS_SCHEDULED;
        }

        if (STATUS_ACTIVE.equalsIgnoreCase(statusValue)) {
            return STATUS_ACTIVE;
        }

        if (STATUS_ONGOING.equalsIgnoreCase(statusValue)) {
            return STATUS_ONGOING;
        }

        if (STATUS_COMPLETED.equalsIgnoreCase(statusValue)) {
            return STATUS_COMPLETED;
        }

        if (STATUS_PUBLISHED.equalsIgnoreCase(statusValue)) {
            return STATUS_PUBLISHED;
        }

        if (STATUS_CANCELLED.equalsIgnoreCase(statusValue)) {
            return STATUS_CANCELLED;
        }

        if (STATUS_INACTIVE.equalsIgnoreCase(statusValue)) {
            return STATUS_INACTIVE;
        }

        return statusValue;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}