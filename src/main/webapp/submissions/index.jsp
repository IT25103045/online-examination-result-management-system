<%--
    Staff Submission Review Dashboard.
    Allows Admin/Lecturer users to review submitted exam attempts and answers.

    Responsible Member:
    IT25103045 - De Silva H.L.D.C.P.C
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ page import="java.util.List" %>
<%@ page import="lk.nextexam.dao.FileUtil" %>
<%@ page import="lk.nextexam.dao.ExamSubmissionDAO" %>
<%@ page import="lk.nextexam.dao.QuestionDAO" %>
<%@ page import="lk.nextexam.dao.ExamDAO" %>
<%@ page import="lk.nextexam.model.ExamSubmission" %>
<%@ page import="lk.nextexam.model.Question" %>
<%@ page import="lk.nextexam.model.Exam" %>

<%!
    private String extractSubmittedAnswer(String answersData, String questionId) {
        if (answersData == null || questionId == null) {
            return "";
        }

        String[] items = answersData.split(";");

        for (String item : items) {
            if (item == null) {
                continue;
            }

            String cleanItem = item.trim();
            String prefix = questionId + "=";

            if (cleanItem.startsWith(prefix)) {
                String value = cleanItem.substring(prefix.length());
                int flaggedIndex = value.indexOf(",flagged=");

                if (flaggedIndex >= 0) {
                    value = value.substring(0, flaggedIndex);
                }

                if ("NO_ANSWER".equalsIgnoreCase(value)) {
                    return "";
                }

                return value.trim();
            }
        }

        return "";
    }

    private String extractFlaggedStatus(String answersData, String questionId) {
        if (answersData == null || questionId == null) {
            return "NO";
        }

        String[] items = answersData.split(";");

        for (String item : items) {
            if (item == null) {
                continue;
            }

            String cleanItem = item.trim();
            String prefix = questionId + "=";

            if (cleanItem.startsWith(prefix)) {
                int flaggedIndex = cleanItem.indexOf(",flagged=");

                if (flaggedIndex >= 0) {
                    String flaggedValue = cleanItem.substring(flaggedIndex + ",flagged=".length());
                    int typeIndex = flaggedValue.indexOf(",type=");

                    if (typeIndex >= 0) {
                        flaggedValue = flaggedValue.substring(0, typeIndex);
                    }

                    return flaggedValue.trim();
                }
            }
        }

        return "NO";
    }

    private String riskBadgeForSubmission(ExamSubmission submission) {
        if (submission == null) {
            return "badge-soft-secondary";
        }

        if (submission.isManualReviewRequired()) {
            return "badge-soft-warning";
        }

        if (submission.isAutoMarked()) {
            return "badge-soft-primary";
        }

        if (submission.isMarked() || submission.isPublished()) {
            return "badge-soft-success";
        }

        if (submission.isCancelled()) {
            return "badge-soft-danger";
        }

        return "badge-soft-info";
    }
%>

<%
    String pageTitle = "Submissions";
    String activeMenu = "submissions";
    String topbarTitle = "Submission Review";

    ExamSubmissionDAO submissionDAO = new ExamSubmissionDAO();
    QuestionDAO questionDAO = new QuestionDAO();
    ExamDAO examDAO = new ExamDAO();

    List<ExamSubmission> submissions = submissionDAO.getAllSubmissions(application);

    int totalSubmissions = submissions != null ? submissions.size() : 0;
    int autoMarkedCount = 0;
    int manualReviewCount = 0;
    int markedCount = 0;
    int publishedCount = 0;
    int cancelledCount = 0;
    double totalPercentage = 0.0;

    if (submissions != null) {
        for (ExamSubmission submission : submissions) {
            if (submission.isAutoMarked()) {
                autoMarkedCount++;
            }

            if (submission.isManualReviewRequired()) {
                manualReviewCount++;
            }

            if (submission.isMarked()) {
                markedCount++;
            }

            if (submission.isPublished()) {
                publishedCount++;
            }

            if (submission.isCancelled()) {
                cancelledCount++;
            }

            totalPercentage += submission.getPercentage();
        }
    }

    double averagePercentage = totalSubmissions > 0 ? totalPercentage / totalSubmissions : 0.0;
    String averageDisplay = averagePercentage == Math.floor(averagePercentage)
            ? String.valueOf((int) averagePercentage)
            : String.format("%.1f", averagePercentage);
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
                            <i class="bi bi-clipboard-data-fill me-1"></i>
                            Staff Assessment Review
                        </span>

                        <h1 class="hero-title">Exam Submission Review</h1>

                        <p class="hero-text">
                            Review student exam attempts, inspect MCQ and essay answers, identify manual review submissions,
                            and support the result processing workflow.
                        </p>
                    </div>

                    <div class="d-flex gap-2 flex-wrap">
                        <a href="<%= request.getContextPath() %>/results" class="btn btn-outline-primary">
                            <i class="bi bi-bar-chart-fill me-2"></i>
                            Results
                        </a>

                        <button type="button" class="btn btn-primary" onclick="window.print()">
                            <i class="bi bi-printer me-2"></i>
                            Print
                        </button>
                    </div>
                </div>
            </div>

            <div class="row g-3 mb-4">
                <div class="col-md-6 col-xl-3">
                    <div class="app-card stat-card">
                        <div class="d-flex justify-content-between gap-3">
                            <div>
                                <div class="stat-label">Total Submissions</div>
                                <div class="stat-value"><%= totalSubmissions %></div>
                                <div class="stat-meta">Submitted exam attempts</div>
                            </div>

                            <div class="stat-icon">
                                <i class="bi bi-inboxes-fill"></i>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-md-6 col-xl-3">
                    <div class="app-card stat-card">
                        <div class="d-flex justify-content-between gap-3">
                            <div>
                                <div class="stat-label">Manual Review</div>
                                <div class="stat-value"><%= manualReviewCount %></div>
                                <div class="stat-meta">Essay or mixed submissions</div>
                            </div>

                            <div class="stat-icon">
                                <i class="bi bi-pencil-square"></i>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-md-6 col-xl-3">
                    <div class="app-card stat-card">
                        <div class="d-flex justify-content-between gap-3">
                            <div>
                                <div class="stat-label">Auto Marked</div>
                                <div class="stat-value"><%= autoMarkedCount %></div>
                                <div class="stat-meta">MCQ-only auto scored</div>
                            </div>

                            <div class="stat-icon">
                                <i class="bi bi-lightning-charge-fill"></i>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-md-6 col-xl-3">
                    <div class="app-card stat-card">
                        <div class="d-flex justify-content-between gap-3">
                            <div>
                                <div class="stat-label">Average Score</div>
                                <div class="stat-value"><%= averageDisplay %>%</div>
                                <div class="stat-meta"><%= publishedCount %> published · <%= markedCount %> marked</div>
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
                    <div class="app-card p-4 h-100">
                        <div class="d-flex justify-content-between align-items-start gap-3 mb-3">
                            <div>
                                <h4 class="fw-bold mb-1">Review Queue</h4>
                                <p class="text-secondary mb-0">
                                    Lecturer attention summary.
                                </p>
                            </div>

                            <span class="badge badge-soft-warning">
                                Queue
                            </span>
                        </div>

                        <div class="submission-review-stack">
                            <div class="submission-review-item warning">
                                <div>
                                    <small>Needs Review</small>
                                    <strong><%= manualReviewCount %></strong>
                                </div>
                                <span>Essay/manual marking required</span>
                            </div>

                            <div class="submission-review-item primary">
                                <div>
                                    <small>Auto Marked</small>
                                    <strong><%= autoMarkedCount %></strong>
                                </div>
                                <span>Ready for result processing</span>
                            </div>

                            <div class="submission-review-item success">
                                <div>
                                    <small>Published</small>
                                    <strong><%= publishedCount %></strong>
                                </div>
                                <span>Visible or final workflow stage</span>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-xl-8">
                    <div class="app-card p-4 h-100">
                        <div class="d-flex justify-content-between align-items-start flex-wrap gap-3 mb-3">
                            <div>
                                <h4 class="fw-bold mb-1">Submission Review Guidelines</h4>
                                <p class="text-secondary mb-0">
                                    Use this dashboard to review answers before creating or publishing final results.
                                </p>
                            </div>

                            <span class="badge badge-soft-primary">
                                Lecturer Workflow
                            </span>
                        </div>

                        <div class="row g-3">
                            <div class="col-md-6">
                                <div class="exam-info-box">
                                    <small>MCQ Answers</small>
                                    <strong>Selected answer and correct answer can be compared.</strong>
                                </div>
                            </div>

                            <div class="col-md-6">
                                <div class="exam-info-box">
                                    <small>Essay Answers</small>
                                    <strong>Lecturer can read submitted text and compare with model answer.</strong>
                                </div>
                            </div>

                            <div class="col-md-6">
                                <div class="exam-info-box">
                                    <small>Manual Review</small>
                                    <strong>Essay or mixed exams are marked as review required.</strong>
                                </div>
                            </div>

                            <div class="col-md-6">
                                <div class="exam-info-box">
                                    <small>Next Step</small>
                                    <strong>Create/update final result after marking review is complete.</strong>
                                </div>
                            </div>
                        </div>

                        <div class="alert alert-info mt-4 mb-0">
                            <strong>Note:</strong>
                            This page reviews submitted attempts. Manual essay mark entry can be added in the next enhancement pack.
                        </div>
                    </div>
                </div>
            </div>

            <div class="app-card crud-card p-4">
                <div class="crud-toolbar">
                    <div>
                        <h4 class="fw-bold mb-1">Submission Records</h4>
                        <p class="text-secondary mb-0">
                            Search, filter, and open submitted exam attempts.
                        </p>
                    </div>

                    <div class="d-flex gap-2 flex-wrap">
                        <div class="input-group search-control">
                            <span class="input-group-text">
                                <i class="bi bi-search"></i>
                            </span>

                            <input type="search"
                                   class="form-control"
                                   id="submissionSearch"
                                   placeholder="Search submission, student, exam, status">
                        </div>

                        <select class="form-select" id="statusFilter" style="width: 210px;">
                            <option value="">All Status</option>
                            <option value="auto marked">Auto Marked</option>
                            <option value="manual review required">Manual Review Required</option>
                            <option value="marked">Marked</option>
                            <option value="published">Published</option>
                            <option value="cancelled">Cancelled</option>
                        </select>
                    </div>
                </div>

                <% if (submissions == null || submissions.isEmpty()) { %>
                    <div class="empty-state">
                        <div class="empty-state-icon">
                            <i class="bi bi-inbox"></i>
                        </div>

                        <h5>No submissions yet</h5>
                        <p>Student exam submissions will appear here after exams are submitted.</p>
                    </div>
                <% } else { %>
                    <div class="table-responsive">
                        <table class="table table-hover align-middle submission-table" id="submissionTable">
                            <thead>
                            <tr>
                                <th>Submission ID</th>
                                <th>Student</th>
                                <th>Exam</th>
                                <th>Submitted At</th>
                                <th>Score</th>
                                <th>Percentage</th>
                                <th>Status</th>
                                <th>Answers</th>
                                <th class="text-end">Action</th>
                            </tr>
                            </thead>

                            <tbody>
                            <% for (ExamSubmission submission : submissions) {
                                String modalId = "submissionModal_" + FileUtil.h(submission.getSubmissionId());
                            %>
                                <tr data-status="<%= FileUtil.h(submission.getStatus().toLowerCase()) %>">
                                    <td class="fw-bold"><%= FileUtil.h(submission.getSubmissionId()) %></td>

                                    <td>
                                        <strong><%= FileUtil.h(submission.getStudentName()) %></strong><br>
                                        <small class="text-secondary"><%= FileUtil.h(submission.getStudentId()) %></small>
                                    </td>

                                    <td>
                                        <strong><%= FileUtil.h(submission.getExamId()) %></strong>
                                    </td>

                                    <td>
                                        <small class="text-secondary">
                                            <%= FileUtil.h(submission.getDisplaySubmittedAt()) %>
                                        </small>
                                    </td>

                                    <td>
                                        <span class="fw-bold"><%= FileUtil.h(submission.getScoreSummary()) %></span>
                                    </td>

                                    <td>
                                        <span class="badge badge-soft-primary">
                                            <%= FileUtil.h(submission.getPercentageDisplay()) %>
                                        </span>
                                    </td>

                                    <td>
                                        <span class="badge <%= riskBadgeForSubmission(submission) %>">
                                            <%= FileUtil.h(submission.getStatus()) %>
                                        </span>
                                    </td>

                                    <td>
                                        <span class="badge badge-soft-secondary">
                                            <%= submission.getAnsweredItemCount() %> items
                                        </span>
                                    </td>

                                    <td class="text-end">
                                        <button type="button"
                                                class="btn btn-sm btn-outline-primary"
                                                data-bs-toggle="modal"
                                                data-bs-target="#<%= modalId %>">
                                            <i class="bi bi-eye me-1"></i>
                                            Review
                                        </button>
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

<% if (submissions != null && !submissions.isEmpty()) {
    for (ExamSubmission submission : submissions) {
        String modalId = "submissionModal_" + FileUtil.h(submission.getSubmissionId());
        List<Question> questions = questionDAO.getStudentVisibleQuestionsByExamId(application, submission.getExamId());
        Exam exam = examDAO.getExamById(application, submission.getExamId());
%>

<div class="modal fade" id="<%= modalId %>" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-xl modal-dialog-centered modal-dialog-scrollable">
        <div class="modal-content border-0 shadow-lg">

            <div class="modal-header">
                <div>
                    <h5 class="modal-title fw-bold">
                        Submission Review — <%= FileUtil.h(submission.getSubmissionId()) %>
                    </h5>

                    <small class="text-secondary">
                        <%= FileUtil.h(submission.getStudentName()) %> ·
                        <%= FileUtil.h(submission.getStudentId()) %> ·
                        <%= FileUtil.h(submission.getExamId()) %>
                    </small>
                </div>

                <button class="btn-close" type="button" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>

            <div class="modal-body">
                <div class="row g-3 mb-4">
                    <div class="col-md-3">
                        <div class="exam-info-box">
                            <small>Subject</small>
                            <strong><%= exam != null ? FileUtil.h(exam.getSubject()) : FileUtil.h(submission.getExamId()) %></strong>
                        </div>
                    </div>

                    <div class="col-md-3">
                        <div class="exam-info-box">
                            <small>Score</small>
                            <strong><%= FileUtil.h(submission.getScoreSummary()) %></strong>
                        </div>
                    </div>

                    <div class="col-md-3">
                        <div class="exam-info-box">
                            <small>Percentage</small>
                            <strong><%= FileUtil.h(submission.getPercentageDisplay()) %></strong>
                        </div>
                    </div>

                    <div class="col-md-3">
                        <div class="exam-info-box">
                            <small>Status</small>
                            <strong><%= FileUtil.h(submission.getStatus()) %></strong>
                        </div>
                    </div>
                </div>

                <% if (questions == null || questions.isEmpty()) { %>
                    <div class="empty-state">
                        <div class="empty-state-icon">
                            <i class="bi bi-patch-question-fill"></i>
                        </div>

                        <h5>No active questions found</h5>
                        <p>Question details may have been archived or removed.</p>
                    </div>
                <% } else {
                    int questionNumber = 0;
                    for (Question question : questions) {
                        questionNumber++;
                        String answer = extractSubmittedAnswer(submission.getAnswersData(), question.getQuestionId());
                        String flagged = extractFlaggedStatus(submission.getAnswersData(), question.getQuestionId());
                        boolean flaggedYes = "YES".equalsIgnoreCase(flagged);
                        boolean correctMcq = question.isMcq() && question.isCorrectMcqAnswer(answer);
                %>

                    <div class="submission-answer-card">
                        <div class="submission-answer-head">
                            <div>
                                <span class="badge badge-soft-primary">
                                    Question <%= questionNumber %>
                                </span>

                                <span class="badge <%= question.getTypeBadgeClass() %>">
                                    <%= FileUtil.h(question.getQuestionType()) %>
                                </span>

                                <% if (flaggedYes) { %>
                                    <span class="badge badge-soft-warning">
                                        <i class="bi bi-flag-fill me-1"></i>
                                        Marked Review
                                    </span>
                                <% } %>
                            </div>

                            <span class="submission-answer-mark">
                                <%= FileUtil.h(question.getDisplayMarks()) %> marks
                            </span>
                        </div>

                        <div class="submission-question-text">
                            <strong><%= FileUtil.h(question.getQuestionText()) %></strong>
                        </div>

                        <% if (question.isMcq()) { %>
                            <div class="row g-3 mt-2">
                                <div class="col-md-6">
                                    <div class="submission-answer-box <%= correctMcq ? "correct" : "wrong" %>">
                                        <small>Student Answer</small>
                                        <strong>
                                            <% if (answer.isEmpty()) { %>
                                                No answer
                                            <% } else { %>
                                                <%= FileUtil.h(answer) %> — <%= FileUtil.h(question.getOptionByLetter(answer)) %>
                                            <% } %>
                                        </strong>
                                    </div>
                                </div>

                                <div class="col-md-6">
                                    <div class="submission-answer-box correct">
                                        <small>Correct Answer</small>
                                        <strong>
                                            <%= FileUtil.h(question.getCorrectAnswer()) %> —
                                            <%= FileUtil.h(question.getOptionByLetter(question.getCorrectAnswer())) %>
                                        </strong>
                                    </div>
                                </div>
                            </div>
                        <% } else { %>
                            <div class="row g-3 mt-2">
                                <div class="col-md-6">
                                    <div class="submission-answer-box essay">
                                        <small>Student Essay Answer</small>
                                        <p>
                                            <%= answer.isEmpty() ? "No answer submitted." : FileUtil.h(answer) %>
                                        </p>
                                    </div>
                                </div>

                                <div class="col-md-6">
                                    <div class="submission-answer-box model">
                                        <small>Model Answer / Marking Guide</small>
                                        <p>
                                            <%= question.getModelAnswer().isEmpty() ? "No model answer saved." : FileUtil.h(question.getModelAnswer()) %>
                                        </p>
                                    </div>
                                </div>
                            </div>
                        <% } %>
                    </div>

                <% }
                } %>
            </div>

            <div class="modal-footer">
                <button class="btn btn-outline-secondary" type="button" data-bs-dismiss="modal">
                    Close
                </button>

                <a href="<%= request.getContextPath() %>/results" class="btn btn-primary">
                    <i class="bi bi-bar-chart-fill me-2"></i>
                    Go to Results
                </a>
            </div>

        </div>
    </div>
</div>

<% }
} %>

<script>
    document.addEventListener("DOMContentLoaded", function () {
        const searchInput = document.getElementById("submissionSearch");
        const statusFilter = document.getElementById("statusFilter");
        const rows = document.querySelectorAll("#submissionTable tbody tr[data-status]");

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