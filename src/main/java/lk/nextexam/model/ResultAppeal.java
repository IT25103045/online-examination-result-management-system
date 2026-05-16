package lk.nextexam.model;

import lk.nextexam.dao.FileUtil;

/**
 * ResultAppeal model stores student result recheck requests.
 *
 * Storage format:
 * appealId|resultId|examId|studentId|studentName|reasonType|message|status|staffReply|createdAt|updatedAt|reviewedBy
 *
 * Responsible Member:
 * IT25103045 - De Silva H.L.D.C.P.C
 */
public class ResultAppeal {

    public static final String STATUS_PENDING = "Pending";
    public static final String STATUS_UNDER_REVIEW = "Under Review";
    public static final String STATUS_RESOLVED = "Resolved";
    public static final String STATUS_REJECTED = "Rejected";

    public static final String REASON_MARK_RECHECK = "Mark Recheck";
    public static final String REASON_MISSING_MARKS = "Missing Marks";
    public static final String REASON_WRONG_RESULT = "Wrong Result";
    public static final String REASON_ESSAY_REVIEW = "Essay Review";
    public static final String REASON_TECHNICAL_ISSUE = "Technical Issue";
    public static final String REASON_OTHER = "Other";

    private String appealId;
    private String resultId;
    private String examId;
    private String studentId;
    private String studentName;
    private String reasonType;
    private String message;
    private String status;
    private String staffReply;
    private String createdAt;
    private String updatedAt;
    private String reviewedBy;

    public ResultAppeal() {
    }

    public ResultAppeal(String appealId,
                        String resultId,
                        String examId,
                        String studentId,
                        String studentName,
                        String reasonType,
                        String message,
                        String status,
                        String staffReply,
                        String createdAt,
                        String updatedAt,
                        String reviewedBy) {
        this.appealId = appealId;
        this.resultId = resultId;
        this.examId = examId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.reasonType = reasonType;
        this.message = message;
        this.status = status;
        this.staffReply = staffReply;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.reviewedBy = reviewedBy;
    }

    public String getAppealId() {
        return safe(appealId);
    }

    public void setAppealId(String appealId) {
        this.appealId = appealId;
    }

    public String getResultId() {
        return safe(resultId);
    }

    public void setResultId(String resultId) {
        this.resultId = resultId;
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

    public String getReasonType() {
        return normalizeReason(reasonType);
    }

    public void setReasonType(String reasonType) {
        this.reasonType = reasonType;
    }

    public String getMessage() {
        return safe(message);
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return normalizeStatus(status);
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStaffReply() {
        return safe(staffReply);
    }

    public void setStaffReply(String staffReply) {
        this.staffReply = staffReply;
    }

    public String getCreatedAt() {
        return safe(createdAt);
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return safe(updatedAt);
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getReviewedBy() {
        return safe(reviewedBy);
    }

    public void setReviewedBy(String reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public boolean isPending() {
        return STATUS_PENDING.equalsIgnoreCase(getStatus());
    }

    public boolean isUnderReview() {
        return STATUS_UNDER_REVIEW.equalsIgnoreCase(getStatus());
    }

    public boolean isResolved() {
        return STATUS_RESOLVED.equalsIgnoreCase(getStatus());
    }

    public boolean isRejected() {
        return STATUS_REJECTED.equalsIgnoreCase(getStatus());
    }

    public boolean isValidStatus() {
        return isPending() || isUnderReview() || isResolved() || isRejected();
    }

    public boolean isValidReason() {
        String reason = getReasonType();

        return REASON_MARK_RECHECK.equalsIgnoreCase(reason)
                || REASON_MISSING_MARKS.equalsIgnoreCase(reason)
                || REASON_WRONG_RESULT.equalsIgnoreCase(reason)
                || REASON_ESSAY_REVIEW.equalsIgnoreCase(reason)
                || REASON_TECHNICAL_ISSUE.equalsIgnoreCase(reason)
                || REASON_OTHER.equalsIgnoreCase(reason);
    }

    public String getStatusBadgeClass() {
        if (isPending()) {
            return "badge-soft-warning";
        }

        if (isUnderReview()) {
            return "badge-soft-primary";
        }

        if (isResolved()) {
            return "badge-soft-success";
        }

        if (isRejected()) {
            return "badge-soft-danger";
        }

        return "badge-soft-secondary";
    }

    public boolean isCompleteForSave() {
        return !getAppealId().isEmpty()
                && !getResultId().isEmpty()
                && !getExamId().isEmpty()
                && !getStudentId().isEmpty()
                && !getStudentName().isEmpty()
                && !getReasonType().isEmpty()
                && !getMessage().isEmpty()
                && isValidReason()
                && isValidStatus()
                && !getCreatedAt().isEmpty()
                && !getUpdatedAt().isEmpty();
    }

    public String toFileString() {
        return FileUtil.clean(getAppealId()) + "|"
                + FileUtil.clean(getResultId()) + "|"
                + FileUtil.clean(getExamId()) + "|"
                + FileUtil.clean(getStudentId()) + "|"
                + FileUtil.clean(getStudentName()) + "|"
                + FileUtil.clean(getReasonType()) + "|"
                + FileUtil.clean(getMessage()) + "|"
                + FileUtil.clean(getStatus()) + "|"
                + FileUtil.clean(getStaffReply()) + "|"
                + FileUtil.clean(getCreatedAt()) + "|"
                + FileUtil.clean(getUpdatedAt()) + "|"
                + FileUtil.clean(getReviewedBy());
    }

    public static ResultAppeal fromFileString(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }

        String[] data = FileUtil.splitRecord(line);

        if (data.length < 12) {
            return null;
        }

        return new ResultAppeal(
                data[0],
                data[1],
                data[2],
                data[3],
                data[4],
                data[5],
                data[6],
                data[7],
                data[8],
                data[9],
                data[10],
                data[11]
        );
    }

    private String normalizeStatus(String value) {
        String clean = safe(value);

        if (clean.isEmpty()) {
            return STATUS_PENDING;
        }

        if (STATUS_PENDING.equalsIgnoreCase(clean)) {
            return STATUS_PENDING;
        }

        if (STATUS_UNDER_REVIEW.equalsIgnoreCase(clean)) {
            return STATUS_UNDER_REVIEW;
        }

        if (STATUS_RESOLVED.equalsIgnoreCase(clean)) {
            return STATUS_RESOLVED;
        }

        if (STATUS_REJECTED.equalsIgnoreCase(clean)) {
            return STATUS_REJECTED;
        }

        return clean;
    }

    private String normalizeReason(String value) {
        String clean = safe(value);

        if (clean.isEmpty()) {
            return REASON_MARK_RECHECK;
        }

        if (REASON_MARK_RECHECK.equalsIgnoreCase(clean)) {
            return REASON_MARK_RECHECK;
        }

        if (REASON_MISSING_MARKS.equalsIgnoreCase(clean)) {
            return REASON_MISSING_MARKS;
        }

        if (REASON_WRONG_RESULT.equalsIgnoreCase(clean)) {
            return REASON_WRONG_RESULT;
        }

        if (REASON_ESSAY_REVIEW.equalsIgnoreCase(clean)) {
            return REASON_ESSAY_REVIEW;
        }

        if (REASON_TECHNICAL_ISSUE.equalsIgnoreCase(clean)) {
            return REASON_TECHNICAL_ISSUE;
        }

        if (REASON_OTHER.equalsIgnoreCase(clean)) {
            return REASON_OTHER;
        }

        return clean;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}