<%--
    Exam Integrity Review Dashboard.
    Allows Admin/Lecturer users to review suspicious exam console activity.

    Responsible Member:
    IT25103045 - De Silva H.L.D.C.P.C
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ page import="java.util.Collections" %>
<%@ page import="java.util.List" %>
<%@ page import="lk.nextexam.dao.FileUtil" %>
<%@ page import="lk.nextexam.dao.ExamIntegrityLogDAO" %>
<%@ page import="lk.nextexam.model.ExamIntegrityLog" %>

<%
    String pageTitle = "Exam Integrity";
    String activeMenu = "integrity";
    String topbarTitle = "Exam Integrity Review";

    ExamIntegrityLogDAO integrityLogDAO = new ExamIntegrityLogDAO();
    List<ExamIntegrityLog> logs = integrityLogDAO.getAllLogs(application);

    if (logs != null) {
        Collections.reverse(logs);
    }

    int totalLogs = logs != null ? logs.size() : 0;
    int tabSwitchCount = 0;
    int fullscreenExitCount = 0;
    int blockedActionCount = 0;
    int highRiskCount = 0;
    int mediumRiskCount = 0;
    int infoCount = 0;

    if (logs != null) {
        for (ExamIntegrityLog log : logs) {
            String eventType = log.getEventType();

            if (ExamIntegrityLog.EVENT_TAB_SWITCH.equalsIgnoreCase(eventType)) {
                tabSwitchCount++;
                mediumRiskCount++;
            } else if (ExamIntegrityLog.EVENT_FULLSCREEN_EXIT.equalsIgnoreCase(eventType)) {
                fullscreenExitCount++;
                mediumRiskCount++;
            } else if (ExamIntegrityLog.EVENT_COPY_BLOCKED.equalsIgnoreCase(eventType)
                    || ExamIntegrityLog.EVENT_PASTE_BLOCKED.equalsIgnoreCase(eventType)) {
                blockedActionCount++;
                highRiskCount++;
            } else if (ExamIntegrityLog.EVENT_RIGHT_CLICK_BLOCKED.equalsIgnoreCase(eventType)) {
                blockedActionCount++;
            } else {
                infoCount++;
            }
        }
    }

    int suspiciousCount = tabSwitchCount + fullscreenExitCount + blockedActionCount;
    int suspiciousRate = totalLogs > 0 ? (suspiciousCount * 100) / totalLogs : 0;
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
                            <i class="bi bi-shield-exclamation me-1"></i>
                            Exam Integrity Monitoring
                        </span>

                        <h1 class="hero-title">Exam Integrity Review</h1>

                        <p class="hero-text">
                            Review suspicious student actions recorded during online exams, including tab switches,
                            fullscreen exits, copy/paste attempts, right-click attempts, exam starts, and submissions.
                        </p>
                    </div>

                    <div class="d-flex gap-2 flex-wrap">
                        <a href="<%= request.getContextPath() %>/dashboard.jsp" class="btn btn-outline-primary">
                            <i class="bi bi-grid-1x2-fill me-2"></i>
                            Dashboard
                        </a>

                        <button type="button" class="btn btn-primary" onclick="window.print()">
                            <i class="bi bi-printer me-2"></i>
                            Print Report
                        </button>
                    </div>
                </div>
            </div>

            <div class="row g-3 mb-4">
                <div class="col-md-6 col-xl-3">
                    <div class="app-card stat-card">
                        <div class="d-flex justify-content-between gap-3">
                            <div>
                                <div class="stat-label">Total Logs</div>
                                <div class="stat-value"><%= totalLogs %></div>
                                <div class="stat-meta">Integrity events saved</div>
                            </div>

                            <div class="stat-icon">
                                <i class="bi bi-clipboard-data-fill"></i>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-md-6 col-xl-3">
                    <div class="app-card stat-card">
                        <div class="d-flex justify-content-between gap-3">
                            <div>
                                <div class="stat-label">Tab Switches</div>
                                <div class="stat-value"><%= tabSwitchCount %></div>
                                <div class="stat-meta">Student left exam tab</div>
                            </div>

                            <div class="stat-icon">
                                <i class="bi bi-window-stack"></i>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-md-6 col-xl-3">
                    <div class="app-card stat-card">
                        <div class="d-flex justify-content-between gap-3">
                            <div>
                                <div class="stat-label">Blocked Actions</div>
                                <div class="stat-value"><%= blockedActionCount %></div>
                                <div class="stat-meta">Copy/paste/right-click attempts</div>
                            </div>

                            <div class="stat-icon">
                                <i class="bi bi-ban"></i>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-md-6 col-xl-3">
                    <div class="app-card stat-card">
                        <div class="d-flex justify-content-between gap-3">
                            <div>
                                <div class="stat-label">Suspicious Rate</div>
                                <div class="stat-value"><%= suspiciousRate %>%</div>
                                <div class="stat-meta"><%= suspiciousCount %> suspicious events</div>
                            </div>

                            <div class="stat-icon">
                                <i class="bi bi-shield-fill-exclamation"></i>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row g-4 mb-4">
                <div class="col-xl-4">
                    <div class="app-card p-4 h-100">
                        <div class="d-flex justify-content-between align-items-start gap-3 mb-3">
                            <div>
                                <h4 class="fw-bold mb-1">Risk Summary</h4>
                                <p class="text-secondary mb-0">
                                    Automatic risk grouping based on event type.
                                </p>
                            </div>

                            <span class="badge badge-soft-danger">
                                Risk
                            </span>
                        </div>

                        <div class="integrity-risk-stack">
                            <div class="integrity-risk-item high">
                                <div>
                                    <small>High Risk</small>
                                    <strong><%= highRiskCount %></strong>
                                </div>
                                <span>Copy / paste attempts</span>
                            </div>

                            <div class="integrity-risk-item medium">
                                <div>
                                    <small>Medium Risk</small>
                                    <strong><%= mediumRiskCount %></strong>
                                </div>
                                <span>Tab switch / fullscreen exit</span>
                            </div>

                            <div class="integrity-risk-item info">
                                <div>
                                    <small>Info Events</small>
                                    <strong><%= infoCount %></strong>
                                </div>
                                <span>Exam start, submit, fullscreen request</span>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-xl-8">
                    <div class="app-card p-4 h-100">
                        <div class="d-flex justify-content-between align-items-start flex-wrap gap-3 mb-3">
                            <div>
                                <h4 class="fw-bold mb-1">Review Guidelines</h4>
                                <p class="text-secondary mb-0">
                                    Use this dashboard as supporting evidence, not as the only proof of cheating.
                                </p>
                            </div>

                            <span class="badge badge-soft-primary">
                                Academic Integrity
                            </span>
                        </div>

                        <div class="row g-3">
                            <div class="col-md-6">
                                <div class="exam-info-box">
                                    <small>Low Risk</small>
                                    <strong>Right-click blocked or normal system event</strong>
                                </div>
                            </div>

                            <div class="col-md-6">
                                <div class="exam-info-box">
                                    <small>Medium Risk</small>
                                    <strong>Tab switching or fullscreen exit</strong>
                                </div>
                            </div>

                            <div class="col-md-6">
                                <div class="exam-info-box">
                                    <small>High Risk</small>
                                    <strong>Copy or paste attempt during exam</strong>
                                </div>
                            </div>

                            <div class="col-md-6">
                                <div class="exam-info-box">
                                    <small>Staff Action</small>
                                    <strong>Review repeated suspicious actions manually</strong>
                                </div>
                            </div>
                        </div>

                        <div class="alert alert-info mt-4 mb-0">
                            <strong>Note:</strong>
                            Browser warnings may also happen due to accidental clicks or device issues.
                            Final decisions should be made by the lecturer or administrator after review.
                        </div>
                    </div>
                </div>
            </div>

            <div class="app-card crud-card p-4">
                <div class="crud-toolbar">
                    <div>
                        <h4 class="fw-bold mb-1">Integrity Log Records</h4>
                        <p class="text-secondary mb-0">
                            Search and filter saved exam integrity events.
                        </p>
                    </div>

                    <div class="d-flex gap-2 flex-wrap">
                        <div class="input-group search-control">
                            <span class="input-group-text">
                                <i class="bi bi-search"></i>
                            </span>

                            <input type="search"
                                   class="form-control"
                                   id="integritySearch"
                                   placeholder="Search student, exam, event, description">
                        </div>

                        <select class="form-select" id="riskFilter" style="width: 160px;">
                            <option value="">All Risk</option>
                            <option value="high">High</option>
                            <option value="medium">Medium</option>
                            <option value="low">Low</option>
                            <option value="info">Info</option>
                        </select>
                    </div>
                </div>

                <% if (logs == null || logs.isEmpty()) { %>
                    <div class="empty-state">
                        <div class="empty-state-icon">
                            <i class="bi bi-shield-check"></i>
                        </div>

                        <h5>No integrity logs yet</h5>
                        <p>
                            Logs will appear here after students start exams and integrity events are recorded.
                        </p>
                    </div>
                <% } else { %>
                    <div class="table-responsive">
                        <table class="table table-hover align-middle integrity-table" id="integrityTable">
                            <thead>
                            <tr>
                                <th>Log ID</th>
                                <th>Student ID</th>
                                <th>Exam ID</th>
                                <th>Event Type</th>
                                <th>Risk</th>
                                <th>Description</th>
                                <th>Created At</th>
                            </tr>
                            </thead>

                            <tbody>
                            <% for (ExamIntegrityLog log : logs) {
                                String eventType = log.getEventType();
                                String risk = "Info";
                                String riskKey = "info";
                                String riskClass = "badge-soft-secondary";
                                String eventClass = "badge-soft-secondary";
                                String icon = "bi-info-circle-fill";

                                if (ExamIntegrityLog.EVENT_COPY_BLOCKED.equalsIgnoreCase(eventType)
                                        || ExamIntegrityLog.EVENT_PASTE_BLOCKED.equalsIgnoreCase(eventType)) {
                                    risk = "High";
                                    riskKey = "high";
                                    riskClass = "badge-soft-danger";
                                    eventClass = "badge-soft-danger";
                                    icon = "bi-exclamation-octagon-fill";
                                } else if (ExamIntegrityLog.EVENT_TAB_SWITCH.equalsIgnoreCase(eventType)
                                        || ExamIntegrityLog.EVENT_FULLSCREEN_EXIT.equalsIgnoreCase(eventType)) {
                                    risk = "Medium";
                                    riskKey = "medium";
                                    riskClass = "badge-soft-warning";
                                    eventClass = "badge-soft-warning";
                                    icon = "bi-exclamation-triangle-fill";
                                } else if (ExamIntegrityLog.EVENT_RIGHT_CLICK_BLOCKED.equalsIgnoreCase(eventType)) {
                                    risk = "Low";
                                    riskKey = "low";
                                    riskClass = "badge-soft-info";
                                    eventClass = "badge-soft-info";
                                    icon = "bi-shield-exclamation";
                                }
                            %>
                                <tr data-risk="<%= FileUtil.h(riskKey) %>">
                                    <td class="fw-bold"><%= FileUtil.h(log.getLogId()) %></td>
                                    <td><%= FileUtil.h(log.getStudentId()) %></td>
                                    <td><%= FileUtil.h(log.getExamId()) %></td>
                                    <td>
                                        <span class="badge <%= eventClass %>">
                                            <i class="bi <%= icon %> me-1"></i>
                                            <%= FileUtil.h(eventType) %>
                                        </span>
                                    </td>
                                    <td>
                                        <span class="badge <%= riskClass %>">
                                            <%= FileUtil.h(risk) %>
                                        </span>
                                    </td>
                                    <td><%= FileUtil.h(log.getDescription()) %></td>
                                    <td>
                                        <small class="text-secondary">
                                            <%= FileUtil.h(log.getCreatedAt()) %>
                                        </small>
                                    </td>
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

<script>
    document.addEventListener("DOMContentLoaded", function () {
        const searchInput = document.getElementById("integritySearch");
        const riskFilter = document.getElementById("riskFilter");
        const rows = document.querySelectorAll("#integrityTable tbody tr[data-risk]");

        function filterRows() {
            const searchValue = searchInput ? searchInput.value.toLowerCase().trim() : "";
            const riskValue = riskFilter ? riskFilter.value.toLowerCase().trim() : "";

            rows.forEach(function (row) {
                const text = row.innerText.toLowerCase();
                const risk = row.getAttribute("data-risk") || "";

                const matchesSearch = text.includes(searchValue);
                const matchesRisk = riskValue === "" || risk === riskValue;

                row.style.display = matchesSearch && matchesRisk ? "" : "none";
            });
        }

        if (searchInput) {
            searchInput.addEventListener("input", filterRows);
        }

        if (riskFilter) {
            riskFilter.addEventListener("change", filterRows);
        }
    });
</script>

<%@ include file="../includes/footer.jsp" %>