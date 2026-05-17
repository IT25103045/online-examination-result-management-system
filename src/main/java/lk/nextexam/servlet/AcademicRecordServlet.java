package lk.nextexam.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lk.nextexam.dao.ExamDAO;
import lk.nextexam.dao.FileUtil;
import lk.nextexam.dao.ResultDAO;
import lk.nextexam.dao.StudentDAO;
import lk.nextexam.model.Exam;
import lk.nextexam.model.Result;
import lk.nextexam.model.Student;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AcademicRecordServlet provides transcript-style student academic records.
 *
 * URL:
 * /academic-record
 *
 * Access:
 * - Student: can view own published academic record
 * - Admin/Lecturer: can view selected student's academic record using ?studentId=
 *
 * Responsible Member:
 * IT25103045 - De Silva H.L.D.C.P.C
 */
@WebServlet("/academic-record")
public class AcademicRecordServlet extends HttpServlet {

    private static final String ROLE_ADMIN = "Admin";
    private static final String ROLE_LECTURER = "Lecturer";
    private static final String ROLE_STUDENT = "Student";

    private final StudentDAO studentDAO = new StudentDAO();
    private final ResultDAO resultDAO = new ResultDAO();
    private final ExamDAO examDAO = new ExamDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        prepareRequestResponse(request, response);

        HttpSession session = request.getSession(false);

        if (!isAuthenticated(session)) {
            response.sendRedirect(request.getContextPath() + "/login.jsp?error=sessionExpired");
            return;
        }

        String userRole = getSessionValue(session, "userRole");
        String sessionUserId = getSessionValue(session, "userId");
        String sessionEmail = getSessionValue(session, "userEmail");

        boolean isStudent = ROLE_STUDENT.equalsIgnoreCase(userRole);
        boolean isStaff = ROLE_ADMIN.equalsIgnoreCase(userRole) || ROLE_LECTURER.equalsIgnoreCase(userRole);

        if (!isStudent && !isStaff) {
            response.sendRedirect(request.getContextPath() + "/dashboard.jsp?error=accessDenied");
            return;
        }

        String selectedStudentId;

        if (isStudent) {
            selectedStudentId = sessionUserId;
        } else {
            selectedStudentId = FileUtil.clean(request.getParameter("studentId"));
        }

        Student selectedStudent = null;

        if (!selectedStudentId.isEmpty()) {
            selectedStudent = studentDAO.getStudentById(getServletContext(), selectedStudentId);
        }

        if (selectedStudent == null && isStudent && !sessionEmail.isEmpty()) {
            selectedStudent = studentDAO.getStudentByEmail(getServletContext(), sessionEmail);

            if (selectedStudent != null) {
                selectedStudentId = selectedStudent.getStudentId();
            }
        }

        List<Student> allStudents = isStaff ? studentDAO.getAllStudents(getServletContext()) : null;

        List<Result> publishedResults = selectedStudent != null
                ? resultDAO.getPublishedResultsByStudentId(getServletContext(), selectedStudent.getStudentId())
                : java.util.Collections.emptyList();

        Map<String, Exam> examMap = new LinkedHashMap<>();

        for (Result result : publishedResults) {
            Exam exam = examDAO.getExamById(getServletContext(), result.getExamId());

            if (exam != null) {
                examMap.put(result.getExamId(), exam);
            }
        }

        AcademicRecordStats stats = calculateStats(publishedResults);

        request.setAttribute("selectedStudent", selectedStudent);
        request.setAttribute("selectedStudentId", selectedStudentId);
        request.setAttribute("allStudents", allStudents);
        request.setAttribute("publishedResults", publishedResults);
        request.setAttribute("examMap", examMap);

        request.setAttribute("totalPublishedResults", stats.totalPublishedResults);
        request.setAttribute("passedCount", stats.passedCount);
        request.setAttribute("failedCount", stats.failedCount);
        request.setAttribute("averageMarks", stats.averageMarks);
        request.setAttribute("highestMarks", stats.highestMarks);
        request.setAttribute("gradeA", stats.gradeA);
        request.setAttribute("gradeB", stats.gradeB);
        request.setAttribute("gradeC", stats.gradeC);
        request.setAttribute("gradeS", stats.gradeS);
        request.setAttribute("gradeF", stats.gradeF);
        request.setAttribute("academicStanding", stats.academicStanding);
        request.setAttribute("standingBadgeClass", stats.standingBadgeClass);

        request.setAttribute("isStudentView", isStudent);
        request.setAttribute("isStaffView", isStaff);
        request.setAttribute("generatedDate", LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));

        request.getRequestDispatcher("/academic-record/index.jsp").forward(request, response);
    }

    private AcademicRecordStats calculateStats(List<Result> results) {
        AcademicRecordStats stats = new AcademicRecordStats();

        if (results == null || results.isEmpty()) {
            stats.academicStanding = "No Published Results";
            stats.standingBadgeClass = "badge-soft-secondary";
            return stats;
        }

        double totalMarks = 0.0;

        for (Result result : results) {
            if (result == null) {
                continue;
            }

            stats.totalPublishedResults++;

            double marks = result.getMarksAsDouble();
            totalMarks += marks;

            if (marks > stats.highestMarks) {
                stats.highestMarks = marks;
            }

            if (result.isPass()) {
                stats.passedCount++;
            } else if (result.isFail()) {
                stats.failedCount++;
            }

            String grade = result.getGrade();

            if (Result.GRADE_A.equalsIgnoreCase(grade)) {
                stats.gradeA++;
            } else if (Result.GRADE_B.equalsIgnoreCase(grade)) {
                stats.gradeB++;
            } else if (Result.GRADE_C.equalsIgnoreCase(grade)) {
                stats.gradeC++;
            } else if (Result.GRADE_S.equalsIgnoreCase(grade)) {
                stats.gradeS++;
            } else if (Result.GRADE_F.equalsIgnoreCase(grade)) {
                stats.gradeF++;
            }
        }

        if (stats.totalPublishedResults > 0) {
            stats.averageMarks = totalMarks / stats.totalPublishedResults;
        }

        if (stats.averageMarks >= 75) {
            stats.academicStanding = "Excellent Standing";
            stats.standingBadgeClass = "badge-soft-success";
        } else if (stats.averageMarks >= 60) {
            stats.academicStanding = "Good Standing";
            stats.standingBadgeClass = "badge-soft-primary";
        } else if (stats.averageMarks >= 40) {
            stats.academicStanding = "Satisfactory Standing";
            stats.standingBadgeClass = "badge-soft-warning";
        } else {
            stats.academicStanding = "Needs Improvement";
            stats.standingBadgeClass = "badge-soft-danger";
        }

        return stats;
    }

    private boolean isAuthenticated(HttpSession session) {
        if (session == null) {
            return false;
        }

        Object loggedUser = session.getAttribute("loggedUser");
        Object loginStatus = session.getAttribute("loginStatus");
        Object userRole = session.getAttribute("userRole");
        Object userId = session.getAttribute("userId");

        return loggedUser != null
                && userId != null
                && userRole != null
                && "authenticated".equals(String.valueOf(loginStatus));
    }

    private String getSessionValue(HttpSession session, String key) {
        if (session == null || key == null) {
            return "";
        }

        Object value = session.getAttribute(key);
        return value == null ? "" : String.valueOf(value).trim();
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

    private static class AcademicRecordStats {
        private int totalPublishedResults = 0;
        private int passedCount = 0;
        private int failedCount = 0;

        private int gradeA = 0;
        private int gradeB = 0;
        private int gradeC = 0;
        private int gradeS = 0;
        private int gradeF = 0;

        private double averageMarks = 0.0;
        private double highestMarks = 0.0;

        private String academicStanding = "No Published Results";
        private String standingBadgeClass = "badge-soft-secondary";
    }
}