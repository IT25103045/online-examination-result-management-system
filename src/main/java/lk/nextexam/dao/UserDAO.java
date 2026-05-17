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
 * Responsible Member:
 * IT25103045 - De Silva H.L.D.C.P.C
 */
public class UserDAO {

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
                "FROM users WHERE LOWER(user_id) = LOWER(?) LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanUserId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToUser(resultSet);
                }
            }

        } catch (SQLException e) {
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
                "FROM users WHERE LOWER(username) = LOWER(?) LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanUsername);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToUser(resultSet);
                }
            }

        } catch (SQLException e) {
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
                "FROM users WHERE LOWER(email) = LOWER(?) LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanEmail);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToUser(resultSet);
                }
            }

        } catch (SQLException e) {
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
                "WHERE LOWER(username) = LOWER(?) OR LOWER(email) = LOWER(?) " +
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
            e.printStackTrace();
        }

        return null;
    }

    public List<User> getUsersByRole(ServletContext context, String role) {
        List<User> selectedUsers = new ArrayList<>();
        String cleanRole = FileUtil.clean(role);

        if (cleanRole.isEmpty()) {
            return selectedUsers;
        }

        String sql = "SELECT user_id, username, password, email, role, status, profile_image " +
                "FROM users WHERE LOWER(role) = LOWER(?) ORDER BY user_id ASC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanRole);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    selectedUsers.add(mapResultSetToUser(resultSet));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
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

        String sql = "SELECT user_id, username, password, email, role, status, profile_image " +
                "FROM users WHERE LOWER(status) = LOWER(?) ORDER BY user_id ASC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanStatus);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    selectedUsers.add(mapResultSetToUser(resultSet));
                }
            }

        } catch (SQLException e) {
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
                "WHERE LOWER(role) = LOWER(?) AND LOWER(status) = LOWER(?) " +
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
            e.printStackTrace();
        }

        users.sort(Comparator.comparing(User::getUserId, String.CASE_INSENSITIVE_ORDER));
        return users;
    }

    /**
     * Authenticates a user by username/email + password + role.
     *
     * Current compatibility:
     * - Plain text passwords still work.
     *
     * Future:
     * - Add BCrypt and verify hashed passwords inside verifyPassword().
     */
    public User login(ServletContext context, String usernameOrEmail, String password, String role) {
        String cleanUsernameOrEmail = FileUtil.clean(usernameOrEmail);
        String cleanPassword = password == null ? "" : password.trim();
        String cleanRole = FileUtil.clean(role);

        if (cleanUsernameOrEmail.isEmpty() || cleanPassword.isEmpty() || cleanRole.isEmpty()) {
            return null;
        }

        String sql = "SELECT user_id, username, password, email, role, status, profile_image " +
                "FROM users " +
                "WHERE (LOWER(username) = LOWER(?) OR LOWER(email) = LOWER(?)) " +
                "AND LOWER(role) = LOWER(?) " +
                "LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanUsernameOrEmail);
            statement.setString(2, cleanUsernameOrEmail);
            statement.setString(3, cleanRole);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    User user = mapResultSetToUser(resultSet);

                    boolean active = user.canLogin();
                    boolean passwordMatches = verifyPassword(cleanPassword, user.getPassword());

                    if (active && passwordMatches) {
                        return user;
                    }
                }
            }

        } catch (SQLException e) {
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
        String cleanStatus = FileUtil.clean(status);

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
                "WHERE (LOWER(username) = LOWER(?) OR LOWER(email) = LOWER(?)) " +
                "AND LOWER(user_id) <> LOWER(?) " +
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
            e.printStackTrace();
            return true;
        }
    }

    public boolean existsById(ServletContext context, String userId) {
        String cleanUserId = FileUtil.clean(userId);

        if (cleanUserId.isEmpty()) {
            return false;
        }

        String sql = "SELECT user_id FROM users WHERE LOWER(user_id) = LOWER(?) LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cleanUserId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException e) {
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
        String cleanRole = FileUtil.clean(role);

        if (cleanRole.isEmpty()) {
            return 0;
        }

        String sql = "SELECT COUNT(*) FROM users WHERE LOWER(role) = LOWER(?)";
        return countBySingleParameterQuery(sql, cleanRole);
    }

    public int countByStatus(ServletContext context, String status) {
        String cleanStatus = FileUtil.clean(status);

        if (cleanStatus.isEmpty()) {
            return 0;
        }

        String sql = "SELECT COUNT(*) FROM users WHERE LOWER(status) = LOWER(?)";
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
        statement.setString(5, user.getRole());
        statement.setString(6, user.getStatus());
        statement.setString(7, user.getProfileImage());
    }

    private User mapResultSetToUser(ResultSet resultSet) throws SQLException {
        return new User(
                safe(resultSet.getString("user_id")),
                safe(resultSet.getString("username")),
                safe(resultSet.getString("password")),
                safe(resultSet.getString("email")),
                safe(resultSet.getString("role")),
                safe(resultSet.getString("status")),
                safe(resultSet.getString("profile_image"))
        );
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

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}