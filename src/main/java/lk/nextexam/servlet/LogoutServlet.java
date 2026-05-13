package lk.nextexam.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * LogoutServlet handles user logout functionality.
 *
 * It invalidates the current session and redirects the user back to
 * the login page to prevent unauthorized access after logout.
 *
 * Responsible Member:
 * IT25103045 - De Silva H.L.D.C.P.C
 */
@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    private static final String SESSION_COOKIE_NAME = "JSESSIONID";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        logout(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        logout(request, response);
    }

    private void logout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        clearSession(request);
        clearSessionCookie(request, response);
        addLogoutSecurityHeaders(response);

        response.sendRedirect(request.getContextPath() + "/login.jsp?logout=success");
    }

    private void clearSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            return;
        }

        /*
         * Remove all known authentication/session attributes.
         * invalidate() is still called after this, but explicit removal keeps
         * the code clear and compatible with future session cleanup rules.
         */
        session.removeAttribute("loggedUser");
        session.removeAttribute("loginStatus");

        session.removeAttribute("userId");
        session.removeAttribute("username");
        session.removeAttribute("displayName");
        session.removeAttribute("userEmail");
        session.removeAttribute("userRole");
        session.removeAttribute("userStatus");
        session.removeAttribute("loginTime");

        session.invalidate();
    }

    private void clearSessionCookie(HttpServletRequest request, HttpServletResponse response) {
        Cookie sessionCookie = new Cookie(SESSION_COOKIE_NAME, "");
        sessionCookie.setMaxAge(0);
        sessionCookie.setHttpOnly(true);
        sessionCookie.setPath(resolveCookiePath(request));

        /*
         * For localhost HTTP development, do not force Secure.
         * Enable this only after deploying over HTTPS:
         * sessionCookie.setSecure(true);
         */
        response.addCookie(sessionCookie);
    }

    private String resolveCookiePath(HttpServletRequest request) {
        String contextPath = request.getContextPath();

        if (contextPath == null || contextPath.trim().isEmpty()) {
            return "/";
        }

        return contextPath;
    }

    private void addLogoutSecurityHeaders(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, private");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "SAMEORIGIN");
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
    }
}