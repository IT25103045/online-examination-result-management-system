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
import lk.nextexam.dao.StudentDAO;
import lk.nextexam.dao.StudentDocumentDAO;
import lk.nextexam.model.Student;
import lk.nextexam.model.StudentDocument;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lk.nextexam.dao.NotificationDAO;
import lk.nextexam.model.Notification;


/**
 * StudentDocumentServlet manages document upload and verification workflow.
 *
 * Supported actions:
 * - upload
 * - approve
 * - reject
 * - delete
 *
 * Responsible Member:
 * IT25103045 - De Silva H.L.D.C.P.C
 */
@WebServlet("/documents")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 5 * 1024 * 1024,
        maxRequestSize = 8 * 1024 * 1024
)
public class StudentDocumentServlet extends HttpServlet {

    private static final String UPLOAD_FOLDER = "uploads/documents";
    private static final String ROLE_ADMIN = "Admin";
    private static final String ROLE_LECTURER = "Lecturer";
    private static final String ROLE_STUDENT = "Student";

    private final StudentDocumentDAO documentDAO = new StudentDocumentDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final ActivityLogDAO activityLogDAO = new ActivityLogDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        request.getRequestDispatcher("/documents/index.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String action = FileUtil.clean(request.getParameter("action"));

        if ("upload".equalsIgnoreCase(action)) {
            uploadDocument(request, response);
            return;
        }

        if ("approve".equalsIgnoreCase(action)) {
            updateStatus(request, response, StudentDocument.STATUS_APPROVED);
            return;
        }

        if ("reject".equalsIgnoreCase(action)) {
            updateStatus(request, response, StudentDocument.STATUS_REJECTED);
            return;
        }

        if ("delete".equalsIgnoreCase(action)) {
            deleteDocument(request, response);
            return;
        }

        redirect(response, request, "error", "invalidAction");
    }

    private void uploadDocument(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        HttpSession session = request.getSession(false);

        if (!isAuthenticated(session)) {
            redirect(response, request, "error", "sessionExpired");
            return;
        }

        String role = getSessionValue(session, "userRole");

        if (!ROLE_STUDENT.equalsIgnoreCase(role)) {
            redirect(response, request, "error", "onlyStudentsCanUpload");
            return;
        }

        String studentId = getSessionValue(session, "userId");
        String displayName = getSessionValue(session, "displayName");

        Student student = studentDAO.getStudentById(getServletContext(), studentId);

        if (student != null) {
            studentId = student.getStudentId();
            displayName = student.getDisplayName();
        }

        if (FileUtil.isBlank(studentId)) {
            redirect(response, request, "error", "studentNotFound");
            return;
        }

        String documentType = FileUtil.clean(request.getParameter("documentType"));
        Part filePart = request.getPart("documentFile");

        if (FileUtil.isBlank(documentType) || filePart == null || filePart.getSize() <= 0) {
            redirect(response, request, "error", "missingUploadData");
            return;
        }

        String originalFileName = sanitizeFileName(getSubmittedFileName(filePart));

        if (!isAllowedFile(originalFileName)) {
            redirect(response, request, "error", "invalidFileType");
            return;
        }

        String documentId = FileUtil.generateId("DOC");
        String extension = getExtension(originalFileName);
        String storedFileName = documentId + "_" + studentId + extension;

        File uploadDirectory = new File(getServletContext().getRealPath("/") + UPLOAD_FOLDER);

        if (!uploadDirectory.exists()) {
            boolean created = uploadDirectory.mkdirs();

            if (!created && !uploadDirectory.exists()) {
                redirect(response, request, "error", "uploadFolderError");
                return;
            }
        }

        File savedFile = new File(uploadDirectory, storedFileName);
        filePart.write(savedFile.getAbsolutePath());

        String relativePath = UPLOAD_FOLDER + "/" + storedFileName;

        StudentDocument document = new StudentDocument(
                documentId,
                studentId,
                displayName,
                documentType,
                originalFileName,
                relativePath,
                StudentDocument.STATUS_PENDING,
                "-",
                documentDAO.now(),
                "-"
        );

        boolean saved = documentDAO.addDocument(getServletContext(), document);

        if (!saved) {
            redirect(response, request, "error", "documentSaveFailed");
            return;
        }

        activityLogDAO.addLog(
                getServletContext(),
                studentId,
                role,
                "DOCUMENT_UPLOAD",
                displayName + " uploaded " + document.getDocumentType()
        );
        notificationDAO.addNotification(
                getServletContext(),
                "",
                "Admin",
                "New Document Uploaded",
                displayName + " uploaded " + document.getDocumentType() + " for verification.",
                Notification.TYPE_DOCUMENT
        );

        notificationDAO.addNotification(
                getServletContext(),
                "",
                "Lecturer",
                "New Document Uploaded",
                displayName + " uploaded " + document.getDocumentType() + " for verification.",
                Notification.TYPE_DOCUMENT
        );

        redirect(response, request, "success", "uploaded");
    }

    private void updateStatus(HttpServletRequest request,
                              HttpServletResponse response,
                              String status)
            throws IOException {

        HttpSession session = request.getSession(false);

        if (!isStaff(session)) {
            redirect(response, request, "error", "accessDenied");
            return;
        }

        String documentId = FileUtil.clean(request.getParameter("documentId"));
        String reviewNote = FileUtil.clean(request.getParameter("reviewNote"));

        if (FileUtil.isBlank(documentId)) {
            redirect(response, request, "error", "missingDocumentId");
            return;
        }

        boolean updated;

        if (StudentDocument.STATUS_APPROVED.equalsIgnoreCase(status)) {
            updated = documentDAO.approveDocument(getServletContext(), documentId, reviewNote);
        } else {
            updated = documentDAO.rejectDocument(getServletContext(), documentId, reviewNote);
        }

        if (!updated) {
            redirect(response, request, "error", "statusUpdateFailed");
            return;
        }

        String staffId = getSessionValue(session, "userId");
        String staffRole = getSessionValue(session, "userRole");
        String staffName = getSessionValue(session, "displayName");

        activityLogDAO.addLog(
                getServletContext(),
                staffId,
                staffRole,
                "DOCUMENT_" + status.toUpperCase(),
                staffName + " marked document " + documentId + " as " + status
        );
        StudentDocument updatedDocument = documentDAO.getDocumentById(getServletContext(), documentId);

        if (updatedDocument != null) {
            notificationDAO.addNotification(
                    getServletContext(),
                    updatedDocument.getStudentId(),
                    "Student",
                    "Document " + status,
                    "Your " + updatedDocument.getDocumentType() + " document was marked as " + status + ".",
                    Notification.TYPE_DOCUMENT
            );
        }

        redirect(response, request, "success", "statusUpdated");
    }

    private void deleteDocument(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);

        if (!isStaff(session)) {
            redirect(response, request, "error", "accessDenied");
            return;
        }

        String documentId = FileUtil.clean(request.getParameter("documentId"));

        if (FileUtil.isBlank(documentId)) {
            redirect(response, request, "error", "missingDocumentId");
            return;
        }

        boolean deleted = documentDAO.deleteDocument(getServletContext(), documentId);

        if (!deleted) {
            redirect(response, request, "error", "deleteFailed");
            return;
        }

        activityLogDAO.addLog(
                getServletContext(),
                getSessionValue(session, "userId"),
                getSessionValue(session, "userRole"),
                "DOCUMENT_DELETE",
                getSessionValue(session, "displayName") + " deleted document " + documentId
        );

        redirect(response, request, "success", "deleted");
    }

    private boolean isAuthenticated(HttpSession session) {
        return session != null
                && session.getAttribute("loggedUser") != null
                && session.getAttribute("userId") != null
                && session.getAttribute("userRole") != null
                && "authenticated".equals(String.valueOf(session.getAttribute("loginStatus")));
    }

    private boolean isStaff(HttpSession session) {
        if (!isAuthenticated(session)) {
            return false;
        }

        String role = getSessionValue(session, "userRole");

        return ROLE_ADMIN.equalsIgnoreCase(role) || ROLE_LECTURER.equalsIgnoreCase(role);
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
            return "document";
        }

        String[] tokens = contentDisposition.split(";");

        for (String token : tokens) {
            if (token.trim().startsWith("filename")) {
                return token.substring(token.indexOf("=") + 1)
                        .trim()
                        .replace("\"", "");
            }
        }

        return "document";
    }

    private String sanitizeFileName(String fileName) {
        String safeName = FileUtil.clean(fileName);

        if (safeName.isEmpty()) {
            return "document.pdf";
        }

        return safeName
                .replace(" ", "_")
                .replace("/", "_")
                .replace("\\", "_")
                .replace("..", "_");
    }

    private boolean isAllowedFile(String fileName) {
        String extension = getExtension(fileName).toLowerCase();

        return ".pdf".equals(extension)
                || ".jpg".equals(extension)
                || ".jpeg".equals(extension)
                || ".png".equals(extension);
    }

    private String getExtension(String fileName) {
        if (fileName == null) {
            return ".pdf";
        }

        int dotIndex = fileName.lastIndexOf(".");

        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return ".pdf";
        }

        return fileName.substring(dotIndex).toLowerCase();
    }

    private void redirect(HttpServletResponse response,
                          HttpServletRequest request,
                          String key,
                          String value)
            throws IOException {

        response.sendRedirect(request.getContextPath()
                + "/documents?"
                + key
                + "="
                + URLEncoder.encode(value, StandardCharsets.UTF_8));
    }
}