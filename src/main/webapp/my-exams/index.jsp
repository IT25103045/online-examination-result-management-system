<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="lk.nextexam.dao.FileUtil" %>
<%@ page import="lk.nextexam.model.Exam" %>
<%@ page import="lk.nextexam.model.ExamSubmission" %>

<%
    String pageTitle = "My Exams";
    String activeMenu = "my-exams";
    String topbarTitle = "My Exams";

    String studentId = request.getAttribute("studentId") != null
            ? request.getAttribute("studentId").toString()
            : "";

    String studentName = request.getAttribute("studentName") != null
            ? request.getAttribute("studentName").toString()
            : "Student";

    List<Exam> attemptableExams = (List<Exam>) request.getAttribute("attemptableExams");
    List<ExamSubmission> mySubmissions = (List<ExamSubmission>) request.getAttribute("mySubmissions");

    Map<String, Integer> questionCounts = (Map<String, Integer>) request.getAttribute("questionCounts");
    Map<String, Integer> mcqQuestionCounts = (Map<String, Integer>) request.getAttribute("mcqQuestionCounts");
    Map<String, Integer> essayQuestionCounts = (Map<String, Integer>) request.getAttribute("essayQuestionCounts");

    Map<String, Double> examTotalMarks = (Map<String, Double>) request.getAttribute("examTotalMarks");
    Map<String, Double> mcqMarks = (Map<String, Double>) request.getAttribute("mcqMarks");
    Map<String, Double> essayMarks = (Map<String, Double>) request.getAttribute("essayMarks");

    Map<String, Boolean> submittedMap = (Map<String, Boolean>) request.getAttribute("submittedMap");
    Map<String, Boolean> readyMap = (Map<String, Boolean>) request.getAttribute("readyMap");
    Map<String, String> readinessMessageMap = (Map<String, String>) request.getAttribute("readinessMessageMap");
    Map<String, ExamSubmission> submissionMap = (Map<String, ExamSubmission>) request.getAttribute("submissionMap");

    int availableExamCount = request.getAttribute("availableExamCount") != null
            ? (Integer) request.getAttribute("availableExamCount")
            : (attemptableExams != null ? attemptableExams.size() : 0);

    int readyExamCount = request.getAttribute("readyExamCount") != null
            ? (Integer) request.getAttribute("readyExamCount")
            : 0;

    int submittedExamCount = request.getAttribute("submittedExamCount") != null
            ? (Integer) request.getAttribute("submittedExamCount")
            : 0;

    int pendingExamCount = request.getAttribute("pendingExamCount") != null
            ? (Integer) request.getAttribute("pendingExamCount")
            : 0;

    int manualReviewCount = request.getAttribute("manualReviewCount") != null
            ? (Integer) request.getAttribute("manualReviewCount")
            : 0;

    int publishedResultCount = request.getAttribute("publishedResultCount") != null
            ? (Integer) request.getAttribute("publishedResultCount")
            : 0;

    String success = request.getParameter("success");
    String error = request.getParameter("error");

    String alertMessage = "";
    String alertType = "";

    if (success != null) {
        alertType = "success";

        if ("examSubmitted".equalsIgnoreCase(success)) {
            alertMessage = "Your exam has been submitted successfully.";
        } else {
            alertMessage = "Action completed successfully.";
        }
    }

    if (error != null) {
        alertType = "danger";

        if ("missingExamId".equalsIgnoreCase(error)) {
            alertMessage = "Exam ID is missing.";
        } else if ("examNotFound".equalsIgnoreCase(error)) {
            alertMessage = "The selected exam could not be found.";
        } else if ("examUnavailable".equalsIgnoreCase(error)) {
            alertMessage = "This exam is not currently available.";
        } else if ("noActiveQuestions".equalsIgnoreCase(error)) {
            alertMessage = "This exam has no student-visible questions yet.";
        } else if ("alreadySubmitted".equalsIgnoreCase(error)) {
            alertMessage = "You have already submitted this exam.";
        } else if ("invalidSubmission".equalsIgnoreCase(error)) {
            alertMessage = "Invalid exam submission request.";
        } else if ("submissionFailed".equalsIgnoreCase(error)) {
            alertMessage = "Your submission could not be saved.";
        } else if ("examNotReady".equalsIgnoreCase(error)) {
            alertMessage = "This exam is not ready for student attempts yet.";
        } else if ("accessDenied".equalsIgnoreCase(error)) {
            alertMessage = "You do not have permission to access that page.";
        } else if ("sessionExpired".equalsIgnoreCase(error)) {
            alertMessage = "Your session has expired. Please log in again.";
        } else {
            alertMessage = "Something went wrong. Please try again.";
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
                            <i class="bi bi-shield-lock-fill me-1"></i>
                            Secure Student Examination Portal
                        </span>

                        <h1 class="hero-title">My Online Exams</h1>

                        <p class="hero-text">
                            Welcome, <strong><%= FileUtil.h(studentName) %></strong>. View available assessments,
                            check readiness, and enter the secure exam console when you are ready.
                        </p>
                    </div>

                    <div class="d-flex gap-2 flex-wrap">
                        <a href="<%= request.getContextPath() %>/my-results" class="btn btn-outline-primary">
                            <i class="bi bi-bar-chart-fill me-2"></i>
                            My Results
                        </a>

                        <a href="<%= request.getContextPath() %>/notices" class="btn btn-outline-primary">
                            <i class="bi bi-megaphone-fill me-2"></i>
                            Notices
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
                                <div class="stat-label">Available Exams</div>
                                <div class="stat-value"><%= availableExamCount %></div>
                                <div class="stat-meta">Visible to you</div>
                            </div>

                            <div class="stat-icon">
                                <i class="bi bi-journal-check"></i>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-md-6 col-xl-3">
                    <div class="app-card stat-card">
                        <div class="d-flex justify-content-between gap-3">
                            <div>
                                <div class="stat-label">Ready</div>
                                <div class="stat-value"><%= readyExamCount %></div>
                                <div class="stat-meta">Can be attempted</div>
                            </div>

                            <div class="stat-icon">
                                <i class="bi bi-unlock-fill"></i>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-md-6 col-xl-3">
                    <div class="app-card stat-card">
                        <div class="d-flex justify-content-between gap-3">
                            <div>
                                <div class="stat-label">Pending</div>
                                <div class="stat-value"><%= pendingExamCount %></div>
                                <div class="stat-meta">Not submitted yet</div>
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
                                <div class="stat-label">Submitted</div>
                                <div class="stat-value"><%= submittedExamCount %></div>
                                <div class="stat-meta">Completed attempts</div>
                            </div>

                            <div class="stat-icon">
                                <i class="bi bi-check-circle-fill"></i>
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
                                <h4 class="fw-bold mb-1">Available Online Exams</h4>
                                <p class="text-secondary mb-0">
                                    Start only when you are ready. Once submitted, the same exam cannot be attempted again.
                                </p>
                            </div>

                            <span class="badge badge-soft-primary">
                                <i class="bi bi-lock-fill me-1"></i>
                                One Attempt Only
                            </span>
                        </div>

                        <% if (attemptableExams == null || attemptableExams.isEmpty()) { %>
                            <div class="empty-state">
                                <div class="empty-state-icon">
                                    <i class="bi bi-calendar-x"></i>
                                </div>
                                <h5>No exams available</h5>
                                <p>There are no active, scheduled, or ongoing online exams available right now.</p>
                            </div>
                        <% } else { %>
                            <div class="row g-3">
                                <% for (Exam exam : attemptableExams) {
                                    String examId = exam.getExamId();

                                    int questionCount = questionCounts != null && questionCounts.get(examId) != null
                                            ? questionCounts.get(examId)
                                            : 0;

                                    int mcqCount = mcqQuestionCounts != null && mcqQuestionCounts.get(examId) != null
                                            ? mcqQuestionCounts.get(examId)
                                            : 0;

                                    int essayCount = essayQuestionCounts != null && essayQuestionCounts.get(examId) != null
                                            ? essayQuestionCounts.get(examId)
                                            : 0;

                                    double totalMarks = examTotalMarks != null && examTotalMarks.get(examId) != null
                                            ? examTotalMarks.get(examId)
                                            : 0.0;

                                    double totalMcqMarks = mcqMarks != null && mcqMarks.get(examId) != null
                                            ? mcqMarks.get(examId)
                                            : 0.0;

                                    double totalEssayMarks = essayMarks != null && essayMarks.get(examId) != null
                                            ? essayMarks.get(examId)
                                            : 0.0;

                                    boolean submitted = submittedMap != null
                                            && submittedMap.get(examId) != null
                                            && submittedMap.get(examId);

                                    boolean ready = readyMap != null
                                            && readyMap.get(examId) != null
                                            && readyMap.get(examId);

                                    String readinessMessage = readinessMessageMap != null && readinessMessageMap.get(examId) != null
                                            ? readinessMessageMap.get(examId)
                                            : "";

                                    ExamSubmission submission = submissionMap != null
                                            ? submissionMap.get(examId)
                                            : null;

                                    String marksDisplay = totalMarks == Math.floor(totalMarks)
                                            ? String.valueOf((int) totalMarks)
                                            : String.format("%.2f", totalMarks);

                                    String mcqMarksDisplay = totalMcqMarks == Math.floor(totalMcqMarks)
                                            ? String.valueOf((int) totalMcqMarks)
                                            : String.format("%.2f", totalMcqMarks);

                                    String essayMarksDisplay = totalEssayMarks == Math.floor(totalEssayMarks)
                                            ? String.valueOf((int) totalEssayMarks)
                                            : String.format("%.2f", totalEssayMarks);

                                    String accessLabel;
                                    String accessClass;
                                    String accessIcon;

                                    if (submitted) {
                                        accessLabel = submission != null ? submission.getProgressLabel() : "Submitted";
                                        accessClass = submission != null ? submission.getStatusBadgeClass() : "badge-soft-success";
                                        accessIcon = "bi-check-circle-fill";
                                    } else if (!ready) {
                                        accessLabel = "Not Ready";
                                        accessClass = "badge-soft-warning";
                                        accessIcon = "bi-lock-fill";
                                    } else {
                                        accessLabel = "Ready to Attempt";
                                        accessClass = "badge-soft-primary";
                                        accessIcon = "bi-play-circle-fill";
                                    }
                                %>
                                    <div class="col-12">
                                        <div class="exam-list-card">
                                            <div class="d-flex justify-content-between align-items-start flex-wrap gap-3">
                                                <div class="flex-grow-1">
                                                    <div class="d-flex align-items-center gap-2 flex-wrap mb-2">
                                                        <span class="badge badge-soft-secondary">
                                                            <%= FileUtil.h(examId) %>
                                                        </span>

                                                        <span class="badge <%= exam.getDisplayStatusClass() %>">
                                                            <%= FileUtil.h(exam.getStatus()) %>
                                                        </span>

                                                        <span class="badge <%= accessClass %>">
                                                            <i class="bi <%= accessIcon %> me-1"></i>
                                                            <%= FileUtil.h(accessLabel) %>
                                                        </span>
                                                    </div>

                                                    <h5 class="fw-bold mb-1">
                                                        <%= FileUtil.h(exam.getSubject()) %>
                                                    </h5>

                                                    <p class="text-secondary mb-3">
                                                        <%= FileUtil.h(exam.getLifecycleHint()) %>
                                                    </p>

                                                    <div class="exam-meta-grid">
                                                        <div class="exam-meta-item">
                                                            <i class="bi bi-calendar-event"></i>
                                                            <span><%= FileUtil.h(exam.getDisplayExamDate()) %></span>
                                                        </div>

                                                        <div class="exam-meta-item">
                                                            <i class="bi bi-stopwatch"></i>
                                                            <span><%= FileUtil.h(exam.getDisplayDuration()) %></span>
                                                        </div>

                                                        <div class="exam-meta-item">
                                                            <i class="bi bi-patch-question"></i>
                                                            <span><%= questionCount %> Questions</span>
                                                        </div>

                                                        <div class="exam-meta-item">
                                                            <i class="bi bi-award"></i>
                                                            <span><%= marksDisplay %> Marks</span>
                                                        </div>
                                                    </div>

                                                    <div class="row g-2 mt-3">
                                                        <div class="col-md-4">
                                                            <div class="exam-info-box">
                                                                <small>MCQ</small>
                                                                <strong><%= mcqCount %> Questions · <%= mcqMarksDisplay %> Marks</strong>
                                                            </div>
                                                        </div>

                                                        <div class="col-md-4">
                                                            <div class="exam-info-box">
                                                                <small>Essay</small>
                                                                <strong><%= essayCount %> Questions · <%= essayMarksDisplay %> Marks</strong>
                                                            </div>
                                                        </div>

                                                        <div class="col-md-4">
                                                            <div class="exam-info-box">
                                                                <small>Result Mode</small>
                                                                <strong><%= essayCount > 0 ? "Manual Review" : "Auto Marked" %></strong>
                                                            </div>
                                                        </div>
                                                    </div>

                                                    <% if (!ready && !submitted && !readinessMessage.isEmpty()) { %>
                                                        <div class="alert alert-warning mt-3 mb-0">
                                                            <i class="bi bi-info-circle-fill me-1"></i>
                                                            <%= FileUtil.h(readinessMessage) %>
                                                        </div>
                                                    <% } %>

                                                    <% if (submitted && submission != null) { %>
                                                        <div class="alert alert-info mt-3 mb-0">
                                                            <i class="bi bi-clock-history me-1"></i>
                                                            Submitted on
                                                            <strong><%= FileUtil.h(submission.getDisplaySubmittedAt()) %></strong>.
                                                            Current status:
                                                            <strong><%= FileUtil.h(submission.getProgressLabel()) %></strong>.
                                                        </div>
                                                    <% } %>
                                                </div>

                                                <div class="text-end">
                                                    <% if (submitted) { %>
                                                        <button class="btn btn-outline-success" disabled>
                                                            <i class="bi bi-check-circle me-2"></i>
                                                            Submitted
                                                        </button>
                                                    <% } else if (!ready) { %>
                                                        <button class="btn btn-outline-secondary" disabled>
                                                            <i class="bi bi-lock-fill me-2"></i>
                                                            Not Ready
                                                        </button>
                                                    <% } else { %>
                                                        <button class="btn btn-primary"
                                                                data-bs-toggle="modal"
                                                                data-bs-target="#startExamModal"
                                                                data-exam-id="<%= FileUtil.h(examId) %>"
                                                                data-exam-subject="<%= FileUtil.h(exam.getSubject()) %>"
                                                                data-exam-duration="<%= FileUtil.h(exam.getDisplayDuration()) %>"
                                                                data-exam-questions="<%= questionCount %>"
                                                                data-exam-marks="<%= marksDisplay %>"
                                                                data-exam-mcq="<%= mcqCount %>"
                                                                data-exam-essay="<%= essayCount %>"
                                                                data-exam-mode="<%= essayCount > 0 ? "Manual Review" : "Auto Marked" %>">
                                                            <i class="bi bi-play-circle-fill me-2"></i>
                                                            Start Exam
                                                        </button>
                                                    <% } %>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                <% } %>
                            </div>
                        <% } %>
                    </div>
                </div>

                <div class="col-xl-4">
                    <div class="app-card p-4 h-100">
                        <div class="d-flex justify-content-between align-items-start mb-3">
                            <div>
                                <h4 class="fw-bold mb-1">Exam Rules</h4>
                                <p class="text-secondary mb-0">Read before starting</p>
                            </div>

                            <span class="badge badge-soft-danger">
                                <i class="bi bi-exclamation-triangle-fill me-1"></i>
                                Important
                            </span>
                        </div>

                        <div class="exam-rules-list">
                            <div class="exam-rule-item">
                                <i class="bi bi-clock-fill"></i>
                                <span>The timer starts immediately when the exam console opens.</span>
                            </div>

                            <div class="exam-rule-item">
                                <i class="bi bi-shield-lock-fill"></i>
                                <span>Do not refresh, close, or navigate away during the exam.</span>
                            </div>

                            <div class="exam-rule-item">
                                <i class="bi bi-check-circle-fill"></i>
                                <span>Only one final submission is allowed per exam.</span>
                            </div>

                            <div class="exam-rule-item">
                                <i class="bi bi-flag-fill"></i>
                                <span>You can mark questions for review before submitting.</span>
                            </div>

                            <div class="exam-rule-item">
                                <i class="bi bi-wifi"></i>
                                <span>Use a stable internet connection before starting.</span>
                            </div>
                        </div>

                        <div class="soft-divider"></div>

                        <div class="row g-2">
                            <div class="col-6">
                                <div class="exam-info-box">
                                    <small>Manual Review</small>
                                    <strong><%= manualReviewCount %></strong>
                                </div>
                            </div>

                            <div class="col-6">
                                <div class="exam-info-box">
                                    <small>Published</small>
                                    <strong><%= publishedResultCount %></strong>
                                </div>
                            </div>
                        </div>

                        <div class="alert alert-info mt-4 mb-0">
                            <strong>Tip:</strong>
                            Attempt all questions first, then use the question palette to review unanswered or flagged items.
                        </div>
                    </div>
                </div>
            </div>

            <div class="app-card p-4">
                <div class="d-flex justify-content-between align-items-start flex-wrap gap-3 mb-3">
                    <div>
                        <h4 class="fw-bold mb-1">My Recent Submissions</h4>
                        <p class="text-secondary mb-0">
                            Track submitted exams, marking progress, and published result status.
                        </p>
                    </div>

                    <span class="badge badge-soft-secondary">
                        <i class="bi bi-clock-history me-1"></i>
                        Attempt History
                    </span>
                </div>

                <div class="table-responsive">
                    <table class="table table-hover align-middle">
                        <thead>
                        <tr>
                            <th>Submission ID</th>
                            <th>Exam ID</th>
                            <th>Submitted At</th>
                            <th>Score</th>
                            <th>Percentage</th>
                            <th>Status</th>
                        </tr>
                        </thead>

                        <tbody>
                        <% if (mySubmissions == null || mySubmissions.isEmpty()) { %>
                            <tr>
                                <td colspan="6">
                                    <div class="empty-state">
                                        <div class="empty-state-icon">
                                            <i class="bi bi-inbox"></i>
                                        </div>
                                        <h5>No submissions yet</h5>
                                        <p>Your submitted exams will appear here.</p>
                                    </div>
                                </td>
                            </tr>
                        <% } else {
                            for (ExamSubmission submission : mySubmissions) {
                        %>
                            <tr>
                                <td class="fw-bold"><%= FileUtil.h(submission.getSubmissionId()) %></td>
                                <td><%= FileUtil.h(submission.getExamId()) %></td>
                                <td><%= FileUtil.h(submission.getDisplaySubmittedAt()) %></td>
                                <td><%= FileUtil.h(submission.getScoreSummary()) %></td>
                                <td><%= FileUtil.h(submission.getPercentageDisplay()) %></td>
                                <td>
                                    <span class="badge <%= submission.getStatusBadgeClass() %>">
                                        <%= FileUtil.h(submission.getStatus()) %>
                                    </span>
                                </td>
                            </tr>
                        <% }
                        } %>
                        </tbody>
                    </table>
                </div>
            </div>

        </section>
    </main>
</div>

<div class="modal fade" id="startExamModal" tabindex="-1" aria-labelledby="startExamModalTitle" aria-hidden="true">
    <div class="modal-dialog modal-lg modal-dialog-centered">
        <div class="modal-content border-0 shadow-lg">

            <div class="modal-header">
                <div>
                    <h5 class="modal-title fw-bold" id="startExamModalTitle">Start Online Exam</h5>
                    <small class="text-secondary">
                        Confirm exam details before reading and accepting the exam rules.
                    </small>
                </div>

                <button class="btn-close" type="button" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>

            <div class="modal-body">
                <div class="exam-start-panel">
                    <div class="exam-start-icon">
                        <i class="bi bi-laptop-fill"></i>
                    </div>

                    <div>
                        <h4 class="fw-bold mb-1" id="modalExamSubject">-</h4>
                        <p class="text-secondary mb-0">
                            Please make sure you are ready before starting. Your timer begins after you enter the exam console.
                        </p>
                    </div>
                </div>

                <div class="row g-3 mt-3">
                    <div class="col-md-3">
                        <div class="exam-info-box">
                            <small>Exam ID</small>
                            <strong id="modalExamId">-</strong>
                        </div>
                    </div>

                    <div class="col-md-3">
                        <div class="exam-info-box">
                            <small>Duration</small>
                            <strong id="modalExamDuration">-</strong>
                        </div>
                    </div>

                    <div class="col-md-3">
                        <div class="exam-info-box">
                            <small>Questions</small>
                            <strong id="modalExamQuestions">-</strong>
                        </div>
                    </div>

                    <div class="col-md-3">
                        <div class="exam-info-box">
                            <small>Marks</small>
                            <strong id="modalExamMarks">-</strong>
                        </div>
                    </div>
                </div>

                <div class="row g-3 mt-3">
                    <div class="col-md-4">
                        <div class="exam-info-box">
                            <small>MCQ Questions</small>
                            <strong id="modalExamMcq">-</strong>
                        </div>
                    </div>

                    <div class="col-md-4">
                        <div class="exam-info-box">
                            <small>Essay Questions</small>
                            <strong id="modalExamEssay">-</strong>
                        </div>
                    </div>

                    <div class="col-md-4">
                        <div class="exam-info-box">
                            <small>Result Mode</small>
                            <strong id="modalExamMode">-</strong>
                        </div>
                    </div>
                </div>

                <div class="alert alert-warning mt-4 mb-0">
                    <strong>Before you start:</strong>
                    Do not refresh the page or close the browser during the exam. Submit only after reviewing your answers.
                </div>
            </div>

            <div class="modal-footer">
                <button class="btn btn-outline-secondary" type="button" data-bs-dismiss="modal">
                    Cancel
                </button>

                <a href="#" id="confirmStartExamBtn" class="btn btn-primary">
                    <i class="bi bi-play-circle-fill me-2"></i>
                    Continue to Rules
                </a>
            </div>

        </div>
    </div>
</div>

<script>
    document.addEventListener("DOMContentLoaded", function () {
        const startExamModal = document.getElementById("startExamModal");
        const confirmStartExamBtn = document.getElementById("confirmStartExamBtn");

        if (startExamModal) {
            startExamModal.addEventListener("show.bs.modal", function (event) {
                const button = event.relatedTarget;

                if (!button) {
                    return;
                }

                const examId = button.getAttribute("data-exam-id") || "";
                const subject = button.getAttribute("data-exam-subject") || "-";
                const duration = button.getAttribute("data-exam-duration") || "-";
                const questions = button.getAttribute("data-exam-questions") || "-";
                const marks = button.getAttribute("data-exam-marks") || "-";
                const mcq = button.getAttribute("data-exam-mcq") || "-";
                const essay = button.getAttribute("data-exam-essay") || "-";
                const mode = button.getAttribute("data-exam-mode") || "-";

                document.getElementById("modalExamId").textContent = examId;
                document.getElementById("modalExamSubject").textContent = subject;
                document.getElementById("modalExamDuration").textContent = duration;
                document.getElementById("modalExamQuestions").textContent = questions;
                document.getElementById("modalExamMarks").textContent = marks;
                document.getElementById("modalExamMcq").textContent = mcq;
                document.getElementById("modalExamEssay").textContent = essay;
                document.getElementById("modalExamMode").textContent = mode;

                if (confirmStartExamBtn) {
                    confirmStartExamBtn.href = "<%= request.getContextPath() %>/exam-rules?examId=" + encodeURIComponent(examId);
                }
            });
        }
    });
</script>

<%@ include file="../includes/footer.jsp" %>