<%--
    Manual Essay Marking Page.
    Allows staff users to review essay answers, enter marks, and provide feedback.

    Responsible Member:
    IT25103045 - De Silva H.L.D.C.P.C
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ page import="java.util.List" %>
<%@ page import="lk.nextexam.dao.FileUtil" %>
<%@ page import="lk.nextexam.model.ExamSubmission" %>
<%@ page import="lk.nextexam.model.Question" %>
<%@ page import="lk.nextexam.model.ManualMark" %>

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

    private ManualMark findExistingMark(List<ManualMark> marks, String questionId) {
        if (marks == null || questionId == null) {
            return null;
        }

        for (ManualMark mark : marks) {
            if (mark != null && mark.getQuestionId().equalsIgnoreCase(questionId)) {
                return mark;
            }
        }

        return null;
    }
%>

<%
    String pageTitle = "Manual Marking";
    String activeMenu = "submissions";
    String topbarTitle = "Manual Essay Marking";

    ExamSubmission submission = (ExamSubmission) request.getAttribute("submission");
    List<Question> essayQuestions = (List<Question>) request.getAttribute("essayQuestions");
    List<ManualMark> existingManualMarks = (List<ManualMark>) request.getAttribute("existingManualMarks");

    double mcqScore = request.getAttribute("mcqScore") != null
            ? (Double) request.getAttribute("mcqScore")
            : 0.0;

    String success = request.getParameter("success");
    String error = request.getParameter("error");

    String alertType = "";
    String alertMessage = "";

    if ("manualMarkingSaved".equalsIgnoreCase(success)) {
        alertType = "success";
        alertMessage = "Manual essay marks were saved successfully and the submission status was updated to Marked.";
    }

    if ("missingMarks".equalsIgnoreCase(error)) {
        alertType = "danger";
        alertMessage = "Please enter marks for every essay question.";
    } else if ("invalidMarks".equalsIgnoreCase(error)) {
        alertType = "danger";
        alertMessage = "Marks must be valid numeric values.";
    } else if ("marksOutOfRange".equalsIgnoreCase(error)) {
        alertType = "danger";
        alertMessage = "Awarded marks cannot be lower than 0 or higher than the question marks.";
    } else if ("markSaveFailed".equalsIgnoreCase(error)) {
        alertType = "danger";
        alertMessage = "Manual marks could not be saved. Please try again.";
    } else if ("submissionUpdateFailed".equalsIgnoreCase(error)) {
        alertType = "danger";
        alertMessage = "Marks were saved, but the submission score could not be updated.";
    } else if ("finalizedSubmission".equalsIgnoreCase(error)) {
        alertType = "danger";
        alertMessage = "This submission is already finalized and cannot be manually marked.";
    }
%>

<%@ include file="../includes/head.jsp" %>

<div class="app-shell">
    <%@ include file="../includes/sidebar.jsp" %>

    <main class="main-content">
        <%@ include file="../includes/topbar.jsp" %>

        <section class="page-wrapper">

            <% if (submission == null || essayQuestions == null || essayQuestions.isEmpty()) { %>

                <div class="app-card p-5 text-center">
                    <div class="empty-state-icon">
                        <i class="bi bi-exclamation-triangle-fill"></i>
                    </div>

                    <h3 class="fw-bold">Manual marking cannot be loaded</h3>

                    <p class="text-secondary mb-4">
                        The selected submission was not found or does not contain essay questions.
                    </p>

                    <a href="<%= request.getContextPath() %>/submissions" class="btn btn-primary">
                        <i class="bi bi-arrow-left me-2"></i>
                        Back to Submissions
                    </a>
                </div>

            <% } else { %>

                <div class="hero-card mb-4">
                    <div class="d-flex justify-content-between align-items-start flex-wrap gap-3">
                        <div>
                            <span class="badge badge-soft-warning mb-3">
                                <i class="bi bi-pencil-square me-1"></i>
                                Essay Manual Marking
                            </span>

                            <h1 class="hero-title">Manual Essay Marking</h1>

                            <p class="hero-text">
                                Review essay answers, compare with the model answer, enter awarded marks,
                                and provide lecturer feedback.
                            </p>
                        </div>

                        <div class="d-flex gap-2 flex-wrap">
                            <a href="<%= request.getContextPath() %>/submissions" class="btn btn-outline-primary">
                                <i class="bi bi-arrow-left me-2"></i>
                                Submissions
                            </a>

                            <button type="submit" form="manualMarkingForm" class="btn btn-primary">
                                <i class="bi bi-check2-circle me-2"></i>
                                Save Marks
                            </button>
                        </div>
                    </div>
                </div>

                <% if (!alertMessage.isEmpty()) { %>
                    <div class="alert alert-<%= FileUtil.h(alertType) %>">
                        <i class="bi <%= "success".equals(alertType) ? "bi-check-circle-fill" : "bi-exclamation-triangle-fill" %> me-1"></i>
                        <%= FileUtil.h(alertMessage) %>
                    </div>
                <% } %>

                <div class="row g-3 mb-4">
                    <div class="col-md-6 col-xl-3">
                        <div class="app-card stat-card">
                            <div class="stat-label">Submission</div>
                            <div class="stat-value fs-5"><%= FileUtil.h(submission.getSubmissionId()) %></div>
                            <div class="stat-meta"><%= FileUtil.h(submission.getDisplaySubmittedAt()) %></div>
                        </div>
                    </div>

                    <div class="col-md-6 col-xl-3">
                        <div class="app-card stat-card">
                            <div class="stat-label">Student</div>
                            <div class="stat-value fs-5"><%= FileUtil.h(submission.getStudentId()) %></div>
                            <div class="stat-meta"><%= FileUtil.h(submission.getStudentName()) %></div>
                        </div>
                    </div>

                    <div class="col-md-6 col-xl-3">
                        <div class="app-card stat-card">
                            <div class="stat-label">Current Score</div>
                            <div class="stat-value"><%= FileUtil.h(submission.getScoreSummary()) %></div>
                            <div class="stat-meta">Before/after manual marking</div>
                        </div>
                    </div>

                    <div class="col-md-6 col-xl-3">
                        <div class="app-card stat-card">
                            <div class="stat-label">MCQ Score</div>
                            <div class="stat-value"><%= mcqScore == Math.floor(mcqScore) ? String.valueOf((int) mcqScore) : String.format("%.2f", mcqScore) %></div>
                            <div class="stat-meta">Calculated from MCQ answers</div>
                        </div>
                    </div>
                </div>

                <form id="manualMarkingForm"
                      method="post"
                      action="<%= request.getContextPath() %>/manual-marking"
                      autocomplete="off">

                    <input type="hidden" name="submissionId" value="<%= FileUtil.h(submission.getSubmissionId()) %>">

                    <div class="manual-marking-stack">

                        <%
                            int questionNumber = 0;
                            for (Question question : essayQuestions) {
                                questionNumber++;
                                String answer = extractSubmittedAnswer(submission.getAnswersData(), question.getQuestionId());
                                ManualMark existingMark = findExistingMark(existingManualMarks, question.getQuestionId());
                                String existingMarksValue = existingMark != null ? existingMark.getMarksAwardedDisplay() : "";
                                String existingFeedback = existingMark != null ? existingMark.getFeedback() : "";
                        %>

                        <div class="app-card manual-mark-card">
                            <div class="manual-mark-head">
                                <div>
                                    <span class="badge badge-soft-primary">
                                        Question <%= questionNumber %>
                                    </span>

                                    <span class="badge badge-soft-warning">
                                        Essay
                                    </span>
                                </div>

                                <span class="manual-mark-total">
                                    Max: <%= FileUtil.h(question.getDisplayMarks()) %> marks
                                </span>
                            </div>

                            <div class="manual-mark-question">
                                <strong><%= FileUtil.h(question.getQuestionText()) %></strong>
                            </div>

                            <div class="row g-3">
                                <div class="col-lg-6">
                                    <div class="manual-answer-panel student">
                                        <small>Student Answer</small>

                                        <p>
                                            <%= answer.isEmpty() ? "No answer submitted." : FileUtil.h(answer) %>
                                        </p>
                                    </div>
                                </div>

                                <div class="col-lg-6">
                                    <div class="manual-answer-panel model">
                                        <small>Model Answer / Marking Guide</small>

                                        <p>
                                            <%= question.getModelAnswer().isEmpty() ? "No model answer saved." : FileUtil.h(question.getModelAnswer()) %>
                                        </p>
                                    </div>
                                </div>
                            </div>

                            <div class="manual-mark-inputs">
                                <div>
                                    <label class="form-label">
                                        Awarded Marks
                                        <span class="text-secondary fw-normal">
                                            / <%= FileUtil.h(question.getDisplayMarks()) %>
                                        </span>
                                    </label>

                                    <input type="number"
                                           class="form-control manual-mark-input"
                                           name="marks_<%= FileUtil.h(question.getQuestionId()) %>"
                                           min="0"
                                           max="<%= FileUtil.h(question.getDisplayMarks()) %>"
                                           step="0.5"
                                           value="<%= FileUtil.h(existingMarksValue) %>"
                                           required>
                                </div>

                                <div>
                                    <label class="form-label">
                                        Lecturer Feedback
                                    </label>

                                    <textarea class="form-control"
                                              name="feedback_<%= FileUtil.h(question.getQuestionId()) %>"
                                              rows="3"
                                              maxlength="500"
                                              placeholder="Write short feedback for this answer..."><%= FileUtil.h(existingFeedback) %></textarea>
                                </div>
                            </div>
                        </div>

                        <% } %>

                    </div>

                    <div class="app-card p-4 mt-4">
                        <div class="d-flex justify-content-between align-items-center flex-wrap gap-3">
                            <div>
                                <h5 class="fw-bold mb-1">Finalize Manual Marking</h5>
                                <p class="text-secondary mb-0">
                                    Saving marks will update this submission status to <strong>Marked</strong>.
                                </p>
                            </div>

                            <button type="submit" class="btn btn-primary">
                                <i class="bi bi-check2-circle me-2"></i>
                                Save Manual Marks
                            </button>
                        </div>
                    </div>

                </form>

            <% } %>

        </section>
    </main>
</div>

<%@ include file="../includes/footer.jsp" %>