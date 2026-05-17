package lk.nextexam.servlet;
import lk.nextexam.dao.AuditLogDAO;
import lk.nextexam.model.AuditLog;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lk.nextexam.dao.ExamSubmissionDAO;
import lk.nextexam.dao.FileUtil;
import lk.nextexam.model.ExamSubmission;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * PublishResultServlet publishes eligible exam submissions to students.
 *
 * URL:
 * /publish-result
 *
 * Only Auto Marked and Marked submissions can be published.
 *
 * Responsible Member:
 * IT25103045 - De Silva H.L.D.C.P.C
 */
@WebServlet("/publish-result")
public class PublishResultServlet extends HttpServlet {

    private static final String ROLE_ADMIN = "Admin";
    private static final String ROLE_LECTURER = "Lecturer";
    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    private final ExamSubmissionDAO submissionDAO = new ExamSubmissionDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        prepareRequestResponse(request, response);

        HttpSession session = request.getSession(false);

        if (!isStaff(session)) {
            response.sendRedirect(request.getContextPath() + "/my-exams?error=accessDenied");
            return;
        }

        String submissionId = FileUtil.clean(request.getParameter("submissionId"));

        if (submissionId.isEmpty()) {
            redirectToSubmissions(request, response, "error", "missingSubmissionId");
            return;
        }

        ExamSubmission submission = submissionDAO.getSubmissionById(getServletContext(), submissionId);

        if (submission == null) {
            redirectToSubmissions(request, response, "error", "submissionNotFound");
            return;
        }

        if (submission.isPublished()) {
            redirectToSubmissions(request, response, "info", "alreadyPublished");
            return;
        }

        if (submission.isCancelled()) {
            redirectToSubmissions(request, response, "error", "cancelledSubmission");
            return;
        }

        if (submission.isManualReviewRequired() || submission.isSubmitted()) {
            redirectToSubmissions(request, response, "error", "manualReviewPending");
            return;
        }

        if (!submission.canBePublished()) {
            redirectToSubmissions(request, response, "error", "notEligibleForPublish");
            return;
        }

        boolean published = submissionDAO.publishSubmission(getServletContext(), submissionId);

        if (published) {
            auditLogDAO.logAction(
                    getServletContext(),
                    request,
                    "PUBLISH_RESULT",
                    AuditLog.MODULE_RESULTS,
                    "Published result for submission " + submissionId + ".",
                    AuditLog.STATUS_SUCCESS
            );
            redirectToSubmissions(request, response, "success", "resultPublished");
        } else {
            redirectToSubmissions(request, response, "error", "publishFailed");
        }
    }

    private boolean isStaff(HttpSession session) {
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

        String role = String.valueOf(userRole).trim();

        return "authenticated".equals(String.valueOf(loginStatus))
                && (ROLE_ADMIN.equalsIgnoreCase(role) || ROLE_LECTURER.equalsIgnoreCase(role));
    }

    private void redirectToSubmissions(HttpServletRequest request,
                                       HttpServletResponse response,
                                       String type,
                                       String code)
            throws IOException {

        response.sendRedirect(request.getContextPath()
                + "/submissions?"
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