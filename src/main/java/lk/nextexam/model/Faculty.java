package lk.nextexam.model;

import lk.nextexam.dao.FileUtil;

public class Faculty {
    private String facultyId;
    private String facultyName;
    private String deanName;
    private String contactEmail;
    private String status;

    public Faculty() {
    }

    public Faculty(String facultyId, String facultyName, String deanName, String contactEmail, String status) {
        this.facultyId = facultyId;
        this.facultyName = facultyName;
        this.deanName = deanName;
        this.contactEmail = contactEmail;
        this.status = status;
    }

    public String getFacultyId() {
        return facultyId;
    }

    public void setFacultyId(String facultyId) {
        this.facultyId = facultyId;
    }

    public String getFacultyName() {
        return facultyName;
    }

    public void setFacultyName(String facultyName) {
        this.facultyName = facultyName;
    }

    public String getDeanName() {
        return deanName;
    }

    public void setDeanName(String deanName) {
        this.deanName = deanName;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String toFileString() {
        return FileUtil.clean(facultyId) + "|"
                + FileUtil.clean(facultyName) + "|"
                + FileUtil.clean(deanName) + "|"
                + FileUtil.clean(contactEmail) + "|"
                + FileUtil.clean(status);
    }

    public static Faculty fromFileString(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }

        String[] data = line.split("\\|", -1);

        if (data.length < 5) {
            return null;
        }

        return new Faculty(
                data[0],
                data[1],
                data[2],
                data[3],
                data[4]
        );
    }
}