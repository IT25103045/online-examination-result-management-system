<%--
    Admin Backup and Restore Center.

    Responsible Member:
    IT25103045 - De Silva H.L.D.C.P.C
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ page import="java.util.List" %>
<%@ page import="lk.nextexam.dao.FileUtil" %>
<%@ page import="lk.nextexam.servlet.BackupRestoreServlet.BackupFileInfo" %>

<%
    String pageTitle = "Backup & Restore";
    String activeMenu = "backup-restore";
    String topbarTitle = "Backup & Restore";

    List<BackupFileInfo> backupFiles = (List<BackupFileInfo>) request.getAttribute("backupFiles");
    List<String> supportedFiles = (List<String>) request.getAttribute("supportedFiles");

    int totalSupported = backupFiles != null ? backupFiles.size() : 0;
    int availableFiles = 0;
    long totalBytes = 0;

    if (backupFiles != null) {
        for (BackupFileInfo fileInfo : backupFiles) {
            if (fileInfo != null && fileInfo.isExists()) {
                availableFiles++;
                totalBytes += fileInfo.getSizeBytes();
            }
        }
    }

    String success = request.getParameter("success");
    String error = request.getParameter("error");
    String restoredFile = request.getParameter("fileName");

    String totalSizeLabel;

    if (totalBytes <= 0) {
        totalSizeLabel = "0 B";
    } else if (totalBytes < 1024) {
        totalSizeLabel = totalBytes + " B";
    } else if (totalBytes < 1024 * 1024) {
        totalSizeLabel = String.format("%.1f KB", totalBytes / 1024.0);
    } else {
        totalSizeLabel = String.format("%.2f MB", totalBytes / 1024.0 / 1024.0);
    }
%>

<%@ include file="../includes/head.jsp" %>

<div class="app-shell">
    <%@ include file="../includes/sidebar.jsp" %>

    <main class="main-content">
        <%@ include file="../includes/topbar.jsp" %>

        <section class="page-wrapper">

            <div class="hero-card backup-hero-card mb-4">
                <div class="d-flex justify-content-between align-items-start flex-wrap gap-3">
                    <div>
                        <span class="badge badge-soft-primary mb-3">
                            <i class="bi bi-database-lock me-1"></i>
                            Admin Data Safety
                        </span>

                        <h1 class="hero-title">Backup & Data Restore Center</h1>

                        <p class="hero-text">
                            Download backup copies of file-based system records and restore selected data files
                            when required. Restore actions are protected with validation and audit logging.
                        </p>
                    </div>

                    <a href="<%= request.getContextPath() %>/audit-logs" class="btn btn-outline-primary">
                        <i class="bi bi-shield-lock-fill me-2"></i>
                        View Audit Logs
                    </a>
                </div>
            </div>

            <% if ("restored".equalsIgnoreCase(success)) { %>
                <div class="alert alert-success" data-auto-close="5000">
                    <i class="bi bi-check-circle-fill me-1"></i>
                    Data file restored successfully:
                    <strong><%= FileUtil.h(restoredFile) %></strong>
                </div>
            <% } %>

            <% if ("invalidFile".equalsIgnoreCase(error)) { %>
                <div class="alert alert-danger">
                    <i class="bi bi-exclamation-triangle-fill me-1"></i>
                    Invalid or unsupported data file selected.
                </div>
            <% } else if ("fileNotFound".equalsIgnoreCase(error)) { %>
                <div class="alert alert-danger">
                    <i class="bi bi-exclamation-triangle-fill me-1"></i>
                    Selected data file was not found.
                </div>
            <% } else if ("confirmationRequired".equalsIgnoreCase(error)) { %>
                <div class="alert alert-warning">
                    <i class="bi bi-exclamation-triangle-fill me-1"></i>
                    Please confirm that you understand restore will replace the selected file.
                </div>
            <% } else if ("missingUpload".equalsIgnoreCase(error)) { %>
                <div class="alert alert-danger">
                    <i class="bi bi-exclamation-triangle-fill me-1"></i>
                    Please upload a restore file.
                </div>
            <% } else if ("txtOnly".equalsIgnoreCase(error)) { %>
                <div class="alert alert-danger">
                    <i class="bi bi-file-earmark-x-fill me-1"></i>
                    Only .txt files are allowed for restore.
                </div>
            <% } else if ("dataPathMissing".equalsIgnoreCase(error)) { %>
                <div class="alert alert-danger">
                    <i class="bi bi-folder-x me-1"></i>
                    Data folder path could not be resolved.
                </div>
            <% } else if ("invalidAction".equalsIgnoreCase(error)) { %>
                <div class="alert alert-danger">
                    <i class="bi bi-exclamation-circle-fill me-1"></i>
                    Invalid backup or restore action.
                </div>
            <% } %>

            <div class="row g-3 mb-4">
                <div class="col-md-6 col-xl-3">
                    <div class="app-card stat-card backup-stat-card">
                        <div class="stat-label">Supported Files</div>
                        <div class="stat-value"><%= totalSupported %></div>
                        <div class="stat-meta">Whitelisted data files</div>
                    </div>
                </div>

                <div class="col-md-6 col-xl-3">
                    <div class="app-card stat-card backup-stat-card">
                        <div class="stat-label">Available Files</div>
                        <div class="stat-value"><%= availableFiles %></div>
                        <div class="stat-meta">Existing files in data folder</div>
                    </div>
                </div>

                <div class="col-md-6 col-xl-3">
                    <div class="app-card stat-card backup-stat-card">
                        <div class="stat-label">Data Size</div>
                        <div class="stat-value backup-size-value"><%= FileUtil.h(totalSizeLabel) %></div>
                        <div class="stat-meta">Approximate total file size</div>
                    </div>
                </div>

                <div class="col-md-6 col-xl-3">
                    <div class="app-card stat-card backup-stat-card">
                        <div class="stat-label">Restore Safety</div>
                        <div class="stat-value">ON</div>
                        <div class="stat-meta">Confirmation + audit logging</div>
                    </div>
                </div>
            </div>

            <div class="row g-4">
                <div class="col-xl-7">
                    <div class="app-card p-4 h-100">
                        <div class="d-flex justify-content-between align-items-start flex-wrap gap-3 mb-3">
                            <div>
                                <h4 class="fw-bold mb-1">Download Backup Files</h4>
                                <p class="text-secondary mb-0">
                                    Download selected file-based records as backup copies.
                                </p>
                            </div>

                            <span class="badge badge-soft-success">
                                <i class="bi bi-download me-1"></i>
                                Backup Ready
                            </span>
                        </div>

                        <div class="table-responsive">
                            <table class="table table-hover align-middle backup-table">
                                <thead>
                                <tr>
                                    <th>File Name</th>
                                    <th>Status</th>
                                    <th>Size</th>
                                    <th>Last Updated</th>
                                    <th class="text-end">Action</th>
                                </tr>
                                </thead>

                                <tbody>
                                <% if (backupFiles == null || backupFiles.isEmpty()) { %>
                                    <tr>
                                        <td colspan="5">
                                            <div class="empty-state">
                                                <div class="empty-state-icon">
                                                    <i class="bi bi-database-x"></i>
                                                </div>
                                                <h5>No backup files configured</h5>
                                                <p>Supported file list is currently empty.</p>
                                            </div>
                                        </td>
                                    </tr>
                                <% } else {
                                    for (BackupFileInfo fileInfo : backupFiles) {
                                %>
                                    <tr>
                                        <td>
                                            <strong><%= FileUtil.h(fileInfo.getFileName()) %></strong>
                                        </td>

                                        <td>
                                            <% if (fileInfo.isExists()) { %>
                                                <span class="badge badge-soft-success">Available</span>
                                            <% } else { %>
                                                <span class="badge badge-soft-danger">Missing</span>
                                            <% } %>
                                        </td>

                                        <td>
                                            <%= FileUtil.h(fileInfo.getSizeLabel()) %>
                                        </td>

                                        <td>
                                            <small class="text-secondary">
                                                <%= FileUtil.h(fileInfo.getUpdatedAt()) %>
                                            </small>
                                        </td>

                                        <td class="text-end">
                                            <% if (fileInfo.isExists()) { %>
                                                <a class="btn btn-outline-primary btn-sm"
                                                   href="<%= request.getContextPath() %>/backup-restore?action=download&fileName=<%= FileUtil.h(fileInfo.getFileName()) %>">
                                                    <i class="bi bi-download me-1"></i>
                                                    Download
                                                </a>
                                            <% } else { %>
                                                <button class="btn btn-light btn-sm" disabled>
                                                    Not Available
                                                </button>
                                            <% } %>
                                        </td>
                                    </tr>
                                <% }
                                } %>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>

                <div class="col-xl-5">
                    <div class="app-card p-4 backup-restore-card">
                        <span class="badge badge-soft-warning mb-3">
                            <i class="bi bi-arrow-counterclockwise me-1"></i>
                            Restore Workflow
                        </span>

                        <h4 class="fw-bold mb-2">Restore Data File</h4>

                        <p class="text-secondary">
                            Upload a replacement .txt file and select the exact target data file to restore.
                            The existing file will be replaced.
                        </p>

                        <div class="backup-warning-box mb-4">
                            <div>
                                <i class="bi bi-exclamation-triangle-fill"></i>
                            </div>

                            <p>
                                Always download a backup before restoring. Restore will overwrite the selected
                                data file. This action is recorded in audit logs.
                            </p>
                        </div>

                        <form method="post"
                              action="<%= request.getContextPath() %>/backup-restore"
                              enctype="multipart/form-data"
                              class="needs-validation"
                              novalidate>

                            <input type="hidden" name="action" value="restore">

                            <div class="mb-3">
                                <label class="form-label">Target Data File</label>

                                <select class="form-select" name="targetFile" required>
                                    <option value="">Select target file</option>

                                    <% if (supportedFiles != null) {
                                        for (String supportedFile : supportedFiles) {
                                    %>
                                        <option value="<%= FileUtil.h(supportedFile) %>">
                                            <%= FileUtil.h(supportedFile) %>
                                        </option>
                                    <% }
                                    } %>
                                </select>

                                <div class="invalid-feedback">
                                    Please select the target data file.
                                </div>
                            </div>

                            <div class="mb-3">
                                <label class="form-label">Upload Restore File (.txt only)</label>

                                <input type="file"
                                       class="form-control"
                                       name="restoreFile"
                                       accept=".txt,text/plain"
                                       required>

                                <div class="invalid-feedback">
                                    Please upload a .txt restore file.
                                </div>
                            </div>

                            <div class="form-check backup-confirm-check mb-4">
                                <input class="form-check-input"
                                       type="checkbox"
                                       name="confirmRestore"
                                       value="yes"
                                       id="confirmRestore"
                                       required>

                                <label class="form-check-label" for="confirmRestore">
                                    I understand that this restore will replace the selected data file.
                                </label>

                                <div class="invalid-feedback">
                                    Restore confirmation is required.
                                </div>
                            </div>

                            <button type="submit" class="btn btn-danger w-100">
                                <i class="bi bi-arrow-repeat me-2"></i>
                                Restore Selected File
                            </button>
                        </form>
                    </div>

                    <div class="app-card p-4 mt-4">
                        <h5 class="fw-bold mb-2">
                            <i class="bi bi-shield-lock-fill me-1"></i>
                            Safety Rules
                        </h5>

                        <ul class="backup-rule-list">
                            <li>Only Admin users can access this page.</li>
                            <li>Only whitelisted data files can be downloaded/restored.</li>
                            <li>Only .txt restore files are accepted.</li>
                            <li>Existing file is backed up before restore.</li>
                            <li>All backup and restore actions are audit logged.</li>
                        </ul>
                    </div>
                </div>
            </div>

        </section>
    </main>
</div>

<%@ include file="../includes/footer.jsp" %>