package lk.nextexam.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lk.nextexam.dao.FeedbackDAO;
import lk.nextexam.dao.FileUtil;
import lk.nextexam.model.Feedback;
import lk.nextexam.model.User;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

/**
 * Professional controller for Feedback management.
 *
 * Route:
 * /feedback
 *
 * Student:
 * - Can submit feedback.
 * - Can view own feedback only.
 * - Can edit/delete feedback only while status is New.
 *
 * Admin/Lecturer:
 * - Can view all feedback.
 * - Can update feedback status.
 * - Can delete only open feedback.
 */
@WebServlet("/feedback")
public class FeedbackServlet extends HttpServlet {

    private static final String ACTION_ADD = "add";
    private static final String ACTION_UPDATE = "update";
    private static final String ACTION_DELETE = "delete";
    private static final String ACTION_STATUS = "status";

    private final FeedbackDAO feedbackDAO = new FeedbackDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        prepareRequestResponse(request, response);

        HttpSession session = request.getSession(false);

        String userRole = getSessionValue(session, "userRole");
        String userId = getSessionValue(session, "userId");
        String username = getSessionValue(session, "username");

        boolean canManageFeedback = canManageFeedback(userRole);

        List<Feedback> feedbackList = canManageFeedback
                ? feedbackDAO.getAllFeedback(getServletContext())
                : feedbackDAO.getFeedbackByStudentId(getServletContext(), userId);

        request.setAttribute("feedbackList", feedbackList);
        request.setAttribute("canManageFeedback", canManageFeedback);
        request.setAttribute("currentUserRole", userRole);
        request.setAttribute("currentUserId", userId);
        request.setAttribute("currentUsername", username);

        request.setAttribute("totalFeedback", feedbackList != null ? feedbackList.size() : 0);
        request.setAttribute("newFeedbackCount", canManageFeedback
                ? feedbackDAO.countNewFeedback(getServletContext())
                : countByStatus(feedbackList, Feedback.STATUS_NEW));

        request.setAttribute("inReviewFeedbackCount", canManageFeedback
                ? feedbackDAO.countInReviewFeedback(getServletContext())
                : countByStatus(feedbackList, Feedback.STATUS_IN_REVIEW));

        request.setAttribute("resolvedFeedbackCount", canManageFeedback
                ? feedbackDAO.countResolvedFeedback(getServletContext())
                : countByStatus(feedbackList, Feedback.STATUS_RESOLVED));

        request.setAttribute("closedFeedbackCount", canManageFeedback
                ? feedbackDAO.countClosedFeedback(getServletContext())
                : countByStatus(feedbackList, Feedback.STATUS_CLOSED));

        request.setAttribute("openFeedbackCount", canManageFeedback
                ? feedbackDAO.countOpenFeedback(getServletContext())
                : countOpen(feedbackList));

        request.setAttribute("completedFeedbackCount", canManageFeedback
                ? feedbackDAO.countCompletedFeedback(getServletContext())
                : countCompleted(feedbackList));

        request.setAttribute("todayFeedbackCount", canManageFeedback
                ? feedbackDAO.countTodayFeedback(getServletContext())
                : countToday(feedbackList));

        request.setAttribute("technicalFeedbackCount", canManageFeedback
                ? feedbackDAO.countTechnicalFeedback(getServletContext())
                : countByCategory(feedbackList, Feedback.CATEGORY_TECHNICAL));

        request.getRequestDispatcher("/feedback/index.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        prepareRequestResponse(request, response);

        HttpSession session = request.getSession(false);

        String userRole = getSessionValue(session, "userRole");
        String userId = getSessionValue(session, "userId");

        String action = FileUtil.clean(request.getParameter("action"));

        if (ACTION_ADD.equalsIgnoreCase(action)) {
            addFeedback(request, response, userId, userRole);
            return;
        }

        if (ACTION_UPDATE.equalsIgnoreCase(action)) {
            updateFeedback(request, response, userId, userRole);
            return;
        }

        if (ACTION_DELETE.equalsIgnoreCase(action)) {
            deleteFeedback(request, response, userId, userRole);
            return;
        }

        if (ACTION_STATUS.equalsIgnoreCase(action)) {
            updateFeedbackStatus(request, response, userRole);
            return;
        }

        redirectToFeedback(request, response, "error", "invalidAction");
    }

    private void addFeedback(HttpServletRequest request,
                             HttpServletResponse response,
                             String userId,
                             String userRole)
            throws IOException {

        Feedback feedback = buildFeedbackFromRequest(request);

        if (FeedbackSafe.isStudent(userRole)) {
            feedback.setStudentId(userId);
            feedback.setStatus(Feedback.STATUS_NEW);

            if (feedback.getFeedbackId().isEmpty()) {
                feedback.setFeedbackId(FileUtil.generateId("FB"));
            }
        }

        String validationError = validateFeedback(feedback);

        if (validationError != null) {
            redirectToFeedback(request, response, "error", validationError);
            return;
        }

        boolean success = feedbackDAO.addFeedback(getServletContext(), feedback);

        if (success) {
            redirectToFeedback(request, response, "success", "feedbackAdded");
        } else {
            redirectToFeedback(request, response, "error", "feedbackAddFailed");
        }
    }

    private void updateFeedback(HttpServletRequest request,
                                HttpServletResponse response,
                                String userId,
                                String userRole)
            throws IOException {

        Feedback feedback = buildFeedbackFromRequest(request);
        Feedback existingFeedback = feedbackDAO.getFeedbackById(getServletContext(), feedback.getFeedbackId());

        if (existingFeedback == null) {
            redirectToFeedback(request, response, "error", "feedbackNotFound");
            return;
        }

        if (FeedbackSafe.isStudent(userRole)) {
            if (!existingFeedback.canEditByStudent(userId)) {
                redirectToFeedback(request, response, "error", "accessDenied");
                return;
            }

            feedback.setStudentId(existingFeedback.getStudentId());
            feedback.setDate(existingFeedback.getDate());
            feedback.setStatus(existingFeedback.getStatus());
        }

        String validationError = validateFeedback(feedback);

        if (validationError != null) {
            redirectToFeedback(request, response, "error", validationError);
            return;
        }

        boolean success = feedbackDAO.updateFeedback(getServletContext(), feedback);

        if (success) {
            redirectToFeedback(request, response, "success", "feedbackUpdated");
        } else {
            redirectToFeedback(request, response, "error", "feedbackUpdateFailed");
        }
    }

    private void deleteFeedback(HttpServletRequest request,
                                HttpServletResponse response,
                                String userId,
                                String userRole)
            throws IOException {

        String feedbackId = firstNonBlank(
                request.getParameter("recordId"),
                request.getParameter("feedbackId"),
                request.getParameter("id")
        );

        if (feedbackId.isEmpty()) {
            redirectToFeedback(request, response, "error", "missingFeedbackId");
            return;
        }

        Feedback feedback = feedbackDAO.getFeedbackById(getServletContext(), feedbackId);

        if (feedback == null) {
            redirectToFeedback(request, response, "error", "feedbackNotFound");
            return;
        }

        if (FeedbackSafe.isStudent(userRole) && !feedback.canDeleteByStudent(userId)) {
            redirectToFeedback(request, response, "error", "accessDenied");
            return;
        }

        boolean success = feedbackDAO.deleteFeedback(getServletContext(), feedbackId);

        if (success) {
            redirectToFeedback(request, response, "success", "feedbackDeleted");
        } else {
            redirectToFeedback(request, response, "error", "feedbackDeleteFailed");
        }
    }

    private void updateFeedbackStatus(HttpServletRequest request,
                                      HttpServletResponse response,
                                      String userRole)
            throws IOException {

        if (!canManageFeedback(userRole)) {
            redirectToFeedback(request, response, "error", "accessDenied");
            return;
        }

        String feedbackId = FileUtil.clean(request.getParameter("feedbackId"));
        String status = FileUtil.clean(request.getParameter("status"));

        if (feedbackId.isEmpty()) {
            redirectToFeedback(request, response, "error", "missingFeedbackId");
            return;
        }

        if (status.isEmpty()) {
            redirectToFeedback(request, response, "error", "missingStatus");
            return;
        }

        Feedback feedback = feedbackDAO.getFeedbackById(getServletContext(), feedbackId);

        if (feedback == null) {
            redirectToFeedback(request, response, "error", "feedbackNotFound");
            return;
        }

        feedback.setStatus(status);

        if (!feedback.isValidStatus()) {
            redirectToFeedback(request, response, "error", "invalidStatus");
            return;
        }

        boolean success = feedbackDAO.updateFeedback(getServletContext(), feedback);

        if (success) {
            redirectToFeedback(request, response, "success", "feedbackStatusUpdated");
        } else {
            redirectToFeedback(request, response, "error", "feedbackStatusUpdateFailed");
        }
    }

    private Feedback buildFeedbackFromRequest(HttpServletRequest request) {
        String date = FileUtil.clean(request.getParameter("date"));
        String status = FileUtil.clean(request.getParameter("status"));

        if (date.isEmpty()) {
            date = LocalDate.now().toString();
        }

        if (status.isEmpty()) {
            status = Feedback.STATUS_NEW;
        }

        return new Feedback(
                FileUtil.clean(request.getParameter("feedbackId")),
                FileUtil.clean(request.getParameter("studentId")),
                FileUtil.clean(request.getParameter("category")),
                FileUtil.clean(request.getParameter("message")),
                date,
                status
        );
    }

    private String validateFeedback(Feedback feedback) {
        if (feedback == null) {
            return "invalidFeedback";
        }

        if (feedback.getFeedbackId().isEmpty()) {
            return "missingFeedbackId";
        }

        if (feedback.getStudentId().isEmpty()) {
            return "missingStudentId";
        }

        if (feedback.getCategory().isEmpty()) {
            return "missingCategory";
        }

        if (!feedback.isValidCategory()) {
            return "invalidCategory";
        }

        if (feedback.getMessage().isEmpty()) {
            return "missingMessage";
        }

        if (feedback.getMessage().length() > 1200) {
            return "messageTooLong";
        }

        if (feedback.getDate().isEmpty()) {
            return "missingDate";
        }

        if (!feedback.isValidDate()) {
            return "invalidDate";
        }

        if (feedback.getStatus().isEmpty()) {
            return "missingStatus";
        }

        if (!feedback.isValidStatus()) {
            return "invalidStatus";
        }

        if (!feedback.isCompleteForSave()) {
            return "incompleteFeedback";
        }

        return null;
    }

    private boolean canManageFeedback(String role) {
        return User.ROLE_ADMIN.equalsIgnoreCase(role)
                || User.ROLE_LECTURER.equalsIgnoreCase(role);
    }

    private String getSessionValue(HttpSession session, String key) {
        if (session == null || key == null) {
            return "";
        }

        Object value = session.getAttribute(key);
        return value == null ? "" : String.valueOf(value).trim();
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

    private int countByStatus(List<Feedback> feedbackList, String status) {
        int count = 0;

        if (feedbackList == null) {
            return count;
        }

        for (Feedback feedback : feedbackList) {
            if (feedback.getStatus().equalsIgnoreCase(status)) {
                count++;
            }
        }

        return count;
    }

    private int countByCategory(List<Feedback> feedbackList, String category) {
        int count = 0;

        if (feedbackList == null) {
            return count;
        }

        for (Feedback feedback : feedbackList) {
            if (feedback.getCategory().equalsIgnoreCase(category)) {
                count++;
            }
        }

        return count;
    }

    private int countOpen(List<Feedback> feedbackList) {
        int count = 0;

        if (feedbackList == null) {
            return count;
        }

        for (Feedback feedback : feedbackList) {
            if (feedback.isOpen()) {
                count++;
            }
        }

        return count;
    }

    private int countCompleted(List<Feedback> feedbackList) {
        int count = 0;

        if (feedbackList == null) {
            return count;
        }

        for (Feedback feedback : feedbackList) {
            if (feedback.isCompleted()) {
                count++;
            }
        }

        return count;
    }

    private int countToday(List<Feedback> feedbackList) {
        int count = 0;

        if (feedbackList == null) {
            return count;
        }

        for (Feedback feedback : feedbackList) {
            if (feedback.isToday()) {
                count++;
            }
        }

        return count;
    }

    private void redirectToFeedback(HttpServletRequest request,
                                    HttpServletResponse response,
                                    String messageType,
                                    String messageCode)
            throws IOException {

        response.sendRedirect(
                request.getContextPath()
                        + "/feedback?"
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

    private static class FeedbackSafe {
        private static boolean isStudent(String role) {
            return User.ROLE_STUDENT.equalsIgnoreCase(role);
        }
    }
}