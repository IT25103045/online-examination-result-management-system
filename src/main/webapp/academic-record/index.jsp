<%--
    Student Academic Record / Transcript View.

    Responsible Member:
    IT25103045 - De Silva H.L.D.C.P.C
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="lk.nextexam.dao.FileUtil" %>
<%@ page import="lk.nextexam.model.Student" %>
<%@ page import="lk.nextexam.model.Result" %>
<%@ page import="lk.nextexam.model.Exam" %>

<%
    String pageTitle = "Academic Record";
    String activeMenu = "academic-record";
    String topbarTitle = "Academic Record";

    Student selectedStudent = (Student) request.getAttribute("selectedStudent");
    String selectedStudentId = request.getAttribute("selectedStudentId") != null
            ? request.getAttribute("selectedStudentId").toString()
            : "";

    List<Student> allStudents = (List<Student>) request.getAttribute("allStudents");
    List<Result> publishedResults = (List<Result>) request.getAttribute("publishedResults");
    Map<String, Exam> examMap = (Map<String, Exam>) request.getAttribute("examMap");

    boolean isStudentView = request.getAttribute("isStudentView") != null
            && (Boolean) request.getAttribute("isStudentView");

    boolean isStaffView = request.getAttribute("isStaffView") != null
            && (Boolean) request.getAttribute("isStaffView");

    int totalPublishedResults = request.getAttribute("totalPublishedResults") != null
            ? (Integer) request.getAttribute("totalPublishedResults")
            : 0;

    int passedCount = request.getAttribute("passedCount") != null
            ? (Integer) request.getAttribute("passedCount")
            : 0;

    int failedCount = request.getAttribute("failedCount") != null
            ? (Integer) request.getAttribute("failedCount")
            : 0;

    int gradeA = request.getAttribute("gradeA") != null ? (Integer) request.getAttribute("gradeA") : 0;
    int gradeB = request.getAttribute("gradeB") != null ? (Integer) request.getAttribute("gradeB") : 0;
    int gradeC = request.getAttribute("gradeC") != null ? (Integer) request.getAttribute("gradeC") : 0;
    int gradeS = request.getAttribute("gradeS") != null ? (Integer) request.getAttribute("gradeS") : 0;
    int gradeF = request.getAttribute("gradeF") != null ? (Integer) request.getAttribute("gradeF") : 0;

    double averageMarks = request.getAttribute("averageMarks") != null
            ? (Double) request.getAttribute("averageMarks")
            : 0.0;

    double highestMarks = request.getAttribute("highestMarks") != null
            ? (Double) request.getAttribute("highestMarks")
            : 0.0;

    String academicStanding = request.getAttribute("academicStanding") != null
            ? request.getAttribute("academicStanding").toString()
            : "No Published Results";

    String standingBadgeClass = request.getAttribute("standingBadgeClass") != null
            ? request.getAttribute("standingBadgeClass").toString()
            : "badge-soft-secondary";

    String generatedDate = request.getAttribute("generatedDate") != null
            ? request.getAttribute("generatedDate").toString()
            : "";

    String studentName = selectedStudent != null ? selectedStudent.getName() : "Not selected";
    String studentEmail = selectedStudent != null ? selectedStudent.getEmail() : "-";
    String studentCourse = selectedStudent != null ? selectedStudent.getCourse() : "-";
    String studentBatch = selectedStudent != null ? selectedStudent.getBatch() : "-";
    String studentStatus = selectedStudent != null ? selectedStudent.getExamStatus() : "-";

    String averageDisplay = String.format("%.1f%%", averageMarks);
    String highestDisplay = String.format("%.1f%%", highestMarks);
%>

<%@ include file="../includes/head.jsp" %>

<div class="app-shell">
    <%@ include file="../includes/sidebar.jsp" %>

    <main class="main-content">
        <%@ include file="../includes/topbar.jsp" %>

        <section class="page-wrapper academic-record-page">

            <div class="hero-card transcript-hero-card mb-4 no-print">
                <div class="d-flex justify-content-between align-items-start flex-wrap gap-3">
                    <div>
                        <span class="badge badge-soft-primary mb-3">
                            <i class="bi bi-file-earmark-person-fill me-1"></i>
                            Transcript View
                        </span>

                        <h1 class="hero-title">Student Academic Record</h1>

                        <p class="hero-text">
                            View published results in a transcript-style academic record with summary,
                            grades, academic standing, and printable formatting.
                        </p>
                    </div>

                    <button type="button" class="btn btn-primary" onclick="window.print()">
                        <i class="bi bi-printer-fill me-2"></i>
                        Print Academic Record
                    </button>
                </div>
            </div>

            <% if (isStaffView) { %>
                <div class="app-card p-4 mb-4 no-print">
                    <form method="get"
                          action="<%= request.getContextPath() %>/academic-record"
                          class="row g-3 align-items-end">

                        <div class="col-lg-8">
                            <label class="form-label">Select Student</label>

                            <select name="studentId" class="form-select" required>
                                <option value="">Choose student</option>

                                <% if (allStudents != null) {
                                    for (Student student : allStudents) {
                                        String selected = student.getStudentId().equalsIgnoreCase(selectedStudentId) ? "selected" : "";
                                %>
                                    <option value="<%= FileUtil.h(student.getStudentId()) %>" <%= selected %>>
                                        <%= FileUtil.h(student.getStudentId()) %> - <%= FileUtil.h(student.getName()) %>
                                    </option>
                                <% }
                                } %>
                            </select>
                        </div>

                        <div class="col-lg-4">
                            <button type="submit" class="btn btn-primary w-100">
                                <i class="bi bi-search me-2"></i>
                                View Academic Record
                            </button>
                        </div>
                    </form>
                </div>
            <% } %>

            <% if (selectedStudent == null) { %>
                <div class="app-card p-5">
                    <div class="empty-state">
                        <div class="empty-state-icon">
                            <i class="bi bi-person-vcard"></i>
                        </div>

                        <h5>No student record selected</h5>

                        <% if (isStudentView) { %>
                            <p>Your student profile could not be matched with the academic record system.</p>
                        <% } else { %>
                            <p>Please select a student to view the academic record transcript.</p>
                        <% } %>
                    </div>
                </div>
            <% } else { %>

                <div class="transcript-paper app-card p-4 p-lg-5">

                    <div class="transcript-header">
                        <div class="transcript-brand">
                            <div class="transcript-logo">
                                <i class="bi bi-mortarboard-fill"></i>
                            </div>

                            <div>
                                <h2>NextExamLK Academic Record</h2>
                                <p>Official published result summary</p>
                            </div>
                        </div>

                        <div class="transcript-generated">
                            <small>Generated Date</small>
                            <strong><%= FileUtil.h(generatedDate) %></strong>
                        </div>
                    </div>

                    <div class="transcript-divider"></div>

                    <div class="row g-4 mb-4">
                        <div class="col-lg-7">
                            <div class="transcript-student-box">
                                <span class="badge badge-soft-primary mb-3">Student Details</span>

                                <h3><%= FileUtil.h(studentName) %></h3>

                                <div class="transcript-info-grid">
                                    <div>
                                        <small>Student ID</small>
                                        <strong><%= FileUtil.h(selectedStudent.getStudentId()) %></strong>
                                    </div>

                                    <div>
                                        <small>Email</small>
                                        <strong><%= FileUtil.h(studentEmail) %></strong>
                                    </div>

                                    <div>
                                        <small>Course</small>
                                        <strong><%= FileUtil.h(studentCourse) %></strong>
                                    </div>

                                    <div>
                                        <small>Batch</small>
                                        <strong><%= FileUtil.h(studentBatch) %></strong>
                                    </div>

                                    <div>
                                        <small>Exam Status</small>
                                        <strong><%= FileUtil.h(studentStatus) %></strong>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="col-lg-5">
                            <div class="transcript-standing-box">
                                <span class="badge <%= standingBadgeClass %> mb-3">
                                    <%= FileUtil.h(academicStanding) %>
                                </span>

                                <h3><%= FileUtil.h(averageDisplay) %></h3>
                                <p>Average marks across published results</p>

                                <div class="transcript-standing-meta">
                                    <span>Passed: <strong><%= passedCount %></strong></span>
                                    <span>Failed: <strong><%= failedCount %></strong></span>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="row g-3 mb-4">
                        <div class="col-md-6 col-xl">
                            <div class="transcript-stat-card">
                                <small>Published Results</small>
                                <strong><%= totalPublishedResults %></strong>
                            </div>
                        </div>

                        <div class="col-md-6 col-xl">
                            <div class="transcript-stat-card">
                                <small>Average Marks</small>
                                <strong><%= FileUtil.h(averageDisplay) %></strong>
                            </div>
                        </div>

                        <div class="col-md-6 col-xl">
                            <div class="transcript-stat-card">
                                <small>Highest Marks</small>
                                <strong><%= FileUtil.h(highestDisplay) %></strong>
                            </div>
                        </div>

                        <div class="col-md-6 col-xl">
                            <div class="transcript-stat-card">
                                <small>Passed Exams</small>
                                <strong><%= passedCount %></strong>
                            </div>
                        </div>

                        <div class="col-md-6 col-xl">
                            <div class="transcript-stat-card">
                                <small>Failed Exams</small>
                                <strong><%= failedCount %></strong>
                            </div>
                        </div>
                    </div>

                    <div class="transcript-grade-strip mb-4">
                        <div><span>A</span><strong><%= gradeA %></strong></div>
                        <div><span>B</span><strong><%= gradeB %></strong></div>
                        <div><span>C</span><strong><%= gradeC %></strong></div>
                        <div><span>S</span><strong><%= gradeS %></strong></div>
                        <div><span>F</span><strong><%= gradeF %></strong></div>
                    </div>

                    <div class="d-flex justify-content-between align-items-center flex-wrap gap-2 mb-3">
                        <div>
                            <h4 class="fw-bold mb-1">Published Result Records</h4>
                            <p class="text-secondary mb-0">
                                Only verified and published records are shown in this academic transcript.
                            </p>
                        </div>
                    </div>

                    <div class="table-responsive transcript-table-wrap">
                        <table class="table table-hover transcript-table align-middle">
                            <thead>
                            <tr>
                                <th>#</th>
                                <th>Exam ID</th>
                                <th>Subject / Module</th>
                                <th>Exam Date</th>
                                <th>Marks</th>
                                <th>Total Marks</th>
                                <th>Grade</th>
                                <th>Status</th>
                                <th>Verification</th>
                            </tr>
                            </thead>

                            <tbody>
                            <% if (publishedResults == null || publishedResults.isEmpty()) { %>
                                <tr>
                                    <td colspan="9">
                                        <div class="empty-state">
                                            <div class="empty-state-icon">
                                                <i class="bi bi-file-earmark-x"></i>
                                            </div>
                                            <h5>No published results yet</h5>
                                            <p>Published academic results will appear here after release.</p>
                                        </div>
                                    </td>
                                </tr>
                            <% } else {
                                int rowNumber = 1;

                                for (Result result : publishedResults) {
                                    Exam exam = examMap != null ? examMap.get(result.getExamId()) : null;

                                    String subject = exam != null ? exam.getSubject() : "Unknown Subject";
                                    String examDate = exam != null ? exam.getDisplayExamDate() : "-";
                                    String totalMarks = exam != null ? exam.getDisplayTotalMarks() : "100";
                            %>
                                <tr>
                                    <td><%= rowNumber++ %></td>

                                    <td>
                                        <strong><%= FileUtil.h(result.getExamId()) %></strong>
                                    </td>

                                    <td>
                                        <%= FileUtil.h(subject) %>
                                    </td>

                                    <td>
                                        <%= FileUtil.h(examDate) %>
                                    </td>

                                    <td>
                                        <strong><%= FileUtil.h(result.getDisplayMarks()) %>%</strong>
                                    </td>

                                    <td>
                                        <%= FileUtil.h(totalMarks) %>
                                    </td>

                                    <td>
                                        <span class="badge <%= result.getGradeBadgeClass() %>">
                                            <%= FileUtil.h(result.getGrade()) %>
                                        </span>
                                    </td>

                                    <td>
                                        <span class="badge <%= result.getStatusBadgeClass() %>">
                                            <%= FileUtil.h(result.getStatus()) %>
                                        </span>
                                    </td>

                                    <td>
                                        <span class="badge badge-soft-success">
                                            <%= FileUtil.h(result.getVerification()) %>
                                        </span>
                                    </td>
                                </tr>
                            <% }
                            } %>
                            </tbody>
                        </table>
                    </div>

                    <div class="transcript-footer-note mt-4">
                        <p>
                            This academic record is generated from published results available in the NextExamLK
                            examination and result management system.
                        </p>
                    </div>
                </div>
            <% } %>

        </section>
    </main>
</div>

<%@ include file="../includes/footer.jsp" %>