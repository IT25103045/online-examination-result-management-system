<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="lk.nextexam.dao.FileUtil" %>
<%@ page import="lk.nextexam.model.Result" %>
<%@ page import="lk.nextexam.dao.ResultDAO" %>

<%
    String pageTitle = "Results";
    String activeMenu = "results";
    String topbarTitle = "Result Management";

    List<Result> results = (List<Result>) request.getAttribute("results");

    if (results == null) {
        ResultDAO resultDAO = new ResultDAO();
        results = resultDAO.getAllResults(application);
    }

    int totalResults = results != null ? results.size() : 0;
    int passCount = 0;
    int failCount = 0;
    int pendingCount = 0;
    double totalMarks = 0.0;

    int gradeA = 0;
    int gradeB = 0;
    int gradeC = 0;
    int gradeS = 0;
    int gradeF = 0;
    int verifiedCount = 0;
    int reviewCount = 0;
    int verificationPendingCount = 0;
    int publishedCount = 0;
    int notPublishedCount = 0;

    if (results != null) {
        for (Result result : results) {
            if ("Pass".equalsIgnoreCase(result.getStatus())) {
                passCount++;
            } else if ("Fail".equalsIgnoreCase(result.getStatus())) {
                failCount++;
            } else {
                pendingCount++;
            }

            if ("A".equalsIgnoreCase(result.getGrade())) {
                gradeA++;
            } else if ("B".equalsIgnoreCase(result.getGrade())) {
                gradeB++;
            } else if ("C".equalsIgnoreCase(result.getGrade())) {
                gradeC++;
            } else if ("S".equalsIgnoreCase(result.getGrade())) {
                gradeS++;
            } else if ("F".equalsIgnoreCase(result.getGrade())) {
                gradeF++;
            }

            if ("Verified".equalsIgnoreCase(result.getVerification())) {
                verifiedCount++;
            } else if ("Review".equalsIgnoreCase(result.getVerification())) {
                reviewCount++;
            } else {
                verificationPendingCount++;
            }

            if ("Published".equalsIgnoreCase(result.getPublished())) {
                publishedCount++;
            } else {
                notPublishedCount++;
            }

            try {
                totalMarks += Double.parseDouble(result.getMarks());
            } catch (Exception e) {
                totalMarks += 0.0;
            }
        }
    }

    double averageMarks = totalResults > 0 ? totalMarks / totalResults : 0.0;

    int verifiedPercentage = totalResults > 0 ? (verifiedCount * 100) / totalResults : 0;
    int publishedPercentage = totalResults > 0 ? (publishedCount * 100) / totalResults : 0;

    int gradeAPercentage = totalResults > 0 ? (gradeA * 100) / totalResults : 0;
    int gradeBPercentage = totalResults > 0 ? (gradeB * 100) / totalResults : 0;
    int gradeCPercentage = totalResults > 0 ? (gradeC * 100) / totalResults : 0;
    int gradeSPercentage = totalResults > 0 ? (gradeS * 100) / totalResults : 0;
    int gradeFPercentage = totalResults > 0 ? (gradeF * 100) / totalResults : 0;

    String averageMarksDisplay = averageMarks == Math.floor(averageMarks)
            ? String.valueOf((int) averageMarks)
            : String.format("%.1f", averageMarks);

    String success = request.getParameter("success");
    String error = request.getParameter("error");

    String alertType = "";
    String alertMessage = "";

    if (success != null) {
        alertType = "success";

        if ("resultAdded".equalsIgnoreCase(success)) {
            alertMessage = "Result record created successfully.";
        } else if ("resultUpdated".equalsIgnoreCase(success)) {
            alertMessage = "Result record updated successfully.";
        } else if ("resultDeleted".equalsIgnoreCase(success)) {
            alertMessage = "Result record deleted successfully.";
        } else {
            alertMessage = "Operation completed successfully.";
        }
    }

    if (error != null) {
        alertType = "danger";

        if ("missingResultId".equalsIgnoreCase(error)) {
            alertMessage = "Result ID is missing.";
        } else if ("missingStudentId".equalsIgnoreCase(error)) {
            alertMessage = "Student ID is missing.";
        } else if ("missingExamId".equalsIgnoreCase(error)) {
            alertMessage = "Exam ID is missing.";
        } else if ("invalidMarks".equalsIgnoreCase(error)) {
            alertMessage = "Invalid marks value. Marks must be between 0 and 100.";
        } else if ("resultAddFailed".equalsIgnoreCase(error)) {
            alertMessage = "Result could not be created. Check duplicate Result ID or incomplete details.";
        } else if ("resultUpdateFailed".equalsIgnoreCase(error)) {
            alertMessage = "Result could not be updated.";
        } else if ("resultDeleteFailed".equalsIgnoreCase(error)) {
            alertMessage = "Result could not be deleted.";
        } else {
            alertMessage = "Something went wrong. Please check the result details and try again.";
        }
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
                            NextExamLK Result Processing Center
                        </span>

                        <h1 class="hero-title">Result Management</h1>

                        <p class="hero-text">
                            Enter marks, calculate grades, verify result records, and control publishing visibility
                            for student result access.
                        </p>
                    </div>

                    <div class="d-flex gap-2 flex-wrap">
                        <button class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#resultModal">
                            <i class="bi bi-plus-lg me-2"></i>
                            Add Result
                        </button>

                        <a href="<%= request.getContextPath() %>/students" class="btn btn-outline-primary">
                            <i class="bi bi-people-fill me-2"></i>
                            Students
                        </a>
                    </div>
                </div>
            </div>

            <% if (!alertMessage.isEmpty()) { %>
                <div class="alert alert-<%= alertType %>" data-auto-close="5500">
                    <% if ("success".equals(alertType)) { %>
                        <i class="bi bi-check-circle-fill me-1"></i>
                    <% } else { %>
                        <i class="bi bi-exclamation-triangle-fill me-1"></i>
                    <% } %>
                    <%= FileUtil.h(alertMessage) %>
                </div>
            <% } %>

            <div class="row g-3 mb-4">
                <div class="col-md-6 col-xl-3">
                    <div class="app-card stat-card">
                        <div class="d-flex justify-content-between gap-3">
                            <div>
                                <div class="stat-label">Total Results</div>
                                <div class="stat-value"><%= totalResults %></div>
                                <div class="stat-meta">Recorded result entries</div>
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
                                <div class="stat-label">Pass Count</div>
                                <div class="stat-value"><%= passCount %></div>
                                <div class="stat-meta"><%= failCount %> failed · <%= pendingCount %> pending</div>
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
                                <div class="stat-label">Verified</div>
                                <div class="stat-value"><%= verifiedCount %></div>
                                <div class="stat-meta"><%= verifiedPercentage %>% verification rate</div>
                            </div>

                            <div class="stat-icon">
                                <i class="bi bi-shield-check"></i>
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
                                <div class="stat-meta">Overall performance rate</div>
                            </div>

                            <div class="stat-icon">
                                <i class="bi bi-speedometer2"></i>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row g-4 mb-4">

                <div class="col-xl-4">
                    <div class="app-card form-card h-100 p-4">
                        <div class="d-flex align-items-start justify-content-between gap-3 mb-3">
                            <div>
                                <h4 class="fw-bold mb-1">Add Result</h4>
                                <p class="text-secondary mb-0">
                                    Enter marks and preview grade before saving.
                                </p>
                            </div>

                            <span class="badge badge-soft-primary">Grade Preview</span>
                        </div>

                        <form class="needs-validation"
                              novalidate
                              action="<%= request.getContextPath() %>/results"
                              method="post">

                            <input type="hidden" name="action" value="add">

                            <div class="mb-3">
                                <label class="form-label">Result ID <span class="required">*</span></label>
                                <input type="text"
                                       name="resultId"
                                       class="form-control"
                                       placeholder="Example: R004"
                                       maxlength="30"
                                       required>
                                <div class="invalid-feedback">Result ID is required.</div>
                            </div>

                            <div class="mb-3">
                                <label class="form-label">Student ID <span class="required">*</span></label>
                                <input type="text"
                                       name="studentId"
                                       class="form-control"
                                       placeholder="Example: STU001"
                                       maxlength="30"
                                       required>
                                <div class="invalid-feedback">Student ID is required.</div>
                            </div>

                            <div class="mb-3">
                                <label class="form-label">Exam ID <span class="required">*</span></label>
                                <input type="text"
                                       name="examId"
                                       class="form-control"
                                       placeholder="Example: EX001"
                                       maxlength="30"
                                       required>
                                <div class="invalid-feedback">Exam ID is required.</div>
                            </div>

                            <div class="mb-3">
                                <label class="form-label">Marks <span class="required">*</span></label>
                                <input type="number"
                                       id="marks"
                                       name="marks"
                                       class="form-control"
                                       min="0"
                                       max="100"
                                       step="0.5"
                                       placeholder="Enter marks between 0 and 100"
                                       required>
                                <div class="invalid-feedback">Marks must be between 0 and 100.</div>
                            </div>

                            <div class="mb-3">
                                <label class="form-label">Grade Preview</label>

                                <div id="gradePreview" class="exam-info-box">
                                    <span class="badge badge-soft-secondary">--</span>
                                    <span class="ms-2 text-secondary">Enter marks to preview grade.</span>
                                </div>

                                <input type="hidden" id="grade" name="grade">
                                <input type="hidden" id="status" name="status">
                            </div>

                            <div class="mb-3">
                                <label class="form-label">Verification <span class="required">*</span></label>
                                <select name="verification" class="form-select" required>
                                    <option value="">Choose verification status</option>
                                    <option value="Verified">Verified</option>
                                    <option value="Pending">Pending</option>
                                    <option value="Review">Review</option>
                                </select>
                                <div class="invalid-feedback">Verification status is required.</div>
                            </div>

                            <div class="mb-3">
                                <label class="form-label">Publishing Status <span class="required">*</span></label>
                                <select name="published" class="form-select" required>
                                    <option value="">Choose publishing status</option>
                                    <option value="Published">Published</option>
                                    <option value="Not Published">Not Published</option>
                                </select>
                                <div class="invalid-feedback">Publishing status is required.</div>
                            </div>

                            <div class="alert alert-info mb-3">
                                <strong>Grade Rule:</strong><br>
                                A: 75–100, B: 65–74, C: 55–64, S: 40–54, F: Below 40.
                            </div>

                            <button class="btn btn-primary w-100" type="submit">
                                <i class="bi bi-save me-2"></i>
                                Save Result
                            </button>
                        </form>
                    </div>
                </div>

                <div class="col-xl-8">
                    <div class="app-card p-4 h-100">
                        <div class="d-flex justify-content-between align-items-start flex-wrap gap-3 mb-4">
                            <div>
                                <h4 class="fw-bold mb-1">Grade Distribution</h4>
                                <p class="text-secondary mb-0">
                                    Overview of student performance by grade category.
                                </p>
                            </div>

                            <span class="badge badge-soft-secondary">Live Summary</span>
                        </div>

                        <div class="row g-3">
                            <div class="col-md-6">
                                <div class="exam-info-box">
                                    <div class="d-flex justify-content-between align-items-center mb-2">
                                        <span class="fw-bold">Grade A</span>
                                        <span class="badge badge-soft-success"><%= gradeA %> Students</span>
                                    </div>
                                    <div class="progress" style="height: 9px;">
                                        <div class="progress-bar bg-success" style="width: <%= gradeAPercentage %>%;"></div>
                                    </div>
                                    <small class="text-secondary">Excellent performance range</small>
                                </div>
                            </div>

                            <div class="col-md-6">
                                <div class="exam-info-box">
                                    <div class="d-flex justify-content-between align-items-center mb-2">
                                        <span class="fw-bold">Grade B</span>
                                        <span class="badge badge-soft-primary"><%= gradeB %> Students</span>
                                    </div>
                                    <div class="progress" style="height: 9px;">
                                        <div class="progress-bar" style="width: <%= gradeBPercentage %>%;"></div>
                                    </div>
                                    <small class="text-secondary">Strong performance range</small>
                                </div>
                            </div>

                            <div class="col-md-6">
                                <div class="exam-info-box">
                                    <div class="d-flex justify-content-between align-items-center mb-2">
                                        <span class="fw-bold">Grade C</span>
                                        <span class="badge badge-soft-info"><%= gradeC %> Students</span>
                                    </div>
                                    <div class="progress" style="height: 9px;">
                                        <div class="progress-bar bg-info" style="width: <%= gradeCPercentage %>%;"></div>
                                    </div>
                                    <small class="text-secondary">Average performance range</small>
                                </div>
                            </div>

                            <div class="col-md-6">
                                <div class="exam-info-box">
                                    <div class="d-flex justify-content-between align-items-center mb-2">
                                        <span class="fw-bold">Grade S</span>
                                        <span class="badge badge-soft-warning"><%= gradeS %> Students</span>
                                    </div>
                                    <div class="progress" style="height: 9px;">
                                        <div class="progress-bar bg-warning" style="width: <%= gradeSPercentage %>%;"></div>
                                    </div>
                                    <small class="text-secondary">Minimum pass range</small>
                                </div>
                            </div>

                            <div class="col-md-6">
                                <div class="exam-info-box">
                                    <div class="d-flex justify-content-between align-items-center mb-2">
                                        <span class="fw-bold">Grade F</span>
                                        <span class="badge badge-soft-danger"><%= gradeF %> Students</span>
                                    </div>
                                    <div class="progress" style="height: 9px;">
                                        <div class="progress-bar bg-danger" style="width: <%= gradeFPercentage %>%;"></div>
                                    </div>
                                    <small class="text-secondary">Below pass range</small>
                                </div>
                            </div>

                            <div class="col-md-6">
                                <div class="exam-info-box">
                                    <div class="d-flex justify-content-between align-items-center mb-2">
                                        <span class="fw-bold">Verification Status</span>
                                        <span class="badge badge-soft-warning"><%= verifiedPercentage %>%</span>
                                    </div>
                                    <div class="progress" style="height: 9px;">
                                        <div class="progress-bar bg-warning" style="width: <%= verifiedPercentage %>%;"></div>
                                    </div>
                                    <small class="text-secondary"><%= reviewCount %> in review · <%= verificationPendingCount %> pending</small>
                                </div>
                            </div>
                        </div>

                        <div class="alert alert-info mt-4 mb-0">
                            <strong>Result workflow:</strong>
                            marks are entered, grades are calculated, records are verified, and final results are published for student access.
                        </div>
                    </div>
                </div>
            </div>

            <div class="row g-4 mb-4">
                <div class="col-xl-6">
                    <div class="app-card p-4 h-100">
                        <div class="d-flex justify-content-between align-items-start mb-3">
                            <div>
                                <h4 class="fw-bold mb-1">Result Verification Checklist</h4>
                                <p class="text-secondary mb-0">
                                    Recommended checks before publishing results.
                                </p>
                            </div>

                            <span class="badge badge-soft-secondary">Review</span>
                        </div>

                        <div class="readiness-board border-0 shadow-none p-0">
                            <div class="readiness-item mb-3">
                                <div class="d-flex justify-content-between mb-1">
                                    <span class="fw-semibold">Marks Entered</span>
                                    <span class="fw-bold"><%= totalResults > 0 ? 100 : 0 %>%</span>
                                </div>
                                <div class="progress" style="height: 9px;">
                                    <div class="progress-bar bg-success" style="width: <%= totalResults > 0 ? 100 : 0 %>%;"></div>
                                </div>
                                <small class="text-secondary">Saved result records in the system.</small>
                            </div>

                            <div class="readiness-item mb-3">
                                <div class="d-flex justify-content-between mb-1">
                                    <span class="fw-semibold">Lecturer Verification</span>
                                    <span class="fw-bold"><%= verifiedPercentage %>%</span>
                                </div>
                                <div class="progress" style="height: 9px;">
                                    <div class="progress-bar bg-warning" style="width: <%= verifiedPercentage %>%;"></div>
                                </div>
                                <small class="text-secondary">Result records checked by lecturers/admins.</small>
                            </div>

                            <div class="readiness-item">
                                <div class="d-flex justify-content-between mb-1">
                                    <span class="fw-semibold">Student Publishing</span>
                                    <span class="fw-bold"><%= publishedPercentage %>%</span>
                                </div>
                                <div class="progress" style="height: 9px;">
                                    <div class="progress-bar bg-info" style="width: <%= publishedPercentage %>%;"></div>
                                </div>
                                <small class="text-secondary"><%= publishedCount %> published · <%= notPublishedCount %> not published</small>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-xl-6">
                    <div class="app-card p-4 h-100">
                        <div class="d-flex justify-content-between align-items-start mb-3">
                            <div>
                                <h4 class="fw-bold mb-1">Result Processing Workflow</h4>
                                <p class="text-secondary mb-0">
                                    Standard grading and publishing lifecycle.
                                </p>
                            </div>

                            <span class="badge badge-soft-primary">Process</span>
                        </div>

                        <div class="timeline">
                            <div class="timeline-item">
                                <div class="activity-title">Enter Marks</div>
                                <small class="text-secondary">Lecturer records marks against student and exam IDs.</small>
                            </div>

                            <div class="timeline-item">
                                <div class="activity-title">Calculate Grade</div>
                                <small class="text-secondary">System previews grade and pass/fail status from marks.</small>
                            </div>

                            <div class="timeline-item">
                                <div class="activity-title">Verify Record</div>
                                <small class="text-secondary">Lecturer or admin verifies result accuracy.</small>
                            </div>

                            <div class="timeline-item">
                                <div class="activity-title">Publish Result</div>
                                <small class="text-secondary">Verified results become available through the student portal.</small>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="page-header">
                <div>
                    <h2 class="page-title">Result Records</h2>
                    <p class="page-description">
                        View, search, update, verify, publish, and delete student examination result records.
                    </p>
                </div>

                <div class="d-flex gap-2 flex-wrap">
                    <button class="btn btn-outline-primary" type="button" onclick="window.print()">
                        <i class="bi bi-printer me-2"></i>
                        Print
                    </button>

                    <button class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#resultModal">
                        <i class="bi bi-plus-lg me-2"></i>
                        Add Result
                    </button>
                </div>
            </div>

            <div class="app-card crud-card p-4">
                <div class="crud-toolbar">
                    <div class="input-group search-control">
                        <span class="input-group-text">
                            <i class="bi bi-search"></i>
                        </span>

                        <input type="search"
                               class="form-control"
                               id="resultSearch"
                               placeholder="Search by result ID, student ID, exam ID, grade, status or verification">
                    </div>

                    <div class="d-flex gap-2 flex-wrap">
                        <select class="form-select" id="gradeFilter" style="width: 150px;">
                            <option value="">All Grades</option>
                            <option value="a">A</option>
                            <option value="b">B</option>
                            <option value="c">C</option>
                            <option value="s">S</option>
                            <option value="f">F</option>
                        </select>

                        <select class="form-select" id="statusFilter" style="width: 150px;">
                            <option value="">All Status</option>
                            <option value="pass">Pass</option>
                            <option value="fail">Fail</option>
                            <option value="pending">Pending</option>
                        </select>

                        <select class="form-select" id="verificationFilter" style="width: 170px;">
                            <option value="">All Verification</option>
                            <option value="verified">Verified</option>
                            <option value="pending">Pending</option>
                            <option value="review">Review</option>
                        </select>

                        <select class="form-select" id="publishedFilter" style="width: 180px;">
                            <option value="">All Publishing</option>
                            <option value="published">Published</option>
                            <option value="not published">Not Published</option>
                        </select>
                    </div>
                </div>

                <div class="table-responsive">
                    <table class="table table-hover align-middle" id="resultsTable">
                        <thead>
                        <tr>
                            <th>Result ID</th>
                            <th>Student ID</th>
                            <th>Exam ID</th>
                            <th>Marks</th>
                            <th>Grade</th>
                            <th>Status</th>
                            <th>Verification</th>
                            <th>Published</th>
                            <th class="text-end">Actions</th>
                        </tr>
                        </thead>

                        <tbody>
                        <% if (results != null && !results.isEmpty()) {
                            for (Result result : results) {
                                String gradeClass = "badge-soft-secondary";
                                String statusClass = "badge-soft-secondary";
                                String verificationClass = "badge-soft-secondary";
                                String publishedClass = "badge-soft-secondary";

                                if ("A".equalsIgnoreCase(result.getGrade())) {
                                    gradeClass = "badge-soft-success";
                                } else if ("B".equalsIgnoreCase(result.getGrade())) {
                                    gradeClass = "badge-soft-primary";
                                } else if ("C".equalsIgnoreCase(result.getGrade())) {
                                    gradeClass = "badge-soft-info";
                                } else if ("S".equalsIgnoreCase(result.getGrade())) {
                                    gradeClass = "badge-soft-warning";
                                } else if ("F".equalsIgnoreCase(result.getGrade())) {
                                    gradeClass = "badge-soft-danger";
                                }

                                if ("Pass".equalsIgnoreCase(result.getStatus())) {
                                    statusClass = "badge-soft-success";
                                } else if ("Fail".equalsIgnoreCase(result.getStatus())) {
                                    statusClass = "badge-soft-danger";
                                } else {
                                    statusClass = "badge-soft-warning";
                                }

                                if ("Verified".equalsIgnoreCase(result.getVerification())) {
                                    verificationClass = "badge-soft-success";
                                } else if ("Pending".equalsIgnoreCase(result.getVerification())) {
                                    verificationClass = "badge-soft-warning";
                                } else if ("Review".equalsIgnoreCase(result.getVerification())) {
                                    verificationClass = "badge-soft-info";
                                }

                                if ("Published".equalsIgnoreCase(result.getPublished())) {
                                    publishedClass = "badge-soft-primary";
                                } else {
                                    publishedClass = "badge-soft-secondary";
                                }
                        %>
                            <tr data-grade="<%= FileUtil.h(result.getGrade().toLowerCase()) %>"
                                data-status="<%= FileUtil.h(result.getStatus().toLowerCase()) %>"
                                data-verification="<%= FileUtil.h(result.getVerification().toLowerCase()) %>"
                                data-published="<%= FileUtil.h(result.getPublished().toLowerCase()) %>">
                                <td class="fw-bold"><%= FileUtil.h(result.getResultId()) %></td>
                                <td><%= FileUtil.h(result.getStudentId()) %></td>
                                <td><%= FileUtil.h(result.getExamId()) %></td>
                                <td><%= FileUtil.h(result.getMarks()) %></td>

                                <td>
                                    <span class="badge <%= gradeClass %>">
                                        <%= FileUtil.h(result.getGrade()) %>
                                    </span>
                                </td>

                                <td>
                                    <span class="badge <%= statusClass %>">
                                        <%= FileUtil.h(result.getStatus()) %>
                                    </span>
                                </td>

                                <td>
                                    <span class="badge <%= verificationClass %>">
                                        <%= FileUtil.h(result.getVerification()) %>
                                    </span>
                                </td>

                                <td>
                                    <span class="badge <%= publishedClass %>">
                                        <%= FileUtil.h(result.getPublished()) %>
                                    </span>
                                </td>

                                <td>
                                    <div class="action-group">
                                        <button class="btn btn-sm btn-outline-primary"
                                                type="button"
                                                title="View Result"
                                                data-bs-toggle="modal"
                                                data-bs-target="#viewResultModal"
                                                data-result-id="<%= FileUtil.h(result.getResultId()) %>"
                                                data-student-id="<%= FileUtil.h(result.getStudentId()) %>"
                                                data-exam-id="<%= FileUtil.h(result.getExamId()) %>"
                                                data-marks="<%= FileUtil.h(result.getMarks()) %>"
                                                data-grade="<%= FileUtil.h(result.getGrade()) %>"
                                                data-status="<%= FileUtil.h(result.getStatus()) %>"
                                                data-verification="<%= FileUtil.h(result.getVerification()) %>"
                                                data-published="<%= FileUtil.h(result.getPublished()) %>">
                                            <i class="bi bi-eye"></i>
                                        </button>

                                        <button class="btn btn-sm btn-outline-primary"
                                                type="button"
                                                title="Edit Result"
                                                data-bs-toggle="modal"
                                                data-bs-target="#editResultModal"
                                                data-result-id="<%= FileUtil.h(result.getResultId()) %>"
                                                data-student-id="<%= FileUtil.h(result.getStudentId()) %>"
                                                data-exam-id="<%= FileUtil.h(result.getExamId()) %>"
                                                data-marks="<%= FileUtil.h(result.getMarks()) %>"
                                                data-grade="<%= FileUtil.h(result.getGrade()) %>"
                                                data-status="<%= FileUtil.h(result.getStatus()) %>"
                                                data-verification="<%= FileUtil.h(result.getVerification()) %>"
                                                data-published="<%= FileUtil.h(result.getPublished()) %>">
                                            <i class="bi bi-pencil-square"></i>
                                        </button>

                                        <button class="btn btn-sm btn-outline-danger"
                                                type="button"
                                                title="Delete Result"
                                                data-bs-toggle="modal"
                                                data-bs-target="#deleteModal"
                                                data-delete-name="<%= FileUtil.h(result.getResultId() + " - " + result.getStudentId()) %>"
                                                data-delete-id="<%= FileUtil.h(result.getResultId()) %>"
                                                data-delete-url="<%= request.getContextPath() %>/results">
                                            <i class="bi bi-trash3"></i>
                                        </button>
                                    </div>
                                </td>
                            </tr>
                        <% }
                        } else { %>
                            <tr>
                                <td colspan="9">
                                    <div class="empty-state">
                                        <div class="empty-state-icon">
                                            <i class="bi bi-inbox"></i>
                                        </div>
                                        <h5>No result records found</h5>
                                        <p>Add a result to display records here.</p>
                                    </div>
                                </td>
                            </tr>
                        <% } %>
                        </tbody>
                    </table>
                </div>
            </div>

        </section>
    </main>
</div>

<!-- Add Result Modal -->
<div class="modal fade" id="resultModal" tabindex="-1" aria-labelledby="resultModalTitle" aria-hidden="true">
    <div class="modal-dialog modal-lg modal-dialog-centered">
        <div class="modal-content border-0 shadow-lg">

            <form class="needs-validation"
                  novalidate
                  action="<%= request.getContextPath() %>/results"
                  method="post">

                <input type="hidden" name="action" value="add">

                <div class="modal-header">
                    <div>
                        <h5 class="modal-title fw-bold" id="resultModalTitle">Add Result Record</h5>
                        <small class="text-secondary">
                            Save marks, grade, verification, and publishing status for a selected student and exam.
                        </small>
                    </div>

                    <button class="btn-close" type="button" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>

                <div class="modal-body">
                    <div class="row g-3">
                        <div class="col-md-6">
                            <label class="form-label">Result ID <span class="required">*</span></label>
                            <input type="text"
                                   name="resultId"
                                   class="form-control"
                                   placeholder="Example: R004"
                                   maxlength="30"
                                   required>
                            <div class="invalid-feedback">Result ID is required.</div>
                        </div>

                        <div class="col-md-6">
                            <label class="form-label">Student ID <span class="required">*</span></label>
                            <input type="text"
                                   name="studentId"
                                   class="form-control"
                                   placeholder="Example: STU004"
                                   maxlength="30"
                                   required>
                            <div class="invalid-feedback">Student ID is required.</div>
                        </div>

                        <div class="col-md-6">
                            <label class="form-label">Exam ID <span class="required">*</span></label>
                            <input type="text"
                                   name="examId"
                                   class="form-control"
                                   placeholder="Example: EX001"
                                   maxlength="30"
                                   required>
                            <div class="invalid-feedback">Exam ID is required.</div>
                        </div>

                        <div class="col-md-6">
                            <label class="form-label">Marks <span class="required">*</span></label>
                            <input type="number"
                                   name="marks"
                                   class="form-control result-marks-input"
                                   min="0"
                                   max="100"
                                   step="0.5"
                                   placeholder="Enter marks"
                                   required>
                            <div class="invalid-feedback">Marks must be between 0 and 100.</div>
                        </div>

                        <div class="col-md-6">
                            <label class="form-label">Grade <span class="required">*</span></label>
                            <select name="grade" class="form-select result-grade-select" required>
                                <option value="">Choose grade</option>
                                <option value="A">A</option>
                                <option value="B">B</option>
                                <option value="C">C</option>
                                <option value="S">S</option>
                                <option value="F">F</option>
                            </select>
                            <div class="invalid-feedback">Grade is required.</div>
                        </div>

                        <div class="col-md-6">
                            <label class="form-label">Status <span class="required">*</span></label>
                            <select name="status" class="form-select result-status-select" required>
                                <option value="">Choose status</option>
                                <option value="Pass">Pass</option>
                                <option value="Fail">Fail</option>
                                <option value="Pending">Pending</option>
                            </select>
                            <div class="invalid-feedback">Status is required.</div>
                        </div>

                        <div class="col-md-6">
                            <label class="form-label">Verification <span class="required">*</span></label>
                            <select name="verification" class="form-select" required>
                                <option value="">Choose verification status</option>
                                <option value="Verified">Verified</option>
                                <option value="Pending">Pending</option>
                                <option value="Review">Review</option>
                            </select>
                            <div class="invalid-feedback">Verification status is required.</div>
                        </div>

                        <div class="col-md-6">
                            <label class="form-label">Published <span class="required">*</span></label>
                            <select name="published" class="form-select" required>
                                <option value="">Choose publishing status</option>
                                <option value="Published">Published</option>
                                <option value="Not Published">Not Published</option>
                            </select>
                            <div class="invalid-feedback">Publishing status is required.</div>
                        </div>
                    </div>

                    <div class="alert alert-info mt-4 mb-0">
                        <strong>Record workflow:</strong>
                        Add the result first, verify it, then publish it when ready for students.
                    </div>
                </div>

                <div class="modal-footer">
                    <button class="btn btn-outline-secondary" type="button" data-bs-dismiss="modal">
                        Cancel
                    </button>

                    <button class="btn btn-primary" type="submit">
                        <i class="bi bi-save me-2"></i>
                        Save Result
                    </button>
                </div>
            </form>

        </div>
    </div>
</div>

<!-- Edit Result Modal -->
<div class="modal fade" id="editResultModal" tabindex="-1" aria-labelledby="editResultModalTitle" aria-hidden="true">
    <div class="modal-dialog modal-lg modal-dialog-centered">
        <div class="modal-content border-0 shadow-lg">

            <form class="needs-validation"
                  novalidate
                  action="<%= request.getContextPath() %>/results"
                  method="post">

                <input type="hidden" name="action" value="update">

                <div class="modal-header">
                    <div>
                        <h5 class="modal-title fw-bold" id="editResultModalTitle">Edit Result</h5>
                        <small class="text-secondary">
                            Update an existing result record in the NextExamLK result system.
                        </small>
                    </div>

                    <button class="btn-close" type="button" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>

                <div class="modal-body">
                    <div class="row g-3">
                        <div class="col-md-6">
                            <label class="form-label">Result ID</label>
                            <input type="text"
                                   id="editResultId"
                                   name="resultId"
                                   class="form-control"
                                   readonly
                                   required>
                            <div class="invalid-feedback">Result ID is required.</div>
                        </div>

                        <div class="col-md-6">
                            <label class="form-label">Student ID</label>
                            <input type="text"
                                   id="editStudentId"
                                   name="studentId"
                                   class="form-control"
                                   maxlength="30"
                                   required>
                            <div class="invalid-feedback">Student ID is required.</div>
                        </div>

                        <div class="col-md-6">
                            <label class="form-label">Exam ID</label>
                            <input type="text"
                                   id="editExamId"
                                   name="examId"
                                   class="form-control"
                                   maxlength="30"
                                   required>
                            <div class="invalid-feedback">Exam ID is required.</div>
                        </div>

                        <div class="col-md-6">
                            <label class="form-label">Marks</label>
                            <input type="number"
                                   id="editMarks"
                                   name="marks"
                                   class="form-control result-marks-input"
                                   min="0"
                                   max="100"
                                   step="0.5"
                                   required>
                            <div class="invalid-feedback">Marks must be between 0 and 100.</div>
                        </div>

                        <div class="col-md-6">
                            <label class="form-label">Grade</label>
                            <select id="editGrade" name="grade" class="form-select result-grade-select" required>
                                <option value="">Choose grade</option>
                                <option value="A">A</option>
                                <option value="B">B</option>
                                <option value="C">C</option>
                                <option value="S">S</option>
                                <option value="F">F</option>
                            </select>
                            <div class="invalid-feedback">Grade is required.</div>
                        </div>

                        <div class="col-md-6">
                            <label class="form-label">Status</label>
                            <select id="editStatus" name="status" class="form-select result-status-select" required>
                                <option value="">Choose status</option>
                                <option value="Pass">Pass</option>
                                <option value="Fail">Fail</option>
                                <option value="Pending">Pending</option>
                            </select>
                            <div class="invalid-feedback">Status is required.</div>
                        </div>

                        <div class="col-md-6">
                            <label class="form-label">Verification</label>
                            <select id="editVerification" name="verification" class="form-select" required>
                                <option value="">Choose verification status</option>
                                <option value="Verified">Verified</option>
                                <option value="Pending">Pending</option>
                                <option value="Review">Review</option>
                            </select>
                            <div class="invalid-feedback">Verification status is required.</div>
                        </div>

                        <div class="col-md-6">
                            <label class="form-label">Published</label>
                            <select id="editPublished" name="published" class="form-select" required>
                                <option value="">Choose publishing status</option>
                                <option value="Published">Published</option>
                                <option value="Not Published">Not Published</option>
                            </select>
                            <div class="invalid-feedback">Publishing status is required.</div>
                        </div>
                    </div>

                    <div class="alert alert-info mt-4 mb-0">
                        <strong>Update workflow:</strong>
                        This updates the existing result record in <code>results.txt</code>.
                    </div>
                </div>

                <div class="modal-footer">
                    <button class="btn btn-outline-secondary" type="button" data-bs-dismiss="modal">
                        Cancel
                    </button>

                    <button class="btn btn-primary" type="submit">
                        <i class="bi bi-save me-2"></i>
                        Update Result
                    </button>
                </div>
            </form>

        </div>
    </div>
</div>

<!-- View Result Modal -->
<div class="modal fade" id="viewResultModal" tabindex="-1" aria-labelledby="viewResultModalTitle" aria-hidden="true">
    <div class="modal-dialog modal-lg modal-dialog-centered">
        <div class="modal-content border-0 shadow-lg">

            <div class="modal-header">
                <div>
                    <h5 class="modal-title fw-bold" id="viewResultModalTitle">Result Details</h5>
                    <small class="text-secondary">
                        View selected student result record.
                    </small>
                </div>

                <button class="btn-close" type="button" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>

            <div class="modal-body">
                <div class="row g-3">
                    <div class="col-md-4">
                        <div class="exam-info-box">
                            <small>Result ID</small>
                            <strong id="viewResultId">-</strong>
                        </div>
                    </div>

                    <div class="col-md-4">
                        <div class="exam-info-box">
                            <small>Student ID</small>
                            <strong id="viewStudentId">-</strong>
                        </div>
                    </div>

                    <div class="col-md-4">
                        <div class="exam-info-box">
                            <small>Exam ID</small>
                            <strong id="viewExamId">-</strong>
                        </div>
                    </div>

                    <div class="col-md-3">
                        <div class="exam-info-box">
                            <small>Marks</small>
                            <strong id="viewMarks">-</strong>
                        </div>
                    </div>

                    <div class="col-md-3">
                        <div class="exam-info-box">
                            <small>Grade</small>
                            <strong id="viewGrade">-</strong>
                        </div>
                    </div>

                    <div class="col-md-3">
                        <div class="exam-info-box">
                            <small>Status</small>
                            <strong id="viewStatus">-</strong>
                        </div>
                    </div>

                    <div class="col-md-3">
                        <div class="exam-info-box">
                            <small>Verification</small>
                            <strong id="viewVerification">-</strong>
                        </div>
                    </div>

                    <div class="col-md-6">
                        <div class="exam-info-box">
                            <small>Published</small>
                            <strong id="viewPublished">-</strong>
                        </div>
                    </div>
                </div>

                <div class="alert alert-info mt-4 mb-0">
                    <strong>Result record:</strong>
                    These details are loaded from the selected result row and displayed for quick review.
                </div>
            </div>

            <div class="modal-footer">
                <button class="btn btn-outline-secondary" type="button" data-bs-dismiss="modal">
                    Close
                </button>
            </div>

        </div>
    </div>
</div>

<script>
    document.addEventListener("DOMContentLoaded", function () {
        const editResultModal = document.getElementById("editResultModal");
        const viewResultModal = document.getElementById("viewResultModal");

        const resultSearch = document.getElementById("resultSearch");
        const gradeFilter = document.getElementById("gradeFilter");
        const statusFilter = document.getElementById("statusFilter");
        const verificationFilter = document.getElementById("verificationFilter");
        const publishedFilter = document.getElementById("publishedFilter");
        const resultRows = document.querySelectorAll("#resultsTable tbody tr[data-grade]");

        function getResultData(button) {
            return {
                resultId: button.getAttribute("data-result-id") || "",
                studentId: button.getAttribute("data-student-id") || "",
                examId: button.getAttribute("data-exam-id") || "",
                marks: button.getAttribute("data-marks") || "",
                grade: button.getAttribute("data-grade") || "",
                status: button.getAttribute("data-status") || "",
                verification: button.getAttribute("data-verification") || "",
                published: button.getAttribute("data-published") || ""
            };
        }

        function calculateGradeAndStatus(marksValue) {
            const marks = Number(marksValue);

            if (Number.isNaN(marks) || marks < 0 || marks > 100) {
                return {
                    grade: "",
                    status: ""
                };
            }

            if (marks >= 75) {
                return {
                    grade: "A",
                    status: "Pass"
                };
            }

            if (marks >= 65) {
                return {
                    grade: "B",
                    status: "Pass"
                };
            }

            if (marks >= 55) {
                return {
                    grade: "C",
                    status: "Pass"
                };
            }

            if (marks >= 40) {
                return {
                    grade: "S",
                    status: "Pass"
                };
            }

            return {
                grade: "F",
                status: "Fail"
            };
        }

        function updateGradeFromMarks(input) {
            const form = input.closest("form");

            if (!form) {
                return;
            }

            const gradeSelect = form.querySelector(".result-grade-select");
            const statusSelect = form.querySelector(".result-status-select");
            const result = calculateGradeAndStatus(input.value);

            if (gradeSelect) {
                gradeSelect.value = result.grade;
            }

            if (statusSelect) {
                statusSelect.value = result.status;
            }
        }

        document.querySelectorAll(".result-marks-input").forEach(function (input) {
            input.addEventListener("input", function () {
                updateGradeFromMarks(input);
            });
        });

        function filterResults() {
            const searchValue = resultSearch ? resultSearch.value.toLowerCase().trim() : "";
            const gradeValue = gradeFilter ? gradeFilter.value.toLowerCase().trim() : "";
            const statusValue = statusFilter ? statusFilter.value.toLowerCase().trim() : "";
            const verificationValue = verificationFilter ? verificationFilter.value.toLowerCase().trim() : "";
            const publishedValue = publishedFilter ? publishedFilter.value.toLowerCase().trim() : "";

            resultRows.forEach(function (row) {
                const rowText = row.innerText.toLowerCase();
                const rowGrade = row.getAttribute("data-grade") || "";
                const rowStatus = row.getAttribute("data-status") || "";
                const rowVerification = row.getAttribute("data-verification") || "";
                const rowPublished = row.getAttribute("data-published") || "";

                const matchesSearch = rowText.includes(searchValue);
                const matchesGrade = gradeValue === "" || rowGrade === gradeValue;
                const matchesStatus = statusValue === "" || rowStatus === statusValue;
                const matchesVerification = verificationValue === "" || rowVerification === verificationValue;
                const matchesPublished = publishedValue === "" || rowPublished === publishedValue;

                row.style.display = matchesSearch && matchesGrade && matchesStatus && matchesVerification && matchesPublished ? "" : "none";
            });
        }

        [resultSearch, gradeFilter, statusFilter, verificationFilter, publishedFilter].forEach(function (element) {
            if (!element) {
                return;
            }

            element.addEventListener("input", filterResults);
            element.addEventListener("change", filterResults);
        });

        if (editResultModal) {
            editResultModal.addEventListener("show.bs.modal", function (event) {
                const button = event.relatedTarget;

                if (!button) {
                    return;
                }

                const result = getResultData(button);

                document.getElementById("editResultId").value = result.resultId;
                document.getElementById("editStudentId").value = result.studentId;
                document.getElementById("editExamId").value = result.examId;
                document.getElementById("editMarks").value = result.marks;
                document.getElementById("editGrade").value = result.grade;
                document.getElementById("editStatus").value = result.status;
                document.getElementById("editVerification").value = result.verification;
                document.getElementById("editPublished").value = result.published;
            });
        }

        if (viewResultModal) {
            viewResultModal.addEventListener("show.bs.modal", function (event) {
                const button = event.relatedTarget;

                if (!button) {
                    return;
                }

                const result = getResultData(button);

                document.getElementById("viewResultId").textContent = result.resultId || "-";
                document.getElementById("viewStudentId").textContent = result.studentId || "-";
                document.getElementById("viewExamId").textContent = result.examId || "-";
                document.getElementById("viewMarks").textContent = result.marks || "-";
                document.getElementById("viewGrade").textContent = result.grade || "-";
                document.getElementById("viewStatus").textContent = result.status || "-";
                document.getElementById("viewVerification").textContent = result.verification || "-";
                document.getElementById("viewPublished").textContent = result.published || "-";
            });
        }
    });
</script>

<%@ include file="../includes/delete-modal.jsp" %>
<%@ include file="../includes/footer.jsp" %>