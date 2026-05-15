package lk.nextexam.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import lk.nextexam.dao.ActivityLogDAO;
import lk.nextexam.dao.FileUtil;
import lk.nextexam.dao.UserDAO;
import lk.nextexam.model.User;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * ProfileServlet manages user profile page and profile image upload.
 *
 * URL:
 * /profile
 *
 * Responsible Member:
 * IT25103045 - De Silva H.L.D.C.P.C
 */
@WebServlet("/profile")
@MultipartConfig(
        fileSizeThreshold = 1024 * 512,
        maxFileSize = 3 * 1024 * 1024,
        maxRequestSize = 4 * 1024 * 1024
)
public class ProfileServlet extends HttpServlet {

    private static final String UPLOAD_FOLDER = "uploads/profile";

    private final UserDAO userDAO = new UserDAO();
    private final ActivityLogDAO activityLogDAO = new ActivityLogDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        request.getRequestDispatcher("/profile/index.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String action = FileUtil.clean(request.getParameter("action"));

        if ("uploadImage".equalsIgnoreCase(action)) {
            uploadProfileImage(request, response);
            return;
        }

        redirect(request, response, "error", "invalidAction");
    }

    private void uploadProfileImage(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (!isAuthenticated(session)) {
            response.sendRedirect(request.getContextPath() + "/login.jsp?error=sessionExpired");
            return;
        }

        String userId = getSessionValue(session, "userId");
        String userRole = getSessionValue(session, "userRole");
        String displayName = getSessionValue(session, "displayName");

        User user = userDAO.getUserById(getServletContext(), userId);

        if (user == null) {
            redirect(request, response, "error", "userNotFound");
            return;
        }

        Part imagePart = request.getPart("profileImage");

        if (imagePart == null || imagePart.getSize() <= 0) {
            redirect(request, response, "error", "missingImage");
            return;
        }

        if (imagePart.getSize() > (3 * 1024 * 1024)) {
            redirect(request, response, "error", "fileTooLarge");
            return;
        }

        String originalFileName = sanitizeFileName(getSubmittedFileName(imagePart));

        if (!isAllowedImage(originalFileName)) {
            redirect(request, response, "error", "invalidImageType");
            return;
        }

        String extension = getExtension(originalFileName);
        String storedFileName = userId + "_profile" + extension;

        File uploadDirectory = new File(getServletContext().getRealPath("/") + UPLOAD_FOLDER);

        if (!uploadDirectory.exists()) {
            boolean created = uploadDirectory.mkdirs();

            if (!created && !uploadDirectory.exists()) {
                redirect(request, response, "error", "uploadFolderError");
                return;
            }
        }

        File savedFile = new File(uploadDirectory, storedFileName);
        imagePart.write(savedFile.getAbsolutePath());

        String relativePath = UPLOAD_FOLDER + "/" + storedFileName;

        boolean updated = userDAO.updateProfileImage(getServletContext(), userId, relativePath);

        if (!updated) {
            redirect(request, response, "error", "updateFailed");
            return;
        }

        User updatedUser = userDAO.getUserById(getServletContext(), userId);

        if (updatedUser != null) {
            session.setAttribute("loggedUser", updatedUser);
            session.setAttribute("profileImage", updatedUser.getProfileImage());
        }

        activityLogDAO.addLog(
                getServletContext(),
                userId,
                userRole,
                "PROFILE_IMAGE_UPDATE",
                displayName + " updated profile image"
        );

        redirect(request, response, "success", "imageUpdated");
    }

    private boolean isAuthenticated(HttpSession session) {
        return session != null
                && session.getAttribute("loggedUser") != null
                && session.getAttribute("userId") != null
                && session.getAttribute("userRole") != null
                && "authenticated".equals(String.valueOf(session.getAttribute("loginStatus")));
    }

    private String getSessionValue(HttpSession session, String key) {
        if (session == null || key == null) {
            return "";
        }

        Object value = session.getAttribute(key);
        return value == null ? "" : String.valueOf(value).trim();
    }

    private String getSubmittedFileName(Part part) {
        String contentDisposition = part.getHeader("content-disposition");

        if (contentDisposition == null) {
            return "profile.png";
        }

        String[] tokens = contentDisposition.split(";");

        for (String token : tokens) {
            if (token.trim().startsWith("filename")) {
                return token.substring(token.indexOf("=") + 1)
                        .trim()
                        .replace("\"", "");
            }
        }

        return "profile.png";
    }

    private String sanitizeFileName(String fileName) {
        String safeName = FileUtil.clean(fileName);

        if (safeName.isEmpty()) {
            return "profile.png";
        }

        return safeName
                .replace(" ", "_")
                .replace("/", "_")
                .replace("\\", "_")
                .replace("..", "_");
    }

    private boolean isAllowedImage(String fileName) {
        String extension = getExtension(fileName).toLowerCase();

        return ".jpg".equals(extension)
                || ".jpeg".equals(extension)
                || ".png".equals(extension)
                || ".webp".equals(extension);
    }

    private String getExtension(String fileName) {
        if (fileName == null) {
            return ".png";
        }

        int dotIndex = fileName.lastIndexOf(".");

        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return ".png";
        }

        return fileName.substring(dotIndex).toLowerCase();
    }

    private void redirect(HttpServletRequest request,
                          HttpServletResponse response,
                          String key,
                          String value)
            throws IOException {

        response.sendRedirect(request.getContextPath()
                + "/profile?"
                + key
                + "="
                + URLEncoder.encode(value, StandardCharsets.UTF_8));
    }
}