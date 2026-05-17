package lk.nextexam.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import lk.nextexam.dao.AuditLogDAO;
import lk.nextexam.dao.FileUtil;
import lk.nextexam.model.AuditLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * BackupRestoreServlet controls Admin-only data backup and restore.
 *
 * URL:
 * /backup-restore
 *
 * Features:
 * - Download selected data file
 * - Restore selected data file using uploaded .txt file
 * - Supported filename whitelist
 * - Restore confirmation checkbox
 * - Audit logging for backup/restore actions
 *
 * Responsible Member:
 * IT25103045 - De Silva H.L.D.C.P.C
 */
@WebServlet("/backup-restore")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 5 * 1024 * 1024,
        maxRequestSize = 8 * 1024 * 1024
)
public class BackupRestoreServlet extends HttpServlet {

    private static final String ROLE_ADMIN = "Admin";

    private static final String ACTION_DOWNLOAD = "download";
    private static final String ACTION_RESTORE = "restore";

    private static final List<String> SUPPORTED_FILES = Arrays.asList(
            "students.txt",
            "users.txt",
            "exams.txt",
            "questions.txt",
            "results.txt",
            "notices.txt",
            "feedback.txt",
            "notifications.txt",
            "exam_submissions.txt",
            "manual_marks.txt",
            "result_appeals.txt",
            "audit_logs.txt",
            "system_settings.txt",
            "documents.txt",
            "integrity_logs.txt"
    );

    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        prepareRequestResponse(request, response);

        HttpSession session = request.getSession(false);

        if (!isAdmin(session)) {
            auditLogDAO.logAction(
                    getServletContext(),
                    request,
                    "ACCESS_DENIED",
                    AuditLog.MODULE_SYSTEM,
                    "Non-admin user attempted to access backup and restore center.",
                    AuditLog.STATUS_DENIED
            );

            response.sendRedirect(request.getContextPath() + "/dashboard.jsp?error=accessDenied");
            return;
        }

        String action = FileUtil.clean(request.getParameter("action"));

        if (ACTION_DOWNLOAD.equalsIgnoreCase(action)) {
            downloadSelectedFile(request, response);
            return;
        }

        request.setAttribute("backupFiles", buildBackupFileInfo());
        request.setAttribute("supportedFiles", SUPPORTED_FILES);

        request.getRequestDispatcher("/backup-restore/index.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        prepareRequestResponse(request, response);

        HttpSession session = request.getSession(false);

        if (!isAdmin(session)) {
            auditLogDAO.logAction(
                    getServletContext(),
                    request,
                    "ACCESS_DENIED",
                    AuditLog.MODULE_SYSTEM,
                    "Non-admin user attempted to restore system data.",
                    AuditLog.STATUS_DENIED
            );

            response.sendRedirect(request.getContextPath() + "/dashboard.jsp?error=accessDenied");
            return;
        }

        String action = FileUtil.clean(request.getParameter("action"));

        if (ACTION_RESTORE.equalsIgnoreCase(action)) {
            restoreSelectedFile(request, response);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/backup-restore?error=invalidAction");
    }

    private void downloadSelectedFile(HttpServletRequest request,
                                      HttpServletResponse response)
            throws IOException {

        String fileName = FileUtil.clean(request.getParameter("fileName"));

        if (!isSupportedFile(fileName)) {
            auditLogDAO.logAction(
                    getServletContext(),
                    request,
                    "DOWNLOAD_BACKUP",
                    AuditLog.MODULE_SYSTEM,
                    "Backup download failed because unsupported file was requested: " + fileName,
                    AuditLog.STATUS_FAILED
            );

            response.sendRedirect(request.getContextPath() + "/backup-restore?error=invalidFile");
            return;
        }

        File dataFile = getDataFile(fileName);

        if (dataFile == null || !dataFile.exists() || !dataFile.isFile()) {
            auditLogDAO.logAction(
                    getServletContext(),
                    request,
                    "DOWNLOAD_BACKUP",
                    AuditLog.MODULE_SYSTEM,
                    "Backup download failed because file was not found: " + fileName,
                    AuditLog.STATUS_FAILED
            );

            response.sendRedirect(request.getContextPath() + "/backup-restore?error=fileNotFound");
            return;
        }

        response.reset();
        response.setContentType("text/plain");
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=\"backup_" + timestampForFile() + "_" + fileName + "\""
        );
        response.setContentLengthLong(dataFile.length());

        try (FileInputStream input = new FileInputStream(dataFile);
             ServletOutputStream output = response.getOutputStream()) {

            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }

            output.flush();
        }

        auditLogDAO.logAction(
                getServletContext(),
                request,
                "DOWNLOAD_BACKUP",
                AuditLog.MODULE_SYSTEM,
                "Admin downloaded backup file: " + fileName,
                AuditLog.STATUS_SUCCESS
        );
    }

    private void restoreSelectedFile(HttpServletRequest request,
                                     HttpServletResponse response)
            throws IOException, ServletException {

        String targetFile = FileUtil.clean(request.getParameter("targetFile"));
        String confirmRestore = FileUtil.clean(request.getParameter("confirmRestore"));

        if (!isSupportedFile(targetFile)) {
            auditLogDAO.logAction(
                    getServletContext(),
                    request,
                    "RESTORE_DATA",
                    AuditLog.MODULE_SYSTEM,
                    "Restore failed because unsupported target file was selected: " + targetFile,
                    AuditLog.STATUS_FAILED
            );

            response.sendRedirect(request.getContextPath() + "/backup-restore?error=invalidFile");
            return;
        }

        if (!"yes".equalsIgnoreCase(confirmRestore)) {
            auditLogDAO.logAction(
                    getServletContext(),
                    request,
                    "RESTORE_DATA",
                    AuditLog.MODULE_SYSTEM,
                    "Restore failed because confirmation checkbox was not selected for " + targetFile,
                    AuditLog.STATUS_FAILED
            );

            response.sendRedirect(request.getContextPath() + "/backup-restore?error=confirmationRequired");
            return;
        }

        Part uploadedFile = request.getPart("restoreFile");

        if (uploadedFile == null || uploadedFile.getSize() <= 0) {
            auditLogDAO.logAction(
                    getServletContext(),
                    request,
                    "RESTORE_DATA",
                    AuditLog.MODULE_SYSTEM,
                    "Restore failed because no file was uploaded for " + targetFile,
                    AuditLog.STATUS_FAILED
            );

            response.sendRedirect(request.getContextPath() + "/backup-restore?error=missingUpload");
            return;
        }

        String submittedFileName = getSubmittedFileName(uploadedFile);

        if (submittedFileName == null || !submittedFileName.toLowerCase().endsWith(".txt")) {
            auditLogDAO.logAction(
                    getServletContext(),
                    request,
                    "RESTORE_DATA",
                    AuditLog.MODULE_SYSTEM,
                    "Restore failed because uploaded file was not a .txt file for " + targetFile,
                    AuditLog.STATUS_FAILED
            );

            response.sendRedirect(request.getContextPath() + "/backup-restore?error=txtOnly");
            return;
        }

        File targetDataFile = getDataFile(targetFile);

        if (targetDataFile == null) {
            response.sendRedirect(request.getContextPath() + "/backup-restore?error=dataPathMissing");
            return;
        }

        File dataFolder = targetDataFile.getParentFile();

        if (dataFolder != null && !dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        createBackupBeforeRestore(targetDataFile, targetFile);

        try (InputStream input = uploadedFile.getInputStream()) {
            Files.copy(input, targetDataFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        auditLogDAO.logAction(
                getServletContext(),
                request,
                "RESTORE_DATA",
                AuditLog.MODULE_SYSTEM,
                "Admin restored data file: " + targetFile + " using uploaded file " + submittedFileName,
                AuditLog.STATUS_SUCCESS
        );

        response.sendRedirect(request.getContextPath() + "/backup-restore?success=restored&fileName=" + urlEncode(targetFile));
    }

    private List<BackupFileInfo> buildBackupFileInfo() {
        List<BackupFileInfo> infoList = new ArrayList<>();

        for (String fileName : SUPPORTED_FILES) {
            File file = getDataFile(fileName);

            boolean exists = file != null && file.exists() && file.isFile();
            long size = exists ? file.length() : 0;
            String updatedAt = exists ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(file.lastModified())) : "-";

            infoList.add(new BackupFileInfo(fileName, exists, size, updatedAt));
        }

        return infoList;
    }

    private void createBackupBeforeRestore(File targetDataFile, String targetFileName) throws IOException {
        if (targetDataFile == null || !targetDataFile.exists() || !targetDataFile.isFile()) {
            return;
        }

        String backupFolderPath = getServletContext().getRealPath("/WEB-INF/backups");

        if (backupFolderPath == null || backupFolderPath.trim().isEmpty()) {
            return;
        }

        File backupFolder = new File(backupFolderPath);

        if (!backupFolder.exists()) {
            backupFolder.mkdirs();
        }

        File backupCopy = new File(
                backupFolder,
                "before_restore_" + timestampForFile() + "_" + targetFileName
        );

        Files.copy(targetDataFile.toPath(), backupCopy.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    private File getDataFile(String fileName) {
        if (!isSupportedFile(fileName)) {
            return null;
        }

        String dataFolderPath = getServletContext().getRealPath("/WEB-INF/data");

        if (dataFolderPath == null || dataFolderPath.trim().isEmpty()) {
            return null;
        }

        return new File(dataFolderPath, fileName);
    }

    private boolean isSupportedFile(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return false;
        }

        return SUPPORTED_FILES.contains(fileName.trim());
    }

    private boolean isAdmin(HttpSession session) {
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
                && "authenticated".equals(String.valueOf(loginStatus))
                && ROLE_ADMIN.equalsIgnoreCase(String.valueOf(userRole));
    }

    private String getSubmittedFileName(Part part) {
        if (part == null) {
            return "";
        }

        String contentDisposition = part.getHeader("content-disposition");

        if (contentDisposition == null) {
            return "";
        }

        String[] tokens = contentDisposition.split(";");

        for (String token : tokens) {
            String cleanToken = token.trim();

            if (cleanToken.startsWith("filename")) {
                String fileName = cleanToken.substring(cleanToken.indexOf("=") + 1).trim().replace("\"", "");
                return new File(fileName).getName();
            }
        }

        return "";
    }

    private String timestampForFile() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    }

    private String urlEncode(String value) {
        try {
            return java.net.URLEncoder.encode(value == null ? "" : value, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
        }
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

    public static class BackupFileInfo {
        private final String fileName;
        private final boolean exists;
        private final long sizeBytes;
        private final String updatedAt;

        public BackupFileInfo(String fileName, boolean exists, long sizeBytes, String updatedAt) {
            this.fileName = fileName;
            this.exists = exists;
            this.sizeBytes = sizeBytes;
            this.updatedAt = updatedAt;
        }

        public String getFileName() {
            return fileName;
        }

        public boolean isExists() {
            return exists;
        }

        public long getSizeBytes() {
            return sizeBytes;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public String getSizeLabel() {
            if (sizeBytes <= 0) {
                return "0 B";
            }

            if (sizeBytes < 1024) {
                return sizeBytes + " B";
            }

            double kb = sizeBytes / 1024.0;

            if (kb < 1024) {
                return String.format("%.1f KB", kb);
            }

            double mb = kb / 1024.0;
            return String.format("%.2f MB", mb);
        }
    }
}