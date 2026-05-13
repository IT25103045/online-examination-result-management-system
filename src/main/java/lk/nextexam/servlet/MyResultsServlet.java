package lk.nextexam.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lk.nextexam.dao.ResultDAO;
import lk.nextexam.model.Result;
import lk.nextexam.model.User;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Student-only result viewer.
 *
 * Route:
 * /my-results
 *
 * Purpose:
 * - Students can view only their own published results.
 * - Students cannot access the staff /results management page.
 */
@WebServlet("/my-results")
public class MyResultsServlet extends HttpServlet {

    private final ResultDAO resultDAO = new ResultDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        prepareRequestResponse(request, response);

        HttpSession session = request.getSession(false);

        String userId = getSessionValue(session, "userId");
        String username = getDisplayName(session);
        String userRole = getSessionValue(session, "userRole");

        if (!User.ROLE_STUDENT.equalsIgnoreCase(userRole)) {
            redirectToDashboard(request, response, "accessDenied");
            return;
        }

        if (userId.isEmpty()) {
            redirectToLogin(request, response, "sessionExpired");
            return;
        }

        List<Result> myResults = resultDAO.getPublishedResultsByStudentId(getServletContext(), userId);

        int totalResults = myResults != null ? myResults.size() : 0;
        int passCount = 0;
        int failCount = 0;
        double totalMarks = 0.0;

        if (myResults != null) {
            for (Result result : myResults) {
                if (result.isPass()) {
                    passCount++;
                }

                if (result.isFail()) {
                    failCount++;
                }

                totalMarks += result.getMarksAsDouble();
            }
        }

        double averageMarks = totalResults > 0 ? totalMarks / totalResults : 0.0;

        request.setAttribute("studentId", userId);
        request.setAttribute("studentName", username);
        request.setAttribute("myResults", myResults);
        request.setAttribute("totalResults", totalResults);
        request.setAttribute("passCount", passCount);
        request.setAttribute("failCount", failCount);
        request.setAttribute("averageMarks", averageMarks);

        request.getRequestDispatcher("/my-results/index.jsp").forward(request, response);
    }

    private String getDisplayName(HttpSession session) {
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