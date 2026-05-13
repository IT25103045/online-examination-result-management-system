package lk.nextexam.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lk.nextexam.dao.FileUtil;
import lk.nextexam.dao.UserDAO;
import lk.nextexam.model.User;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Professional controller for User Management.
 *
 * Supported actions:
 * - GET  /users
 * - POST /users action=add
 * - POST /users action=update
 * - POST /users action=delete
 * - POST /users action=activate
 * - POST /users action=deactivate
 * - POST /users action=status
 *
 * Backward compatibility:
 * - recordId, userId, and id are all accepted for actions that require a user ID.
 */
@WebServlet("/users")
public class UserServlet extends HttpServlet {

    private static final String ACTION_ADD = "add";
    private static final String ACTION_UPDATE = "update";
    private static final String ACTION_DELETE = "delete";
    private static final String ACTION_ACTIVATE = "activate";
    private static final String ACTION_DEACTIVATE = "deactivate";
    private static final String ACTION_STATUS = "status";

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        prepareRequestResponse(request, response);

        request.setAttribute("users", userDAO.getAllUsers(getServletContext()));
        request.getRequestDispatcher("/users/index.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        prepareRequestResponse(request, response);

        String action = FileUtil.clean(request.getParameter("action"));

        if (ACTION_ADD.equalsIgnoreCase(action)) {
            addUser(request, response);
            return;
        }

        if (ACTION_UPDATE.equalsIgnoreCase(action)) {
            updateUser(request, response);
            return;
        }

        if (ACTION_DELETE.equalsIgnoreCase(action)) {
            deleteUser(request, response);
            return;
        }

        if (ACTION_ACTIVATE.equalsIgnoreCase(action)) {
            activateUser(request, response);
            return;
        }

        if (ACTION_DEACTIVATE.equalsIgnoreCase(action)) {
            deactivateUser(request, response);
            return;
        }

        if (ACTION_STATUS.equalsIgnoreCase(action)) {
            updateUserStatus(request, response);
            return;
        }

        redirectToUsers(request, response, "error", "invalidAction");
    }

    private void addUser(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        User user = buildUserFromRequest(request);

        String validationError = validateUser(user, true);

        if (validationError != null) {
            redirectToUsers(request, response, "error", validationError);
            return;
        }

        boolean success = userDAO.addUser(getServletContext(), user);

        if (success) {
            redirectToUsers(request, response, "success", "userAdded");
        } else {
            redirectToUsers(request, response, "error", "userAddFailed");
        }
    }

    private void updateUser(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        User user = buildUserFromRequest(request);

        String validationError = validateUser(user, true);

        if (validationError != null) {
            redirectToUsers(request, response, "error", validationError);
            return;
        }

        boolean success = userDAO.updateUser(getServletContext(), user);

        if (success) {
            redirectToUsers(request, response, "success", "userUpdated");
        } else {
            redirectToUsers(request, response, "error", "userUpdateFailed");
        }
    }

    private void deleteUser(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String userId = getRequestUserId(request);

        if (userId.isEmpty()) {
            redirectToUsers(request, response, "error", "missingUserId");
            return;
        }

        if (isCurrentLoggedUser(request, userId)) {
            redirectToUsers(request, response, "error", "cannotDeleteCurrentUser");
            return;
        }

        boolean success = userDAO.deleteUser(getServletContext(), userId);

        if (success) {
            redirectToUsers(request, response, "success", "userDeleted");
        } else {
            redirectToUsers(request, response, "error", "userDeleteFailed");
        }
    }

    private void activateUser(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String userId = getRequestUserId(request);

        if (userId.isEmpty()) {
            redirectToUsers(request, response, "error", "missingUserId");
            return;
        }

        boolean success = updateStatus(userId, User.STATUS_ACTIVE);

        if (success) {
            redirectToUsers(request, response, "success", "userActivated");
        } else {
            redirectToUsers(request, response, "error", "userStatusUpdateFailed");
        }
    }

    private void deactivateUser(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String userId = getRequestUserId(request);

        if (userId.isEmpty()) {
            redirectToUsers(request, response, "error", "missingUserId");
            return;
        }

        if (isCurrentLoggedUser(request, userId)) {
            redirectToUsers(request, response, "error", "cannotDeactivateCurrentUser");
            return;
        }

        boolean success = updateStatus(userId, User.STATUS_INACTIVE);

        if (success) {
            redirectToUsers(request, response, "success", "userDeactivated");
        } else {
            redirectToUsers(request, response, "error", "userStatusUpdateFailed");
        }
    }

    private void updateUserStatus(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String userId = getRequestUserId(request);
        String status = FileUtil.clean(request.getParameter("status"));

        if (userId.isEmpty()) {
            redirectToUsers(request, response, "error", "missingUserId");
            return;
        }

        if (status.isEmpty()) {
            redirectToUsers(request, response, "error", "missingStatus");
            return;
        }

        if (!isValidStatus(status)) {
            redirectToUsers(request, response, "error", "invalidStatus");
            return;
        }

        if (User.STATUS_INACTIVE.equalsIgnoreCase(status) && isCurrentLoggedUser(request, userId)) {
            redirectToUsers(request, response, "error", "cannotDeactivateCurrentUser");
            return;
        }

        boolean success = updateStatus(userId, status);

        if (success) {
            redirectToUsers(request, response, "success", "userStatusUpdated");
        } else {
            redirectToUsers(request, response, "error", "userStatusUpdateFailed");
        }
    }

    private boolean updateStatus(String userId, String status) {
        User user = userDAO.getUserById(getServletContext(), userId);

        if (user == null) {
            return false;
        }

        user.setStatus(status);

        if (!isValidStatus(user.getStatus())) {
            return false;
        }

        return userDAO.updateUser(getServletContext(), user);
    }

    private User buildUserFromRequest(HttpServletRequest request) {
        return new User(
                FileUtil.clean(request.getParameter("userId")),
                FileUtil.clean(request.getParameter("username")),
                FileUtil.clean(request.getParameter("password")),
                FileUtil.clean(request.getParameter("email")),
                FileUtil.clean(request.getParameter("role")),
                FileUtil.clean(request.getParameter("status"))
        );
    }

    private String validateUser(User user, boolean passwordRequired) {
        if (user == null) {
            return "invalidUser";
        }

        if (user.getUserId().isEmpty()) {
            return "missingUserId";
        }

        if (user.getUsername().isEmpty()) {
            return "missingUsername";
        }

        if (user.getEmail().isEmpty()) {
            return "missingEmail";
        }

        if (!isValidEmail(user.getEmail())) {
            return "invalidEmail";
        }

        if (passwordRequired && user.getPassword().isEmpty()) {
            return "missingPassword";
        }

        if (passwordRequired && user.getPassword().length() < 4) {
            return "weakPassword";
        }

        if (user.getRole().isEmpty()) {
            return "missingRole";
        }

        if (!isValidRole(user.getRole())) {
            return "invalidRole";
        }

        if (user.getStatus().isEmpty()) {
            return "missingStatus";
        }

        if (!isValidStatus(user.getStatus())) {
            return "invalidStatus";
        }

        return null;
    }

    private boolean isValidEmail(String email) {
        String value = FileUtil.clean(email);

        return value.contains("@")
                && value.contains(".")
                && value.length() >= 6
                && !value.startsWith("@")
                && !value.endsWith("@")
                && !value.contains(" ");
    }

    private boolean isValidRole(String role) {
        return User.ROLE_ADMIN.equalsIgnoreCase(role)
                || User.ROLE_LECTURER.equalsIgnoreCase(role)
                || User.ROLE_STUDENT.equalsIgnoreCase(role);
    }

    private boolean isValidStatus(String status) {
        return User.STATUS_ACTIVE.equalsIgnoreCase(status)
                || User.STATUS_INACTIVE.equalsIgnoreCase(status);
    }

    private String getRequestUserId(HttpServletRequest request) {
        return firstNonBlank(
                request.getParameter("recordId"),
                request.getParameter("userId"),
                request.getParameter("id")
        );
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }

        for (String value : values) {
            String cleaned = FileUtil.clean(value);

            if (!cleaned.isEmpty()) {
                return cleaned;
            }
        }

        return "";
    }

    private boolean isCurrentLoggedUser(HttpServletRequest request, String userId) {
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            return false;
        }

        String loggedUserId = String.valueOf(session.getAttribute("userId"));
        return userId.equalsIgnoreCase(loggedUserId);
    }

    private void redirectToUsers(HttpServletRequest request,
                                 HttpServletResponse response,
                                 String messageType,
                                 String messageCode)
            throws IOException {

        response.sendRedirect(
                request.getContextPath()
                        + "/users?"
                        + urlEncode(messageType)
                        + "="
                        + urlEncode(messageCode)
        );
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private void prepareRequestResponse(HttpServletRequest request,
                                        HttpServletResponse response)
            throws IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
    }
}