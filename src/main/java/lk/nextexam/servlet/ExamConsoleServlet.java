package lk.nextexam.servlet;

import lk.nextexam.dao.ExamIntegrityLogDAO;
import lk.nextexam.model.ExamIntegrityLog;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lk.nextexam.dao.ExamDAO;
import lk.nextexam.dao.ExamSubmissionDAO;
import lk.nextexam.dao.FileUtil;
import lk.nextexam.dao.QuestionDAO;
import lk.nextexam.model.Exam;
import lk.nextexam.model.Question;
import lk.nextexam.model.User;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Professional servlet for loading the secure student exam console.
 *
 * Pack 23:
 * - Server-side attempt deadline stored in session
 * - Server-calculated remaining seconds sent to JSP
 * - Expired attempts are blocked safely
 * - Duplicate attempts are prevented
 *
 * Responsible Member:
 * IT25103045 - De Silva H.L.D.C.P.C
 */
@WebServlet("/exam-console")
public class ExamConsoleServlet extends HttpServlet {

    private final ExamDAO examDAO = new ExamDAO();
    private final QuestionDAO questionDAO = new QuestionDAO();
    private final ExamSubmissionDAO submissionDAO = new ExamSubmissionDAO();
    private final ExamIntegrityLogDAO integrityLogDAO = new ExamIntegrityLogDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

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

        if (examId.isEmpty()) {
            redirectToMyExams(request, response, "error", "missingExamId");
            return;
        }

        Exam exam = examDAO.getExamById(getServletContext(), examId);

        if (exam == null) {
            redirectToMyExams(request, response, "error", "examNotFound");
            return;
        }

        if (!exam.canStudentAttempt()) {
            redirectToMyExams(request, response, "error", "examUnavailable");
            return;
        }

        if (submissionDAO.hasStudentSubmitted(getServletContext(), studentId, examId)) {
            redirectToMyExams(request, response, "error", "alreadySubmitted");
            return;
        }

        String readinessMessage = questionDAO.getExamReadinessMessage(getServletContext(), examId);

        if (!"OK".equalsIgnoreCase(readinessMessage)) {
            redirectToMyExams(request, response, "error", "examNotReady");
            return;
        }

        String rulesKey = "examRulesAccepted_" + examId;
        Object rulesAccepted = session.getAttribute(rulesKey);

        if (!"accepted".equals(String.valueOf(rulesAccepted))) {
            response.sendRedirect(
                    request.getContextPath()
                            + "/exam-rules?examId="
                            + urlEncode(examId)
                            + "&error=rulesRequired"
            );
            return;
        }

        List<Question> questions = questionDAO.getStudentVisibleQuestionsByExamId(getServletContext(), examId);

        if (questions == null || questions.isEmpty()) {
            redirectToMyExams(request, response, "error", "noActiveQuestions");
            return;
        }

        long nowMillis = System.currentTimeMillis();
        long examDurationMillis = Math.max(1, exam.getDurationMinutes()) * 60L * 1000L;

        String attemptStartKey = "examAttemptStartedAt_" + examId + "_" + studentId;
        String attemptDeadlineKey = "examAttemptDeadlineAt_" + examId + "_" + studentId;

        Long deadlineAtMillis = getLongSessionValue(session, attemptDeadlineKey);

        if (deadlineAtMillis == null || deadlineAtMillis <= 0) {
            long startedAtMillis = nowMillis;
            deadlineAtMillis = startedAtMillis + examDurationMillis;

            session.setAttribute(attemptStartKey, startedAtMillis);
            session.setAttribute(attemptDeadlineKey, deadlineAtMillis);
        }

        long remainingSeconds = Math.max(0, (deadlineAtMillis - nowMillis) / 1000L);

        if (remainingSeconds <= 0) {
            integrityLogDAO.addLog(
                    getServletContext(),
                    studentId,
                    examId,
                    ExamIntegrityLog.EVENT_EXAM_SUBMITTED,
                    "Student attempted to open exam console after timer expired"
            );

            redirectToMyExams(request, response, "error", "examTimeExpired");
            return;
        }

        ExamConsoleStats stats = calculateStats(questions);

        request.setAttribute("exam", exam);
        request.setAttribute("questions", questions);

        request.setAttribute("studentId", studentId);
        request.setAttribute("studentName", studentName);

        request.setAttribute("totalMarks", stats.totalMarks);
        request.setAttribute("questionCount", stats.questionCount);
        request.setAttribute("mcqQuestionCount", stats.mcqQuestionCount);
        request.setAttribute("essayQuestionCount", stats.essayQuestionCount);
        request.setAttribute("mcqMarks", stats.mcqMarks);
        request.setAttribute("essayMarks", stats.essayMarks);
        request.setAttribute("requiresManualReview", stats.essayQuestionCount > 0);
        request.setAttribute("readinessMessage", readinessMessage);

        request.setAttribute("serverRemainingSeconds", remainingSeconds);
        request.setAttribute("serverDeadlineAtMillis", deadlineAtMillis);

        integrityLogDAO.addLog(
                getServletContext(),
                studentId,
                examId,
                ExamIntegrityLog.EVENT_EXAM_STARTED,
                studentName + " opened the secure exam console. Remaining seconds: " + remainingSeconds
        );

        request.getRequestDispatcher("/exam-console/index.jsp").forward(request, response);
    }

    private ExamConsoleStats calculateStats(List<Question> questions) {
        ExamConsoleStats stats = new ExamConsoleStats();

        if (questions == null) {
            return stats;
        }

        stats.questionCount = questions.size();

        for (Question question : questions) {
            double marks = question.getMarksAsDouble();

            stats.totalMarks += marks;

            if (question.isMcq()) {
                stats.mcqQuestionCount++;
                stats.mcqMarks += marks;
            }

            if (question.isEssay()) {
                stats.essayQuestionCount++;
                stats.essayMarks += marks;
            }
        }

        return stats;
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

    private static class ExamConsoleStats {
        private int questionCount = 0;
        private int mcqQuestionCount = 0;
        private int essayQuestionCount = 0;
        private double totalMarks = 0.0;
        private double mcqMarks = 0.0;
        private double essayMarks = 0.0;
    }
}