<%--
    Exam Rules Agreement Page.
    Students must read and accept exam rules before entering the secure exam console.

    Responsible Member:
    IT25103045 - De Silva H.L.D.C.P.C
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ page import="lk.nextexam.dao.FileUtil" %>
<%@ page import="lk.nextexam.model.Exam" %>

<%
    String pageTitle = "Exam Rules";
    String activeMenu = "my-exams";
    String topbarTitle = "Exam Rules Agreement";

    Exam exam = (Exam) request.getAttribute("exam");

    String studentId = request.getAttribute("studentId") != null
            ? request.getAttribute("studentId").toString()
            : "";

    String studentName = request.getAttribute("studentName") != null
            ? request.getAttribute("studentName").toString()
            : "Student";

    int questionCount = request.getAttribute("questionCount") != null
            ? (Integer) request.getAttribute("questionCount")
            : 0;

    int mcqQuestionCount = request.getAttribute("mcqQuestionCount") != null
            ? (Integer) request.getAttribute("mcqQuestionCount")
            : 0;

    int essayQuestionCount = request.getAttribute("essayQuestionCount") != null
            ? (Integer) request.getAttribute("essayQuestionCount")
            : 0;

    double totalMarks = request.getAttribute("totalMarks") != null
            ? (Double) request.getAttribute("totalMarks")
            : 0.0;

    boolean requiresManualReview = request.getAttribute("requiresManualReview") != null
            && (Boolean) request.getAttribute("requiresManualReview");

    String marksDisplay = totalMarks == Math.floor(totalMarks)
            ? String.valueOf((int) totalMarks)
            : String.format("%.2f", totalMarks);

    String error = request.getParameter("error");
    String alertMessage = "";

    if ("agreementRequired".equalsIgnoreCase(error)) {
        alertMessage = "Please accept the exam rules before entering the exam console.";
    }
%>

<%@ include file="../includes/head.jsp" %>

<div class="app-shell">
    <%@ include file="../includes/sidebar.jsp" %>

    <main class="main-content">
        <%@ include file="../includes/topbar.jsp" %>

        <section class="page-wrapper">

            <% if (exam == null) { %>
                <div class="app-card p-5 text-center">
                    <div class="empty-state-icon">
                        <i class="bi bi-exclamation-triangle-fill"></i>
                    </div>

                    <h3 class="fw-bold">Exam rules cannot be loaded</h3>

                    <p class="text-secondary mb-4">
                        The selected exam could not be found.
                    </p>

                    <a href="<%= request.getContextPath() %>/my-exams" class="btn btn-primary">
                        <i class="bi bi-arrow-left me-2"></i>
                        Back to My Exams
                    </a>
                </div>
            <% } else { %>

                <div class="exam-rules-hero mb-4">
                    <div class="exam-rules-hero-content">
                        <span class="badge badge-soft-danger mb-3">
                            <i class="bi bi-shield-lock-fill me-1"></i>
                            Secure Exam Agreement
                        </span>

                        <h1>Read and Accept Exam Rules</h1>

                        <p>
                            Before entering the secure exam console, you must confirm that you understand and agree to the examination rules.
                        </p>
                    </div>

                    <div class="exam-rules-hero-icon">
                        <i class="bi bi-file-earmark-lock2-fill"></i>
                    </div>
                </div>

                <% if (!alertMessage.isEmpty()) { %>
                    <div class="alert alert-danger" data-auto-close="5500">
                        <i class="bi bi-exclamation-triangle-fill me-1"></i>
                        <%= FileUtil.h(alertMessage) %>
                    </div>
                <% } %>

                <div class="row g-4">
                    <div class="col-xl-8">
                        <div class="app-card p-4 h-100">
                            <div class="d-flex justify-content-between align-items-start flex-wrap gap-3 mb-4">
                                <div>
                                    <h4 class="fw-bold mb-1">Exam Integrity Rules</h4>
                                    <p class="text-secondary mb-0">
                                        These rules help maintain a fair online examination environment.
                                    </p>
                                </div>

                                <span class="badge badge-soft-primary">
                                    <i class="bi bi-person-check-fill me-1"></i>
                                    Student Agreement
                                </span>
                            </div>

                            <div class="exam-rules-agreement-list">
                                <div class="exam-rules-agreement-item danger">
                                    <i class="bi bi-window-x"></i>
                                    <div>
                                        <strong>No tab switching</strong>
                                        <p>Do not switch to another browser tab, app, or window during the exam.</p>
                                    </div>
                                </div>

                                <div class="exam-rules-agreement-item danger">
                                    <i class="bi bi-clipboard-x"></i>
                                    <div>
                                        <strong>No copy or paste</strong>
                                        <p>Copy, paste, right-click, and similar actions may be blocked and logged.</p>
                                    </div>
                                </div>

                                <div class="exam-rules-agreement-item warning">
                                    <i class="bi bi-fullscreen-exit"></i>
                                    <div>
                                        <strong>Stay in fullscreen mode</strong>
                                        <p>If fullscreen mode is enabled, exiting fullscreen may be recorded as an integrity event.</p>
                                    </div>
                                </div>

                                <div class="exam-rules-agreement-item info">
                                    <i class="bi bi-wifi"></i>
                                    <div>
                                        <strong>Use a stable connection</strong>
                                        <p>Make sure your device is charged and your internet connection is stable before starting.</p>
                                    </div>
                                </div>

                                <div class="exam-rules-agreement-item info">
                                    <i class="bi bi-send-check-fill"></i>
                                    <div>
                                        <strong>Only one final submission</strong>
                                        <p>After final submission, you cannot reattempt or edit this exam attempt.</p>
                                    </div>
                                </div>

                                <div class="exam-rules-agreement-item success">
                                    <i class="bi bi-shield-check"></i>
                                    <div>
                                        <strong>Integrity monitoring is active</strong>
                                        <p>Suspicious activity may be stored in the exam integrity log for staff review.</p>
                                    </div>
                                </div>
                            </div>

                            <form action="<%= request.getContextPath() %>/exam-rules" method="post" class="mt-4" id="examRulesForm">
                                <input type="hidden" name="examId" value="<%= FileUtil.h(exam.getExamId()) %>">

                                <label class="exam-rules-confirm-box">
                                    <input type="checkbox" name="agreement" value="accepted" id="agreementCheckbox">

                                    <span>
                                        I have read and understood the exam rules. I agree to follow the online examination integrity policy.
                                    </span>
                                </label>

                                <div class="d-flex justify-content-between align-items-center flex-wrap gap-3 mt-4">
                                    <a href="<%= request.getContextPath() %>/my-exams" class="btn btn-outline-secondary">
                                        <i class="bi bi-arrow-left me-2"></i>
                                        Back to My Exams
                                    </a>

                                    <button type="submit" class="btn btn-primary" id="enterExamBtn" disabled>
                                        <i class="bi bi-shield-check me-2"></i>
                                        Accept Rules & Enter Exam
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>

                    <div class="col-xl-4">
                        <div class="app-card p-4 h-100">
                            <div class="d-flex justify-content-between align-items-start gap-3 mb-3">
                                <div>
                                    <h4 class="fw-bold mb-1">Exam Summary</h4>
                                    <p class="text-secondary mb-0">
                                        Confirm details before starting.
                                    </p>
                                </div>

                                <span class="badge <%= requiresManualReview ? "badge-soft-warning" : "badge-soft-success" %>">
                                    <%= requiresManualReview ? "Manual Review" : "Auto Marked" %>
                                </span>
                            </div>

                            <div class="exam-info-box mb-3">
                                <small>Exam ID</small>
                                <strong><%= FileUtil.h(exam.getExamId()) %></strong>
                            </div>

                            <div class="exam-info-box mb-3">
                                <small>Subject</small>
                                <strong><%= FileUtil.h(exam.getSubject()) %></strong>
                            </div>

                            <div class="exam-info-box mb-3">
                                <small>Candidate</small>
                                <strong><%= FileUtil.h(studentName) %></strong>
                            </div>

                            <div class="exam-info-box mb-3">
                                <small>Student ID</small>
                                <strong><%= FileUtil.h(studentId) %></strong>
                            </div>

                            <div class="row g-2 mb-3">
                                <div class="col-6">
                                    <div class="exam-info-box">
                                        <small>Questions</small>
                                        <strong><%= questionCount %></strong>
                                    </div>
                                </div>

                                <div class="col-6">
                                    <div class="exam-info-box">
                                        <small>Marks</small>
                                        <strong><%= marksDisplay %></strong>
                                    </div>
                                </div>
                            </div>

                            <div class="row g-2 mb-3">
                                <div class="col-6">
                                    <div class="exam-info-box">
                                        <small>MCQ</small>
                                        <strong><%= mcqQuestionCount %></strong>
                                    </div>
                                </div>

                                <div class="col-6">
                                    <div class="exam-info-box">
                                        <small>Essay</small>
                                        <strong><%= essayQuestionCount %></strong>
                                    </div>
                                </div>
                            </div>

                            <div class="exam-info-box mb-3">
                                <small>Duration</small>
                                <strong><%= FileUtil.h(exam.getDisplayDuration()) %></strong>
                            </div>

                            <div class="alert alert-warning mb-0">
                                <strong>Important:</strong>
                                The exam timer starts after you enter the secure exam console.
                            </div>
                        </div>
                    </div>
                </div>

            <% } %>

        </section>
    </main>
</div>

<script>
    document.addEventListener("DOMContentLoaded", function () {
        const agreementCheckbox = document.getElementById("agreementCheckbox");
        const enterExamBtn = document.getElementById("enterExamBtn");

        if (agreementCheckbox && enterExamBtn) {
            agreementCheckbox.addEventListener("change", function () {
                enterExamBtn.disabled = !agreementCheckbox.checked;
            });
        }
    });
</script>

<%@ include file="../includes/footer.jsp" %>