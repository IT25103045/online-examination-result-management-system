<%--
    Result Appeals Page.
    Student users can submit result recheck requests.
    Staff users can review, reply, and update appeal status.

    Responsible Member:
    IT25103045 - De Silva H.L.D.C.P.C
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ page import="java.util.List" %>
<%@ page import="lk.nextexam.dao.FileUtil" %>
<%@ page import="lk.nextexam.model.Result" %>
<%@ page import="lk.nextexam.model.ResultAppeal" %>

<%
    boolean isStaff = request.getAttribute("isStaff") != null && (Boolean) request.getAttribute("isStaff");
    boolean isStudent = request.getAttribute("isStudent") != null && (Boolean) request.getAttribute("isStudent");

    String pageTitle = "Result Appeals";
    String activeMenu = "result-appeals";
    String topbarTitle = "Result Appeals";

    List<ResultAppeal> appeals = (List<ResultAppeal>) request.getAttribute("appeals");
    List<Result> publishedResults = (List<Result>) request.getAttribute("publishedResults");

    String selectedResultId = request.getAttribute("selectedResultId") != null
            ? request.getAttribute("selectedResultId").toString()
            : "";

    int pendingCount = request.getAttribute("pendingCount") != null ? (Integer) request.getAttribute("pendingCount") : 0;
    int underReviewCount = request.getAttribute("underReviewCount") != null ? (Integer) request.getAttribute("underReviewCount") : 0;
    int resolvedCount = request.getAttribute("resolvedCount") != null ? (Integer) request.getAttribute("resolvedCount") : 0;
    int rejectedCount = request.getAttribute("rejectedCount") != null ? (Integer) request.getAttribute("rejectedCount") : 0;

    int totalAppeals = appeals != null ? appeals.size() : 0;

    String success = request.getParameter("success");
    String error = request.getParameter("error");

    String alertType = "";
    String alertMessage = "";

    if ("appealSubmitted".equalsIgnoreCase(success)) {
        alertType = "success";
        alertMessage = "Your result recheck request was submitted successfully.";
    } else if ("appealUpdated".equalsIgnoreCase(success)) {
        alertType = "success";
        alertMessage = "The appeal status and staff reply were updated successfully.";
    } else if ("appealAlreadyExists".equalsIgnoreCase(error)) {
        alertType = "warning";
        alertMessage = "You have already submitted an appeal for this result.";
    } else if ("resultNotPublished".equalsIgnoreCase(error)) {
        alertType = "warning";
        alertMessage = "Only published results can be appealed.";
    } else if ("missingAppealDetails".equalsIgnoreCase(error)) {
        alertType = "danger";
        alertMessage = "Please select a reason and enter your appeal message.";
    } else if ("appealSaveFailed".equalsIgnoreCase(error)) {
        alertType = "danger";
        alertMessage = "Appeal could not be saved. Please try again.";
    } else if ("appealUpdateFailed".equalsIgnoreCase(error)) {
        alertType = "danger";
        alertMessage = "Appeal update failed. Please try again.";
    } else if ("accessDenied".equalsIgnoreCase(error)) {
        alertType = "danger";
        alertMessage = "You do not have permission to perform this action.";
    } else if ("resultNotFound".equalsIgnoreCase(error)) {
        alertType = "danger";
        alertMessage = "Selected result could not be found.";
    }
%>

<%@ include file="../includes/head.jsp" %>

<div class="app-shell">
    <%@ include file="../includes/sidebar.jsp" %>

    <main class="main-content">
        <%@ include file="../includes/topbar.jsp" %>

        <section class="page-wrapper">

            <div class="hero-card mb-4">
                <div class="d-flex justify-content-between align-items-start flex-wrap gap-3">
                    <div>
                        <span class="badge badge-soft-primary mb-3">
                            <i class="bi bi-arrow-repeat me-1"></i>
                            Result Recheck Workflow
                        </span>

                        <h1 class="hero-title">Result Appeals</h1>

                        <p class="hero-text">
                            <% if (isStudent) { %>
                                Submit and track result recheck requests for your published results.
                            <% } else { %>
                                Review student result recheck requests, add staff replies, and update request status.
                            <% } %>
                        </p>
                    </div>

                    <div class="d-flex gap-2 flex-wrap">
                        <% if (isStudent) { %>
                            <a href="<%= request.getContextPath() %>/my-results" class="btn btn-outline-primary">
                                <i class="bi bi-bar-chart-fill me-2"></i>
                                My Results
                            </a>
                        <% } else { %>
                            <a href="<%= request.getContextPath() %>/results" class="btn btn-outline-primary">
                                <i class="bi bi-bar-chart-fill me-2"></i>
                                Results
                            </a>
                        <% } %>
                    </div>
                </div>
            </div>

            <% if (!alertMessage.isEmpty()) { %>
                <div class="alert alert-<%= FileUtil.h(alertType) %>">
                    <i class="bi <%= "success".equals(alertType) ? "bi-check-circle-fill" : "bi-info-circle-fill" %> me-1"></i>
                    <%= FileUtil.h(alertMessage) %>
                </div>
            <% } %>

            <div class="row g-3 mb-4">
                <div class="col-md-6 col-xl-3">
                    <div class="app-card stat-card">
                        <div class="stat-label">Total Appeals</div>
                        <div class="stat-value"><%= totalAppeals %></div>
                        <div class="stat-meta"><%= isStudent ? "Your requests" : "All student requests" %></div>
                    </div>
                </div>

                <div class="col-md-6 col-xl-3">
                    <div class="app-card stat-card">
                        <div class="stat-label">Pending</div>
                        <div class="stat-value"><%= pendingCount %></div>
                        <div class="stat-meta">Awaiting staff review</div>
                    </div>
                </div>

                <div class="col-md-6 col-xl-3">
                    <div class="app-card stat-card">
                        <div class="stat-label">Under Review</div>
                        <div class="stat-value"><%= underReviewCount %></div>
                        <div class="stat-meta">Being checked</div>
                    </div>
                </div>

                <div class="col-md-6 col-xl-3">
                    <div class="app-card stat-card">
                        <div class="stat-label">Resolved</div>
                        <div class="stat-value"><%= resolvedCount %></div>
                        <div class="stat-meta"><%= rejectedCount %> rejected</div>
                    </div>
                </div>
            </div>

            <% if (isStudent) { %>
                <div class="app-card p-4 mb-4">
                    <div class="d-flex justify-content-between align-items-start flex-wrap gap-3 mb-3">
                        <div>
                            <h4 class="fw-bold mb-1">Request Result Recheck</h4>
                            <p class="text-secondary mb-0">
                                Select a published result and explain your concern clearly.
                            </p>
                        </div>

                        <span class="badge badge-soft-warning">
                            Student Request
                        </span>
                    </div>

                    <form method="post" action="<%= request.getContextPath() %>/result-appeals">
                        <input type="hidden" name="action" value="create">

                        <div class="row g-3">
                            <div class="col-md-6">
                                <label class="form-label">Published Result</label>
                                <select class="form-select" name="resultId" required>
                                    <option value="">Select result</option>
                                    <% if (publishedResults != null) {
                                        for (Result result : publishedResults) {
                                            boolean selected = result.getResultId().equalsIgnoreCase(selectedResultId);
                                    %>
                                        <option value="<%= FileUtil.h(result.getResultId()) %>" <%= selected ? "selected" : "" %>>
                                            <%= FileUtil.h(result.getResultId()) %> — <%= FileUtil.h(result.getExamId()) %> — <%= FileUtil.h(result.getDisplayMarks()) %>
                                        </option>
                                    <% }
                                    } %>
                                </select>
                            </div>

                            <div class="col-md-6">
                                <label class="form-label">Reason Type</label>
                                <select class="form-select" name="reasonType" required>
                                    <option value="Mark Recheck">Mark Recheck</option>
                                    <option value="Missing Marks">Missing Marks</option>
                                    <option value="Wrong Result">Wrong Result</option>
                                    <option value="Essay Review">Essay Review</option>
                                    <option value="Technical Issue">Technical Issue</option>
                                    <option value="Other">Other</option>
                                </select>
                            </div>

                            <div class="col-12">
                                <label class="form-label">Concern Message</label>
                                <textarea class="form-control"
                                          name="message"
                                          rows="4"
                                          maxlength="800"
                                          required
                                          placeholder="Write your result concern professionally..."></textarea>
                            </div>

                            <div class="col-12">
                                <button type="submit" class="btn btn-primary">
                                    <i class="bi bi-send-check-fill me-2"></i>
                                    Submit Recheck Request
                                </button>
                            </div>
                        </div>
                    </form>
                </div>
            <% } %>

            <div class="app-card crud-card p-4">
                <div class="crud-toolbar">
                    <div>
                        <h4 class="fw-bold mb-1"><%= isStudent ? "My Appeal Requests" : "Student Appeal Requests" %></h4>
                        <p class="text-secondary mb-0">
                            Track appeal status, staff replies, and review decisions.
                        </p>
                    </div>

                    <div class="d-flex gap-2 flex-wrap">
                        <div class="input-group search-control">
                            <span class="input-group-text">
                                <i class="bi bi-search"></i>
                            </span>

                            <input type="search"
                                   class="form-control"
                                   id="appealSearch"
                                   placeholder="Search appeals">
                        </div>

                        <select class="form-select" id="appealStatusFilter" style="width: 190px;">
                            <option value="">All Status</option>
                            <option value="pending">Pending</option>
                            <option value="under review">Under Review</option>
                            <option value="resolved">Resolved</option>
                            <option value="rejected">Rejected</option>
                        </select>
                    </div>
                </div>

                <% if (appeals == null || appeals.isEmpty()) { %>
                    <div class="empty-state">
                        <div class="empty-state-icon">
                            <i class="bi bi-inbox"></i>
                        </div>

                        <h5>No result appeals yet</h5>
                        <p>
                            <% if (isStudent) { %>
                                Your submitted appeal requests will appear here.
                            <% } else { %>
                                Student result recheck requests will appear here.
                            <% } %>
                        </p>
                    </div>
                <% } else { %>
                    <div class="table-responsive">
                        <table class="table table-hover align-middle result-appeal-table" id="appealTable">
                            <thead>
                            <tr>
                                <th>Appeal ID</th>
                                <th>Result</th>
                                <th>Student</th>
                                <th>Reason</th>
                                <th>Message</th>
                                <th>Status</th>
                                <th>Staff Reply</th>
                                <th>Updated</th>
                                <% if (isStaff) { %>
                                    <th class="text-end">Action</th>
                                <% } %>
                            </tr>
                            </thead>

                            <tbody>
                            <% for (ResultAppeal appeal : appeals) { %>
                                <tr data-status="<%= FileUtil.h(appeal.getStatus().toLowerCase()) %>">
                                    <td class="fw-bold"><%= FileUtil.h(appeal.getAppealId()) %></td>

                                    <td>
                                        <strong><%= FileUtil.h(appeal.getResultId()) %></strong><br>
                                        <small class="text-secondary"><%= FileUtil.h(appeal.getExamId()) %></small>
                                    </td>

                                    <td>
                                        <strong><%= FileUtil.h(appeal.getStudentName()) %></strong><br>
                                        <small class="text-secondary"><%= FileUtil.h(appeal.getStudentId()) %></small>
                                    </td>

                                    <td>
                                        <span class="badge badge-soft-primary">
                                            <%= FileUtil.h(appeal.getReasonType()) %>
                                        </span>
                                    </td>

                                    <td class="appeal-message-cell">
                                        <%= FileUtil.h(appeal.getMessage()) %>
                                    </td>

                                    <td>
                                        <span class="badge <%= appeal.getStatusBadgeClass() %>">
                                            <%= FileUtil.h(appeal.getStatus()) %>
                                        </span>
                                    </td>

                                    <td class="appeal-message-cell">
                                        <% if (appeal.getStaffReply().isEmpty()) { %>
                                            <span class="text-secondary">No reply yet</span>
                                        <% } else { %>
                                            <%= FileUtil.h(appeal.getStaffReply()) %>
                                            <% if (!appeal.getReviewedBy().isEmpty()) { %>
                                                <br>
                                                <small class="text-secondary">
                                                    By <%= FileUtil.h(appeal.getReviewedBy()) %>
                                                </small>
                                            <% } %>
                                        <% } %>
                                    </td>

                                    <td>
                                        <small class="text-secondary">
                                            <%= FileUtil.h(appeal.getUpdatedAt()) %>
                                        </small>
                                    </td>

                                    <% if (isStaff) { %>
                                        <td class="text-end">
                                            <button type="button"
                                                    class="btn btn-sm btn-outline-primary"
                                                    data-bs-toggle="modal"
                                                    data-bs-target="#appealModal_<%= FileUtil.h(appeal.getAppealId()) %>">
                                                <i class="bi bi-pencil-square me-1"></i>
                                                Review
                                            </button>
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

<% if (isStaff && appeals != null && !appeals.isEmpty()) {
    for (ResultAppeal appeal : appeals) {
%>
<div class="modal fade" id="appealModal_<%= FileUtil.h(appeal.getAppealId()) %>" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-lg modal-dialog-centered">
        <div class="modal-content border-0 shadow-lg">

            <div class="modal-header">
                <div>
                    <h5 class="modal-title fw-bold">Review Appeal</h5>
                    <small class="text-secondary">
                        <%= FileUtil.h(appeal.getAppealId()) %> · <%= FileUtil.h(appeal.getResultId()) %>
                    </small>
                </div>

                <button class="btn-close" type="button" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>

            <form method="post" action="<%= request.getContextPath() %>/result-appeals">
                <div class="modal-body">
                    <input type="hidden" name="action" value="update">
                    <input type="hidden" name="appealId" value="<%= FileUtil.h(appeal.getAppealId()) %>">

                    <div class="appeal-review-box mb-3">
                        <small>Student Concern</small>
                        <p><%= FileUtil.h(appeal.getMessage()) %></p>
                    </div>

                    <div class="row g-3">
                        <div class="col-md-6">
                            <label class="form-label">Appeal Status</label>
                            <select class="form-select" name="status" required>
                                <option value="Pending" <%= appeal.isPending() ? "selected" : "" %>>Pending</option>
                                <option value="Under Review" <%= appeal.isUnderReview() ? "selected" : "" %>>Under Review</option>
                                <option value="Resolved" <%= appeal.isResolved() ? "selected" : "" %>>Resolved</option>
                                <option value="Rejected" <%= appeal.isRejected() ? "selected" : "" %>>Rejected</option>
                            </select>
                        </div>

                        <div class="col-md-6">
                            <label class="form-label">Reason</label>
                            <input type="text"
                                   class="form-control"
                                   value="<%= FileUtil.h(appeal.getReasonType()) %>"
                                   readonly>
                        </div>

                        <div class="col-12">
                            <label class="form-label">Staff Reply</label>
                            <textarea class="form-control"
                                      name="staffReply"
                                      rows="4"
                                      maxlength="800"
                                      placeholder="Write staff reply or final decision..."><%= FileUtil.h(appeal.getStaffReply()) %></textarea>
                        </div>
                    </div>
                </div>

                <div class="modal-footer">
                    <button class="btn btn-outline-secondary" type="button" data-bs-dismiss="modal">
                        Cancel
                    </button>

                    <button type="submit" class="btn btn-primary">
                        <i class="bi bi-check2-circle me-2"></i>
                        Update Appeal
                    </button>
                </div>
            </form>

        </div>
    </div>
</div>
<% }
} %>

<script>
    document.addEventListener("DOMContentLoaded", function () {
        const searchInput = document.getElementById("appealSearch");
        const statusFilter = document.getElementById("appealStatusFilter");
        const rows = document.querySelectorAll("#appealTable tbody tr[data-status]");

        function filterRows() {
            const searchValue = searchInput ? searchInput.value.toLowerCase().trim() : "";
            const statusValue = statusFilter ? statusFilter.value.toLowerCase().trim() : "";

            rows.forEach(function (row) {
                const text = row.innerText.toLowerCase();
                const status = row.getAttribute("data-status") || "";

                const matchesSearch = text.includes(searchValue);
                const matchesStatus = statusValue === "" || status === statusValue;

                row.style.display = matchesSearch && matchesStatus ? "" : "none";
            });
        }

        if (searchInput) {
            searchInput.addEventListener("input", filterRows);
        }

        if (statusFilter) {
            statusFilter.addEventListener("change", filterRows);
        }
    });
</script>

<%@ include file="../includes/footer.jsp" %>