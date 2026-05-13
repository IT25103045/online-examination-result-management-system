package lk.nextexam.servlet;

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
import lk.nextexam.model.ExamSubmission;
import lk.nextexam.model.User;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Professional servlet for student exam listing.
 *
 * Responsibilities:
 * - Validate authenticated student session.
 * - Load attemptable exams.
 * - Load student's previous submissions.
 * - Prepare question counts, marks, readiness status, and submission status maps.
 * - Provide clean attributes for /my-exams/index.jsp.
 */
@WebServlet("/my-exams")
public class MyExamServlet extends HttpServlet {

    private final ExamDAO examDAO = new ExamDAO();
    private final QuestionDAO questionDAO = new QuestionDAO();
    private final ExamSubmissionDAO submissionDAO = new ExamSubmissionDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        prepareRequestResponse(request, response);

        HttpSession session = request.getSession(false);

        String studentId = getSessionValue(session, "userId");
        String studentName = getStudentDisplayName(session);
        String userRole = getSessionValue(session, "userRole");

        if (!User.ROLE_STUDENT.equalsIgnoreCase(userRole)) {
            redirectToDashboard(request, response, "accessDenied");
            return;
        }

        if (studentId.isEmpty()) {
            redirectToLogin(request, response, "sessionExpired");
            return;
        }

        List<Exam> attemptableExams = examDAO.getAttemptableExams(getServletContext());
        List<ExamSubmission> mySubmissions = submissionDAO.getSubmissionsByStudent(getServletContext(), studentId);

        Map<String, Integer> questionCounts = new HashMap<>();
        Map<String, Integer> mcqQuestionCounts = new HashMap<>();
        Map<String, Integer> essayQuestionCounts = new HashMap<>();

        Map<String, Double> examTotalMarks = new HashMap<>();
        Map<String, Double> mcqMarks = new HashMap<>();
        Map<String, Double> essayMarks = new HashMap<>();

        Map<String, Boolean> submittedMap = new HashMap<>();
        Map<String, Boolean> readyMap = new HashMap<>();
        Map<String, String> readinessMessageMap = new HashMap<>();
        Map<String, ExamSubmission> submissionMap = new HashMap<>();

        int availableExamCount = 0;
        int readyExamCount = 0;
        int submittedExamCount = 0;
        int pendingExamCount = 0;
        int manualReviewCount = 0;
        int publishedResultCount = 0;

        for (Exam exam : attemptableExams) {
            String examId = exam.getExamId();

            int questionCount = questionDAO.countStudentVisibleQuestionsByExamId(getServletContext(), examId);
            int mcqCount = questionDAO.countMcqQuestionsByExamId(getServletContext(), examId);
            int essayCount = questionDAO.countEssayQuestionsByExamId(getServletContext(), examId);

            double totalMarks = questionDAO.calculateTotalMarksByExamId(getServletContext(), examId);
            double totalMcqMarks = questionDAO.calculateMcqMarksByExamId(getServletContext(), examId);
            double totalEssayMarks = questionDAO.calculateEssayMarksByExamId(getServletContext(), examId);

            String readinessMessage = questionDAO.getExamReadinessMessage(getServletContext(), examId);
            boolean ready = "OK".equalsIgnoreCase(readinessMessage);

            ExamSubmission submission = submissionDAO.getSubmissionByStudentAndExam(
                    getServletContext(),
                    studentId,
                    examId
            );

            boolean submitted = submission != null;

            questionCounts.put(examId, questionCount);
            mcqQuestionCounts.put(examId, mcqCount);
            essayQuestionCounts.put(examId, essayCount);

            examTotalMarks.put(examId, totalMarks);
            mcqMarks.put(examId, totalMcqMarks);
            essayMarks.put(examId, totalEssayMarks);

            readyMap.put(examId, ready);
            readinessMessageMap.put(examId, readinessMessage);
            submittedMap.put(examId, submitted);

            if (submission != null) {
                submissionMap.put(examId, submission);
            }

            availableExamCount++;

            if (ready) {
                readyExamCount++;
            }

            if (submitted) {
                submittedExamCount++;

                if (submission.isManualReviewRequired()) {
                    manualReviewCount++;
                }

                if (submission.isPublished()) {
                    publishedResultCount++;
                }
            } else {
                pendingExamCount++;
            }
        }

        request.setAttribute("studentId", studentId);
        request.setAttribute("studentName", studentName);

        request.setAttribute("attemptableExams", attemptableExams);
        request.setAttribute("mySubmissions", mySubmissions);

        request.setAttribute("questionCounts", questionCounts);
        request.setAttribute("mcqQuestionCounts", mcqQuestionCounts);
        request.setAttribute("essayQuestionCounts", essayQuestionCounts);

        request.setAttribute("examTotalMarks", examTotalMarks);
        request.setAttribute("mcqMarks", mcqMarks);
        request.setAttribute("essayMarks", essayMarks);

        request.setAttribute("submittedMap", submittedMap);
        request.setAttribute("readyMap", readyMap);
        request.setAttribute("readinessMessageMap", readinessMessageMap);
        request.setAttribute("submissionMap", submissionMap);

        request.setAttribute("availableExamCount", availableExamCount);
        request.setAttribute("readyExamCount", readyExamCount);
        request.setAttribute("submittedExamCount", submittedExamCount);
        request.setAttribute("pendingExamCount", pendingExamCount);
        request.setAttribute("manualReviewCount", manualReviewCount);
        request.setAttribute("publishedResultCount", publishedResultCount);

        request.getRequestDispatcher("/my-exams/index.jsp").forward(request, response);
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

    private void redirectToLogin(HttpServletRequest request,
                                 HttpServletResponse response,
                                 String errorCode)
            throws IOException {

        response.sendRedirect(
                request.getContextPath()
                        + "/login.jsp?error="
                        + urlEncode(errorCode)
        );
    }

    private void redirectToDashboard(HttpServletRequest request,
                                     HttpServletResponse response,
                                     String errorCode)
            throws IOException {

        response.sendRedirect(
                request.getContextPath()
                        + "/dashboard.jsp?error="
                        + urlEncode(errorCode)
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
}