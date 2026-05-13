<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="lk.nextexam.dao.FileUtil" %>
<%@ page import="lk.nextexam.model.Student" %>
<%@ page import="lk.nextexam.dao.StudentDAO" %>

<%
    String pageTitle = "Students";
    String activeMenu = "students";
    String topbarTitle = "Student Management";

    List<Student> students = (List<Student>) request.getAttribute("students");

    if (students == null) {
        StudentDAO studentDAO = new StudentDAO();
        students = studentDAO.getAllStudents(application);
    }

    int totalStudents = students != null ? students.size() : 0;
    int eligibleStudents = 0;
    int pendingStudents = 0;
    int blockedStudents = 0;

    int y1s1Count = 0;
    int y1s2Count = 0;
    int y2s1Count = 0;
    int y2s2Count = 0;
    int y3s1Count = 0;
    int y3s2Count = 0;
    int y4s1Count = 0;
    int y4s2Count = 0;

    if (students != null) {
        for (Student student : students) {
            if ("Eligible".equalsIgnoreCase(student.getExamStatus())) {
                eligibleStudents++;
            } else if ("Pending".equalsIgnoreCase(student.getExamStatus())) {
                pendingStudents++;
            } else if ("Blocked".equalsIgnoreCase(student.getExamStatus())) {
                blockedStudents++;
            }

            if ("Y1S1".equalsIgnoreCase(student.getBatch())) {
                y1s1Count++;
            } else if ("Y1S2".equalsIgnoreCase(student.getBatch())) {
                y1s2Count++;
            } else if ("Y2S1".equalsIgnoreCase(student.getBatch())) {
                y2s1Count++;
            } else if ("Y2S2".equalsIgnoreCase(student.getBatch())) {
                y2s2Count++;
            } else if ("Y3S1".equalsIgnoreCase(student.getBatch())) {
                y3s1Count++;
            } else if ("Y3S2".equalsIgnoreCase(student.getBatch())) {
                y3s2Count++;
            } else if ("Y4S1".equalsIgnoreCase(student.getBatch())) {
                y4s1Count++;
            } else if ("Y4S2".equalsIgnoreCase(student.getBatch())) {
                y4s2Count++;
            }
        }
    }

    int eligibilityRate = totalStudents > 0 ? (eligibleStudents * 100) / totalStudents : 0;

    String success = request.getParameter("success");
    String error = request.getParameter("error");

    String alertType = "";
    String alertMessage = "";

    if (success != null) {
        alertType = "success";

        if ("studentAdded".equalsIgnoreCase(success)) {
            alertMessage = "Student registered successfully.";
        } else if ("studentUpdated".equalsIgnoreCase(success)) {
            alertMessage = "Student record updated successfully.";
        } else if ("studentDeleted".equalsIgnoreCase(success)) {
            alertMessage = "Student record deleted successfully.";
        } else {
            alertMessage = "Operation completed successfully.";
        }
    }

    if (error != null) {
        alertType = "danger";

        if ("missingStudentId".equalsIgnoreCase(error)) {
            alertMessage = "Student ID is missing.";
        } else if ("studentAddFailed".equalsIgnoreCase(error)) {
            alertMessage = "Student could not be registered. Check duplicate Student ID or invalid details.";
        } else if ("studentUpdateFailed".equalsIgnoreCase(error)) {
            alertMessage = "Student record could not be updated.";
        } else if ("studentDeleteFailed".equalsIgnoreCase(error)) {
            alertMessage = "Student record could not be deleted.";
        } else {
            alertMessage = "Something went wrong. Please check the student details and try again.";
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
                            <i class="bi bi-people-fill me-1"></i>
                            NextExamLK Candidate Management Center
                        </span>

                        <h1 class="hero-title">Student Management</h1>

                        <p class="hero-text">
                            Register students, manage candidate profiles, update exam eligibility, organize batches from
                            <strong>Y1S1</strong> to <strong>Y4S2</strong>, and maintain clean student records for the
                            online examination workflow.
                        </p>
                    </div>

                    <div class="d-flex gap-2 flex-wrap">
                        <button class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#studentModal">
                            <i class="bi bi-person-plus-fill me-2"></i>
                            Register Student
                        </button>

                        <a href="<%= request.getContextPath() %>/results" class="btn btn-outline-primary">
                            <i class="bi bi-bar-chart-fill me-2"></i>
                            View Results
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
                                <div class="stat-label">Registered Students</div>
                                <div class="stat-value"><%= totalStudents %></div>
                                <div class="stat-meta">Total candidate records</div>
                            </div>

                            <div class="stat-icon">
                                <i class="bi bi-people-fill"></i>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-md-6 col-xl-3">
                    <div class="app-card stat-card">
                        <div class="d-flex justify-content-between gap-3">
                            <div>
                                <div class="stat-label">Eligible</div>
                                <div class="stat-value"><%= eligibleStudents %></div>
                                <div class="stat-meta"><%= eligibilityRate %>% ready for exams</div>
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
                                <div class="stat-label">Pending Review</div>
                                <div class="stat-value"><%= pendingStudents %></div>
                                <div class="stat-meta">Need eligibility check</div>
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
                                <div class="stat-label">Blocked</div>
                                <div class="stat-value"><%= blockedStudents %></div>
                                <div class="stat-meta">Restricted from exams</div>
                            </div>

                            <div class="stat-icon">
                                <i class="bi bi-shield-exclamation"></i>
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
                                <h4 class="fw-bold mb-1">Academic Semester Coverage</h4>
                                <p class="text-secondary mb-0">
                                    Student records are organized across the complete four-year academic structure.
                                </p>
                            </div>

                            <span class="badge badge-soft-primary">
                                <i class="bi bi-calendar3 me-1"></i>
                                Y1S1 to Y4S2
                            </span>
                        </div>

                        <div class="row g-3">
                            <div class="col-md-3 col-6">
                                <div class="exam-info-box">
                                    <small>Y1S1</small>
                                    <strong><%= y1s1Count %> Students</strong>
                                </div>
                            </div>

                            <div class="col-md-3 col-6">
                                <div class="exam-info-box">
                                    <small>Y1S2</small>
                                    <strong><%= y1s2Count %> Students</strong>
                                </div>
                            </div>

                            <div class="col-md-3 col-6">
                                <div class="exam-info-box">
                                    <small>Y2S1</small>
                                    <strong><%= y2s1Count %> Students</strong>
                                </div>
                            </div>

                            <div class="col-md-3 col-6">
                                <div class="exam-info-box">
                                    <small>Y2S2</small>
                                    <strong><%= y2s2Count %> Students</strong>
                                </div>
                            </div>

                            <div class="col-md-3 col-6">
                                <div class="exam-info-box">
                                    <small>Y3S1</small>
                                    <strong><%= y3s1Count %> Students</strong>
                                </div>
                            </div>

                            <div class="col-md-3 col-6">
                                <div class="exam-info-box">
                                    <small>Y3S2</small>
                                    <strong><%= y3s2Count %> Students</strong>
                                </div>
                            </div>

                            <div class="col-md-3 col-6">
                                <div class="exam-info-box">
                                    <small>Y4S1</small>
                                    <strong><%= y4s1Count %> Students</strong>
                                </div>
                            </div>

                            <div class="col-md-3 col-6">
                                <div class="exam-info-box">
                                    <small>Y4S2</small>
                                    <strong><%= y4s2Count %> Students</strong>
                                </div>
                            </div>
                        </div>

                        <div class="alert alert-info mt-4 mb-0">
                            <strong>Professional improvement:</strong>
                            This page now supports all academic semesters from first year to final year.
                        </div>
                    </div>
                </div>

                <div class="col-xl-5">
                    <div class="app-card p-4 h-100">
                        <div class="d-flex justify-content-between align-items-start mb-3">
                            <div>
                                <h4 class="fw-bold mb-1">Eligibility Workflow</h4>
                                <p class="text-secondary mb-0">
                                    Recommended student examination status flow.
                                </p>
                            </div>

                            <span class="badge badge-soft-secondary">Process</span>
                        </div>

                        <div class="timeline">
                            <div class="timeline-item">
                                <div class="activity-title">Register Student</div>
                                <small class="text-secondary">Create the student profile with batch, course, email, and contact information.</small>
                            </div>

                            <div class="timeline-item">
                                <div class="activity-title">Pending Review</div>
                                <small class="text-secondary">Keep students pending while eligibility is being checked.</small>
                            </div>

                            <div class="timeline-item">
                                <div class="activity-title">Eligible</div>
                                <small class="text-secondary">Eligible students can access assigned online exams.</small>
                            </div>

                            <div class="timeline-item">
                                <div class="activity-title">Blocked</div>
                                <small class="text-secondary">Use blocked status for students restricted from exam access.</small>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="page-header">
                <div>
                    <h2 class="page-title">Student Records</h2>

                    <p class="page-description">
                        Search, filter, create, view, update, and delete student records used for online examinations.
                    </p>
                </div>

                <button class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#studentModal">
                    <i class="bi bi-plus-lg me-2"></i>
                    Register Student
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
                               id="studentSearch"
                               placeholder="Search by ID, name, email, course, batch, contact, or status">
                    </div>

                    <div class="d-flex gap-2 flex-wrap">
                        <select class="form-select" id="batchFilter" style="width: 160px;">
                            <option value="">All Batches</option>
                            <option value="y1s1">Y1S1</option>
                            <option value="y1s2">Y1S2</option>
                            <option value="y2s1">Y2S1</option>
                            <option value="y2s2">Y2S2</option>
                            <option value="y3s1">Y3S1</option>
                            <option value="y3s2">Y3S2</option>
                            <option value="y4s1">Y4S1</option>
                            <option value="y4s2">Y4S2</option>
                        </select>

                        <select class="form-select" id="statusFilter" style="width: 170px;">
                            <option value="">All Status</option>
                            <option value="eligible">Eligible</option>
                            <option value="pending">Pending</option>
                            <option value="blocked">Blocked</option>
                        </select>

                        <button class="btn btn-outline-secondary" type="button" id="clearStudentFiltersBtn">
                            <i class="bi bi-x-circle me-1"></i>
                            Clear
                        </button>
                    </div>
                </div>

                <div class="table-responsive">
                    <table class="table table-hover align-middle" id="studentsTable">
                        <thead>
                        <tr>
                            <th>Student ID</th>
                            <th>Full Name</th>
                            <th>Email</th>
                            <th>Course</th>
                            <th>Batch</th>
                            <th>Contact</th>
                            <th>Exam Status</th>
                            <th>Readiness</th>
                            <th class="text-end">Actions</th>
                        </tr>
                        </thead>

                        <tbody>
                        <%
                            if (students != null && !students.isEmpty()) {
                                for (Student student : students) {
                                    String statusClass = "badge-soft-secondary";
                                    String progressClass = "bg-secondary";
                                    String progressValue = "50";
                                    String readinessLabel = "Needs Review";

                                    if ("Eligible".equalsIgnoreCase(student.getExamStatus())) {
                                        statusClass = "badge-soft-success";
                                        progressClass = "bg-success";
                                        progressValue = "100";
                                        readinessLabel = "Ready";
                                    } else if ("Pending".equalsIgnoreCase(student.getExamStatus())) {
                                        statusClass = "badge-soft-warning";
                                        progressClass = "bg-warning";
                                        progressValue = "55";
                                        readinessLabel = "Pending";
                                    } else if ("Blocked".equalsIgnoreCase(student.getExamStatus())) {
                                        statusClass = "badge-soft-danger";
                                        progressClass = "bg-danger";
                                        progressValue = "15";
                                        readinessLabel = "Restricted";
                                    }
                        %>
                        <tr data-batch="<%= FileUtil.h(student.getBatch().toLowerCase()) %>"
                            data-status="<%= FileUtil.h(student.getExamStatus().toLowerCase()) %>">
                            <td class="fw-bold"><%= FileUtil.h(student.getStudentId()) %></td>
                            <td><%= FileUtil.h(student.getName()) %></td>
                            <td><%= FileUtil.h(student.getEmail()) %></td>
                            <td><%= FileUtil.h(student.getCourse()) %></td>

                            <td>
                                <span class="badge badge-soft-primary">
                                    <%= FileUtil.h(student.getBatch()) %>
                                </span>
                            </td>

                            <td><%= FileUtil.h(student.getContact()) %></td>

                            <td>
                                <span class="badge <%= statusClass %>">
                                    <%= FileUtil.h(student.getExamStatus()) %>
                                </span>
                            </td>

                            <td>
                                <div class="d-flex align-items-center gap-2">
                                    <div class="progress flex-grow-1" style="height: 8px; min-width: 90px;">
                                        <div class="progress-bar <%= progressClass %>" style="width: <%= progressValue %>%;"></div>
                                    </div>
                                    <small class="fw-bold"><%= readinessLabel %></small>
                                </div>
                            </td>

                            <td>
                                <div class="action-group">
                                    <button class="btn btn-sm btn-outline-primary"
                                            type="button"
                                            title="View Student"
                                            data-bs-toggle="modal"
                                            data-bs-target="#viewStudentModal"
                                            data-student-id="<%= FileUtil.h(student.getStudentId()) %>"
                                            data-student-name="<%= FileUtil.h(student.getName()) %>"
                                            data-student-email="<%= FileUtil.h(student.getEmail()) %>"
                                            data-student-course="<%= FileUtil.h(student.getCourse()) %>"
                                            data-student-batch="<%= FileUtil.h(student.getBatch()) %>"
                                            data-student-contact="<%= FileUtil.h(student.getContact()) %>"
                                            data-student-status="<%= FileUtil.h(student.getExamStatus()) %>"
                                            data-readiness-label="<%= FileUtil.h(readinessLabel) %>">
                                        <i class="bi bi-eye"></i>
                                    </button>

                                    <button class="btn btn-sm btn-outline-primary"
                                            type="button"
                                            title="Edit Student"
                                            data-bs-toggle="modal"
                                            data-bs-target="#editStudentModal"
                                            data-student-id="<%= FileUtil.h(student.getStudentId()) %>"
                                            data-student-name="<%= FileUtil.h(student.getName()) %>"
                                            data-student-email="<%= FileUtil.h(student.getEmail()) %>"
                                            data-student-course="<%= FileUtil.h(student.getCourse()) %>"
                                            data-student-batch="<%= FileUtil.h(student.getBatch()) %>"
                                            data-student-contact="<%= FileUtil.h(student.getContact()) %>"
                                            data-student-status="<%= FileUtil.h(student.getExamStatus()) %>">
                                        <i class="bi bi-pencil-square"></i>
                                    </button>

                                    <button class="btn btn-sm btn-outline-danger"
                                            type="button"
                                            title="Delete Student"
                                            data-bs-toggle="modal"
                                            data-bs-target="#deleteModal"
                                            data-delete-name="<%= FileUtil.h(student.getStudentId() + " - " + student.getName()) %>"
                                            data-delete-id="<%= FileUtil.h(student.getStudentId()) %>"
                                            data-delete-url="<%= request.getContextPath() %>/students">
                                        <i class="bi bi-trash3"></i>
                                    </button>
                                </div>
                            </td>
                        </tr>
                        <%
                                }
                            } else {
                        %>
                        <tr>
                            <td colspan="9">
                                <div class="empty-state">
                                    <div class="empty-state-icon">
                                        <i class="bi bi-inbox"></i>
                                    </div>
                                    <h5>No student records found</h5>
                                    <p>Add a student to display records here.</p>
                                </div>
                            </td>
                        </tr>
                        <%
                            }
                        %>
                        </tbody>
                    </table>
                </div>
            </div>

        </section>
    </main>
</div>

<!-- Add Student Modal -->
<div class="modal fade" id="studentModal" tabindex="-1" aria-labelledby="studentModalTitle" aria-hidden="true">
    <div class="modal-dialog modal-lg modal-dialog-centered">
        <div class="modal-content border-0 shadow-lg">

            <form class="needs-validation"
                  novalidate
                  action="<%= request.getContextPath() %>/students"
                  method="post">

                <input type="hidden" name="action" value="add">

                <div class="modal-header">
                    <div>
                        <h5 class="modal-title fw-bold" id="studentModalTitle">Register Student</h5>
                        <small class="text-secondary">
                            Add a new candidate to the NextExamLK examination system.
                        </small>
                    </div>

                    <button class="btn-close" type="button" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>

                <div class="modal-body">
                    <div class="row g-3">
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
                            <label class="form-label">Full Name <span class="required">*</span></label>
                            <input type="text"
                                   name="name"
                                   class="form-control"
                                   placeholder="Enter full name"
                                   maxlength="120"
                                   required>
                            <div class="invalid-feedback">Full name is required.</div>
                        </div>

                        <div class="col-md-6">
                            <label class="form-label">Email Address <span class="required">*</span></label>
                            <input type="email"
                                   name="email"
                                   class="form-control"
                                   placeholder="student@email.com"
                                   maxlength="120"
                                   required>
                            <div class="invalid-feedback">Valid email is required.</div>
                        </div>

                        <div class="col-md-3">
                            <label class="form-label">Course / Module <span class="required">*</span></label>
                            <input type="text"
                                   name="course"
                                   class="form-control"
                                   placeholder="SE1020"
                                   maxlength="80"
                                   required>
                            <div class="invalid-feedback">Course is required.</div>
                        </div>

                        <div class="col-md-3">
                            <label class="form-label">Batch <span class="required">*</span></label>
                            <select name="batch" class="form-select" required>
                                <option value="">Choose batch</option>
                                <option value="Y1S1">Y1S1</option>
                                <option value="Y1S2">Y1S2</option>
                                <option value="Y2S1">Y2S1</option>
                                <option value="Y2S2">Y2S2</option>
                                <option value="Y3S1">Y3S1</option>
                                <option value="Y3S2">Y3S2</option>
                                <option value="Y4S1">Y4S1</option>
                                <option value="Y4S2">Y4S2</option>
                            </select>
                            <div class="invalid-feedback">Batch is required.</div>
                        </div>

                        <div class="col-md-6">
                            <label class="form-label">Contact Number <span class="required">*</span></label>
                            <input type="text"
                                   name="contact"
                                   class="form-control"
                                   placeholder="0771234567"
                                   maxlength="20"
                                   required>
                            <div class="invalid-feedback">Contact number is required.</div>
                        </div>

                        <div class="col-md-6">
                            <label class="form-label">Exam Status <span class="required">*</span></label>
                            <select name="examStatus" class="form-select" required>
                                <option value="">Choose status</option>
                                <option value="Eligible">Eligible</option>
                                <option value="Pending">Pending</option>
                                <option value="Blocked">Blocked</option>
                            </select>
                            <div class="invalid-feedback">Exam status is required.</div>
                        </div>
                    </div>

                    <div class="alert alert-info mt-4 mb-0">
                        <strong>Record workflow:</strong>
                        This form submits student data to the StudentServlet using <code>action=add</code>
                        and saves the record into <code>students.txt</code>.
                    </div>
                </div>

                <div class="modal-footer">
                    <button class="btn btn-outline-secondary" type="button" data-bs-dismiss="modal">
                        Cancel
                    </button>

                    <button class="btn btn-primary" type="submit">
                        <i class="bi bi-save me-2"></i>
                        Save Student
                    </button>
                </div>
            </form>

        </div>
    </div>
</div>

<!-- Edit Student Modal -->
<div class="modal fade" id="editStudentModal" tabindex="-1" aria-labelledby="editStudentModalTitle" aria-hidden="true">
    <div class="modal-dialog modal-lg modal-dialog-centered">
        <div class="modal-content border-0 shadow-lg">

            <form class="needs-validation"
                  novalidate
                  action="<%= request.getContextPath() %>/students"
                  method="post">

                <input type="hidden" name="action" value="update">

                <div class="modal-header">
                    <div>
                        <h5 class="modal-title fw-bold" id="editStudentModalTitle">Edit Student</h5>
                        <small class="text-secondary">
                            Update an existing candidate record in the NextExamLK examination system.
                        </small>
                    </div>

                    <button class="btn-close" type="button" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>

                <div class="modal-body">
                    <div class="row g-3">

                        <div class="col-md-6">
                            <label class="form-label">Student ID</label>
                            <input type="text"
                                   id="editStudentId"
                                   name="studentId"
                                   class="form-control"
                                   readonly
                                   required>
                            <div class="invalid-feedback">Student ID is required.</div>
                        </div>

                        <div class="col-md-6">
                            <label class="form-label">Full Name</label>
                            <input type="text"
                                   id="editStudentName"
                                   name="name"
                                   class="form-control"
                                   maxlength="120"
                                   required>
                            <div class="invalid-feedback">Full name is required.</div>
                        </div>

                        <div class="col-md-6">
                            <label class="form-label">Email Address</label>
                            <input type="email"
                                   id="editStudentEmail"
                                   name="email"
                                   class="form-control"
                                   maxlength="120"
                                   required>
                            <div class="invalid-feedback">Valid email is required.</div>
                        </div>

                        <div class="col-md-3">
                            <label class="form-label">Course / Module</label>
                            <input type="text"
                                   id="editStudentCourse"
                                   name="course"
                                   class="form-control"
                                   maxlength="80"
                                   required>
                            <div class="invalid-feedback">Course is required.</div>
                        </div>

                        <div class="col-md-3">
                            <label class="form-label">Batch</label>
                            <select id="editStudentBatch" name="batch" class="form-select" required>
                                <option value="">Choose batch</option>
                                <option value="Y1S1">Y1S1</option>
                                <option value="Y1S2">Y1S2</option>
                                <option value="Y2S1">Y2S1</option>
                                <option value="Y2S2">Y2S2</option>
                                <option value="Y3S1">Y3S1</option>
                                <option value="Y3S2">Y3S2</option>
                                <option value="Y4S1">Y4S1</option>
                                <option value="Y4S2">Y4S2</option>
                            </select>
                            <div class="invalid-feedback">Batch is required.</div>
                        </div>

                        <div class="col-md-6">
                            <label class="form-label">Contact Number</label>
                            <input type="text"
                                   id="editStudentContact"
                                   name="contact"
                                   class="form-control"
                                   maxlength="20"
                                   required>
                            <div class="invalid-feedback">Contact number is required.</div>
                        </div>

                        <div class="col-md-6">
                            <label class="form-label">Exam Status</label>
                            <select id="editStudentStatus" name="examStatus" class="form-select" required>
                                <option value="">Choose status</option>
                                <option value="Eligible">Eligible</option>
                                <option value="Pending">Pending</option>
                                <option value="Blocked">Blocked</option>
                            </select>
                            <div class="invalid-feedback">Exam status is required.</div>
                        </div>
                    </div>

                    <div class="alert alert-info mt-4 mb-0">
                        <strong>Update workflow:</strong>
                        This form submits updated student data to the StudentServlet using <code>action=update</code>
                        and updates the existing record in <code>students.txt</code>.
                    </div>
                </div>

                <div class="modal-footer">
                    <button class="btn btn-outline-secondary" type="button" data-bs-dismiss="modal">
                        Cancel
                    </button>

                    <button class="btn btn-primary" type="submit">
                        <i class="bi bi-save me-2"></i>
                        Update Student
                    </button>
                </div>
            </form>

        </div>
    </div>
</div>

<!-- View Student Modal -->
<div class="modal fade" id="viewStudentModal" tabindex="-1" aria-labelledby="viewStudentModalTitle" aria-hidden="true">
    <div class="modal-dialog modal-lg modal-dialog-centered">
        <div class="modal-content border-0 shadow-lg">

            <div class="modal-header">
                <div>
                    <h5 class="modal-title fw-bold" id="viewStudentModalTitle">Student Details</h5>
                    <small class="text-secondary">
                        View candidate information saved in the examination system.
                    </small>
                </div>

                <button class="btn-close" type="button" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>

            <div class="modal-body">
                <div class="row g-3">
                    <div class="col-md-6">
                        <div class="exam-info-box">
                            <small>Student ID</small>
                            <strong id="viewStudentId">-</strong>
                        </div>
                    </div>

                    <div class="col-md-6">
                        <div class="exam-info-box">
                            <small>Full Name</small>
                            <strong id="viewStudentName">-</strong>
                        </div>
                    </div>

                    <div class="col-md-6">
                        <div class="exam-info-box">
                            <small>Email</small>
                            <strong id="viewStudentEmail">-</strong>
                        </div>
                    </div>

                    <div class="col-md-6">
                        <div class="exam-info-box">
                            <small>Contact</small>
                            <strong id="viewStudentContact">-</strong>
                        </div>
                    </div>

                    <div class="col-md-4">
                        <div class="exam-info-box">
                            <small>Course / Module</small>
                            <strong id="viewStudentCourse">-</strong>
                        </div>
                    </div>

                    <div class="col-md-4">
                        <div class="exam-info-box">
                            <small>Batch</small>
                            <strong id="viewStudentBatch">-</strong>
                        </div>
                    </div>

                    <div class="col-md-4">
                        <div class="exam-info-box">
                            <small>Exam Status</small>
                            <strong id="viewStudentStatus">-</strong>
                        </div>
                    </div>

                    <div class="col-md-12">
                        <div class="exam-info-box">
                            <small>Readiness</small>
                            <strong id="viewStudentReadiness">-</strong>
                        </div>
                    </div>
                </div>

                <div class="alert alert-info mt-4 mb-0">
                    <strong>Student record:</strong>
                    These details are loaded from the selected table row and displayed for quick review.
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
        const editStudentModal = document.getElementById("editStudentModal");
        const viewStudentModal = document.getElementById("viewStudentModal");

        const studentSearch = document.getElementById("studentSearch");
        const batchFilter = document.getElementById("batchFilter");
        const statusFilter = document.getElementById("statusFilter");
        const clearFiltersBtn = document.getElementById("clearStudentFiltersBtn");
        const studentRows = document.querySelectorAll("#studentsTable tbody tr[data-batch]");

        function getStudentData(button) {
            return {
                id: button.getAttribute("data-student-id") || "",
                name: button.getAttribute("data-student-name") || "",
                email: button.getAttribute("data-student-email") || "",
                course: button.getAttribute("data-student-course") || "",
                batch: button.getAttribute("data-student-batch") || "",
                contact: button.getAttribute("data-student-contact") || "",
                status: button.getAttribute("data-student-status") || "",
                readiness: button.getAttribute("data-readiness-label") || ""
            };
        }

        function filterStudents() {
            const searchValue = studentSearch ? studentSearch.value.toLowerCase().trim() : "";
            const batchValue = batchFilter ? batchFilter.value.toLowerCase().trim() : "";
            const statusValue = statusFilter ? statusFilter.value.toLowerCase().trim() : "";

            studentRows.forEach(function (row) {
                const rowText = row.innerText.toLowerCase();
                const rowBatch = row.getAttribute("data-batch") || "";
                const rowStatus = row.getAttribute("data-status") || "";

                const matchesSearch = rowText.includes(searchValue);
                const matchesBatch = batchValue === "" || rowBatch === batchValue;
                const matchesStatus = statusValue === "" || rowStatus === statusValue;

                row.style.display = matchesSearch && matchesBatch && matchesStatus ? "" : "none";
            });
        }

        if (studentSearch) {
            studentSearch.addEventListener("input", filterStudents);
        }

        if (batchFilter) {
            batchFilter.addEventListener("change", filterStudents);
        }

        if (statusFilter) {
            statusFilter.addEventListener("change", filterStudents);
        }

        if (clearFiltersBtn) {
            clearFiltersBtn.addEventListener("click", function () {
                if (studentSearch) {
                    studentSearch.value = "";
                }

                if (batchFilter) {
                    batchFilter.value = "";
                }

                if (statusFilter) {
                    statusFilter.value = "";
                }

                filterStudents();
            });
        }

        if (editStudentModal) {
            editStudentModal.addEventListener("show.bs.modal", function (event) {
                const button = event.relatedTarget;

                if (!button) {
                    return;
                }

                const student = getStudentData(button);

                document.getElementById("editStudentId").value = student.id;
                document.getElementById("editStudentName").value = student.name;
                document.getElementById("editStudentEmail").value = student.email;
                document.getElementById("editStudentCourse").value = student.course;
                document.getElementById("editStudentBatch").value = student.batch;
                document.getElementById("editStudentContact").value = student.contact;
                document.getElementById("editStudentStatus").value = student.status;
            });
        }

        if (viewStudentModal) {
            viewStudentModal.addEventListener("show.bs.modal", function (event) {
                const button = event.relatedTarget;

                if (!button) {
                    return;
                }

                const student = getStudentData(button);

                document.getElementById("viewStudentId").textContent = student.id || "-";
                document.getElementById("viewStudentName").textContent = student.name || "-";
                document.getElementById("viewStudentEmail").textContent = student.email || "-";
                document.getElementById("viewStudentCourse").textContent = student.course || "-";
                document.getElementById("viewStudentBatch").textContent = student.batch || "-";
                document.getElementById("viewStudentContact").textContent = student.contact || "-";
                document.getElementById("viewStudentStatus").textContent = student.status || "-";
                document.getElementById("viewStudentReadiness").textContent = student.readiness || "-";
            });
        }
    });
</script>

<%@ include file="../includes/delete-modal.jsp" %>
<%@ include file="../includes/footer.jsp" %>