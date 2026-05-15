package lk.nextexam.model;

import lk.nextexam.dao.FileUtil;

/**
 * ExamIntegrityLog model represents a suspicious or important exam console event.
 *
 * Storage format:
 * logId|studentId|examId|eventType|description|createdAt
 *
 * Responsible Member:
 * IT25103045 - De Silva H.L.D.C.P.C
 */
public class ExamIntegrityLog {

    public static final String EVENT_EXAM_STARTED = "EXAM_STARTED";
    public static final String EVENT_EXAM_SUBMITTED = "EXAM_SUBMITTED";
    public static final String EVENT_TAB_SWITCH = "TAB_SWITCH";
    public static final String EVENT_RIGHT_CLICK_BLOCKED = "RIGHT_CLICK_BLOCKED";
    public static final String EVENT_COPY_BLOCKED = "COPY_BLOCKED";
    public static final String EVENT_PASTE_BLOCKED = "PASTE_BLOCKED";
    public static final String EVENT_FULLSCREEN_EXIT = "FULLSCREEN_EXIT";
    public static final String EVENT_FULLSCREEN_REQUESTED = "FULLSCREEN_REQUESTED";

    private String logId;
    private String studentId;
    private String examId;
    private String eventType;
    private String description;
    private String createdAt;

    public ExamIntegrityLog() {
    }

    public ExamIntegrityLog(String logId,
                            String studentId,
                            String examId,
                            String eventType,
                            String description,
                            String createdAt) {
        this.logId = logId;
        this.studentId = studentId;
        this.examId = examId;
        this.eventType = eventType;
        this.description = description;
        this.createdAt = createdAt;
    }

    public String getLogId() {
        return safe(logId);
    }

    public void setLogId(String logId) {
        this.logId = logId;
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

    public String getEventType() {
        return safe(eventType).toUpperCase();
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getDescription() {
        return safe(description);
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatedAt() {
        return safe(createdAt);
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isCompleteForSave() {
        return !getLogId().isEmpty()
                && !getStudentId().isEmpty()
                && !getExamId().isEmpty()
                && !getEventType().isEmpty()
                && !getDescription().isEmpty()
                && !getCreatedAt().isEmpty();
    }

    public String toFileString() {
        return FileUtil.clean(getLogId()) + "|"
                + FileUtil.clean(getStudentId()) + "|"
                + FileUtil.clean(getExamId()) + "|"
                + FileUtil.clean(getEventType()) + "|"
                + FileUtil.clean(getDescription()) + "|"
                + FileUtil.clean(getCreatedAt());
    }

    public static ExamIntegrityLog fromFileString(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }

        String[] data = FileUtil.splitRecord(line);

        if (data.length < 6) {
            return null;
        }

        return new ExamIntegrityLog(
                data[0],
                data[1],
                data[2],
                data[3],
                data[4],
                data[5]
        );
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}