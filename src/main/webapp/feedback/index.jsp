<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="lk.nextexam.dao.FileUtil" %>
<%@ page import="lk.nextexam.dao.FeedbackDAO" %>
<%@ page import="lk.nextexam.model.Feedback" %>

<%
    String pageTitle = "Feedback";
    String activeMenu = "feedback";
    String topbarTitle = "Feedback";

    String currentUserRole = request.getAttribute("currentUserRole") != null
            ? request.getAttribute("currentUserRole").toString()
            : "";

    String currentUserId = request.getAttribute("currentUserId") != null
            ? request.getAttribute("currentUserId").toString()
            : "";

    String currentUsername = request.getAttribute("currentUsername") != null
            ? request.getAttribute("currentUsername").toString()
            : "User";

    if (currentUserRole.isEmpty() && session != null && session.getAttribute("userRole") != null) {
        currentUserRole = session.getAttribute("userRole").toString();
    }

    if (currentUserId.isEmpty() && session != null && session.getAttribute("userId") != null) {
        currentUserId = session.getAttribute("userId").toString();
    }

    if ((currentUsername == null || currentUsername.trim().isEmpty() || "User".equals(currentUsername))
            && session != null && session.getAttribute("username") != null) {
        currentUsername = session.getAttribute("username").toString();
    }

    boolean canManageFeedback = request.getAttribute("canManageFeedback") != null
            && (Boolean) request.getAttribute("canManageFeedback");

    List<Feedback> feedbackList = (List<Feedback>) request.getAttribute("feedbackList");

    if (feedbackList == null) {
        FeedbackDAO feedbackDAO = new FeedbackDAO();

        if ("Admin".equalsIgnoreCase(currentUserRole) || "Lecturer".equalsIgnoreCase(currentUserRole)) {
            feedbackList = feedbackDAO.getAllFeedback(application);
            canManageFeedback = true;
        } else {
            feedbackList = feedbackDAO.getFeedbackByStudentId(application, currentUserId);
        }
    }

    int totalFeedback = feedbackList != null ? feedbackList.size() : 0;
    int newFeedbackCount = 0;
    int inReviewFeedbackCount = 0;
    int resolvedFeedbackCount = 0;
    int closedFeedbackCount = 0;
    int openFeedbackCount = 0;
    int completedFeedbackCount = 0;
    int todayFeedbackCount = 0;

    int examFeedbackCount = 0;
    int resultFeedbackCount = 0;
    int technicalFeedbackCount = 0;
    int accountFeedbackCount = 0;
    int generalFeedbackCount = 0;

    if (feedbackList != null) {
        for (Feedback feedback : feedbackList) {
            if (feedback.isNew()) {
                newFeedbackCount++;
            }

            if (feedback.isInReview()) {
                inReviewFeedbackCount++;
            }

            if (feedback.isResolved()) {
                resolvedFeedbackCount++;
            }

            if (feedback.isClosed()) {
                closedFeedbackCount++;
            }

            if (feedback.isOpen()) {
                openFeedbackCount++;
            }

            if (feedback.isCompleted()) {
                completedFeedbackCount++;
            }

            if (feedback.isToday()) {
                todayFeedbackCount++;
            }

            if (feedback.isExamCategory()) {
                examFeedbackCount++;
            } else if (feedback.isResultCategory()) {
                resultFeedbackCount++;
            } else if (feedback.isTechnicalCategory()) {
                technicalFeedbackCount++;
            } else if (feedback.isAccountCategory()) {
                accountFeedbackCount++;
            } else if (feedback.isGeneralCategory()) {
                generalFeedbackCount++;
            }
        }
    }

    int examPercentage = totalFeedback > 0 ? (examFeedbackCount * 100) / totalFeedback : 0;
    int resultPercentage = totalFeedback > 0 ? (resultFeedbackCount * 100) / totalFeedback : 0;
    int technicalPercentage = totalFeedback > 0 ? (technicalFeedbackCount * 100) / totalFeedback : 0;
    int accountPercentage = totalFeedback > 0 ? (accountFeedbackCount * 100) / totalFeedback : 0;
    int generalPercentage = totalFeedback > 0 ? (generalFeedbackCount * 100) / totalFeedback : 0;
    int completionPercentage = totalFeedback > 0 ? (completedFeedbackCount * 100) / totalFeedback : 0;

    String success = request.getParameter("success");
    String error = request.getParameter("error");

    String alertType = "";
    String alertMessage = "";

    if (success != null) {
        alertType = "success";

        if ("feedbackAdded".equalsIgnoreCase(success)) {
            alertMessage = "Feedback submitted successfully.";
        } else if ("feedbackUpdated".equalsIgnoreCase(success)) {
            alertMessage = "Feedback updated successfully.";
        } else if ("feedbackDeleted".equalsIgnoreCase(success)) {
            alertMessage = "Feedback deleted successfully.";
        } else if ("feedbackStatusUpdated".equalsIgnoreCase(success)) {
            alertMessage = "Feedback status updated successfully.";
        } else {
            alertMessage = "Operation completed successfully.";
        }
    }

    if (error != null) {
        alertType = "danger";

        if ("accessDenied".equalsIgnoreCase(error)) {
            alertMessage = "You do not have permission to perform this feedback action.";
        } else if ("missingFeedbackId".equalsIgnoreCase(error)) {
            alertMessage = "Feedback ID is missing.";
        } else if ("missingStudentId".equalsIgnoreCase(error)) {
            alertMessage = "Student ID is missing.";
        } else if ("missingCategory".equalsIgnoreCase(error)) {
            alertMessage = "Feedback category is required.";
        } else if ("invalidCategory".equalsIgnoreCase(error)) {
            alertMessage = "Invalid feedback category selected.";
        } else if ("missingMessage".equalsIgnoreCase(error)) {
            alertMessage = "Feedback message is required.";
        } else if ("messageTooLong".equalsIgnoreCase(error)) {
            alertMessage = "Feedback message is too long.";
        } else if ("missingDate".equalsIgnoreCase(error)) {
            alertMessage = "Feedback date is required.";
        } else if ("invalidDate".equalsIgnoreCase(error)) {
            alertMessage = "Invalid feedback date.";
        } else if ("missingStatus".equalsIgnoreCase(error)) {
            alertMessage = "Feedback status is required.";
        } else if ("invalidStatus".equalsIgnoreCase(error)) {
            alertMessage = "Invalid feedback status selected.";
        } else if ("feedbackAddFailed".equalsIgnoreCase(error)) {
            alertMessage = "Feedback could not be submitted. Check duplicate ID or incomplete fields.";
        } else if ("feedbackUpdateFailed".equalsIgnoreCase(error)) {
            alertMessage = "Feedback could not be updated.";
        } else if ("feedbackDeleteFailed".equalsIgnoreCase(error)) {
            alertMessage = "Feedback could not be deleted. Resolved or closed feedback is protected.";
        } else if ("feedbackStatusUpdateFailed".equalsIgnoreCase(error)) {
            alertMessage = "Feedback status could not be updated.";
        } else if ("feedbackNotFound".equalsIgnoreCase(error)) {
            alertMessage = "The selected feedback record could not be found.";
        } else {
            alertMessage = "Something went wrong. Please check the feedback details and try again.";
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
                            <i class="bi bi-chat-dots-fill me-1"></i>
                            NextExamLK Feedback Center
                        </span>

                        <% if (canManageFeedback) { %>
                            <h1 class="hero-title">Feedback Management</h1>
                            <p class="hero-text">
                                Review student feedback, track issue categories, update response status,
                                and manage exam platform improvement requests.
                            </p>
                        <% } else { %>
                            <h1 class="hero-title">My Feedback</h1>
                            <p class="hero-text">
                                Submit feedback about exams, results, technical issues, account access,
                                or general platform experience.
                            </p>
                        <% } %>
                    </div>

                    <div class="d-flex gap-2 flex-wrap">
                        <button class="btn btn-primary"
                                type="button"
                                data-bs-toggle="modal"
                                data-bs-target="#feedbackModal">
                            <i class="bi bi-plus-lg me-2"></i>
                            Submit Feedback
                        </button>

                        <% if ("Student".equalsIgnoreCase(currentUserRole)) { %>
                            <a href="<%= request.getContextPath() %>/my-exams" class="btn btn-outline-primary">
                                <i class="bi bi-laptop-fill me-2"></i>
                                My Exams
                            </a>
                        <% } else { %>
                            <a href="<%= request.getContextPath() %>/dashboard.jsp" class="btn btn-outline-primary">
                                <i class="bi bi-grid-1x2-fill me-2"></i>
                                Dashboard
                            </a>
                        <% } %>
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
                                <div class="stat-label"><%= canManageFeedback ? "Total Feedback" : "My Feedback" %></div>
                                <div class="stat-value"><%= totalFeedback %></div>
                                <div class="stat-meta"><%= canManageFeedback ? "All submitted records" : "Submitted by you" %></div>
                            </div>

                            <div class="stat-icon">
                                <i class="bi bi-chat-square-text-fill"></i>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-md-6 col-xl-3">
                    <div class="app-card stat-card">
                        <div class="d-flex justify-content-between gap-3">
                            <div>
                                <div class="stat-label">Open</div>
                                <div class="stat-value"><%= openFeedbackCount %></div>
                                <div class="stat-meta">New or in review</div>
                            </div>

                            <div class="stat-icon">
                                <i class="bi bi-envelope-exclamation-fill"></i>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-md-6 col-xl-3">
                    <div class="app-card stat-card">
                        <div class="d-flex justify-content-between gap-3">
                            <div>
                                <div class="stat-label">Resolved / Closed</div>
                                <div class="stat-value"><%= resolvedFeedbackCount %>/<%= closedFeedbackCount %></div>
                                <div class="stat-meta">Completed feedback</div>
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
                                <div class="stat-label">Today</div>
                                <div class="stat-value"><%= todayFeedbackCount %></div>
                                <div class="stat-meta">Submitted today</div>
                            </div>

                            <div class="stat-icon">
                                <i class="bi bi-calendar-day-fill"></i>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row g-4 mb-4">
                <div class="col-xl-4">
                    <div class="app-card form-card h-100 p-4">
                        <div class="d-flex justify-content-between align-items-start gap-3 mb-3">
                            <div>
                                <h4 class="fw-bold mb-1">Submit Feedback</h4>
                                <p class="text-secondary mb-0">
                                    Share your issue, suggestion, or platform experience.
                                </p>
                            </div>

                            <span class="badge badge-soft-primary">
                                <%= FileUtil.h(currentUserRole.isEmpty() ? "User" : currentUserRole) %>
                            </span>
                        </div>

                        <form class="needs-validation"
                              novalidate
                              action="<%= request.getContextPath() %>/feedback"
                              method="post">

                            <input type="hidden" name="action" value="add">
                            <input type="hidden" name="status" value="New">

                            <% if (!canManageFeedback) { %>
                                <input type="hidden" name="studentId" value="<%= FileUtil.h(currentUserId) %>">
                            <% } %>

                            <% if (canManageFeedback) { %>
                                <div class="mb-3">
                                    <label class="form-label">Feedback ID <span class="required">*</span></label>
                                    <input type="text"
                                           name="feedbackId"
                                           class="form-control"
                                           placeholder="Example: FB001"
                                           maxlength="30"
                                           required>
                                    <div class="invalid-feedback">Feedback ID is required.</div>
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
                            <% } %>

                            <div class="mb-3">
                                <label class="form-label">Category <span class="required">*</span></label>
                                <select name="category" class="form-select" required>
                                    <option value="">Choose category</option>
                                    <option value="Exam">Exam</option>
                                    <option value="Result">Result</option>
                                    <option value="Technical">Technical</option>
                                    <option value="Account">Account</option>
                                    <option value="General">General</option>
                                </select>
                                <div class="invalid-feedback">Category is required.</div>
                            </div>

                            <div class="mb-3">
                                <label class="form-label">Message <span class="required">*</span></label>
                                <textarea name="message"
                                          class="form-control"
                                          rows="5"
                                          maxlength="1200"
                                          data-character-counter="feedbackCounter"
                                          placeholder="Write your feedback clearly..."
                                          required></textarea>
                                <div class="invalid-feedback">Feedback message is required.</div>
                                <small class="text-secondary" id="feedbackCounter">0 / 1200 characters</small>
                            </div>

                            <button class="btn btn-primary w-100" type="submit">
                                <i class="bi bi-send-fill me-2"></i>
                                Submit Feedback
                            </button>
                        </form>
                    </div>
                </div>

                <div class="col-xl-8">
                    <div class="app-card p-4 h-100 feedback-insights-panel">
                        <div class="d-flex justify-content-between align-items-start flex-wrap gap-3 mb-4">
                            <div>
                                <span class="badge badge-soft-primary mb-2">
                                    <% if (canManageFeedback) { %>
                                        <i class="bi bi-clipboard-data-fill me-1"></i>
                                        Staff Review Dashboard
                                    <% } else { %>
                                        <i class="bi bi-person-check-fill me-1"></i>
                                        Personal Feedback Tracker
                                    <% } %>
                                </span>

                                <h4 class="fw-bold mb-1">
                                    <%= canManageFeedback ? "Review Insights" : "My Feedback Progress" %>
                                </h4>

                                <p class="text-secondary mb-0">
                                    <%= canManageFeedback
                                            ? "Monitor issue categories, open requests, review progress, and priority signals from students."
                                            : "Track your submitted feedback, review progress, and current response status in one place." %>
                                </p>
                            </div>

                            <div class="feedback-completion-ring">
                                <div class="feedback-ring-circle" style="--progress:<%= completionPercentage %>;">
                                    <span><%= completionPercentage %>%</span>
                                </div>
                                <small>Completed</small>
                            </div>
                        </div>

                        <div class="feedback-status-strip mb-4">
                            <div class="feedback-status-item">
                                <div class="feedback-status-icon feedback-status-new">
                                    <i class="bi bi-envelope-fill"></i>
                                </div>
                                <div>
                                    <small>New</small>
                                    <strong><%= newFeedbackCount %></strong>
                                </div>
                            </div>

                            <div class="feedback-status-item">
                                <div class="feedback-status-icon feedback-status-review">
                                    <i class="bi bi-hourglass-split"></i>
                                </div>
                                <div>
                                    <small>In Review</small>
                                    <strong><%= inReviewFeedbackCount %></strong>
                                </div>
                            </div>

                            <div class="feedback-status-item">
                                <div class="feedback-status-icon feedback-status-resolved">
                                    <i class="bi bi-check-circle-fill"></i>
                                </div>
                                <div>
                                    <small>Resolved</small>
                                    <strong><%= resolvedFeedbackCount %></strong>
                                </div>
                            </div>

                            <div class="feedback-status-item">
                                <div class="feedback-status-icon feedback-status-closed">
                                    <i class="bi bi-archive-fill"></i>
                                </div>
                                <div>
                                    <small>Closed</small>
                                    <strong><%= closedFeedbackCount %></strong>
                                </div>
                            </div>
                        </div>

                        <% if (canManageFeedback) { %>
                            <div class="feedback-admin-highlight mb-4">
                                <div class="feedback-admin-icon">
                                    <i class="bi bi-shield-check"></i>
                                </div>

                                <div class="flex-grow-1">
                                    <div class="d-flex justify-content-between align-items-start gap-3 flex-wrap">
                                        <div>
                                            <h5 class="fw-bold mb-1">Review Queue Health</h5>
                                            <p class="mb-0">
                                                You currently have <strong><%= openFeedbackCount %></strong> open feedback records.
                                                Technical feedback is treated as high priority.
                                            </p>
                                        </div>

                                        <span class="badge <%= openFeedbackCount > 0 ? "badge-soft-warning" : "badge-soft-success" %>">
                                            <%= openFeedbackCount > 0 ? "Action Needed" : "Queue Clear" %>
                                        </span>
                                    </div>
                                </div>
                            </div>
                        <% } else { %>
                            <div class="feedback-student-highlight mb-4">
                                <div class="feedback-student-icon">
                                    <i class="bi bi-chat-heart-fill"></i>
                                </div>

                                <div class="flex-grow-1">
                                    <div class="d-flex justify-content-between align-items-start gap-3 flex-wrap">
                                        <div>
                                            <h5 class="fw-bold mb-1">Your Feedback Journey</h5>
                                            <p class="mb-0">
                                                You have submitted <strong><%= totalFeedback %></strong> feedback record(s).
                                                <strong><%= openFeedbackCount %></strong> are still being reviewed.
                                            </p>
                                        </div>

                                        <span class="badge <%= openFeedbackCount > 0 ? "badge-soft-primary" : "badge-soft-success" %>">
                                            <%= openFeedbackCount > 0 ? "In Progress" : "All Completed" %>
                                        </span>
                                    </div>
                                </div>
                            </div>
                        <% } %>

                        <div class="row g-3">
                            <div class="col-md-6">
                                <div class="feedback-insight-card h-100">
                                    <div class="feedback-insight-header">
                                        <div class="feedback-insight-icon feedback-insight-exam">
                                            <i class="bi bi-laptop-fill"></i>
                                        </div>
                                        <div>
                                            <h6>Exam</h6>
                                            <small>Access, attempt, timing, or exam flow</small>
                                        </div>
                                        <strong><%= examFeedbackCount %></strong>
                                    </div>

                                    <div class="feedback-progress-track">
                                        <div class="feedback-progress-fill" style="width:<%= examPercentage %>%;"></div>
                                    </div>
                                </div>
                            </div>

                            <div class="col-md-6">
                                <div class="feedback-insight-card h-100">
                                    <div class="feedback-insight-header">
                                        <div class="feedback-insight-icon feedback-insight-result">
                                            <i class="bi bi-bar-chart-fill"></i>
                                        </div>
                                        <div>
                                            <h6>Result</h6>
                                            <small>Marks, grading, publishing, or visibility</small>
                                        </div>
                                        <strong><%= resultFeedbackCount %></strong>
                                    </div>

                                    <div class="feedback-progress-track">
                                        <div class="feedback-progress-fill success" style="width:<%= resultPercentage %>%;"></div>
                                    </div>
                                </div>
                            </div>

                            <div class="col-md-6">
                                <div class="feedback-insight-card h-100">
                                    <div class="feedback-insight-header">
                                        <div class="feedback-insight-icon feedback-insight-technical">
                                            <i class="bi bi-tools"></i>
                                        </div>
                                        <div>
                                            <h6>Technical</h6>
                                            <small>Browser, loading, submission, or errors</small>
                                        </div>
                                        <strong><%= technicalFeedbackCount %></strong>
                                    </div>

                                    <div class="feedback-progress-track">
                                        <div class="feedback-progress-fill danger" style="width:<%= technicalPercentage %>%;"></div>
                                    </div>
                                </div>
                            </div>

                            <div class="col-md-6">
                                <div class="feedback-insight-card h-100">
                                    <div class="feedback-insight-header">
                                        <div class="feedback-insight-icon feedback-insight-account">
                                            <i class="bi bi-person-gear"></i>
                                        </div>
                                        <div>
                                            <h6>Account</h6>
                                            <small>Login, profile, access, or role issues</small>
                                        </div>
                                        <strong><%= accountFeedbackCount %></strong>
                                    </div>

                                    <div class="feedback-progress-track">
                                        <div class="feedback-progress-fill warning" style="width:<%= accountPercentage %>%;"></div>
                                    </div>
                                </div>
                            </div>

                            <div class="col-md-6">
                                <div class="feedback-insight-card h-100">
                                    <div class="feedback-insight-header">
                                        <div class="feedback-insight-icon feedback-insight-general">
                                            <i class="bi bi-chat-left-text-fill"></i>
                                        </div>
                                        <div>
                                            <h6>General</h6>
                                            <small>Suggestions and general comments</small>
                                        </div>
                                        <strong><%= generalFeedbackCount %></strong>
                                    </div>

                                    <div class="feedback-progress-track">
                                        <div class="feedback-progress-fill muted" style="width:<%= generalPercentage %>%;"></div>
                                    </div>
                                </div>
                            </div>

                            <div class="col-md-6">
                                <div class="feedback-insight-card h-100 feedback-completion-card">
                                    <div class="feedback-insight-header">
                                        <div class="feedback-insight-icon feedback-insight-complete">
                                            <i class="bi bi-patch-check-fill"></i>
                                        </div>
                                        <div>
                                            <h6>Completion</h6>
                                            <small>Resolved or closed feedback records</small>
                                        </div>
                                        <strong><%= completionPercentage %>%</strong>
                                    </div>

                                    <div class="feedback-progress-track">
                                        <div class="feedback-progress-fill success" style="width:<%= completionPercentage %>%;"></div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="feedback-insights-footer mt-4">
                            <div>
                                <strong>Feedback workflow:</strong>
                                New feedback is reviewed by staff, moved to In Review, then marked as Resolved or Closed.
                            </div>

                            <span>
                                <i class="bi bi-shield-check me-1"></i>
                                Role-safe feedback view enabled
                            </span>
                        </div>
                    </div>
                </div>
            </div>

            <div class="page-header">
                <div>
                    <h2 class="page-title"><%= canManageFeedback ? "Feedback Records" : "My Feedback History" %></h2>
                    <p class="page-description">
                        <%= canManageFeedback
                                ? "Search, review, update status, and manage feedback records."
                                : "View your submitted feedback and current review progress." %>
                    </p>
                </div>

                <button class="btn btn-primary"
                        type="button"
                        data-bs-toggle="modal"
                        data-bs-target="#feedbackModal">
                    <i class="bi bi-plus-lg me-2"></i>
                    Submit Feedback
                </button>
            </div>

            <div class="app-card crud-card p-4">
                <div class="crud-toolbar">
                    <div class="input-group search-control">
                        <span class="input-group-text">
                            <i class="bi bi-search"></i>
                        </span>

                        <input type="search"
                               class="form-control"
                               id="feedbackSearch"
                               placeholder="Search by feedback ID, student ID, category, message, date, or status">
                    </div>

                    <div class="d-flex gap-2 flex-wrap">
                        <select class="form-select" id="feedbackCategoryFilter" style="width: 170px;">
                            <option value="">All Categories</option>
                            <option value="exam">Exam</option>
                            <option value="result">Result</option>
                            <option value="technical">Technical</option>
                            <option value="account">Account</option>
                            <option value="general">General</option>
                        </select>

                        <select class="form-select" id="feedbackStatusFilter" style="width: 170px;">
                            <option value="">All Status</option>
                            <option value="new">New</option>
                            <option value="in review">In Review</option>
                            <option value="resolved">Resolved</option>
                            <option value="closed">Closed</option>
                        </select>

                        <button class="btn btn-outline-secondary" type="button" id="clearFeedbackFiltersBtn">
                            <i class="bi bi-x-circle me-1"></i>
                            Clear
                        </button>
                    </div>
                </div>

                <div class="table-responsive">
                    <table class="table table-hover align-middle" id="feedbackTable">
                        <thead>
                        <tr>
                            <th>Feedback ID</th>
                            <th>Student ID</th>
                            <th>Category</th>
                            <th>Message</th>
                            <th>Date</th>
                            <th>Priority</th>
                            <th>Status</th>
                            <% if (canManageFeedback) { %>
                                <th>Review</th>
                            <% } %>
                            <th class="text-end">Actions</th>
                        </tr>
                        </thead>

                        <tbody>
                        <% if (feedbackList != null && !feedbackList.isEmpty()) {
                            for (Feedback feedback : feedbackList) {
                                boolean studentCanEdit = feedback.canEditByStudent(currentUserId);
                                boolean studentCanDelete = feedback.canDeleteByStudent(currentUserId);
                        %>
                            <tr data-category="<%= FileUtil.h(feedback.getCategory().toLowerCase()) %>"
                                data-status="<%= FileUtil.h(feedback.getStatus().toLowerCase()) %>">
                                <td class="fw-bold"><%= FileUtil.h(feedback.getFeedbackId()) %></td>
                                <td><%= FileUtil.h(feedback.getStudentId()) %></td>

                                <td>
                                    <span class="badge <%= feedback.getCategoryBadgeClass() %>">
                                        <%= FileUtil.h(feedback.getCategory()) %>
                                    </span>
                                </td>

                                <td>
                                    <div class="fw-bold"><%= FileUtil.h(feedback.getShortMessage()) %></div>
                                    <small class="text-secondary"><%= FileUtil.h(feedback.getProgressLabel()) %></small>
                                </td>

                                <td><%= FileUtil.h(feedback.getDisplayDate()) %></td>

                                <td>
                                    <span class="badge <%= feedback.getPriorityBadgeClass() %>">
                                        <%= FileUtil.h(feedback.getPriorityLabel()) %>
                                    </span>
                                </td>

                                <td>
                                    <span class="badge <%= feedback.getStatusBadgeClass() %>">
                                        <%= FileUtil.h(feedback.getStatus()) %>
                                    </span>
                                </td>

                                <% if (canManageFeedback) { %>
                                    <td>
                                        <form action="<%= request.getContextPath() %>/feedback"
                                              method="post"
                                              class="d-flex gap-2 align-items-center">
                                            <input type="hidden" name="action" value="status">
                                            <input type="hidden" name="feedbackId" value="<%= FileUtil.h(feedback.getFeedbackId()) %>">

                                            <select name="status" class="form-select form-select-sm" style="min-width: 130px;">
                                                <option value="New" <%= feedback.isNew() ? "selected" : "" %>>New</option>
                                                <option value="In Review" <%= feedback.isInReview() ? "selected" : "" %>>In Review</option>
                                                <option value="Resolved" <%= feedback.isResolved() ? "selected" : "" %>>Resolved</option>
                                                <option value="Closed" <%= feedback.isClosed() ? "selected" : "" %>>Closed</option>
                                            </select>

                                            <button class="btn btn-sm btn-outline-primary" type="submit" title="Update Status">
                                                <i class="bi bi-check2"></i>
                                            </button>
                                        </form>
                                    </td>
                                <% } %>

                                <td>
                                    <div class="action-group">
                                        <button class="btn btn-sm btn-outline-primary view-feedback-btn"
                                                type="button"
                                                title="View Feedback"
                                                data-bs-toggle="modal"
                                                data-bs-target="#viewFeedbackModal"
                                                data-feedback-id="<%= FileUtil.h(feedback.getFeedbackId()) %>"
                                                data-student-id="<%= FileUtil.h(feedback.getStudentId()) %>"
                                                data-category="<%= FileUtil.h(feedback.getCategory()) %>"
                                                data-message="<%= FileUtil.h(feedback.getMessage()) %>"
                                                data-date="<%= FileUtil.h(feedback.getDate()) %>"
                                                data-display-date="<%= FileUtil.h(feedback.getDisplayDate()) %>"
                                                data-status="<%= FileUtil.h(feedback.getStatus()) %>"
                                                data-priority="<%= FileUtil.h(feedback.getPriorityLabel()) %>"
                                                data-progress="<%= FileUtil.h(feedback.getProgressLabel()) %>">
                                            <i class="bi bi-eye"></i>
                                        </button>

                                        <% if (canManageFeedback || studentCanEdit) { %>
                                            <button class="btn btn-sm btn-outline-primary edit-feedback-btn"
                                                    type="button"
                                                    title="Edit Feedback"
                                                    data-bs-toggle="modal"
                                                    data-bs-target="#editFeedbackModal"
                                                    data-feedback-id="<%= FileUtil.h(feedback.getFeedbackId()) %>"
                                                    data-student-id="<%= FileUtil.h(feedback.getStudentId()) %>"
                                                    data-category="<%= FileUtil.h(feedback.getCategory()) %>"
                                                    data-message="<%= FileUtil.h(feedback.getMessage()) %>"
                                                    data-date="<%= FileUtil.h(feedback.getDate()) %>"
                                                    data-status="<%= FileUtil.h(feedback.getStatus()) %>">
                                                <i class="bi bi-pencil-square"></i>
                                            </button>
                                        <% } else { %>
                                            <button class="btn btn-sm btn-outline-secondary"
                                                    type="button"
                                                    disabled
                                                    title="Only New feedback can be edited by students">
                                                <i class="bi bi-lock-fill"></i>
                                            </button>
                                        <% } %>

                                        <% if (canManageFeedback || studentCanDelete) { %>
                                            <button class="btn btn-sm btn-outline-danger"
                                                    type="button"
                                                    title="Delete Feedback"
                                                    data-bs-toggle="modal"
                                                    data-bs-target="#deleteModal"
                                                    data-delete-name="<%= FileUtil.h(feedback.getFeedbackId() + " - " + feedback.getStudentId()) %>"
                                                    data-delete-id="<%= FileUtil.h(feedback.getFeedbackId()) %>"
                                                    data-delete-url="<%= request.getContextPath() %>/feedback">
                                                <i class="bi bi-trash3"></i>
                                            </button>
                                        <% } else { %>
                                            <button class="btn btn-sm btn-outline-secondary"
                                                    type="button"
                                                    disabled
                                                    title="Resolved or closed feedback is protected">
                                                <i class="bi bi-trash3"></i>
                                            </button>
                                        <% } %>
                                    </div>
                                </td>
                            </tr>
                        <% }
                        } else { %>
                            <tr>
                                <td colspan="<%= canManageFeedback ? "9" : "8" %>">
                                    <div class="empty-state">
                                        <div class="empty-state-icon">
                                            <i class="bi bi-inbox"></i>
                                        </div>
                                        <h5>No feedback records found</h5>
                                        <p>Submit feedback to display records here.</p>
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

<!-- Add Feedback Modal -->
<div class="modal fade" id="feedbackModal" tabindex="-1" aria-labelledby="feedbackModalTitle" aria-hidden="true">
    <div class="modal-dialog modal-lg modal-dialog-centered">
        <div class="modal-content border-0 shadow-lg">

            <form class="needs-validation"
                  novalidate
                  action="<%= request.getContextPath() %>/feedback"
                  method="post">

                <input type="hidden" name="action" value="add">
                <input type="hidden" name="status" value="New">

                <% if (!canManageFeedback) { %>
                    <input type="hidden" name="studentId" value="<%= FileUtil.h(currentUserId) %>">
                <% } %>

                <div class="modal-header">
                    <div>
                        <h5 class="modal-title fw-bold" id="feedbackModalTitle">Submit Feedback</h5>
                        <small class="text-secondary">
                            Submit a feedback message related to exams, results, access, or platform experience.
                        </small>
                    </div>

                    <button class="btn-close" type="button" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>

                <div class="modal-body">
                    <div class="row g-3">
                        <% if (canManageFeedback) { %>
                            <div class="col-md-6">
                                <label class="form-label">Feedback ID <span class="required">*</span></label>
                                <input type="text"
                                       name="feedbackId"
                                       class="form-control"
                                       placeholder="Example: FB001"
                                       maxlength="30"
                                       required>
                                <div class="invalid-feedback">Feedback ID is required.</div>
                            </div>

                            <div class="col-md-6">
                                <label class="form-label">Student ID <span class="required">*</span></label>
                                <input type="text"
                                       name="studentId"
                                       class="form-control"
                                       placeholder="Example: STU001"
                                       maxlength="30"
                                       required>
                                <div class="invalid-feedback">Student ID is required.</div>
                            </div>
                        <% } %>

                        <div class="col-md-6">
                            <label class="form-label">Category <span class="required">*</span></label>
                            <select name="category" class="form-select" required>
                                <option value="">Choose category</option>
                                <option value="Exam">Exam</option>
                                <option value="Result">Result</option>
                                <option value="Technical">Technical</option>
                                <option value="Account">Account</option>
                                <option value="General">General</option>
                            </select>
                            <div class="invalid-feedback">Category is required.</div>
                        </div>

                        <% if (canManageFeedback) { %>
                            <div class="col-md-6">
                                <label class="form-label">Status <span class="required">*</span></label>
                                <select name="status" class="form-select" required>
                                    <option value="New">New</option>
                                    <option value="In Review">In Review</option>
                                    <option value="Resolved">Resolved</option>
                                    <option value="Closed">Closed</option>
                                </select>
                                <div class="invalid-feedback">Status is required.</div>
                            </div>
                        <% } %>

                        <div class="col-12">
                            <label class="form-label">Message <span class="required">*</span></label>
                            <textarea name="message"
                                      class="form-control"
                                      rows="5"
                                      maxlength="1200"
                                      data-character-counter="feedbackModalCounter"
                                      placeholder="Write feedback message..."
                                      required></textarea>
                            <div class="invalid-feedback">Feedback message is required.</div>
                            <small class="text-secondary" id="feedbackModalCounter">0 / 1200 characters</small>
                        </div>
                    </div>

                    <div class="alert alert-info mt-4 mb-0">
                        <strong>Feedback rule:</strong>
                        Student feedback is submitted as New and can be edited only before staff review starts.
                    </div>
                </div>

                <div class="modal-footer">
                    <button class="btn btn-outline-secondary" type="button" data-bs-dismiss="modal">
                        Cancel
                    </button>

                    <button class="btn btn-primary" type="submit">
                        <i class="bi bi-send-fill me-2"></i>
                        Submit Feedback
                    </button>
                </div>
            </form>

        </div>
    </div>
</div>

<!-- Edit Feedback Modal -->
<div class="modal fade" id="editFeedbackModal" tabindex="-1" aria-labelledby="editFeedbackModalTitle" aria-hidden="true">
    <div class="modal-dialog modal-lg modal-dialog-centered">
        <div class="modal-content border-0 shadow-lg">

            <form class="needs-validation"
                  novalidate
                  action="<%= request.getContextPath() %>/feedback"
                  method="post">

                <input type="hidden" name="action" value="update">

                <div class="modal-header">
                    <div>
                        <h5 class="modal-title fw-bold" id="editFeedbackModalTitle">Edit Feedback</h5>
                        <small class="text-secondary">
                            Update feedback category, message, date, or review status.
                        </small>
                    </div>

                    <button class="btn-close" type="button" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>

                <div class="modal-body">
                    <div class="row g-3">
                        <div class="col-md-6">
                            <label class="form-label">Feedback ID</label>
                            <input type="text"
                                   id="editFeedbackId"
                                   name="feedbackId"
                                   class="form-control"
                                   readonly
                                   required>
                            <div class="invalid-feedback">Feedback ID is required.</div>
                        </div>

                        <div class="col-md-6">
                            <label class="form-label">Student ID</label>
                            <input type="text"
                                   id="editFeedbackStudentId"
                                   name="studentId"
                                   class="form-control"
                                   <%= canManageFeedback ? "" : "readonly" %>
                                   required>
                            <div class="invalid-feedback">Student ID is required.</div>
                        </div>

                        <div class="col-md-6">
                            <label class="form-label">Category</label>
                            <select id="editFeedbackCategory" name="category" class="form-select" required>
                                <option value="">Choose category</option>
                                <option value="Exam">Exam</option>
                                <option value="Result">Result</option>
                                <option value="Technical">Technical</option>
                                <option value="Account">Account</option>
                                <option value="General">General</option>
                            </select>
                            <div class="invalid-feedback">Category is required.</div>
                        </div>

                        <div class="col-md-6">
                            <label class="form-label">Date</label>
                            <input type="date"
                                   id="editFeedbackDate"
                                   name="date"
                                   class="form-control"
                                   <%= canManageFeedback ? "" : "readonly" %>
                                   required>
                            <div class="invalid-feedback">Date is required.</div>
                        </div>

                        <% if (canManageFeedback) { %>
                            <div class="col-md-6">
                                <label class="form-label">Status</label>
                                <select id="editFeedbackStatus" name="status" class="form-select" required>
                                    <option value="">Choose status</option>
                                    <option value="New">New</option>
                                    <option value="In Review">In Review</option>
                                    <option value="Resolved">Resolved</option>
                                    <option value="Closed">Closed</option>
                                </select>
                                <div class="invalid-feedback">Status is required.</div>
                            </div>
                        <% } else { %>
                            <input type="hidden" id="editFeedbackStatus" name="status" value="New">
                        <% } %>

                        <div class="col-12">
                            <label class="form-label">Message</label>
                            <textarea id="editFeedbackMessage"
                                      name="message"
                                      class="form-control"
                                      rows="5"
                                      maxlength="1200"
                                      data-character-counter="editFeedbackCounter"
                                      required></textarea>
                            <div class="invalid-feedback">Feedback message is required.</div>
                            <small class="text-secondary" id="editFeedbackCounter">0 / 1200 characters</small>
                        </div>
                    </div>

                    <div class="alert alert-info mt-4 mb-0">
                        <strong>Update workflow:</strong>
                        Students can update only New feedback. Staff can update status and review progress.
                    </div>
                </div>

                <div class="modal-footer">
                    <button class="btn btn-outline-secondary" type="button" data-bs-dismiss="modal">
                        Cancel
                    </button>

                    <button class="btn btn-primary" type="submit">
                        <i class="bi bi-save me-2"></i>
                        Update Feedback
                    </button>
                </div>
            </form>

        </div>
    </div>
</div>

<!-- View Feedback Modal -->
<div class="modal fade" id="viewFeedbackModal" tabindex="-1" aria-labelledby="viewFeedbackModalTitle" aria-hidden="true">
    <div class="modal-dialog modal-lg modal-dialog-centered">
        <div class="modal-content border-0 shadow-lg">

            <div class="modal-header">
                <div>
                    <h5 class="modal-title fw-bold" id="viewFeedbackModalTitle">Feedback Details</h5>
                    <small class="text-secondary">
                        View selected feedback message and review progress.
                    </small>
                </div>

                <button class="btn-close" type="button" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>

            <div class="modal-body">
                <div class="row g-3">
                    <div class="col-md-6">
                        <div class="exam-info-box">
                            <small>Feedback ID</small>
                            <strong id="viewFeedbackId">-</strong>
                        </div>
                    </div>

                    <div class="col-md-6">
                        <div class="exam-info-box">
                            <small>Student ID</small>
                            <strong id="viewFeedbackStudentId">-</strong>
                        </div>
                    </div>

                    <div class="col-md-6">
                        <div class="exam-info-box">
                            <small>Category</small>
                            <strong id="viewFeedbackCategory">-</strong>
                        </div>
                    </div>

                    <div class="col-md-6">
                        <div class="exam-info-box">
                            <small>Date</small>
                            <strong id="viewFeedbackDate">-</strong>
                        </div>
                    </div>

                    <div class="col-md-6">
                        <div class="exam-info-box">
                            <small>Status</small>
                            <strong id="viewFeedbackStatus">-</strong>
                        </div>
                    </div>

                    <div class="col-md-6">
                        <div class="exam-info-box">
                            <small>Priority</small>
                            <strong id="viewFeedbackPriority">-</strong>
                        </div>
                    </div>

                    <div class="col-12">
                        <div class="exam-info-box">
                            <small>Progress</small>
                            <strong id="viewFeedbackProgress">-</strong>
                        </div>
                    </div>

                    <div class="col-12">
                        <div class="exam-info-box">
                            <small>Message</small>
                            <strong id="viewFeedbackMessage">-</strong>
                        </div>
                    </div>
                </div>

                <div class="alert alert-info mt-4 mb-0">
                    <strong>Feedback record:</strong>
                    These details are loaded from the selected feedback row and displayed for quick review.
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
        const feedbackSearch = document.getElementById("feedbackSearch");
        const feedbackCategoryFilter = document.getElementById("feedbackCategoryFilter");
        const feedbackStatusFilter = document.getElementById("feedbackStatusFilter");
        const clearFeedbackFiltersBtn = document.getElementById("clearFeedbackFiltersBtn");
        const feedbackRows = document.querySelectorAll("#feedbackTable tbody tr[data-category]");

        const editFeedbackModal = document.getElementById("editFeedbackModal");
        const viewFeedbackModal = document.getElementById("viewFeedbackModal");

        function getFeedbackData(button) {
            return {
                feedbackId: button.getAttribute("data-feedback-id") || "",
                studentId: button.getAttribute("data-student-id") || "",
                category: button.getAttribute("data-category") || "",
                message: button.getAttribute("data-message") || "",
                date: button.getAttribute("data-date") || "",
                displayDate: button.getAttribute("data-display-date") || "",
                status: button.getAttribute("data-status") || "",
                priority: button.getAttribute("data-priority") || "",
                progress: button.getAttribute("data-progress") || ""
            };
        }

        function updateCounter(textareaId, counterId) {
            const textarea = document.getElementById(textareaId);
            const counter = document.getElementById(counterId);

            if (!textarea || !counter) {
                return;
            }

            const maxLength = textarea.getAttribute("maxlength") || "1200";
            counter.textContent = textarea.value.length + " / " + maxLength + " characters";
        }

        function filterFeedback() {
            const searchValue = feedbackSearch ? feedbackSearch.value.toLowerCase().trim() : "";
            const categoryValue = feedbackCategoryFilter ? feedbackCategoryFilter.value.toLowerCase().trim() : "";
            const statusValue = feedbackStatusFilter ? feedbackStatusFilter.value.toLowerCase().trim() : "";

            feedbackRows.forEach(function (row) {
                const rowText = row.innerText.toLowerCase();
                const rowCategory = row.getAttribute("data-category") || "";
                const rowStatus = row.getAttribute("data-status") || "";

                const matchesSearch = rowText.includes(searchValue);
                const matchesCategory = categoryValue === "" || rowCategory === categoryValue;
                const matchesStatus = statusValue === "" || rowStatus === statusValue;

                row.style.display = matchesSearch && matchesCategory && matchesStatus ? "" : "none";
            });
        }

        if (feedbackSearch) {
            feedbackSearch.addEventListener("input", filterFeedback);
        }

        if (feedbackCategoryFilter) {
            feedbackCategoryFilter.addEventListener("change", filterFeedback);
        }

        if (feedbackStatusFilter) {
            feedbackStatusFilter.addEventListener("change", filterFeedback);
        }

        if (clearFeedbackFiltersBtn) {
            clearFeedbackFiltersBtn.addEventListener("click", function () {
                if (feedbackSearch) {
                    feedbackSearch.value = "";
                }

                if (feedbackCategoryFilter) {
                    feedbackCategoryFilter.value = "";
                }

                if (feedbackStatusFilter) {
                    feedbackStatusFilter.value = "";
                }

                filterFeedback();
            });
        }

        if (editFeedbackModal) {
            editFeedbackModal.addEventListener("show.bs.modal", function (event) {
                const button = event.relatedTarget;

                if (!button) {
                    return;
                }

                const feedback = getFeedbackData(button);

                document.getElementById("editFeedbackId").value = feedback.feedbackId;
                document.getElementById("editFeedbackStudentId").value = feedback.studentId;
                document.getElementById("editFeedbackCategory").value = feedback.category;
                document.getElementById("editFeedbackMessage").value = feedback.message;
                document.getElementById("editFeedbackDate").value = feedback.date;

                const statusElement = document.getElementById("editFeedbackStatus");

                if (statusElement) {
                    statusElement.value = feedback.status;
                }

                updateCounter("editFeedbackMessage", "editFeedbackCounter");
            });
        }

        if (viewFeedbackModal) {
            viewFeedbackModal.addEventListener("show.bs.modal", function (event) {
                const button = event.relatedTarget;

                if (!button) {
                    return;
                }

                const feedback = getFeedbackData(button);

                document.getElementById("viewFeedbackId").textContent = feedback.feedbackId || "-";
                document.getElementById("viewFeedbackStudentId").textContent = feedback.studentId || "-";
                document.getElementById("viewFeedbackCategory").textContent = feedback.category || "-";
                document.getElementById("viewFeedbackMessage").textContent = feedback.message || "-";
                document.getElementById("viewFeedbackDate").textContent = feedback.displayDate || feedback.date || "-";
                document.getElementById("viewFeedbackStatus").textContent = feedback.status || "-";
                document.getElementById("viewFeedbackPriority").textContent = feedback.priority || "-";
                document.getElementById("viewFeedbackProgress").textContent = feedback.progress || "-";
            });
        }

        const editFeedbackMessage = document.getElementById("editFeedbackMessage");

        if (editFeedbackMessage) {
            editFeedbackMessage.addEventListener("input", function () {
                updateCounter("editFeedbackMessage", "editFeedbackCounter");
            });
        }
    });
</script>

<%@ include file="../includes/delete-modal.jsp" %>
<%@ include file="../includes/footer.jsp" %>