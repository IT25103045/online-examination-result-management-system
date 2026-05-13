package lk.nextexam.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.nextexam.dao.FileUtil;
import lk.nextexam.dao.StudentDAO;
import lk.nextexam.model.Student;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Professional controller for Student Management.
 *
 * Supported actions:
 * - GET  /students
 * - POST /students action=add
 * - POST /students action=update
 * - POST /students action=delete
 * - POST /students action=eligible
 * - POST /students action=pending
 * - POST /students action=blocked
 *
 * Backward compatibility:
 * - recordId, studentId, and id are all accepted for actions that require a student ID.
 */
@WebServlet("/students")
public class StudentServlet extends HttpServlet {

    private static final String ACTION_ADD = "add";
    private static final String ACTION_UPDATE = "update";
    private static final String ACTION_DELETE = "delete";
    private static final String ACTION_ELIGIBLE = "eligible";
    private static final String ACTION_PENDING = "pending";
    private static final String ACTION_BLOCKED = "blocked";
    private static final String ACTION_STATUS = "status";

    private final StudentDAO studentDAO = new StudentDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        prepareRequestResponse(request, response);

        request.setAttribute("students", studentDAO.getAllStudents(getServletContext()));
        request.getRequestDispatcher("/students/index.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        prepareRequestResponse(request, response);

        String action = FileUtil.clean(request.getParameter("action"));

        if (ACTION_ADD.equalsIgnoreCase(action)) {
            addStudent(request, response);
            return;
        }

        if (ACTION_UPDATE.equalsIgnoreCase(action)) {
            updateStudent(request, response);
            return;
        }

        if (ACTION_DELETE.equalsIgnoreCase(action)) {
            deleteStudent(request, response);
            return;
        }

        if (ACTION_ELIGIBLE.equalsIgnoreCase(action)) {
            markEligible(request, response);
            return;
        }

        if (ACTION_PENDING.equalsIgnoreCase(action)) {
            markPending(request, response);
            return;
        }

        if (ACTION_BLOCKED.equalsIgnoreCase(action)) {
            markBlocked(request, response);
            return;
        }

        if (ACTION_STATUS.equalsIgnoreCase(action)) {
            updateStudentStatus(request, response);
            return;
        }

        redirectToStudents(request, response, "error", "invalidAction");
    }

    private void addStudent(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        Student student = buildStudentFromRequest(request);
        String validationError = validateStudent(student);

        if (validationError != null) {
            redirectToStudents(request, response, "error", validationError);
            return;
        }

        boolean success = studentDAO.addStudent(getServletContext(), student);

        if (success) {
            redirectToStudents(request, response, "success", "studentAdded");
        } else {
            redirectToStudents(request, response, "error", "studentAddFailed");
        }
    }

    private void updateStudent(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        Student student = buildStudentFromRequest(request);
        String validationError = validateStudent(student);

        if (validationError != null) {
            redirectToStudents(request, response, "error", validationError);
            return;
        }

        boolean success = studentDAO.updateStudent(getServletContext(), student);

        if (success) {
            redirectToStudents(request, response, "success", "studentUpdated");
        } else {
            redirectToStudents(request, response, "error", "studentUpdateFailed");
        }
    }

    private void deleteStudent(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String studentId = getRequestStudentId(request);

        if (studentId.isEmpty()) {
            redirectToStudents(request, response, "error", "missingStudentId");
            return;
        }

        boolean success = studentDAO.deleteStudent(getServletContext(), studentId);

        if (success) {
            redirectToStudents(request, response, "success", "studentDeleted");
        } else {
            redirectToStudents(request, response, "error", "studentDeleteFailed");
        }
    }

    private void markEligible(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String studentId = getRequestStudentId(request);

        if (studentId.isEmpty()) {
            redirectToStudents(request, response, "error", "missingStudentId");
            return;
        }

        boolean success = studentDAO.markEligible(getServletContext(), studentId);

        if (success) {
            redirectToStudents(request, response, "success", "studentMarkedEligible");
        } else {
            redirectToStudents(request, response, "error", "studentStatusUpdateFailed");
        }
    }

    private void markPending(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String studentId = getRequestStudentId(request);

        if (studentId.isEmpty()) {
            redirectToStudents(request, response, "error", "missingStudentId");
            return;
        }

        boolean success = studentDAO.markPending(getServletContext(), studentId);

        if (success) {
            redirectToStudents(request, response, "success", "studentMarkedPending");
        } else {
            redirectToStudents(request, response, "error", "studentStatusUpdateFailed");
        }
    }

    private void markBlocked(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String studentId = getRequestStudentId(request);

        if (studentId.isEmpty()) {
            redirectToStudents(request, response, "error", "missingStudentId");
            return;
        }

        boolean success = studentDAO.markBlocked(getServletContext(), studentId);

        if (success) {
            redirectToStudents(request, response, "success", "studentMarkedBlocked");
        } else {
            redirectToStudents(request, response, "error", "studentStatusUpdateFailed");
        }
    }

    private void updateStudentStatus(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String studentId = getRequestStudentId(request);
        String examStatus = FileUtil.clean(request.getParameter("examStatus"));

        if (studentId.isEmpty()) {
            redirectToStudents(request, response, "error", "missingStudentId");
            return;
        }

        if (examStatus.isEmpty()) {
            redirectToStudents(request, response, "error", "missingExamStatus");
            return;
        }

        boolean success = studentDAO.updateStudentStatus(getServletContext(), studentId, examStatus);

        if (success) {
            redirectToStudents(request, response, "success", "studentStatusUpdated");
        } else {
            redirectToStudents(request, response, "error", "studentStatusUpdateFailed");
        }
    }

    private Student buildStudentFromRequest(HttpServletRequest request) {
        return new Student(
                FileUtil.clean(request.getParameter("studentId")),
                FileUtil.clean(request.getParameter("name")),
                FileUtil.clean(request.getParameter("email")),
                FileUtil.clean(request.getParameter("course")),
                FileUtil.clean(request.getParameter("batch")),
                FileUtil.clean(request.getParameter("contact")),
                FileUtil.clean(request.getParameter("examStatus"))
        );
    }

    private String validateStudent(Student student) {
        if (student == null) {
            return "invalidStudent";
        }

        if (student.getStudentId().isEmpty()) {
            return "missingStudentId";
        }

        if (student.getName().isEmpty()) {
            return "missingStudentName";
        }

        if (student.getEmail().isEmpty()) {
            return "missingStudentEmail";
        }

        if (!student.isValidEmail()) {
            return "invalidStudentEmail";
        }

        if (student.getCourse().isEmpty()) {
            return "missingCourse";
        }

        if (student.getBatch().isEmpty()) {
            return "missingBatch";
        }

        if (!student.isValidBatch()) {
            return "invalidBatch";
        }

        if (student.getContact().isEmpty()) {
            return "missingContact";
        }

        if (!student.isValidContact()) {
            return "invalidContact";
        }

        if (student.getExamStatus().isEmpty()) {
            return "missingExamStatus";
        }

        if (!student.isValidExamStatus()) {
            return "invalidExamStatus";
        }

        if (!student.isCompleteForSave()) {
            return "incompleteStudent";
        }

        return null;
    }

    private String getRequestStudentId(HttpServletRequest request) {
        return firstNonBlank(
                request.getParameter("recordId"),
                request.getParameter("studentId"),
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

    private void redirectToStudents(HttpServletRequest request,
                                    HttpServletResponse response,
                                    String messageType,
                                    String messageCode)
            throws IOException {

        response.sendRedirect(
                request.getContextPath()
                        + "/students?"
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