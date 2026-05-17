package lk.nextexam.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lk.nextexam.dao.AuditLogDAO;
import lk.nextexam.model.AuditLog;

import java.io.IOException;

/**
 * AuditLogServlet controls the admin-only audit log page.
 *
 * URL:
 * /audit-logs
 *
 * Responsible Member:
 * IT25103045 - De Silva H.L.D.C.P.C
 */
@WebServlet("/audit-logs")
public class AuditLogServlet extends HttpServlet {

    private static final String ROLE_ADMIN = "Admin";

    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        prepareRequestResponse(request, response);

        HttpSession session = request.getSession(false);

        if (!isAdmin(session)) {
            auditLogDAO.logAction(
                    getServletContext(),
                    request,
                    "ACCESS_DENIED",
                    AuditLog.MODULE_SYSTEM,
                    "Non-admin user attempted to access audit logs.",
                    AuditLog.STATUS_DENIED
            );

            response.sendRedirect(request.getContextPath() + "/dashboard.jsp?error=accessDenied");
            return;
        }

        request.setAttribute("auditLogs", auditLogDAO.getAllLogs(getServletContext()));
        request.setAttribute("totalLogs", auditLogDAO.countAll(getServletContext()));
        request.setAttribute("successLogs", auditLogDAO.countByStatus(getServletContext(), AuditLog.STATUS_SUCCESS));
        request.setAttribute("failedLogs", auditLogDAO.countByStatus(getServletContext(), AuditLog.STATUS_FAILED));
        request.setAttribute("deniedLogs", auditLogDAO.countByStatus(getServletContext(), AuditLog.STATUS_DENIED));
        request.setAttribute("warningLogs", auditLogDAO.countByStatus(getServletContext(), AuditLog.STATUS_WARNING));
        request.setAttribute("todayLogs", auditLogDAO.countToday(getServletContext()));

        request.getRequestDispatcher("/audit-logs/index.jsp").forward(request, response);
    }

    private boolean isAdmin(HttpSession session) {
        if (session == null) {
            return false;
        }

        Object loggedUser = session.getAttribute("loggedUser");
        Object loginStatus = session.getAttribute("loginStatus");
        Object userRole = session.getAttribute("userRole");
        Object userId = session.getAttribute("userId");

        if (loggedUser == null || userId == null || userRole == null) {
            return false;
        }

        return "authenticated".equals(String.valueOf(loginStatus))
                && ROLE_ADMIN.equalsIgnoreCase(String.valueOf(userRole));
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