<%--
    Main dashboard page for Nextexam.

    Enhancement Pack 19:
    Student Academic Dashboard Upgrade

    This dashboard now supports:
    - Admin executive analytics dashboard
    - Lecturer workload-focused dashboard
    - Manual marking queue
    - Submission review queue
    - Result appeal queue
    - Notification and feedback workload
    - Role-based quick actions
    - Student academic dashboard
    - Student exam/result/appeal/notification overview

    Responsible Member:
    IT25103045 - De Silva H.L.D.C.P.C
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
<%@ page import="lk.nextexam.dao.ResultAppealDAO" %>
<%@ page import="lk.nextexam.dao.NotificationDAO" %>

<%@ page import="lk.nextexam.model.Student" %>
<%@ page import="lk.nextexam.model.Exam" %>
<%@ page import="lk.nextexam.model.Question" %>
<%@ page import="lk.nextexam.model.Result" %>
<%@ page import="lk.nextexam.model.User" %>
<%@ page import="lk.nextexam.model.Notice" %>
<%@ page import="lk.nextexam.model.ResultAppeal" %>
<%@ page import="lk.nextexam.model.ExamSubmission" %>
<%@ page import="lk.nextexam.model.Feedback" %>

<%
    String pageTitle = "Dashboard";
    String activeMenu = "dashboard";
    String topbarTitle = "Dashboard";

    String sessionRole = session != null && session.getAttribute("userRole") != null
            ? String.valueOf(session.getAttribute("userRole"))
            : "";

    String sessionUserId = session != null && session.getAttribute("userId") != null
            ? String.valueOf(session.getAttribute("userId"))
            : "";

    String displayName = session != null && session.getAttribute("displayName") != null
            ? String.valueOf(session.getAttribute("displayName"))
            : "";

    String username = session != null && session.getAttribute("username") != null
            ? String.valueOf(session.getAttribute("username"))
            : "";

    String userDisplayName = !displayName.trim().isEmpty()
            ? displayName
            : !username.trim().isEmpty()
                ? username
                : sessionUserId;

    boolean isAdmin = "Admin".equalsIgnoreCase(sessionRole);
    boolean isLecturer = "Lecturer".equalsIgnoreCase(sessionRole);
    boolean isStudent = "Student".equalsIgnoreCase(sessionRole);

    if (!isAdmin && !isLecturer && !isStudent) {
        response.sendRedirect(request.getContextPath() + "/login.jsp?error=accessDenied");
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
    ResultAppealDAO appealDAO = new ResultAppealDAO();
    NotificationDAO notificationDAO = new NotificationDAO();

    List<Student> students = studentDAO.getAllStudents(application);
    List<Exam> exams = examDAO.getAllExams(application);
    List<Question> questions = questionDAO.getAllQuestions(application);
    List<Result> results = resultDAO.getAllResults(application);
    List<User> users = userDAO.getAllUsers(application);
    List<Notice> notices = noticeDAO.getAllNotices(application);

    int totalStudents = students != null ? students.size() : 0;
    int totalExams = exams != null ? exams.size() : 0;
    int totalQuestions = questions != null ? questions.size() : 0;
    int totalResults = results != null ? results.size() : 0;
    int totalUsers = users != null ? users.size() : 0;
    int totalNotices = notices != null ? notices.size() : 0;

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
    int submittedOnlyAttempts = submissionDAO.countSubmitted(application);
    int autoMarkedAttempts = submissionDAO.countAutoMarked(application);
    int manualReviewAttempts = submissionDAO.countManualReviewRequired(application);
    int markedAttempts = submissionDAO.countMarked(application);
    int publishedAttempts = submissionDAO.countPublished(application);
    int cancelledAttempts = submissionDAO.countCancelled(application);

    int totalAppeals = appealDAO.countAll(application);
    int pendingAppeals = appealDAO.countByStatus(application, ResultAppeal.STATUS_PENDING);
    int underReviewAppeals = appealDAO.countByStatus(application, ResultAppeal.STATUS_UNDER_REVIEW);
    int resolvedAppeals = appealDAO.countByStatus(application, ResultAppeal.STATUS_RESOLVED);
    int rejectedAppeals = appealDAO.countByStatus(application, ResultAppeal.STATUS_REJECTED);

    int unreadForCurrentRole = notificationDAO.countUnreadForUser(application, sessionUserId, sessionRole);
    int adminUnreadNotifications = notificationDAO.countUnreadForUser(application, "", "Admin");
    int lecturerUnreadNotifications = notificationDAO.countUnreadForUser(application, "", "Lecturer");
    int studentUnreadNotifications = notificationDAO.countUnreadForUser(application, "", "Student");
    int totalNotifications = notificationDAO.getAllNotifications(application).size();
    int totalUnreadNotifications = adminUnreadNotifications + lecturerUnreadNotifications + studentUnreadNotifications;

    int totalFeedback = feedbackDAO.countAllFeedback(application);
    int newFeedback = feedbackDAO.countNewFeedback(application);
    int inReviewFeedback = feedbackDAO.countInReviewFeedback(application);
    int resolvedFeedback = feedbackDAO.countResolvedFeedback(application);
    int closedFeedback = feedbackDAO.countClosedFeedback(application);
    int openFeedback = feedbackDAO.countOpenFeedback(application);
    int todayFeedback = feedbackDAO.countTodayFeedback(application);

    int draftQuestions = questionDAO.countByStatus(application, Question.STATUS_DRAFT);
    int activeQuestions = questionDAO.countByStatus(application, Question.STATUS_ACTIVE);
    int publishedQuestions = questionDAO.countByStatus(application, Question.STATUS_PUBLISHED);
    int mcqQuestions = questionDAO.countByType(application, Question.TYPE_MCQ);
    int essayQuestions = questionDAO.countByType(application, Question.TYPE_ESSAY);

    int eligibleStudents = 0;
    int pendingStudents = 0;
    int blockedStudents = 0;

    if (students != null) {
        for (Student student : students) {
            if ("Eligible".equalsIgnoreCase(student.getExamStatus())) {
                eligibleStudents++;
            } else if ("Pending".equalsIgnoreCase(student.getExamStatus())) {
                pendingStudents++;
            } else if ("Blocked".equalsIgnoreCase(student.getExamStatus())) {
                blockedStudents++;
            }
        }
    }

    int publishedResults = 0;
    int verifiedResults = 0;
    int totalMarks = 0;

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

    int averageMarks = totalResults > 0 ? totalMarks / totalResults : 0;
    int questionCompletion = totalQuestions > 0 ? ((activeQuestions + publishedQuestions) * 100) / totalQuestions : 0;
    int resultVerification = totalResults > 0 ? (verifiedResults * 100) / totalResults : 0;
    int studentEligibility = totalStudents > 0 ? (eligibleStudents * 100) / totalStudents : 0;
    int feedbackReviewRate = totalFeedback > 0 ? ((resolvedFeedback + closedFeedback) * 100) / totalFeedback : 0;
    int attemptPublishRate = submittedAttempts > 0 ? (publishedAttempts * 100) / submittedAttempts : 0;
    int manualReviewRate = submittedAttempts > 0 ? (manualReviewAttempts * 100) / submittedAttempts : 0;
    int markingCompletionRate = submittedAttempts > 0
            ? ((autoMarkedAttempts + markedAttempts + publishedAttempts) * 100) / submittedAttempts
            : 0;
    int appealResolutionRate = totalAppeals > 0
            ? ((resolvedAppeals + rejectedAppeals) * 100) / totalAppeals
            : 0;
    int notificationReadRate = totalNotifications > 0
            ? ((totalNotifications - totalUnreadNotifications) * 100) / totalNotifications
            : 0;

    int lecturerWorkloadScore = manualReviewAttempts + pendingAppeals + openFeedback + unreadForCurrentRole + submittedOnlyAttempts;
    int adminRiskScore = manualReviewAttempts + pendingAppeals + openFeedback + totalUnreadNotifications + submittedOnlyAttempts;

    int activeAcademicExams = activeExams + ongoingExams + attemptableExams;

    String workloadLabel = lecturerWorkloadScore == 0
            ? "Clear"
            : lecturerWorkloadScore <= 5
                ? "Balanced"
                : "High Workload";

    String workloadBadge = lecturerWorkloadScore == 0
            ? "badge-soft-success"
            : lecturerWorkloadScore <= 5
                ? "badge-soft-warning"
                : "badge-soft-danger";

    String adminRiskLabel = adminRiskScore == 0
            ? "Stable"
            : adminRiskScore <= 5
                ? "Moderate"
                : "Needs Attention";

    String adminRiskBadge = adminRiskScore == 0
            ? "badge-soft-success"
            : adminRiskScore <= 5
                ? "badge-soft-warning"
                : "badge-soft-danger";

    /* ================================
       Student Academic Dashboard Data
    ================================ */
    List<Exam> studentAttemptableExamList = examDAO.getAttemptableExams(application);
    List<ExamSubmission> studentSubmissionList = submissionDAO.getSubmissionsByStudent(application, sessionUserId);
    List<Result> studentPublishedResultList = resultDAO.getPublishedResultsByStudentId(application, sessionUserId);
    List<ResultAppeal> studentAppealList = appealDAO.getAppealsByStudent(application, sessionUserId);
    List<Feedback> studentFeedbackList = feedbackDAO.getFeedbackByStudentId(application, sessionUserId);
    List<Notice> studentNoticeList = noticeDAO.getVisibleNoticesForRole(application, "Student");

    int studentSubmittedAttempts = studentSubmissionList != null ? studentSubmissionList.size() : 0;
    int studentPublishedResults = studentPublishedResultList != null ? studentPublishedResultList.size() : 0;
    int studentTotalAppeals = studentAppealList != null ? studentAppealList.size() : 0;
    int studentFeedbackCount = studentFeedbackList != null ? studentFeedbackList.size() : 0;

    int studentAvailableExams = 0;
    int studentOpenFeedback = 0;
    int studentPendingAppeals = 0;
    int studentUnderReviewAppeals = 0;
    int studentResolvedAppeals = 0;
    int studentRejectedAppeals = 0;
    int studentResultMarksTotal = 0;

    if (studentAttemptableExamList != null) {
        for (Exam exam : studentAttemptableExamList) {
            if (exam != null && !submissionDAO.hasStudentSubmitted(application, sessionUserId, exam.getExamId())) {
                studentAvailableExams++;
            }
        }
    }

    if (studentFeedbackList != null) {
        for (Feedback feedback : studentFeedbackList) {
            if (feedback != null && feedback.isOpen()) {
                studentOpenFeedback++;
            }
        }
    }

    if (studentAppealList != null) {
        for (ResultAppeal appeal : studentAppealList) {
            if (appeal == null) {
                continue;
            }

            if (ResultAppeal.STATUS_PENDING.equalsIgnoreCase(appeal.getStatus())) {
                studentPendingAppeals++;
            } else if (ResultAppeal.STATUS_UNDER_REVIEW.equalsIgnoreCase(appeal.getStatus())) {
                studentUnderReviewAppeals++;
            } else if (ResultAppeal.STATUS_RESOLVED.equalsIgnoreCase(appeal.getStatus())) {
                studentResolvedAppeals++;
            } else if (ResultAppeal.STATUS_REJECTED.equalsIgnoreCase(appeal.getStatus())) {
                studentRejectedAppeals++;
            }
        }
    }

    if (studentPublishedResultList != null) {
        for (Result result : studentPublishedResultList) {
            try {
                studentResultMarksTotal += Integer.parseInt(result.getMarks());
            } catch (Exception e) {
                studentResultMarksTotal += 0;
            }
        }
    }

    int studentAverageMarks = studentPublishedResults > 0 ? studentResultMarksTotal / studentPublishedResults : 0;
    int studentAcademicItems = studentAvailableExams + studentSubmittedAttempts;
    int studentExamCompletionRate = studentAcademicItems > 0 ? (studentSubmittedAttempts * 100) / studentAcademicItems : 0;
    int studentResultPublishRate = studentSubmittedAttempts > 0 ? (studentPublishedResults * 100) / studentSubmittedAttempts : 0;
    int studentAppealResolutionRate = studentTotalAppeals > 0
            ? ((studentResolvedAppeals + studentRejectedAppeals) * 100) / studentTotalAppeals
            : 0;
    int currentStudentTotalNotifications = notificationDAO.countAllForUser(application, sessionUserId, sessionRole);
    int currentStudentUnreadNotifications = notificationDAO.countUnreadForUser(application, sessionUserId, sessionRole);
    int studentNotificationReadRate = currentStudentTotalNotifications > 0
            ? ((currentStudentTotalNotifications - currentStudentUnreadNotifications) * 100) / currentStudentTotalNotifications
            : 0;

    int studentPriorityScore = studentAvailableExams + studentPendingAppeals + studentUnreadNotifications + studentOpenFeedback;

    String studentPriorityLabel = studentPriorityScore == 0
            ? "Clear"
            : studentPriorityScore <= 4
                ? "Good Progress"
                : "Needs Attention";

    String studentPriorityBadge = studentPriorityScore == 0
            ? "badge-soft-success"
            : studentPriorityScore <= 4
                ? "badge-soft-warning"
                : "badge-soft-danger";

    Notice latestStudentNotice = null;

    if (studentNoticeList != null && !studentNoticeList.isEmpty()) {
        latestStudentNotice = studentNoticeList.get(studentNoticeList.size() - 1);
    }

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

            <% if (isStudent) { %>

                <!-- Student Dashboard Hero -->
                <div class="hero-card student-hero-card mb-4">
                    <div class="d-flex justify-content-between align-items-start flex-wrap gap-3">
                        <div>
                            <span class="badge badge-soft-primary mb-3">
                                <i class="bi bi-mortarboard-fill me-1"></i>
                                Student Academic Dashboard
                            </span>

                            <h1 class="hero-title">Welcome, <%= FileUtil.h(userDisplayName) %></h1>

                            <p class="hero-text">
                                View available exams, track submitted attempts, check published results,
                                follow result appeals, read notices, and manage academic support from one workspace.
                            </p>
                        </div>

                        <div class="d-flex gap-2 flex-wrap">
                            <a href="<%= request.getContextPath() %>/my-exams" class="btn btn-primary">
                                <i class="bi bi-laptop-fill me-2"></i>
                                My Exams
                            </a>

                            <a href="<%= request.getContextPath() %>/my-results" class="btn btn-outline-primary">
                                <i class="bi bi-bar-chart-fill me-2"></i>
                                My Results
                            </a>

                            <a href="<%= request.getContextPath() %>/notifications" class="btn btn-outline-primary">
                                <i class="bi bi-bell-fill me-2"></i>
                                Notifications
                            </a>
                        </div>
                    </div>
                </div>

                <!-- Student KPI Cards -->
                <div class="row g-3 mb-4">
                    <div class="col-md-6 col-xl-3">
                        <div class="app-card stat-card student-stat-card">
                            <div class="d-flex justify-content-between gap-3">
                                <div>
                                    <div class="stat-label">Available Exams</div>
                                    <div class="stat-value"><%= studentAvailableExams %></div>
                                    <div class="stat-meta">Ready for your attempt</div>
                                </div>

                                <div class="stat-icon">
                                    <i class="bi bi-laptop-fill"></i>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="col-md-6 col-xl-3">
                        <div class="app-card stat-card student-stat-card">
                            <div class="d-flex justify-content-between gap-3">
                                <div>
                                    <div class="stat-label">My Submissions</div>
                                    <div class="stat-value"><%= studentSubmittedAttempts %></div>
                                    <div class="stat-meta">Submitted exam attempts</div>
                                </div>

                                <div class="stat-icon">
                                    <i class="bi bi-send-check-fill"></i>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="col-md-6 col-xl-3">
                        <div class="app-card stat-card student-stat-card">
                            <div class="d-flex justify-content-between gap-3">
                                <div>
                                    <div class="stat-label">Published Results</div>
                                    <div class="stat-value"><%= studentPublishedResults %></div>
                                    <div class="stat-meta">Average marks: <%= studentAverageMarks %>%</div>
                                </div>

                                <div class="stat-icon">
                                    <i class="bi bi-bar-chart-fill"></i>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="col-md-6 col-xl-3">
                        <div class="app-card stat-card student-stat-card">
                            <div class="d-flex justify-content-between gap-3">
                                <div>
                                    <div class="stat-label">Academic Alerts</div>
                                    <div class="stat-value"><%= studentPriorityScore %></div>
                                    <div class="stat-meta"><%= currentStudentUnreadNotifications %>> unread · <%= studentPendingAppeals %> appeals</div>
                                </div>

                                <div class="stat-icon">
                                    <i class="bi bi-bell-fill"></i>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Student Main Academic Overview -->
                <div class="row g-4 mb-4">
                    <div class="col-xl-5">
                        <div class="app-card p-4 h-100 student-priority-card">
                            <div class="d-flex justify-content-between align-items-start flex-wrap gap-3 mb-3">
                                <div>
                                    <h4 class="fw-bold mb-1">Student Priority Queue</h4>
                                    <p class="text-secondary mb-0">
                                        Your most important academic actions are shown here.
                                    </p>
                                </div>

                                <span class="badge <%= studentPriorityBadge %>">
                                    <i class="bi bi-lightning-charge-fill me-1"></i>
                                    <%= studentPriorityLabel %>
                                </span>
                            </div>

                            <div class="priority-task-list student-priority-list">
                                <a href="<%= request.getContextPath() %>/my-exams" class="priority-task-item primary">
                                    <div>
                                        <small>Available Exams</small>
                                        <strong><%= studentAvailableExams %></strong>
                                        <span>Exams currently available for you to attempt</span>
                                    </div>
                                    <i class="bi bi-chevron-right"></i>
                                </a>

                                <a href="<%= request.getContextPath() %>/my-results" class="priority-task-item success">
                                    <div>
                                        <small>Published Results</small>
                                        <strong><%= studentPublishedResults %></strong>
                                        <span>Results released by academic staff</span>
                                    </div>
                                    <i class="bi bi-chevron-right"></i>
                                </a>

                                <a href="<%= request.getContextPath() %>/result-appeals" class="priority-task-item warning">
                                    <div>
                                        <small>Pending Appeals</small>
                                        <strong><%= studentPendingAppeals %></strong>
                                        <span>Result recheck requests waiting for review</span>
                                    </div>
                                    <i class="bi bi-chevron-right"></i>
                                </a>

                                <a href="<%= request.getContextPath() %>/notifications" class="priority-task-item info">
                                    <div>
                                        <small>Unread Notifications</small>
                                        <strong><%= studentUnreadNotifications %></strong>
                                        <span>Important academic updates for you</span>
                                    </div>
                                    <i class="bi bi-chevron-right"></i>
                                </a>

                                <a href="<%= request.getContextPath() %>/feedback" class="priority-task-item danger">
                                    <div>
                                        <small>Open Feedback</small>
                                        <strong><%= studentOpenFeedback %></strong>
                                        <span>Feedback or support messages still open</span>
                                    </div>
                                    <i class="bi bi-chevron-right"></i>
                                </a>
                            </div>
                        </div>
                    </div>

                    <div class="col-xl-7">
                        <div class="app-card p-4 h-100 student-progress-card">
                            <div class="d-flex justify-content-between align-items-start flex-wrap gap-3 mb-4">
                                <div>
                                    <h4 class="fw-bold mb-1">Academic Progress Overview</h4>
                                    <p class="text-secondary mb-0">
                                        Track exam completion, result publishing, appeals, and notification progress.
                                    </p>
                                </div>

                                <span class="badge badge-soft-primary">
                                    <i class="bi bi-activity me-1"></i>
                                    Student Progress
                                </span>
                            </div>

                            <div class="dashboard-analytics-grid student-analytics-grid">
                                <div class="dashboard-analytics-tile primary">
                                    <div>
                                        <small>Exam Completion</small>
                                        <strong><%= studentExamCompletionRate %>%</strong>
                                        <span><%= studentSubmittedAttempts %> submitted attempts</span>
                                    </div>
                                    <i class="bi bi-check2-circle"></i>
                                </div>

                                <div class="dashboard-analytics-tile success">
                                    <div>
                                        <small>Result Publish Rate</small>
                                        <strong><%= studentResultPublishRate %>%</strong>
                                        <span><%= studentPublishedResults %> published results</span>
                                    </div>
                                    <i class="bi bi-bar-chart-fill"></i>
                                </div>

                                <div class="dashboard-analytics-tile warning">
                                    <div>
                                        <small>Appeal Resolution</small>
                                        <strong><%= studentAppealResolutionRate %>%</strong>
                                        <span><%= studentResolvedAppeals + studentRejectedAppeals %> completed appeals</span>
                                    </div>
                                    <i class="bi bi-arrow-repeat"></i>
                                </div>

                                <div class="dashboard-analytics-tile info">
                                    <div>
                                        <small>Notification Read Rate</small>
                                        <strong><%= studentNotificationReadRate %>%</strong>
                                        <span><%= studentUnreadNotifications %> unread notifications</span>
                                    </div>
                                    <i class="bi bi-bell-fill"></i>
                                </div>
                            </div>

                            <div class="dashboard-progress-list mt-4">
                                <div class="dashboard-progress-item">
                                    <div class="d-flex justify-content-between mb-1">
                                        <span>Exam Completion Progress</span>
                                        <strong><%= studentExamCompletionRate %>%</strong>
                                    </div>
                                    <div class="progress dashboard-progress">
                                        <div class="progress-bar" style="width:<%= studentExamCompletionRate %>%;"></div>
                                    </div>
                                </div>

                                <div class="dashboard-progress-item">
                                    <div class="d-flex justify-content-between mb-1">
                                        <span>Result Publishing Progress</span>
                                        <strong><%= studentResultPublishRate %>%</strong>
                                    </div>
                                    <div class="progress dashboard-progress">
                                        <div class="progress-bar bg-success" style="width:<%= studentResultPublishRate %>%;"></div>
                                    </div>
                                </div>

                                <div class="dashboard-progress-item">
                                    <div class="d-flex justify-content-between mb-1">
                                        <span>Appeal Resolution Progress</span>
                                        <strong><%= studentAppealResolutionRate %>%</strong>
                                    </div>
                                    <div class="progress dashboard-progress">
                                        <div class="progress-bar bg-warning" style="width:<%= studentAppealResolutionRate %>%;"></div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Student Quick Actions -->
                <div class="row g-4 mb-4">
                    <div class="col-xl-8">
                        <div class="app-card p-4 h-100">
                            <div class="d-flex justify-content-between align-items-start flex-wrap gap-3 mb-3">
                                <div>
                                    <h4 class="fw-bold mb-1">Student Quick Actions</h4>
                                    <p class="text-secondary mb-0">
                                        Open your most important academic pages quickly.
                                    </p>
                                </div>

                                <span class="badge badge-soft-primary">Academic Workspace</span>
                            </div>

                            <div class="row g-3">
                                <div class="col-md-6 col-xl-4">
                                    <a href="<%= request.getContextPath() %>/my-exams">
                                        <div class="app-card quick-card student-quick-card">
                                            <div class="quick-icon"><i class="bi bi-laptop-fill"></i></div>
                                            <h5>My Exams</h5>
                                            <p>View available exams and continue your exam workflow.</p>
                                        </div>
                                    </a>
                                </div>

                                <div class="col-md-6 col-xl-4">
                                    <a href="<%= request.getContextPath() %>/my-results">
                                        <div class="app-card quick-card student-quick-card">
                                            <div class="quick-icon"><i class="bi bi-bar-chart-fill"></i></div>
                                            <h5>My Results</h5>
                                            <p>Check published results and performance information.</p>
                                        </div>
                                    </a>
                                </div>

                                <div class="col-md-6 col-xl-4">
                                    <a href="<%= request.getContextPath() %>/result-appeals">
                                        <div class="app-card quick-card student-quick-card">
                                            <div class="quick-icon"><i class="bi bi-arrow-repeat"></i></div>
                                            <h5>Result Appeals</h5>
                                            <p>Submit or track result recheck requests.</p>
                                        </div>
                                    </a>
                                </div>

                                <div class="col-md-6 col-xl-4">
                                    <a href="<%= request.getContextPath() %>/notifications">
                                        <div class="app-card quick-card student-quick-card">
                                            <div class="quick-icon"><i class="bi bi-bell-fill"></i></div>
                                            <h5>Notifications</h5>
                                            <p>Read important academic updates and alerts.</p>
                                        </div>
                                    </a>
                                </div>

                                <div class="col-md-6 col-xl-4">
                                    <a href="<%= request.getContextPath() %>/feedback">
                                        <div class="app-card quick-card student-quick-card">
                                            <div class="quick-icon"><i class="bi bi-chat-dots-fill"></i></div>
                                            <h5>Feedback</h5>
                                            <p>Send academic concerns or support requests.</p>
                                        </div>
                                    </a>
                                </div>

                                <div class="col-md-6 col-xl-4">
                                    <a href="<%= request.getContextPath() %>/notices">
                                        <div class="app-card quick-card student-quick-card">
                                            <div class="quick-icon"><i class="bi bi-megaphone-fill"></i></div>
                                            <h5>Notices</h5>
                                            <p>View academic announcements and exam notices.</p>
                                        </div>
                                    </a>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="col-xl-4">
                        <div class="app-card p-4 h-100">
                            <div class="d-flex justify-content-between align-items-start mb-3">
                                <div>
                                    <h4 class="fw-bold mb-1">Student Summary</h4>
                                    <p class="text-secondary mb-0">Your academic snapshot</p>
                                </div>

                                <span class="badge <%= studentPriorityBadge %>">
                                    <%= studentPriorityScore %> Items
                                </span>
                            </div>

                            <div class="student-summary-stack">
                                <div class="student-summary-item">
                                    <small>Available Exams</small>
                                    <strong><%= studentAvailableExams %> Ready</strong>
                                    <span>Go to My Exams to begin available attempts</span>
                                </div>

                                <div class="student-summary-item">
                                    <small>Results</small>
                                    <strong><%= studentPublishedResults %> Published</strong>
                                    <span>Average published score: <%= studentAverageMarks %>%</span>
                                </div>

                                <div class="student-summary-item">
                                    <small>Appeals</small>
                                    <strong><%= studentTotalAppeals %> Total</strong>
                                    <span><%= studentPendingAppeals %> pending · <%= studentUnderReviewAppeals %> under review</span>
                                </div>

                                <div class="student-summary-item">
                                    <small>Latest Notice</small>
                                    <strong>
                                        <%= latestStudentNotice != null ? FileUtil.h(latestStudentNotice.getTitle()) : "No notice available" %>
                                    </strong>
                                    <span>
                                        <%= latestStudentNotice != null ? FileUtil.h(latestStudentNotice.getNoticeDate()) : "Check notices later for academic announcements" %>
                                    </span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

            <% } else if (isLecturer) { %>

                <!-- Lecturer Dashboard Hero -->
                <div class="hero-card lecturer-hero-card mb-4">
                    <div class="d-flex justify-content-between align-items-start flex-wrap gap-3">
                        <div>
                            <span class="badge badge-soft-primary mb-3">
                                <i class="bi bi-person-video3 me-1"></i>
                                Lecturer Workload Dashboard
                            </span>

                            <h1 class="hero-title">Welcome, <%= FileUtil.h(userDisplayName) %></h1>

                            <p class="hero-text">
                                Manage active exams, review submissions, complete manual essay marking,
                                monitor result appeals, and respond to academic support tasks from one focused workspace.
                            </p>
                        </div>

                        <div class="d-flex gap-2 flex-wrap">
                            <a href="<%= request.getContextPath() %>/submissions" class="btn btn-primary">
                                <i class="bi bi-inboxes-fill me-2"></i>
                                View Submissions
                            </a>

                            <a href="<%= request.getContextPath() %>/questions" class="btn btn-outline-primary">
                                <i class="bi bi-patch-question-fill me-2"></i>
                                Question Bank
                            </a>

                            <a href="<%= request.getContextPath() %>/result-appeals" class="btn btn-outline-primary">
                                <i class="bi bi-arrow-repeat me-2"></i>
                                Appeals
                            </a>
                        </div>
                    </div>
                </div>

                <!-- Lecturer KPI Cards -->
                <div class="row g-3 mb-4">
                    <div class="col-md-6 col-xl-3">
                        <div class="app-card stat-card lecturer-stat-card">
                            <div class="d-flex justify-content-between gap-3">
                                <div>
                                    <div class="stat-label">Active Exams</div>
                                    <div class="stat-value"><%= activeAcademicExams %></div>
                                    <div class="stat-meta"><%= todayExams %> today · <%= upcomingExams %> upcoming</div>
                                </div>

                                <div class="stat-icon">
                                    <i class="bi bi-journal-check"></i>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="col-md-6 col-xl-3">
                        <div class="app-card stat-card lecturer-stat-card">
                            <div class="d-flex justify-content-between gap-3">
                                <div>
                                    <div class="stat-label">Submissions</div>
                                    <div class="stat-value"><%= submittedAttempts %></div>
                                    <div class="stat-meta"><%= submittedOnlyAttempts %> newly submitted</div>
                                </div>

                                <div class="stat-icon">
                                    <i class="bi bi-send-check-fill"></i>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="col-md-6 col-xl-3">
                        <div class="app-card stat-card lecturer-stat-card">
                            <div class="d-flex justify-content-between gap-3">
                                <div>
                                    <div class="stat-label">Manual Review</div>
                                    <div class="stat-value"><%= manualReviewAttempts %></div>
                                    <div class="stat-meta">Essay answers need marking</div>
                                </div>

                                <div class="stat-icon">
                                    <i class="bi bi-pencil-square"></i>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="col-md-6 col-xl-3">
                        <div class="app-card stat-card lecturer-stat-card">
                            <div class="d-flex justify-content-between gap-3">
                                <div>
                                    <div class="stat-label">Appeals</div>
                                    <div class="stat-value"><%= pendingAppeals %></div>
                                    <div class="stat-meta"><%= underReviewAppeals %> under review</div>
                                </div>

                                <div class="stat-icon">
                                    <i class="bi bi-arrow-repeat"></i>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Lecturer Workload Main Section -->
                <div class="row g-4 mb-4">
                    <div class="col-xl-5">
                        <div class="app-card p-4 h-100 lecturer-workload-card">
                            <div class="d-flex justify-content-between align-items-start flex-wrap gap-3 mb-3">
                                <div>
                                    <h4 class="fw-bold mb-1">Workload Priority Queue</h4>
                                    <p class="text-secondary mb-0">
                                        Focus on the academic tasks that need attention first.
                                    </p>
                                </div>

                                <span class="badge <%= workloadBadge %>">
                                    <i class="bi bi-lightning-charge-fill me-1"></i>
                                    <%= workloadLabel %>
                                </span>
                            </div>

                            <div class="priority-task-list lecturer-priority-list">
                                <a href="<%= request.getContextPath() %>/submissions" class="priority-task-item warning">
                                    <div>
                                        <small>Manual Marking</small>
                                        <strong><%= manualReviewAttempts %></strong>
                                        <span>Essay submissions waiting for lecturer marks</span>
                                    </div>
                                    <i class="bi bi-chevron-right"></i>
                                </a>

                                <a href="<%= request.getContextPath() %>/submissions" class="priority-task-item primary">
                                    <div>
                                        <small>Submission Review</small>
                                        <strong><%= submittedOnlyAttempts + autoMarkedAttempts %></strong>
                                        <span>Newly submitted or auto-marked attempts</span>
                                    </div>
                                    <i class="bi bi-chevron-right"></i>
                                </a>

                                <a href="<%= request.getContextPath() %>/result-appeals" class="priority-task-item info">
                                    <div>
                                        <small>Result Appeals</small>
                                        <strong><%= pendingAppeals %></strong>
                                        <span>Student recheck requests pending staff response</span>
                                    </div>
                                    <i class="bi bi-chevron-right"></i>
                                </a>

                                <a href="<%= request.getContextPath() %>/feedback" class="priority-task-item danger">
                                    <div>
                                        <small>Feedback Queue</small>
                                        <strong><%= openFeedback %></strong>
                                        <span>Open student support and exam concerns</span>
                                    </div>
                                    <i class="bi bi-chevron-right"></i>
                                </a>

                                <a href="<%= request.getContextPath() %>/notifications" class="priority-task-item primary">
                                    <div>
                                        <small>Unread Notifications</small>
                                        <strong><%= unreadForCurrentRole %></strong>
                                        <span>Role-based academic alerts for you</span>
                                    </div>
                                    <i class="bi bi-chevron-right"></i>
                                </a>
                            </div>
                        </div>
                    </div>

                    <div class="col-xl-7">
                        <div class="app-card p-4 h-100 lecturer-progress-card">
                            <div class="d-flex justify-content-between align-items-start flex-wrap gap-3 mb-4">
                                <div>
                                    <h4 class="fw-bold mb-1">Assessment Workflow Progress</h4>
                                    <p class="text-secondary mb-0">
                                        Monitor marking, publishing, question readiness, and appeal handling progress.
                                    </p>
                                </div>

                                <span class="badge badge-soft-primary">
                                    <i class="bi bi-activity me-1"></i>
                                    Live Workload
                                </span>
                            </div>

                            <div class="dashboard-analytics-grid lecturer-analytics-grid">
                                <div class="dashboard-analytics-tile success">
                                    <div>
                                        <small>Marking Completion</small>
                                        <strong><%= markingCompletionRate %>%</strong>
                                        <span><%= autoMarkedAttempts + markedAttempts + publishedAttempts %> processed submissions</span>
                                    </div>
                                    <i class="bi bi-check2-circle"></i>
                                </div>

                                <div class="dashboard-analytics-tile warning">
                                    <div>
                                        <small>Manual Review Load</small>
                                        <strong><%= manualReviewRate %>%</strong>
                                        <span><%= manualReviewAttempts %> submissions need review</span>
                                    </div>
                                    <i class="bi bi-pencil-square"></i>
                                </div>

                                <div class="dashboard-analytics-tile primary">
                                    <div>
                                        <small>Appeal Resolution</small>
                                        <strong><%= appealResolutionRate %>%</strong>
                                        <span><%= resolvedAppeals + rejectedAppeals %> completed appeals</span>
                                    </div>
                                    <i class="bi bi-arrow-repeat"></i>
                                </div>

                                <div class="dashboard-analytics-tile info">
                                    <div>
                                        <small>Question Readiness</small>
                                        <strong><%= questionCompletion %>%</strong>
                                        <span><%= activeQuestions + publishedQuestions %> ready questions</span>
                                    </div>
                                    <i class="bi bi-patch-question-fill"></i>
                                </div>
                            </div>

                            <div class="dashboard-progress-list mt-4">
                                <div class="dashboard-progress-item">
                                    <div class="d-flex justify-content-between mb-1">
                                        <span>Result Publishing Progress</span>
                                        <strong><%= attemptPublishRate %>%</strong>
                                    </div>
                                    <div class="progress dashboard-progress">
                                        <div class="progress-bar" style="width:<%= attemptPublishRate %>%;"></div>
                                    </div>
                                </div>

                                <div class="dashboard-progress-item">
                                    <div class="d-flex justify-content-between mb-1">
                                        <span>Feedback Review Progress</span>
                                        <strong><%= feedbackReviewRate %>%</strong>
                                    </div>
                                    <div class="progress dashboard-progress">
                                        <div class="progress-bar bg-warning" style="width:<%= feedbackReviewRate %>%;"></div>
                                    </div>
                                </div>

                                <div class="dashboard-progress-item">
                                    <div class="d-flex justify-content-between mb-1">
                                        <span>Notification Read Progress</span>
                                        <strong><%= notificationReadRate %>%</strong>
                                    </div>
                                    <div class="progress dashboard-progress">
                                        <div class="progress-bar bg-info" style="width:<%= notificationReadRate %>%;"></div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Lecturer Quick Actions -->
                <div class="row g-4 mb-4">
                    <div class="col-xl-8">
                        <div class="app-card p-4 h-100">
                            <div class="d-flex justify-content-between align-items-start flex-wrap gap-3 mb-3">
                                <div>
                                    <h4 class="fw-bold mb-1">Lecturer Quick Actions</h4>
                                    <p class="text-secondary mb-0">
                                        Open the main lecturer workflows quickly.
                                    </p>
                                </div>

                                <span class="badge badge-soft-primary">Academic Tools</span>
                            </div>

                            <div class="row g-3">
                                <div class="col-md-6 col-xl-4">
                                    <a href="<%= request.getContextPath() %>/exams">
                                        <div class="app-card quick-card lecturer-quick-card">
                                            <div class="quick-icon">
                                                <i class="bi bi-journal-check"></i>
                                            </div>
                                            <h5>Exam Management</h5>
                                            <p>Review active, scheduled, ongoing, and published exams.</p>
                                        </div>
                                    </a>
                                </div>

                                <div class="col-md-6 col-xl-4">
                                    <a href="<%= request.getContextPath() %>/questions">
                                        <div class="app-card quick-card lecturer-quick-card">
                                            <div class="quick-icon">
                                                <i class="bi bi-patch-question-fill"></i>
                                            </div>
                                            <h5>Question Bank</h5>
                                            <p>Manage MCQ and essay questions for exams.</p>
                                        </div>
                                    </a>
                                </div>

                                <div class="col-md-6 col-xl-4">
                                    <a href="<%= request.getContextPath() %>/submissions">
                                        <div class="app-card quick-card lecturer-quick-card">
                                            <div class="quick-icon">
                                                <i class="bi bi-inboxes-fill"></i>
                                            </div>
                                            <h5>Submissions</h5>
                                            <p>Review submitted attempts and answer records.</p>
                                        </div>
                                    </a>
                                </div>

                                <div class="col-md-6 col-xl-4">
                                    <a href="<%= request.getContextPath() %>/results">
                                        <div class="app-card quick-card lecturer-quick-card">
                                            <div class="quick-icon">
                                                <i class="bi bi-bar-chart-fill"></i>
                                            </div>
                                            <h5>Results</h5>
                                            <p>Monitor result processing, verification, and publishing.</p>
                                        </div>
                                    </a>
                                </div>

                                <div class="col-md-6 col-xl-4">
                                    <a href="<%= request.getContextPath() %>/result-appeals">
                                        <div class="app-card quick-card lecturer-quick-card">
                                            <div class="quick-icon">
                                                <i class="bi bi-arrow-repeat"></i>
                                            </div>
                                            <h5>Result Appeals</h5>
                                            <p>Review student recheck requests and update appeal status.</p>
                                        </div>
                                    </a>
                                </div>

                                <div class="col-md-6 col-xl-4">
                                    <a href="<%= request.getContextPath() %>/reports">
                                        <div class="app-card quick-card lecturer-quick-card">
                                            <div class="quick-icon">
                                                <i class="bi bi-filetype-csv"></i>
                                            </div>
                                            <h5>Reports</h5>
                                            <p>Export academic records and workload reports as CSV.</p>
                                        </div>
                                    </a>
                                </div>

                                <div class="col-md-6 col-xl-4">
                                    <a href="<%= request.getContextPath() %>/notifications">
                                        <div class="app-card quick-card lecturer-quick-card">
                                            <div class="quick-icon">
                                                <i class="bi bi-bell-fill"></i>
                                            </div>
                                            <h5>Notifications</h5>
                                            <p>View academic alerts and role-based updates.</p>
                                        </div>
                                    </a>
                                </div>

                                <div class="col-md-6 col-xl-4">
                                    <a href="<%= request.getContextPath() %>/feedback">
                                        <div class="app-card quick-card lecturer-quick-card">
                                            <div class="quick-icon">
                                                <i class="bi bi-chat-dots-fill"></i>
                                            </div>
                                            <h5>Feedback</h5>
                                            <p>Review student messages and academic support concerns.</p>
                                        </div>
                                    </a>
                                </div>

                                <div class="col-md-6 col-xl-4">
                                    <a href="<%= request.getContextPath() %>/integrity">
                                        <div class="app-card quick-card lecturer-quick-card">
                                            <div class="quick-icon">
                                                <i class="bi bi-shield-check"></i>
                                            </div>
                                            <h5>Integrity Review</h5>
                                            <p>Review suspicious activity and exam integrity signals.</p>
                                        </div>
                                    </a>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="col-xl-4">
                        <div class="app-card p-4 h-100">
                            <div class="d-flex justify-content-between align-items-start mb-3">
                                <div>
                                    <h4 class="fw-bold mb-1">Lecturer Summary</h4>
                                    <p class="text-secondary mb-0">Today’s academic workload snapshot</p>
                                </div>

                                <span class="badge <%= workloadBadge %>">
                                    <%= lecturerWorkloadScore %> Tasks
                                </span>
                            </div>

                            <div class="lecturer-summary-stack">
                                <div class="lecturer-summary-item">
                                    <small>Next Exam</small>
                                    <strong><%= FileUtil.h(nextExamSubject) %></strong>
                                    <span><%= FileUtil.h(nextExamDate) %></span>
                                </div>

                                <div class="lecturer-summary-item">
                                    <small>Question Bank</small>
                                    <strong><%= totalQuestions %> Questions</strong>
                                    <span><%= mcqQuestions %> MCQ · <%= essayQuestions %> Essay</span>
                                </div>

                                <div class="lecturer-summary-item">
                                    <small>Feedback</small>
                                    <strong><%= openFeedback %> Open</strong>
                                    <span><%= todayFeedback %> received today</span>
                                </div>

                                <div class="lecturer-summary-item">
                                    <small>Latest Notice</small>
                                    <strong>
                                        <%= latestNotice != null ? FileUtil.h(latestNotice.getTitle()) : "No notice available" %>
                                    </strong>
                                    <span>
                                        <%= latestNotice != null ? FileUtil.h(latestNotice.getNoticeDate()) : "Publish notices from Notices page" %>
                                    </span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

            <% } else { %>

                <!-- Admin Dashboard Hero -->
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
                                published results, notices, feedback, appeals, notifications, reports, and platform activity.
                            </p>
                        </div>

                        <div class="d-flex gap-2 flex-wrap">
                            <a href="<%= request.getContextPath() %>/exams" class="btn btn-primary">
                                <i class="bi bi-journal-plus me-2"></i>
                                Manage Exams
                            </a>

                            <a href="<%= request.getContextPath() %>/reports" class="btn btn-outline-primary">
                                <i class="bi bi-filetype-csv me-2"></i>
                                Export Reports
                            </a>
                        </div>
                    </div>
                </div>

                <!-- Admin KPI Cards -->
                <div class="row g-3 mb-4">
                    <div class="col-md-6 col-xl-3">
                        <div class="app-card stat-card">
                            <div class="d-flex justify-content-between gap-3">
                                <div>
                                    <div class="stat-label">Students</div>
                                    <div class="stat-value"><%= totalStudents %></div>
                                    <div class="stat-meta"><%= eligibleStudents %> eligible · <%= pendingStudents %> pending</div>
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

                    <div class="col-md-6 col-xl-3">
                        <div class="app-card stat-card">
                            <div class="d-flex justify-content-between gap-3">
                                <div>
                                    <div class="stat-label">System Alerts</div>
                                    <div class="stat-value"><%= adminRiskScore %></div>
                                    <div class="stat-meta"><%= pendingAppeals %> appeals · <%= totalUnreadNotifications %> unread</div>
                                </div>

                                <div class="stat-icon">
                                    <i class="bi bi-shield-exclamation"></i>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Admin Analytics Upgrade -->
                <div class="row g-4 mb-4">
                    <div class="col-xl-8">
                        <div class="app-card p-4 h-100 admin-analytics-card">
                            <div class="d-flex justify-content-between align-items-start flex-wrap gap-3 mb-4">
                                <div>
                                    <h4 class="fw-bold mb-1">Executive Analytics Snapshot</h4>
                                    <p class="text-secondary mb-0">
                                        Decision-support overview for academic operations, marking progress, appeals, and notifications.
                                    </p>
                                </div>

                                <span class="badge <%= adminRiskBadge %>">
                                    <i class="bi bi-shield-exclamation me-1"></i>
                                    <%= adminRiskLabel %>
                                </span>
                            </div>

                            <div class="dashboard-analytics-grid">
                                <div class="dashboard-analytics-tile success">
                                    <div>
                                        <small>Marking Completion</small>
                                        <strong><%= markingCompletionRate %>%</strong>
                                        <span><%= autoMarkedAttempts + markedAttempts + publishedAttempts %> processed attempts</span>
                                    </div>
                                    <i class="bi bi-check2-circle"></i>
                                </div>

                                <div class="dashboard-analytics-tile warning">
                                    <div>
                                        <small>Manual Review Load</small>
                                        <strong><%= manualReviewRate %>%</strong>
                                        <span><%= manualReviewAttempts %> submissions need essay review</span>
                                    </div>
                                    <i class="bi bi-pencil-square"></i>
                                </div>

                                <div class="dashboard-analytics-tile primary">
                                    <div>
                                        <small>Appeal Resolution</small>
                                        <strong><%= appealResolutionRate %>%</strong>
                                        <span><%= pendingAppeals %> pending appeals</span>
                                    </div>
                                    <i class="bi bi-arrow-repeat"></i>
                                </div>

                                <div class="dashboard-analytics-tile info">
                                    <div>
                                        <small>Notification Read Rate</small>
                                        <strong><%= notificationReadRate %>%</strong>
                                        <span><%= totalUnreadNotifications %> unread notifications</span>
                                    </div>
                                    <i class="bi bi-bell-fill"></i>
                                </div>
                            </div>

                            <div class="dashboard-progress-list mt-4">
                                <div class="dashboard-progress-item">
                                    <div class="d-flex justify-content-between mb-1">
                                        <span>Submission Publishing</span>
                                        <strong><%= attemptPublishRate %>%</strong>
                                    </div>
                                    <div class="progress dashboard-progress">
                                        <div class="progress-bar" style="width:<%= attemptPublishRate %>%;"></div>
                                    </div>
                                </div>

                                <div class="dashboard-progress-item">
                                    <div class="d-flex justify-content-between mb-1">
                                        <span>Question Bank Readiness</span>
                                        <strong><%= questionCompletion %>%</strong>
                                    </div>
                                    <div class="progress dashboard-progress">
                                        <div class="progress-bar bg-success" style="width:<%= questionCompletion %>%;"></div>
                                    </div>
                                </div>

                                <div class="dashboard-progress-item">
                                    <div class="d-flex justify-content-between mb-1">
                                        <span>Feedback Review Rate</span>
                                        <strong><%= feedbackReviewRate %>%</strong>
                                    </div>
                                    <div class="progress dashboard-progress">
                                        <div class="progress-bar bg-warning" style="width:<%= feedbackReviewRate %>%;"></div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="col-xl-4">
                        <div class="app-card p-4 h-100 admin-task-card">
                            <div class="d-flex justify-content-between align-items-start mb-3">
                                <div>
                                    <h4 class="fw-bold mb-1">Priority Work Queue</h4>
                                    <p class="text-secondary mb-0">
                                        Items that need staff attention.
                                    </p>
                                </div>

                                <span class="badge <%= adminRiskBadge %>">
                                    <%= adminRiskScore %> Tasks
                                </span>
                            </div>

                            <div class="priority-task-list">
                                <a href="<%= request.getContextPath() %>/submissions" class="priority-task-item warning">
                                    <div>
                                        <small>Manual Marking</small>
                                        <strong><%= manualReviewAttempts %></strong>
                                        <span>Essay submissions pending review</span>
                                    </div>
                                    <i class="bi bi-chevron-right"></i>
                                </a>

                                <a href="<%= request.getContextPath() %>/result-appeals" class="priority-task-item primary">
                                    <div>
                                        <small>Result Appeals</small>
                                        <strong><%= pendingAppeals %></strong>
                                        <span>Student recheck requests pending</span>
                                    </div>
                                    <i class="bi bi-chevron-right"></i>
                                </a>

                                <a href="<%= request.getContextPath() %>/feedback" class="priority-task-item info">
                                    <div>
                                        <small>Feedback</small>
                                        <strong><%= openFeedback %></strong>
                                        <span>Messages awaiting staff response</span>
                                    </div>
                                    <i class="bi bi-chevron-right"></i>
                                </a>

                                <a href="<%= request.getContextPath() %>/notifications" class="priority-task-item danger">
                                    <div>
                                        <small>Notifications</small>
                                        <strong><%= totalUnreadNotifications %></strong>
                                        <span>Unread role-based notifications</span>
                                    </div>
                                    <i class="bi bi-chevron-right"></i>
                                </a>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Admin Quick Modules -->
                <div class="row g-4 mb-4">
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
                                    <a href="<%= request.getContextPath() %>/submissions">
                                        <div class="app-card quick-card">
                                            <div class="quick-icon">
                                                <i class="bi bi-inboxes-fill"></i>
                                            </div>
                                            <h5>Submissions</h5>
                                            <p>Review submitted attempts, MCQ answers, and essay responses.</p>
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

                                <div class="col-md-6 col-xl-4">
                                    <a href="<%= request.getContextPath() %>/result-appeals">
                                        <div class="app-card quick-card">
                                            <div class="quick-icon">
                                                <i class="bi bi-arrow-repeat"></i>
                                            </div>
                                            <h5>Result Appeals</h5>
                                            <p>Review student result recheck requests and update appeal status.</p>
                                        </div>
                                    </a>
                                </div>

                                <div class="col-md-6 col-xl-4">
                                    <a href="<%= request.getContextPath() %>/reports">
                                        <div class="app-card quick-card">
                                            <div class="quick-icon">
                                                <i class="bi bi-filetype-csv"></i>
                                            </div>
                                            <h5>Export Reports</h5>
                                            <p>Download students, exams, submissions, results, appeals, and feedback as CSV files.</p>
                                        </div>
                                    </a>
                                </div>

                                <div class="col-md-6 col-xl-4">
                                    <a href="<%= request.getContextPath() %>/notifications">
                                        <div class="app-card quick-card">
                                            <div class="quick-icon">
                                                <i class="bi bi-bell-fill"></i>
                                            </div>
                                            <h5>Notifications</h5>
                                            <p>View important academic updates and role-based alerts.</p>
                                        </div>
                                    </a>
                                </div>

                                <div class="col-md-6 col-xl-4">
                                    <a href="<%= request.getContextPath() %>/audit-logs">
                                        <div class="app-card quick-card">
                                            <div class="quick-icon">
                                                <i class="bi bi-shield-lock-fill"></i>
                                            </div>
                                            <h5>Audit Logs</h5>
                                            <p>Review report exports, access events, and staff activity logs.</p>
                                        </div>
                                    </a>
                                </div>

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

                    <div class="col-xl-4">
                        <div class="app-card p-4 h-100">
                            <div class="d-flex justify-content-between align-items-start mb-3">
                                <div>
                                    <h4 class="fw-bold mb-1">Platform Summary</h4>
                                    <p class="text-secondary mb-0">Current academic system status</p>
                                </div>

                                <span class="badge badge-soft-success">
                                    Online
                                </span>
                            </div>

                            <div class="lecturer-summary-stack">
                                <div class="lecturer-summary-item">
                                    <small>Users</small>
                                    <strong><%= totalUsers %> Accounts</strong>
                                    <span>Admin, lecturer, and student access</span>
                                </div>

                                <div class="lecturer-summary-item">
                                    <small>Question Bank</small>
                                    <strong><%= totalQuestions %> Questions</strong>
                                    <span><%= mcqQuestions %> MCQ · <%= essayQuestions %> Essay</span>
                                </div>

                                <div class="lecturer-summary-item">
                                    <small>Results</small>
                                    <strong><%= totalResults %> Records</strong>
                                    <span><%= publishedResults %> published · <%= verifiedResults %> verified</span>
                                </div>

                                <div class="lecturer-summary-item">
                                    <small>Next Exam</small>
                                    <strong><%= FileUtil.h(nextExamSubject) %></strong>
                                    <span><%= FileUtil.h(nextExamDate) %></span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

            <% } %>

            <!-- Shared Bottom Summary -->
            <div class="row g-4">
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
                                <small class="text-secondary">Students complete the online exam console.</small>
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
                                        <%= latestNotice != null ? FileUtil.h(latestNotice.getDescription()) : "Publish a notice to show the latest announcement here." %>
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
                                <h4 class="fw-bold mb-1">Support & Appeals Summary</h4>
                                <p class="text-secondary mb-0">Student support workload</p>
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
                                <span class="text-secondary">In Review</span>
                                <span class="fw-bold"><%= inReviewFeedback %></span>
                            </div>

                            <div class="d-flex justify-content-between mb-3">
                                <span class="text-secondary">Resolved / Closed</span>
                                <span class="fw-bold"><%= resolvedFeedback %> / <%= closedFeedback %></span>
                            </div>

                            <div class="progress" style="height: 9px;">
                                <div class="progress-bar bg-warning" style="width: <%= feedbackReviewRate %>%;"></div>
                            </div>

                            <small class="text-secondary d-block mt-2">
                                <%= feedbackReviewRate %>% of feedback messages have been completed.
                            </small>
                        </div>

                        <div class="soft-divider"></div>

                        <div class="feedback-card shadow-none">
                            <div class="d-flex justify-content-between mb-2">
                                <span class="text-secondary">Pending Appeals</span>
                                <span class="fw-bold"><%= pendingAppeals %></span>
                            </div>

                            <div class="d-flex justify-content-between mb-2">
                                <span class="text-secondary">Under Review</span>
                                <span class="fw-bold"><%= underReviewAppeals %></span>
                            </div>

                            <div class="d-flex justify-content-between">
                                <span class="text-secondary">Resolved / Rejected</span>
                                <span class="fw-bold"><%= resolvedAppeals %> / <%= rejectedAppeals %></span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

        </section>
    </main>
</div>

<%@ include file="includes/footer.jsp" %>