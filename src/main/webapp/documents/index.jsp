<%--
    Document Upload and Verification Page.
    Students upload academic documents.
    Admin/Lecturer users review, approve, or reject uploaded documents.

    Responsible Member:
    IT25103045 - De Silva H.L.D.C.P.C
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ page import="java.util.List" %>
<%@ page import="lk.nextexam.dao.FileUtil" %>
<%@ page import="lk.nextexam.dao.StudentDocumentDAO" %>
<%@ page import="lk.nextexam.model.StudentDocument" %>

<%
    String pageTitle = "Documents";
    String activeMenu = "documents";
    String topbarTitle = "Document Verification";

    String sessionRole = session != null && session.getAttribute("userRole") != null
            ? String.valueOf(session.getAttribute("userRole"))
            : "";

    String sessionUserId = session != null && session.getAttribute("userId") != null
            ? String.valueOf(session.getAttribute("userId"))
            : "";

    boolean isAdmin = "Admin".equalsIgnoreCase(sessionRole);
    boolean isLecturer = "Lecturer".equalsIgnoreCase(sessionRole);
    boolean isStudent = "Student".equalsIgnoreCase(sessionRole);
    boolean isStaff = isAdmin || isLecturer;

    StudentDocumentDAO documentDAO = new StudentDocumentDAO();

    List<StudentDocument> documents;

    if (isStudent) {
        documents = documentDAO.getDocumentsByStudentId(application, sessionUserId);
    } else {
        documents = documentDAO.getAllDocuments(application);
    }

    int totalDocuments = documentDAO.countAllDocuments(application);
    int pendingDocuments = documentDAO.countPendingDocuments(application);
    int approvedDocuments = documentDAO.countApprovedDocuments(application);
    int rejectedDocuments = documentDAO.countRejectedDocuments(application);

    String success = request.getParameter("success");
    String error = request.getParameter("error");
%>

<%@ include file="../includes/head.jsp" %>

<div class="app-shell">
    <%@ include file="../includes/sidebar.jsp" %>

    <main class="main-content">
        <%@ include file="../includes/topbar.jsp" %>

        <section class="page-wrapper">

            <div class="page-header">
                <div>
                    <h1 class="page-title">
                        <%= isStudent ? "My Documents" : "Document Verification" %>
                    </h1>
                    <p class="page-description">
                        <%= isStudent
                                ? "Upload and track your academic document verification status."
                                : "Review uploaded academic documents and update verification decisions." %>
                    </p>
                </div>

                <span class="badge badge-soft-primary">
                    <i class="bi bi-folder-check me-1"></i>
                    <%= totalDocuments %> Total Documents
                </span>
            </div>

            <% if (success != null) { %>
                <div class="alert alert-success">
                    <i class="bi bi-check-circle-fill me-2"></i>
                    <% if ("uploaded".equalsIgnoreCase(success)) { %>
                        Document uploaded successfully. It is now pending verification.
                    <% } else if ("statusUpdated".equalsIgnoreCase(success)) { %>
                        Document verification status updated successfully.
                    <% } else if ("deleted".equalsIgnoreCase(success)) { %>
                        Document record deleted successfully.
                    <% } else { %>
                        Action completed successfully.
                    <% } %>
                </div>
            <% } %>

            <% if (error != null) { %>
                <div class="alert alert-danger">
                    <i class="bi bi-exclamation-triangle-fill me-2"></i>
                    <% if ("invalidFileType".equalsIgnoreCase(error)) { %>
                        Invalid file type. Please upload PDF, JPG, JPEG, or PNG files only.
                    <% } else if ("missingUploadData".equalsIgnoreCase(error)) { %>
                        Please select a document type and upload a file.
                    <% } else if ("onlyStudentsCanUpload".equalsIgnoreCase(error)) { %>
                        Only students can upload documents.
                    <% } else if ("accessDenied".equalsIgnoreCase(error)) { %>
                        You do not have permission to perform this action.
                    <% } else { %>
                        Action failed. Please check your details and try again.
                    <% } %>
                </div>
            <% } %>

            <div class="row g-3 mb-4">
                <div class="col-md-6 col-xl-3">
                    <div class="app-card stat-card">
                        <div class="d-flex justify-content-between gap-3">
                            <div>
                                <div class="stat-label">Total</div>
                                <div class="stat-value"><%= totalDocuments %></div>
                                <div class="stat-meta">Uploaded documents</div>
                            </div>
                            <div class="stat-icon">
                                <i class="bi bi-folder2-open"></i>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-md-6 col-xl-3">
                    <div class="app-card stat-card">
                        <div class="d-flex justify-content-between gap-3">
                            <div>
                                <div class="stat-label">Pending</div>
                                <div class="stat-value"><%= pendingDocuments %></div>
                                <div class="stat-meta">Awaiting review</div>
                            </div>
                            <div class="stat-icon">
                                <i class="bi bi-hourglass-split"></i>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-md-6 col-xl-3">
                    <div class="app-card stat-card">
                        <div class="d-flex justify-content-between gap-3">
                            <div>
                                <div class="stat-label">Approved</div>
                                <div class="stat-value"><%= approvedDocuments %></div>
                                <div class="stat-meta">Verified documents</div>
                            </div>
                            <div class="stat-icon">
                                <i class="bi bi-patch-check-fill"></i>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-md-6 col-xl-3">
                    <div class="app-card stat-card">
                        <div class="d-flex justify-content-between gap-3">
                            <div>
                                <div class="stat-label">Rejected</div>
                                <div class="stat-value"><%= rejectedDocuments %></div>
                                <div class="stat-meta">Need correction</div>
                            </div>
                            <div class="stat-icon">
                                <i class="bi bi-x-octagon-fill"></i>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <% if (isStudent) { %>
                <div class="app-card form-card p-4 mb-4">
                    <div class="d-flex align-items-start justify-content-between gap-3 flex-wrap mb-3">
                        <div>
                            <h4 class="fw-bold mb-1">Upload Academic Document</h4>
                            <p class="text-secondary mb-0">
                                Supported file types: PDF, JPG, JPEG, PNG. Maximum size: 5 MB.
                            </p>
                        </div>

                        <span class="badge badge-soft-info">
                            <i class="bi bi-cloud-upload me-1"></i>
                            Student Upload
                        </span>
                    </div>

                    <form action="<%= request.getContextPath() %>/documents"
                          method="post"
                          enctype="multipart/form-data"
                          class="row g-3">

                        <input type="hidden" name="action" value="upload">

                        <div class="col-md-5">
                            <label class="form-label">Document Type <span class="required">*</span></label>
                            <select name="documentType" class="form-select" required>
                                <option value="">Select document type</option>
                                <option value="Student ID">Student ID</option>
                                <option value="Medical Certificate">Medical Certificate</option>
                                <option value="Exam Eligibility">Exam Eligibility</option>
                                <option value="Other">Other</option>
                            </select>
                        </div>

                        <div class="col-md-5">
                            <label class="form-label">Document File <span class="required">*</span></label>
                            <input type="file"
                                   name="documentFile"
                                   class="form-control"
                                   accept=".pdf,.jpg,.jpeg,.png"
                                   required>
                        </div>

                        <div class="col-md-2 d-flex align-items-end">
                            <button type="submit" class="btn btn-primary w-100">
                                <i class="bi bi-upload me-2"></i>
                                Upload
                            </button>
                        </div>
                    </form>
                </div>
            <% } %>

            <div class="app-card crud-card p-4">
                <div class="crud-toolbar">
                    <div>
                        <h4 class="fw-bold mb-1">
                            <%= isStudent ? "My Uploaded Documents" : "Uploaded Documents" %>
                        </h4>
                        <p class="text-secondary mb-0">
                            <%= isStudent
                                    ? "Track review notes and verification status."
                                    : "Approve or reject student documents with review notes." %>
                        </p>
                    </div>
                </div>

                <% if (documents == null || documents.isEmpty()) { %>
                    <div class="empty-state">
                        <div class="empty-state-icon">
                            <i class="bi bi-folder-x"></i>
                        </div>
                        <h5>No documents found</h5>
                        <p>
                            <%= isStudent
                                    ? "Upload your first academic document to begin verification."
                                    : "No student documents are available for review yet." %>
                        </p>
                    </div>
                <% } else { %>
                    <div class="table-responsive">
                        <table class="table table-hover align-middle document-table">
                            <thead>
                            <tr>
                                <th>Document</th>
                                <th>Student</th>
                                <th>Type</th>
                                <th>Status</th>
                                <th>Uploaded</th>
                                <th>Review Note</th>
                                <th>File</th>
                                <% if (isStaff) { %>
                                    <th>Action</th>
                                <% } %>
                            </tr>
                            </thead>

                            <tbody>
                            <% for (StudentDocument document : documents) { %>
                                <tr>
                                    <td>
                                        <strong><%= FileUtil.h(document.getDocumentId()) %></strong><br>
                                        <small class="text-secondary"><%= FileUtil.h(document.getFileName()) %></small>
                                    </td>

                                    <td>
                                        <strong><%= FileUtil.h(document.getStudentName()) %></strong><br>
                                        <small class="text-secondary"><%= FileUtil.h(document.getStudentId()) %></small>
                                    </td>

                                    <td>
                                        <span class="badge badge-soft-info">
                                            <%= FileUtil.h(document.getDocumentType()) %>
                                        </span>
                                    </td>

                                    <td>
                                        <span class="badge <%= document.getStatusBadgeClass() %>">
                                            <i class="bi <%= document.getStatusIcon() %> me-1"></i>
                                            <%= FileUtil.h(document.getStatus()) %>
                                        </span>
                                    </td>

                                    <td>
                                        <small class="text-secondary">
                                            <%= FileUtil.h(document.getUploadedAt()) %>
                                        </small>
                                    </td>

                                    <td>
                                        <small class="text-secondary">
                                            <%= FileUtil.h(document.getReviewNote()) %>
                                        </small>
                                    </td>

                                    <td>
                                        <a href="<%= request.getContextPath() %>/<%= FileUtil.h(document.getFilePath()) %>"
                                           target="_blank"
                                           class="btn btn-sm btn-outline-primary">
                                            <i class="bi bi-eye me-1"></i>
                                            View
                                        </a>
                                    </td>

                                    <% if (isStaff) { %>
                                        <td>
                                            <form action="<%= request.getContextPath() %>/documents"
                                                  method="post"
                                                  class="document-action-form">

                                                <input type="hidden" name="documentId" value="<%= FileUtil.h(document.getDocumentId()) %>">

                                                <textarea name="reviewNote"
                                                          class="form-control form-control-sm mb-2"
                                                          rows="2"
                                                          placeholder="Review note"><%= "-".equals(document.getReviewNote()) ? "" : FileUtil.h(document.getReviewNote()) %></textarea>

                                                <div class="action-group">
                                                    <button type="submit"
                                                            name="action"
                                                            value="approve"
                                                            class="btn btn-sm btn-outline-success">
                                                        <i class="bi bi-check-circle me-1"></i>
                                                        Approve
                                                    </button>

                                                    <button type="submit"
                                                            name="action"
                                                            value="reject"
                                                            class="btn btn-sm btn-outline-danger">
                                                        <i class="bi bi-x-circle me-1"></i>
                                                        Reject
                                                    </button>

                                                    <% if (isAdmin) { %>
                                                        <button type="submit"
                                                                name="action"
                                                                value="delete"
                                                                class="btn btn-sm btn-outline-secondary"
                                                                onclick="return confirm('Delete this document record?');">
                                                            <i class="bi bi-trash me-1"></i>
                                                        </button>
                                                    <% } %>
                                                </div>
                                            </form>
                                        </td>
                                    <% } %>
                                </tr>
                            <% } %>
                            </tbody>
                        </table>
                    </div>
                <% } %>
            </div>

        </section>
    </main>
</div>

<%@ include file="../includes/footer.jsp" %>