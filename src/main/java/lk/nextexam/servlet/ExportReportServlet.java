package lk.nextexam.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lk.nextexam.dao.ExamDAO;
import lk.nextexam.dao.ExamSubmissionDAO;
import lk.nextexam.dao.FeedbackDAO;
import lk.nextexam.dao.FileUtil;
import lk.nextexam.dao.NotificationDAO;
import lk.nextexam.dao.QuestionDAO;
import lk.nextexam.dao.ResultAppealDAO;
import lk.nextexam.dao.ResultDAO;
import lk.nextexam.dao.StudentDAO;
import lk.nextexam.dao.UserDAO;
import lk.nextexam.model.Exam;
import lk.nextexam.model.ExamSubmission;
import lk.nextexam.model.Feedback;
import lk.nextexam.model.Notification;
import lk.nextexam.model.Question;
import lk.nextexam.model.Result;
import lk.nextexam.model.ResultAppeal;
import lk.nextexam.model.Student;
import lk.nextexam.model.User;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ExportReportServlet exports system records as CSV files.
 *
 * URL:
 * /export-report?type=students
 *
 * Supported types:
 * students, users, exams, questions, submissions, results, appeals, notifications, feedback
 *
 * Responsible Member:
 * IT25103045 - De Silva H.L.D.C.P.C
 */
@WebServlet("/export-report")
public class ExportReportServlet extends HttpServlet {

    private static final String ROLE_ADMIN = "Admin";
    private static final String ROLE_LECTURER = "Lecturer";

    private static final DateTimeFormatter FILE_DATE_TIME =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final StudentDAO studentDAO = new StudentDAO();
    private final UserDAO userDAO = new UserDAO();
    private final ExamDAO examDAO = new ExamDAO();
    private final QuestionDAO questionDAO = new QuestionDAO();
    private final ExamSubmissionDAO submissionDAO = new ExamSubmissionDAO();
    private final ResultDAO resultDAO = new ResultDAO();
    private final ResultAppealDAO appealDAO = new ResultAppealDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final FeedbackDAO feedbackDAO = new FeedbackDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);

        if (!isStaff(session)) {
            response.sendRedirect(request.getContextPath() + "/dashboard.jsp?error=accessDenied");
            return;
        }

        String type = FileUtil.clean(request.getParameter("type")).toLowerCase();

        if (type.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/reports?error=missingType");
            return;
        }

        String fileName = "nextexam_" + type + "_" + LocalDateTime.now().format(FILE_DATE_TIME) + ".csv";

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, private");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        PrintWriter out = response.getWriter();

        /*
         * UTF-8 BOM helps Microsoft Excel open CSV files correctly.
         */
        out.write('\uFEFF');

        switch (type) {
            case "students":
                exportStudents(out);
                break;

            case "users":
                exportUsers(out);
                break;

            case "exams":
                exportExams(out);
                break;

            case "questions":
                exportQuestions(out);
                break;

            case "submissions":
                exportSubmissions(out);
                break;

            case "results":
                exportResults(out);
                break;

            case "appeals":
                exportAppeals(out);
                break;

            case "notifications":
                exportNotifications(out);
                break;

            case "feedback":
                exportFeedback(out);
                break;

            default:
                response.reset();
                response.sendRedirect(request.getContextPath() + "/reports?error=invalidType");
                return;
        }

        out.flush();
    }

    private void exportStudents(PrintWriter out) {
        writeRow(out, "Student ID", "Name", "Email", "Course", "Batch", "Contact", "Exam Status");

        for (Student student : studentDAO.getAllStudents(getServletContext())) {
            writeRow(
                    out,
                    student.getStudentId(),
                    student.getName(),
                    student.getEmail(),
                    student.getCourse(),
                    student.getBatch(),
                    student.getContact(),
                    student.getExamStatus()
            );
        }
    }

    private void exportUsers(PrintWriter out) {
        writeRow(out, "User ID", "Username", "Email", "Role", "Status");

        for (User user : userDAO.getAllUsers(getServletContext())) {
            writeRow(
                    out,
                    user.getUserId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getRole(),
                    user.getStatus()
            );
        }
    }

    private void exportExams(PrintWriter out) {
        writeRow(out, "Exam ID", "Subject", "Exam Date", "Duration", "Total Marks", "Status");

        for (Exam exam : examDAO.getAllExams(getServletContext())) {
            writeRow(
                    out,
                    exam.getExamId(),
                    exam.getSubject(),
                    exam.getExamDate(),
                    exam.getDuration(),
                    exam.getTotalMarks(),
                    exam.getStatus()
            );
        }
    }

    private void exportQuestions(PrintWriter out) {
        writeRow(
                out,
                "Question ID",
                "Exam ID",
                "Question Type",
                "Question Text",
                "Option A",
                "Option B",
                "Option C",
                "Option D",
                "Correct Answer",
                "Marks",
                "Status",
                "Model Answer"
        );

        for (Question question : questionDAO.getAllQuestions(getServletContext())) {
            writeRow(
                    out,
                    question.getQuestionId(),
                    question.getExamId(),
                    question.getQuestionType(),
                    question.getQuestionText(),
                    question.getOptionA(),
                    question.getOptionB(),
                    question.getOptionC(),
                    question.getOptionD(),
                    question.getCorrectAnswer(),
                    question.getMarks(),
                    question.getStatus(),
                    question.getModelAnswer()
            );
        }
    }

    private void exportSubmissions(PrintWriter out) {
        writeRow(
                out,
                "Submission ID",
                "Exam ID",
                "Student ID",
                "Student Name",
                "Submitted At",
                "Score",
                "Total Marks",
                "Percentage",
                "Status",
                "Answered Items"
        );

        for (ExamSubmission submission : submissionDAO.getAllSubmissions(getServletContext())) {
            writeRow(
                    out,
                    submission.getSubmissionId(),
                    submission.getExamId(),
                    submission.getStudentId(),
                    submission.getStudentName(),
                    submission.getSubmittedAt(),
                    submission.getScore(),
                    submission.getTotalMarks(),
                    submission.getPercentageDisplay(),
                    submission.getStatus(),
                    String.valueOf(submission.getAnsweredItemCount())
            );
        }
    }

    private void exportResults(PrintWriter out) {
        writeRow(
                out,
                "Result ID",
                "Student ID",
                "Exam ID",
                "Marks",
                "Grade",
                "Status",
                "Verification",
                "Published"
        );

        for (Result result : resultDAO.getAllResults(getServletContext())) {
            writeRow(
                    out,
                    result.getResultId(),
                    result.getStudentId(),
                    result.getExamId(),
                    result.getMarks(),
                    result.getGrade(),
                    result.getStatus(),
                    result.getVerification(),
                    result.getPublished()
            );
        }
    }

    private void exportAppeals(PrintWriter out) {
        writeRow(
                out,
                "Appeal ID",
                "Result ID",
                "Exam ID",
                "Student ID",
                "Student Name",
                "Reason Type",
                "Message",
                "Status",
                "Staff Reply",
                "Created At",
                "Updated At",
                "Reviewed By"
        );

        for (ResultAppeal appeal : appealDAO.getAllAppeals(getServletContext())) {
            writeRow(
                    out,
                    appeal.getAppealId(),
                    appeal.getResultId(),
                    appeal.getExamId(),
                    appeal.getStudentId(),
                    appeal.getStudentName(),
                    appeal.getReasonType(),
                    appeal.getMessage(),
                    appeal.getStatus(),
                    appeal.getStaffReply(),
                    appeal.getCreatedAt(),
                    appeal.getUpdatedAt(),
                    appeal.getReviewedBy()
            );
        }
    }

    private void exportNotifications(PrintWriter out) {
        writeRow(
                out,
                "Notification ID",
                "Target User ID",
                "Target Role",
                "Title",
                "Message",
                "Type",
                "Status",
                "Created At",
                "Read At",
                "Target URL"
        );

        for (Notification notification : notificationDAO.getAllNotifications(getServletContext())) {
            writeRow(
                    out,
                    notification.getNotificationId(),
                    notification.getTargetUserId(),
                    notification.getTargetRole(),
                    notification.getTitle(),
                    notification.getMessage(),
                    notification.getType(),
                    notification.getStatus(),
                    notification.getCreatedAt(),
                    notification.getReadAt(),
                    notification.getTargetUrl()
            );
        }
    }

    private void exportFeedback(PrintWriter out) {
        writeRow(out, "Feedback ID", "Student ID", "Category", "Message", "Date", "Status");

        for (Feedback feedback : feedbackDAO.getAllFeedback(getServletContext())) {
            writeRow(
                    out,
                    feedback.getFeedbackId(),
                    feedback.getStudentId(),
                    feedback.getCategory(),
                    feedback.getMessage(),
                    feedback.getDate(),
                    feedback.getStatus()
            );
        }
    }

    private void writeRow(PrintWriter out, String... values) {
        StringBuilder row = new StringBuilder();

        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                row.append(",");
            }

            row.append(csv(values[i]));
        }

        out.println(row);
    }

    private String csv(String value) {
        String cleanValue = value == null ? "" : value;

        cleanValue = cleanValue.replace("\"", "\"\"");

        return "\"" + cleanValue + "\"";
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
}