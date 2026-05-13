package lk.nextexam.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.nextexam.dao.ExamDAO;
import lk.nextexam.dao.FileUtil;
import lk.nextexam.model.Exam;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Professional controller for Exam Management.
 *
 * Supported actions:
 * - GET  /exams
 * - POST /exams action=add
 * - POST /exams action=update
 * - POST /exams action=delete
 * - POST /exams action=status
 *
 * Backward compatibility:
 * - GET /exams?action=delete&id=EX001 still works,
 *   but POST delete is recommended.
 */
@WebServlet("/exams")
public class ExamServlet extends HttpServlet {

    private static final String ACTION_ADD = "add";
    private static final String ACTION_UPDATE = "update";
    private static final String ACTION_DELETE = "delete";
    private static final String ACTION_STATUS = "status";

    private final ExamDAO examDAO = new ExamDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        prepareRequestResponse(request, response);

        String action = FileUtil.clean(request.getParameter("action"));

        if (ACTION_DELETE.equalsIgnoreCase(action)) {
            deleteExamFromGet(request, response);
            return;
        }

        showExamPage(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        prepareRequestResponse(request, response);

        String action = FileUtil.clean(request.getParameter("action"));

        if (ACTION_ADD.equalsIgnoreCase(action)) {
            addExam(request, response);
            return;
        }

        if (ACTION_UPDATE.equalsIgnoreCase(action)) {
            updateExam(request, response);
            return;
        }

        if (ACTION_DELETE.equalsIgnoreCase(action)) {
            deleteExam(request, response);
            return;
        }

        if (ACTION_STATUS.equalsIgnoreCase(action)) {
            updateExamStatus(request, response);
            return;
        }

        redirectToExams(request, response, "error", "invalidAction");
    }

    private void showExamPage(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setAttribute("exams", examDAO.getAllExams(getServletContext()));

        request.setAttribute("totalExamCount", examDAO.countAllExams(getServletContext()));
        request.setAttribute("draftExamCount", examDAO.countDraftExams(getServletContext()));
        request.setAttribute("scheduledExamCount", examDAO.countScheduledExams(getServletContext()));
        request.setAttribute("activeExamCount", examDAO.countActiveExams(getServletContext()));
        request.setAttribute("ongoingExamCount", examDAO.countOngoingExams(getServletContext()));
        request.setAttribute("attemptableExamCount", examDAO.countAttemptableExams(getServletContext()));
        request.setAttribute("completedExamCount", examDAO.countCompletedExams(getServletContext()));
        request.setAttribute("publishedExamCount", examDAO.countPublishedExams(getServletContext()));
        request.setAttribute("cancelledExamCount", examDAO.countCancelledExams(getServletContext()));
        request.setAttribute("inactiveExamCount", examDAO.countInactiveExams(getServletContext()));
        request.setAttribute("todayExamCount", examDAO.countTodayExams(getServletContext()));
        request.setAttribute("upcomingExamCount", examDAO.countUpcomingExams(getServletContext()));

        request.getRequestDispatcher("/exams/index.jsp").forward(request, response);
    }

    private void addExam(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        Exam exam = buildExamFromRequest(request);
        String validationError = validateExam(exam);

        if (validationError != null) {
            redirectToExams(request, response, "error", validationError);
            return;
        }

        boolean success = examDAO.addExam(getServletContext(), exam);

        if (success) {
            redirectToExams(request, response, "success", "examAdded");
        } else {
            redirectToExams(request, response, "error", "examAddFailed");
        }
    }

    private void updateExam(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        Exam exam = buildExamFromRequest(request);
        String validationError = validateExam(exam);

        if (validationError != null) {
            redirectToExams(request, response, "error", validationError);
            return;
        }

        boolean success = examDAO.updateExam(getServletContext(), exam);

        if (success) {
            redirectToExams(request, response, "success", "examUpdated");
        } else {
            redirectToExams(request, response, "error", "examUpdateFailed");
        }
    }

    private void deleteExam(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String examId = firstNonBlank(
                request.getParameter("recordId"),
                request.getParameter("examId"),
                request.getParameter("id")
        );

        deleteExamById(request, response, examId);
    }

    /**
     * Backward-compatible GET delete.
     * Prefer POST delete in professional UI.
     */
    private void deleteExamFromGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String examId = firstNonBlank(
                request.getParameter("id"),
                request.getParameter("recordId"),
                request.getParameter("examId")
        );

        deleteExamById(request, response, examId);
    }

    private void deleteExamById(HttpServletRequest request,
                                HttpServletResponse response,
                                String examId)
            throws IOException {

        String cleanExamId = FileUtil.clean(examId);

        if (cleanExamId.isEmpty()) {
            redirectToExams(request, response, "error", "missingExamId");
            return;
        }

        Exam existingExam = examDAO.getExamById(getServletContext(), cleanExamId);

        if (existingExam == null) {
            redirectToExams(request, response, "error", "examNotFound");
            return;
        }

        boolean success = examDAO.deleteExam(getServletContext(), cleanExamId);

        if (success) {
            redirectToExams(request, response, "success", "examDeleted");
        } else {
            redirectToExams(request, response, "error", "examDeleteFailed");
        }
    }

    private void updateExamStatus(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String examId = firstNonBlank(
                request.getParameter("recordId"),
                request.getParameter("examId"),
                request.getParameter("id")
        );

        String newStatus = FileUtil.clean(request.getParameter("status"));

        if (examId.isEmpty()) {
            redirectToExams(request, response, "error", "missingExamId");
            return;
        }

        if (newStatus.isEmpty()) {
            redirectToExams(request, response, "error", "missingStatus");
            return;
        }

        Exam existingExam = examDAO.getExamById(getServletContext(), examId);

        if (existingExam == null) {
            redirectToExams(request, response, "error", "examNotFound");
            return;
        }

        existingExam.setStatus(newStatus);

        if (!existingExam.isValidStatus()) {
            redirectToExams(request, response, "error", "invalidStatus");
            return;
        }

        boolean success = examDAO.updateExamStatus(getServletContext(), examId, newStatus);

        if (success) {
            redirectToExams(request, response, "success", "examStatusUpdated");
        } else {
            redirectToExams(request, response, "error", "examStatusUpdateFailed");
        }
    }

    private Exam buildExamFromRequest(HttpServletRequest request) {
        return new Exam(
                FileUtil.clean(request.getParameter("examId")),
                FileUtil.clean(request.getParameter("subject")),
                FileUtil.clean(request.getParameter("examDate")),
                FileUtil.clean(request.getParameter("duration")),
                FileUtil.clean(request.getParameter("totalMarks")),
                FileUtil.clean(request.getParameter("status"))
        );
    }

    private String validateExam(Exam exam) {
        if (exam == null) {
            return "invalidExam";
        }

        if (exam.getExamId().isEmpty()) {
            return "missingExamId";
        }

        if (exam.getSubject().isEmpty()) {
            return "missingSubject";
        }

        if (exam.getExamDate().isEmpty()) {
            return "missingExamDate";
        }

        if (!exam.isValidExamDate()) {
            return "invalidExamDate";
        }

        if (exam.getDuration().isEmpty()) {
            return "missingDuration";
        }

        if (!exam.isValidDuration()) {
            return "invalidDuration";
        }

        if (exam.getTotalMarks().isEmpty()) {
            return "missingTotalMarks";
        }

        if (!exam.isValidTotalMarks()) {
            return "invalidTotalMarks";
        }

        if (exam.getStatus().isEmpty()) {
            return "missingStatus";
        }

        if (!exam.isValidStatus()) {
            return "invalidStatus";
        }

        if (!exam.isCompleteForSave()) {
            return "incompleteExam";
        }

        return null;
    }

    private void redirectToExams(HttpServletRequest request,
                                 HttpServletResponse response,
                                 String messageType,
                                 String messageCode)
            throws IOException {

        response.sendRedirect(
                request.getContextPath()
                        + "/exams?"
                        + urlEncode(messageType)
                        + "="
                        + urlEncode(messageCode)
        );
    }

    private void prepareRequestResponse(HttpServletRequest request,
                                        HttpServletResponse response)
            throws IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
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

    private String urlEncode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}