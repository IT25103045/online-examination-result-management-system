package lk.nextexam.servlet;

import jakarta.servlet.ServletException;
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
import lk.nextexam.model.Question;
import lk.nextexam.model.User;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * ExamRulesServlet controls the exam rules agreement page before the exam console.
 *
 * This feature improves exam integrity by requiring students to read and accept
 * examination rules before entering the secure online exam environment.
 *
 * Responsible Member:
 * IT25103045 - De Silva H.L.D.C.P.C
 */
@WebServlet("/exam-rules")
public class ExamRulesServlet extends HttpServlet {

    private static final String EVENT_RULES_ACCEPTED = "EXAM_RULES_ACCEPTED";

    private final ExamDAO examDAO = new ExamDAO();
    private final QuestionDAO questionDAO = new QuestionDAO();
    private final ExamSubmissionDAO submissionDAO = new ExamSubmissionDAO();
    private final ExamIntegrityLogDAO integrityLogDAO = new ExamIntegrityLogDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        prepareRequestResponse(request, response);

        HttpSession session = request.getSession(false);

        if (!isStudentAuthenticated(session)) {
            response.sendRedirect(request.getContextPath() + "/login.jsp?error=sessionExpired");
            return;
        }

        String studentId = getSessionValue(session, "userId");
        String studentName = getStudentDisplayName(session);
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

        List<Question> questions = questionDAO.getStudentVisibleQuestionsByExamId(getServletContext(), examId);

        if (questions == null || questions.isEmpty()) {
            redirectToMyExams(request, response, "error", "noActiveQuestions");
            return;
        }

        ExamRuleStats stats = calculateStats(questions);

        request.setAttribute("exam", exam);
        request.setAttribute("studentId", studentId);
        request.setAttribute("studentName", studentName);
        request.setAttribute("questionCount", stats.questionCount);
        request.setAttribute("mcqQuestionCount", stats.mcqQuestionCount);
        request.setAttribute("essayQuestionCount", stats.essayQuestionCount);
        request.setAttribute("totalMarks", stats.totalMarks);
        request.setAttribute("requiresManualReview", stats.essayQuestionCount > 0);

        request.getRequestDispatcher("/exam-rules/index.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        prepareRequestResponse(request, response);

        HttpSession session = request.getSession(false);

        if (!isStudentAuthenticated(session)) {
            response.sendRedirect(request.getContextPath() + "/login.jsp?error=sessionExpired");
            return;
        }

        String studentId = getSessionValue(session, "userId");
        String studentName = getStudentDisplayName(session);
        String examId = FileUtil.clean(request.getParameter("examId"));
        String agreement = FileUtil.clean(request.getParameter("agreement"));

        if (examId.isEmpty()) {
            redirectToMyExams(request, response, "error", "missingExamId");
            return;
        }

        if (!"accepted".equalsIgnoreCase(agreement)) {
            response.sendRedirect(request.getContextPath()
                    + "/exam-rules?examId="
                    + urlEncode(examId)
                    + "&error=agreementRequired");
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

        session.setAttribute(getRulesSessionKey(examId), "accepted");

        integrityLogDAO.addLog(
                getServletContext(),
                studentId,
                examId,
                EVENT_RULES_ACCEPTED,
                studentName + " accepted the exam rules before starting the exam"
        );

        response.sendRedirect(request.getContextPath()
                + "/exam-console?examId="
                + urlEncode(examId));
    }

    private ExamRuleStats calculateStats(List<Question> questions) {
        ExamRuleStats stats = new ExamRuleStats();

        if (questions == null) {
            return stats;
        }

        stats.questionCount = questions.size();

        for (Question question : questions) {
            double marks = question.getMarksAsDouble();
            stats.totalMarks += marks;

            if (question.isMcq()) {
                stats.mcqQuestionCount++;
            }

            if (question.isEssay()) {
                stats.essayQuestionCount++;
            }
        }

        return stats;
    }

    private boolean isStudentAuthenticated(HttpSession session) {
        if (session == null) {
            return false;
        }

        String role = getSessionValue(session, "userRole");

        return session.getAttribute("loggedUser") != null
                && session.getAttribute("userId") != null
                && "authenticated".equals(String.valueOf(session.getAttribute("loginStatus")))
                && User.ROLE_STUDENT.equalsIgnoreCase(role);
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

    private String getRulesSessionKey(String examId) {
        return "examRulesAccepted_" + FileUtil.clean(examId);
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

    private static class ExamRuleStats {
        private int questionCount = 0;
        private int mcqQuestionCount = 0;
        private int essayQuestionCount = 0;
        private double totalMarks = 0.0;
    }
}