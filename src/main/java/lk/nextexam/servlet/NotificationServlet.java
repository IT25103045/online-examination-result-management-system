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
 * NotificationServlet controls the Notification Center.
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

    private final NotificationDAO notificationDAO = new NotificationDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        request.getRequestDispatcher("/notifications/index.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);

        if (!isAuthenticated(session)) {
            response.sendRedirect(request.getContextPath() + "/login.jsp?error=sessionExpired");
            return;
        }

        String action = FileUtil.clean(request.getParameter("action"));

        if ("markRead".equalsIgnoreCase(action)) {
            markRead(request, response);
            return;
        }

        if ("markAllRead".equalsIgnoreCase(action)) {
            markAllRead(request, response, session);
            return;
        }

        if ("delete".equalsIgnoreCase(action)) {
            deleteNotification(request, response);
            return;
        }

        redirect(request, response, "error", "invalidAction");
    }

    private void markRead(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String notificationId = FileUtil.clean(request.getParameter("notificationId"));

        if (notificationId.isEmpty()) {
            redirect(request, response, "error", "missingNotificationId");
            return;
        }

        boolean success = notificationDAO.markAsRead(getServletContext(), notificationId);

        redirect(request, response, success ? "success" : "error", success ? "markedRead" : "markReadFailed");
    }

    private void markAllRead(HttpServletRequest request,
                             HttpServletResponse response,
                             HttpSession session)
            throws IOException {

        String userId = getSessionValue(session, "userId");
        String userRole = getSessionValue(session, "userRole");

        boolean success = notificationDAO.markAllAsRead(getServletContext(), userId, userRole);

        redirect(request, response, success ? "success" : "error", success ? "allMarkedRead" : "markAllFailed");
    }

    private void deleteNotification(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String notificationId = FileUtil.clean(request.getParameter("notificationId"));

        if (notificationId.isEmpty()) {
            redirect(request, response, "error", "missingNotificationId");
            return;
        }

        boolean success = notificationDAO.deleteNotification(getServletContext(), notificationId);

        redirect(request, response, success ? "success" : "error", success ? "deleted" : "deleteFailed");
    }

    private boolean isAuthenticated(HttpSession session) {
        return session != null
                && session.getAttribute("loggedUser") != null
                && session.getAttribute("userId") != null
                && session.getAttribute("userRole") != null
                && "authenticated".equals(String.valueOf(session.getAttribute("loginStatus")));
    }

    private String getSessionValue(HttpSession session, String key) {
        if (session == null || key == null) {
            return "";
        }

        Object value = session.getAttribute(key);
        return value == null ? "" : String.valueOf(value).trim();
    }

    private void redirect(HttpServletRequest request,
                          HttpServletResponse response,
                          String key,
                          String value)
            throws IOException {

        response.sendRedirect(request.getContextPath()
                + "/notifications?"
                + key
                + "="
                + URLEncoder.encode(value, StandardCharsets.UTF_8));
    }
}