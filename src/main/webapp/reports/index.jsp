<%--
    CSV Export Reports page.
    Allows staff users to export system records as CSV files.

    Responsible Member:
    IT25103045 - De Silva H.L.D.C.P.C
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%
    String pageTitle = "Reports";
    String activeMenu = "reports";
    String topbarTitle = "CSV Export Reports";

    String error = request.getParameter("error");

    String alertMessage = "";

    if ("missingType".equalsIgnoreCase(error)) {
        alertMessage = "Please select a valid report type.";
    } else if ("invalidType".equalsIgnoreCase(error)) {
        alertMessage = "Invalid report type selected.";
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
                            <i class="bi bi-filetype-csv me-1"></i>
                            Academic Reporting Center
                        </span>

                        <h1 class="hero-title">CSV Export Reports</h1>

                        <p class="hero-text">
                            Export academic and administrative records as CSV files for Excel analysis,
                            documentation, auditing, and academic record keeping.
                        </p>
                    </div>

                    <a href="<%= request.getContextPath() %>/dashboard.jsp" class="btn btn-outline-primary">
                        <i class="bi bi-speedometer2 me-2"></i>
                        Dashboard
                    </a>
                </div>
            </div>

            <% if (!alertMessage.isEmpty()) { %>
                <div class="alert alert-danger">
                    <i class="bi bi-exclamation-triangle-fill me-1"></i>
                    <%= alertMessage %>
                </div>
            <% } %>

            <div class="row g-4">
                <div class="col-md-6 col-xl-4">
                    <div class="app-card report-export-card">
                        <div class="report-export-icon">
                            <i class="bi bi-people-fill"></i>
                        </div>

                        <h4>Student Report</h4>
                        <p>Export student profiles, contact details, course, batch, and exam eligibility status.</p>

                        <a href="<%= request.getContextPath() %>/export-report?type=students" class="btn btn-primary w-100">
                            <i class="bi bi-download me-2"></i>
                            Export Students CSV
                        </a>
                    </div>
                </div>

                <div class="col-md-6 col-xl-4">
                    <div class="app-card report-export-card">
                        <div class="report-export-icon">
                            <i class="bi bi-person-gear"></i>
                        </div>

                        <h4>User Report</h4>
                        <p>Export system users including user ID, username, email, role, and account status.</p>

                        <a href="<%= request.getContextPath() %>/export-report?type=users" class="btn btn-primary w-100">
                            <i class="bi bi-download me-2"></i>
                            Export Users CSV
                        </a>
                    </div>
                </div>

                <div class="col-md-6 col-xl-4">
                    <div class="app-card report-export-card">
                        <div class="report-export-icon">
                            <i class="bi bi-journal-check"></i>
                        </div>

                        <h4>Exam Report</h4>
                        <p>Export exam schedule details, subject, date, duration, total marks, and status.</p>

                        <a href="<%= request.getContextPath() %>/export-report?type=exams" class="btn btn-primary w-100">
                            <i class="bi bi-download me-2"></i>
                            Export Exams CSV
                        </a>
                    </div>
                </div>

                <div class="col-md-6 col-xl-4">
                    <div class="app-card report-export-card">
                        <div class="report-export-icon">
                            <i class="bi bi-patch-question-fill"></i>
                        </div>

                        <h4>Question Bank Report</h4>
                        <p>Export MCQ and essay questions, answers, marks, status, and model answers.</p>

                        <a href="<%= request.getContextPath() %>/export-report?type=questions" class="btn btn-primary w-100">
                            <i class="bi bi-download me-2"></i>
                            Export Questions CSV
                        </a>
                    </div>
                </div>

                <div class="col-md-6 col-xl-4">
                    <div class="app-card report-export-card">
                        <div class="report-export-icon">
                            <i class="bi bi-inboxes-fill"></i>
                        </div>

                        <h4>Submission Report</h4>
                        <p>Export student exam attempts, scores, percentages, answer counts, and marking status.</p>

                        <a href="<%= request.getContextPath() %>/export-report?type=submissions" class="btn btn-primary w-100">
                            <i class="bi bi-download me-2"></i>
                            Export Submissions CSV
                        </a>
                    </div>
                </div>

                <div class="col-md-6 col-xl-4">
                    <div class="app-card report-export-card">
                        <div class="report-export-icon">
                            <i class="bi bi-bar-chart-fill"></i>
                        </div>

                        <h4>Result Report</h4>
                        <p>Export result records, marks, grade, verification status, and publication status.</p>

                        <a href="<%= request.getContextPath() %>/export-report?type=results" class="btn btn-primary w-100">
                            <i class="bi bi-download me-2"></i>
                            Export Results CSV
                        </a>
                    </div>
                </div>

                <div class="col-md-6 col-xl-4">
                    <div class="app-card report-export-card">
                        <div class="report-export-icon">
                            <i class="bi bi-arrow-repeat"></i>
                        </div>

                        <h4>Appeal Report</h4>
                        <p>Export student result recheck requests, reason, message, status, and staff replies.</p>

                        <a href="<%= request.getContextPath() %>/export-report?type=appeals" class="btn btn-primary w-100">
                            <i class="bi bi-download me-2"></i>
                            Export Appeals CSV
                        </a>
                    </div>
                </div>

                <div class="col-md-6 col-xl-4">
                    <div class="app-card report-export-card">
                        <div class="report-export-icon">
                            <i class="bi bi-bell-fill"></i>
                        </div>

                        <h4>Notification Report</h4>
                        <p>Export notification records including target users, roles, types, read status, and URLs.</p>

                        <a href="<%= request.getContextPath() %>/export-report?type=notifications" class="btn btn-primary w-100">
                            <i class="bi bi-download me-2"></i>
                            Export Notifications CSV
                        </a>
                    </div>
                </div>

                <div class="col-md-6 col-xl-4">
                    <div class="app-card report-export-card">
                        <div class="report-export-icon">
                            <i class="bi bi-chat-dots-fill"></i>
                        </div>

                        <h4>Feedback Report</h4>
                        <p>Export student feedback, categories, messages, submitted dates, and status values.</p>

                        <a href="<%= request.getContextPath() %>/export-report?type=feedback" class="btn btn-primary w-100">
                            <i class="bi bi-download me-2"></i>
                            Export Feedback CSV
                        </a>
                    </div>
                </div>
            </div>

            <div class="app-card p-4 mt-4">
                <div class="d-flex align-items-start gap-3">
                    <div class="report-note-icon">
                        <i class="bi bi-info-circle-fill"></i>
                    </div>

                    <div>
                        <h5 class="fw-bold mb-1">Export Note</h5>
                        <p class="text-secondary mb-0">
                            CSV reports are generated from the current file-based records. These files can be opened
                            in Microsoft Excel, Google Sheets, or any spreadsheet software for analysis and printing.
                        </p>
                    </div>
                </div>
            </div>

        </section>
    </main>
</div>

<%@ include file="../includes/footer.jsp" %>