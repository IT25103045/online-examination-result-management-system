package lk.nextexam.model;

import lk.nextexam.dao.FileUtil;

/**
 * User model represents a system user in Nextexam.
 *
 * This class is used to store common user details such as user ID,
 * username, password, role, and account status. It demonstrates
 * encapsulation by keeping fields private and exposing controlled
 * access through getter and setter methods.
 *
 * Responsible Member:
 * IT25103045 - De Silva H.L.D.C.P.C
 */
public class User {

    public static final String ROLE_ADMIN = "Admin";
    public static final String ROLE_LECTURER = "Lecturer";
    public static final String ROLE_STUDENT = "Student";

    public static final String STATUS_ACTIVE = "Active";
    public static final String STATUS_INACTIVE = "Inactive";
    public static final String STATUS_SUSPENDED = "Suspended";

    private String userId;
    private String username;
    private String password;
    private String email;
    private String role;
    private String status;

    public User() {
    }

    public User(String userId,
                String username,
                String password,
                String email,
                String role,
                String status) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
        this.status = status;
    }

    public String getUserId() {
        return safe(userId);
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return safe(username);
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return safe(password);
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return safe(email);
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return normalizeRole(role);
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return normalizeStatus(status);
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isActive() {
        return STATUS_ACTIVE.equalsIgnoreCase(getStatus());
    }

    public boolean isInactive() {
        return STATUS_INACTIVE.equalsIgnoreCase(getStatus());
    }

    public boolean isSuspended() {
        return STATUS_SUSPENDED.equalsIgnoreCase(getStatus());
    }

    public boolean isAdmin() {
        return ROLE_ADMIN.equalsIgnoreCase(getRole());
    }

    public boolean isLecturer() {
        return ROLE_LECTURER.equalsIgnoreCase(getRole());
    }

    public boolean isStudent() {
        return ROLE_STUDENT.equalsIgnoreCase(getRole());
    }

    public boolean hasRole(String expectedRole) {
        return expectedRole != null && getRole().equalsIgnoreCase(expectedRole.trim());
    }

    public boolean canLogin() {
        return isActive() && isValidRole();
    }

    public boolean canManageUsers() {
        return isAdmin();
    }

    public boolean canManageAcademicData() {
        return isAdmin() || isLecturer();
    }

    public boolean canManageExams() {
        return isAdmin() || isLecturer();
    }

    public boolean canAttemptExam() {
        return isStudent() && isActive();
    }

    public boolean isValidRole() {
        return isAdmin() || isLecturer() || isStudent();
    }

    public boolean isValidStatus() {
        return isActive() || isInactive() || isSuspended();
    }

    public boolean hasEmail() {
        return !getEmail().isEmpty();
    }

    public boolean hasUsername() {
        return !getUsername().isEmpty();
    }

    public boolean hasPassword() {
        return !getPassword().isEmpty();
    }

    public String getDisplayName() {
        if (!getUsername().isEmpty()) {
            return getUsername();
        }

        if (!getEmail().isEmpty()) {
            return getEmail();
        }

        if (!getUserId().isEmpty()) {
            return getUserId();
        }

        return "User";
    }

    public String getInitials() {
        String displayName = getDisplayName();

        if (displayName.trim().isEmpty()) {
            return "U";
        }

        String[] parts = displayName.trim().split("\\s+");

        if (parts.length == 1) {
            return parts[0].substring(0, 1).toUpperCase();
        }

        return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
    }

    /**
     * Future-ready check for BCrypt-like hashes.
     * Existing project can still use plain-text passwords until UserDAO is upgraded.
     */
    public boolean isPasswordHash() {
        String value = getPassword();

        return value.startsWith("$2a$")
                || value.startsWith("$2b$")
                || value.startsWith("$2y$");
    }

    /**
     * Basic model validation for create/update workflows.
     */
    public boolean isCompleteForSave() {
        return !getUserId().isEmpty()
                && !getUsername().isEmpty()
                && !getPassword().isEmpty()
                && !getEmail().isEmpty()
                && isValidRole()
                && isValidStatus();
    }

    /**
     * Stores user in pipe-separated text-file format.
     */
    public String toFileString() {
        return FileUtil.clean(getUserId()) + "|"
                + FileUtil.clean(getUsername()) + "|"
                + FileUtil.clean(getPassword()) + "|"
                + FileUtil.clean(getEmail()) + "|"
                + FileUtil.clean(getRole()) + "|"
                + FileUtil.clean(getStatus());
    }

    public static User fromFileString(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }

        String[] data = FileUtil.splitRecord(line);

        if (data.length < 6) {
            return null;
        }

        return new User(
                data[0],
                data[1],
                data[2],
                data[3],
                data[4],
                data[5]
        );
    }

    private String normalizeRole(String value) {
        String roleValue = safe(value);

        if (ROLE_ADMIN.equalsIgnoreCase(roleValue)) {
            return ROLE_ADMIN;
        }

        if (ROLE_LECTURER.equalsIgnoreCase(roleValue)) {
            return ROLE_LECTURER;
        }

        if (ROLE_STUDENT.equalsIgnoreCase(roleValue)) {
            return ROLE_STUDENT;
        }

        return roleValue;
    }

    private String normalizeStatus(String value) {
        String statusValue = safe(value);

        if (STATUS_ACTIVE.equalsIgnoreCase(statusValue)) {
            return STATUS_ACTIVE;
        }

        if (STATUS_INACTIVE.equalsIgnoreCase(statusValue)) {
            return STATUS_INACTIVE;
        }

        if (STATUS_SUSPENDED.equalsIgnoreCase(statusValue)) {
            return STATUS_SUSPENDED;
        }

        return statusValue;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}