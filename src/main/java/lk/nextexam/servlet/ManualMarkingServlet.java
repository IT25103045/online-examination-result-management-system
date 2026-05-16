package lk.nextexam.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lk.nextexam.dao.ExamSubmissionDAO;
import lk.nextexam.dao.FileUtil;
import lk.nextexam.dao.ManualMarkDAO;
import lk.nextexam.dao.QuestionDAO;
import lk.nextexam.model.ExamSubmission;
import lk.nextexam.model.ManualMark;
import lk.nextexam.model.Question;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * ManualMarkingServlet controls staff essay/manual marking.
 *
 * URL:
 * /manual-marking?submissionId=SUB001
 *
 * Responsible Member:
 * IT25103045 - De Silva H.L.D.C.P.C
 */
@WebServlet("/manual-marking")
public class ManualMarkingServlet extends HttpServlet {

    private static final String ROLE_ADMIN = "Admin";
    private static final String ROLE_LECTURER = "Lecturer";

    private final ExamSubmissionDAO submissionDAO = new ExamSubmissionDAO();
    private final QuestionDAO questionDAO = new QuestionDAO();
    private final ManualMarkDAO manualMarkDAO = new ManualMarkDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

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

        List<Question> allQuestions = questionDAO.getStudentVisibleQuestionsByExamId(
                getServletContext(),
                submission.getExamId()
        );

        List<Question> essayQuestions = getEssayQuestions(allQuestions);

        if (essayQuestions.isEmpty()) {
            redirectToSubmissions(request, response, "error", "noEssayQuestions");
            return;
        }

        request.setAttribute("submission", submission);
        request.setAttribute("essayQuestions", essayQuestions);
        request.setAttribute("mcqScore", calculateMcqScore(submission, allQuestions));
        request.setAttribute("existingManualMarks", manualMarkDAO.getMarksBySubmission(getServletContext(), submissionId));

        request.getRequestDispatcher("/manual-marking/index.jsp").forward(request, response);
    }

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

        if (submission.isPublished() || submission.isCancelled()) {
            redirectToManualMarking(request, response, submissionId, "error", "finalizedSubmission");
            return;
        }

        List<Question> allQuestions = questionDAO.getStudentVisibleQuestionsByExamId(
                getServletContext(),
                submission.getExamId()
        );

        List<Question> essayQuestions = getEssayQuestions(allQuestions);

        if (essayQuestions.isEmpty()) {
            redirectToSubmissions(request, response, "error", "noEssayQuestions");
            return;
        }

        String markedBy = getStaffDisplayName(session);
        double manualScore = 0.0;

        for (Question question : essayQuestions) {
            String questionId = question.getQuestionId();
            String markValue = FileUtil.clean(request.getParameter("marks_" + questionId));
            String feedback = FileUtil.clean(request.getParameter("feedback_" + questionId));

            if (markValue.isEmpty()) {
                redirectToManualMarking(request, response, submissionId, "error", "missingMarks");
                return;
            }

            double awardedMarks;

            try {
                awardedMarks = Double.parseDouble(markValue);
            } catch (NumberFormatException e) {
                redirectToManualMarking(request, response, submissionId, "error", "invalidMarks");
                return;
            }

            if (awardedMarks < 0 || awardedMarks > question.getMarksAsDouble()) {
                redirectToManualMarking(request, response, submissionId, "error", "marksOutOfRange");
                return;
            }

            ManualMark mark = new ManualMark(
                    FileUtil.generateId("MM"),
                    submission.getSubmissionId(),
                    submission.getExamId(),
                    submission.getStudentId(),
                    questionId,
                    formatNumber(awardedMarks),
                    feedback,
                    markedBy,
                    manualMarkDAO.now()
            );

            boolean saved = manualMarkDAO.saveOrUpdateMark(getServletContext(), mark);

            if (!saved) {
                redirectToManualMarking(request, response, submissionId, "error", "markSaveFailed");
                return;
            }

            manualScore += awardedMarks;
        }

        double mcqScore = calculateMcqScore(submission, allQuestions);
        double finalScore = mcqScore + manualScore;
        double totalMarks = submission.getTotalMarksAsDouble();

        boolean updated = submissionDAO.updateScore(
                getServletContext(),
                submissionId,
                finalScore,
                totalMarks,
                ExamSubmission.STATUS_MARKED
        );

        if (!updated) {
            redirectToManualMarking(request, response, submissionId, "error", "submissionUpdateFailed");
            return;
        }

        redirectToManualMarking(request, response, submissionId, "success", "manualMarkingSaved");
    }

    private List<Question> getEssayQuestions(List<Question> questions) {
        List<Question> essayQuestions = new ArrayList<>();

        if (questions == null) {
            return essayQuestions;
        }

        for (Question question : questions) {
            if (question != null && question.isEssay()) {
                essayQuestions.add(question);
            }
        }

        return essayQuestions;
    }

    private double calculateMcqScore(ExamSubmission submission, List<Question> questions) {
        double score = 0.0;

        if (submission == null || questions == null) {
            return score;
        }

        for (Question question : questions) {
            if (question != null && question.isMcq()) {
                String submittedAnswer = extractSubmittedAnswer(
                        submission.getAnswersData(),
                        question.getQuestionId()
                );

                if (question.isCorrectMcqAnswer(submittedAnswer)) {
                    score += question.getMarksAsDouble();
                }
            }
        }

        return score;
    }

    private String extractSubmittedAnswer(String answersData, String questionId) {
        if (answersData == null || questionId == null) {
            return "";
        }

        String[] items = answersData.split(";");

        for (String item : items) {
            if (item == null) {
                continue;
            }

            String cleanItem = item.trim();
            String prefix = questionId + "=";

            if (cleanItem.startsWith(prefix)) {
                String value = cleanItem.substring(prefix.length());
                int flaggedIndex = value.indexOf(",flagged=");

                if (flaggedIndex >= 0) {
                    value = value.substring(0, flaggedIndex);
                }

                if ("NO_ANSWER".equalsIgnoreCase(value)) {
                    return "";
                }

                return value.trim();
            }
        }

        return "";
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

    private void redirectToManualMarking(HttpServletRequest request,
                                         HttpServletResponse response,
                                         String submissionId,
                                         String type,
                                         String code)
            throws IOException {

        response.sendRedirect(request.getContextPath()
                + "/manual-marking?submissionId="
                + urlEncode(submissionId)
                + "&"
                + urlEncode(type)
                + "="
                + urlEncode(code));
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private String formatNumber(double value) {
        if (value == Math.floor(value)) {
            return String.valueOf((int) value);
        }

        return String.format("%.2f", value);
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