package lk.nextexam.model;

import lk.nextexam.dao.FileUtil;

/**
 * ManualMark model stores lecturer/admin marks for essay questions.
 *
 * Storage format:
 * markId|submissionId|examId|studentId|questionId|marksAwarded|feedback|markedBy|markedAt
 *
 * Responsible Member:
 * IT25103045 - De Silva H.L.D.C.P.C
 */
public class ManualMark {

    private String markId;
    private String submissionId;
    private String examId;
    private String studentId;
    private String questionId;
    private String marksAwarded;
    private String feedback;
    private String markedBy;
    private String markedAt;

    public ManualMark() {
    }

    public ManualMark(String markId,
                      String submissionId,
                      String examId,
                      String studentId,
                      String questionId,
                      String marksAwarded,
                      String feedback,
                      String markedBy,
                      String markedAt) {
        this.markId = markId;
        this.submissionId = submissionId;
        this.examId = examId;
        this.studentId = studentId;
        this.questionId = questionId;
        this.marksAwarded = marksAwarded;
        this.feedback = feedback;
        this.markedBy = markedBy;
        this.markedAt = markedAt;
    }

    public String getMarkId() {
        return safe(markId);
    }

    public void setMarkId(String markId) {
        this.markId = markId;
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

    public String getQuestionId() {
        return safe(questionId);
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public String getMarksAwarded() {
        return safe(marksAwarded);
    }

    public void setMarksAwarded(String marksAwarded) {
        this.marksAwarded = marksAwarded;
    }

    public String getFeedback() {
        return safe(feedback);
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public String getMarkedBy() {
        return safe(markedBy);
    }

    public void setMarkedBy(String markedBy) {
        this.markedBy = markedBy;
    }

    public String getMarkedAt() {
        return safe(markedAt);
    }

    public void setMarkedAt(String markedAt) {
        this.markedAt = markedAt;
    }

    public double getMarksAwardedAsDouble() {
        try {
            return Double.parseDouble(getMarksAwarded());
        } catch (Exception e) {
            return 0.0;
        }
    }

    public String getMarksAwardedDisplay() {
        double value = getMarksAwardedAsDouble();

        if (value == Math.floor(value)) {
            return String.valueOf((int) value);
        }

        return String.format("%.2f", value);
    }

    public boolean isCompleteForSave() {
        return !getMarkId().isEmpty()
                && !getSubmissionId().isEmpty()
                && !getExamId().isEmpty()
                && !getStudentId().isEmpty()
                && !getQuestionId().isEmpty()
                && !getMarksAwarded().isEmpty()
                && !getMarkedBy().isEmpty()
                && !getMarkedAt().isEmpty();
    }

    public String toFileString() {
        return FileUtil.clean(getMarkId()) + "|"
                + FileUtil.clean(getSubmissionId()) + "|"
                + FileUtil.clean(getExamId()) + "|"
                + FileUtil.clean(getStudentId()) + "|"
                + FileUtil.clean(getQuestionId()) + "|"
                + FileUtil.clean(getMarksAwardedDisplay()) + "|"
                + FileUtil.clean(getFeedback()) + "|"
                + FileUtil.clean(getMarkedBy()) + "|"
                + FileUtil.clean(getMarkedAt());
    }

    public static ManualMark fromFileString(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }

        String[] data = FileUtil.splitRecord(line);

        if (data.length < 9) {
            return null;
        }

        return new ManualMark(
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