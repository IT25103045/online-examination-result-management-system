package lk.nextexam.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lk.nextexam.dao.ExamDAO;
import lk.nextexam.dao.ExamIntegrityLogDAO;
import lk.nextexam.dao.ExamSubmissionDAO;
import lk.nextexam.dao.FileUtil;
import lk.nextexam.dao.QuestionDAO;
import lk.nextexam.model.Exam;
import lk.nextexam.model.ExamIntegrityLog;
import lk.nextexam.model.ExamSubmission;
import lk.nextexam.model.Question;
import lk.nextexam.model.User;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Professional servlet for final exam submission.
 *
 * Pack 23:
 * - Prevents duplicate submission
 * - Supports auto-submit timeout workflow
 * - Validates server-side exam deadline from session
 * - Saves NO_ANSWER for missing answers
 * - Logs timeout, late, duplicate, and successful submissions
 *
 * URL:
 * /submit-exam
 *
 * Responsible Member:
 * IT25103045 - De Silva H.L.D.C.P.C
 */
@WebServlet("/submit-exam")
public class ExamSubmissionServlet extends HttpServlet {

    private static final long SUBMISSION_GRACE_SECONDS = 10L;

    private final ExamDAO examDAO = new ExamDAO();
    private final QuestionDAO questionDAO = new QuestionDAO();
    private final ExamSubmissionDAO submissionDAO = new ExamSubmissionDAO();
    private final ExamIntegrityLogDAO integrityLogDAO = new ExamIntegrityLogDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        prepareRequestResponse(request, response);

        HttpSession session = request.getSession(false);

        String studentId = getSessionValue(session, "userId");
        String studentName = getStudentDisplayName(session);
        String userRole = getSessionValue(session, "userRole");

        if (!User.ROLE_STUDENT.equalsIgnoreCase(userRole)) {
            redirectToMyExams(request, response, "error", "accessDenied");
            return;
        }

        if (studentId.isEmpty()) {
            redirectToMyExams(request, response, "error", "sessionExpired");
            return;
        }

        String examId = FileUtil.clean(request.getParameter("examId"));
        String autoSubmit = FileUtil.clean(request.getParameter("autoSubmit"));
        String clientSubmitReason = FileUtil.clean(request.getParameter("clientSubmitReason"));
        String remainingSecondsAtSubmit = FileUtil.clean(request.getParameter("remainingSecondsAtSubmit"));

        boolean timeoutAutoSubmit = "true".equalsIgnoreCase(autoSubmit)
                || "timeout".equalsIgnoreCase(clientSubmitReason);

        if (examId.isEmpty()) {
            redirectToMyExams(request, response, "error", "missingExamId");
            return;
        }

        Exam exam = examDAO.getExamById(getServletContext(), examId);

        if (exam == null) {
            redirectToMyExams(request, response, "error", "examNotFound");
            return;
        }

        if (submissionDAO.hasStudentSubmitted(getServletContext(), studentId, examId)) {
            integrityLogDAO.addLog(
                    getServletContext(),
                    studentId,
                    examId,
                    ExamIntegrityLog.EVENT_EXAM_SUBMITTED,
                    "Duplicate submission attempt blocked"
            );

            redirectToMyExams(request, response, "error", "alreadySubmitted");
            return;
        }

        if (!exam.canStudentAttempt() && !timeoutAutoSubmit) {
            integrityLogDAO.addLog(
                    getServletContext(),
                    studentId,
                    examId,
                    ExamIntegrityLog.EVENT_EXAM_SUBMITTED,
                    "Manual submission blocked because exam is no longer attemptable"
            );

            redirectToMyExams(request, response, "error", "examUnavailable");
            return;
        }

        long nowMillis = System.currentTimeMillis();
        String deadlineKey = "examAttemptDeadlineAt_" + examId + "_" + studentId;
        Long deadlineAtMillis = getLongSessionValue(session, deadlineKey);

        boolean deadlineKnown = deadlineAtMillis != null && deadlineAtMillis > 0;
        boolean deadlinePassed = deadlineKnown && nowMillis > deadlineAtMillis + (SUBMISSION_GRACE_SECONDS * 1000L);

        if (deadlinePassed && !timeoutAutoSubmit) {
            integrityLogDAO.addLog(
                    getServletContext(),
                    studentId,
                    examId,
                    ExamIntegrityLog.EVENT_EXAM_SUBMITTED,
                    "Late manual submission blocked. Client remaining seconds: " + remainingSecondsAtSubmit
            );

            redirectToMyExams(request, response, "error", "examTimeExpired");
            return;
        }

        String readinessMessage = questionDAO.getExamReadinessMessage(getServletContext(), examId);

        if (!"OK".equalsIgnoreCase(readinessMessage)) {
            redirectToMyExams(request, response, "error", "examNotReady");
            return;
        }

        List<Question> questions = questionDAO.getStudentVisibleQuestionsByExamId(getServletContext(), examId);

        if (questions == null || questions.isEmpty()) {
            redirectToMyExams(request, response, "error", "noActiveQuestions");
            return;
        }

        SubmissionCalculation calculation = calculateSubmission(request, questions);

        String submissionStatus = calculation.requiresManualReview
                ? ExamSubmission.STATUS_MANUAL_REVIEW_REQUIRED
                : ExamSubmission.STATUS_AUTO_MARKED;

        String submissionId = FileUtil.generateId("SUB");
        String timestamp = ExamSubmission.nowTimestamp();

        String finalAnswersData = calculation.answersData;

        if (timeoutAutoSubmit) {
            finalAnswersData = finalAnswersData
                    + ";META_SUBMIT_REASON=TIMEOUT_AUTO_SUBMIT"
                    + ",remainingSecondsAtSubmit=" + encodeAnswerValue(remainingSecondsAtSubmit);
        } else {
            finalAnswersData = finalAnswersData
                    + ";META_SUBMIT_REASON=MANUAL_SUBMIT"
                    + ",remainingSecondsAtSubmit=" + encodeAnswerValue(remainingSecondsAtSubmit);
        }

        ExamSubmission submission = new ExamSubmission(
                submissionId,
                examId,
                studentId,
                studentName,
                timestamp,
                finalAnswersData,
                formatNumber(calculation.score),
                formatNumber(calculation.totalMarks),
                submissionStatus
        );

        boolean saved = submissionDAO.addSubmission(getServletContext(), submission);

        if (saved) {
            clearExamAttemptSession(session, examId, studentId);

            integrityLogDAO.addLog(
                    getServletContext(),
                    studentId,
                    examId,
                    ExamIntegrityLog.EVENT_EXAM_SUBMITTED,
                    timeoutAutoSubmit
                            ? "Exam auto-submitted after timer expiry"
                            : "Student submitted final answers manually"
            );

            redirectToMyExams(
                    request,
                    response,
                    "success",
                    timeoutAutoSubmit ? "examAutoSubmitted" : "examSubmitted"
            );
        } else {
            integrityLogDAO.addLog(
                    getServletContext(),
                    studentId,
                    examId,
                    ExamIntegrityLog.EVENT_EXAM_SUBMITTED,
                    "Submission save failed"
            );

            redirectToMyExams(request, response, "error", "submissionFailed");
        }
    }

    private SubmissionCalculation calculateSubmission(HttpServletRequest request, List<Question> questions) {
        SubmissionCalculation calculation = new SubmissionCalculation();

        for (Question question : questions) {
            String questionId = question.getQuestionId();
            String rawAnswer = request.getParameter("answer_" + questionId);
            String rawFlagged = request.getParameter("flagged_" + questionId);

            String answer = FileUtil.clean(rawAnswer);
            String flagged = FileUtil.clean(rawFlagged);

            calculation.totalMarks += question.getMarksAsDouble();

            if (question.isMcq()) {
                if (question.isCorrectMcqAnswer(answer)) {
                    calculation.score += question.getMarksAsDouble();
                }
            }

            if (question.requiresManualMarking()) {
                calculation.requiresManualReview = true;
            }

            if (calculation.answersBuilder.length() > 0) {
                calculation.answersBuilder.append(";");
            }

            calculation.answersBuilder
                    .append(questionId)
                    .append("=")
                    .append(encodeAnswerValue(answer))
                    .append(",flagged=")
                    .append(isFlagged(flagged) ? "YES" : "NO")
                    .append(",type=")
                    .append(question.getQuestionType());
        }

        calculation.answersData = calculation.answersBuilder.toString();
        return calculation;
    }

    private String encodeAnswerValue(String answer) {
        String cleanAnswer = FileUtil.clean(answer);

        if (cleanAnswer.isEmpty()) {
            return "NO_ANSWER";
        }

        return cleanAnswer
                .replace(";", " ")
                .replace(",", " ")
                .replace("=", " ")
                .replace("|", " ")
                .replaceAll("\\s{2,}", " ")
                .trim();
    }

    private boolean isFlagged(String flaggedValue) {
        return "on".equalsIgnoreCase(flaggedValue)
                || "yes".equalsIgnoreCase(flaggedValue)
                || "true".equalsIgnoreCase(flaggedValue)
                || "1".equals(flaggedValue);
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

        String email = getSessionValue(session, "userEmail");

        if (!email.isEmpty()) {
            return email;
        }

        return "Student";
    }

    private String getSessionValue(HttpSession session, String key) {
        if (session == null || key == null) {
            return "";
        }

        Object value = session.getAttribute(key);
        return value == null ? "" : String.valueOf(value).trim();
    }

    private Long getLongSessionValue(HttpSession session, String key) {
        if (session == null || key == null) {
            return null;
        }

        Object value = session.getAttribute(key);

        if (value instanceof Long) {
            return (Long) value;
        }

        if (value instanceof Number) {
            return ((Number) value).longValue();
        }

        try {
            return Long.parseLong(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }

    private void clearExamAttemptSession(HttpSession session, String examId, String studentId) {
        if (session == null) {
            return;
        }

        session.removeAttribute("examAttemptStartedAt_" + examId + "_" + studentId);
        session.removeAttribute("examAttemptDeadlineAt_" + examId + "_" + studentId);
    }

    private void redirectToMyExams(HttpServletRequest request,
                                   HttpServletResponse response,
                                   String messageType,
                                   String messageCode)
            throws IOException {

        response.sendRedirect(
                request.getContextPath()
                        + "/my-exams?"
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

        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, private");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
    }

    private String formatNumber(double value) {
        if (value == Math.floor(value)) {
            return String.valueOf((int) value);
        }

        return String.format("%.2f", value);
    }

    private static class SubmissionCalculation {
        private double score = 0.0;
        private double totalMarks = 0.0;
        private boolean requiresManualReview = false;
        private String answersData = "";
        private final StringBuilder answersBuilder = new StringBuilder();
    }
}