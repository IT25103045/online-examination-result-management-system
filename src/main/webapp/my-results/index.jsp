<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="lk.nextexam.dao.FileUtil" %>
<%@ page import="lk.nextexam.model.Result" %>

<%
    String pageTitle = "My Results";
    String activeMenu = "my-results";
    String topbarTitle = "My Results";

    String studentId = request.getAttribute("studentId") != null
            ? request.getAttribute("studentId").toString()
            : "";

    String studentName = request.getAttribute("studentName") != null
            ? request.getAttribute("studentName").toString()
            : "Student";

    List<Result> myResults = (List<Result>) request.getAttribute("myResults");

    int totalResults = request.getAttribute("totalResults") != null
            ? (Integer) request.getAttribute("totalResults")
            : (myResults != null ? myResults.size() : 0);

    int passCount = request.getAttribute("passCount") != null
            ? (Integer) request.getAttribute("passCount")
            : 0;

    int failCount = request.getAttribute("failCount") != null
            ? (Integer) request.getAttribute("failCount")
            : 0;

    double averageMarks = request.getAttribute("averageMarks") != null
            ? (Double) request.getAttribute("averageMarks")
            : 0.0;

    String averageMarksDisplay = averageMarks == Math.floor(averageMarks)
            ? String.valueOf((int) averageMarks)
            : String.format("%.1f", averageMarks);

    int passRate = totalResults > 0 ? (passCount * 100) / totalResults : 0;

    String error = request.getParameter("error");
    String alertMessage = "";

    if ("accessDenied".equalsIgnoreCase(error)) {
        alertMessage = "You do not have permission to access that page.";
    } else if ("sessionExpired".equalsIgnoreCase(error)) {
        alertMessage = "Your session has expired. Please log in again.";
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
                            <i class="bi bi-bar-chart-fill me-1"></i>
                            Student Result Center
                        </span>

                        <h1 class="hero-title">My Published Results</h1>

                        <p class="hero-text">
                            Welcome, <strong><%= FileUtil.h(studentName) %></strong>. View your published examination
                            results, grades, pass/fail status, and verification information.
                        </p>
                    </div>

                    <div class="d-flex gap-2 flex-wrap">
                        <a href="<%= request.getContextPath() %>/my-exams" class="btn btn-primary">
                            <i class="bi bi-journal-check me-2"></i>
                            My Exams
                        </a>

                        <a href="<%= request.getContextPath() %>/notices" class="btn btn-outline-primary">
                            <i class="bi bi-megaphone-fill me-2"></i>
                            Notices
                        </a>
                    </div>
                </div>
            </div>

            <% if (!alertMessage.isEmpty()) { %>
                <div class="alert alert-danger" data-auto-close="5500">
                    <i class="bi bi-exclamation-triangle-fill me-1"></i>
                    <%= FileUtil.h(alertMessage) %>
                </div>
            <% } %>

            <div class="row g-3 mb-4">
                <div class="col-md-6 col-xl-3">
                    <div class="app-card stat-card">
                        <div class="d-flex justify-content-between gap-3">
                            <div>
                                <div class="stat-label">Published Results</div>
                                <div class="stat-value"><%= totalResults %></div>
                                <div class="stat-meta">Visible result records</div>
                            </div>

                            <div class="stat-icon">
                                <i class="bi bi-file-earmark-bar-graph-fill"></i>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-md-6 col-xl-3">
                    <div class="app-card stat-card">
                        <div class="d-flex justify-content-between gap-3">
                            <div>
                                <div class="stat-label">Passed</div>
                                <div class="stat-value"><%= passCount %></div>
                                <div class="stat-meta"><%= passRate %>% pass rate</div>
                            </div>

                            <div class="stat-icon">
                                <i class="bi bi-check-circle-fill"></i>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-md-6 col-xl-3">
                    <div class="app-card stat-card">
                        <div class="d-flex justify-content-between gap-3">
                            <div>
                                <div class="stat-label">Failed</div>
                                <div class="stat-value"><%= failCount %></div>
                                <div class="stat-meta">Below pass range</div>
                            </div>

                            <div class="stat-icon">
                                <i class="bi bi-x-circle-fill"></i>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-md-6 col-xl-3">
                    <div class="app-card stat-card">
                        <div class="d-flex justify-content-between gap-3">
                            <div>
                                <div class="stat-label">Average Marks</div>
                                <div class="stat-value"><%= averageMarksDisplay %>%</div>
                                <div class="stat-meta">Overall average</div>
                            </div>

                            <div class="stat-icon">
                                <i class="bi bi-speedometer2"></i>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row g-4 mb-4">
                <div class="col-xl-8">
                    <div class="app-card p-4 h-100">
                        <div class="d-flex justify-content-between align-items-start flex-wrap gap-3 mb-4">
                            <div>
                                <h4 class="fw-bold mb-1">Result Records</h4>
                                <p class="text-secondary mb-0">
                                    Only verified and published results are displayed here.
                                </p>
                            </div>

                            <span class="badge badge-soft-success">
                                <i class="bi bi-shield-check me-1"></i>
                                Published View
                            </span>
                        </div>

                        <div class="table-responsive">
                            <table class="table table-hover align-middle" id="myResultsTable">
                                <thead>
                                <tr>
                                    <th>Result ID</th>
                                    <th>Exam ID</th>
                                    <th>Marks</th>
                                    <th>Grade</th>
                                    <th>Status</th>
                                    <th>Verification</th>
                                    <th>Performance</th>
                                </tr>
                                </thead>

                                <tbody>
                                <% if (myResults == null || myResults.isEmpty()) { %>
                                    <tr>
                                        <td colspan="7">
                                            <div class="empty-state">
                                                <div class="empty-state-icon">
                                                    <i class="bi bi-inbox"></i>
                                                </div>

                                                <h5>No published results yet</h5>
                                                <p>
                                                    Your results will appear here after they are verified and published
                                                    by the lecturer or administrator.
                                                </p>
                                            </div>
                                        </td>
                                    </tr>
                                <% } else {
                                    for (Result result : myResults) {
                                %>
                                    <tr>
                                        <td class="fw-bold"><%= FileUtil.h(result.getResultId()) %></td>
                                        <td><%= FileUtil.h(result.getExamId()) %></td>
                                        <td><%= FileUtil.h(result.getDisplayMarks()) %></td>

                                        <td>
                                            <span class="badge <%= result.getGradeBadgeClass() %>">
                                                <%= FileUtil.h(result.getGrade()) %>
                                            </span>
                                        </td>

                                        <td>
                                            <span class="badge <%= result.getStatusBadgeClass() %>">
                                                <%= FileUtil.h(result.getStatus()) %>
                                            </span>
                                        </td>

                                        <td>
                                            <span class="badge <%= result.getVerificationBadgeClass() %>">
                                                <%= FileUtil.h(result.getVerification()) %>
                                            </span>
                                        </td>

                                        <td>
                                            <span class="badge badge-soft-secondary">
                                                <%= FileUtil.h(result.getPerformanceLabel()) %>
                                            </span>
                                        </td>
                                    </tr>
                                <% }
                                } %>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>

                <div class="col-xl-4">
                    <div class="app-card p-4 h-100">
                        <div class="d-flex justify-content-between align-items-start mb-3">
                            <div>
                                <h4 class="fw-bold mb-1">Grade Guide</h4>
                                <p class="text-secondary mb-0">
                                    Standard grading scale.
                                </p>
                            </div>

                            <span class="badge badge-soft-primary">
                                Guide
                            </span>
                        </div>

                        <div class="exam-rules-list">
                            <div class="exam-rule-item">
                                <i class="bi bi-award-fill"></i>
                                <span><strong>A</strong> — 75 to 100 marks</span>
                            </div>

                            <div class="exam-rule-item">
                                <i class="bi bi-award"></i>
                                <span><strong>B</strong> — 65 to 74 marks</span>
                            </div>

                            <div class="exam-rule-item">
                                <i class="bi bi-award"></i>
                                <span><strong>C</strong> — 55 to 64 marks</span>
                            </div>

                            <div class="exam-rule-item">
                                <i class="bi bi-check-circle-fill"></i>
                                <span><strong>S</strong> — 40 to 54 marks</span>
                            </div>

                            <div class="exam-rule-item">
                                <i class="bi bi-x-circle-fill"></i>
                                <span><strong>F</strong> — Below 40 marks</span>
                            </div>
                        </div>

                        <div class="soft-divider"></div>

                        <div class="exam-info-box mb-3">
                            <small>Student ID</small>
                            <strong><%= FileUtil.h(studentId) %></strong>
                        </div>

                        <div class="exam-info-box">
                            <small>Result Visibility</small>
                            <strong>Only published results are shown</strong>
                        </div>

                        <div class="alert alert-info mt-4 mb-0">
                            <strong>Note:</strong>
                            If an exam is submitted but not shown here, the result may still be under marking,
                            verification, or publishing review.
                        </div>
                    </div>
                </div>
            </div>

        </section>
    </main>
</div>

<%@ include file="../includes/footer.jsp" %>