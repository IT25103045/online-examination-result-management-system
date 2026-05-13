package lk.nextexam.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Professional authentication and authorization filter for NextExamLK.
 *
 * Responsibilities:
 * - Protect secured application routes.
 * - Validate authenticated sessions.
 * - Enforce Admin / Lecturer / Student role access.
 * - Prevent students from accessing management modules.
 * - Prevent staff from entering the student exam console.
 * - Add no-cache and browser security headers.
 */
@WebFilter(urlPatterns = {
        "/dashboard.jsp",

        "/students",
        "/students/index.jsp",

        "/exams",
        "/exams/index.jsp",

        "/questions",
        "/questions/index.jsp",

        "/results",
        "/results/index.jsp",

        "/users",
        "/users/index.jsp",

        "/notices",
        "/notices/index.jsp",

        "/feedback",
        "/feedback/index.jsp",

        "/faculties",
        "/faculties/index.jsp",

        "/programmes",
        "/programmes/index.jsp",

        "/batches",
        "/batches/index.jsp",

        "/modules",
        "/modules/index.jsp",

        "/enrollments",
        "/enrollments/index.jsp",

        "/academic-calendar",
        "/academic-calendar/index.jsp",

        "/attendance",
        "/attendance/index.jsp",

        "/eligibility",
        "/eligibility/index.jsp",

        "/my-exams",
        "/my-exams/index.jsp",

        "/my-results",
        "/my-results/index.jsp",

        "/exam-console",
        "/exam-console/index.jsp",

        "/submit-exam",

        "/exam-attempts",
        "/exam-attempts/index.jsp",

        "/ca-marks",
        "/ca-marks/index.jsp",

        "/gpa",
        "/gpa/index.jsp",

        "/result-approval",
        "/result-approval/index.jsp",

        "/transcripts",
        "/transcripts/index.jsp",

        "/lab-practicals",
        "/lab-practicals/index.jsp",

        "/workshops",
        "/workshops/index.jsp",

        "/engineering-projects",
        "/engineering-projects/index.jsp",

        "/industrial-training",
        "/industrial-training/index.jsp",

        "/outcome-mapping",
        "/outcome-mapping/index.jsp",

        "/settings",
        "/settings/index.jsp"
})
public class AuthFilter implements Filter {

    private static final String ROLE_ADMIN = "Admin";
    private static final String ROLE_LECTURER = "Lecturer";
    private static final String ROLE_STUDENT = "Student";

    /*
     * Shared pages.
     * All authenticated users can access these.
     * Keep only safe non-management pages here.
     */
    private static final Set<String> COMMON_ROUTES = setOf(
            "/notices",
            "/notices/index.jsp",
            "/feedback",
            "/feedback/index.jsp"
    );

    /*
     * Admin-only pages.
     */
    private static final Set<String> ADMIN_ROUTES = setOf(
            "/users",
            "/users/index.jsp",

            "/settings",
            "/settings/index.jsp"
    );

    /*
     * Admin and Lecturer pages.
     * These are management/control routes.
     */
    private static final Set<String> STAFF_ROUTES = setOf(
            "/dashboard.jsp",

            "/students",
            "/students/index.jsp",

            "/exams",
            "/exams/index.jsp",

            "/questions",
            "/questions/index.jsp",

            "/results",
            "/results/index.jsp",

            "/faculties",
            "/faculties/index.jsp",

            "/programmes",
            "/programmes/index.jsp",

            "/batches",
            "/batches/index.jsp",

            "/modules",
            "/modules/index.jsp",

            "/enrollments",
            "/enrollments/index.jsp",

            "/academic-calendar",
            "/academic-calendar/index.jsp",

            "/attendance",
            "/attendance/index.jsp",

            "/eligibility",
            "/eligibility/index.jsp",

            "/exam-attempts",
            "/exam-attempts/index.jsp",

            "/ca-marks",
            "/ca-marks/index.jsp",

            "/gpa",
            "/gpa/index.jsp",

            "/result-approval",
            "/result-approval/index.jsp",

            "/transcripts",
            "/transcripts/index.jsp",

            "/lab-practicals",
            "/lab-practicals/index.jsp",

            "/workshops",
            "/workshops/index.jsp",

            "/engineering-projects",
            "/engineering-projects/index.jsp",

            "/industrial-training",
            "/industrial-training/index.jsp",

            "/outcome-mapping",
            "/outcome-mapping/index.jsp"
    );

    /*
     * Student-only pages.
     * Students should never access /exams, /questions, /students, /users, or management /results.
     *
     * Important:
     * /results is intentionally NOT here because it is the staff result management module.
     * Students use /my-results instead.
     */
    private static final Set<String> STUDENT_ROUTES = setOf(
            "/my-exams",
            "/my-exams/index.jsp",

            "/my-results",
            "/my-results/index.jsp",

            "/exam-console",
            "/exam-console/index.jsp",

            "/submit-exam"
    );

    @Override
    public void init(FilterConfig filterConfig) {
        // No initialization required
    }

    @Override
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        addSecurityHeaders(response);

        HttpSession session = request.getSession(false);

        if (!isAuthenticated(session)) {
            redirectToLogin(request, response, "sessionExpired");
            return;
        }

        String userRole = getSessionValue(session, "userRole");
        String requestPath = normalizePath(request.getServletPath());

        if (!isAllowedForRole(userRole, requestPath)) {
            redirectAccessDenied(request, response, userRole);
            return;
        }

        filterChain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // No cleanup required
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

    private boolean isAllowedForRole(String role, String path) {
        if (isBlank(role) || isBlank(path)) {
            return false;
        }

        if (COMMON_ROUTES.contains(path)) {
            return true;
        }

        if (ROLE_ADMIN.equalsIgnoreCase(role)) {
            return ADMIN_ROUTES.contains(path)
                    || STAFF_ROUTES.contains(path);
        }

        if (ROLE_LECTURER.equalsIgnoreCase(role)) {
            return STAFF_ROUTES.contains(path);
        }

        if (ROLE_STUDENT.equalsIgnoreCase(role)) {
            return STUDENT_ROUTES.contains(path);
        }

        return false;
    }

    private void redirectToLogin(HttpServletRequest request,
                                 HttpServletResponse response,
                                 String errorCode)
            throws IOException {

        String redirectUrl = request.getContextPath()
                + "/login.jsp?error="
                + urlEncode(errorCode);

        response.sendRedirect(redirectUrl);
    }

    private void redirectAccessDenied(HttpServletRequest request,
                                      HttpServletResponse response,
                                      String role)
            throws IOException {

        String safeRole = isBlank(role) ? "unknown" : role;

        String target;

        if (ROLE_STUDENT.equalsIgnoreCase(safeRole)) {
            target = "/my-exams";
        } else {
            target = "/dashboard.jsp";
        }

        String redirectUrl = request.getContextPath()
                + target
                + "?error=accessDenied&role="
                + urlEncode(safeRole);

        response.sendRedirect(redirectUrl);
    }

    private void addSecurityHeaders(HttpServletResponse response) {
        /*
         * Prevent logged-in pages from being cached.
         */
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, private");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        /*
         * Basic browser security headers.
         */
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "SAMEORIGIN");
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

        /*
         * Do not add strict CSP yet because the current JSP pages still use:
         * - inline scripts
         * - inline styles
         * - Bootstrap CDN
         * - Google Fonts CDN
         *
         * Add Content-Security-Policy later after moving page scripts into app.js.
         */
    }

    private String getSessionValue(HttpSession session, String key) {
        if (session == null || key == null) {
            return "";
        }

        Object value = session.getAttribute(key);
        return value == null ? "" : String.valueOf(value).trim();
    }

    private String normalizePath(String path) {
        if (path == null || path.trim().isEmpty()) {
            return "";
        }

        return path.trim();
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static Set<String> setOf(String... values) {
        return new HashSet<>(Arrays.asList(values));
    }
}