package lk.nextexam.model;

import lk.nextexam.dao.FileUtil;

/**
 * Professional Student model for NextExamLK.
 *
 * Storage format:
 * studentId|name|email|course|batch|contact|examStatus
 */
public class Student {

    public static final String STATUS_ELIGIBLE = "Eligible";
    public static final String STATUS_PENDING = "Pending";
    public static final String STATUS_BLOCKED = "Blocked";

    public static final String BATCH_Y1S1 = "Y1S1";
    public static final String BATCH_Y1S2 = "Y1S2";
    public static final String BATCH_Y2S1 = "Y2S1";
    public static final String BATCH_Y2S2 = "Y2S2";
    public static final String BATCH_Y3S1 = "Y3S1";
    public static final String BATCH_Y3S2 = "Y3S2";
    public static final String BATCH_Y4S1 = "Y4S1";
    public static final String BATCH_Y4S2 = "Y4S2";

    private String studentId;
    private String name;
    private String email;
    private String course;
    private String batch;
    private String contact;
    private String examStatus;

    public Student() {
    }

    public Student(String studentId,
                   String name,
                   String email,
                   String course,
                   String batch,
                   String contact,
                   String examStatus) {
        this.studentId = studentId;
        this.name = name;
        this.email = email;
        this.course = course;
        this.batch = batch;
        this.contact = contact;
        this.examStatus = examStatus;
    }

    public String getStudentId() {
        return safe(studentId);
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getName() {
        return safe(name);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return safe(email);
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCourse() {
        return safe(course);
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getBatch() {
        return normalizeBatch(batch);
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }

    public String getContact() {
        return safe(contact);
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getExamStatus() {
        return normalizeExamStatus(examStatus);
    }

    public void setExamStatus(String examStatus) {
        this.examStatus = examStatus;
    }

    public boolean isEligible() {
        return STATUS_ELIGIBLE.equalsIgnoreCase(getExamStatus());
    }

    public boolean isPending() {
        return STATUS_PENDING.equalsIgnoreCase(getExamStatus());
    }

    public boolean isBlocked() {
        return STATUS_BLOCKED.equalsIgnoreCase(getExamStatus());
    }

    public boolean canAttemptExam() {
        return isEligible();
    }

    public boolean needsReview() {
        return isPending();
    }

    public boolean isValidBatch() {
        String value = getBatch();

        return BATCH_Y1S1.equalsIgnoreCase(value)
                || BATCH_Y1S2.equalsIgnoreCase(value)
                || BATCH_Y2S1.equalsIgnoreCase(value)
                || BATCH_Y2S2.equalsIgnoreCase(value)
                || BATCH_Y3S1.equalsIgnoreCase(value)
                || BATCH_Y3S2.equalsIgnoreCase(value)
                || BATCH_Y4S1.equalsIgnoreCase(value)
                || BATCH_Y4S2.equalsIgnoreCase(value);
    }

    public boolean isValidExamStatus() {
        return STATUS_ELIGIBLE.equalsIgnoreCase(getExamStatus())
                || STATUS_PENDING.equalsIgnoreCase(getExamStatus())
                || STATUS_BLOCKED.equalsIgnoreCase(getExamStatus());
    }

    public boolean isValidEmail() {
        String value = getEmail();

        return value.contains("@")
                && value.contains(".")
                && value.length() >= 6
                && !value.contains(" ");
    }

    public boolean isValidContact() {
        String value = getContact();

        if (value.isEmpty()) {
            return false;
        }

        String numbersOnly = value.replaceAll("[^0-9]", "");
        return numbersOnly.length() >= 9 && numbersOnly.length() <= 15;
    }

    public boolean isCompleteForSave() {
        return !getStudentId().isEmpty()
                && !getName().isEmpty()
                && !getEmail().isEmpty()
                && isValidEmail()
                && !getCourse().isEmpty()
                && isValidBatch()
                && isValidContact()
                && isValidExamStatus();
    }

    public String getStatusBadgeClass() {
        if (isEligible()) {
            return "badge-soft-success";
        }

        if (isPending()) {
            return "badge-soft-warning";
        }

        if (isBlocked()) {
            return "badge-soft-danger";
        }

        return "badge-soft-secondary";
    }

    public String getBatchBadgeClass() {
        if (getBatch().startsWith("Y1")) {
            return "badge-soft-primary";
        }

        if (getBatch().startsWith("Y2")) {
            return "badge-soft-info";
        }

        if (getBatch().startsWith("Y3")) {
            return "badge-soft-warning";
        }

        if (getBatch().startsWith("Y4")) {
            return "badge-soft-success";
        }

        return "badge-soft-secondary";
    }

    public String getReadinessLabel() {
        if (isEligible()) {
            return "Ready";
        }

        if (isPending()) {
            return "Pending Review";
        }

        if (isBlocked()) {
            return "Restricted";
        }

        return "Unknown";
    }

    public String getReadinessProgress() {
        if (isEligible()) {
            return "100";
        }

        if (isPending()) {
            return "55";
        }

        if (isBlocked()) {
            return "15";
        }

        return "50";
    }

    public String getReadinessProgressClass() {
        if (isEligible()) {
            return "bg-success";
        }

        if (isPending()) {
            return "bg-warning";
        }

        if (isBlocked()) {
            return "bg-danger";
        }

        return "bg-secondary";
    }

    public String getAcademicYearLabel() {
        String value = getBatch();

        if (value.startsWith("Y1")) {
            return "Year 1";
        }

        if (value.startsWith("Y2")) {
            return "Year 2";
        }

        if (value.startsWith("Y3")) {
            return "Year 3";
        }

        if (value.startsWith("Y4")) {
            return "Year 4";
        }

        return "Unknown Year";
    }

    public String getSemesterLabel() {
        String value = getBatch();

        if (value.endsWith("S1")) {
            return "Semester 1";
        }

        if (value.endsWith("S2")) {
            return "Semester 2";
        }

        return "Unknown Semester";
    }

    public String getDisplayName() {
        if (!getName().isEmpty()) {
            return getName();
        }

        if (!getEmail().isEmpty()) {
            return getEmail();
        }

        return getStudentId().isEmpty() ? "Student" : getStudentId();
    }

    public String getProfileSummary() {
        return getStudentId() + " · " + getBatch() + " · " + getCourse();
    }

    public String toFileString() {
        return FileUtil.clean(getStudentId()) + "|"
                + FileUtil.clean(getName()) + "|"
                + FileUtil.clean(getEmail()) + "|"
                + FileUtil.clean(getCourse()) + "|"
                + FileUtil.clean(getBatch()) + "|"
                + FileUtil.clean(getContact()) + "|"
                + FileUtil.clean(getExamStatus());
    }

    public static Student fromFileString(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }

        String[] data = FileUtil.splitRecord(line);

        if (data.length < 7) {
            return null;
        }

        return new Student(
                data[0],
                data[1],
                data[2],
                data[3],
                data[4],
                data[5],
                data[6]
        );
    }

    private String normalizeBatch(String value) {
        String batchValue = safe(value).toUpperCase();

        if (BATCH_Y1S1.equalsIgnoreCase(batchValue)) {
            return BATCH_Y1S1;
        }

        if (BATCH_Y1S2.equalsIgnoreCase(batchValue)) {
            return BATCH_Y1S2;
        }

        if (BATCH_Y2S1.equalsIgnoreCase(batchValue)) {
            return BATCH_Y2S1;
        }

        if (BATCH_Y2S2.equalsIgnoreCase(batchValue)) {
            return BATCH_Y2S2;
        }

        if (BATCH_Y3S1.equalsIgnoreCase(batchValue)) {
            return BATCH_Y3S1;
        }

        if (BATCH_Y3S2.equalsIgnoreCase(batchValue)) {
            return BATCH_Y3S2;
        }

        if (BATCH_Y4S1.equalsIgnoreCase(batchValue)) {
            return BATCH_Y4S1;
        }

        if (BATCH_Y4S2.equalsIgnoreCase(batchValue)) {
            return BATCH_Y4S2;
        }

        return batchValue;
    }

    private String normalizeExamStatus(String value) {
        String statusValue = safe(value);

        if (STATUS_ELIGIBLE.equalsIgnoreCase(statusValue)) {
            return STATUS_ELIGIBLE;
        }

        if (STATUS_PENDING.equalsIgnoreCase(statusValue)) {
            return STATUS_PENDING;
        }

        if (STATUS_BLOCKED.equalsIgnoreCase(statusValue)) {
            return STATUS_BLOCKED;
        }

        return statusValue;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}