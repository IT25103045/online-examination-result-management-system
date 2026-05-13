package lk.nextexam.model;

import lk.nextexam.dao.FileUtil;

/**
 * Professional Result model for NextExamLK.
 *
 * Storage format:
 * resultId|studentId|examId|marks|grade|status|verification|published
 */
public class Result {

    public static final String GRADE_A = "A";
    public static final String GRADE_B = "B";
    public static final String GRADE_C = "C";
    public static final String GRADE_S = "S";
    public static final String GRADE_F = "F";

    public static final String STATUS_PASS = "Pass";
    public static final String STATUS_FAIL = "Fail";
    public static final String STATUS_PENDING = "Pending";

    public static final String VERIFICATION_VERIFIED = "Verified";
    public static final String VERIFICATION_PENDING = "Pending";
    public static final String VERIFICATION_REVIEW = "Review";

    public static final String PUBLISHED_YES = "Published";
    public static final String PUBLISHED_NO = "Not Published";

    private String resultId;
    private String studentId;
    private String examId;
    private String marks;
    private String grade;
    private String status;
    private String verification;
    private String published;

    public Result() {
    }

    public Result(String resultId,
                  String studentId,
                  String examId,
                  String marks,
                  String grade,
                  String status,
                  String verification,
                  String published) {
        this.resultId = resultId;
        this.studentId = studentId;
        this.examId = examId;
        this.marks = marks;
        this.grade = grade;
        this.status = status;
        this.verification = verification;
        this.published = published;
    }

    public String getResultId() {
        return safe(resultId);
    }

    public void setResultId(String resultId) {
        this.resultId = resultId;
    }

    public String getStudentId() {
        return safe(studentId);
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getExamId() {
        return safe(examId);
    }

    public void setExamId(String examId) {
        this.examId = examId;
    }

    public String getMarks() {
        return safe(marks);
    }

    public void setMarks(String marks) {
        this.marks = marks;
    }

    public String getGrade() {
        return normalizeGrade(grade);
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getStatus() {
        return normalizeStatus(status);
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getVerification() {
        return normalizeVerification(verification);
    }

    public void setVerification(String verification) {
        this.verification = verification;
    }

    public String getPublished() {
        return normalizePublished(published);
    }

    public void setPublished(String published) {
        this.published = published;
    }

    public double getMarksAsDouble() {
        try {
            return Double.parseDouble(getMarks());
        } catch (Exception e) {
            return 0.0;
        }
    }

    public String getDisplayMarks() {
        double value = getMarksAsDouble();

        if (value == Math.floor(value)) {
            return String.valueOf((int) value);
        }

        return String.format("%.2f", value);
    }

    public boolean isPass() {
        return STATUS_PASS.equalsIgnoreCase(getStatus());
    }

    public boolean isFail() {
        return STATUS_FAIL.equalsIgnoreCase(getStatus());
    }

    public boolean isPending() {
        return STATUS_PENDING.equalsIgnoreCase(getStatus());
    }

    public boolean isVerified() {
        return VERIFICATION_VERIFIED.equalsIgnoreCase(getVerification());
    }

    public boolean isVerificationPending() {
        return VERIFICATION_PENDING.equalsIgnoreCase(getVerification());
    }

    public boolean isInReview() {
        return VERIFICATION_REVIEW.equalsIgnoreCase(getVerification());
    }

    public boolean isPublished() {
        return PUBLISHED_YES.equalsIgnoreCase(getPublished());
    }

    public boolean isNotPublished() {
        return PUBLISHED_NO.equalsIgnoreCase(getPublished());
    }

    public boolean canPublish() {
        return isVerified() && !isPublished();
    }

    public boolean canEdit() {
        return !isPublished();
    }

    public boolean canDelete() {
        return !isPublished();
    }

    public boolean isValidMarks() {
        double value = getMarksAsDouble();
        return value >= 0 && value <= 100;
    }

    public boolean isValidGrade() {
        return GRADE_A.equalsIgnoreCase(getGrade())
                || GRADE_B.equalsIgnoreCase(getGrade())
                || GRADE_C.equalsIgnoreCase(getGrade())
                || GRADE_S.equalsIgnoreCase(getGrade())
                || GRADE_F.equalsIgnoreCase(getGrade());
    }

    public boolean isValidStatus() {
        return STATUS_PASS.equalsIgnoreCase(getStatus())
                || STATUS_FAIL.equalsIgnoreCase(getStatus())
                || STATUS_PENDING.equalsIgnoreCase(getStatus());
    }

    public boolean isValidVerification() {
        return VERIFICATION_VERIFIED.equalsIgnoreCase(getVerification())
                || VERIFICATION_PENDING.equalsIgnoreCase(getVerification())
                || VERIFICATION_REVIEW.equalsIgnoreCase(getVerification());
    }

    public boolean isValidPublishedStatus() {
        return PUBLISHED_YES.equalsIgnoreCase(getPublished())
                || PUBLISHED_NO.equalsIgnoreCase(getPublished());
    }

    public boolean isCompleteForSave() {
        return !getResultId().isEmpty()
                && !getStudentId().isEmpty()
                && !getExamId().isEmpty()
                && !getMarks().isEmpty()
                && isValidMarks()
                && isValidGrade()
                && isValidStatus()
                && isValidVerification()
                && isValidPublishedStatus();
    }

    public String getGradeBadgeClass() {
        if (GRADE_A.equalsIgnoreCase(getGrade())) {
            return "badge-soft-success";
        }

        if (GRADE_B.equalsIgnoreCase(getGrade())) {
            return "badge-soft-primary";
        }

        if (GRADE_C.equalsIgnoreCase(getGrade())) {
            return "badge-soft-info";
        }

        if (GRADE_S.equalsIgnoreCase(getGrade())) {
            return "badge-soft-warning";
        }

        if (GRADE_F.equalsIgnoreCase(getGrade())) {
            return "badge-soft-danger";
        }

        return "badge-soft-secondary";
    }

    public String getStatusBadgeClass() {
        if (isPass()) {
            return "badge-soft-success";
        }

        if (isFail()) {
            return "badge-soft-danger";
        }

        if (isPending()) {
            return "badge-soft-warning";
        }

        return "badge-soft-secondary";
    }

    public String getVerificationBadgeClass() {
        if (isVerified()) {
            return "badge-soft-success";
        }

        if (isVerificationPending()) {
            return "badge-soft-warning";
        }

        if (isInReview()) {
            return "badge-soft-info";
        }

        return "badge-soft-secondary";
    }

    public String getPublishedBadgeClass() {
        if (isPublished()) {
            return "badge-soft-primary";
        }

        return "badge-soft-secondary";
    }

    public String getPerformanceLabel() {
        if (GRADE_A.equalsIgnoreCase(getGrade())) {
            return "Excellent";
        }

        if (GRADE_B.equalsIgnoreCase(getGrade())) {
            return "Strong";
        }

        if (GRADE_C.equalsIgnoreCase(getGrade())) {
            return "Average";
        }

        if (GRADE_S.equalsIgnoreCase(getGrade())) {
            return "Minimum Pass";
        }

        if (GRADE_F.equalsIgnoreCase(getGrade())) {
            return "Below Pass";
        }

        return "Not Graded";
    }

    public String toFileString() {
        return FileUtil.clean(getResultId()) + "|"
                + FileUtil.clean(getStudentId()) + "|"
                + FileUtil.clean(getExamId()) + "|"
                + FileUtil.clean(getDisplayMarks()) + "|"
                + FileUtil.clean(getGrade()) + "|"
                + FileUtil.clean(getStatus()) + "|"
                + FileUtil.clean(getVerification()) + "|"
                + FileUtil.clean(getPublished());
    }

    public static Result fromFileString(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }

        String[] data = FileUtil.splitRecord(line);

        if (data.length < 8) {
            return null;
        }

        return new Result(
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

    public static String calculateGrade(double marks) {
        if (marks >= 75) {
            return GRADE_A;
        }

        if (marks >= 65) {
            return GRADE_B;
        }

        if (marks >= 55) {
            return GRADE_C;
        }

        if (marks >= 40) {
            return GRADE_S;
        }

        return GRADE_F;
    }

    public static String calculateStatus(double marks) {
        return marks >= 40 ? STATUS_PASS : STATUS_FAIL;
    }

    public void applyGradeAndStatusFromMarks() {
        double value = getMarksAsDouble();
        this.grade = calculateGrade(value);
        this.status = calculateStatus(value);
    }

    private String normalizeGrade(String value) {
        String gradeValue = safe(value).toUpperCase();

        if (GRADE_A.equalsIgnoreCase(gradeValue)) {
            return GRADE_A;
        }

        if (GRADE_B.equalsIgnoreCase(gradeValue)) {
            return GRADE_B;
        }

        if (GRADE_C.equalsIgnoreCase(gradeValue)) {
            return GRADE_C;
        }

        if (GRADE_S.equalsIgnoreCase(gradeValue)) {
            return GRADE_S;
        }

        if (GRADE_F.equalsIgnoreCase(gradeValue)) {
            return GRADE_F;
        }

        return gradeValue;
    }

    private String normalizeStatus(String value) {
        String statusValue = safe(value);

        if (STATUS_PASS.equalsIgnoreCase(statusValue)) {
            return STATUS_PASS;
        }

        if (STATUS_FAIL.equalsIgnoreCase(statusValue)) {
            return STATUS_FAIL;
        }

        if (STATUS_PENDING.equalsIgnoreCase(statusValue)) {
            return STATUS_PENDING;
        }

        return statusValue;
    }

    private String normalizeVerification(String value) {
        String verificationValue = safe(value);

        if (VERIFICATION_VERIFIED.equalsIgnoreCase(verificationValue)) {
            return VERIFICATION_VERIFIED;
        }

        if (VERIFICATION_PENDING.equalsIgnoreCase(verificationValue)) {
            return VERIFICATION_PENDING;
        }

        if (VERIFICATION_REVIEW.equalsIgnoreCase(verificationValue)) {
            return VERIFICATION_REVIEW;
        }

        return verificationValue;
    }

    private String normalizePublished(String value) {
        String publishedValue = safe(value);

        if (PUBLISHED_YES.equalsIgnoreCase(publishedValue)) {
            return PUBLISHED_YES;
        }

        if (PUBLISHED_NO.equalsIgnoreCase(publishedValue)
                || "NotPublished".equalsIgnoreCase(publishedValue)
                || "Unpublished".equalsIgnoreCase(publishedValue)) {
            return PUBLISHED_NO;
        }

        return publishedValue;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}