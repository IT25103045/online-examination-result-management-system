package lk.nextexam.model;

import lk.nextexam.dao.FileUtil;

/**
 * StudentDocument model represents an uploaded academic document.
 *
 * Storage format:
 * documentId|studentId|studentName|documentType|fileName|filePath|status|reviewNote|uploadedAt|reviewedAt
 *
 * OOP Concepts:
 * - Encapsulation: private fields with getters/setters
 * - Information hiding: JSP/Servlets use model methods instead of directly handling record format
 *
 * Responsible Member:
 * IT25103045 - De Silva H.L.D.C.P.C
 */
public class StudentDocument {

    public static final String STATUS_PENDING = "Pending";
    public static final String STATUS_APPROVED = "Approved";
    public static final String STATUS_REJECTED = "Rejected";

    public static final String TYPE_STUDENT_ID = "Student ID";
    public static final String TYPE_MEDICAL = "Medical Certificate";
    public static final String TYPE_EXAM_ELIGIBILITY = "Exam Eligibility";
    public static final String TYPE_OTHER = "Other";

    private String documentId;
    private String studentId;
    private String studentName;
    private String documentType;
    private String fileName;
    private String filePath;
    private String status;
    private String reviewNote;
    private String uploadedAt;
    private String reviewedAt;

    public StudentDocument() {
    }

    public StudentDocument(String documentId,
                           String studentId,
                           String studentName,
                           String documentType,
                           String fileName,
                           String filePath,
                           String status,
                           String reviewNote,
                           String uploadedAt,
                           String reviewedAt) {
        this.documentId = documentId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.documentType = documentType;
        this.fileName = fileName;
        this.filePath = filePath;
        this.status = status;
        this.reviewNote = reviewNote;
        this.uploadedAt = uploadedAt;
        this.reviewedAt = reviewedAt;
    }

    public String getDocumentId() {
        return safe(documentId);
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
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

    public String getDocumentType() {
        return normalizeDocumentType(documentType);
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getFileName() {
        return safe(fileName);
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return safe(filePath);
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getStatus() {
        return normalizeStatus(status);
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReviewNote() {
        String value = safe(reviewNote);
        return value.isEmpty() ? "-" : value;
    }

    public void setReviewNote(String reviewNote) {
        this.reviewNote = reviewNote;
    }

    public String getUploadedAt() {
        return safe(uploadedAt);
    }

    public void setUploadedAt(String uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public String getReviewedAt() {
        String value = safe(reviewedAt);
        return value.isEmpty() ? "-" : value;
    }

    public void setReviewedAt(String reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public boolean isPending() {
        return STATUS_PENDING.equalsIgnoreCase(getStatus());
    }

    public boolean isApproved() {
        return STATUS_APPROVED.equalsIgnoreCase(getStatus());
    }

    public boolean isRejected() {
        return STATUS_REJECTED.equalsIgnoreCase(getStatus());
    }

    public boolean isValidStatus() {
        return isPending() || isApproved() || isRejected();
    }

    public boolean isValidDocumentType() {
        String type = getDocumentType();

        return TYPE_STUDENT_ID.equalsIgnoreCase(type)
                || TYPE_MEDICAL.equalsIgnoreCase(type)
                || TYPE_EXAM_ELIGIBILITY.equalsIgnoreCase(type)
                || TYPE_OTHER.equalsIgnoreCase(type);
    }

    public boolean isCompleteForSave() {
        return !getDocumentId().isEmpty()
                && !getStudentId().isEmpty()
                && !getStudentName().isEmpty()
                && isValidDocumentType()
                && !getFileName().isEmpty()
                && !getFilePath().isEmpty()
                && isValidStatus()
                && !getUploadedAt().isEmpty();
    }

    public String getStatusBadgeClass() {
        if (isApproved()) {
            return "badge-soft-success";
        }

        if (isRejected()) {
            return "badge-soft-danger";
        }

        if (isPending()) {
            return "badge-soft-warning";
        }

        return "badge-soft-secondary";
    }

    public String getStatusIcon() {
        if (isApproved()) {
            return "bi-check-circle-fill";
        }

        if (isRejected()) {
            return "bi-x-circle-fill";
        }

        if (isPending()) {
            return "bi-hourglass-split";
        }

        return "bi-file-earmark";
    }

    public String toFileString() {
        return FileUtil.clean(getDocumentId()) + "|"
                + FileUtil.clean(getStudentId()) + "|"
                + FileUtil.clean(getStudentName()) + "|"
                + FileUtil.clean(getDocumentType()) + "|"
                + FileUtil.clean(getFileName()) + "|"
                + FileUtil.clean(getFilePath()) + "|"
                + FileUtil.clean(getStatus()) + "|"
                + FileUtil.clean(getReviewNote()) + "|"
                + FileUtil.clean(getUploadedAt()) + "|"
                + FileUtil.clean(getReviewedAt());
    }

    public static StudentDocument fromFileString(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }

        String[] data = FileUtil.splitRecord(line);

        if (data.length < 10) {
            return null;
        }

        return new StudentDocument(
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
        String statusValue = safe(value);

        if (STATUS_APPROVED.equalsIgnoreCase(statusValue)) {
            return STATUS_APPROVED;
        }

        if (STATUS_REJECTED.equalsIgnoreCase(statusValue)) {
            return STATUS_REJECTED;
        }

        if (STATUS_PENDING.equalsIgnoreCase(statusValue)) {
            return STATUS_PENDING;
        }

        return STATUS_PENDING;
    }

    private String normalizeDocumentType(String value) {
        String type = safe(value);

        if (TYPE_STUDENT_ID.equalsIgnoreCase(type)) {
            return TYPE_STUDENT_ID;
        }

        if (TYPE_MEDICAL.equalsIgnoreCase(type)) {
            return TYPE_MEDICAL;
        }

        if (TYPE_EXAM_ELIGIBILITY.equalsIgnoreCase(type)) {
            return TYPE_EXAM_ELIGIBILITY;
        }

        if (TYPE_OTHER.equalsIgnoreCase(type)) {
            return TYPE_OTHER;
        }

        return TYPE_OTHER;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}