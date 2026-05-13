<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="lk.nextexam.dao.FileUtil" %>
<%@ page import="lk.nextexam.model.Question" %>
<%@ page import="lk.nextexam.dao.QuestionDAO" %>

<%
    String pageTitle = "Questions";
    String activeMenu = "questions";
    String topbarTitle = "Question Bank";

    List<Question> questions = (List<Question>) request.getAttribute("questions");

    String selectedExamId = request.getAttribute("selectedExamId") != null
            ? request.getAttribute("selectedExamId").toString()
            : "";

    if (questions == null) {
        QuestionDAO questionDAO = new QuestionDAO();

        if (selectedExamId != null && !selectedExamId.trim().isEmpty()) {
            questions = questionDAO.getQuestionsByExamId(application, selectedExamId);
        } else {
            questions = questionDAO.getAllQuestions(application);
        }
    }

    int totalQuestions = questions != null ? questions.size() : 0;
    int activeQuestions = 0;
    int publishedQuestions = 0;
    int studentVisibleQuestions = 0;
    int draftQuestions = 0;
    int inactiveQuestions = 0;
    int archivedQuestions = 0;
    int mcqQuestions = 0;
    int essayQuestions = 0;
    double totalMarks = 0.0;
    double studentVisibleMarks = 0.0;

    if (questions != null) {
        for (Question question : questions) {
            if (question.isMcq()) {
                mcqQuestions++;
            }

            if (question.isEssay()) {
                essayQuestions++;
            }

            if (question.isActive()) {
                activeQuestions++;
            } else if (question.isPublished()) {
                publishedQuestions++;
            } else if (question.isDraft()) {
                draftQuestions++;
            } else if (question.isInactive()) {
                inactiveQuestions++;
            } else if (question.isArchived()) {
                archivedQuestions++;
            }

            if (question.isVisibleToStudent()) {
                studentVisibleQuestions++;
                studentVisibleMarks += question.getMarksAsDouble();
            }

            totalMarks += question.getMarksAsDouble();
        }
    }

    String totalMarksDisplay = totalMarks == Math.floor(totalMarks)
            ? String.valueOf((int) totalMarks)
            : String.format("%.2f", totalMarks);

    String visibleMarksDisplay = studentVisibleMarks == Math.floor(studentVisibleMarks)
            ? String.valueOf((int) studentVisibleMarks)
            : String.format("%.2f", studentVisibleMarks);

    String success = request.getParameter("success");
    String error = request.getParameter("error");

    String alertType = "";
    String alertMessage = "";

    if (success != null) {
        alertType = "success";

        if ("questionAdded".equalsIgnoreCase(success)) {
            alertMessage = "Question created successfully.";
        } else if ("questionUpdated".equalsIgnoreCase(success)) {
            alertMessage = "Question updated successfully.";
        } else if ("questionDeleted".equalsIgnoreCase(success)) {
            alertMessage = "Question deleted successfully.";
        } else {
            alertMessage = "Operation completed successfully.";
        }
    }

    if (error != null) {
        alertType = "danger";

        if ("missingQuestionId".equalsIgnoreCase(error)) {
            alertMessage = "Question ID is missing.";
        } else if ("missingExamId".equalsIgnoreCase(error)) {
            alertMessage = "Exam ID is missing.";
        } else if ("missingQuestionType".equalsIgnoreCase(error)) {
            alertMessage = "Question type is missing.";
        } else if ("invalidQuestionType".equalsIgnoreCase(error)) {
            alertMessage = "Invalid question type selected.";
        } else if ("missingQuestionText".equalsIgnoreCase(error)) {
            alertMessage = "Question text is required.";
        } else if ("invalidMarks".equalsIgnoreCase(error)) {
            alertMessage = "Invalid marks value. Marks must be between 0.5 and 100.";
        } else if ("invalidStatus".equalsIgnoreCase(error)) {
            alertMessage = "Invalid question status selected.";
        } else if ("missingCorrectAnswer".equalsIgnoreCase(error)) {
            alertMessage = "Correct answer is required for MCQ questions.";
        } else if ("invalidCorrectAnswer".equalsIgnoreCase(error)) {
            alertMessage = "Correct answer must be A, B, C, or D.";
        } else if ("missingModelAnswer".equalsIgnoreCase(error)) {
            alertMessage = "Model answer is required for essay questions.";
        } else if ("questionAddFailed".equalsIgnoreCase(error)) {
            alertMessage = "Question could not be created. Check duplicate Question ID or incomplete fields.";
        } else if ("questionUpdateFailed".equalsIgnoreCase(error)) {
            alertMessage = "Question could not be updated.";
        } else if ("questionDeleteFailed".equalsIgnoreCase(error)) {
            alertMessage = "Question could not be deleted. Archived questions are protected.";
        } else {
            alertMessage = "Something went wrong. Please check the question details and try again.";
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
                            <i class="bi bi-patch-question-fill me-1"></i>
                            NextExamLK Question Bank Center
                        </span>

                        <h1 class="hero-title">Question Management</h1>

                        <p class="hero-text">
                            Create, organize, validate, and activate MCQ or essay questions for online examinations.
                            <strong>Active</strong> and <strong>Published</strong> questions are student-visible.
                            <% if (selectedExamId != null && !selectedExamId.trim().isEmpty()) { %>
                                <br>
                                <strong>Currently managing questions for exam:</strong>
                                <%= FileUtil.h(selectedExamId) %>
                            <% } %>
                        </p>
                    </div>

                    <div class="d-flex gap-2 flex-wrap">
                        <button class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#questionModal">
                            <i class="bi bi-plus-lg me-2"></i>
                            Add Question
                        </button>

                        <a href="<%= request.getContextPath() %>/exams" class="btn btn-outline-primary">
                            <i class="bi bi-journal-check me-2"></i>
                            View Exams
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
                                <div class="stat-label">Total Questions</div>
                                <div class="stat-value"><%= totalQuestions %></div>
                                <div class="stat-meta">Stored question records</div>
                            </div>

                            <div class="stat-icon">
                                <i class="bi bi-patch-question-fill"></i>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-md-6 col-xl-3">
                    <div class="app-card stat-card">
                        <div class="d-flex justify-content-between gap-3">
                            <div>
                                <div class="stat-label">Student Visible</div>
                                <div class="stat-value"><%= studentVisibleQuestions %></div>
                                <div class="stat-meta">Active + published questions</div>
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
                                <div class="stat-label">MCQ / Essay</div>
                                <div class="stat-value"><%= mcqQuestions %>/<%= essayQuestions %></div>
                                <div class="stat-meta">Auto and manual marking mix</div>
                            </div>

                            <div class="stat-icon">
                                <i class="bi bi-ui-checks-grid"></i>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-md-6 col-xl-3">
                    <div class="app-card stat-card">
                        <div class="d-flex justify-content-between gap-3">
                            <div>
                                <div class="stat-label">Visible Marks</div>
                                <div class="stat-value"><%= visibleMarksDisplay %></div>
                                <div class="stat-meta"><%= totalMarksDisplay %> total marks listed</div>
                            </div>

                            <div class="stat-icon">
                                <i class="bi bi-award-fill"></i>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row g-4 mb-4">

                <div class="col-xl-5">
                    <div class="app-card form-card h-100 p-4">
                        <div class="d-flex justify-content-between align-items-start gap-3 mb-3">
                            <div>
                                <h4 class="fw-bold mb-1">Quick Question Builder</h4>
                                <p class="text-secondary mb-0">
                                    Add a question directly to the selected exam.
                                </p>
                            </div>

                            <span class="badge badge-soft-secondary">Draft First</span>
                        </div>

                        <form class="needs-validation"
                              novalidate
                              action="<%= request.getContextPath() %>/questions"
                              method="post">

                            <input type="hidden" name="action" value="add">

                            <div class="row g-3">
                                <div class="col-md-6">
                                    <label class="form-label">Question ID <span class="required">*</span></label>
                                    <input type="text"
                                           name="questionId"
                                           class="form-control"
                                           placeholder="Example: Q004"
                                           maxlength="30"
                                           required>
                                    <div class="invalid-feedback">Question ID is required.</div>
                                </div>

                                <div class="col-md-6">
                                    <label class="form-label">Exam ID <span class="required">*</span></label>
                                    <input type="text"
                                           name="examId"
                                           class="form-control"
                                           value="<%= FileUtil.h(selectedExamId) %>"
                                           placeholder="Example: EX001"
                                           maxlength="30"
                                           required>
                                    <div class="invalid-feedback">Exam ID is required.</div>
                                </div>

                                <div class="col-md-6">
                                    <label class="form-label">Question Type <span class="required">*</span></label>
                                    <select name="questionType"
                                            class="form-select question-type-selector"
                                            data-target-prefix="quick"
                                            required>
                                        <option value="">Choose type</option>
                                        <option value="MCQ">MCQ</option>
                                        <option value="Essay">Essay</option>
                                    </select>
                                    <div class="invalid-feedback">Question type is required.</div>
                                </div>

                                <div class="col-md-6">
                                    <label class="form-label">Marks <span class="required">*</span></label>
                                    <input type="number"
                                           name="marks"
                                           class="form-control"
                                           placeholder="Example: 5"
                                           min="0.5"
                                           max="100"
                                           step="0.5"
                                           required>
                                    <div class="invalid-feedback">Marks are required.</div>
                                </div>

                                <div class="col-12">
                                    <label class="form-label">Question Text <span class="required">*</span></label>
                                    <textarea name="questionText"
                                              class="form-control"
                                              rows="4"
                                              maxlength="2000"
                                              placeholder="Enter the question clearly..."
                                              required></textarea>
                                    <div class="invalid-feedback">Question text is required.</div>
                                </div>

                                <div class="col-md-6 quick-mcq-field">
                                    <label class="form-label">Option A</label>
                                    <input type="text" name="optionA" class="form-control" maxlength="500" placeholder="Option A">
                                </div>

                                <div class="col-md-6 quick-mcq-field">
                                    <label class="form-label">Option B</label>
                                    <input type="text" name="optionB" class="form-control" maxlength="500" placeholder="Option B">
                                </div>

                                <div class="col-md-6 quick-mcq-field">
                                    <label class="form-label">Option C</label>
                                    <input type="text" name="optionC" class="form-control" maxlength="500" placeholder="Option C">
                                </div>

                                <div class="col-md-6 quick-mcq-field">
                                    <label class="form-label">Option D</label>
                                    <input type="text" name="optionD" class="form-control" maxlength="500" placeholder="Option D">
                                </div>

                                <div class="col-md-6 quick-mcq-field">
                                    <label class="form-label">Correct Answer</label>
                                    <select name="correctAnswer" class="form-select">
                                        <option value="">Choose answer</option>
                                        <option value="A">A</option>
                                        <option value="B">B</option>
                                        <option value="C">C</option>
                                        <option value="D">D</option>
                                    </select>
                                </div>

                                <div class="col-12 quick-essay-field">
                                    <label class="form-label">Model Answer / Marking Guide</label>
                                    <textarea name="modelAnswer"
                                              class="form-control"
                                              rows="3"
                                              maxlength="2000"
                                              placeholder="Write expected answer points, explanation, or marking guide..."></textarea>
                                </div>

                                <div class="col-md-6">
                                    <label class="form-label">Status <span class="required">*</span></label>
                                    <select name="status" class="form-select" required>
                                        <option value="">Choose status</option>
                                        <option value="Draft">Draft</option>
                                        <option value="Active">Active</option>
                                        <option value="Published">Published</option>
                                        <option value="Inactive">Inactive</option>
                                        <option value="Archived">Archived</option>
                                    </select>
                                    <div class="invalid-feedback">Question status is required.</div>
                                </div>

                                <div class="col-12">
                                    <div class="alert alert-info mb-0">
                                        <strong>Student visibility:</strong>
                                        Set status to <strong>Active</strong> or <strong>Published</strong>
                                        only when this question is ready for students.
                                    </div>
                                </div>
                            </div>

                            <div class="d-flex gap-2 mt-4 flex-wrap">
                                <button class="btn btn-primary flex-grow-1" type="submit">
                                    <i class="bi bi-save me-2"></i>
                                    Save Question
                                </button>

                                <button class="btn btn-outline-secondary" type="reset">
                                    <i class="bi bi-arrow-clockwise me-1"></i>
                                    Reset
                                </button>
                            </div>
                        </form>
                    </div>
                </div>

                <div class="col-xl-7">
                    <div class="app-card p-4 h-100">
                        <div class="d-flex justify-content-between align-items-start flex-wrap gap-3 mb-4">
                            <div>
                                <span class="badge badge-soft-primary mb-2">
                                    <i class="bi bi-eye-fill me-1"></i>
                                    Student Question Preview
                                </span>

                                <h4 class="fw-bold mb-1">Online Exam Layout Preview</h4>

                                <p class="text-secondary mb-0">
                                    This preview shows the professional question layout used inside the student exam console.
                                </p>
                            </div>

                            <span class="badge badge-soft-success">
                                5 Marks
                            </span>
                        </div>

                        <div class="exam-info-box mb-3">
                            <small>Sample Question</small>
                            <strong>
                                Which Object Oriented Programming concept allows a class to inherit
                                properties and methods from another class?
                            </strong>
                        </div>

                        <div class="d-grid gap-2">
                            <div class="exam-info-box">
                                <small>Option A</small>
                                <strong>Encapsulation</strong>
                            </div>

                            <div class="exam-info-box" style="border-color:#bbf7d0;background:#f0fdf4;">
                                <small>Option B</small>
                                <strong>Inheritance</strong>
                            </div>

                            <div class="exam-info-box">
                                <small>Option C</small>
                                <strong>Polymorphism</strong>
                            </div>

                            <div class="exam-info-box">
                                <small>Option D</small>
                                <strong>File Handling</strong>
                            </div>
                        </div>

                        <div class="alert alert-info mt-4 mb-0">
                            <strong>Next step:</strong>
                            Student-visible questions will appear in the secure exam console after the exam is available.
                        </div>
                    </div>
                </div>
            </div>

            <div class="page-header">
                <div>
                    <h2 class="page-title">Question Bank Records</h2>

                    <p class="page-description">
                        Search, filter, edit, review, archive, and delete questions prepared for online examinations.
                    </p>
                </div>

                <button class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#questionModal">
                    <i class="bi bi-plus-lg me-2"></i>
                    Add Question
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
                               id="questionSearch"
                               placeholder="Search by question ID, exam ID, type, text, marks, or status">
                    </div>

                    <div class="d-flex gap-2 flex-wrap">
                        <select class="form-select" id="questionTypeFilter" style="width: 160px;">
                            <option value="">All Types</option>
                            <option value="mcq">MCQ</option>
                            <option value="essay">Essay</option>
                        </select>

                        <select class="form-select" id="questionStatusFilter" style="width: 170px;">
                            <option value="">All Status</option>
                            <option value="draft">Draft</option>
                            <option value="active">Active</option>
                            <option value="published">Published</option>
                            <option value="inactive">Inactive</option>
                            <option value="archived">Archived</option>
                        </select>

                        <button class="btn btn-outline-secondary" type="button" id="clearQuestionFiltersBtn">
                            <i class="bi bi-x-circle me-1"></i>
                            Clear
                        </button>

                        <% if (selectedExamId != null && !selectedExamId.trim().isEmpty()) { %>
                            <a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/questions">
                                <i class="bi bi-x-circle me-1"></i>
                                Clear Exam
                            </a>
                        <% } %>
                    </div>
                </div>

                <div class="table-responsive">
                    <table class="table table-hover align-middle" id="questionsTable">
                        <thead>
                        <tr>
                            <th>Question ID</th>
                            <th>Exam ID</th>
                            <th>Type</th>
                            <th>Question Preview</th>
                            <th>Marks</th>
                            <th>Status</th>
                            <th>Visibility</th>
                            <th class="text-end">Actions</th>
                        </tr>
                        </thead>

                        <tbody>
                        <% if (questions != null && !questions.isEmpty()) {
                            for (Question question : questions) {
                                String questionId = question.getQuestionId();
                                String visibilityLabel = question.isVisibleToStudent() ? "Student Visible" : "Hidden";
                                String visibilityClass = question.isVisibleToStudent() ? "badge-soft-success" : "badge-soft-secondary";
                        %>
                            <tr data-type="<%= FileUtil.h(question.getQuestionType().toLowerCase()) %>"
                                data-status="<%= FileUtil.h(question.getStatus().toLowerCase()) %>">
                                <td class="fw-bold"><%= FileUtil.h(questionId) %></td>
                                <td><%= FileUtil.h(question.getExamId()) %></td>

                                <td>
                                    <span class="badge <%= question.getTypeBadgeClass() %>">
                                        <%= FileUtil.h(question.getQuestionType()) %>
                                    </span>
                                </td>

                                <td>
                                    <div class="fw-bold"><%= FileUtil.h(question.getShortQuestionText()) %></div>
                                    <small class="text-secondary">
                                        <%= question.isMcq() ? "Auto-marked MCQ question" : "Manual review essay question" %>
                                    </small>
                                </td>

                                <td><%= FileUtil.h(question.getDisplayMarks()) %></td>

                                <td>
                                    <span class="badge <%= question.getStatusBadgeClass() %>">
                                        <%= FileUtil.h(question.getStatus()) %>
                                    </span>
                                </td>

                                <td>
                                    <span class="badge <%= visibilityClass %>">
                                        <%= visibilityLabel %>
                                    </span>
                                </td>

                                <td>
                                    <div class="action-group">
                                        <button class="btn btn-sm btn-outline-primary view-question-btn"
                                                type="button"
                                                title="View Question"
                                                data-bs-toggle="modal"
                                                data-bs-target="#viewQuestionModal"
                                                data-question-id="<%= FileUtil.h(questionId) %>"
                                                data-exam-id="<%= FileUtil.h(question.getExamId()) %>"
                                                data-question-type="<%= FileUtil.h(question.getQuestionType()) %>"
                                                data-question-text="<%= FileUtil.h(question.getQuestionText()) %>"
                                                data-option-a="<%= FileUtil.h(question.getOptionA()) %>"
                                                data-option-b="<%= FileUtil.h(question.getOptionB()) %>"
                                                data-option-c="<%= FileUtil.h(question.getOptionC()) %>"
                                                data-option-d="<%= FileUtil.h(question.getOptionD()) %>"
                                                data-correct-answer="<%= FileUtil.h(question.getCorrectAnswer()) %>"
                                                data-marks="<%= FileUtil.h(question.getMarks()) %>"
                                                data-display-marks="<%= FileUtil.h(question.getDisplayMarks()) %>"
                                                data-status="<%= FileUtil.h(question.getStatus()) %>"
                                                data-model-answer="<%= FileUtil.h(question.getModelAnswer()) %>">
                                            <i class="bi bi-eye"></i>
                                        </button>

                                        <% if (question.canEdit()) { %>
                                            <button class="btn btn-sm btn-outline-primary edit-question-btn"
                                                    type="button"
                                                    title="Edit Question"
                                                    data-bs-toggle="modal"
                                                    data-bs-target="#editQuestionModal"
                                                    data-question-id="<%= FileUtil.h(questionId) %>"
                                                    data-exam-id="<%= FileUtil.h(question.getExamId()) %>"
                                                    data-question-type="<%= FileUtil.h(question.getQuestionType()) %>"
                                                    data-question-text="<%= FileUtil.h(question.getQuestionText()) %>"
                                                    data-option-a="<%= FileUtil.h(question.getOptionA()) %>"
                                                    data-option-b="<%= FileUtil.h(question.getOptionB()) %>"
                                                    data-option-c="<%= FileUtil.h(question.getOptionC()) %>"
                                                    data-option-d="<%= FileUtil.h(question.getOptionD()) %>"
                                                    data-correct-answer="<%= FileUtil.h(question.getCorrectAnswer()) %>"
                                                    data-marks="<%= FileUtil.h(question.getMarks()) %>"
                                                    data-status="<%= FileUtil.h(question.getStatus()) %>"
                                                    data-model-answer="<%= FileUtil.h(question.getModelAnswer()) %>">
                                                <i class="bi bi-pencil-square"></i>
                                            </button>
                                        <% } else { %>
                                            <button class="btn btn-sm btn-outline-secondary"
                                                    type="button"
                                                    disabled
                                                    title="Archived questions cannot be edited">
                                                <i class="bi bi-lock-fill"></i>
                                            </button>
                                        <% } %>

                                        <button class="btn btn-sm btn-outline-danger"
                                                type="button"
                                                title="Delete Question"
                                                data-bs-toggle="modal"
                                                data-bs-target="#deleteModal"
                                                data-delete-name="<%= FileUtil.h(questionId + " - " + question.getShortQuestionText()) %>"
                                                data-delete-id="<%= FileUtil.h(questionId) %>"
                                                data-exam-id="<%= FileUtil.h(question.getExamId()) %>"
                                                data-delete-url="<%= request.getContextPath() %>/questions">
                                            <i class="bi bi-trash3"></i>
                                        </button>
                                    </div>
                                </td>
                            </tr>
                        <% }
                        } else { %>
                            <tr>
                                <td colspan="8">
                                    <div class="empty-state">
                                        <div class="empty-state-icon">
                                            <i class="bi bi-inbox"></i>
                                        </div>
                                        <h5>No question records found</h5>
                                        <p>Add a question to display records here.</p>
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

<!-- Add Question Modal -->
<div class="modal fade" id="questionModal" tabindex="-1" aria-labelledby="questionModalTitle" aria-hidden="true">
    <div class="modal-dialog modal-xl modal-dialog-centered">
        <div class="modal-content border-0 shadow-lg">

            <form class="needs-validation"
                  novalidate
                  action="<%= request.getContextPath() %>/questions"
                  method="post">

                <input type="hidden" name="action" value="add">

                <div class="modal-header">
                    <div>
                        <h5 class="modal-title fw-bold" id="questionModalTitle">Add Question Record</h5>
                        <small class="text-secondary">
                            Create a new question for the NextExamLK examination question bank.
                        </small>
                    </div>

                    <button class="btn-close" type="button" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>

                <div class="modal-body">
                    <div class="row g-3">
                        <div class="col-md-4">
                            <label class="form-label">Question ID <span class="required">*</span></label>
                            <input type="text"
                                   name="questionId"
                                   class="form-control"
                                   placeholder="Example: Q004"
                                   maxlength="30"
                                   required>
                            <div class="invalid-feedback">Question ID is required.</div>
                        </div>

                        <div class="col-md-4">
                            <label class="form-label">Exam ID <span class="required">*</span></label>
                            <input type="text"
                                   name="examId"
                                   class="form-control"
                                   value="<%= FileUtil.h(selectedExamId) %>"
                                   placeholder="Example: EX001"
                                   maxlength="30"
                                   required>
                            <div class="invalid-feedback">Exam ID is required.</div>
                        </div>

                        <div class="col-md-4">
                            <label class="form-label">Question Type <span class="required">*</span></label>
                            <select name="questionType"
                                    class="form-select question-type-selector"
                                    data-target-prefix="add"
                                    required>
                                <option value="">Choose type</option>
                                <option value="MCQ">MCQ</option>
                                <option value="Essay">Essay</option>
                            </select>
                            <div class="invalid-feedback">Question type is required.</div>
                        </div>

                        <div class="col-12">
                            <label class="form-label">Question Text <span class="required">*</span></label>
                            <textarea name="questionText"
                                      class="form-control"
                                      rows="4"
                                      maxlength="2000"
                                      placeholder="Enter question text..."
                                      required></textarea>
                            <div class="invalid-feedback">Question text is required.</div>
                        </div>

                        <div class="col-md-3 add-mcq-field">
                            <label class="form-label">Option A</label>
                            <input type="text" name="optionA" class="form-control" maxlength="500" placeholder="Option A">
                        </div>

                        <div class="col-md-3 add-mcq-field">
                            <label class="form-label">Option B</label>
                            <input type="text" name="optionB" class="form-control" maxlength="500" placeholder="Option B">
                        </div>

                        <div class="col-md-3 add-mcq-field">
                            <label class="form-label">Option C</label>
                            <input type="text" name="optionC" class="form-control" maxlength="500" placeholder="Option C">
                        </div>

                        <div class="col-md-3 add-mcq-field">
                            <label class="form-label">Option D</label>
                            <input type="text" name="optionD" class="form-control" maxlength="500" placeholder="Option D">
                        </div>

                        <div class="col-md-4 add-mcq-field">
                            <label class="form-label">Correct Answer</label>
                            <select name="correctAnswer" class="form-select">
                                <option value="">Choose answer</option>
                                <option value="A">A</option>
                                <option value="B">B</option>
                                <option value="C">C</option>
                                <option value="D">D</option>
                            </select>
                        </div>

                        <div class="col-md-4">
                            <label class="form-label">Marks <span class="required">*</span></label>
                            <input type="number"
                                   name="marks"
                                   class="form-control"
                                   min="0.5"
                                   max="100"
                                   step="0.5"
                                   placeholder="Example: 5"
                                   required>
                            <div class="invalid-feedback">Marks are required.</div>
                        </div>

                        <div class="col-md-4">
                            <label class="form-label">Status <span class="required">*</span></label>
                            <select name="status" class="form-select" required>
                                <option value="">Choose status</option>
                                <option value="Draft">Draft</option>
                                <option value="Active">Active</option>
                                <option value="Published">Published</option>
                                <option value="Inactive">Inactive</option>
                                <option value="Archived">Archived</option>
                            </select>
                            <div class="invalid-feedback">Status is required.</div>
                        </div>

                        <div class="col-12 add-essay-field">
                            <label class="form-label">Model Answer / Explanation</label>
                            <textarea name="modelAnswer"
                                      class="form-control"
                                      rows="3"
                                      maxlength="2000"
                                      placeholder="Add explanation, expected answer points, or marking guide..."></textarea>
                        </div>
                    </div>

                    <div class="alert alert-info mt-4 mb-0">
                        <strong>Student exam rule:</strong>
                        Questions are displayed in the student exam console only when their status is
                        <strong>Active</strong> or <strong>Published</strong>.
                    </div>
                </div>

                <div class="modal-footer">
                    <button class="btn btn-outline-secondary" type="button" data-bs-dismiss="modal">
                        Cancel
                    </button>

                    <button class="btn btn-primary" type="submit">
                        <i class="bi bi-save me-2"></i>
                        Save Question
                    </button>
                </div>
            </form>

        </div>
    </div>
</div>

<!-- Edit Question Modal -->
<div class="modal fade" id="editQuestionModal" tabindex="-1" aria-labelledby="editQuestionModalTitle" aria-hidden="true">
    <div class="modal-dialog modal-xl modal-dialog-centered">
        <div class="modal-content border-0 shadow-lg">

            <form class="needs-validation"
                  novalidate
                  action="<%= request.getContextPath() %>/questions"
                  method="post">

                <input type="hidden" name="action" value="update">

                <div class="modal-header">
                    <div>
                        <h5 class="modal-title fw-bold" id="editQuestionModalTitle">Edit Question</h5>
                        <small class="text-secondary">
                            Update an existing question record in the NextExamLK question bank.
                        </small>
                    </div>

                    <button class="btn-close" type="button" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>

                <div class="modal-body">
                    <div class="row g-3">

                        <div class="col-md-4">
                            <label class="form-label">Question ID</label>
                            <input type="text"
                                   id="editQuestionId"
                                   name="questionId"
                                   class="form-control"
                                   readonly
                                   required>
                            <div class="invalid-feedback">Question ID is required.</div>
                        </div>

                        <div class="col-md-4">
                            <label class="form-label">Exam ID</label>
                            <input type="text"
                                   id="editQuestionExamId"
                                   name="examId"
                                   class="form-control"
                                   maxlength="30"
                                   required>
                            <div class="invalid-feedback">Exam ID is required.</div>
                        </div>

                        <div class="col-md-4">
                            <label class="form-label">Question Type</label>
                            <select id="editQuestionType"
                                    name="questionType"
                                    class="form-select"
                                    required>
                                <option value="">Choose type</option>
                                <option value="MCQ">MCQ</option>
                                <option value="Essay">Essay</option>
                            </select>
                            <div class="invalid-feedback">Question type is required.</div>
                        </div>

                        <div class="col-12">
                            <label class="form-label">Question Text</label>
                            <textarea id="editQuestionText"
                                      name="questionText"
                                      class="form-control"
                                      rows="4"
                                      maxlength="2000"
                                      required></textarea>
                            <div class="invalid-feedback">Question text is required.</div>
                        </div>

                        <div class="col-md-3 edit-mcq-field">
                            <label class="form-label">Option A</label>
                            <input type="text" id="editOptionA" name="optionA" class="form-control" maxlength="500">
                        </div>

                        <div class="col-md-3 edit-mcq-field">
                            <label class="form-label">Option B</label>
                            <input type="text" id="editOptionB" name="optionB" class="form-control" maxlength="500">
                        </div>

                        <div class="col-md-3 edit-mcq-field">
                            <label class="form-label">Option C</label>
                            <input type="text" id="editOptionC" name="optionC" class="form-control" maxlength="500">
                        </div>

                        <div class="col-md-3 edit-mcq-field">
                            <label class="form-label">Option D</label>
                            <input type="text" id="editOptionD" name="optionD" class="form-control" maxlength="500">
                        </div>

                        <div class="col-md-4 edit-mcq-field">
                            <label class="form-label">Correct Answer</label>
                            <select id="editCorrectAnswer" name="correctAnswer" class="form-select">
                                <option value="">Choose answer</option>
                                <option value="A">A</option>
                                <option value="B">B</option>
                                <option value="C">C</option>
                                <option value="D">D</option>
                            </select>
                        </div>

                        <div class="col-md-4">
                            <label class="form-label">Marks</label>
                            <input type="number"
                                   id="editQuestionMarks"
                                   name="marks"
                                   class="form-control"
                                   min="0.5"
                                   max="100"
                                   step="0.5"
                                   required>
                            <div class="invalid-feedback">Marks are required.</div>
                        </div>

                        <div class="col-md-4">
                            <label class="form-label">Status</label>
                            <select id="editQuestionStatus" name="status" class="form-select" required>
                                <option value="">Choose status</option>
                                <option value="Draft">Draft</option>
                                <option value="Active">Active</option>
                                <option value="Published">Published</option>
                                <option value="Inactive">Inactive</option>
                                <option value="Archived">Archived</option>
                            </select>
                            <div class="invalid-feedback">Status is required.</div>
                        </div>

                        <div class="col-12 edit-essay-field">
                            <label class="form-label">Model Answer / Explanation</label>
                            <textarea id="editModelAnswer"
                                      name="modelAnswer"
                                      class="form-control"
                                      rows="3"
                                      maxlength="2000"></textarea>
                        </div>
                    </div>

                    <div class="alert alert-info mt-4 mb-0">
                        <strong>Student visibility:</strong>
                        Active and published questions appear in the online exam interface.
                    </div>
                </div>

                <div class="modal-footer">
                    <button class="btn btn-outline-secondary" type="button" data-bs-dismiss="modal">
                        Cancel
                    </button>

                    <button class="btn btn-primary" type="submit">
                        <i class="bi bi-save me-2"></i>
                        Update Question
                    </button>
                </div>
            </form>

        </div>
    </div>
</div>

<!-- View Question Modal -->
<div class="modal fade" id="viewQuestionModal" tabindex="-1" aria-labelledby="viewQuestionModalTitle" aria-hidden="true">
    <div class="modal-dialog modal-xl modal-dialog-centered">
        <div class="modal-content border-0 shadow-lg">

            <div class="modal-header">
                <div>
                    <h5 class="modal-title fw-bold" id="viewQuestionModalTitle">Question Details</h5>
                    <small class="text-secondary">
                        View selected question information from the question bank.
                    </small>
                </div>

                <button class="btn-close" type="button" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>

            <div class="modal-body">
                <div class="row g-3">

                    <div class="col-md-4">
                        <div class="exam-info-box">
                            <small>Question ID</small>
                            <strong id="viewQuestionId">-</strong>
                        </div>
                    </div>

                    <div class="col-md-4">
                        <div class="exam-info-box">
                            <small>Exam ID</small>
                            <strong id="viewQuestionExamId">-</strong>
                        </div>
                    </div>

                    <div class="col-md-4">
                        <div class="exam-info-box">
                            <small>Question Type</small>
                            <strong id="viewQuestionType">-</strong>
                        </div>
                    </div>

                    <div class="col-12">
                        <div class="exam-info-box">
                            <small>Question Text</small>
                            <strong id="viewQuestionText">-</strong>
                        </div>
                    </div>

                    <div class="col-md-3 view-mcq-field">
                        <div class="exam-info-box">
                            <small>Option A</small>
                            <strong id="viewOptionA">-</strong>
                        </div>
                    </div>

                    <div class="col-md-3 view-mcq-field">
                        <div class="exam-info-box">
                            <small>Option B</small>
                            <strong id="viewOptionB">-</strong>
                        </div>
                    </div>

                    <div class="col-md-3 view-mcq-field">
                        <div class="exam-info-box">
                            <small>Option C</small>
                            <strong id="viewOptionC">-</strong>
                        </div>
                    </div>

                    <div class="col-md-3 view-mcq-field">
                        <div class="exam-info-box">
                            <small>Option D</small>
                            <strong id="viewOptionD">-</strong>
                        </div>
                    </div>

                    <div class="col-md-4 view-mcq-field">
                        <div class="exam-info-box">
                            <small>Correct Answer</small>
                            <strong id="viewCorrectAnswer">-</strong>
                        </div>
                    </div>

                    <div class="col-md-4">
                        <div class="exam-info-box">
                            <small>Marks</small>
                            <strong id="viewQuestionMarks">-</strong>
                        </div>
                    </div>

                    <div class="col-md-4">
                        <div class="exam-info-box">
                            <small>Status</small>
                            <strong id="viewQuestionStatus">-</strong>
                        </div>
                    </div>

                    <div class="col-12">
                        <div class="exam-info-box">
                            <small>Model Answer / Explanation</small>
                            <strong id="viewModelAnswer">-</strong>
                        </div>
                    </div>

                </div>

                <div class="alert alert-info mt-4 mb-0">
                    <strong>Question record:</strong>
                    These details are loaded from the selected question and displayed for quick review.
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
        const questionSearch = document.getElementById("questionSearch");
        const questionTypeFilter = document.getElementById("questionTypeFilter");
        const questionStatusFilter = document.getElementById("questionStatusFilter");
        const clearQuestionFiltersBtn = document.getElementById("clearQuestionFiltersBtn");
        const questionRows = document.querySelectorAll("#questionsTable tbody tr[data-type]");

        function getQuestionData(button) {
            return {
                questionId: button.getAttribute("data-question-id") || "",
                examId: button.getAttribute("data-exam-id") || "",
                questionType: button.getAttribute("data-question-type") || "",
                questionText: button.getAttribute("data-question-text") || "",
                optionA: button.getAttribute("data-option-a") || "",
                optionB: button.getAttribute("data-option-b") || "",
                optionC: button.getAttribute("data-option-c") || "",
                optionD: button.getAttribute("data-option-d") || "",
                correctAnswer: button.getAttribute("data-correct-answer") || "",
                marks: button.getAttribute("data-marks") || "",
                displayMarks: button.getAttribute("data-display-marks") || "",
                status: button.getAttribute("data-status") || "",
                modelAnswer: button.getAttribute("data-model-answer") || ""
            };
        }

        function filterQuestions() {
            const searchValue = questionSearch ? questionSearch.value.toLowerCase().trim() : "";
            const typeValue = questionTypeFilter ? questionTypeFilter.value.toLowerCase().trim() : "";
            const statusValue = questionStatusFilter ? questionStatusFilter.value.toLowerCase().trim() : "";

            questionRows.forEach(function (row) {
                const rowText = row.innerText.toLowerCase();
                const rowType = row.getAttribute("data-type") || "";
                const rowStatus = row.getAttribute("data-status") || "";

                const matchesSearch = rowText.includes(searchValue);
                const matchesType = typeValue === "" || rowType === typeValue;
                const matchesStatus = statusValue === "" || rowStatus === statusValue;

                row.style.display = matchesSearch && matchesType && matchesStatus ? "" : "none";
            });
        }

        function updateTypeFields(prefix, type) {
            const mcqFields = document.querySelectorAll("." + prefix + "-mcq-field");
            const essayFields = document.querySelectorAll("." + prefix + "-essay-field");

            mcqFields.forEach(function (field) {
                const inputs = field.querySelectorAll("input, select, textarea");

                if (type === "MCQ") {
                    field.style.display = "";

                    inputs.forEach(function (input) {
                        if (input.name === "optionA" ||
                            input.name === "optionB" ||
                            input.name === "optionC" ||
                            input.name === "optionD" ||
                            input.name === "correctAnswer") {
                            input.required = true;
                        }
                    });
                } else {
                    field.style.display = "none";

                    inputs.forEach(function (input) {
                        input.required = false;
                        input.value = "";
                    });
                }
            });

            essayFields.forEach(function (field) {
                const inputs = field.querySelectorAll("textarea, input, select");

                if (type === "Essay") {
                    field.style.display = "";

                    inputs.forEach(function (input) {
                        if (input.name === "modelAnswer") {
                            input.required = true;
                        }
                    });
                } else if (type === "MCQ") {
                    field.style.display = "";

                    inputs.forEach(function (input) {
                        input.required = false;
                    });
                } else {
                    field.style.display = "none";

                    inputs.forEach(function (input) {
                        input.required = false;
                    });
                }
            });
        }

        document.querySelectorAll(".question-type-selector").forEach(function (selector) {
            selector.addEventListener("change", function () {
                const prefix = selector.getAttribute("data-target-prefix");
                updateTypeFields(prefix, selector.value);
            });

            const prefix = selector.getAttribute("data-target-prefix");
            updateTypeFields(prefix, selector.value);
        });

        const editQuestionType = document.getElementById("editQuestionType");

        if (editQuestionType) {
            editQuestionType.addEventListener("change", function () {
                updateTypeFields("edit", editQuestionType.value);
            });
        }

        document.querySelectorAll(".edit-question-btn").forEach(function (button) {
            button.addEventListener("click", function () {
                const question = getQuestionData(button);

                document.getElementById("editQuestionId").value = question.questionId;
                document.getElementById("editQuestionExamId").value = question.examId;
                document.getElementById("editQuestionType").value = question.questionType;
                document.getElementById("editQuestionText").value = question.questionText;
                document.getElementById("editOptionA").value = question.optionA;
                document.getElementById("editOptionB").value = question.optionB;
                document.getElementById("editOptionC").value = question.optionC;
                document.getElementById("editOptionD").value = question.optionD;
                document.getElementById("editCorrectAnswer").value = question.correctAnswer;
                document.getElementById("editQuestionMarks").value = question.marks;
                document.getElementById("editQuestionStatus").value = question.status;
                document.getElementById("editModelAnswer").value = question.modelAnswer;

                updateTypeFields("edit", question.questionType);
            });
        });

        document.querySelectorAll(".view-question-btn").forEach(function (button) {
            button.addEventListener("click", function () {
                const question = getQuestionData(button);

                document.getElementById("viewQuestionId").textContent = question.questionId || "-";
                document.getElementById("viewQuestionExamId").textContent = question.examId || "-";
                document.getElementById("viewQuestionType").textContent = question.questionType || "-";
                document.getElementById("viewQuestionText").textContent = question.questionText || "-";
                document.getElementById("viewOptionA").textContent = question.optionA || "-";
                document.getElementById("viewOptionB").textContent = question.optionB || "-";
                document.getElementById("viewOptionC").textContent = question.optionC || "-";
                document.getElementById("viewOptionD").textContent = question.optionD || "-";
                document.getElementById("viewCorrectAnswer").textContent = question.correctAnswer || "-";
                document.getElementById("viewQuestionMarks").textContent = question.displayMarks || question.marks || "-";
                document.getElementById("viewQuestionStatus").textContent = question.status || "-";
                document.getElementById("viewModelAnswer").textContent = question.modelAnswer || "-";

                const viewMcqFields = document.querySelectorAll(".view-mcq-field");

                if (question.questionType === "Essay") {
                    viewMcqFields.forEach(function (field) {
                        field.style.display = "none";
                    });
                } else {
                    viewMcqFields.forEach(function (field) {
                        field.style.display = "";
                    });
                }
            });
        });

        if (questionSearch) {
            questionSearch.addEventListener("input", filterQuestions);
        }

        if (questionTypeFilter) {
            questionTypeFilter.addEventListener("change", filterQuestions);
        }

        if (questionStatusFilter) {
            questionStatusFilter.addEventListener("change", filterQuestions);
        }

        if (clearQuestionFiltersBtn) {
            clearQuestionFiltersBtn.addEventListener("click", function () {
                if (questionSearch) {
                    questionSearch.value = "";
                }

                if (questionTypeFilter) {
                    questionTypeFilter.value = "";
                }

                if (questionStatusFilter) {
                    questionStatusFilter.value = "";
                }

                filterQuestions();
            });
        }

        updateTypeFields("quick", "");
        updateTypeFields("add", "");
        updateTypeFields("edit", "");
    });
</script>

<%@ include file="../includes/delete-modal.jsp" %>
<%@ include file="../includes/footer.jsp" %>