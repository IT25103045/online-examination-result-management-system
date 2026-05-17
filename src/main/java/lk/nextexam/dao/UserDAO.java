package lk.nextexam.dao;

import jakarta.servlet.ServletContext;
import lk.nextexam.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * UserDAO manages user-related MySQL database operations.
 *
 * MySQL table:
 * users
 *
 * Columns:
 * user_id, username, password, email, role, status, profile_image
 *
 * Notes:
 * - Uses MySQL through DBConnection.
 * - Login now fetches by username/email first, then validates role, status, and password in Java.
 * - This makes debugging login failures easier.
 *
 * Responsible Member:
 * IT25103045 - De Silva H.L.D.C.P.C
 */
public class UserDAO {

    private static final boolean DEBUG_LOGIN = false;

    public List<User> getAllUsers(ServletContext context) {
        List<User> users = new ArrayList<>();

        String sql = "SELECT user_id, username, password, email, role, status, profile_image " +
                "FROM users " +
                "ORDER BY role ASC, user_id ASC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                users.add(mapResultSetToUser(resultSet));
            }

        } catch (SQLException e) {
            System.out.println("USERDAO ERROR -> getAllUsers failed");
            e.printStackTrace();
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

        String sql = "SELECT user_id, username, password, email, role, status, profile_image " +
                "FROM users WHERE LOWER(TRIM(user_id)) = LOWER(TRIM(?)) LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanUserId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToUser(resultSet);
                }
            }

        } catch (SQLException e) {
            System.out.println("USERDAO ERROR -> getUserById failed for " + cleanUserId);
            e.printStackTrace();
        }

        return null;
    }

    public User getUserByUsername(ServletContext context, String username) {
        String cleanUsername = FileUtil.clean(username);

        if (cleanUsername.isEmpty()) {
            return null;
        }

        String sql = "SELECT user_id, username, password, email, role, status, profile_image " +
                "FROM users WHERE LOWER(TRIM(username)) = LOWER(TRIM(?)) LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanUsername);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToUser(resultSet);
                }
            }

        } catch (SQLException e) {
            System.out.println("USERDAO ERROR -> getUserByUsername failed for " + cleanUsername);
            e.printStackTrace();
        }

        return null;
    }

    public User getUserByEmail(ServletContext context, String email) {
        String cleanEmail = FileUtil.clean(email);

        if (cleanEmail.isEmpty()) {
            return null;
        }

        String sql = "SELECT user_id, username, password, email, role, status, profile_image " +
                "FROM users WHERE LOWER(TRIM(email)) = LOWER(TRIM(?)) LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanEmail);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToUser(resultSet);
                }
            }

        } catch (SQLException e) {
            System.out.println("USERDAO ERROR -> getUserByEmail failed for " + cleanEmail);
            e.printStackTrace();
        }

        return null;
    }

    public User getUserByUsernameOrEmail(ServletContext context, String usernameOrEmail) {
        String value = FileUtil.clean(usernameOrEmail);

        if (value.isEmpty()) {
            return null;
        }

        String sql = "SELECT user_id, username, password, email, role, status, profile_image " +
                "FROM users " +
                "WHERE LOWER(TRIM(username)) = LOWER(TRIM(?)) " +
                "OR LOWER(TRIM(email)) = LOWER(TRIM(?)) " +
                "LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, value);
            statement.setString(2, value);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToUser(resultSet);
                }
            }

        } catch (SQLException e) {
            System.out.println("USERDAO ERROR -> getUserByUsernameOrEmail failed for " + value);
            e.printStackTrace();
        }

        return null;
    }

    public List<User> getUsersByRole(ServletContext context, String role) {
        List<User> selectedUsers = new ArrayList<>();
        String cleanRole = normalizeRoleInput(role);

        if (cleanRole.isEmpty()) {
            return selectedUsers;
        }

        String sql = "SELECT user_id, username, password, email, role, status, profile_image " +
                "FROM users WHERE LOWER(TRIM(role)) = LOWER(TRIM(?)) ORDER BY user_id ASC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanRole);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    selectedUsers.add(mapResultSetToUser(resultSet));
                }
            }

        } catch (SQLException e) {
            System.out.println("USERDAO ERROR -> getUsersByRole failed for " + cleanRole);
            e.printStackTrace();
        }

        selectedUsers.sort(Comparator.comparing(User::getUserId, String.CASE_INSENSITIVE_ORDER));
        return selectedUsers;
    }

    public List<User> getUsersByStatus(ServletContext context, String status) {
        List<User> selectedUsers = new ArrayList<>();
        String cleanStatus = normalizeStatusInput(status);

        if (cleanStatus.isEmpty()) {
            return selectedUsers;
        }

        String sql = "SELECT user_id, username, password, email, role, status, profile_image " +
                "FROM users WHERE LOWER(TRIM(status)) = LOWER(TRIM(?)) ORDER BY user_id ASC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanStatus);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    selectedUsers.add(mapResultSetToUser(resultSet));
                }
            }

        } catch (SQLException e) {
            System.out.println("USERDAO ERROR -> getUsersByStatus failed for " + cleanStatus);
            e.printStackTrace();
        }

        selectedUsers.sort(Comparator.comparing(User::getUserId, String.CASE_INSENSITIVE_ORDER));
        return selectedUsers;
    }

    public List<User> getActiveStudents(ServletContext context) {
        return getActiveUsersByRole(User.ROLE_STUDENT);
    }

    public List<User> getActiveLecturers(ServletContext context) {
        return getActiveUsersByRole(User.ROLE_LECTURER);
    }

    private List<User> getActiveUsersByRole(String role) {
        List<User> users = new ArrayList<>();

        String sql = "SELECT user_id, username, password, email, role, status, profile_image " +
                "FROM users " +
                "WHERE LOWER(TRIM(role)) = LOWER(TRIM(?)) " +
                "AND LOWER(TRIM(status)) = LOWER(TRIM(?)) " +
                "ORDER BY user_id ASC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, role);
            statement.setString(2, User.STATUS_ACTIVE);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    users.add(mapResultSetToUser(resultSet));
                }
            }

        } catch (SQLException e) {
            System.out.println("USERDAO ERROR -> getActiveUsersByRole failed for " + role);
            e.printStackTrace();
        }

        users.sort(Comparator.comparing(User::getUserId, String.CASE_INSENSITIVE_ORDER));
        return users;
    }

    /**
     * Authenticates a user by username/email + password + role.
     *
     * Debug-safe flow:
     * 1. Fetch user using username/email only.
     * 2. Validate role in Java.
     * 3. Validate active status.
     * 4. Validate password.
     *
     * This helps identify whether invalid login is caused by DB, role, status, password, or connection.
     */
    public User login(ServletContext context, String usernameOrEmail, String password, String role) {
        String cleanUsernameOrEmail = FileUtil.clean(usernameOrEmail);
        String cleanPassword = password == null ? "" : password.trim();
        String cleanRole = normalizeRoleInput(role);

        debug("LOGIN DEBUG -> input username/email = [" + cleanUsernameOrEmail + "]");
        debug("LOGIN DEBUG -> input role = [" + cleanRole + "]");
        debug("LOGIN DEBUG -> input password length = " + cleanPassword.length());

        if (cleanUsernameOrEmail.isEmpty() || cleanPassword.isEmpty() || cleanRole.isEmpty()) {
            debug("LOGIN DEBUG -> failed because username/password/role is empty");
            return null;
        }

        String sql = "SELECT user_id, username, password, email, role, status, profile_image " +
                "FROM users " +
                "WHERE LOWER(TRIM(username)) = LOWER(TRIM(?)) " +
                "OR LOWER(TRIM(email)) = LOWER(TRIM(?)) " +
                "LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            debug("LOGIN DEBUG -> connected to MySQL successfully");

            statement.setString(1, cleanUsernameOrEmail);
            statement.setString(2, cleanUsernameOrEmail);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    debug("LOGIN DEBUG -> NO USER FOUND for [" + cleanUsernameOrEmail + "]");
                    return null;
                }

                User user = mapResultSetToUser(resultSet);

                String dbRole = normalizeRoleInput(user.getRole());
                String dbStatus = normalizeStatusInput(user.getStatus());

                boolean roleMatches = dbRole.equalsIgnoreCase(cleanRole);
                boolean active = User.STATUS_ACTIVE.equalsIgnoreCase(dbStatus) && user.isValidRole();
                boolean passwordMatches = verifyPassword(cleanPassword, user.getPassword());

                debug("LOGIN DEBUG -> DB userId = [" + user.getUserId() + "]");
                debug("LOGIN DEBUG -> DB username = [" + user.getUsername() + "]");
                debug("LOGIN DEBUG -> DB email = [" + user.getEmail() + "]");
                debug("LOGIN DEBUG -> DB role = [" + dbRole + "]");
                debug("LOGIN DEBUG -> DB status = [" + dbStatus + "]");
                debug("LOGIN DEBUG -> roleMatches = " + roleMatches);
                debug("LOGIN DEBUG -> active = " + active);
                debug("LOGIN DEBUG -> passwordMatches = " + passwordMatches);

                if (roleMatches && active && passwordMatches) {
                    debug("LOGIN DEBUG -> LOGIN SUCCESS");
                    return user;
                }

                debug("LOGIN DEBUG -> LOGIN FAILED AFTER USER FOUND");
            }

        } catch (SQLException e) {
            System.out.println("USERDAO ERROR -> login SQL failed");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("USERDAO ERROR -> login failed unexpectedly");
            e.printStackTrace();
        }

        return null;
    }

    public boolean addUser(ServletContext context, User user) {
        if (!isValidForCreate(context, user)) {
            return false;
        }

        String sql = "INSERT INTO users " +
                "(user_id, username, password, email, role, status, profile_image) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            fillUserStatement(statement, user);
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("USERDAO ERROR -> addUser failed");
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateUser(ServletContext context, User user) {
        if (!isValidForUpdate(context, user)) {
            return false;
        }

        String sql = "UPDATE users SET " +
                "username = ?, " +
                "password = ?, " +
                "email = ?, " +
                "role = ?, " +
                "status = ?, " +
                "profile_image = ? " +
                "WHERE user_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPassword());
            statement.setString(3, user.getEmail());
            statement.setString(4, user.getRole());
            statement.setString(5, user.getStatus());
            statement.setString(6, user.getProfileImage());
            statement.setString(7, user.getUserId());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("USERDAO ERROR -> updateUser failed for " + (user != null ? user.getUserId() : ""));
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteUser(ServletContext context, String userId) {
        String cleanUserId = FileUtil.clean(userId);

        if (cleanUserId.isEmpty()) {
            return false;
        }

        String sql = "DELETE FROM users WHERE user_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanUserId);
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("USERDAO ERROR -> deleteUser failed for " + cleanUserId);
            e.printStackTrace();
            return false;
        }
    }

    public boolean activateUser(ServletContext context, String userId) {
        return updateUserStatus(userId, User.STATUS_ACTIVE);
    }

    public boolean deactivateUser(ServletContext context, String userId) {
        return updateUserStatus(userId, User.STATUS_INACTIVE);
    }

    public boolean suspendUser(ServletContext context, String userId) {
        return updateUserStatus(userId, User.STATUS_SUSPENDED);
    }

    private boolean updateUserStatus(String userId, String status) {
        String cleanUserId = FileUtil.clean(userId);
        String cleanStatus = normalizeStatusInput(status);

        if (cleanUserId.isEmpty() || cleanStatus.isEmpty()) {
            return false;
        }

        String sql = "UPDATE users SET status = ? WHERE user_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanStatus);
            statement.setString(2, cleanUserId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("USERDAO ERROR -> updateUserStatus failed for " + cleanUserId);
            e.printStackTrace();
            return false;
        }
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

        String sql = "SELECT user_id FROM users " +
                "WHERE (LOWER(TRIM(username)) = LOWER(TRIM(?)) " +
                "OR LOWER(TRIM(email)) = LOWER(TRIM(?))) " +
                "AND LOWER(TRIM(user_id)) <> LOWER(TRIM(?)) " +
                "LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanUsername);
            statement.setString(2, cleanEmail);
            statement.setString(3, cleanCurrentUserId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException e) {
            System.out.println("USERDAO ERROR -> isUsernameOrEmailTaken failed");
            e.printStackTrace();
            return true;
        }
    }

    public boolean existsById(ServletContext context, String userId) {
        String cleanUserId = FileUtil.clean(userId);

        if (cleanUserId.isEmpty()) {
            return false;
        }

        String sql = "SELECT user_id FROM users WHERE LOWER(TRIM(user_id)) = LOWER(TRIM(?)) LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanUserId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException e) {
            System.out.println("USERDAO ERROR -> existsById failed for " + cleanUserId);
            e.printStackTrace();
            return false;
        }
    }

    public int countAllUsers(ServletContext context) {
        return countByQuery("SELECT COUNT(*) FROM users");
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
        String cleanRole = normalizeRoleInput(role);

        if (cleanRole.isEmpty()) {
            return 0;
        }

        String sql = "SELECT COUNT(*) FROM users WHERE LOWER(TRIM(role)) = LOWER(TRIM(?))";
        return countBySingleParameterQuery(sql, cleanRole);
    }

    public int countByStatus(ServletContext context, String status) {
        String cleanStatus = normalizeStatusInput(status);

        if (cleanStatus.isEmpty()) {
            return 0;
        }

        String sql = "SELECT COUNT(*) FROM users WHERE LOWER(TRIM(status)) = LOWER(TRIM(?))";
        return countBySingleParameterQuery(sql, cleanStatus);
    }

    public boolean updateProfileImage(ServletContext context, String userId, String profileImagePath) {
        String cleanUserId = FileUtil.clean(userId);
        String cleanProfileImagePath = FileUtil.clean(profileImagePath);

        if (cleanUserId.isEmpty()) {
            return false;
        }

        String sql = "UPDATE users SET profile_image = ? WHERE user_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanProfileImagePath);
            statement.setString(2, cleanUserId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("USERDAO ERROR -> updateProfileImage failed for " + cleanUserId);
            e.printStackTrace();
            return false;
        }
    }

    private int countByQuery(String sql) {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

        } catch (SQLException e) {
            System.out.println("USERDAO ERROR -> countByQuery failed");
            e.printStackTrace();
        }

        return 0;
    }

    private int countBySingleParameterQuery(String sql, String parameter) {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, parameter);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }

        } catch (SQLException e) {
            System.out.println("USERDAO ERROR -> countBySingleParameterQuery failed");
            e.printStackTrace();
        }

        return 0;
    }

    private boolean isValidForCreate(ServletContext context, User user) {
        if (!isUserObjectValid(user)) {
            return false;
        }

        if (existsById(context, user.getUserId())) {
            return false;
        }

        return !isUsernameOrEmailTaken(context, user.getUsername(), user.getEmail(), user.getUserId());
    }

    private boolean isValidForUpdate(ServletContext context, User user) {
        if (!isUserObjectValid(user)) {
            return false;
        }

        if (!existsById(context, user.getUserId())) {
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

    private void fillUserStatement(PreparedStatement statement, User user) throws SQLException {
        statement.setString(1, user.getUserId());
        statement.setString(2, user.getUsername());
        statement.setString(3, user.getPassword());
        statement.setString(4, user.getEmail());
        statement.setString(5, normalizeRoleInput(user.getRole()));
        statement.setString(6, normalizeStatusInput(user.getStatus()));
        statement.setString(7, user.getProfileImage());
    }

    private User mapResultSetToUser(ResultSet resultSet) throws SQLException {
        return new User(
                safe(resultSet.getString("user_id")),
                safe(resultSet.getString("username")),
                safe(resultSet.getString("password")),
                safe(resultSet.getString("email")),
                normalizeRoleInput(resultSet.getString("role")),
                normalizeStatusInput(resultSet.getString("status")),
                safe(resultSet.getString("profile_image"))
        );
    }

    private boolean verifyPassword(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null) {
            return false;
        }

        String raw = rawPassword.trim();
        String stored = storedPassword.trim();

        if (raw.isEmpty() || stored.isEmpty()) {
            return false;
        }

        return stored.equals(raw);
    }

    private String normalizeRoleInput(String value) {
        String roleValue = safe(value);

        if (roleValue.equalsIgnoreCase(User.ROLE_ADMIN)
                || roleValue.equalsIgnoreCase("admin")
                || roleValue.equalsIgnoreCase("administrator")
                || roleValue.equalsIgnoreCase("role_admin")) {
            return User.ROLE_ADMIN;
        }

        if (roleValue.equalsIgnoreCase(User.ROLE_LECTURER)
                || roleValue.equalsIgnoreCase("lecturer")
                || roleValue.equalsIgnoreCase("teacher")
                || roleValue.equalsIgnoreCase("role_lecturer")) {
            return User.ROLE_LECTURER;
        }

        if (roleValue.equalsIgnoreCase(User.ROLE_STUDENT)
                || roleValue.equalsIgnoreCase("student")
                || roleValue.equalsIgnoreCase("role_student")) {
            return User.ROLE_STUDENT;
        }

        return roleValue;
    }

    private String normalizeStatusInput(String value) {
        String statusValue = safe(value);

        if (statusValue.equalsIgnoreCase(User.STATUS_ACTIVE)
                || statusValue.equalsIgnoreCase("active")
                || statusValue.equalsIgnoreCase("enabled")) {
            return User.STATUS_ACTIVE;
        }

        if (statusValue.equalsIgnoreCase(User.STATUS_INACTIVE)
                || statusValue.equalsIgnoreCase("inactive")
                || statusValue.equalsIgnoreCase("disabled")) {
            return User.STATUS_INACTIVE;
        }

        if (statusValue.equalsIgnoreCase(User.STATUS_SUSPENDED)
                || statusValue.equalsIgnoreCase("suspended")
                || statusValue.equalsIgnoreCase("blocked")) {
            return User.STATUS_SUSPENDED;
        }

        return statusValue;
    }

    private void debug(String message) {
        if (DEBUG_LOGIN) {
            System.out.println(message);
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}