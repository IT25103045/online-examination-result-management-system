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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * LoginServlet handles user authentication for the Nextexam system.
 *
 * This servlet validates user credentials, creates a user session,
 * and redirects users to the correct dashboard based on their role.
 *
 * Responsible Member:
 * IT25103045 - De Silva H.L.D.C.P.C
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final int SESSION_TIMEOUT_SECONDS = 30 * 60;

    private static final String ROLE_ADMIN = "Admin";
    private static final String ROLE_LECTURER = "Lecturer";
    private static final String ROLE_STUDENT = "Student";

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String username = FileUtil.clean(request.getParameter("username"));
        String password = FileUtil.clean(request.getParameter("password"));
        String role = FileUtil.clean(request.getParameter("role"));

        if (FileUtil.isBlank(username) || FileUtil.isBlank(password) || FileUtil.isBlank(role)) {
            redirectToLogin(request, response, "missing");
            return;
        }

        if (!isValidRole(role)) {
            redirectToLogin(request, response, "invalidRole");
            return;
        }

        User user = userDAO.login(getServletContext(), username, password, role);

        if (user == null) {
            redirectToLogin(request, response, "invalid");
            return;
        }

        if (!user.isActive()) {
            redirectToLogin(request, response, "inactive");
            return;
        }

        createAuthenticatedSession(request, user);

        response.sendRedirect(request.getContextPath() + getRoleRedirectPath(user));
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);

        if (isAlreadyAuthenticated(session)) {
            String userRole = getSessionValue(session, "userRole");
            response.sendRedirect(request.getContextPath() + getRoleRedirectPath(userRole));
            return;
        }

        response.sendRedirect(request.getContextPath() + "/login.jsp");
    }

    private void createAuthenticatedSession(HttpServletRequest request, User user) {
        HttpSession oldSession = request.getSession(false);

        if (oldSession != null) {
            oldSession.invalidate();
        }

        HttpSession session = request.getSession(true);

        session.setAttribute("loggedUser", user);
        session.setAttribute("loginStatus", "authenticated");

        session.setAttribute("userId", user.getUserId());
        session.setAttribute("username", user.getUsername());
        session.setAttribute("displayName", user.getDisplayName());
        session.setAttribute("userEmail", user.getEmail());
        session.setAttribute("userRole", user.getRole());
        session.setAttribute("userStatus", user.getStatus());

        session.setAttribute("loginTime", LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        session.setMaxInactiveInterval(SESSION_TIMEOUT_SECONDS);
    }

    private boolean isAlreadyAuthenticated(HttpSession session) {
        return session != null
                && session.getAttribute("loggedUser") != null
                && session.getAttribute("userRole") != null
                && "authenticated".equals(String.valueOf(session.getAttribute("loginStatus")));
    }

    private String getRoleRedirectPath(User user) {
        if (user == null) {
            return "/login.jsp";
        }

        return getRoleRedirectPath(user.getRole());
    }

    private String getRoleRedirectPath(String role) {
        if (ROLE_STUDENT.equalsIgnoreCase(role)) {
            return "/my-exams?login=success";
        }

        if (ROLE_ADMIN.equalsIgnoreCase(role) || ROLE_LECTURER.equalsIgnoreCase(role)) {
            return "/dashboard.jsp?login=success";
        }

        return "/dashboard.jsp?login=success";
    }

    private boolean isValidRole(String role) {
        return ROLE_ADMIN.equalsIgnoreCase(role)
                || ROLE_LECTURER.equalsIgnoreCase(role)
                || ROLE_STUDENT.equalsIgnoreCase(role);
    }

    private void redirectToLogin(HttpServletRequest request,
                                 HttpServletResponse response,
                                 String errorCode)
            throws IOException {

        response.sendRedirect(request.getContextPath()
                + "/login.jsp?error="
                + URLEncoder.encode(errorCode, StandardCharsets.UTF_8));
    }

    private String getSessionValue(HttpSession session, String key) {
        if (session == null || key == null) {
            return "";
        }

        Object value = session.getAttribute(key);
        return value == null ? "" : String.valueOf(value).trim();
    }
}