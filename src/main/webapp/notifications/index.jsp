<%--
    Notification Center page for Nextexam.
    Shows user/role-based notifications and allows marking notifications as read.

    Responsible Member:
    IT25103045 - De Silva H.L.D.C.P.C
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ page import="java.util.List" %>
<%@ page import="lk.nextexam.dao.FileUtil" %>
<%@ page import="lk.nextexam.dao.NotificationDAO" %>
<%@ page import="lk.nextexam.model.Notification" %>

<%
    String pageTitle = "Notifications";
    String activeMenu = "notifications";
    String topbarTitle = "Notification Center";

    String sessionUserId = session != null && session.getAttribute("userId") != null
            ? String.valueOf(session.getAttribute("userId"))
            : "";

    String sessionRole = session != null && session.getAttribute("userRole") != null
            ? String.valueOf(session.getAttribute("userRole"))
            : "";

    NotificationDAO notificationDAO = new NotificationDAO();

    List<Notification> notifications = notificationDAO.getNotificationsForUser(application, sessionUserId, sessionRole);
    int unreadCount = notificationDAO.countUnreadForUser(application, sessionUserId, sessionRole);
    int totalCount = notifications != null ? notifications.size() : 0;
    int readCount = totalCount - unreadCount;

    String success = request.getParameter("success");
    String error = request.getParameter("error");

    String alertType = "";
    String alertMessage = "";

    if (success != null) {
        alertType = "success";

        if ("markedRead".equalsIgnoreCase(success)) {
            alertMessage = "Notification marked as read.";
        } else if ("allMarkedRead".equalsIgnoreCase(success)) {
            alertMessage = "All notifications marked as read.";
        } else if ("deleted".equalsIgnoreCase(success)) {
            alertMessage = "Notification deleted successfully.";
        } else {
            alertMessage = "Action completed successfully.";
        }
    }

    if (error != null) {
        alertType = "danger";
        alertMessage = "Notification action failed. Please try again.";
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
                            <i class="bi bi-bell-fill me-1"></i>
                            Nextexam Notification Center
                        </span>

                        <h1 class="hero-title">Notifications</h1>

                        <p class="hero-text">
                            View important academic updates such as result publishing, result appeals,
                            document verification, feedback responses, notices, and system messages.
                        </p>
                    </div>

                    <form action="<%= request.getContextPath() %>/notifications" method="post">
                        <input type="hidden" name="action" value="markAllRead">

                        <button type="submit" class="btn btn-primary">
                            <i class="bi bi-check2-all me-2"></i>
                            Mark All Read
                        </button>
                    </form>
                </div>
            </div>

            <% if (!alertMessage.isEmpty()) { %>
                <div class="alert alert-<%= FileUtil.h(alertType) %>" data-auto-close="5000">
                    <% if ("success".equals(alertType)) { %>
                        <i class="bi bi-check-circle-fill me-1"></i>
                    <% } else { %>
                        <i class="bi bi-exclamation-triangle-fill me-1"></i>
                    <% } %>
                    <%= FileUtil.h(alertMessage) %>
                </div>
            <% } %>

            <div class="row g-3 mb-4">
                <div class="col-md-4">
                    <div class="app-card stat-card">
                        <div class="d-flex justify-content-between gap-3">
                            <div>
                                <div class="stat-label">Total</div>
                                <div class="stat-value"><%= totalCount %></div>
                                <div class="stat-meta">All notifications</div>
                            </div>

                            <div class="stat-icon">
                                <i class="bi bi-bell-fill"></i>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-md-4">
                    <div class="app-card stat-card">
                        <div class="d-flex justify-content-between gap-3">
                            <div>
                                <div class="stat-label">Unread</div>
                                <div class="stat-value"><%= unreadCount %></div>
                                <div class="stat-meta">Need attention</div>
                            </div>

                            <div class="stat-icon">
                                <i class="bi bi-bell"></i>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-md-4">
                    <div class="app-card stat-card">
                        <div class="d-flex justify-content-between gap-3">
                            <div>
                                <div class="stat-label">Read</div>
                                <div class="stat-value"><%= readCount %></div>
                                <div class="stat-meta">Already reviewed</div>
                            </div>

                            <div class="stat-icon">
                                <i class="bi bi-check2-circle"></i>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="app-card p-4">
                <div class="d-flex justify-content-between align-items-start flex-wrap gap-3 mb-4">
                    <div>
                        <h4 class="fw-bold mb-1">Notification List</h4>
                        <p class="text-secondary mb-0">
                            Notifications shown here are filtered by your user ID and role.
                        </p>
                    </div>

                    <span class="badge badge-soft-primary">
                        <i class="bi bi-person-badge me-1"></i>
                        <%= FileUtil.h(sessionRole) %>
                    </span>
                </div>

                <% if (notifications == null || notifications.isEmpty()) { %>
                    <div class="empty-state">
                        <div class="empty-state-icon">
                            <i class="bi bi-bell-slash"></i>
                        </div>

                        <h5>No notifications yet</h5>
                        <p>Important academic updates will appear here.</p>
                    </div>
                <% } else { %>
                    <div class="notification-list">
                        <% for (Notification notification : notifications) { %>
                            <div class="notification-item <%= notification.isUnread() ? "unread" : "" %>">
                                <div class="notification-icon">
                                    <i class="bi <%= notification.getTypeIcon() %>"></i>
                                </div>

                                <div class="notification-content">
                                    <div class="notification-head">
                                        <div>
                                            <h5><%= FileUtil.h(notification.getTitle()) %></h5>
                                            <p><%= FileUtil.h(notification.getMessage()) %></p>
                                        </div>

                                        <div class="notification-badges">
                                            <span class="badge <%= notification.getTypeBadgeClass() %>">
                                                <%= FileUtil.h(notification.getType()) %>
                                            </span>

                                            <span class="badge <%= notification.getStatusBadgeClass() %>">
                                                <%= FileUtil.h(notification.getStatus()) %>
                                            </span>
                                        </div>
                                    </div>

                                    <div class="notification-meta">
                                        <span>
                                            <i class="bi bi-clock me-1"></i>
                                            <%= FileUtil.h(notification.getCreatedAt()) %>
                                        </span>

                                        <span>
                                            <i class="bi bi-person me-1"></i>
                                            <%= FileUtil.h(notification.getTargetUserId().isEmpty() ? notification.getTargetRole() : notification.getTargetUserId()) %>
                                        </span>
                                    </div>

                                    <div class="notification-actions">
                                        <% if (notification.isUnread()) { %>
                                            <form action="<%= request.getContextPath() %>/notifications" method="post">
                                                <input type="hidden" name="action" value="markRead">
                                                <input type="hidden" name="notificationId" value="<%= FileUtil.h(notification.getNotificationId()) %>">

                                                <button type="submit" class="btn btn-sm btn-outline-primary">
                                                    <i class="bi bi-check2 me-1"></i>
                                                    Mark Read
                                                </button>
                                            </form>
                                        <% } %>

                                        <% if (!notification.getTargetUrl().isEmpty()) { %>
                                            <a href="<%= request.getContextPath() %><%= FileUtil.h(notification.getTargetUrl()) %>"
                                               class="btn btn-sm btn-primary">
                                                <i class="bi bi-box-arrow-up-right me-1"></i>
                                                Open
                                            </a>
                                        <% } %>

                                        <form action="<%= request.getContextPath() %>/notifications" method="post">
                                            <input type="hidden" name="action" value="delete">
                                            <input type="hidden" name="notificationId" value="<%= FileUtil.h(notification.getNotificationId()) %>">

                                            <button type="submit"
                                                    class="btn btn-sm btn-outline-danger"
                                                    onclick="return confirm('Delete this notification?');">
                                                <i class="bi bi-trash me-1"></i>
                                                Delete
                                            </button>
                                        </form>
                                    </div>
                                </div>
                            </div>
                        <% } %>
                    </div>
                <% } %>
            </div>

        </section>
    </main>
</div>

<%@ include file="../includes/footer.jsp" %>