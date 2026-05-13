<%--
    Main dashboard page for Nextexam.
    Displays role-based dashboard content and quick access cards
    for Admin, Lecturer, and Student users.
    Responsible Member: IT25103045 - De Silva H.L.D.C.P.C
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="lk.nextexam.dao.FileUtil" %>
<%@ page import="lk.nextexam.dao.StudentDAO" %>
<%@ page import="lk.nextexam.dao.ExamDAO" %>
<%@ page import="lk.nextexam.dao.QuestionDAO" %>
<%@ page import="lk.nextexam.dao.ResultDAO" %>
<%@ page import="lk.nextexam.dao.UserDAO" %>
<%@ page import="lk.nextexam.dao.NoticeDAO" %>
<%@ page import="lk.nextexam.dao.FeedbackDAO" %>
<%@ page import="lk.nextexam.dao.ExamSubmissionDAO" %>
<%@ page import="lk.nextexam.model.Student" %>
<%@ page import="lk.nextexam.model.Exam" %>
<%@ page import="lk.nextexam.model.Question" %>
<%@ page import="lk.nextexam.model.Result" %>
<%@ page import="lk.nextexam.model.User" %>
<%@ page import="lk.nextexam.model.Notice" %>
<%@ page import="lk.nextexam.model.Feedback" %>

<%
    String pageTitle = "Dashboard";
    String activeMenu = "dashboard";
    String topbarTitle = "Dashboard";

    String sessionRole = session != null && session.getAttribute("userRole") != null
            ? session.getAttribute("userRole").toString()
            : "";

    boolean isAdmin = "Admin".equalsIgnoreCase(sessionRole);
    boolean isLecturer = "Lecturer".equalsIgnoreCase(sessionRole);

    if (!isAdmin && !isLecturer) {
        response.sendRedirect(request.getContextPath() + "/my-exams?error=accessDenied");
        return;
    }

    StudentDAO studentDAO = new StudentDAO();
    ExamDAO examDAO = new ExamDAO();
    QuestionDAO questionDAO = new QuestionDAO();
    ResultDAO resultDAO = new ResultDAO();
    UserDAO userDAO = new UserDAO();
    NoticeDAO noticeDAO = new NoticeDAO();
    FeedbackDAO feedbackDAO = new FeedbackDAO();
    ExamSubmissionDAO submissionDAO = new ExamSubmissionDAO();

    List<Student> students = studentDAO.getAllStudents(application);
    List<Exam> exams = examDAO.getAllExams(application);
    List<Question> questions = questionDAO.getAllQuestions(application);
    List<Result> results = resultDAO.getAllResults(application);
    List<User> users = userDAO.getAllUsers(application);
    List<Notice> notices = noticeDAO.getAllNotices(application);
    List<Feedback> feedbackList = feedbackDAO.getAllFeedback(application);

    int totalStudents = students != null ? students.size() : 0;
    int totalExams = exams != null ? exams.size() : 0;
    int totalQuestions = questions != null ? questions.size() : 0;
    int totalResults = results != null ? results.size() : 0;
    int totalUsers = users != null ? users.size() : 0;
    int totalNotices = notices != null ? notices.size() : 0;
    int totalFeedback = feedbackList != null ? feedbackList.size() : 0;

    int draftExams = examDAO.countDraftExams(application);
    int scheduledExams = examDAO.countScheduledExams(application);
    int activeExams = examDAO.countActiveExams(application);
    int ongoingExams = examDAO.countOngoingExams(application);
    int attemptableExams = examDAO.countAttemptableExams(application);
    int completedExams = examDAO.countCompletedExams(application);
    int publishedExams = examDAO.countPublishedExams(application);
    int todayExams = examDAO.countTodayExams(application);
    int upcomingExams = examDAO.countUpcomingExams(application);

    int submittedAttempts = submissionDAO.countAllSubmissions(application);
    int autoMarkedAttempts = submissionDAO.countAutoMarked(application);
    int manualReviewAttempts = submissionDAO.countManualReviewRequired(application);
    int markedAttempts = submissionDAO.countMarked(application);
    int publishedAttempts = submissionDAO.countPublished(application);

    int eligibleStudents = 0;
    int pendingStudents = 0;
    int publishedResults = 0;
    int verifiedResults = 0;
    int newFeedback = 0;
    int reviewedFeedback = 0;
    int pendingFeedback = 0;
    int activeUsers = userDAO.countActiveUsers(application);
    int studentUsers = userDAO.countStudents(application);
    int lecturerUsers = userDAO.countLecturers(application);
    int adminUsers = userDAO.countAdmins(application);

    int draftQuestions = questionDAO.countByStatus(application, Question.STATUS_DRAFT);
    int activeQuestions = questionDAO.countByStatus(application, Question.STATUS_ACTIVE);
    int publishedQuestions = questionDAO.countByStatus(application, Question.STATUS_PUBLISHED);
    int mcqQuestions = questionDAO.countByType(application, Question.TYPE_MCQ);
    int essayQuestions = questionDAO.countByType(application, Question.TYPE_ESSAY);

    int totalMarks = 0;

    if (students != null) {
        for (Student student : students) {
            if ("Eligible".equalsIgnoreCase(student.getExamStatus())) {
                eligibleStudents++;
            } else if ("Pending".equalsIgnoreCase(student.getExamStatus())) {
                pendingStudents++;
            }
        }
    }

    if (results != null) {
        for (Result result : results) {
            if ("Published".equalsIgnoreCase(result.getPublished())) {
                publishedResults++;
            }

            if ("Verified".equalsIgnoreCase(result.getVerification())) {
                verifiedResults++;
            }

            try {
                totalMarks += Integer.parseInt(result.getMarks());
            } catch (Exception e) {
                totalMarks += 0;
            }
        }
    }

    if (feedbackList != null) {
        for (Feedback feedback : feedbackList) {
            if ("New".equalsIgnoreCase(feedback.getStatus())) {
                newFeedback++;
            } else if ("Reviewed".equalsIgnoreCase(feedback.getStatus())) {
                reviewedFeedback++;
            } else if ("Pending".equalsIgnoreCase(feedback.getStatus())) {
                pendingFeedback++;
            }
        }
    }

    int averageMarks = totalResults > 0 ? totalMarks / totalResults : 0;
    int questionCompletion = totalQuestions > 0 ? ((activeQuestions + publishedQuestions) * 100) / totalQuestions : 0;
    int resultVerification = totalResults > 0 ? (verifiedResults * 100) / totalResults : 0;
    int studentEligibility = totalStudents > 0 ? (eligibleStudents * 100) / totalStudents : 0;
    int feedbackReviewRate = totalFeedback > 0 ? (reviewedFeedback * 100) / totalFeedback : 0;
    int attemptPublishRate = submittedAttempts > 0 ? (publishedAttempts * 100) / submittedAttempts : 0;

    Notice latestNotice = null;
    if (notices != null && !notices.isEmpty()) {
        latestNotice = notices.get(notices.size() - 1);
    }

    String nextExamSubject = "No scheduled exam";
    String nextExamDate = "Not available";

    if (exams != null) {
        for (Exam exam : exams) {
            if (exam.isScheduled() || exam.isActive() || exam.isOngoing()) {
                nextExamSubject = exam.getSubject();
                nextExamDate = exam.getDisplayExamDate();
                break;
            }
        }
    }
%>

<%@ include file="includes/head.jsp" %>

<div class="app-shell">
    <%@ include file="includes/sidebar.jsp" %>

    <main class="main-content">
        <%@ include file="includes/topbar.jsp" %>

        <section class="page-wrapper">

            <div class="hero-card mb-4">
                <div class="d-flex justify-content-between align-items-start flex-wrap gap-3">
                    <div>
                        <span class="badge badge-soft-primary mb-3">
                            <i class="bi bi-mortarboard-fill me-1"></i>
                            NextExamLK Control Center
                        </span>

                        <h1 class="hero-title">Professional Examination Dashboard</h1>

                        <p class="hero-text">
                            Monitor students, exams, question banks, submissions, manual review queues,
                            published results, notices, feedback, and platform user activity from one secure workspace.
                        </p>
                    </div>

                    <div class="d-flex gap-2 flex-wrap">
                        <a href="<%= request.getContextPath() %>/exams" class="btn btn-primary">
                            <i class="bi bi-journal-plus me-2"></i>
                            Manage Exams
                        </a>

                        <a href="<%= request.getContextPath() %>/questions" class="btn btn-outline-primary">
                            <i class="bi bi-patch-question-fill me-2"></i>
                            Question Bank
                        </a>
                    </div>
                </div>
            </div>

            <div class="row g-3 mb-4">
                <div class="col-md-6 col-xl-3">
                    <div class="app-card stat-card">
                        <div class="d-flex justify-content-between gap-3">
                            <div>
                                <div class="stat-label">Students</div>
                                <div class="stat-value"><%= totalStudents %></div>
                                <div class="stat-meta">Candidate records</div>
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
                                <div class="stat-label">Attemptable Exams</div>
                                <div class="stat-value"><%= attemptableExams %></div>
                                <div class="stat-meta"><%= todayExams %> today · <%= upcomingExams %> upcoming</div>
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
                                <div class="stat-label">Question Bank</div>
                                <div class="stat-value"><%= totalQuestions %></div>
                                <div class="stat-meta"><%= mcqQuestions %> MCQ · <%= essayQuestions %> Essay</div>
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
                                <div class="stat-label">Submissions</div>
                                <div class="stat-value"><%= submittedAttempts %></div>
                                <div class="stat-meta"><%= manualReviewAttempts %> need manual review</div>
                            </div>

                            <div class="stat-icon">
                                <i class="bi bi-send-check-fill"></i>
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
                                <h4 class="fw-bold mb-1">Operational Overview</h4>
                                <p class="text-secondary mb-0">
                                    A live summary of platform readiness, exam activity, and result workflow progress.
                                </p>
                            </div>

                            <span class="badge badge-soft-primary">
                                <i class="bi bi-activity me-1"></i>
                                Live Workspace
                            </span>
                        </div>

                        <div class="row g-3">
                            <div class="col-md-4">
                                <a href="<%= request.getContextPath() %>/users">
                                    <div class="quick-card app-card border-0" style="background:#F8FAFC;">
                                        <div class="quick-icon">
                                            <i class="bi bi-shield-lock-fill"></i>
                                        </div>

                                        <h5>Admin Control</h5>
                                        <p><%= activeUsers %> active users across admin, lecturer, and student roles.</p>
                                    </div>
                                </a>
                            </div>

                            <div class="col-md-4">
                                <a href="<%= request.getContextPath() %>/exams">
                                    <div class="quick-card app-card border-0" style="background:#F8FAFC;">
                                        <div class="quick-icon">
                                            <i class="bi bi-calendar-event-fill"></i>
                                        </div>

                                        <h5>Exam Lifecycle</h5>
                                        <p><%= scheduledExams %> scheduled, <%= activeExams + ongoingExams %> active/ongoing exams.</p>
                                    </div>
                                </a>
                            </div>

                            <div class="col-md-4">
                                <a href="<%= request.getContextPath() %>/questions">
                                    <div class="quick-card app-card border-0" style="background:#F8FAFC;">
                                        <div class="quick-icon">
                                            <i class="bi bi-person-video3"></i>
                                        </div>

                                        <h5>Assessment Design</h5>
                                        <p><%= activeQuestions + publishedQuestions %> student-visible questions are ready.</p>
                                    </div>
                                </a>
                            </div>
                        </div>

                        <div class="row g-3 mt-1">
                            <div class="col-md-4">
                                <div class="exam-info-box">
                                    <small>Users</small>
                                    <strong><%= adminUsers %> Admin · <%= lecturerUsers %> Lecturer · <%= studentUsers %> Student</strong>
                                </div>
                            </div>

                            <div class="col-md-4">
                                <div class="exam-info-box">
                                    <small>Exam States</small>
                                    <strong><%= draftExams %> Draft · <%= completedExams %> Completed · <%= publishedExams %> Published</strong>
                                </div>
                            </div>

                            <div class="col-md-4">
                                <div class="exam-info-box">
                                    <small>Attempt States</small>
                                    <strong><%= autoMarkedAttempts %> Auto · <%= markedAttempts %> Marked · <%= publishedAttempts %> Published</strong>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-xl-4">
                    <div class="app-card p-4 h-100">
                        <div class="d-flex justify-content-between align-items-start mb-3">
                            <div>
                                <h4 class="fw-bold mb-1">Platform Readiness</h4>
                                <p class="text-secondary mb-0">Current preparation status</p>
                            </div>

                            <span class="badge badge-soft-success">
                                <span style="display:inline-block;width:7px;height:7px;border-radius:999px;background:#16a34a;margin-right:5px;"></span>
                                Online
                            </span>
                        </div>

                        <div class="readiness-board border-0 shadow-none p-0">
                            <div class="readiness-item mb-3">
                                <div class="d-flex justify-content-between mb-1">
                                    <span class="fw-semibold">Question Readiness</span>
                                    <span class="fw-bold"><%= questionCompletion %>%</span>
                                </div>

                                <div class="progress" style="height: 9px;">
                                    <div class="progress-bar" style="width: <%= questionCompletion %>%;"></div>
                                </div>

                                <small class="text-secondary">Active/published questions compared with total questions.</small>
                            </div>

                            <div class="readiness-item mb-3">
                                <div class="d-flex justify-content-between mb-1">
                                    <span class="fw-semibold">Result Verification</span>
                                    <span class="fw-bold"><%= resultVerification %>%</span>
                                </div>

                                <div class="progress" style="height: 9px;">
                                    <div class="progress-bar bg-success" style="width: <%= resultVerification %>%;"></div>
                                </div>

                                <small class="text-secondary">Verified results compared with total result records.</small>
                            </div>

                            <div class="readiness-item mb-3">
                                <div class="d-flex justify-content-between mb-1">
                                    <span class="fw-semibold">Student Eligibility</span>
                                    <span class="fw-bold"><%= studentEligibility %>%</span>
                                </div>

                                <div class="progress" style="height: 9px;">
                                    <div class="progress-bar bg-warning" style="width: <%= studentEligibility %>%;"></div>
                                </div>

                                <small class="text-secondary">Eligible students compared with all student records.</small>
                            </div>

                            <div class="readiness-item">
                                <div class="d-flex justify-content-between mb-1">
                                    <span class="fw-semibold">Attempt Publication</span>
                                    <span class="fw-bold"><%= attemptPublishRate %>%</span>
                                </div>

                                <div class="progress" style="height: 9px;">
                                    <div class="progress-bar bg-info" style="width: <%= attemptPublishRate %>%;"></div>
                                </div>

                                <small class="text-secondary">Published attempts compared with all exam submissions.</small>
                            </div>
                        </div>

                        <div class="alert alert-info mt-3 mb-0">
                            <strong>Next Exam:</strong><br>
                            <%= FileUtil.h(nextExamSubject) %><br>
                            <small>Scheduled date: <%= FileUtil.h(nextExamDate) %></small>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row g-4 mb-4">
                <div class="col-xl-4">
                    <div class="app-card p-4 h-100">
                        <div class="d-flex justify-content-between align-items-start mb-3">
                            <div>
                                <h4 class="fw-bold mb-1">Exam Workflow</h4>
                                <p class="text-secondary mb-0">Professional assessment process</p>
                            </div>

                            <span class="badge badge-soft-secondary">Process</span>
                        </div>

                        <div class="timeline">
                            <div class="timeline-item">
                                <div class="activity-title">Create Exam</div>
                                <small class="text-secondary">Admin or lecturer schedules the examination.</small>
                            </div>

                            <div class="timeline-item">
                                <div class="activity-title">Prepare Questions</div>
                                <small class="text-secondary">MCQ and essay questions are prepared and activated.</small>
                            </div>

                            <div class="timeline-item">
                                <div class="activity-title">Student Attempt</div>
                                <small class="text-secondary">Students complete the secure online exam console.</small>
                            </div>

                            <div class="timeline-item">
                                <div class="activity-title">Auto Mark / Manual Review</div>
                                <small class="text-secondary">MCQ answers are auto-marked; essays are reviewed manually.</small>
                            </div>

                            <div class="timeline-item">
                                <div class="activity-title">Publish Results</div>
                                <small class="text-secondary">Final results become visible to students.</small>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-xl-8">
                    <div class="app-card p-4 h-100">
                        <div class="d-flex justify-content-between align-items-start flex-wrap gap-3 mb-3">
                            <div>
                                <h4 class="fw-bold mb-1">Quick Access Modules</h4>
                                <p class="text-secondary mb-0">Open the main professional workflows quickly.</p>
                            </div>
                        </div>

                        <div class="row g-3">
                            <div class="col-md-6 col-xl-4">
                                <a href="<%= request.getContextPath() %>/students">
                                    <div class="app-card quick-card">
                                        <div class="quick-icon">
                                            <i class="bi bi-people-fill"></i>
                                        </div>

                                        <h5>Students</h5>
                                        <p>Manage candidate profiles, programme details, and exam status.</p>
                                    </div>
                                </a>
                            </div>

                            <div class="col-md-6 col-xl-4">
                                <a href="<%= request.getContextPath() %>/exams">
                                    <div class="app-card quick-card">
                                        <div class="quick-icon">
                                            <i class="bi bi-journal-check"></i>
                                        </div>

                                        <h5>Exams</h5>
                                        <p>Create, schedule, activate, complete, and publish exams.</p>
                                    </div>
                                </a>
                            </div>

                            <div class="col-md-6 col-xl-4">
                                <a href="<%= request.getContextPath() %>/questions">
                                    <div class="app-card quick-card">
                                        <div class="quick-icon">
                                            <i class="bi bi-patch-question-fill"></i>
                                        </div>

                                        <h5>Question Bank</h5>
                                        <p>Manage MCQ and essay questions for each exam.</p>
                                    </div>
                                </a>
                            </div>

                            <div class="col-md-6 col-xl-4">
                                <a href="<%= request.getContextPath() %>/results">
                                    <div class="app-card quick-card">
                                        <div class="quick-icon">
                                            <i class="bi bi-bar-chart-fill"></i>
                                        </div>

                                        <h5>Results</h5>
                                        <p>Manage result records, grading, verification, and publishing.</p>
                                    </div>
                                </a>
                            </div>

                            <% if (isAdmin) { %>
                                <div class="col-md-6 col-xl-4">
                                    <a href="<%= request.getContextPath() %>/users">
                                        <div class="app-card quick-card">
                                            <div class="quick-icon">
                                                <i class="bi bi-person-gear"></i>
                                            </div>

                                            <h5>Users & Roles</h5>
                                            <p>Manage administrator, lecturer, and student accounts.</p>
                                        </div>
                                    </a>
                                </div>
                            <% } %>

                            <div class="col-md-6 col-xl-4">
                                <a href="<%= request.getContextPath() %>/notices">
                                    <div class="app-card quick-card">
                                        <div class="quick-icon">
                                            <i class="bi bi-megaphone-fill"></i>
                                        </div>

                                        <h5>Notices</h5>
                                        <p>Publish exam notices, alerts, and academic announcements.</p>
                                    </div>
                                </a>
                            </div>

                            <div class="col-md-6 col-xl-4">
                                <a href="<%= request.getContextPath() %>/feedback">
                                    <div class="app-card quick-card">
                                        <div class="quick-icon">
                                            <i class="bi bi-chat-dots-fill"></i>
                                        </div>

                                        <h5>Feedback</h5>
                                        <p>Review student messages and support requests.</p>
                                    </div>
                                </a>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row g-4">
                <div class="col-xl-4">
                    <div class="app-card p-4 h-100">
                        <div class="d-flex justify-content-between align-items-start mb-3">
                            <div>
                                <h4 class="fw-bold mb-1">Recent Activity</h4>
                                <p class="text-secondary mb-0">Latest platform summary</p>
                            </div>

                            <span class="badge badge-soft-secondary">Live</span>
                        </div>

                        <div class="activity-item">
                            <div class="activity-title">Student records available</div>
                            <small class="text-secondary"><%= totalStudents %> candidates saved in the system.</small>
                        </div>

                        <div class="activity-item">
                            <div class="activity-title">Exam records available</div>
                            <small class="text-secondary"><%= totalExams %> total exams, <%= attemptableExams %> currently attemptable.</small>
                        </div>

                        <div class="activity-item">
                            <div class="activity-title">Question bank updated</div>
                            <small class="text-secondary"><%= totalQuestions %> questions saved, <%= draftQuestions %> in draft.</small>
                        </div>

                        <div class="activity-item">
                            <div class="activity-title">Submission workflow</div>
                            <small class="text-secondary"><%= submittedAttempts %> submissions, <%= manualReviewAttempts %> awaiting manual review.</small>
                        </div>
                    </div>
                </div>

                <div class="col-xl-4">
                    <div class="app-card p-4 h-100">
                        <div class="d-flex justify-content-between align-items-start mb-3">
                            <div>
                                <h4 class="fw-bold mb-1">Recent Notice</h4>
                                <p class="text-secondary mb-0">Latest academic announcement</p>
                            </div>

                            <a href="<%= request.getContextPath() %>/notices" class="badge badge-soft-primary">
                                View All
                            </a>
                        </div>

                        <div class="notice-card app-card shadow-none p-3">
                            <div class="d-flex justify-content-between gap-3">
                                <div>
                                    <h6 class="fw-bold mb-1">
                                        <%= latestNotice != null ? FileUtil.h(latestNotice.getTitle()) : "No notice available" %>
                                    </h6>

                                    <p class="text-secondary mb-2">
                                        <%= latestNotice != null ? FileUtil.h(latestNotice.getDescription()) : "Publish a notice to show the latest academic announcement here." %>
                                    </p>

                                    <span class="badge badge-soft-primary">
                                        <%= latestNotice != null ? FileUtil.h(latestNotice.getTargetGroup()) : "No Target Group" %>
                                    </span>
                                </div>

                                <small class="text-secondary">
                                    <%= latestNotice != null ? FileUtil.h(latestNotice.getNoticeDate()) : "-" %>
                                </small>
                            </div>
                        </div>

                        <div class="mt-3">
                            <small class="text-secondary">
                                Total notices saved: <%= totalNotices %>
                            </small>
                        </div>
                    </div>
                </div>

                <div class="col-xl-4">
                    <div class="app-card p-4 h-100">
                        <div class="d-flex justify-content-between align-items-start mb-3">
                            <div>
                                <h4 class="fw-bold mb-1">Feedback Summary</h4>
                                <p class="text-secondary mb-0">Student support queue</p>
                            </div>

                            <a href="<%= request.getContextPath() %>/feedback" class="badge badge-soft-warning">
                                Review
                            </a>
                        </div>

                        <div class="feedback-card shadow-none">
                            <div class="d-flex justify-content-between mb-2">
                                <span class="text-secondary">New Feedback</span>
                                <span class="fw-bold"><%= newFeedback %></span>
                            </div>

                            <div class="d-flex justify-content-between mb-2">
                                <span class="text-secondary">Reviewed</span>
                                <span class="fw-bold"><%= reviewedFeedback %></span>
                            </div>

                            <div class="d-flex justify-content-between mb-3">
                                <span class="text-secondary">Pending Response</span>
                                <span class="fw-bold"><%= pendingFeedback %></span>
                            </div>

                            <div class="progress" style="height: 9px;">
                                <div class="progress-bar bg-warning" style="width: <%= feedbackReviewRate %>%;"></div>
                            </div>

                            <small class="text-secondary d-block mt-2">
                                <%= feedbackReviewRate %>% of feedback messages have been reviewed.
                            </small>
                        </div>
                    </div>
                </div>
            </div>

        </section>
    </main>
</div>

<%@ include file="includes/footer.jsp" %>