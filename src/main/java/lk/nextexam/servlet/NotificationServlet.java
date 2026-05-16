package lk.nextexam.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lk.nextexam.dao.FileUtil;
import lk.nextexam.dao.NotificationDAO;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * NotificationServlet controls the notification center workflow.
 *
 * URL:
 * /notifications
 *
 * Supported actions:
 * - markRead
 * - markAllRead
 * - delete
 *
 * Responsible Member:
 * IT25103045 - De Silva H.L.D.C.P.C
 */
@WebServlet("/notifications")
public class NotificationServlet extends HttpServlet {

    private static final String ACTION_MARK_READ = "markRead";
    private static final String ACTION_MARK_ALL_READ = "markAllRead";
    private static final String ACTION_DELETE = "delete";

    private final NotificationDAO notificationDAO = new NotificationDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        prepareRequestResponse(request, response);

        HttpSession session = request.getSession(false);

        if (!isAuthenticated(session)) {
            response.sendRedirect(request.getContextPath() + "/login.jsp?error=sessionExpired");
            return;
        }

        request.getRequestDispatcher("/notifications/index.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        prepareRequestResponse(request, response);

        HttpSession session = request.getSession(false);

        if (!isAuthenticated(session)) {
            response.sendRedirect(request.getContextPath() + "/login.jsp?error=sessionExpired");
            return;
        }

        String action = FileUtil.clean(request.getParameter("action"));
        String userId = getSessionValue(session, "userId");
        String role = getSessionValue(session, "userRole");

        if (ACTION_MARK_READ.equalsIgnoreCase(action)) {
            String notificationId = FileUtil.clean(request.getParameter("notificationId"));
            boolean updated = notificationDAO.markAsRead(getServletContext(), notificationId);
            redirectToNotifications(request, response, updated ? "success" : "error", updated ? "markedRead" : "markReadFailed");
            return;
        }

        if (ACTION_MARK_ALL_READ.equalsIgnoreCase(action)) {
            notificationDAO.markAllAsReadForUser(getServletContext(), userId, role);
            redirectToNotifications(request, response, "success", "allMarkedRead");
            return;
        }

        if (ACTION_DELETE.equalsIgnoreCase(action)) {
            String notificationId = FileUtil.clean(request.getParameter("notificationId"));
            boolean deleted = notificationDAO.deleteNotification(getServletContext(), notificationId);
            redirectToNotifications(request, response, deleted ? "success" : "error", deleted ? "deleted" : "deleteFailed");
            return;
        }

        redirectToNotifications(request, response, "error", "invalidAction");
    }

    private boolean isAuthenticated(HttpSession session) {
        if (session == null) {
            return false;
        }

        Object loggedUser = session.getAttribute("loggedUser");
        Object loginStatus = session.getAttribute("loginStatus");
        Object userRole = session.getAttribute("userRole");
        Object userId = session.getAttribute("userId");

        return loggedUser != null
                && userId != null
                && userRole != null
                && "authenticated".equals(String.valueOf(loginStatus));
    }

    private String getSessionValue(HttpSession session, String key) {
        if (session == null || key == null) {
            return "";
        }

        Object value = session.getAttribute(key);
        return value == null ? "" : String.valueOf(value).trim();
    }

    private void redirectToNotifications(HttpServletRequest request,
                                         HttpServletResponse response,
                                         String type,
                                         String code)
            throws IOException {

        response.sendRedirect(request.getContextPath()
                + "/notifications?"
                + urlEncode(type)
                + "="
                + urlEncode(code));
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private void prepareRequestResponse(HttpServletRequest request,
                                        HttpServletResponse response)
            throws IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, private");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
    }
}