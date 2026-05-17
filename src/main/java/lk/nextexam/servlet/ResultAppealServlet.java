package lk.nextexam.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lk.nextexam.dao.AuditLogDAO;
import lk.nextexam.dao.FileUtil;
import lk.nextexam.dao.ResultAppealDAO;
import lk.nextexam.dao.ResultDAO;
import lk.nextexam.model.AuditLog;
import lk.nextexam.model.Result;
import lk.nextexam.model.ResultAppeal;
import lk.nextexam.model.User;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * ResultAppealServlet controls student result recheck requests
 * and staff review/reply workflow.
 *
 * URL:
 * /result-appeals
 *
 * Audit Logging:
 * - Student appeal creation
 * - Staff appeal update
 * - Access denied attempts
 * - Validation failures
 *
 * Responsible Member:
 * IT25103045 - De Silva H.L.D.C.P.C
 */
@WebServlet("/result-appeals")
public class ResultAppealServlet extends HttpServlet {

    private static final String ROLE_ADMIN = "Admin";
    private static final String ROLE_LECTURER = "Lecturer";

    private static final String ACTION_CREATE = "create";
    private static final String ACTION_UPDATE = "update";

    private final ResultAppealDAO appealDAO = new ResultAppealDAO();
    private final ResultDAO resultDAO = new ResultDAO();
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
                    AuditLog.MODULE_RESULT_APPEALS,
                    "Unauthenticated user attempted to access result appeals page.",
                    AuditLog.STATUS_DENIED
            );

            response.sendRedirect(request.getContextPath() + "/login.jsp?error=sessionExpired");
            return;
        }

        String role = getSessionValue(session, "userRole");
        String userId = getSessionValue(session, "userId");
        boolean isStaff = isStaffRole(role);
        boolean isStudent = User.ROLE_STUDENT.equalsIgnoreCase(role);

        if (isStaff) {
            request.setAttribute("appeals", appealDAO.getAllAppeals(getServletContext()));
            request.setAttribute("isStaff", true);
            request.setAttribute("isStudent", false);
        } else if (isStudent) {
            request.setAttribute("appeals", appealDAO.getAppealsByStudent(getServletContext(), userId));
            request.setAttribute("publishedResults", resultDAO.getPublishedResultsByStudentId(getServletContext(), userId));
            request.setAttribute("selectedResultId", FileUtil.clean(request.getParameter("resultId")));
            request.setAttribute("isStaff", false);
            request.setAttribute("isStudent", true);
        } else {
            auditLogDAO.logAction(
                    getServletContext(),
                    request,
                    "ACCESS_DENIED",
                    AuditLog.MODULE_RESULT_APPEALS,
                    "Unsupported role attempted to access result appeals page.",
                    AuditLog.STATUS_DENIED
            );

            response.sendRedirect(request.getContextPath() + "/login.jsp?error=accessDenied");
            return;
        }

        request.setAttribute("pendingCount", appealDAO.countByStatus(getServletContext(), ResultAppeal.STATUS_PENDING));
        request.setAttribute("underReviewCount", appealDAO.countByStatus(getServletContext(), ResultAppeal.STATUS_UNDER_REVIEW));
        request.setAttribute("resolvedCount", appealDAO.countByStatus(getServletContext(), ResultAppeal.STATUS_RESOLVED));
        request.setAttribute("rejectedCount", appealDAO.countByStatus(getServletContext(), ResultAppeal.STATUS_REJECTED));

        request.getRequestDispatcher("/result-appeals/index.jsp").forward(request, response);
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
                    AuditLog.MODULE_RESULT_APPEALS,
                    "Unauthenticated user attempted to submit result appeal action.",
                    AuditLog.STATUS_DENIED
            );

            response.sendRedirect(request.getContextPath() + "/login.jsp?error=sessionExpired");
            return;
        }

        String action = FileUtil.clean(request.getParameter("action"));

        if (ACTION_CREATE.equalsIgnoreCase(action)) {
            createAppeal(request, response, session);
            return;
        }

        if (ACTION_UPDATE.equalsIgnoreCase(action)) {
            updateAppeal(request, response, session);
            return;
        }

        auditLogDAO.logAction(
                getServletContext(),
                request,
                "INVALID_ACTION",
                AuditLog.MODULE_RESULT_APPEALS,
                "Invalid result appeal action submitted: " + action + ".",
                AuditLog.STATUS_WARNING
        );

        redirectToAppeals(request, response, "error", "invalidAction");
    }

    private void createAppeal(HttpServletRequest request,
                              HttpServletResponse response,
                              HttpSession session)
            throws IOException {

        String role = getSessionValue(session, "userRole");

        if (!User.ROLE_STUDENT.equalsIgnoreCase(role)) {
            auditLogDAO.logAction(
                    getServletContext(),
                    request,
                    "ACCESS_DENIED",
                    AuditLog.MODULE_RESULT_APPEALS,
                    "Non-student user attempted to create a result appeal.",
                    AuditLog.STATUS_DENIED
            );

            redirectToAppeals(request, response, "error", "accessDenied");
            return;
        }

        String studentId = getSessionValue(session, "userId");
        String studentName = getStudentDisplayName(session);
        String resultId = FileUtil.clean(request.getParameter("resultId"));
        String reasonType = FileUtil.clean(request.getParameter("reasonType"));
        String message = FileUtil.clean(request.getParameter("message"));

        if (resultId.isEmpty()) {
            auditLogDAO.logAction(
                    getServletContext(),
                    request,
                    "CREATE_APPEAL",
                    AuditLog.MODULE_RESULT_APPEALS,
                    "Result appeal creation failed because result ID was missing.",
                    AuditLog.STATUS_FAILED
            );

            redirectToAppeals(request, response, "error", "missingResultId");
            return;
        }

        if (reasonType.isEmpty() || message.isEmpty()) {
            auditLogDAO.logAction(
                    getServletContext(),
                    request,
                    "CREATE_APPEAL",
                    AuditLog.MODULE_RESULT_APPEALS,
                    "Result appeal creation failed for result " + resultId + " because appeal details were missing.",
                    AuditLog.STATUS_FAILED
            );

            redirectToAppeals(request, response, "error", "missingAppealDetails");
            return;
        }

        Result result = resultDAO.getResultById(getServletContext(), resultId);

        if (result == null) {
            auditLogDAO.logAction(
                    getServletContext(),
                    request,
                    "CREATE_APPEAL",
                    AuditLog.MODULE_RESULT_APPEALS,
                    "Result appeal creation failed because result " + resultId + " was not found.",
                    AuditLog.STATUS_FAILED
            );

            redirectToAppeals(request, response, "error", "resultNotFound");
            return;
        }

        if (!result.getStudentId().equalsIgnoreCase(studentId)) {
            auditLogDAO.logAction(
                    getServletContext(),
                    request,
                    "ACCESS_DENIED",
                    AuditLog.MODULE_RESULT_APPEALS,
                    "Student " + studentId + " attempted to appeal another student's result " + resultId + ".",
                    AuditLog.STATUS_DENIED
            );

            redirectToAppeals(request, response, "error", "accessDenied");
            return;
        }

        if (!result.isPublished()) {
            auditLogDAO.logAction(
                    getServletContext(),
                    request,
                    "CREATE_APPEAL",
                    AuditLog.MODULE_RESULT_APPEALS,
                    "Result appeal creation failed because result " + resultId + " is not published.",
                    AuditLog.STATUS_WARNING
            );

            redirectToAppeals(request, response, "error", "resultNotPublished");
            return;
        }

        if (appealDAO.hasStudentAppealedResult(getServletContext(), studentId, resultId)) {
            auditLogDAO.logAction(
                    getServletContext(),
                    request,
                    "CREATE_APPEAL",
                    AuditLog.MODULE_RESULT_APPEALS,
                    "Duplicate appeal blocked for student " + studentId + " and result " + resultId + ".",
                    AuditLog.STATUS_WARNING
            );

            redirectToAppeals(request, response, "error", "appealAlreadyExists");
            return;
        }

        String now = appealDAO.now();

        ResultAppeal appeal = new ResultAppeal(
                FileUtil.generateId("RA"),
                result.getResultId(),
                result.getExamId(),
                studentId,
                studentName,
                reasonType,
                message,
                ResultAppeal.STATUS_PENDING,
                "",
                now,
                now,
                ""
        );

        boolean saved = appealDAO.addAppeal(getServletContext(), appeal);

        if (saved) {
            auditLogDAO.logAction(
                    getServletContext(),
                    request,
                    "CREATE_APPEAL",
                    AuditLog.MODULE_RESULT_APPEALS,
                    "Student submitted result appeal " + appeal.getAppealId()
                            + " for result " + resultId
                            + " with reason type " + reasonType + ".",
                    AuditLog.STATUS_SUCCESS
            );

            redirectToAppeals(request, response, "success", "appealSubmitted");
        } else {
            auditLogDAO.logAction(
                    getServletContext(),
                    request,
                    "CREATE_APPEAL",
                    AuditLog.MODULE_RESULT_APPEALS,
                    "Result appeal save failed for result " + resultId + ".",
                    AuditLog.STATUS_FAILED
            );

            redirectToAppeals(request, response, "error", "appealSaveFailed");
        }
    }

    private void updateAppeal(HttpServletRequest request,
                              HttpServletResponse response,
                              HttpSession session)
            throws IOException {

        String role = getSessionValue(session, "userRole");

        if (!isStaffRole(role)) {
            auditLogDAO.logAction(
                    getServletContext(),
                    request,
                    "ACCESS_DENIED",
                    AuditLog.MODULE_RESULT_APPEALS,
                    "Non-staff user attempted to update a result appeal.",
                    AuditLog.STATUS_DENIED
            );

            redirectToAppeals(request, response, "error", "accessDenied");
            return;
        }

        String appealId = FileUtil.clean(request.getParameter("appealId"));
        String status = FileUtil.clean(request.getParameter("status"));
        String staffReply = FileUtil.clean(request.getParameter("staffReply"));
        String reviewedBy = getStaffDisplayName(session);

        if (appealId.isEmpty()) {
            auditLogDAO.logAction(
                    getServletContext(),
                    request,
                    "UPDATE_APPEAL",
                    AuditLog.MODULE_RESULT_APPEALS,
                    "Appeal update failed because appeal ID was missing.",
                    AuditLog.STATUS_FAILED
            );

            redirectToAppeals(request, response, "error", "missingAppealId");
            return;
        }

        ResultAppeal appeal = appealDAO.getAppealById(getServletContext(), appealId);

        if (appeal == null) {
            auditLogDAO.logAction(
                    getServletContext(),
                    request,
                    "UPDATE_APPEAL",
                    AuditLog.MODULE_RESULT_APPEALS,
                    "Appeal update failed because appeal " + appealId + " was not found.",
                    AuditLog.STATUS_FAILED
            );

            redirectToAppeals(request, response, "error", "appealNotFound");
            return;
        }

        if (status.isEmpty()) {
            auditLogDAO.logAction(
                    getServletContext(),
                    request,
                    "UPDATE_APPEAL",
                    AuditLog.MODULE_RESULT_APPEALS,
                    "Appeal update failed because status was missing for appeal " + appealId + ".",
                    AuditLog.STATUS_FAILED
            );

            redirectToAppeals(request, response, "error", "missingStatus");
            return;
        }

        appeal.setStatus(status);
        appeal.setStaffReply(staffReply);
        appeal.setReviewedBy(reviewedBy);
        appeal.setUpdatedAt(appealDAO.now());

        if (!appeal.isValidStatus()) {
            auditLogDAO.logAction(
                    getServletContext(),
                    request,
                    "UPDATE_APPEAL",
                    AuditLog.MODULE_RESULT_APPEALS,
                    "Appeal update failed because invalid status was provided for appeal " + appealId + ": " + status + ".",
                    AuditLog.STATUS_FAILED
            );

            redirectToAppeals(request, response, "error", "invalidStatus");
            return;
        }

        boolean updated = appealDAO.updateAppeal(getServletContext(), appeal);

        if (updated) {
            auditLogDAO.logAction(
                    getServletContext(),
                    request,
                    "UPDATE_APPEAL",
                    AuditLog.MODULE_RESULT_APPEALS,
                    "Staff updated appeal " + appealId
                            + " to status " + appeal.getStatus()
                            + ". Reviewed by " + reviewedBy + ".",
                    AuditLog.STATUS_SUCCESS
            );

            redirectToAppeals(request, response, "success", "appealUpdated");
        } else {
            auditLogDAO.logAction(
                    getServletContext(),
                    request,
                    "UPDATE_APPEAL",
                    AuditLog.MODULE_RESULT_APPEALS,
                    "Appeal update failed while saving appeal " + appealId + ".",
                    AuditLog.STATUS_FAILED
            );

            redirectToAppeals(request, response, "error", "appealUpdateFailed");
        }
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

    private boolean isStaffRole(String role) {
        return ROLE_ADMIN.equalsIgnoreCase(role) || ROLE_LECTURER.equalsIgnoreCase(role);
    }

    private String getStudentDisplayName(HttpSession session) {
        String displayName = getSessionValue(session, "displayName");

        if (!displayName.isEmpty()) {
            return displayName;
        }

        String username = getSessionValue(session, "username");

        if (!username.isEmpty()) {
            return username;
        }

        return getSessionValue(session, "userId");
    }

    private String getStaffDisplayName(HttpSession session) {
        String userId = getSessionValue(session, "userId");
        String displayName = getSessionValue(session, "displayName");
        String username = getSessionValue(session, "username");

        if (!displayName.isEmpty()) {
            return displayName + " (" + userId + ")";
        }

        if (!username.isEmpty()) {
            return username + " (" + userId + ")";
        }

        return userId;
    }

    private String getSessionValue(HttpSession session, String key) {
        if (session == null || key == null) {
            return "";
        }

        Object value = session.getAttribute(key);
        return value == null ? "" : String.valueOf(value).trim();
    }

    private void redirectToAppeals(HttpServletRequest request,
                                   HttpServletResponse response,
                                   String type,
                                   String code)
            throws IOException {

        response.sendRedirect(request.getContextPath()
                + "/result-appeals?"
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