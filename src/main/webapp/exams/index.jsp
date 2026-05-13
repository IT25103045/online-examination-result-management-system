<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="lk.nextexam.dao.FileUtil" %>
<%@ page import="lk.nextexam.model.Exam" %>
<%@ page import="lk.nextexam.dao.ExamDAO" %>

<%
    String pageTitle = "Exams";
    String activeMenu = "exams";
    String topbarTitle = "Exam Management";

    List<Exam> exams = (List<Exam>) request.getAttribute("exams");

    if (exams == null) {
        ExamDAO examDAO = new ExamDAO();
        exams = examDAO.getAllExams(application);
    }

    int totalExams = exams != null ? exams.size() : 0;
    int draftExams = 0;
    int scheduledExams = 0;
    int activeExams = 0;
    int ongoingExams = 0;
    int completedExams = 0;
    int publishedExams = 0;
    int cancelledExams = 0;
    int inactiveExams = 0;
    int attemptableExams = 0;
    int totalDuration = 0;

    if (exams != null) {
        for (Exam exam : exams) {
            if (exam.isDraft()) {
                draftExams++;
            } else if (exam.isScheduled()) {
                scheduledExams++;
            } else if (exam.isActive()) {
                activeExams++;
            } else if (exam.isOngoing()) {
                ongoingExams++;
            } else if (exam.isCompleted()) {
                completedExams++;
            } else if (exam.isPublished()) {
                publishedExams++;
            } else if (exam.isCancelled()) {
                cancelledExams++;
            } else if (exam.isInactive()) {
                inactiveExams++;
            }

            if (exam.canStudentAttempt()) {
                attemptableExams++;
            }

            totalDuration += exam.getDurationMinutes();
        }
    }

    int averageDuration = totalExams > 0 ? totalDuration / totalExams : 0;

    String success = request.getParameter("success");
    String error = request.getParameter("error");

    String alertType = "";
    String alertMessage = "";

    if (success != null) {
        alertType = "success";

        if ("examAdded".equalsIgnoreCase(success)) {
            alertMessage = "Exam created successfully.";
        } else if ("examUpdated".equalsIgnoreCase(success)) {
            alertMessage = "Exam updated successfully.";
        } else if ("examDeleted".equalsIgnoreCase(success)) {
            alertMessage = "Exam deleted successfully.";
        } else if ("examStatusUpdated".equalsIgnoreCase(success)) {
            alertMessage = "Exam status updated successfully.";
        } else {
            alertMessage = "Operation completed successfully.";
        }
    }

    if (error != null) {
        alertType = "danger";

        if ("missingExamId".equalsIgnoreCase(error)) {
            alertMessage = "Exam ID is missing.";
        } else if ("examNotFound".equalsIgnoreCase(error)) {
            alertMessage = "The selected exam could not be found.";
        } else if ("invalidExamDate".equalsIgnoreCase(error)) {
            alertMessage = "Invalid exam date. Please use a valid date.";
        } else if ("invalidDuration".equalsIgnoreCase(error)) {
            alertMessage = "Invalid duration. Duration must be between 1 and 360 minutes.";
        } else if ("invalidTotalMarks".equalsIgnoreCase(error)) {
            alertMessage = "Invalid total marks. Please enter a valid marks value.";
        } else if ("invalidStatus".equalsIgnoreCase(error)) {
            alertMessage = "Invalid exam status selected.";
        } else if ("examAddFailed".equalsIgnoreCase(error)) {
            alertMessage = "Exam could not be created. Check duplicate Exam ID or invalid details.";
        } else if ("examUpdateFailed".equalsIgnoreCase(error)) {
            alertMessage = "Exam could not be updated.";
        } else if ("examDeleteFailed".equalsIgnoreCase(error)) {
            alertMessage = "Exam could not be deleted. Ongoing, completed, or published exams should be cancelled instead.";
        } else if ("examStatusUpdateFailed".equalsIgnoreCase(error)) {
            alertMessage = "Exam status could not be updated.";
        } else {
            alertMessage = "Something went wrong. Please check the exam details and try again.";
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
                            <i class="bi bi-journal-check me-1"></i>
                            NextExamLK Exam Scheduling Center
                        </span>

                        <h1 class="hero-title">Exam Management</h1>

                        <p class="hero-text">
                            Create, schedule, activate, monitor, complete, and publish examination records.
                            Use <strong>Scheduled</strong>, <strong>Active</strong>, or <strong>Ongoing</strong>
                            when students should see the exam in their portal.
                        </p>
                    </div>

                    <div class="d-flex gap-2 flex-wrap">
                        <button class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#examModal">
                            <i class="bi bi-plus-lg me-2"></i>
                            Add Exam
                        </button>

                        <a href="<%= request.getContextPath() %>/questions" class="btn btn-outline-primary">
                            <i class="bi bi-patch-question-fill me-2"></i>
                            Question Bank
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
                                <div class="stat-label">Total Exams</div>
                                <div class="stat-value"><%= totalExams %></div>
                                <div class="stat-meta">All examination records</div>
                            </div>

                            <div class="stat-icon">
                                <i class="bi bi-journals"></i>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-md-6 col-xl-3">
                    <div class="app-card stat-card">
                        <div class="d-flex justify-content-between gap-3">
                            <div>
                                <div class="stat-label">Scheduled</div>
                                <div class="stat-value"><%= scheduledExams %></div>
                                <div class="stat-meta">Upcoming assessments</div>
                            </div>

                            <div class="stat-icon">
                                <i class="bi bi-calendar-check-fill"></i>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-md-6 col-xl-3">
                    <div class="app-card stat-card">
                        <div class="d-flex justify-content-between gap-3">
                            <div>
                                <div class="stat-label">Student Access</div>
                                <div class="stat-value"><%= attemptableExams %></div>
                                <div class="stat-meta">Scheduled / active / ongoing</div>
                            </div>

                            <div class="stat-icon">
                                <i class="bi bi-laptop-fill"></i>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-md-6 col-xl-3">
                    <div class="app-card stat-card">
                        <div class="d-flex justify-content-between gap-3">
                            <div>
                                <div class="stat-label">Average Duration</div>
                                <div class="stat-value"><%= averageDuration %></div>
                                <div class="stat-meta">Minutes per exam</div>
                            </div>

                            <div class="stat-icon">
                                <i class="bi bi-stopwatch-fill"></i>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row g-4 mb-4">
                <div class="col-xl-7">
                    <div class="app-card p-4 h-100">
                        <div class="d-flex justify-content-between align-items-start flex-wrap gap-3 mb-4">
                            <div>
                                <h4 class="fw-bold mb-1">Exam Status Overview</h4>
                                <p class="text-secondary mb-0">
                                    Track lifecycle readiness before students access online exams.
                                </p>
                            </div>

                            <span class="badge badge-soft-primary">
                                <i class="bi bi-speedometer2 me-1"></i>
                                Lifecycle
                            </span>
                        </div>

                        <div class="readiness-board border-0 shadow-none p-0">
                            <div class="readiness-item mb-3">
                                <div class="d-flex justify-content-between mb-1">
                                    <span class="fw-semibold">Draft Exams</span>
                                    <span class="fw-bold"><%= draftExams %></span>
                                </div>

                                <div class="progress" style="height: 9px;">
                                    <div class="progress-bar bg-warning"
                                         style="width: <%= totalExams > 0 ? (draftExams * 100 / totalExams) : 0 %>%;"></div>
                                </div>

                                <small class="text-secondary">Created but not yet ready for students.</small>
                            </div>

                            <div class="readiness-item mb-3">
                                <div class="d-flex justify-content-between mb-1">
                                    <span class="fw-semibold">Attemptable Exams</span>
                                    <span class="fw-bold"><%= attemptableExams %></span>
                                </div>

                                <div class="progress" style="height: 9px;">
                                    <div class="progress-bar bg-success"
                                         style="width: <%= totalExams > 0 ? (attemptableExams * 100 / totalExams) : 0 %>%;"></div>
                                </div>

                                <small class="text-secondary">Scheduled, active, or ongoing exams visible to students.</small>
                            </div>

                            <div class="readiness-item">
                                <div class="d-flex justify-content-between mb-1">
                                    <span class="fw-semibold">Closed Exams</span>
                                    <span class="fw-bold"><%= completedExams + publishedExams + cancelledExams + inactiveExams %></span>
                                </div>

                                <div class="progress" style="height: 9px;">
                                    <div class="progress-bar bg-info"
                                         style="width: <%= totalExams > 0 ? ((completedExams + publishedExams + cancelledExams + inactiveExams) * 100 / totalExams) : 0 %>%;"></div>
                                </div>

                                <small class="text-secondary">Completed, published, cancelled, or inactive exams.</small>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-xl-5">
                    <div class="app-card p-4 h-100">
                        <div class="d-flex justify-content-between align-items-start mb-3">
                            <div>
                                <h4 class="fw-bold mb-1">Recommended Workflow</h4>
                                <p class="text-secondary mb-0">Professional exam lifecycle</p>
                            </div>

                            <span class="badge badge-soft-secondary">Process</span>
                        </div>

                        <div class="timeline">
                            <div class="timeline-item">
                                <div class="activity-title">Draft</div>
                                <small class="text-secondary">Create exam details and prepare the assessment.</small>
                            </div>

                            <div class="timeline-item">
                                <div class="activity-title">Scheduled</div>
                                <small class="text-secondary">Exam appears in the student portal as available.</small>
                            </div>

                            <div class="timeline-item">
                                <div class="activity-title">Active / Ongoing</div>
                                <small class="text-secondary">Students can enter the secure online exam console.</small>
                            </div>

                            <div class="timeline-item">
                                <div class="activity-title">Completed</div>
                                <small class="text-secondary">Exam is closed and ready for marking/review.</small>
                            </div>

                            <div class="timeline-item">
                                <div class="activity-title">Published</div>
                                <small class="text-secondary">Result workflow is finalized for students.</small>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="page-header">
                <div>
                    <h2 class="page-title">Exam Records</h2>
                    <p class="page-description">
                        Search, filter, update, change lifecycle status, and manage exam question banks.
                    </p>
                </div>

                <button class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#examModal">
                    <i class="bi bi-plus-lg me-2"></i>
                    Add Exam
                </button>
            </div>

            <div class="app-card crud-card p-4">
                <div class="crud-toolbar">
                    <div class="input-group search-control">
                        <span class="input-group-text">
                            <i class="bi bi-search"></i>
                        </span>

                        <input type="search"
                               id="examSearch"
                               class="form-control"
                               placeholder="Search by exam ID, subject, date, duration, marks, or status">
                    </div>

                    <div class="d-flex gap-2 flex-wrap">
                        <select class="form-select" id="examStatusFilter" style="width:190px;">
                            <option value="">All Status</option>
                            <option value="draft">Draft</option>
                            <option value="scheduled">Scheduled</option>
                            <option value="active">Active</option>
                            <option value="ongoing">Ongoing</option>
                            <option value="completed">Completed</option>
                            <option value="published">Published</option>
                            <option value="cancelled">Cancelled</option>
                            <option value="inactive">Inactive</option>
                        </select>

                        <button class="btn btn-outline-secondary" type="button" id="clearExamFiltersBtn">
                            <i class="bi bi-x-circle me-1"></i>
                            Clear
                        </button>

                        <a href="<%= request.getContextPath() %>/questions" class="btn btn-outline-primary">
                            <i class="bi bi-patch-question me-1"></i>
                            Manage Questions
                        </a>
                    </div>
                </div>

                <div class="table-responsive">
                    <table class="table table-hover align-middle" id="examTable">
                        <thead>
                        <tr>
                            <th>Exam ID</th>
                            <th>Subject</th>
                            <th>Date</th>
                            <th>Duration</th>
                            <th>Total Marks</th>
                            <th>Status</th>
                            <th>Student Access</th>
                            <th>Lifecycle</th>
                            <th class="text-end">Actions</th>
                        </tr>
                        </thead>

                        <tbody>
                        <% if (exams != null && !exams.isEmpty()) {
                            for (Exam exam : exams) {
                                String examId = exam.getExamId();
                                String statusClass = exam.getDisplayStatusClass();
                                String accessLabel = exam.getAccessLabel();
                                String accessClass = exam.getAccessBadgeClass();
                        %>
                            <tr data-status="<%= FileUtil.h(exam.getStatus().toLowerCase()) %>">
                                <td class="fw-bold"><%= FileUtil.h(examId) %></td>

                                <td>
                                    <div class="fw-bold"><%= FileUtil.h(exam.getSubject()) %></div>
                                    <small class="text-muted"><%= FileUtil.h(exam.getLifecycleHint()) %></small>
                                </td>

                                <td><%= FileUtil.h(exam.getDisplayExamDate()) %></td>
                                <td><%= FileUtil.h(exam.getDisplayDuration()) %></td>
                                <td><%= FileUtil.h(exam.getDisplayTotalMarks()) %></td>

                                <td>
                                    <span class="badge <%= statusClass %>">
                                        <%= FileUtil.h(exam.getStatus()) %>
                                    </span>
                                </td>

                                <td>
                                    <span class="badge <%= accessClass %>">
                                        <%= FileUtil.h(accessLabel) %>
                                    </span>
                                </td>

                                <td>
                                    <form action="<%= request.getContextPath() %>/exams"
                                          method="post"
                                          class="d-flex gap-2 align-items-center">
                                        <input type="hidden" name="action" value="status">
                                        <input type="hidden" name="examId" value="<%= FileUtil.h(examId) %>">

                                        <select name="status" class="form-select form-select-sm" style="min-width: 130px;">
                                            <option value="Draft" <%= exam.isDraft() ? "selected" : "" %>>Draft</option>
                                            <option value="Scheduled" <%= exam.isScheduled() ? "selected" : "" %>>Scheduled</option>
                                            <option value="Active" <%= exam.isActive() ? "selected" : "" %>>Active</option>
                                            <option value="Ongoing" <%= exam.isOngoing() ? "selected" : "" %>>Ongoing</option>
                                            <option value="Completed" <%= exam.isCompleted() ? "selected" : "" %>>Completed</option>
                                            <option value="Published" <%= exam.isPublished() ? "selected" : "" %>>Published</option>
                                            <option value="Cancelled" <%= exam.isCancelled() ? "selected" : "" %>>Cancelled</option>
                                            <option value="Inactive" <%= exam.isInactive() ? "selected" : "" %>>Inactive</option>
                                        </select>

                                        <button class="btn btn-sm btn-outline-primary" type="submit" title="Update Status">
                                            <i class="bi bi-check2"></i>
                                        </button>
                                    </form>
                                </td>

                                <td>
                                    <div class="action-group">
                                        <button class="btn btn-sm btn-outline-primary"
                                                type="button"
                                                title="View Exam"
                                                data-bs-toggle="modal"
                                                data-bs-target="#viewExamModal"
                                                data-exam-id="<%= FileUtil.h(examId) %>"
                                                data-exam-subject="<%= FileUtil.h(exam.getSubject()) %>"
                                                data-exam-date="<%= FileUtil.h(exam.getExamDate()) %>"
                                                data-exam-display-date="<%= FileUtil.h(exam.getDisplayExamDate()) %>"
                                                data-exam-duration="<%= FileUtil.h(exam.getDuration()) %>"
                                                data-exam-display-duration="<%= FileUtil.h(exam.getDisplayDuration()) %>"
                                                data-exam-marks="<%= FileUtil.h(exam.getTotalMarks()) %>"
                                                data-exam-display-marks="<%= FileUtil.h(exam.getDisplayTotalMarks()) %>"
                                                data-exam-status="<%= FileUtil.h(exam.getStatus()) %>"
                                                data-exam-access="<%= FileUtil.h(accessLabel) %>"
                                                data-exam-hint="<%= FileUtil.h(exam.getLifecycleHint()) %>">
                                            <i class="bi bi-eye"></i>
                                        </button>

                                        <% if (exam.canEditExam()) { %>
                                            <button class="btn btn-sm btn-outline-primary"
                                                    type="button"
                                                    title="Edit Exam"
                                                    data-bs-toggle="modal"
                                                    data-bs-target="#editExamModal"
                                                    data-exam-id="<%= FileUtil.h(examId) %>"
                                                    data-exam-subject="<%= FileUtil.h(exam.getSubject()) %>"
                                                    data-exam-date="<%= FileUtil.h(exam.getExamDate()) %>"
                                                    data-exam-duration="<%= FileUtil.h(exam.getDuration()) %>"
                                                    data-exam-marks="<%= FileUtil.h(exam.getTotalMarks()) %>"
                                                    data-exam-status="<%= FileUtil.h(exam.getStatus()) %>">
                                                <i class="bi bi-pencil-square"></i>
                                            </button>
                                        <% } else { %>
                                            <button class="btn btn-sm btn-outline-secondary"
                                                    type="button"
                                                    disabled
                                                    title="Published exams cannot be edited">
                                                <i class="bi bi-lock-fill"></i>
                                            </button>
                                        <% } %>

                                        <a href="<%= request.getContextPath() %>/questions?examId=<%= FileUtil.h(examId) %>"
                                           class="btn btn-sm btn-outline-primary"
                                           title="Manage Questions">
                                            <i class="bi bi-patch-question"></i>
                                        </a>

                                        <% if (exam.isOngoing() || exam.isCompleted() || exam.isPublished()) { %>
                                            <button class="btn btn-sm btn-outline-secondary"
                                                    type="button"
                                                    disabled
                                                    title="Use Cancelled or Inactive instead of deleting closed exams">
                                                <i class="bi bi-trash3"></i>
                                            </button>
                                        <% } else { %>
                                            <button class="btn btn-sm btn-outline-danger"
                                                    type="button"
                                                    title="Delete Exam"
                                                    data-bs-toggle="modal"
                                                    data-bs-target="#deleteModal"
                                                    data-delete-name="<%= FileUtil.h(examId + " - " + exam.getSubject()) %>"
                                                    data-delete-id="<%= FileUtil.h(examId) %>"
                                                    data-delete-url="<%= request.getContextPath() %>/exams">
                                                <i class="bi bi-trash3"></i>
                                            </button>
                                        <% } %>
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
                                        <h5>No exam records found</h5>
                                        <p>Add an exam to display records here.</p>
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

<!-- Add Exam Modal -->
<div class="modal fade" id="examModal" tabindex="-1" aria-labelledby="examModalTitle" aria-hidden="true">
    <div class="modal-dialog modal-lg modal-dialog-centered">
        <div class="modal-content border-0 shadow-lg">

            <form class="needs-validation"
                  novalidate
                  action="<%= request.getContextPath() %>/exams"
                  method="post">

                <input type="hidden" name="action" value="add">

                <div class="modal-header">
                    <div>
                        <h5 class="modal-title fw-bold" id="examModalTitle">Add Exam</h5>
                        <small class="text-secondary">
                            Schedule a new examination record in NextExamLK.
                        </small>
                    </div>

                    <button class="btn-close" type="button" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>

                <div class="modal-body">
                    <div class="row g-3">
                        <div class="col-md-6">
                            <label class="form-label">Exam ID <span class="required">*</span></label>
                            <input type="text"
                                   name="examId"
                                   class="form-control"
                                   placeholder="Example: EX004"
                                   maxlength="30"
                                   required>
                            <div class="invalid-feedback">Exam ID is required.</div>
                        </div>

                        <div class="col-md-6">
                            <label class="form-label">Subject <span class="required">*</span></label>
                            <input type="text"
                                   name="subject"
                                   class="form-control"
                                   placeholder="Object Oriented Programming"
                                   maxlength="120"
                                   required>
                            <div class="invalid-feedback">Subject is required.</div>
                        </div>

                        <div class="col-md-6">
                            <label class="form-label">Exam Date <span class="required">*</span></label>
                            <input type="date"
                                   name="examDate"
                                   class="form-control"
                                   required>
                            <div class="invalid-feedback">Exam date is required.</div>
                        </div>

                        <div class="col-md-3">
                            <label class="form-label">Duration <span class="required">*</span></label>
                            <input type="number"
                                   name="duration"
                                   class="form-control"
                                   placeholder="120"
                                   min="1"
                                   max="360"
                                   required>
                            <div class="invalid-feedback">Duration must be between 1 and 360 minutes.</div>
                        </div>

                        <div class="col-md-3">
                            <label class="form-label">Total Marks <span class="required">*</span></label>
                            <input type="number"
                                   name="totalMarks"
                                   class="form-control"
                                   placeholder="100"
                                   min="1"
                                   max="1000"
                                   step="0.5"
                                   required>
                            <div class="invalid-feedback">Total marks are required.</div>
                        </div>

                        <div class="col-md-6">
                            <label class="form-label">Status <span class="required">*</span></label>
                            <select name="status" class="form-select" required>
                                <option value="">Choose status</option>
                                <option value="Draft">Draft</option>
                                <option value="Scheduled">Scheduled</option>
                                <option value="Active">Active</option>
                                <option value="Ongoing">Ongoing</option>
                                <option value="Completed">Completed</option>
                                <option value="Published">Published</option>
                                <option value="Cancelled">Cancelled</option>
                                <option value="Inactive">Inactive</option>
                            </select>
                            <div class="invalid-feedback">Status is required.</div>
                        </div>
                    </div>

                    <div class="alert alert-info mt-4 mb-0">
                        <strong>Status tip:</strong>
                        Scheduled, Active, and Ongoing exams are visible in the student exam portal.
                    </div>
                </div>

                <div class="modal-footer">
                    <button class="btn btn-outline-secondary" type="button" data-bs-dismiss="modal">
                        Cancel
                    </button>

                    <button class="btn btn-primary" type="submit">
                        <i class="bi bi-save me-2"></i>
                        Save Exam
                    </button>
                </div>
            </form>

        </div>
    </div>
</div>

<!-- Edit Exam Modal -->
<div class="modal fade" id="editExamModal" tabindex="-1" aria-labelledby="editExamModalTitle" aria-hidden="true">
    <div class="modal-dialog modal-lg modal-dialog-centered">
        <div class="modal-content border-0 shadow-lg">

            <form class="needs-validation"
                  novalidate
                  action="<%= request.getContextPath() %>/exams"
                  method="post">

                <input type="hidden" name="action" value="update">

                <div class="modal-header">
                    <div>
                        <h5 class="modal-title fw-bold" id="editExamModalTitle">Edit Exam</h5>
                        <small class="text-secondary">
                            Update an existing examination schedule in NextExamLK.
                        </small>
                    </div>

                    <button class="btn-close" type="button" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>

                <div class="modal-body">
                    <div class="row g-3">
                        <div class="col-md-6">
                            <label class="form-label">Exam ID</label>
                            <input type="text"
                                   id="editExamId"
                                   name="examId"
                                   class="form-control"
                                   readonly
                                   required>
                            <div class="invalid-feedback">Exam ID is required.</div>
                        </div>

                        <div class="col-md-6">
                            <label class="form-label">Subject</label>
                            <input type="text"
                                   id="editExamSubject"
                                   name="subject"
                                   class="form-control"
                                   maxlength="120"
                                   required>
                            <div class="invalid-feedback">Subject is required.</div>
                        </div>

                        <div class="col-md-6">
                            <label class="form-label">Exam Date</label>
                            <input type="date"
                                   id="editExamDate"
                                   name="examDate"
                                   class="form-control"
                                   required>
                            <div class="invalid-feedback">Exam date is required.</div>
                        </div>

                        <div class="col-md-3">
                            <label class="form-label">Duration</label>
                            <input type="number"
                                   id="editExamDuration"
                                   name="duration"
                                   class="form-control"
                                   min="1"
                                   max="360"
                                   required>
                            <div class="invalid-feedback">Duration must be between 1 and 360 minutes.</div>
                        </div>

                        <div class="col-md-3">
                            <label class="form-label">Total Marks</label>
                            <input type="number"
                                   id="editExamMarks"
                                   name="totalMarks"
                                   class="form-control"
                                   min="1"
                                   max="1000"
                                   step="0.5"
                                   required>
                            <div class="invalid-feedback">Total marks are required.</div>
                        </div>

                        <div class="col-md-6">
                            <label class="form-label">Status</label>
                            <select id="editExamStatus" name="status" class="form-select" required>
                                <option value="">Choose status</option>
                                <option value="Draft">Draft</option>
                                <option value="Scheduled">Scheduled</option>
                                <option value="Active">Active</option>
                                <option value="Ongoing">Ongoing</option>
                                <option value="Completed">Completed</option>
                                <option value="Published">Published</option>
                                <option value="Cancelled">Cancelled</option>
                                <option value="Inactive">Inactive</option>
                            </select>
                            <div class="invalid-feedback">Status is required.</div>
                        </div>
                    </div>

                    <div class="alert alert-info mt-4 mb-0">
                        <strong>Student access rule:</strong>
                        Scheduled, Active, and Ongoing exams are treated as attemptable by the student portal.
                    </div>
                </div>

                <div class="modal-footer">
                    <button class="btn btn-outline-secondary" type="button" data-bs-dismiss="modal">
                        Cancel
                    </button>

                    <button class="btn btn-primary" type="submit">
                        <i class="bi bi-save me-2"></i>
                        Update Exam
                    </button>
                </div>
            </form>

        </div>
    </div>
</div>

<!-- View Exam Modal -->
<div class="modal fade" id="viewExamModal" tabindex="-1" aria-labelledby="viewExamModalTitle" aria-hidden="true">
    <div class="modal-dialog modal-lg modal-dialog-centered">
        <div class="modal-content border-0 shadow-lg">

            <div class="modal-header">
                <div>
                    <h5 class="modal-title fw-bold" id="viewExamModalTitle">Exam Details</h5>
                    <small class="text-secondary">
                        View selected examination schedule details.
                    </small>
                </div>

                <button class="btn-close" type="button" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>

            <div class="modal-body">
                <div class="row g-3">
                    <div class="col-md-6">
                        <div class="exam-info-box">
                            <small>Exam ID</small>
                            <strong id="viewExamId">-</strong>
                        </div>
                    </div>

                    <div class="col-md-6">
                        <div class="exam-info-box">
                            <small>Subject</small>
                            <strong id="viewExamSubject">-</strong>
                        </div>
                    </div>

                    <div class="col-md-6">
                        <div class="exam-info-box">
                            <small>Exam Date</small>
                            <strong id="viewExamDate">-</strong>
                        </div>
                    </div>

                    <div class="col-md-3">
                        <div class="exam-info-box">
                            <small>Duration</small>
                            <strong id="viewExamDuration">-</strong>
                        </div>
                    </div>

                    <div class="col-md-3">
                        <div class="exam-info-box">
                            <small>Total Marks</small>
                            <strong id="viewExamMarks">-</strong>
                        </div>
                    </div>

                    <div class="col-md-6">
                        <div class="exam-info-box">
                            <small>Status</small>
                            <strong id="viewExamStatus">-</strong>
                        </div>
                    </div>

                    <div class="col-md-6">
                        <div class="exam-info-box">
                            <small>Student Access</small>
                            <strong id="viewExamAccess">-</strong>
                        </div>
                    </div>
                </div>

                <div class="alert alert-info mt-4 mb-0">
                    <strong>Lifecycle note:</strong>
                    <span id="viewExamHint">Students can access exams when the status is Scheduled, Active, or Ongoing.</span>
                </div>
            </div>

            <div class="modal-footer">
                <button class="btn btn-outline-secondary" type="button" data-bs-dismiss="modal">
                    Close
                </button>

                <a href="<%= request.getContextPath() %>/questions" class="btn btn-primary">
                    <i class="bi bi-patch-question me-2"></i>
                    Open Question Bank
                </a>
            </div>

        </div>
    </div>
</div>

<script>
    document.addEventListener("DOMContentLoaded", function () {
        const editExamModal = document.getElementById("editExamModal");
        const viewExamModal = document.getElementById("viewExamModal");
        const examSearch = document.getElementById("examSearch");
        const examStatusFilter = document.getElementById("examStatusFilter");
        const clearExamFiltersBtn = document.getElementById("clearExamFiltersBtn");
        const examRows = document.querySelectorAll("#examTable tbody tr[data-status]");

        function getExamData(button) {
            return {
                id: button.getAttribute("data-exam-id") || "",
                subject: button.getAttribute("data-exam-subject") || "",
                date: button.getAttribute("data-exam-date") || "",
                displayDate: button.getAttribute("data-exam-display-date") || "",
                duration: button.getAttribute("data-exam-duration") || "",
                displayDuration: button.getAttribute("data-exam-display-duration") || "",
                marks: button.getAttribute("data-exam-marks") || "",
                displayMarks: button.getAttribute("data-exam-display-marks") || "",
                status: button.getAttribute("data-exam-status") || "",
                access: button.getAttribute("data-exam-access") || "",
                hint: button.getAttribute("data-exam-hint") || ""
            };
        }

        function filterExams() {
            const searchValue = examSearch ? examSearch.value.toLowerCase().trim() : "";
            const statusValue = examStatusFilter ? examStatusFilter.value.toLowerCase().trim() : "";

            examRows.forEach(function (row) {
                const rowText = row.innerText.toLowerCase();
                const rowStatus = row.getAttribute("data-status") || "";

                const matchesSearch = rowText.includes(searchValue);
                const matchesStatus = statusValue === "" || rowStatus === statusValue;

                row.style.display = matchesSearch && matchesStatus ? "" : "none";
            });
        }

        if (examSearch) {
            examSearch.addEventListener("input", filterExams);
        }

        if (examStatusFilter) {
            examStatusFilter.addEventListener("change", filterExams);
        }

        if (clearExamFiltersBtn) {
            clearExamFiltersBtn.addEventListener("click", function () {
                if (examSearch) {
                    examSearch.value = "";
                }

                if (examStatusFilter) {
                    examStatusFilter.value = "";
                }

                filterExams();
            });
        }

        if (editExamModal) {
            editExamModal.addEventListener("show.bs.modal", function (event) {
                const button = event.relatedTarget;

                if (!button) {
                    return;
                }

                const exam = getExamData(button);

                document.getElementById("editExamId").value = exam.id;
                document.getElementById("editExamSubject").value = exam.subject;
                document.getElementById("editExamDate").value = exam.date;
                document.getElementById("editExamDuration").value = exam.duration;
                document.getElementById("editExamMarks").value = exam.marks;
                document.getElementById("editExamStatus").value = exam.status;
            });
        }

        if (viewExamModal) {
            viewExamModal.addEventListener("show.bs.modal", function (event) {
                const button = event.relatedTarget;

                if (!button) {
                    return;
                }

                const exam = getExamData(button);

                document.getElementById("viewExamId").textContent = exam.id || "-";
                document.getElementById("viewExamSubject").textContent = exam.subject || "-";
                document.getElementById("viewExamDate").textContent = exam.displayDate || exam.date || "-";
                document.getElementById("viewExamDuration").textContent = exam.displayDuration || exam.duration || "-";
                document.getElementById("viewExamMarks").textContent = exam.displayMarks || exam.marks || "-";
                document.getElementById("viewExamStatus").textContent = exam.status || "-";
                document.getElementById("viewExamAccess").textContent = exam.access || "-";
                document.getElementById("viewExamHint").textContent = exam.hint || "Students can access exams when the status is Scheduled, Active, or Ongoing.";
            });
        }
    });
</script>

<%@ include file="../includes/delete-modal.jsp" %>
<%@ include file="../includes/footer.jsp" %>