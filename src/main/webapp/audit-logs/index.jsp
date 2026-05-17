<%--
    Admin Audit Logs page.
    Displays enterprise-style audit trail records.

    Responsible Member:
    IT25103045 - De Silva H.L.D.C.P.C
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ page import="java.util.List" %>
<%@ page import="lk.nextexam.dao.FileUtil" %>
<%@ page import="lk.nextexam.model.AuditLog" %>

<%
    String pageTitle = "Audit Logs";
    String activeMenu = "audit-logs";
    String topbarTitle = "Audit Logs";

    List<AuditLog> auditLogs = (List<AuditLog>) request.getAttribute("auditLogs");

    int totalLogs = request.getAttribute("totalLogs") != null ? (Integer) request.getAttribute("totalLogs") : 0;
    int successLogs = request.getAttribute("successLogs") != null ? (Integer) request.getAttribute("successLogs") : 0;
    int failedLogs = request.getAttribute("failedLogs") != null ? (Integer) request.getAttribute("failedLogs") : 0;
    int deniedLogs = request.getAttribute("deniedLogs") != null ? (Integer) request.getAttribute("deniedLogs") : 0;
    int warningLogs = request.getAttribute("warningLogs") != null ? (Integer) request.getAttribute("warningLogs") : 0;
    int todayLogs = request.getAttribute("todayLogs") != null ? (Integer) request.getAttribute("todayLogs") : 0;
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
                        <span class="badge badge-soft-danger mb-3">
                            <i class="bi bi-shield-lock-fill me-1"></i>
                            Admin Security Monitoring
                        </span>

                        <h1 class="hero-title">Enterprise Audit Logs</h1>

                        <p class="hero-text">
                            Track important system actions including report exports, result publishing,
                            appeal updates, manual marking, notifications, and access-denied events.
                        </p>
                    </div>

                    <a href="<%= request.getContextPath() %>/dashboard.jsp" class="btn btn-outline-primary">
                        <i class="bi bi-speedometer2 me-2"></i>
                        Dashboard
                    </a>
                </div>
            </div>

            <div class="row g-3 mb-4">
                <div class="col-md-6 col-xl-3">
                    <div class="app-card stat-card">
                        <div class="stat-label">Total Logs</div>
                        <div class="stat-value"><%= totalLogs %></div>
                        <div class="stat-meta">All recorded actions</div>
                    </div>
                </div>

                <div class="col-md-6 col-xl-3">
                    <div class="app-card stat-card">
                        <div class="stat-label">Success</div>
                        <div class="stat-value"><%= successLogs %></div>
                        <div class="stat-meta">Completed actions</div>
                    </div>
                </div>

                <div class="col-md-6 col-xl-3">
                    <div class="app-card stat-card">
                        <div class="stat-label">Warnings / Failed</div>
                        <div class="stat-value"><%= warningLogs + failedLogs %></div>
                        <div class="stat-meta"><%= warningLogs %> warnings · <%= failedLogs %> failed</div>
                    </div>
                </div>

                <div class="col-md-6 col-xl-3">
                    <div class="app-card stat-card">
                        <div class="stat-label">Today / Denied</div>
                        <div class="stat-value"><%= todayLogs %></div>
                        <div class="stat-meta"><%= deniedLogs %> access denied logs</div>
                    </div>
                </div>
            </div>

            <div class="app-card crud-card p-4">
                <div class="crud-toolbar">
                    <div>
                        <h4 class="fw-bold mb-1">Audit Trail Records</h4>
                        <p class="text-secondary mb-0">
                            Search and filter system activity records by user, action, module, status, or IP address.
                        </p>
                    </div>

                    <div class="d-flex gap-2 flex-wrap">
                        <div class="input-group search-control">
                            <span class="input-group-text">
                                <i class="bi bi-search"></i>
                            </span>

                            <input type="search"
                                   class="form-control"
                                   id="auditSearch"
                                   placeholder="Search audit logs">
                        </div>

                        <select class="form-select" id="auditStatusFilter" style="width: 170px;">
                            <option value="">All Status</option>
                            <option value="success">Success</option>
                            <option value="failed">Failed</option>
                            <option value="warning">Warning</option>
                            <option value="denied">Denied</option>
                        </select>

                        <select class="form-select" id="auditModuleFilter" style="width: 190px;">
                            <option value="">All Modules</option>
                            <option value="authentication">Authentication</option>
                            <option value="students">Students</option>
                            <option value="users">Users</option>
                            <option value="exams">Exams</option>
                            <option value="questions">Questions</option>
                            <option value="submissions">Submissions</option>
                            <option value="manual marking">Manual Marking</option>
                            <option value="results">Results</option>
                            <option value="result appeals">Result Appeals</option>
                            <option value="reports">Reports</option>
                            <option value="notifications">Notifications</option>
                            <option value="documents">Documents</option>
                            <option value="feedback">Feedback</option>
                            <option value="system">System</option>
                        </select>

                        <button type="button" class="btn btn-outline-primary" id="auditResetBtn">
                            <i class="bi bi-arrow-counterclockwise me-1"></i>
                            Reset
                        </button>
                    </div>
                </div>

                <% if (auditLogs == null || auditLogs.isEmpty()) { %>
                    <div class="empty-state">
                        <div class="empty-state-icon">
                            <i class="bi bi-shield-slash"></i>
                        </div>

                        <h5>No audit logs yet</h5>
                        <p>System activity records will appear here after important actions are logged.</p>
                    </div>
                <% } else { %>
                    <div class="audit-filter-counter" id="auditCounter">
                        <i class="bi bi-funnel-fill"></i>
                        <span>Showing all logs</span>
                    </div>

                    <div class="table-responsive">
                        <table class="table table-hover align-middle audit-table" id="auditTable">
                            <thead>
                            <tr>
                                <th>Audit ID</th>
                                <th>User</th>
                                <th>Action</th>
                                <th>Module</th>
                                <th>Description</th>
                                <th>Status</th>
                                <th>IP Address</th>
                                <th>Created At</th>
                            </tr>
                            </thead>

                            <tbody>
                            <% for (int i = auditLogs.size() - 1; i >= 0; i--) {
                                AuditLog log = auditLogs.get(i);
                            %>
                                <tr data-status="<%= FileUtil.h(log.getStatus().toLowerCase()) %>"
                                    data-module="<%= FileUtil.h(log.getModule().toLowerCase()) %>">
                                    <td class="fw-bold"><%= FileUtil.h(log.getAuditId()) %></td>

                                    <td>
                                        <strong><%= FileUtil.h(log.getUserId()) %></strong><br>
                                        <small class="text-secondary"><%= FileUtil.h(log.getUserRole()) %></small>
                                    </td>

                                    <td>
                                        <span class="badge badge-soft-secondary">
                                            <%= FileUtil.h(log.getAction()) %>
                                        </span>
                                    </td>

                                    <td>
                                        <span class="badge <%= log.getModuleBadgeClass() %>">
                                            <%= FileUtil.h(log.getModule()) %>
                                        </span>
                                    </td>

                                    <td class="audit-description-cell">
                                        <%= FileUtil.h(log.getDescription()) %>
                                    </td>

                                    <td>
                                        <span class="badge <%= log.getStatusBadgeClass() %>">
                                            <%= FileUtil.h(log.getStatus()) %>
                                        </span>
                                    </td>

                                    <td>
                                        <small class="text-secondary"><%= FileUtil.h(log.getIpAddress()) %></small>
                                    </td>

                                    <td>
                                        <small class="text-secondary"><%= FileUtil.h(log.getCreatedAt()) %></small>
                                    </td>
                                </tr>
                            <% } %>
                            </tbody>
                        </table>
                    </div>

                    <div class="advanced-filter-empty audit-empty-state" id="auditEmptyState" style="display:none;">
                        <div class="empty-state-icon">
                            <i class="bi bi-search"></i>
                        </div>
                        <h5>No matching audit logs found</h5>
                        <p>Try changing your search text or filter selection.</p>
                    </div>
                <% } %>
            </div>

        </section>
    </main>
</div>

<script>
    document.addEventListener("DOMContentLoaded", function () {
        const search = document.getElementById("auditSearch");
        const statusFilter = document.getElementById("auditStatusFilter");
        const moduleFilter = document.getElementById("auditModuleFilter");
        const resetBtn = document.getElementById("auditResetBtn");
        const rows = document.querySelectorAll("#auditTable tbody tr[data-status]");
        const counter = document.getElementById("auditCounter");
        const empty = document.getElementById("auditEmptyState");

        function normalize(value) {
            return (value || "").toLowerCase().trim();
        }

        function applyFilters() {
            const searchValue = normalize(search ? search.value : "");
            const statusValue = normalize(statusFilter ? statusFilter.value : "");
            const moduleValue = normalize(moduleFilter ? moduleFilter.value : "");

            let visibleCount = 0;

            rows.forEach(function (row) {
                const rowText = normalize(row.innerText);
                const rowStatus = normalize(row.getAttribute("data-status"));
                const rowModule = normalize(row.getAttribute("data-module"));

                const matchesSearch = !searchValue || rowText.includes(searchValue);
                const matchesStatus = !statusValue || rowStatus === statusValue;
                const matchesModule = !moduleValue || rowModule === moduleValue;

                const visible = matchesSearch && matchesStatus && matchesModule;

                row.style.display = visible ? "" : "none";

                if (visible) {
                    visibleCount++;
                }
            });

            if (counter) {
                const span = counter.querySelector("span");
                if (span) {
                    span.textContent = "Showing " + visibleCount + " of " + rows.length + " logs";
                }
            }

            if (empty) {
                empty.style.display = visibleCount === 0 ? "" : "none";
            }
        }

        if (search) {
            search.addEventListener("input", applyFilters);
        }

        if (statusFilter) {
            statusFilter.addEventListener("change", applyFilters);
        }

        if (moduleFilter) {
            moduleFilter.addEventListener("change", applyFilters);
        }

        if (resetBtn) {
            resetBtn.addEventListener("click", function () {
                if (search) search.value = "";
                if (statusFilter) statusFilter.value = "";
                if (moduleFilter) moduleFilter.value = "";
                applyFilters();
            });
        }

        applyFilters();
    });
</script>

<%@ include file="../includes/footer.jsp" %>