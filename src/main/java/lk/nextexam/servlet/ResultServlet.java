package lk.nextexam.servlet;
import lk.nextexam.dao.NotificationDAO;
import lk.nextexam.model.Notification;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.nextexam.dao.FileUtil;
import lk.nextexam.dao.ResultDAO;
import lk.nextexam.model.Result;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lk.nextexam.dao.NotificationDAO;
import lk.nextexam.model.Notification;

/**
 * Professional controller for Result Management.
 *
 * Supported actions:
 * - GET  /results
 * - POST /results action=add
 * - POST /results action=update
 * - POST /results action=delete
 * - POST /results action=verify
 * - POST /results action=review
 * - POST /results action=pending
 * - POST /results action=publish
 * - POST /results action=unpublish
 *
 * Backward compatibility:
 * - recordId, resultId, and id are all accepted for actions that require an ID.
 */
@WebServlet("/results")
public class ResultServlet extends HttpServlet {

    private static final String ACTION_ADD = "add";
    private static final String ACTION_UPDATE = "update";
    private static final String ACTION_DELETE = "delete";
    private static final String ACTION_VERIFY = "verify";
    private static final String ACTION_REVIEW = "review";
    private static final String ACTION_PENDING = "pending";
    private static final String ACTION_PUBLISH = "publish";
    private static final String ACTION_UNPUBLISH = "unpublish";
    private static final String ACTION_VERIFICATION = "verification";
    private static final String ACTION_PUBLISHED_STATUS = "publishedStatus";

    private final ResultDAO resultDAO = new ResultDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        prepareRequestResponse(request, response);

        request.setAttribute("results", resultDAO.getAllResults(getServletContext()));
        request.getRequestDispatcher("/results/index.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        prepareRequestResponse(request, response);

        String action = FileUtil.clean(request.getParameter("action"));

        if (ACTION_ADD.equalsIgnoreCase(action)) {
            addResult(request, response);
            return;
        }

        if (ACTION_UPDATE.equalsIgnoreCase(action)) {
            updateResult(request, response);
            return;
        }

        if (ACTION_DELETE.equalsIgnoreCase(action)) {
            deleteResult(request, response);
            return;
        }

        if (ACTION_VERIFY.equalsIgnoreCase(action)) {
            verifyResult(request, response);
            return;
        }

        if (ACTION_REVIEW.equalsIgnoreCase(action)) {
            markResultForReview(request, response);
            return;
        }

        if (ACTION_PENDING.equalsIgnoreCase(action)) {
            markResultPending(request, response);
            return;
        }

        if (ACTION_PUBLISH.equalsIgnoreCase(action)) {
            publishResult(request, response);
            return;
        }

        if (ACTION_UNPUBLISH.equalsIgnoreCase(action)) {
            unpublishResult(request, response);
            return;
        }

        if (ACTION_VERIFICATION.equalsIgnoreCase(action)) {
            updateVerification(request, response);
            return;
        }

        if (ACTION_PUBLISHED_STATUS.equalsIgnoreCase(action)) {
            updatePublishedStatus(request, response);
            return;
        }

        redirectToResults(request, response, "error", "invalidAction");
    }

    private void addResult(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        Result result = buildResultFromRequest(request);
        result.applyGradeAndStatusFromMarks();

        String validationError = validateResult(result);

        if (validationError != null) {
            redirectToResults(request, response, "error", validationError);
            return;
        }

        boolean success = resultDAO.addResult(getServletContext(), result);

        if (success) {
            redirectToResults(request, response, "success", "resultAdded");
        } else {
            redirectToResults(request, response, "error", "resultAddFailed");
        }
    }

    private void updateResult(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        Result result = buildResultFromRequest(request);
        result.applyGradeAndStatusFromMarks();

        String validationError = validateResult(result);

        if (validationError != null) {
            redirectToResults(request, response, "error", validationError);
            return;
        }

        boolean success = resultDAO.updateResult(getServletContext(), result);

        if (success) {
            redirectToResults(request, response, "success", "resultUpdated");
        } else {
            redirectToResults(request, response, "error", "resultUpdateFailed");
        }
    }

    private void deleteResult(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String resultId = getRequestRecordId(request);

        if (resultId.isEmpty()) {
            redirectToResults(request, response, "error", "missingResultId");
            return;
        }

        boolean success = resultDAO.deleteResult(getServletContext(), resultId);

        if (success) {
            redirectToResults(request, response, "success", "resultDeleted");
        } else {
            redirectToResults(request, response, "error", "resultDeleteFailed");
        }
    }

    private void verifyResult(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String resultId = getRequestRecordId(request);

        if (resultId.isEmpty()) {
            redirectToResults(request, response, "error", "missingResultId");
            return;
        }

        boolean success = resultDAO.verifyResult(getServletContext(), resultId);

        if (success) {
            redirectToResults(request, response, "success", "resultVerified");
        } else {
            redirectToResults(request, response, "error", "resultVerificationFailed");
        }
    }

    private void markResultForReview(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String resultId = getRequestRecordId(request);

        if (resultId.isEmpty()) {
            redirectToResults(request, response, "error", "missingResultId");
            return;
        }

        boolean success = resultDAO.markResultForReview(getServletContext(), resultId);

        if (success) {
            redirectToResults(request, response, "success", "resultMarkedReview");
        } else {
            redirectToResults(request, response, "error", "resultVerificationFailed");
        }
    }

    private void markResultPending(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String resultId = getRequestRecordId(request);

        if (resultId.isEmpty()) {
            redirectToResults(request, response, "error", "missingResultId");
            return;
        }

        boolean success = resultDAO.markResultPending(getServletContext(), resultId);

        if (success) {
            redirectToResults(request, response, "success", "resultMarkedPending");
        } else {
            redirectToResults(request, response, "error", "resultVerificationFailed");
        }
    }

    private void publishResult(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String resultId = getRequestRecordId(request);

        if (resultId.isEmpty()) {
            redirectToResults(request, response, "error", "missingResultId");
            return;
        }

        boolean success = resultDAO.publishResult(getServletContext(), resultId);

        if (success) {
            Result publishedResult = resultDAO.getResultById(getServletContext(), resultId);

            if (publishedResult != null) {
                notificationDAO.addNotification(
                        getServletContext(),
                        publishedResult.getStudentId(),
                        "Student",
                        "Result Published",
                        "Your result for exam " + publishedResult.getExamId() + " has been published.",
                        Notification.TYPE_RESULT
                );
            }

            redirectToResults(request, response, "success", "resultPublished");
        } else {
            redirectToResults(request, response, "error", "resultPublishFailed");
        }
    }

    private void unpublishResult(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String resultId = getRequestRecordId(request);

        if (resultId.isEmpty()) {
            redirectToResults(request, response, "error", "missingResultId");
            return;
        }

        boolean success = resultDAO.unpublishResult(getServletContext(), resultId);

        if (success) {
            redirectToResults(request, response, "success", "resultUnpublished");
        } else {
            redirectToResults(request, response, "error", "resultUnpublishFailed");
        }
    }

    private void updateVerification(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String resultId = getRequestRecordId(request);
        String verification = FileUtil.clean(request.getParameter("verification"));

        if (resultId.isEmpty()) {
            redirectToResults(request, response, "error", "missingResultId");
            return;
        }

        if (verification.isEmpty()) {
            redirectToResults(request, response, "error", "missingVerification");
            return;
        }

        boolean success = resultDAO.updateVerification(getServletContext(), resultId, verification);

        if (success) {
            redirectToResults(request, response, "success", "resultVerificationUpdated");
        } else {
            redirectToResults(request, response, "error", "resultVerificationFailed");
        }
    }

    private void updatePublishedStatus(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String resultId = getRequestRecordId(request);
        String published = FileUtil.clean(request.getParameter("published"));

        if (resultId.isEmpty()) {
            redirectToResults(request, response, "error", "missingResultId");
            return;
        }

        if (published.isEmpty()) {
            redirectToResults(request, response, "error", "missingPublishedStatus");
            return;
        }

        boolean success = resultDAO.updatePublishedStatus(getServletContext(), resultId, published);

        if (success) {
            redirectToResults(request, response, "success", "resultPublishedStatusUpdated");
        } else {
            redirectToResults(request, response, "error", "resultPublishedStatusUpdateFailed");
        }
    }

    private Result buildResultFromRequest(HttpServletRequest request) {
        String marks = FileUtil.clean(request.getParameter("marks"));
        String grade = FileUtil.clean(request.getParameter("grade"));
        String status = FileUtil.clean(request.getParameter("status"));

        Result result = new Result(
                FileUtil.clean(request.getParameter("resultId")),
                FileUtil.clean(request.getParameter("studentId")),
                FileUtil.clean(request.getParameter("examId")),
                marks,
                grade,
                status,
                FileUtil.clean(request.getParameter("verification")),
                FileUtil.clean(request.getParameter("published"))
        );

        if (result.getGrade().isEmpty() || result.getStatus().isEmpty()) {
            result.applyGradeAndStatusFromMarks();
        }

        return result;
    }

    private String validateResult(Result result) {
        if (result == null) {
            return "invalidResult";
        }

        if (result.getResultId().isEmpty()) {
            return "missingResultId";
        }

        if (result.getStudentId().isEmpty()) {
            return "missingStudentId";
        }

        if (result.getExamId().isEmpty()) {
            return "missingExamId";
        }

        if (result.getMarks().isEmpty()) {
            return "missingMarks";
        }

        if (!result.isValidMarks()) {
            return "invalidMarks";
        }

        if (result.getGrade().isEmpty()) {
            return "missingGrade";
        }

        if (!result.isValidGrade()) {
            return "invalidGrade";
        }

        if (result.getStatus().isEmpty()) {
            return "missingStatus";
        }

        if (!result.isValidStatus()) {
            return "invalidStatus";
        }

        if (result.getVerification().isEmpty()) {
            return "missingVerification";
        }

        if (!result.isValidVerification()) {
            return "invalidVerification";
        }

        if (result.getPublished().isEmpty()) {
            return "missingPublishedStatus";
        }

        if (!result.isValidPublishedStatus()) {
            return "invalidPublishedStatus";
        }

        if (result.isPublished() && !result.isVerified()) {
            return "cannotPublishUnverifiedResult";
        }

        if (!result.isCompleteForSave()) {
            return "incompleteResult";
        }

        return null;
    }

    private String getRequestRecordId(HttpServletRequest request) {
        return firstNonBlank(
                request.getParameter("recordId"),
                request.getParameter("resultId"),
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

    private void redirectToResults(HttpServletRequest request,
                                   HttpServletResponse response,
                                   String messageType,
                                   String messageCode)
            throws IOException {

        response.sendRedirect(
                request.getContextPath()
                        + "/results?"
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