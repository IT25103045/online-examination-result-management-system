package lk.nextexam.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lk.nextexam.dao.AuditLogDAO;
import lk.nextexam.dao.FileUtil;
import lk.nextexam.dao.NotificationDAO;
import lk.nextexam.model.AuditLog;

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
 * Audit Logging:
 * - Access denied attempts
 * - Mark notification as read
 * - Mark all notifications as read
 * - Delete notification
 * - Invalid notification actions
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
    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        prepareRequestResponse(request, response);

        HttpSession session = request.getSession(false);

        if (!isAuthenticated(session)) {
            auditLogDAO.logAction(
                    getServletContext(),
                    request,
                    "ACCESS_DENIED",
                    AuditLog.MODULE_NOTIFICATIONS,
                    "Unauthenticated user attempted to access notification center.",
                    AuditLog.STATUS_DENIED
            );

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
            auditLogDAO.logAction(
                    getServletContext(),
                    request,
                    "ACCESS_DENIED",
                    AuditLog.MODULE_NOTIFICATIONS,
                    "Unauthenticated user attempted to perform notification action.",
                    AuditLog.STATUS_DENIED
            );

            response.sendRedirect(request.getContextPath() + "/login.jsp?error=sessionExpired");
            return;
        }

        String action = FileUtil.clean(request.getParameter("action"));
        String userId = getSessionValue(session, "userId");
        String role = getSessionValue(session, "userRole");

        if (ACTION_MARK_READ.equalsIgnoreCase(action)) {
            String notificationId = FileUtil.clean(request.getParameter("notificationId"));

            if (notificationId.isEmpty()) {
                auditLogDAO.logAction(
                        getServletContext(),
                        request,
                        "MARK_NOTIFICATION_READ",
                        AuditLog.MODULE_NOTIFICATIONS,
                        "Notification mark-read failed because notification ID was missing.",
                        AuditLog.STATUS_FAILED
                );

                redirectToNotifications(request, response, "error", "missingNotificationId");
                return;
            }

            boolean updated = notificationDAO.markAsRead(getServletContext(), notificationId);

            if (updated) {
                auditLogDAO.logAction(
                        getServletContext(),
                        request,
                        "MARK_NOTIFICATION_READ",
                        AuditLog.MODULE_NOTIFICATIONS,
                        "Marked notification " + notificationId + " as read.",
                        AuditLog.STATUS_SUCCESS
                );
            } else {
                auditLogDAO.logAction(
                        getServletContext(),
                        request,
                        "MARK_NOTIFICATION_READ",
                        AuditLog.MODULE_NOTIFICATIONS,
                        "Failed to mark notification " + notificationId + " as read.",
                        AuditLog.STATUS_FAILED
                );
            }

            redirectToNotifications(
                    request,
                    response,
                    updated ? "success" : "error",
                    updated ? "markedRead" : "markReadFailed"
            );
            return;
        }

        if (ACTION_MARK_ALL_READ.equalsIgnoreCase(action)) {
            int updatedCount = notificationDAO.markAllAsReadForUser(getServletContext(), userId, role);

            auditLogDAO.logAction(
                    getServletContext(),
                    request,
                    "MARK_ALL_NOTIFICATIONS_READ",
                    AuditLog.MODULE_NOTIFICATIONS,
                    "Marked " + updatedCount + " notifications as read for user " + userId + " (" + role + ").",
                    AuditLog.STATUS_SUCCESS
            );

            redirectToNotifications(request, response, "success", "allMarkedRead");
            return;
        }

        if (ACTION_DELETE.equalsIgnoreCase(action)) {
            String notificationId = FileUtil.clean(request.getParameter("notificationId"));

            if (notificationId.isEmpty()) {
                auditLogDAO.logAction(
                        getServletContext(),
                        request,
                        "DELETE_NOTIFICATION",
                        AuditLog.MODULE_NOTIFICATIONS,
                        "Notification delete failed because notification ID was missing.",
                        AuditLog.STATUS_FAILED
                );

                redirectToNotifications(request, response, "error", "missingNotificationId");
                return;
            }

            boolean deleted = notificationDAO.deleteNotification(getServletContext(), notificationId);

            if (deleted) {
                auditLogDAO.logAction(
                        getServletContext(),
                        request,
                        "DELETE_NOTIFICATION",
                        AuditLog.MODULE_NOTIFICATIONS,
                        "Deleted notification " + notificationId + ".",
                        AuditLog.STATUS_SUCCESS
                );
            } else {
                auditLogDAO.logAction(
                        getServletContext(),
                        request,
                        "DELETE_NOTIFICATION",
                        AuditLog.MODULE_NOTIFICATIONS,
                        "Failed to delete notification " + notificationId + ".",
                        AuditLog.STATUS_FAILED
                );
            }

            redirectToNotifications(
                    request,
                    response,
                    deleted ? "success" : "error",
                    deleted ? "deleted" : "deleteFailed"
            );
            return;
        }

        auditLogDAO.logAction(
                getServletContext(),
                request,
                "INVALID_ACTION",
                AuditLog.MODULE_NOTIFICATIONS,
                "Invalid notification action submitted: " + action + ".",
                AuditLog.STATUS_WARNING
        );

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