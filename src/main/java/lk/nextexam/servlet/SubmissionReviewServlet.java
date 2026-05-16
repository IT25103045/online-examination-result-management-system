package lk.nextexam.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * SubmissionReviewServlet loads the staff-only submission review dashboard.
 *
 * URL:
 * /submissions
 *
 * Purpose:
 * Allows Admin and Lecturer users to review submitted exam attempts,
 * inspect answers, and identify submissions requiring manual review.
 *
 * Responsible Member:
 * IT25103045 - De Silva H.L.D.C.P.C
 */
@WebServlet("/submissions")
public class SubmissionReviewServlet extends HttpServlet {

    private static final String ROLE_ADMIN = "Admin";
    private static final String ROLE_LECTURER = "Lecturer";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);

        if (!isStaff(session)) {
            response.sendRedirect(request.getContextPath() + "/my-exams?error=accessDenied");
            return;
        }

        request.getRequestDispatcher("/submissions/index.jsp").forward(request, response);
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