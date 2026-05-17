package lk.nextexam.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lk.nextexam.dao.AuditLogDAO;
import lk.nextexam.dao.FileUtil;
import lk.nextexam.dao.SystemSettingDAO;
import lk.nextexam.model.AuditLog;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * SystemSettingServlet controls the Admin-only system settings page.
 *
 * URL:
 * /settings
 *
 * Responsible Member:
 * IT25103045 - De Silva H.L.D.C.P.C
 */
@WebServlet("/settings")
public class SystemSettingServlet extends HttpServlet {

    private static final String ROLE_ADMIN = "Admin";

    private final SystemSettingDAO settingDAO = new SystemSettingDAO();
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
                    "Non-admin user attempted to access system settings.",
                    AuditLog.STATUS_DENIED
            );

            response.sendRedirect(request.getContextPath() + "/dashboard.jsp?error=accessDenied");
            return;
        }

        request.setAttribute("settingsMap", settingDAO.getSettingsMap(getServletContext()));
        request.getRequestDispatcher("/settings/index.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        prepareRequestResponse(request, response);

        HttpSession session = request.getSession(false);

        if (!isAdmin(session)) {
            auditLogDAO.logAction(
                    getServletContext(),
                    request,
                    "ACCESS_DENIED",
                    AuditLog.MODULE_SYSTEM,
                    "Non-admin user attempted to update system settings.",
                    AuditLog.STATUS_DENIED
            );

            response.sendRedirect(request.getContextPath() + "/dashboard.jsp?error=accessDenied");
            return;
        }

        Map<String, String> updatedSettings = new LinkedHashMap<>();

        updatedSettings.put(SystemSettingDAO.KEY_APP_NAME, FileUtil.clean(request.getParameter(SystemSettingDAO.KEY_APP_NAME)));
        updatedSettings.put(SystemSettingDAO.KEY_INSTITUTION_NAME, FileUtil.clean(request.getParameter(SystemSettingDAO.KEY_INSTITUTION_NAME)));
        updatedSettings.put(SystemSettingDAO.KEY_ACADEMIC_YEAR, FileUtil.clean(request.getParameter(SystemSettingDAO.KEY_ACADEMIC_YEAR)));
        updatedSettings.put(SystemSettingDAO.KEY_SEMESTER, FileUtil.clean(request.getParameter(SystemSettingDAO.KEY_SEMESTER)));
        updatedSettings.put(SystemSettingDAO.KEY_SUPPORT_EMAIL, FileUtil.clean(request.getParameter(SystemSettingDAO.KEY_SUPPORT_EMAIL)));
        updatedSettings.put(SystemSettingDAO.KEY_SUPPORT_PHONE, FileUtil.clean(request.getParameter(SystemSettingDAO.KEY_SUPPORT_PHONE)));
        updatedSettings.put(SystemSettingDAO.KEY_FOOTER_TEXT, FileUtil.clean(request.getParameter(SystemSettingDAO.KEY_FOOTER_TEXT)));
        updatedSettings.put(SystemSettingDAO.KEY_SYSTEM_STATUS, FileUtil.clean(request.getParameter(SystemSettingDAO.KEY_SYSTEM_STATUS)));
        updatedSettings.put(SystemSettingDAO.KEY_DEFAULT_EXAM_NOTE, FileUtil.clean(request.getParameter(SystemSettingDAO.KEY_DEFAULT_EXAM_NOTE)));
        updatedSettings.put(SystemSettingDAO.KEY_HELP_DESK_MESSAGE, FileUtil.clean(request.getParameter(SystemSettingDAO.KEY_HELP_DESK_MESSAGE)));

        if (updatedSettings.get(SystemSettingDAO.KEY_APP_NAME).isEmpty()
                || updatedSettings.get(SystemSettingDAO.KEY_INSTITUTION_NAME).isEmpty()
                || updatedSettings.get(SystemSettingDAO.KEY_SUPPORT_EMAIL).isEmpty()) {

            auditLogDAO.logAction(
                    getServletContext(),
                    request,
                    "UPDATE_SETTINGS",
                    AuditLog.MODULE_SYSTEM,
                    "System settings update failed because required branding/support fields were missing.",
                    AuditLog.STATUS_FAILED
            );

            redirectToSettings(request, response, "error", "missingRequired");
            return;
        }

        boolean updated = settingDAO.updateSettings(getServletContext(), updatedSettings);

        if (updated) {
            auditLogDAO.logAction(
                    getServletContext(),
                    request,
                    "UPDATE_SETTINGS",
                    AuditLog.MODULE_SYSTEM,
                    "Admin updated system settings and branding information.",
                    AuditLog.STATUS_SUCCESS
            );

            redirectToSettings(request, response, "success", "settingsUpdated");
        } else {
            auditLogDAO.logAction(
                    getServletContext(),
                    request,
                    "UPDATE_SETTINGS",
                    AuditLog.MODULE_SYSTEM,
                    "System settings update failed while saving configuration file.",
                    AuditLog.STATUS_FAILED
            );

            redirectToSettings(request, response, "error", "settingsUpdateFailed");
        }
    }

    private boolean isAdmin(HttpSession session) {
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
                && "authenticated".equals(String.valueOf(loginStatus))
                && ROLE_ADMIN.equalsIgnoreCase(String.valueOf(userRole));
    }

    private void redirectToSettings(HttpServletRequest request,
                                    HttpServletResponse response,
                                    String type,
                                    String code)
            throws IOException {

        response.sendRedirect(request.getContextPath()
                + "/settings?"
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