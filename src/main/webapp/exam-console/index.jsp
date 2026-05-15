<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="lk.nextexam.dao.FileUtil" %>
<%@ page import="lk.nextexam.model.Exam" %>
<%@ page import="lk.nextexam.model.Question" %>

<%
    String pageTitle = "Secure Exam Console";
    String activeMenu = "my-exams";
    String topbarTitle = "Secure Exam Console";

    Exam exam = (Exam) request.getAttribute("exam");
    List<Question> questions = (List<Question>) request.getAttribute("questions");

    String studentId = request.getAttribute("studentId") != null
            ? request.getAttribute("studentId").toString()
            : "";

    String studentName = request.getAttribute("studentName") != null
            ? request.getAttribute("studentName").toString()
            : "Student";

    double totalMarks = request.getAttribute("totalMarks") != null
            ? (Double) request.getAttribute("totalMarks")
            : 0.0;

    int questionCount = request.getAttribute("questionCount") != null
            ? (Integer) request.getAttribute("questionCount")
            : (questions != null ? questions.size() : 0);

    int mcqQuestionCount = request.getAttribute("mcqQuestionCount") != null
            ? (Integer) request.getAttribute("mcqQuestionCount")
            : 0;

    int essayQuestionCount = request.getAttribute("essayQuestionCount") != null
            ? (Integer) request.getAttribute("essayQuestionCount")
            : 0;

    double mcqMarks = request.getAttribute("mcqMarks") != null
            ? (Double) request.getAttribute("mcqMarks")
            : 0.0;

    double essayMarks = request.getAttribute("essayMarks") != null
            ? (Double) request.getAttribute("essayMarks")
            : 0.0;

    boolean requiresManualReview = request.getAttribute("requiresManualReview") != null
            && (Boolean) request.getAttribute("requiresManualReview");

    String readinessMessage = request.getAttribute("readinessMessage") != null
            ? request.getAttribute("readinessMessage").toString()
            : "";

    int durationMinutes = exam != null ? exam.getDurationMinutes() : 60;

    String totalMarksDisplay = totalMarks == Math.floor(totalMarks)
            ? String.valueOf((int) totalMarks)
            : String.format("%.2f", totalMarks);

    String mcqMarksDisplay = mcqMarks == Math.floor(mcqMarks)
            ? String.valueOf((int) mcqMarks)
            : String.format("%.2f", mcqMarks);

    String essayMarksDisplay = essayMarks == Math.floor(essayMarks)
            ? String.valueOf((int) essayMarks)
            : String.format("%.2f", essayMarks);
%>

<%@ include file="../includes/head.jsp" %>

<div class="exam-pro-shell">

    <!-- Exam Integrity Warning Banner -->
    <div class="exam-integrity-banner" id="integrityBanner">
        <div class="exam-integrity-icon">
            <i class="bi bi-shield-exclamation"></i>
        </div>

        <div>
            <strong>Exam Integrity Warning</strong>
            <p id="integrityMessage">Suspicious exam action detected.</p>
        </div>
    </div>

    <header class="exam-pro-header">
        <div class="exam-pro-brand">
            <div class="exam-pro-logo">
                <i class="bi bi-mortarboard-fill"></i>
            </div>

            <div>
                <div class="exam-pro-brand-title">NextExamLK</div>
                <div class="exam-pro-brand-subtitle">Secure Examination Console</div>
            </div>
        </div>

        <div class="exam-pro-title-block">
            <div class="exam-pro-title">
                <%= exam != null ? FileUtil.h(exam.getSubject()) : "Online Exam" %>
            </div>

            <div class="exam-pro-meta">
                Candidate: <strong><%= FileUtil.h(studentName) %></strong>
                <% if (!studentId.isEmpty()) { %>
                    <span>·</span> Student ID: <strong><%= FileUtil.h(studentId) %></strong>
                <% } %>
            </div>
        </div>

        <div class="exam-pro-header-actions">
            <button type="button" class="exam-integrity-fullscreen-btn" id="fullscreenBtn">
                <i class="bi bi-fullscreen"></i>
                <span>Fullscreen</span>
            </button>

            <div class="exam-pro-timer" id="timerBox">
                <i class="bi bi-clock-fill"></i>
                <span id="examTimer">--:--:--</span>
            </div>
        </div>
    </header>

    <% if (exam == null || questions == null || questions.isEmpty()) { %>

        <main class="exam-pro-empty">
            <div class="app-card p-5 text-center">
                <div class="empty-state-icon">
                    <i class="bi bi-exclamation-triangle-fill"></i>
                </div>

                <h3 class="fw-bold">Exam cannot be loaded</h3>

                <p class="text-secondary mb-4">
                    The selected exam is unavailable or does not have student-visible questions.
                </p>

                <a href="<%= request.getContextPath() %>/my-exams" class="btn btn-primary">
                    <i class="bi bi-arrow-left me-2"></i>
                    Back to My Exams
                </a>
            </div>
        </main>

    <% } else { %>

        <form id="examForm"
              method="post"
              action="<%= request.getContextPath() %>/submit-exam"
              autocomplete="off">

            <input type="hidden" name="examId" value="<%= FileUtil.h(exam.getExamId()) %>">

            <main class="exam-pro-layout">

                <section class="exam-pro-main">

                    <div class="exam-pro-hero">
                        <div>
                            <span class="badge badge-soft-primary mb-2">
                                <i class="bi bi-shield-lock-fill me-1"></i>
                                Secure Exam Session
                            </span>

                            <% if (requiresManualReview) { %>
                                <span class="badge badge-soft-warning mb-2">
                                    <i class="bi bi-pencil-square me-1"></i>
                                    Manual Review Required
                                </span>
                            <% } else { %>
                                <span class="badge badge-soft-success mb-2">
                                    <i class="bi bi-lightning-charge-fill me-1"></i>
                                    Auto-Marked MCQ Exam
                                </span>
                            <% } %>

                            <h1><%= FileUtil.h(exam.getSubject()) %></h1>

                            <p>
                                Answer each question carefully. Use the question palette to move between questions,
                                review unanswered items, and check marked questions before final submission.
                            </p>
                        </div>

                        <div class="exam-pro-hero-actions">
                            <button type="button"
                                    class="btn btn-light"
                                    data-bs-toggle="modal"
                                    data-bs-target="#exitConfirmModal">
                                <i class="bi bi-arrow-left me-1"></i>
                                Exit
                            </button>

                            <button type="button"
                                    class="btn btn-danger"
                                    data-bs-toggle="modal"
                                    data-bs-target="#submitConfirmModal">
                                <i class="bi bi-send-check-fill me-1"></i>
                                Submit
                            </button>
                        </div>
                    </div>

                    <div class="exam-pro-stats">
                        <div class="exam-pro-stat">
                            <small>Exam ID</small>
                            <strong><%= FileUtil.h(exam.getExamId()) %></strong>
                        </div>

                        <div class="exam-pro-stat">
                            <small>Questions</small>
                            <strong><%= questionCount %></strong>
                        </div>

                        <div class="exam-pro-stat">
                            <small>Total Marks</small>
                            <strong><%= totalMarksDisplay %></strong>
                        </div>

                        <div class="exam-pro-stat">
                            <small>Duration</small>
                            <strong><%= FileUtil.h(exam.getDisplayDuration()) %></strong>
                        </div>
                    </div>

                    <div class="exam-pro-progress-card">
                        <div class="d-flex justify-content-between align-items-center mb-2 flex-wrap gap-2">
                            <span class="fw-bold">Exam Progress</span>
                            <span class="fw-bold text-primary" id="progressText">Question 1 of <%= questionCount %></span>
                        </div>

                        <div class="exam-pro-progress-track">
                            <div class="exam-pro-progress-fill" id="progressFill"></div>
                        </div>
                    </div>

                    <% if (requiresManualReview) { %>
                        <div class="alert alert-warning">
                            <i class="bi bi-info-circle-fill me-1"></i>
                            This exam contains essay/structured questions. MCQ answers will be auto-marked,
                            while written answers may require lecturer review before final result publication.
                        </div>
                    <% } %>

                    <% if (!readinessMessage.isEmpty() && !"OK".equalsIgnoreCase(readinessMessage)) { %>
                        <div class="alert alert-info">
                            <i class="bi bi-info-circle-fill me-1"></i>
                            <%= FileUtil.h(readinessMessage) %>
                        </div>
                    <% } %>

                    <div class="exam-pro-question-area">

                        <%
                            int index = 0;
                            for (Question question : questions) {
                                index++;
                                boolean isFirst = index == 1;
                                String questionId = question.getQuestionId();
                        %>

                        <article class="exam-pro-question-slide <%= isFirst ? "active" : "" %>"
                                 data-question-index="<%= index %>"
                                 data-question-id="<%= FileUtil.h(questionId) %>"
                                 data-question-type="<%= FileUtil.h(question.getQuestionType()) %>">

                            <div class="exam-pro-question-card">

                                <div class="exam-pro-question-header">
                                    <div>
                                        <span class="badge badge-soft-primary">
                                            Question <%= index %> of <%= questionCount %>
                                        </span>

                                        <span class="badge <%= question.getTypeBadgeClass() %>">
                                            <%= FileUtil.h(question.getQuestionType()) %>
                                        </span>
                                    </div>

                                    <span class="exam-pro-mark">
                                        <i class="bi bi-award-fill me-1"></i>
                                        <%= FileUtil.h(question.getDisplayMarks()) %> marks
                                    </span>
                                </div>

                                <div class="exam-pro-question-body">
                                    <div class="exam-pro-question-number">
                                        <%= index < 10 ? "0" + index : index %>
                                    </div>

                                    <div class="exam-pro-question-content">
                                        <h3>Read the question carefully</h3>
                                        <p><%= FileUtil.h(question.getQuestionText()) %></p>
                                    </div>
                                </div>

                                <% if (question.isMcq()) { %>

                                    <div class="exam-pro-options">

                                        <label class="exam-pro-option">
                                            <input type="radio"
                                                   name="answer_<%= FileUtil.h(questionId) %>"
                                                   value="A"
                                                   data-question-id="<%= FileUtil.h(questionId) %>">
                                            <span class="exam-pro-option-letter">A</span>
                                            <span class="exam-pro-option-text"><%= FileUtil.h(question.getOptionA()) %></span>
                                        </label>

                                        <label class="exam-pro-option">
                                            <input type="radio"
                                                   name="answer_<%= FileUtil.h(questionId) %>"
                                                   value="B"
                                                   data-question-id="<%= FileUtil.h(questionId) %>">
                                            <span class="exam-pro-option-letter">B</span>
                                            <span class="exam-pro-option-text"><%= FileUtil.h(question.getOptionB()) %></span>
                                        </label>

                                        <label class="exam-pro-option">
                                            <input type="radio"
                                                   name="answer_<%= FileUtil.h(questionId) %>"
                                                   value="C"
                                                   data-question-id="<%= FileUtil.h(questionId) %>">
                                            <span class="exam-pro-option-letter">C</span>
                                            <span class="exam-pro-option-text"><%= FileUtil.h(question.getOptionC()) %></span>
                                        </label>

                                        <label class="exam-pro-option">
                                            <input type="radio"
                                                   name="answer_<%= FileUtil.h(questionId) %>"
                                                   value="D"
                                                   data-question-id="<%= FileUtil.h(questionId) %>">
                                            <span class="exam-pro-option-letter">D</span>
                                            <span class="exam-pro-option-text"><%= FileUtil.h(question.getOptionD()) %></span>
                                        </label>

                                    </div>

                                <% } else { %>

                                    <div class="exam-pro-essay">
                                        <label class="form-label">
                                            Your Answer
                                            <span class="text-secondary fw-normal">(written response)</span>
                                        </label>

                                        <textarea class="form-control"
                                                  name="answer_<%= FileUtil.h(questionId) %>"
                                                  data-question-id="<%= FileUtil.h(questionId) %>"
                                                  rows="10"
                                                  maxlength="3000"
                                                  placeholder="Type your structured answer here..."></textarea>

                                        <div class="d-flex justify-content-between align-items-center mt-2 flex-wrap gap-2">
                                            <small class="text-secondary">
                                                Your answer will be reviewed manually by the examiner.
                                            </small>
                                            <small class="text-secondary">
                                                <span class="essay-count" data-count-for="<%= FileUtil.h(questionId) %>">0</span>/3000 characters
                                            </small>
                                        </div>
                                    </div>

                                <% } %>

                                <div class="exam-pro-question-footer">
                                    <label class="exam-pro-review">
                                        <input type="checkbox"
                                               name="flagged_<%= FileUtil.h(questionId) %>"
                                               data-flag-question="<%= FileUtil.h(questionId) %>">
                                        <span>
                                            <i class="bi bi-flag-fill me-1"></i>
                                            Mark for Review
                                        </span>
                                    </label>

                                    <button class="btn btn-light"
                                            type="button"
                                            onclick="clearAnswer('<%= FileUtil.h(questionId) %>')">
                                        <i class="bi bi-eraser me-1"></i>
                                        Clear Answer
                                    </button>
                                </div>

                            </div>

                        </article>

                        <% } %>

                    </div>

                    <div class="exam-pro-bottom-nav">
                        <button type="button" class="btn btn-outline-primary" id="prevBtn">
                            <i class="bi bi-arrow-left me-1"></i>
                            Previous
                        </button>

                        <div class="exam-pro-save-state">
                            <i class="bi bi-shield-check"></i>
                            Your final answers are recorded only after submission
                        </div>

                        <button type="button" class="btn btn-primary" id="nextBtn">
                            Next
                            <i class="bi bi-arrow-right ms-1"></i>
                        </button>
                    </div>

                </section>

                <aside class="exam-pro-sidebar">

                    <div class="exam-pro-side-card">
                        <h5>Question Palette</h5>
                        <p>Jump to any question and track your attempt status.</p>

                        <div class="exam-pro-palette">
                            <%
                                int paletteIndex = 0;
                                for (Question question : questions) {
                                    paletteIndex++;
                            %>
                            <button type="button"
                                    class="exam-pro-palette-btn <%= paletteIndex == 1 ? "current" : "" %>"
                                    data-go-question="<%= paletteIndex %>"
                                    data-question-id="<%= FileUtil.h(question.getQuestionId()) %>">
                                <%= paletteIndex %>
                            </button>
                            <% } %>
                        </div>

                        <div class="exam-pro-legend">
                            <div><span class="legend-dot current"></span> Current</div>
                            <div><span class="legend-dot answered"></span> Answered</div>
                            <div><span class="legend-dot flagged"></span> Review</div>
                            <div><span class="legend-dot empty"></span> Unanswered</div>
                        </div>
                    </div>

                    <div class="exam-pro-side-card">
                        <h5>Attempt Summary</h5>

                        <div class="exam-pro-summary-row">
                            <span>Answered</span>
                            <strong id="answeredCount">0</strong>
                        </div>

                        <div class="exam-pro-summary-row">
                            <span>Unanswered</span>
                            <strong id="unansweredCount"><%= questionCount %></strong>
                        </div>

                        <div class="exam-pro-summary-row">
                            <span>Marked Review</span>
                            <strong id="flaggedCount">0</strong>
                        </div>

                        <div class="exam-pro-summary-row">
                            <span>Total Questions</span>
                            <strong><%= questionCount %></strong>
                        </div>
                    </div>

                    <div class="exam-integrity-mini-card">
                        <div>
                            <small>Integrity Warnings</small>
                            <strong id="integrityWarningCount">0</strong>
                        </div>
                        <i class="bi bi-shield-exclamation"></i>
                    </div>

                    <div class="exam-pro-side-card">
                        <h5>Exam Breakdown</h5>

                        <div class="exam-pro-summary-row">
                            <span>MCQ Questions</span>
                            <strong><%= mcqQuestionCount %></strong>
                        </div>

                        <div class="exam-pro-summary-row">
                            <span>Essay Questions</span>
                            <strong><%= essayQuestionCount %></strong>
                        </div>

                        <div class="exam-pro-summary-row">
                            <span>MCQ Marks</span>
                            <strong><%= mcqMarksDisplay %></strong>
                        </div>

                        <div class="exam-pro-summary-row">
                            <span>Essay Marks</span>
                            <strong><%= essayMarksDisplay %></strong>
                        </div>
                    </div>

                    <div class="exam-pro-side-card exam-pro-warning-card">
                        <h5>Final Submission</h5>
                        <p>
                            Submit only after reviewing all unanswered and marked questions.
                        </p>

                        <button type="button"
                                class="btn btn-danger w-100"
                                data-bs-toggle="modal"
                                data-bs-target="#submitConfirmModal">
                            <i class="bi bi-send-check-fill me-2"></i>
                            Submit Exam
                        </button>
                    </div>

                </aside>

            </main>

            <div class="modal fade" id="submitConfirmModal" tabindex="-1" aria-labelledby="submitConfirmModalTitle" aria-hidden="true">
                <div class="modal-dialog modal-lg modal-dialog-centered">
                    <div class="modal-content border-0 shadow-lg">

                        <div class="modal-header">
                            <div>
                                <h5 class="modal-title fw-bold" id="submitConfirmModalTitle">Confirm Final Submission</h5>
                                <small class="text-secondary">
                                    Once submitted, you cannot attempt this exam again.
                                </small>
                            </div>

                            <button class="btn-close" type="button" data-bs-dismiss="modal" aria-label="Close"></button>
                        </div>

                        <div class="modal-body">
                            <div class="exam-pro-submit-warning">
                                <div class="exam-pro-submit-icon">
                                    <i class="bi bi-exclamation-triangle-fill"></i>
                                </div>

                                <div>
                                    <h4>Are you sure you want to submit?</h4>
                                    <p>
                                        Your final answers will be saved immediately. You cannot edit this attempt after submission.
                                    </p>
                                </div>
                            </div>

                            <div class="row g-3 mt-3">
                                <div class="col-md-3">
                                    <div class="exam-info-box">
                                        <small>Answered</small>
                                        <strong id="modalAnsweredCount">0</strong>
                                    </div>
                                </div>

                                <div class="col-md-3">
                                    <div class="exam-info-box">
                                        <small>Unanswered</small>
                                        <strong id="modalUnansweredCount"><%= questionCount %></strong>
                                    </div>
                                </div>

                                <div class="col-md-3">
                                    <div class="exam-info-box">
                                        <small>Review</small>
                                        <strong id="modalFlaggedCount">0</strong>
                                    </div>
                                </div>

                                <div class="col-md-3">
                                    <div class="exam-info-box">
                                        <small>Total</small>
                                        <strong><%= questionCount %></strong>
                                    </div>
                                </div>
                            </div>

                            <% if (requiresManualReview) { %>
                                <div class="alert alert-info mt-4 mb-0">
                                    <strong>Note:</strong>
                                    This exam contains essay questions. Your final result may be published after manual marking.
                                </div>
                            <% } else { %>
                                <div class="alert alert-info mt-4 mb-0">
                                    <strong>Note:</strong>
                                    This MCQ exam will be auto-marked after submission.
                                </div>
                            <% } %>
                        </div>

                        <div class="modal-footer">
                            <button class="btn btn-outline-secondary" type="button" data-bs-dismiss="modal">
                                Continue Exam
                            </button>

                            <button class="btn btn-danger" type="submit" id="finalSubmitBtn">
                                <i class="bi bi-send-check-fill me-2"></i>
                                Submit Final Answers
                            </button>
                        </div>

                    </div>
                </div>
            </div>

            <div class="modal fade" id="exitConfirmModal" tabindex="-1" aria-labelledby="exitConfirmModalTitle" aria-hidden="true">
                <div class="modal-dialog modal-dialog-centered">
                    <div class="modal-content border-0 shadow-lg">

                        <div class="modal-header">
                            <div>
                                <h5 class="modal-title fw-bold" id="exitConfirmModalTitle">Exit Exam Console?</h5>
                                <small class="text-secondary">
                                    Unsaved answers will not be submitted.
                                </small>
                            </div>

                            <button class="btn-close" type="button" data-bs-dismiss="modal" aria-label="Close"></button>
                        </div>

                        <div class="modal-body">
                            <div class="alert alert-warning mb-0">
                                <strong>Warning:</strong>
                                If you exit now, your current answers will not be recorded as a final submission.
                            </div>
                        </div>

                        <div class="modal-footer">
                            <button class="btn btn-outline-secondary" type="button" data-bs-dismiss="modal">
                                Stay in Exam
                            </button>

                            <a href="<%= request.getContextPath() %>/my-exams" class="btn btn-danger" id="confirmExitBtn">
                                Exit Without Submitting
                            </a>
                        </div>

                    </div>
                </div>
            </div>

        </form>

    <% } %>

</div>

<script>
    document.addEventListener("DOMContentLoaded", function () {
        const totalQuestions = <%= questionCount %>;
        const durationSeconds = <%= durationMinutes %> * 60;
        const contextPath = "<%= request.getContextPath() %>";
        const examId = "<%= exam != null ? FileUtil.h(exam.getExamId()) : "" %>";

        let currentQuestion = 1;
        let remainingSeconds = durationSeconds;
        let isSubmitting = false;
        let integrityWarnings = 0;
        let lastVisibilityWarningAt = 0;
        let lastFullscreenWarningAt = 0;

        const slides = document.querySelectorAll(".exam-pro-question-slide");
        const paletteButtons = document.querySelectorAll(".exam-pro-palette-btn");
        const prevBtn = document.getElementById("prevBtn");
        const nextBtn = document.getElementById("nextBtn");
        const progressText = document.getElementById("progressText");
        const progressFill = document.getElementById("progressFill");
        const timer = document.getElementById("examTimer");
        const timerBox = document.getElementById("timerBox");
        const examForm = document.getElementById("examForm");
        const finalSubmitBtn = document.getElementById("finalSubmitBtn");
        const confirmExitBtn = document.getElementById("confirmExitBtn");

        const answeredCountEl = document.getElementById("answeredCount");
        const unansweredCountEl = document.getElementById("unansweredCount");
        const flaggedCountEl = document.getElementById("flaggedCount");

        const modalAnsweredCount = document.getElementById("modalAnsweredCount");
        const modalUnansweredCount = document.getElementById("modalUnansweredCount");
        const modalFlaggedCount = document.getElementById("modalFlaggedCount");

        const integrityBanner = document.getElementById("integrityBanner");
        const integrityMessage = document.getElementById("integrityMessage");
        const integrityWarningCount = document.getElementById("integrityWarningCount");
        const fullscreenBtn = document.getElementById("fullscreenBtn");

        function formatTime(seconds) {
            const safeSeconds = Math.max(0, seconds);
            const h = Math.floor(safeSeconds / 3600);
            const m = Math.floor((safeSeconds % 3600) / 60);
            const s = safeSeconds % 60;

            return String(h).padStart(2, "0") + ":" +
                String(m).padStart(2, "0") + ":" +
                String(s).padStart(2, "0");
        }

        function updateTimer() {
            if (!timer) return;

            timer.textContent = formatTime(remainingSeconds);

            if (remainingSeconds <= 300 && timerBox) {
                timerBox.classList.add("danger");
            }

            if (remainingSeconds <= 0) {
                submitExamAutomatically();
                return;
            }

            remainingSeconds--;
        }

        function submitExamAutomatically() {
            if (!examForm || isSubmitting) return;

            isSubmitting = true;
            window.onbeforeunload = null;

            logIntegrityEvent("EXAM_SUBMITTED", "Exam auto-submitted because timer ended");

            if (finalSubmitBtn) {
                finalSubmitBtn.disabled = true;
                finalSubmitBtn.innerHTML = "<i class='bi bi-hourglass-split me-2'></i>Submitting...";
            }

            examForm.submit();
        }

        function showQuestion(index) {
            if (index < 1 || index > totalQuestions) return;

            currentQuestion = index;

            slides.forEach(function (slide) {
                const slideIndex = parseInt(slide.getAttribute("data-question-index"), 10);
                slide.classList.toggle("active", slideIndex === currentQuestion);
            });

            paletteButtons.forEach(function (button) {
                const buttonIndex = parseInt(button.getAttribute("data-go-question"), 10);
                button.classList.toggle("current", buttonIndex === currentQuestion);
            });

            if (progressText) {
                progressText.textContent = "Question " + currentQuestion + " of " + totalQuestions;
            }

            if (progressFill) {
                progressFill.style.width = ((currentQuestion / totalQuestions) * 100) + "%";
            }

            if (prevBtn) {
                prevBtn.disabled = currentQuestion === 1;
            }

            if (nextBtn) {
                if (currentQuestion === totalQuestions) {
                    nextBtn.innerHTML = "Review Summary <i class='bi bi-list-check ms-1'></i>";
                } else {
                    nextBtn.innerHTML = "Next <i class='bi bi-arrow-right ms-1'></i>";
                }
            }

            window.scrollTo({ top: 0, behavior: "smooth" });
        }

        function isQuestionAnswered(questionId) {
            const radios = document.querySelectorAll("input[type='radio'][name='answer_" + questionId + "']");
            const textarea = document.querySelector("textarea[name='answer_" + questionId + "']");

            if (radios.length > 0) {
                for (let i = 0; i < radios.length; i++) {
                    if (radios[i].checked) {
                        return true;
                    }
                }
            }

            if (textarea) {
                return textarea.value.trim().length > 0;
            }

            return false;
        }

        function updateSelectedOptions() {
            document.querySelectorAll(".exam-pro-option").forEach(function (option) {
                const input = option.querySelector("input[type='radio']");
                option.classList.toggle("selected", input && input.checked);
            });
        }

        function updateEssayCounters() {
            document.querySelectorAll("textarea[data-question-id]").forEach(function (textarea) {
                const questionId = textarea.getAttribute("data-question-id");
                const counter = document.querySelector(".essay-count[data-count-for='" + questionId + "']");

                if (counter) {
                    counter.textContent = textarea.value.length;
                }
            });
        }

        function updateSummary() {
            let answered = 0;
            let flagged = 0;

            paletteButtons.forEach(function (button) {
                const questionId = button.getAttribute("data-question-id");
                const flagInput = document.querySelector("input[data-flag-question='" + questionId + "']");
                const isAnswered = isQuestionAnswered(questionId);
                const isFlagged = flagInput && flagInput.checked;

                button.classList.toggle("answered", isAnswered);
                button.classList.toggle("flagged", isFlagged);

                if (isAnswered) answered++;
                if (isFlagged) flagged++;
            });

            const unanswered = totalQuestions - answered;

            if (answeredCountEl) answeredCountEl.textContent = answered;
            if (unansweredCountEl) unansweredCountEl.textContent = unanswered;
            if (flaggedCountEl) flaggedCountEl.textContent = flagged;

            if (modalAnsweredCount) modalAnsweredCount.textContent = answered;
            if (modalUnansweredCount) modalUnansweredCount.textContent = unanswered;
            if (modalFlaggedCount) modalFlaggedCount.textContent = flagged;

            updateSelectedOptions();
            updateEssayCounters();
        }

        function showIntegrityWarning(message) {
            integrityWarnings++;

            if (integrityWarningCount) {
                integrityWarningCount.textContent = integrityWarnings;
            }

            if (integrityMessage) {
                integrityMessage.textContent = message;
            }

            if (integrityBanner) {
                integrityBanner.classList.add("show");
                window.setTimeout(function () {
                    integrityBanner.classList.remove("show");
                }, 4500);
            }
        }

        function logIntegrityEvent(eventType, description) {
            if (!examId || (isSubmitting && eventType !== "EXAM_SUBMITTED")) {
                return;
            }

            const body = new URLSearchParams();
            body.append("examId", examId);
            body.append("eventType", eventType);
            body.append("description", description);

            fetch(contextPath + "/exam-integrity", {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8"
                },
                body: body.toString(),
                keepalive: true
            }).catch(function () {
                // Silent fail: exam should not stop if integrity logging request fails.
            });
        }

        function requestFullscreenMode() {
            const root = document.documentElement;

            if (root.requestFullscreen) {
                root.requestFullscreen().then(function () {
                    logIntegrityEvent("FULLSCREEN_REQUESTED", "Student entered fullscreen exam mode");
                }).catch(function () {
                    showIntegrityWarning("Fullscreen mode could not be started. Continue carefully.");
                });
            }
        }

        window.clearAnswer = function (questionId) {
            const radios = document.querySelectorAll("input[type='radio'][name='answer_" + questionId + "']");
            const textarea = document.querySelector("textarea[name='answer_" + questionId + "']");

            radios.forEach(function (radio) {
                radio.checked = false;
            });

            if (textarea) {
                textarea.value = "";
            }

            updateSummary();
        };

        paletteButtons.forEach(function (button) {
            button.addEventListener("click", function () {
                const index = parseInt(button.getAttribute("data-go-question"), 10);
                showQuestion(index);
            });
        });

        document.querySelectorAll("input[type='radio'], textarea, input[type='checkbox']").forEach(function (input) {
            input.addEventListener("change", updateSummary);
            input.addEventListener("input", updateSummary);
        });

        if (prevBtn) {
            prevBtn.addEventListener("click", function () {
                showQuestion(currentQuestion - 1);
            });
        }

        if (nextBtn) {
            nextBtn.addEventListener("click", function () {
                if (currentQuestion < totalQuestions) {
                    showQuestion(currentQuestion + 1);
                } else {
                    const sidePanel = document.querySelector(".exam-pro-sidebar");
                    if (sidePanel) {
                        sidePanel.scrollIntoView({ behavior: "smooth", block: "start" });
                    }
                }
            });
        }

        if (fullscreenBtn) {
            fullscreenBtn.addEventListener("click", requestFullscreenMode);
        }

        document.addEventListener("visibilitychange", function () {
            const now = Date.now();

            if (document.hidden && !isSubmitting && now - lastVisibilityWarningAt > 2500) {
                lastVisibilityWarningAt = now;
                showIntegrityWarning("Warning recorded: you switched away from the exam tab.");
                logIntegrityEvent("TAB_SWITCH", "Student switched away from the exam tab/window");
            }
        });

        document.addEventListener("contextmenu", function (event) {
            event.preventDefault();
            showIntegrityWarning("Right-click is disabled during the exam.");
            logIntegrityEvent("RIGHT_CLICK_BLOCKED", "Student attempted to use right-click during exam");
        });

        document.addEventListener("copy", function (event) {
            event.preventDefault();
            showIntegrityWarning("Copy action is disabled during the exam.");
            logIntegrityEvent("COPY_BLOCKED", "Student attempted to copy content during exam");
        });

        document.addEventListener("paste", function (event) {
            event.preventDefault();
            showIntegrityWarning("Paste action is disabled during the exam.");
            logIntegrityEvent("PASTE_BLOCKED", "Student attempted to paste content during exam");
        });

        document.addEventListener("fullscreenchange", function () {
            const now = Date.now();

            if (!document.fullscreenElement && !isSubmitting && now - lastFullscreenWarningAt > 2500) {
                lastFullscreenWarningAt = now;
                showIntegrityWarning("Warning recorded: fullscreen mode was exited.");
                logIntegrityEvent("FULLSCREEN_EXIT", "Student exited fullscreen mode during exam");
            }
        });

        if (examForm) {
            examForm.addEventListener("submit", function (event) {
                if (isSubmitting) {
                    event.preventDefault();
                    return;
                }

                isSubmitting = true;
                window.onbeforeunload = null;

                logIntegrityEvent("EXAM_SUBMITTED", "Student clicked final submit button");

                if (finalSubmitBtn) {
                    finalSubmitBtn.disabled = true;
                    finalSubmitBtn.innerHTML = "<i class='bi bi-hourglass-split me-2'></i>Submitting...";
                }
            });
        }

        if (confirmExitBtn) {
            confirmExitBtn.addEventListener("click", function () {
                window.onbeforeunload = null;
            });
        }

        window.onbeforeunload = function () {
            if (!isSubmitting) {
                return "Your exam has not been submitted yet. Are you sure you want to leave?";
            }
        };

        logIntegrityEvent("EXAM_STARTED", "Student started the secure exam console");

        updateTimer();
        setInterval(updateTimer, 1000);
        showQuestion(1);
        updateSummary();
    });
</script>

<%@ include file="../includes/footer.jsp" %>