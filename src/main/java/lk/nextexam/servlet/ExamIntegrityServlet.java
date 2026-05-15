package lk.nextexam.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lk.nextexam.dao.ExamIntegrityLogDAO;
import lk.nextexam.dao.FileUtil;
import lk.nextexam.model.ExamIntegrityLog;
import lk.nextexam.model.User;

import java.io.IOException;

/**
 * ExamIntegrityServlet receives client-side integrity events from the exam console.
 *
 * URL:
 * /exam-integrity
 *
 * Responsible Member:
 * IT25103045 - De Silva H.L.D.C.P.C
 */
@WebServlet("/exam-integrity")
public class ExamIntegrityServlet extends HttpServlet {

    private final ExamIntegrityLogDAO integrityLogDAO = new ExamIntegrityLogDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");

        HttpSession session = request.getSession(false);

        if (!isStudentAuthenticated(session)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"success\":false,\"message\":\"unauthorized\"}");
            return;
        }

        String studentId = getSessionValue(session, "userId");
        String examId = FileUtil.clean(request.getParameter("examId"));
        String eventType = normalizeEventType(request.getParameter("eventType"));
        String description = FileUtil.clean(request.getParameter("description"));

        if (studentId.isEmpty() || examId.isEmpty() || eventType.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"success\":false,\"message\":\"missingData\"}");
            return;
        }

        if (description.isEmpty()) {
            description = "Exam integrity event recorded";
        }

        boolean saved = integrityLogDAO.addLog(
                getServletContext(),
                studentId,
                examId,
                eventType,
                description
        );

        if (saved) {
            response.getWriter().write("{\"success\":true}");
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"success\":false,\"message\":\"saveFailed\"}");
        }
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

    private String normalizeEventType(String eventType) {
        String cleanEventType = FileUtil.clean(eventType).toUpperCase();

        if (ExamIntegrityLog.EVENT_EXAM_STARTED.equalsIgnoreCase(cleanEventType)) {
            return ExamIntegrityLog.EVENT_EXAM_STARTED;
        }

        if (ExamIntegrityLog.EVENT_EXAM_SUBMITTED.equalsIgnoreCase(cleanEventType)) {
            return ExamIntegrityLog.EVENT_EXAM_SUBMITTED;
        }

        if (ExamIntegrityLog.EVENT_TAB_SWITCH.equalsIgnoreCase(cleanEventType)) {
            return ExamIntegrityLog.EVENT_TAB_SWITCH;
        }

        if (ExamIntegrityLog.EVENT_RIGHT_CLICK_BLOCKED.equalsIgnoreCase(cleanEventType)) {
            return ExamIntegrityLog.EVENT_RIGHT_CLICK_BLOCKED;
        }

        if (ExamIntegrityLog.EVENT_COPY_BLOCKED.equalsIgnoreCase(cleanEventType)) {
            return ExamIntegrityLog.EVENT_COPY_BLOCKED;
        }

        if (ExamIntegrityLog.EVENT_PASTE_BLOCKED.equalsIgnoreCase(cleanEventType)) {
            return ExamIntegrityLog.EVENT_PASTE_BLOCKED;
        }

        if (ExamIntegrityLog.EVENT_FULLSCREEN_EXIT.equalsIgnoreCase(cleanEventType)) {
            return ExamIntegrityLog.EVENT_FULLSCREEN_EXIT;
        }

        if (ExamIntegrityLog.EVENT_FULLSCREEN_REQUESTED.equalsIgnoreCase(cleanEventType)) {
            return ExamIntegrityLog.EVENT_FULLSCREEN_REQUESTED;
        }

        return "";
    }

    private String getSessionValue(HttpSession session, String key) {
        if (session == null || key == null) {
            return "";
        }

        Object value = session.getAttribute(key);
        return value == null ? "" : String.valueOf(value).trim();
    }
}