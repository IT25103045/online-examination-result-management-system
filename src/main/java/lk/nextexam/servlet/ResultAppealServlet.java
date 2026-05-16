package lk.nextexam.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lk.nextexam.dao.FileUtil;
import lk.nextexam.dao.ResultAppealDAO;
import lk.nextexam.dao.ResultDAO;
import lk.nextexam.model.Result;
import lk.nextexam.model.ResultAppeal;
import lk.nextexam.model.User;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * ResultAppealServlet controls student result recheck requests
 * and staff review/reply workflow.
 *
 * URL:
 * /result-appeals
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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        prepareRequestResponse(request, response);

        HttpSession session = request.getSession(false);

        if (!isAuthenticated(session)) {
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

        redirectToAppeals(request, response, "error", "invalidAction");
    }

    private void createAppeal(HttpServletRequest request,
                              HttpServletResponse response,
                              HttpSession session)
            throws IOException {

        String role = getSessionValue(session, "userRole");

        if (!User.ROLE_STUDENT.equalsIgnoreCase(role)) {
            redirectToAppeals(request, response, "error", "accessDenied");
            return;
        }

        String studentId = getSessionValue(session, "userId");
        String studentName = getStudentDisplayName(session);
        String resultId = FileUtil.clean(request.getParameter("resultId"));
        String reasonType = FileUtil.clean(request.getParameter("reasonType"));
        String message = FileUtil.clean(request.getParameter("message"));

        if (resultId.isEmpty()) {
            redirectToAppeals(request, response, "error", "missingResultId");
            return;
        }

        if (reasonType.isEmpty() || message.isEmpty()) {
            redirectToAppeals(request, response, "error", "missingAppealDetails");
            return;
        }

        Result result = resultDAO.getResultById(getServletContext(), resultId);

        if (result == null) {
            redirectToAppeals(request, response, "error", "resultNotFound");
            return;
        }

        if (!result.getStudentId().equalsIgnoreCase(studentId)) {
            redirectToAppeals(request, response, "error", "accessDenied");
            return;
        }

        if (!result.isPublished()) {
            redirectToAppeals(request, response, "error", "resultNotPublished");
            return;
        }

        if (appealDAO.hasStudentAppealedResult(getServletContext(), studentId, resultId)) {
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
            redirectToAppeals(request, response, "success", "appealSubmitted");
        } else {
            redirectToAppeals(request, response, "error", "appealSaveFailed");
        }
    }

    private void updateAppeal(HttpServletRequest request,
                              HttpServletResponse response,
                              HttpSession session)
            throws IOException {

        String role = getSessionValue(session, "userRole");

        if (!isStaffRole(role)) {
            redirectToAppeals(request, response, "error", "accessDenied");
            return;
        }

        String appealId = FileUtil.clean(request.getParameter("appealId"));
        String status = FileUtil.clean(request.getParameter("status"));
        String staffReply = FileUtil.clean(request.getParameter("staffReply"));
        String reviewedBy = getStaffDisplayName(session);

        if (appealId.isEmpty()) {
            redirectToAppeals(request, response, "error", "missingAppealId");
            return;
        }

        ResultAppeal appeal = appealDAO.getAppealById(getServletContext(), appealId);

        if (appeal == null) {
            redirectToAppeals(request, response, "error", "appealNotFound");
            return;
        }

        if (status.isEmpty()) {
            redirectToAppeals(request, response, "error", "missingStatus");
            return;
        }

        appeal.setStatus(status);
        appeal.setStaffReply(staffReply);
        appeal.setReviewedBy(reviewedBy);
        appeal.setUpdatedAt(appealDAO.now());

        if (!appeal.isValidStatus()) {
            redirectToAppeals(request, response, "error", "invalidStatus");
            return;
        }

        boolean updated = appealDAO.updateAppeal(getServletContext(), appeal);

        if (updated) {
            redirectToAppeals(request, response, "success", "appealUpdated");
        } else {
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