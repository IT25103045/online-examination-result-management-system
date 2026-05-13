package lk.nextexam.dao;

import jakarta.servlet.ServletContext;
import lk.nextexam.model.User;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Professional DAO for user account management.
 *
 * Storage file:
 * users.txt
 *
 * Format:
 * userId|username|password|email|role|status
 *
 * Notes:
 * - Current project still supports plain-text passwords for compatibility.
 * - verifyPassword() is prepared for future BCrypt support.
 */
public class UserDAO {

    private static final String FILE_NAME = "users.txt";

    public List<User> getAllUsers(ServletContext context) {
        List<User> users = new ArrayList<>();
        List<String> lines = FileUtil.readLines(context, FILE_NAME);

        for (String line : lines) {
            User user = User.fromFileString(line);

            if (user != null && !user.getUserId().isEmpty()) {
                users.add(user);
            }
        }

        users.sort(
                Comparator.comparing(User::getRole, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(User::getUserId, String.CASE_INSENSITIVE_ORDER)
        );

        return users;
    }

    public User getUserById(ServletContext context, String userId) {
        String cleanUserId = FileUtil.clean(userId);

        if (cleanUserId.isEmpty()) {
            return null;
        }

        for (User user : getAllUsers(context)) {
            if (user.getUserId().equalsIgnoreCase(cleanUserId)) {
                return user;
            }
        }

        return null;
    }

    public User getUserByUsername(ServletContext context, String username) {
        String cleanUsername = FileUtil.clean(username);

        if (cleanUsername.isEmpty()) {
            return null;
        }

        for (User user : getAllUsers(context)) {
            if (user.getUsername().equalsIgnoreCase(cleanUsername)) {
                return user;
            }
        }

        return null;
    }

    public User getUserByEmail(ServletContext context, String email) {
        String cleanEmail = FileUtil.clean(email);

        if (cleanEmail.isEmpty()) {
            return null;
        }

        for (User user : getAllUsers(context)) {
            if (user.getEmail().equalsIgnoreCase(cleanEmail)) {
                return user;
            }
        }

        return null;
    }

    public User getUserByUsernameOrEmail(ServletContext context, String usernameOrEmail) {
        String value = FileUtil.clean(usernameOrEmail);

        if (value.isEmpty()) {
            return null;
        }

        for (User user : getAllUsers(context)) {
            boolean usernameMatches = user.getUsername().equalsIgnoreCase(value);
            boolean emailMatches = user.getEmail().equalsIgnoreCase(value);

            if (usernameMatches || emailMatches) {
                return user;
            }
        }

        return null;
    }

    public List<User> getUsersByRole(ServletContext context, String role) {
        List<User> selectedUsers = new ArrayList<>();
        String cleanRole = FileUtil.clean(role);

        if (cleanRole.isEmpty()) {
            return selectedUsers;
        }

        for (User user : getAllUsers(context)) {
            if (user.getRole().equalsIgnoreCase(cleanRole)) {
                selectedUsers.add(user);
            }
        }

        selectedUsers.sort(Comparator.comparing(User::getUserId, String.CASE_INSENSITIVE_ORDER));
        return selectedUsers;
    }

    public List<User> getUsersByStatus(ServletContext context, String status) {
        List<User> selectedUsers = new ArrayList<>();
        String cleanStatus = FileUtil.clean(status);

        if (cleanStatus.isEmpty()) {
            return selectedUsers;
        }

        for (User user : getAllUsers(context)) {
            if (user.getStatus().equalsIgnoreCase(cleanStatus)) {
                selectedUsers.add(user);
            }
        }

        selectedUsers.sort(Comparator.comparing(User::getUserId, String.CASE_INSENSITIVE_ORDER));
        return selectedUsers;
    }

    public List<User> getActiveStudents(ServletContext context) {
        List<User> students = new ArrayList<>();

        for (User user : getAllUsers(context)) {
            if (user.isStudent() && user.isActive()) {
                students.add(user);
            }
        }

        students.sort(Comparator.comparing(User::getUserId, String.CASE_INSENSITIVE_ORDER));
        return students;
    }

    public List<User> getActiveLecturers(ServletContext context) {
        List<User> lecturers = new ArrayList<>();

        for (User user : getAllUsers(context)) {
            if (user.isLecturer() && user.isActive()) {
                lecturers.add(user);
            }
        }

        lecturers.sort(Comparator.comparing(User::getUserId, String.CASE_INSENSITIVE_ORDER));
        return lecturers;
    }

    /**
     * Authenticates a user by username/email + password + role.
     */
    public User login(ServletContext context, String usernameOrEmail, String password, String role) {
        String cleanUsernameOrEmail = FileUtil.clean(usernameOrEmail);
        String cleanPassword = password == null ? "" : password.trim();
        String cleanRole = FileUtil.clean(role);

        if (cleanUsernameOrEmail.isEmpty() || cleanPassword.isEmpty() || cleanRole.isEmpty()) {
            return null;
        }

        for (User user : getAllUsers(context)) {
            boolean identityMatches = user.getUsername().equalsIgnoreCase(cleanUsernameOrEmail)
                    || user.getEmail().equalsIgnoreCase(cleanUsernameOrEmail);

            boolean roleMatches = user.getRole().equalsIgnoreCase(cleanRole);
            boolean active = user.canLogin();
            boolean passwordMatches = verifyPassword(cleanPassword, user.getPassword());

            if (identityMatches && roleMatches && active && passwordMatches) {
                return user;
            }
        }

        return null;
    }

    /**
     * Adds a user after validation and duplicate checks.
     */
    public boolean addUser(ServletContext context, User user) {
        if (!isValidForCreate(context, user)) {
            return false;
        }

        return FileUtil.appendLine(context, FILE_NAME, user.toFileString());
    }

    /**
     * Updates a user after validation and duplicate checks.
     */
    public boolean updateUser(ServletContext context, User user) {
        if (!isValidForUpdate(context, user)) {
            return false;
        }

        return FileUtil.updateLineById(context, FILE_NAME, user.getUserId(), user.toFileString());
    }

    public boolean deleteUser(ServletContext context, String userId) {
        String cleanUserId = FileUtil.clean(userId);

        if (cleanUserId.isEmpty()) {
            return false;
        }

        return FileUtil.deleteLineById(context, FILE_NAME, cleanUserId);
    }

    public boolean activateUser(ServletContext context, String userId) {
        User user = getUserById(context, userId);

        if (user == null) {
            return false;
        }

        user.setStatus(User.STATUS_ACTIVE);
        return updateUser(context, user);
    }

    public boolean deactivateUser(ServletContext context, String userId) {
        User user = getUserById(context, userId);

        if (user == null) {
            return false;
        }

        user.setStatus(User.STATUS_INACTIVE);
        return updateUser(context, user);
    }

    public boolean suspendUser(ServletContext context, String userId) {
        User user = getUserById(context, userId);

        if (user == null) {
            return false;
        }

        user.setStatus(User.STATUS_SUSPENDED);
        return updateUser(context, user);
    }

    public boolean isUsernameOrEmailTaken(ServletContext context,
                                          String username,
                                          String email,
                                          String currentUserId) {

        String cleanUsername = FileUtil.clean(username);
        String cleanEmail = FileUtil.clean(email);
        String cleanCurrentUserId = FileUtil.clean(currentUserId);

        if (cleanUsername.isEmpty() && cleanEmail.isEmpty()) {
            return false;
        }

        for (User existingUser : getAllUsers(context)) {
            if (!cleanCurrentUserId.isEmpty()
                    && existingUser.getUserId().equalsIgnoreCase(cleanCurrentUserId)) {
                continue;
            }

            boolean sameUsername = !cleanUsername.isEmpty()
                    && existingUser.getUsername().equalsIgnoreCase(cleanUsername);

            boolean sameEmail = !cleanEmail.isEmpty()
                    && existingUser.getEmail().equalsIgnoreCase(cleanEmail);

            if (sameUsername || sameEmail) {
                return true;
            }
        }

        return false;
    }

    public boolean existsById(ServletContext context, String userId) {
        return FileUtil.existsById(context, FILE_NAME, userId);
    }

    public int countAllUsers(ServletContext context) {
        return getAllUsers(context).size();
    }

    public int countActiveUsers(ServletContext context) {
        return countByStatus(context, User.STATUS_ACTIVE);
    }

    public int countInactiveUsers(ServletContext context) {
        return countByStatus(context, User.STATUS_INACTIVE);
    }

    public int countSuspendedUsers(ServletContext context) {
        return countByStatus(context, User.STATUS_SUSPENDED);
    }

    public int countStudents(ServletContext context) {
        return countByRole(context, User.ROLE_STUDENT);
    }

    public int countLecturers(ServletContext context) {
        return countByRole(context, User.ROLE_LECTURER);
    }

    public int countAdmins(ServletContext context) {
        return countByRole(context, User.ROLE_ADMIN);
    }

    public int countByRole(ServletContext context, String role) {
        int count = 0;
        String cleanRole = FileUtil.clean(role);

        if (cleanRole.isEmpty()) {
            return count;
        }

        for (User user : getAllUsers(context)) {
            if (user.getRole().equalsIgnoreCase(cleanRole)) {
                count++;
            }
        }

        return count;
    }

    public int countByStatus(ServletContext context, String status) {
        int count = 0;
        String cleanStatus = FileUtil.clean(status);

        if (cleanStatus.isEmpty()) {
            return count;
        }

        for (User user : getAllUsers(context)) {
            if (user.getStatus().equalsIgnoreCase(cleanStatus)) {
                count++;
            }
        }

        return count;
    }

    private boolean isValidForCreate(ServletContext context, User user) {
        if (!isUserObjectValid(user)) {
            return false;
        }

        if (FileUtil.existsById(context, FILE_NAME, user.getUserId())) {
            return false;
        }

        return !isUsernameOrEmailTaken(context, user.getUsername(), user.getEmail(), user.getUserId());
    }

    private boolean isValidForUpdate(ServletContext context, User user) {
        if (!isUserObjectValid(user)) {
            return false;
        }

        if (!FileUtil.existsById(context, FILE_NAME, user.getUserId())) {
            return false;
        }

        return !isUsernameOrEmailTaken(context, user.getUsername(), user.getEmail(), user.getUserId());
    }

    private boolean isUserObjectValid(User user) {
        if (user == null) {
            return false;
        }

        if (!user.isCompleteForSave()) {
            return false;
        }

        return isValidEmailFormat(user.getEmail());
    }

    private boolean isValidEmailFormat(String email) {
        String cleanEmail = FileUtil.clean(email);

        if (cleanEmail.isEmpty()) {
            return false;
        }

        return cleanEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    /**
     * Future-ready password verification.
     *
     * Current compatibility:
     * - Plain text passwords still work.
     *
     * Future:
     * - Add BCrypt library and verify hashes here.
     */
    private boolean verifyPassword(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null) {
            return false;
        }

        String raw = rawPassword.trim();
        String stored = storedPassword.trim();

        if (raw.isEmpty() || stored.isEmpty()) {
            return false;
        }

        /*
         * Placeholder for future BCrypt support.
         * Example later:
         * if (stored.startsWith("$2a$") || stored.startsWith("$2b$")) {
         *     return BCrypt.checkpw(raw, stored);
         * }
         */

        return stored.equals(raw);
    }
}