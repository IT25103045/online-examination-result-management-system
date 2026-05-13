package lk.nextexam.model;

import lk.nextexam.dao.FileUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * ExamSubmission model for NextExamLK.
 *
 * Current storage format:
 * submissionId|examId|studentId|studentName|submittedAt|answersData|score|totalMarks|status
 *
 * answersData format example:
 * Q001=A,flagged=NO;Q002=C,flagged=YES;Q003=Essay answer here,flagged=NO
 *
 * Professional lifecycle:
 * Submitted              - Student submitted the exam.
 * Auto Marked            - MCQ-only exam was automatically marked.
 * Manual Review Required - Essay questions need lecturer review.
 * Marked                 - Lecturer completed marking.
 * Published              - Result is visible to student.
 * Cancelled              - Submission cancelled by authorized staff.
 */
public class ExamSubmission {

    public static final String STATUS_SUBMITTED = "Submitted";
    public static final String STATUS_AUTO_MARKED = "Auto Marked";
    public static final String STATUS_MANUAL_REVIEW_REQUIRED = "Manual Review Required";
    public static final String STATUS_MARKED = "Marked";
    public static final String STATUS_PUBLISHED = "Published";
    public static final String STATUS_CANCELLED = "Cancelled";

    private static final DateTimeFormatter STORAGE_DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final DateTimeFormatter DISPLAY_DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    private String submissionId;
    private String examId;
    private String studentId;
    private String studentName;
    private String submittedAt;
    private String answersData;
    private String score;
    private String totalMarks;
    private String status;

    public ExamSubmission() {
    }

    public ExamSubmission(String submissionId,
                          String examId,
                          String studentId,
                          String studentName,
                          String submittedAt,
                          String answersData,
                          String score,
                          String totalMarks,
                          String status) {
        this.submissionId = submissionId;
        this.examId = examId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.submittedAt = submittedAt;
        this.answersData = answersData;
        this.score = score;
        this.totalMarks = totalMarks;
        this.status = status;
    }

    public String getSubmissionId() {
        return safe(submissionId);
    }

    public void setSubmissionId(String submissionId) {
        this.submissionId = submissionId;
    }

    public String getExamId() {
        return safe(examId);
    }

    public void setExamId(String examId) {
        this.examId = examId;
    }

    public String getStudentId() {
        return safe(studentId);
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return safe(studentName);
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getSubmittedAt() {
        return safe(submittedAt);
    }

    public void setSubmittedAt(String submittedAt) {
        this.submittedAt = submittedAt;
    }

    public String getAnswersData() {
        return safe(answersData);
    }

    public void setAnswersData(String answersData) {
        this.answersData = answersData;
    }

    public String getScore() {
        return safe(score);
    }

    public void setScore(String score) {
        this.score = score;
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
     * Status helpers
     */
    public boolean isSubmitted() {
        return STATUS_SUBMITTED.equalsIgnoreCase(getStatus());
    }

    public boolean isAutoMarked() {
        return STATUS_AUTO_MARKED.equalsIgnoreCase(getStatus());
    }

    public boolean isManualReviewRequired() {
        return STATUS_MANUAL_REVIEW_REQUIRED.equalsIgnoreCase(getStatus());
    }

    public boolean isMarked() {
        return STATUS_MARKED.equalsIgnoreCase(getStatus());
    }

    public boolean isPublished() {
        return STATUS_PUBLISHED.equalsIgnoreCase(getStatus());
    }

    public boolean isCancelled() {
        return STATUS_CANCELLED.equalsIgnoreCase(getStatus());
    }

    public boolean isValidStatus() {
        return isSubmitted()
                || isAutoMarked()
                || isManualReviewRequired()
                || isMarked()
                || isPublished()
                || isCancelled();
    }

    public boolean canBeMarked() {
        return isSubmitted() || isManualReviewRequired() || isAutoMarked();
    }

    public boolean canBePublished() {
        return isMarked() || isAutoMarked();
    }

    public boolean isVisibleToStudent() {
        return isPublished();
    }

    public boolean isFinalized() {
        return isPublished() || isCancelled();
    }

    /*
     * Score helpers
     */
    public double getScoreAsDouble() {
        try {
            return Double.parseDouble(getScore());
        } catch (Exception e) {
            return 0.0;
        }
    }

    public double getTotalMarksAsDouble() {
        try {
            return Double.parseDouble(getTotalMarks());
        } catch (Exception e) {
            return 0.0;
        }
    }

    public double getPercentage() {
        double total = getTotalMarksAsDouble();

        if (total <= 0) {
            return 0.0;
        }

        return (getScoreAsDouble() / total) * 100;
    }

    public String getPercentageDisplay() {
        return String.format("%.1f%%", getPercentage());
    }

    public String getScoreDisplay() {
        double value = getScoreAsDouble();

        if (value == Math.floor(value)) {
            return String.valueOf((int) value);
        }

        return String.format("%.2f", value);
    }

    public String getTotalMarksDisplay() {
        double value = getTotalMarksAsDouble();

        if (value == Math.floor(value)) {
            return String.valueOf((int) value);
        }

        return String.format("%.2f", value);
    }

    public String getScoreSummary() {
        return getScoreDisplay() + " / " + getTotalMarksDisplay();
    }

    public boolean isPass(double passPercentage) {
        return getPercentage() >= passPercentage;
    }

    /*
     * Date/time helpers
     */
    public LocalDateTime getSubmittedDateTime() {
        String value = getSubmittedAt();

        if (value.isEmpty()) {
            return null;
        }

        try {
            return LocalDateTime.parse(value, STORAGE_DATE_TIME_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public String getDisplaySubmittedAt() {
        LocalDateTime dateTime = getSubmittedDateTime();

        if (dateTime == null) {
            return getSubmittedAt().isEmpty() ? "Not available" : getSubmittedAt();
        }

        return dateTime.format(DISPLAY_DATE_TIME_FORMAT);
    }

    public boolean hasAnswers() {
        return !getAnswersData().isEmpty();
    }

    public int getAnsweredItemCount() {
        if (!hasAnswers()) {
            return 0;
        }

        String[] items = getAnswersData().split(";");

        int count = 0;

        for (String item : items) {
            if (item != null && !item.trim().isEmpty()) {
                count++;
            }
        }

        return count;
    }

    /*
     * UI helpers
     */
    public String getStatusBadgeClass() {
        if (isPublished()) {
            return "badge-soft-success";
        }

        if (isMarked() || isAutoMarked()) {
            return "badge-soft-primary";
        }

        if (isManualReviewRequired()) {
            return "badge-soft-warning";
        }

        if (isSubmitted()) {
            return "badge-soft-info";
        }

        if (isCancelled()) {
            return "badge-soft-danger";
        }

        return "badge-soft-secondary";
    }

    public String getProgressLabel() {
        if (isSubmitted()) {
            return "Submitted";
        }

        if (isAutoMarked()) {
            return "Auto marked";
        }

        if (isManualReviewRequired()) {
            return "Needs manual review";
        }

        if (isMarked()) {
            return "Marked";
        }

        if (isPublished()) {
            return "Published";
        }

        if (isCancelled()) {
            return "Cancelled";
        }

        return "Unknown";
    }

    /*
     * Validation
     */
    public boolean isCompleteForSave() {
        return !getSubmissionId().isEmpty()
                && !getExamId().isEmpty()
                && !getStudentId().isEmpty()
                && !getStudentName().isEmpty()
                && !getSubmittedAt().isEmpty()
                && !getAnswersData().isEmpty()
                && !getScore().isEmpty()
                && !getTotalMarks().isEmpty()
                && isValidStatus();
    }

    /*
     * File serialization
     */
    public String toFileString() {
        return FileUtil.clean(getSubmissionId()) + "|"
                + FileUtil.clean(getExamId()) + "|"
                + FileUtil.clean(getStudentId()) + "|"
                + FileUtil.clean(getStudentName()) + "|"
                + FileUtil.clean(getSubmittedAt()) + "|"
                + FileUtil.clean(getAnswersData()) + "|"
                + FileUtil.clean(getScoreDisplay()) + "|"
                + FileUtil.clean(getTotalMarksDisplay()) + "|"
                + FileUtil.clean(getStatus());
    }

    public static ExamSubmission fromFileString(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }

        String[] data = FileUtil.splitRecord(line);

        if (data.length < 9) {
            return null;
        }

        return new ExamSubmission(
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

    public static String nowTimestamp() {
        return LocalDateTime.now().format(STORAGE_DATE_TIME_FORMAT);
    }

    private String normalizeStatus(String value) {
        String statusValue = safe(value);

        if (STATUS_SUBMITTED.equalsIgnoreCase(statusValue)) {
            return STATUS_SUBMITTED;
        }

        if (STATUS_AUTO_MARKED.equalsIgnoreCase(statusValue)) {
            return STATUS_AUTO_MARKED;
        }

        if (STATUS_MANUAL_REVIEW_REQUIRED.equalsIgnoreCase(statusValue)) {
            return STATUS_MANUAL_REVIEW_REQUIRED;
        }

        if (STATUS_MARKED.equalsIgnoreCase(statusValue)) {
            return STATUS_MARKED;
        }

        if (STATUS_PUBLISHED.equalsIgnoreCase(statusValue)) {
            return STATUS_PUBLISHED;
        }

        if (STATUS_CANCELLED.equalsIgnoreCase(statusValue)) {
            return STATUS_CANCELLED;
        }

        return statusValue;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}