<%--
    Sidebar navigation component.
    Provides role-based navigation links for Admin, Lecturer, and Student users.
    Responsible Member: IT25103045 - De Silva H.L.D.C.P.C
--%>

<%@ page import="lk.nextexam.dao.FileUtil" %>

<%
    String sbActiveMenu = "";
    try {
        sbActiveMenu = activeMenu != null ? activeMenu : "";
    } catch (Exception e) {
        sbActiveMenu = "";
    }

    String sbUserRole = "";
    String sbUsername = "User";
    String sbDisplayName = "";

    if (session != null) {
        if (session.getAttribute("userRole") != null) {
            sbUserRole = session.getAttribute("userRole").toString();
        }

        if (session.getAttribute("displayName") != null) {
            sbDisplayName = session.getAttribute("displayName").toString();
        }

        if (session.getAttribute("username") != null) {
            sbUsername = session.getAttribute("username").toString();
        }
    }

    if (sbDisplayName != null && !sbDisplayName.trim().isEmpty()) {
        sbUsername = sbDisplayName;
    }

    boolean sbIsAdmin = "Admin".equalsIgnoreCase(sbUserRole);
    boolean sbIsLecturer = "Lecturer".equalsIgnoreCase(sbUserRole);
    boolean sbIsStudent = "Student".equalsIgnoreCase(sbUserRole);

    boolean sbCanManageAcademic = sbIsAdmin || sbIsLecturer;

    /*
     * Professional note:
     * Keep unfinished modules hidden until their Servlet + JSP are completed.
     * Turn these to true only after implementing each module.
     */
    boolean showAcademicExpansion = false;
    boolean showEngineeringExpansion = false;
    boolean showResultExpansion = false;
%>

<div class="sidebar-backdrop" id="sidebarBackdrop"></div>

<aside class="sidebar" id="sidebar">

    <!-- Brand -->
    <div class="sidebar-brand">
        <div class="brand-icon">
            <i class="bi bi-mortarboard-fill"></i>
        </div>

        <div>
            <div class="brand-title">NextExamLK</div>
            <div class="brand-subtitle">Examination Platform</div>
        </div>
    </div>

    <!-- User Mini Profile -->
    <div class="sidebar-profile">
        <div class="profile-avatar">
            <i class="bi bi-person-fill"></i>
        </div>

        <div class="profile-info">
            <div class="profile-name">
                <%= FileUtil.h(sbUsername) %>
            </div>

            <div class="profile-role">
                <%= FileUtil.h(sbUserRole.isEmpty() ? "Guest" : sbUserRole) %>
            </div>
        </div>
    </div>

    <nav class="sidebar-menu">

        <!-- Main -->
        <div class="sidebar-label">Main Workspace</div>

        <% if (sbIsAdmin || sbIsLecturer) { %>
            <a class="sidebar-link <%= "dashboard".equals(sbActiveMenu) ? "active" : "" %>"
               href="<%= request.getContextPath() %>/dashboard.jsp">
                <i class="bi bi-grid-1x2-fill"></i>
                <span>Dashboard</span>
            </a>
        <% } %>

        <% if (sbIsStudent) { %>
            <a class="sidebar-link <%= "my-exams".equals(sbActiveMenu) ? "active" : "" %>"
               href="<%= request.getContextPath() %>/my-exams">
                <i class="bi bi-laptop-fill"></i>
                <span>My Exams</span>
            </a>

            <a class="sidebar-link <%= "my-results".equals(sbActiveMenu) ? "active" : "" %>"
               href="<%= request.getContextPath() %>/my-results">
                <i class="bi bi-bar-chart-fill"></i>
                <span>My Results</span>
            </a>
        <% } %>

        <!-- Academic / Administration -->
        <% if (sbCanManageAcademic) { %>
            <div class="sidebar-label">Academic Management</div>

            <a class="sidebar-link <%= "faculties".equals(sbActiveMenu) ? "active" : "" %>"
               href="<%= request.getContextPath() %>/faculties">
                <i class="bi bi-building-fill"></i>
                <span>Faculties</span>
            </a>

            <a class="sidebar-link <%= "students".equals(sbActiveMenu) ? "active" : "" %>"
               href="<%= request.getContextPath() %>/students">
                <i class="bi bi-people-fill"></i>
                <span>Students</span>
            </a>

            <% if (showAcademicExpansion) { %>
                <a class="sidebar-link <%= "programmes".equals(sbActiveMenu) ? "active" : "" %>"
                   href="<%= request.getContextPath() %>/programmes">
                    <i class="bi bi-diagram-3-fill"></i>
                    <span>Programmes</span>
                </a>

                <a class="sidebar-link <%= "batches".equals(sbActiveMenu) ? "active" : "" %>"
                   href="<%= request.getContextPath() %>/batches">
                    <i class="bi bi-collection-fill"></i>
                    <span>Batches</span>
                </a>

                <a class="sidebar-link <%= "modules".equals(sbActiveMenu) ? "active" : "" %>"
                   href="<%= request.getContextPath() %>/modules">
                    <i class="bi bi-book-half"></i>
                    <span>Modules</span>
                </a>

                <a class="sidebar-link <%= "enrollments".equals(sbActiveMenu) ? "active" : "" %>"
                   href="<%= request.getContextPath() %>/enrollments">
                    <i class="bi bi-person-lines-fill"></i>
                    <span>Enrollments</span>
                </a>

                <a class="sidebar-link <%= "academic-calendar".equals(sbActiveMenu) ? "active" : "" %>"
                   href="<%= request.getContextPath() %>/academic-calendar">
                    <i class="bi bi-calendar-event-fill"></i>
                    <span>Academic Calendar</span>
                </a>
            <% } %>
        <% } %>

        <!-- Examination Management -->
        <% if (sbCanManageAcademic) { %>
            <div class="sidebar-label">Examination Control</div>

            <a class="sidebar-link <%= "exams".equals(sbActiveMenu) ? "active" : "" %>"
               href="<%= request.getContextPath() %>/exams">
                <i class="bi bi-journal-check"></i>
                <span>Exam Management</span>
            </a>

            <a class="sidebar-link <%= "integrity".equals(sbActiveMenu) ? "active" : "" %>"
               href="<%= request.getContextPath() %>/integrity">
                <i class="bi bi-shield-exclamation"></i>
                <span>Integrity Review</span>
            </a>

            <a class="sidebar-link <%= "submissions".equals(sbActiveMenu) ? "active" : "" %>"
               href="<%= request.getContextPath() %>/submissions">
                <i class="bi bi-inboxes-fill"></i>
                <span>Submissions</span>
            </a>

            <a class="sidebar-link <%= "questions".equals(sbActiveMenu) ? "active" : "" %>"
               href="<%= request.getContextPath() %>/questions">
                <i class="bi bi-patch-question-fill"></i>
                <span>Question Bank</span>
            </a>

            <a class="sidebar-link <%= "results".equals(sbActiveMenu) ? "active" : "" %>"
               href="<%= request.getContextPath() %>/results">
                <i class="bi bi-bar-chart-fill"></i>
                <span>Results</span>
            </a>

            <% if (showResultExpansion) { %>
                <a class="sidebar-link <%= "exam-attempts".equals(sbActiveMenu) ? "active" : "" %>"
                   href="<%= request.getContextPath() %>/exam-attempts">
                    <i class="bi bi-pencil-square"></i>
                    <span>Exam Attempts</span>
                </a>

                <a class="sidebar-link <%= "result-approval".equals(sbActiveMenu) ? "active" : "" %>"
                   href="<%= request.getContextPath() %>/result-approval">
                    <i class="bi bi-check2-circle"></i>
                    <span>Result Approval</span>
                </a>
            <% } %>
        <% } %>

        <!-- Future Academic Features -->
        <% if (sbCanManageAcademic && showResultExpansion) { %>
            <div class="sidebar-label">Assessment Records</div>

            <a class="sidebar-link <%= "attendance".equals(sbActiveMenu) ? "active" : "" %>"
               href="<%= request.getContextPath() %>/attendance">
                <i class="bi bi-clipboard-check-fill"></i>
                <span>Attendance</span>
            </a>

            <a class="sidebar-link <%= "eligibility".equals(sbActiveMenu) ? "active" : "" %>"
               href="<%= request.getContextPath() %>/eligibility">
                <i class="bi bi-shield-check"></i>
                <span>Exam Eligibility</span>
            </a>

            <a class="sidebar-link <%= "ca-marks".equals(sbActiveMenu) ? "active" : "" %>"
               href="<%= request.getContextPath() %>/ca-marks">
                <i class="bi bi-clipboard-data-fill"></i>
                <span>CA Marks</span>
            </a>

            <a class="sidebar-link <%= "gpa".equals(sbActiveMenu) ? "active" : "" %>"
               href="<%= request.getContextPath() %>/gpa">
                <i class="bi bi-award-fill"></i>
                <span>GPA / CGPA</span>
            </a>

            <a class="sidebar-link <%= "transcripts".equals(sbActiveMenu) ? "active" : "" %>"
               href="<%= request.getContextPath() %>/transcripts">
                <i class="bi bi-file-earmark-text-fill"></i>
                <span>Transcripts</span>
            </a>
        <% } %>

        <!-- Engineering Faculty - hidden until modules are implemented -->
        <% if (sbCanManageAcademic && showEngineeringExpansion) { %>
            <div class="sidebar-label">Engineering Faculty</div>

            <a class="sidebar-link <%= "lab-practicals".equals(sbActiveMenu) ? "active" : "" %>"
               href="<%= request.getContextPath() %>/lab-practicals">
                <i class="bi bi-tools"></i>
                <span>Lab Practicals</span>
            </a>

            <a class="sidebar-link <%= "workshops".equals(sbActiveMenu) ? "active" : "" %>"
               href="<%= request.getContextPath() %>/workshops">
                <i class="bi bi-gear-wide-connected"></i>
                <span>Workshop Attendance</span>
            </a>

            <a class="sidebar-link <%= "engineering-projects".equals(sbActiveMenu) ? "active" : "" %>"
               href="<%= request.getContextPath() %>/engineering-projects">
                <i class="bi bi-kanban-fill"></i>
                <span>Design Projects</span>
            </a>

            <a class="sidebar-link <%= "industrial-training".equals(sbActiveMenu) ? "active" : "" %>"
               href="<%= request.getContextPath() %>/industrial-training">
                <i class="bi bi-briefcase-fill"></i>
                <span>Industrial Training</span>
            </a>

            <a class="sidebar-link <%= "outcome-mapping".equals(sbActiveMenu) ? "active" : "" %>"
               href="<%= request.getContextPath() %>/outcome-mapping">
                <i class="bi bi-diagram-2-fill"></i>
                <span>Outcome Mapping</span>
            </a>
        <% } %>

        <!-- Communication -->
        <div class="sidebar-label">Communication</div>

        <a class="sidebar-link <%= "notices".equals(sbActiveMenu) ? "active" : "" %>"
           href="<%= request.getContextPath() %>/notices">
            <i class="bi bi-megaphone-fill"></i>
            <span>Notices</span>
        </a>

        <a class="sidebar-link <%= "feedback".equals(sbActiveMenu) ? "active" : "" %>"
           href="<%= request.getContextPath() %>/feedback">
            <i class="bi bi-chat-dots-fill"></i>
            <span>Feedback</span>
        </a>

        <a class="sidebar-link <%= "notifications".equals(sbActiveMenu) ? "active" : "" %>"
           href="<%= request.getContextPath() %>/notifications">
            <i class="bi bi-bell-fill"></i>
            <span>Notifications</span>
        </a>

        <a class="sidebar-link <%= "documents".equals(sbActiveMenu) ? "active" : "" %>"
           href="<%= request.getContextPath() %>/documents">
            <i class="bi bi-folder-check"></i>
            <span>
                <% if (sbIsStudent) { %>
                    My Documents
                <% } else { %>
                    Document Verification
                <% } %>
            </span>
        </a>

        <!-- System Administration -->
        <% if (sbIsAdmin) { %>
            <div class="sidebar-label">System Administration</div>

            <a class="sidebar-link <%= "users".equals(sbActiveMenu) ? "active" : "" %>"
               href="<%= request.getContextPath() %>/users">
                <i class="bi bi-person-gear"></i>
                <span>Users & Roles</span>
            </a>

            <% if (showAcademicExpansion) { %>
                <a class="sidebar-link <%= "settings".equals(sbActiveMenu) ? "active" : "" %>"
                   href="<%= request.getContextPath() %>/settings">
                    <i class="bi bi-sliders"></i>
                    <span>Settings</span>
                </a>
            <% } %>
        <% } %>

        <!-- Account -->
        <div class="sidebar-label">Account</div>

        <a class="sidebar-link <%= "profile".equals(sbActiveMenu) ? "active" : "" %>"
           href="<%= request.getContextPath() %>/profile">
            <i class="bi bi-person-circle"></i>
            <span>My Profile</span>
        </a>

        <a class="sidebar-link" href="<%= request.getContextPath() %>/logout">
            <i class="bi bi-box-arrow-left"></i>
            <span>Logout</span>
        </a>

    </nav>

    <!-- Platform Status Card -->
    <div class="sidebar-note">
        <strong>NextExamLK</strong>
        <p>
            Secure online examination, question management, submission tracking, and result management platform.
        </p>

        <div class="sidebar-status">
            <span class="status-dot"></span>
            <small>Platform Online</small>
        </div>
    </div>

</aside>