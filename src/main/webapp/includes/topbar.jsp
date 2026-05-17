<%--
    Topbar component for Nextexam dashboard layout.
    Displays page actions, user information, notifications, and responsive navigation controls.
    Responsible Member: IT25103045 - De Silva H.L.D.C.P.C
--%>

<%@ page import="lk.nextexam.dao.FileUtil" %>
<%@ page import="lk.nextexam.dao.NotificationDAO" %>

<%
    String tbUsername = "";
    String tbDisplayName = "";
    String tbUserRole = "";
    String tbUserEmail = "";
    String tbUserId = "";

    if (session != null) {
        if (session.getAttribute("username") != null) {
            tbUsername = session.getAttribute("username").toString();
        }

        if (session.getAttribute("displayName") != null) {
            tbDisplayName = session.getAttribute("displayName").toString();
        }

        if (session.getAttribute("userRole") != null) {
            tbUserRole = session.getAttribute("userRole").toString();
        }

        if (session.getAttribute("userEmail") != null) {
            tbUserEmail = session.getAttribute("userEmail").toString();
        }

        if (session.getAttribute("userId") != null) {
            tbUserId = session.getAttribute("userId").toString();
        }
    }

    String tbDisplayUsername = tbDisplayName != null && !tbDisplayName.trim().isEmpty()
            ? tbDisplayName.trim()
            : tbUsername;

    if (tbDisplayUsername == null || tbDisplayUsername.trim().isEmpty()) {
        tbDisplayUsername = "Guest User";
    }

    String tbDisplayRole = tbUserRole != null && !tbUserRole.trim().isEmpty()
            ? tbUserRole.trim()
            : "Guest";

    String tbSafeTitle = "Dashboard";

    try {
        tbSafeTitle = topbarTitle != null && !topbarTitle.trim().isEmpty()
                ? topbarTitle.trim()
                : "Dashboard";
    } catch (Exception e) {
        tbSafeTitle = "Dashboard";
    }

    String tbSubtitle = "NextExamLK Secure Examination Platform";
    String tbRoleIcon = "bi-person-badge";

    if ("Student".equalsIgnoreCase(tbDisplayRole)) {
        tbSubtitle = "Student examination workspace";
        tbRoleIcon = "bi-mortarboard-fill";
    } else if ("Lecturer".equalsIgnoreCase(tbDisplayRole)) {
        tbSubtitle = "Lecturer assessment management workspace";
        tbRoleIcon = "bi-person-video3";
    } else if ("Admin".equalsIgnoreCase(tbDisplayRole)) {
        tbSubtitle = "Administrative control workspace";
        tbRoleIcon = "bi-shield-lock-fill";
    }

    String tbInitial = "U";

    if (tbDisplayUsername != null && !tbDisplayUsername.trim().isEmpty()) {
        tbInitial = tbDisplayUsername.trim().substring(0, 1).toUpperCase();
    }

    NotificationDAO tbNotificationDAO = new NotificationDAO();
    int tbUnreadNotifications = 0;

    try {
        tbUnreadNotifications = tbNotificationDAO.countUnreadForUser(application, tbUserId, tbDisplayRole);
    } catch (Exception e) {
        tbUnreadNotifications = 0;
    }
%>

<header class="topbar">
    <div class="d-flex align-items-center gap-3 topbar-left">
        <button class="btn btn-outline-primary mobile-menu-btn"
                id="mobileMenuBtn"
                type="button"
                aria-label="Open sidebar menu"
                aria-controls="sidebar"
                aria-expanded="false">
            <i class="bi bi-list"></i>
        </button>

        <div class="topbar-title-wrap">
            <div class="topbar-title">
                <%= FileUtil.h(tbSafeTitle) %>
            </div>

            <div class="topbar-subtitle">
                <%= FileUtil.h(tbSubtitle) %>
            </div>
        </div>
    </div>

    <div class="topbar-search d-none d-xl-block">
        <div class="input-group">
            <span class="input-group-text bg-white border-end-0">
                <i class="bi bi-search text-secondary"></i>
            </span>

            <input type="search"
                   id="topbarQuickSearch"
                   class="form-control border-start-0"
                   placeholder="Search current page..."
                   autocomplete="off">
        </div>
    </div>

    <div class="topbar-actions">
        <a href="<%= request.getContextPath() %>/notifications"
           class="notification-btn"
           title="View notifications"
           aria-label="View notifications">
            <i class="bi bi-bell-fill"></i>

            <% if (tbUnreadNotifications > 0) { %>
                <span class="notification-count">
                    <%= tbUnreadNotifications > 99 ? "99+" : tbUnreadNotifications %>
                </span>
            <% } %>
        </a>

        <div class="topbar-user d-none d-lg-flex">
            <div class="topbar-user-avatar">
                <span><%= FileUtil.h(tbInitial) %></span>
            </div>

            <div class="topbar-user-meta">
                <div class="topbar-user-name">
                    <%= FileUtil.h(tbDisplayUsername) %>
                </div>

                <div class="topbar-user-role">
                    <%= FileUtil.h(tbDisplayRole) %>
                </div>
            </div>
        </div>

        <span class="role-pill d-none d-sm-inline-flex align-items-center">
            <i class="bi <%= tbRoleIcon %> me-1"></i>
            <%= FileUtil.h(tbDisplayRole) %>
        </span>

        <a href="<%= request.getContextPath() %>/logout"
           class="btn btn-outline-danger btn-sm"
           title="Logout">
            <i class="bi bi-box-arrow-left me-1"></i>
            <span class="d-none d-md-inline">Logout</span>
        </a>
    </div>
</header>