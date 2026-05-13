<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="lk.nextexam.dao.FileUtil" %>
<%@ page import="lk.nextexam.dao.NoticeDAO" %>
<%@ page import="lk.nextexam.model.Notice" %>

<%
    String pageTitle = "Notices";
    String activeMenu = "notices";
    String topbarTitle = "Notices";

    String currentUserRole = request.getAttribute("currentUserRole") != null
            ? request.getAttribute("currentUserRole").toString()
            : "";

    if (currentUserRole.isEmpty() && session != null && session.getAttribute("userRole") != null) {
        currentUserRole = session.getAttribute("userRole").toString();
    }

    boolean canManageNotices = request.getAttribute("canManageNotices") != null
            && (Boolean) request.getAttribute("canManageNotices");

    List<Notice> notices = (List<Notice>) request.getAttribute("notices");

    if (notices == null) {
        NoticeDAO noticeDAO = new NoticeDAO();

        if ("Admin".equalsIgnoreCase(currentUserRole) || "Lecturer".equalsIgnoreCase(currentUserRole)) {
            notices = noticeDAO.getAllNotices(application);
            canManageNotices = true;
        } else {
            notices = noticeDAO.getVisibleNoticesForRole(application, currentUserRole);
        }
    }

    int totalNotices = notices != null ? notices.size() : 0;
    int publishedCount = 0;
    int draftCount = 0;
    int archivedCount = 0;
    int urgentCount = 0;
    int highCount = 0;
    int studentTargetCount = 0;
    int allTargetCount = 0;

    if (notices != null) {
        for (Notice notice : notices) {
            if (notice.isPublished()) {
                publishedCount++;
            }

            if (notice.isDraft()) {
                draftCount++;
            }

            if (notice.isArchived()) {
                archivedCount++;
            }

            if (notice.isUrgentPriority()) {
                urgentCount++;
            }

            if (notice.isHighPriority()) {
                highCount++;
            }

            if (notice.isForStudent()) {
                studentTargetCount++;
            }

            if (notice.isForAll()) {
                allTargetCount++;
            }
        }
    }

    int publishedPercentage = totalNotices > 0 ? (publishedCount * 100) / totalNotices : 0;
    int draftPercentage = totalNotices > 0 ? (draftCount * 100) / totalNotices : 0;
    int archivedPercentage = totalNotices > 0 ? (archivedCount * 100) / totalNotices : 0;

    String success = request.getParameter("success");
    String error = request.getParameter("error");

    String alertType = "";
    String alertMessage = "";

    if (success != null) {
        alertType = "success";

        if ("noticeAdded".equalsIgnoreCase(success)) {
            alertMessage = "Notice created successfully.";
        } else if ("noticeUpdated".equalsIgnoreCase(success)) {
            alertMessage = "Notice updated successfully.";
        } else if ("noticeDeleted".equalsIgnoreCase(success)) {
            alertMessage = "Notice deleted successfully.";
        } else if ("noticeStatusUpdated".equalsIgnoreCase(success)) {
            alertMessage = "Notice status updated successfully.";
        } else {
            alertMessage = "Operation completed successfully.";
        }
    }

    if (error != null) {
        alertType = "danger";

        if ("accessDenied".equalsIgnoreCase(error)) {
            alertMessage = "You do not have permission to manage notices.";
        } else if ("missingNoticeId".equalsIgnoreCase(error)) {
            alertMessage = "Notice ID is missing.";
        } else if ("missingTitle".equalsIgnoreCase(error)) {
            alertMessage = "Notice title is required.";
        } else if ("missingDescription".equalsIgnoreCase(error)) {
            alertMessage = "Notice description is required.";
        } else if ("missingNoticeDate".equalsIgnoreCase(error)) {
            alertMessage = "Notice date is required.";
        } else if ("invalidNoticeDate".equalsIgnoreCase(error)) {
            alertMessage = "Invalid notice date. Please use a valid date.";
        } else if ("missingTargetGroup".equalsIgnoreCase(error)) {
            alertMessage = "Target group is required.";
        } else if ("invalidTargetGroup".equalsIgnoreCase(error)) {
            alertMessage = "Invalid target group selected.";
        } else if ("missingPriority".equalsIgnoreCase(error)) {
            alertMessage = "Priority is required.";
        } else if ("invalidPriority".equalsIgnoreCase(error)) {
            alertMessage = "Invalid priority selected.";
        } else if ("missingStatus".equalsIgnoreCase(error)) {
            alertMessage = "Status is required.";
        } else if ("invalidStatus".equalsIgnoreCase(error)) {
            alertMessage = "Invalid status selected.";
        } else if ("noticeAddFailed".equalsIgnoreCase(error)) {
            alertMessage = "Notice could not be created. Check duplicate Notice ID or incomplete fields.";
        } else if ("noticeUpdateFailed".equalsIgnoreCase(error)) {
            alertMessage = "Notice could not be updated.";
        } else if ("noticeDeleteFailed".equalsIgnoreCase(error)) {
            alertMessage = "Notice could not be deleted. Published notices should be archived instead.";
        } else if ("noticeStatusUpdateFailed".equalsIgnoreCase(error)) {
            alertMessage = "Notice status could not be updated.";
        } else {
            alertMessage = "Something went wrong. Please check the notice details and try again.";
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
                            <i class="bi bi-megaphone-fill me-1"></i>
                            NextExamLK Announcement Center
                        </span>

                        <% if (canManageNotices) { %>
                            <h1 class="hero-title">Notice Management</h1>
                            <p class="hero-text">
                                Create, publish, archive, and manage academic announcements for students,
                                lecturers, administrators, or all users.
                            </p>
                        <% } else { %>
                            <h1 class="hero-title">My Notices</h1>
                            <p class="hero-text">
                                View published academic announcements, examination updates, result notices,
                                and important messages shared with your role.
                            </p>
                        <% } %>
                    </div>

                    <div class="d-flex gap-2 flex-wrap">
                        <% if (canManageNotices) { %>
                            <button class="btn btn-primary"
                                    type="button"
                                    data-bs-toggle="modal"
                                    data-bs-target="#noticeModal">
                                <i class="bi bi-plus-lg me-2"></i>
                                Add Notice
                            </button>
                        <% } %>

                        <% if ("Student".equalsIgnoreCase(currentUserRole)) { %>
                            <a href="<%= request.getContextPath() %>/my-exams" class="btn btn-outline-primary">
                                <i class="bi bi-laptop-fill me-2"></i>
                                My Exams
                            </a>
                        <% } else { %>
                            <a href="<%= request.getContextPath() %>/dashboard.jsp" class="btn btn-outline-primary">
                                <i class="bi bi-grid-1x2-fill me-2"></i>
                                Dashboard
                            </a>
                        <% } %>
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
                                <div class="stat-label"><%= canManageNotices ? "Total Notices" : "Visible Notices" %></div>
                                <div class="stat-value"><%= totalNotices %></div>
                                <div class="stat-meta"><%= canManageNotices ? "All notice records" : "Published for your role" %></div>
                            </div>

                            <div class="stat-icon">
                                <i class="bi bi-megaphone-fill"></i>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-md-6 col-xl-3">
                    <div class="app-card stat-card">
                        <div class="d-flex justify-content-between gap-3">
                            <div>
                                <div class="stat-label">Published</div>
                                <div class="stat-value"><%= publishedCount %></div>
                                <div class="stat-meta"><%= publishedPercentage %>% of current list</div>
                            </div>

                            <div class="stat-icon">
                                <i class="bi bi-broadcast-pin"></i>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-md-6 col-xl-3">
                    <div class="app-card stat-card">
                        <div class="d-flex justify-content-between gap-3">
                            <div>
                                <div class="stat-label">Urgent / High</div>
                                <div class="stat-value"><%= urgentCount %>/<%= highCount %></div>
                                <div class="stat-meta">Important announcements</div>
                            </div>

                            <div class="stat-icon">
                                <i class="bi bi-exclamation-triangle-fill"></i>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-md-6 col-xl-3">
                    <div class="app-card stat-card">
                        <div class="d-flex justify-content-between gap-3">
                            <div>
                                <div class="stat-label">All / Student</div>
                                <div class="stat-value"><%= allTargetCount %>/<%= studentTargetCount %></div>
                                <div class="stat-meta">Audience targeting</div>
                            </div>

                            <div class="stat-icon">
                                <i class="bi bi-people-fill"></i>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <% if (canManageNotices) { %>
                <div class="row g-4 mb-4">
                    <div class="col-xl-4">
                        <div class="app-card form-card h-100 p-4">
                            <div class="d-flex justify-content-between align-items-start gap-3 mb-3">
                                <div>
                                    <h4 class="fw-bold mb-1">Quick Publish</h4>
                                    <p class="text-secondary mb-0">
                                        Create a notice for a selected role.
                                    </p>
                                </div>

                                <span class="badge badge-soft-primary">Publisher</span>
                            </div>

                            <form class="needs-validation"
                                  novalidate
                                  action="<%= request.getContextPath() %>/notices"
                                  method="post">

                                <input type="hidden" name="action" value="add">

                                <div class="mb-3">
                                    <label class="form-label">Notice ID <span class="required">*</span></label>
                                    <input type="text"
                                           name="noticeId"
                                           class="form-control"
                                           placeholder="Example: N004"
                                           maxlength="30"
                                           required>
                                    <div class="invalid-feedback">Notice ID is required.</div>
                                </div>

                                <div class="mb-3">
                                    <label class="form-label">Title <span class="required">*</span></label>
                                    <input type="text"
                                           name="title"
                                           class="form-control"
                                           placeholder="Enter notice title"
                                           maxlength="150"
                                           required>
                                    <div class="invalid-feedback">Title is required.</div>
                                </div>

                                <div class="mb-3">
                                    <label class="form-label">Description <span class="required">*</span></label>
                                    <textarea name="description"
                                              class="form-control"
                                              rows="4"
                                              maxlength="2500"
                                              placeholder="Write notice message..."
                                              required></textarea>
                                    <div class="invalid-feedback">Description is required.</div>
                                </div>

                                <div class="mb-3">
                                    <label class="form-label">Date <span class="required">*</span></label>
                                    <input type="date"
                                           name="noticeDate"
                                           class="form-control"
                                           required>
                                    <div class="invalid-feedback">Notice date is required.</div>
                                </div>

                                <div class="mb-3">
                                    <label class="form-label">Target Group <span class="required">*</span></label>
                                    <select name="targetGroup" class="form-select" required>
                                        <option value="">Choose group</option>
                                        <option value="All">All</option>
                                        <option value="Student">Student</option>
                                        <option value="Lecturer">Lecturer</option>
                                        <option value="Admin">Admin</option>
                                    </select>
                                    <div class="invalid-feedback">Target group is required.</div>
                                </div>

                                <div class="mb-3">
                                    <label class="form-label">Priority <span class="required">*</span></label>
                                    <select name="priority" class="form-select" required>
                                        <option value="">Choose priority</option>
                                        <option value="Low">Low</option>
                                        <option value="Normal">Normal</option>
                                        <option value="High">High</option>
                                        <option value="Urgent">Urgent</option>
                                    </select>
                                    <div class="invalid-feedback">Priority is required.</div>
                                </div>

                                <div class="mb-3">
                                    <label class="form-label">Status <span class="required">*</span></label>
                                    <select name="status" class="form-select" required>
                                        <option value="">Choose status</option>
                                        <option value="Draft">Draft</option>
                                        <option value="Published">Published</option>
                                        <option value="Archived">Archived</option>
                                    </select>
                                    <div class="invalid-feedback">Status is required.</div>
                                </div>

                                <button class="btn btn-primary w-100" type="submit">
                                    <i class="bi bi-send-fill me-2"></i>
                                    Save Notice
                                </button>
                            </form>
                        </div>
                    </div>

                    <div class="col-xl-8">
                        <div class="app-card p-4 h-100 notice-board-card">
                            <div class="d-flex justify-content-between align-items-start flex-wrap gap-3 mb-4">
                                <div>
                                    <span class="badge badge-soft-primary mb-2">
                                        <i class="bi bi-broadcast-pin me-1"></i>
                                        Live Announcement Board
                                    </span>

                                    <h4 class="fw-bold mb-1">Priority Announcement Board</h4>

                                    <p class="text-secondary mb-0">
                                        Highlight the latest notices, urgent updates, and role-targeted announcements.
                                    </p>
                                </div>

                                <div class="notice-board-summary">
                                    <div>
                                        <small>Total</small>
                                        <strong><%= totalNotices %></strong>
                                    </div>

                                    <div>
                                        <small>Urgent</small>
                                        <strong><%= urgentCount %></strong>
                                    </div>

                                    <div>
                                        <small>Published</small>
                                        <strong><%= publishedCount %></strong>
                                    </div>
                                </div>
                            </div>

                            <%
                                Notice featuredNotice = null;

                                if (notices != null && !notices.isEmpty()) {
                                    for (Notice notice : notices) {
                                        if (notice.isUrgentPriority()) {
                                            featuredNotice = notice;
                                            break;
                                        }
                                    }

                                    if (featuredNotice == null) {
                                        featuredNotice = notices.get(0);
                                    }
                                }
                            %>

                            <% if (featuredNotice != null) { %>
                                <div class="featured-notice-card mb-4">
                                    <div class="featured-notice-icon">
                                        <% if (featuredNotice.isUrgentPriority()) { %>
                                            <i class="bi bi-exclamation-triangle-fill"></i>
                                        <% } else if (featuredNotice.isHighPriority()) { %>
                                            <i class="bi bi-lightning-charge-fill"></i>
                                        <% } else { %>
                                            <i class="bi bi-megaphone-fill"></i>
                                        <% } %>
                                    </div>

                                    <div class="flex-grow-1">
                                        <div class="d-flex justify-content-between align-items-start gap-3 flex-wrap mb-2">
                                            <div class="d-flex gap-2 flex-wrap">
                                                <span class="badge <%= featuredNotice.getPriorityBadgeClass() %>">
                                                    <i class="bi bi-flag-fill me-1"></i>
                                                    <%= FileUtil.h(featuredNotice.getPriority()) %>
                                                </span>

                                                <span class="badge <%= featuredNotice.getTargetBadgeClass() %>">
                                                    <i class="bi bi-people-fill me-1"></i>
                                                    <%= FileUtil.h(featuredNotice.getTargetGroup()) %>
                                                </span>

                                                <span class="badge <%= featuredNotice.getStatusBadgeClass() %>">
                                                    <%= FileUtil.h(featuredNotice.getStatus()) %>
                                                </span>
                                            </div>

                                            <small class="featured-notice-date">
                                                <i class="bi bi-calendar-event me-1"></i>
                                                <%= FileUtil.h(featuredNotice.getDisplayNoticeDate()) %>
                                            </small>
                                        </div>

                                        <h3 class="featured-notice-title">
                                            <%= FileUtil.h(featuredNotice.getTitle()) %>
                                        </h3>

                                        <p class="featured-notice-text">
                                            <%= FileUtil.h(featuredNotice.getShortDescription()) %>
                                        </p>

                                        <button class="btn btn-light btn-sm"
                                                type="button"
                                                data-bs-toggle="modal"
                                                data-bs-target="#viewNoticeModal"
                                                data-notice-id="<%= FileUtil.h(featuredNotice.getNoticeId()) %>"
                                                data-title="<%= FileUtil.h(featuredNotice.getTitle()) %>"
                                                data-description="<%= FileUtil.h(featuredNotice.getDescription()) %>"
                                                data-date="<%= FileUtil.h(featuredNotice.getNoticeDate()) %>"
                                                data-display-date="<%= FileUtil.h(featuredNotice.getDisplayNoticeDate()) %>"
                                                data-target-group="<%= FileUtil.h(featuredNotice.getTargetGroup()) %>"
                                                data-priority="<%= FileUtil.h(featuredNotice.getPriority()) %>"
                                                data-status="<%= FileUtil.h(featuredNotice.getStatus()) %>">
                                            <i class="bi bi-eye me-1"></i>
                                            View Featured Notice
                                        </button>
                                    </div>
                                </div>
                            <% } %>

                            <div class="row g-3">
                                <%
                                    if (notices != null && !notices.isEmpty()) {
                                        int shown = 0;

                                        for (Notice notice : notices) {
                                            if (shown >= 4) {
                                                break;
                                            }

                                            shown++;

                                            String noticeIcon = "bi-megaphone-fill";

                                            if (notice.isUrgentPriority()) {
                                                noticeIcon = "bi-exclamation-triangle-fill";
                                            } else if (notice.isHighPriority()) {
                                                noticeIcon = "bi-lightning-charge-fill";
                                            } else if (notice.isNormalPriority()) {
                                                noticeIcon = "bi-info-circle-fill";
                                            }
                                %>
                                    <div class="col-md-6">
                                        <div class="mini-notice-card h-100">
                                            <div class="mini-notice-top">
                                                <div class="mini-notice-icon">
                                                    <i class="bi <%= noticeIcon %>"></i>
                                                </div>

                                                <div class="flex-grow-1">
                                                    <div class="d-flex justify-content-between align-items-start gap-2">
                                                        <span class="badge <%= notice.getPriorityBadgeClass() %>">
                                                            <%= FileUtil.h(notice.getPriority()) %>
                                                        </span>

                                                        <small class="text-secondary">
                                                            <%= FileUtil.h(notice.getDisplayNoticeDate()) %>
                                                        </small>
                                                    </div>

                                                    <h5 class="mini-notice-title">
                                                        <%= FileUtil.h(notice.getTitle()) %>
                                                    </h5>
                                                </div>
                                            </div>

                                            <p class="mini-notice-text">
                                                <%= FileUtil.h(notice.getShortDescription()) %>
                                            </p>

                                            <div class="d-flex justify-content-between align-items-center gap-2 flex-wrap">
                                                <div class="d-flex gap-2 flex-wrap">
                                                    <span class="badge <%= notice.getTargetBadgeClass() %>">
                                                        <%= FileUtil.h(notice.getTargetGroup()) %>
                                                    </span>

                                                    <span class="badge <%= notice.getStatusBadgeClass() %>">
                                                        <%= FileUtil.h(notice.getStatus()) %>
                                                    </span>
                                                </div>

                                                <button class="btn btn-sm btn-outline-primary"
                                                        type="button"
                                                        data-bs-toggle="modal"
                                                        data-bs-target="#viewNoticeModal"
                                                        data-notice-id="<%= FileUtil.h(notice.getNoticeId()) %>"
                                                        data-title="<%= FileUtil.h(notice.getTitle()) %>"
                                                        data-description="<%= FileUtil.h(notice.getDescription()) %>"
                                                        data-date="<%= FileUtil.h(notice.getNoticeDate()) %>"
                                                        data-display-date="<%= FileUtil.h(notice.getDisplayNoticeDate()) %>"
                                                        data-target-group="<%= FileUtil.h(notice.getTargetGroup()) %>"
                                                        data-priority="<%= FileUtil.h(notice.getPriority()) %>"
                                                        data-status="<%= FileUtil.h(notice.getStatus()) %>">
                                                    <i class="bi bi-eye"></i>
                                                </button>
                                            </div>
                                        </div>
                                    </div>
                                <%
                                        }
                                    } else {
                                %>
                                    <div class="col-12">
                                        <div class="empty-state">
                                            <div class="empty-state-icon">
                                                <i class="bi bi-inbox"></i>
                                            </div>
                                            <h5>No priority notices available</h5>
                                            <p>Publish a notice to show it here.</p>
                                        </div>
                                    </div>
                                <%
                                    }
                                %>
                            </div>

                            <div class="notice-board-footer mt-4">
                                <div>
                                    <strong>Notice workflow:</strong>
                                    Draft notices are hidden from students. Published notices become visible to the selected target group.
                                </div>

                                <span>
                                    <i class="bi bi-shield-check me-1"></i>
                                    Role-based visibility enabled
                                </span>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="row g-4 mb-4">
                    <div class="col-xl-6">
                        <div class="app-card p-4 h-100">
                            <div class="d-flex justify-content-between align-items-start mb-3">
                                <div>
                                    <h4 class="fw-bold mb-1">Publishing Status</h4>
                                    <p class="text-secondary mb-0">
                                        Current status of saved notice records.
                                    </p>
                                </div>

                                <span class="badge badge-soft-secondary">Status</span>
                            </div>

                            <div class="readiness-board border-0 shadow-none p-0">
                                <div class="readiness-item mb-3">
                                    <div class="d-flex justify-content-between mb-1">
                                        <span class="fw-semibold">Published Notices</span>
                                        <span class="fw-bold"><%= publishedPercentage %>%</span>
                                    </div>
                                    <div class="progress" style="height: 9px;">
                                        <div class="progress-bar bg-success" style="width: <%= publishedPercentage %>%;"></div>
                                    </div>
                                    <small class="text-secondary"><%= publishedCount %> notices are currently published.</small>
                                </div>

                                <div class="readiness-item mb-3">
                                    <div class="d-flex justify-content-between mb-1">
                                        <span class="fw-semibold">Draft Notices</span>
                                        <span class="fw-bold"><%= draftPercentage %>%</span>
                                    </div>
                                    <div class="progress" style="height: 9px;">
                                        <div class="progress-bar bg-warning" style="width: <%= draftPercentage %>%;"></div>
                                    </div>
                                    <small class="text-secondary"><%= draftCount %> notices are still in draft state.</small>
                                </div>

                                <div class="readiness-item">
                                    <div class="d-flex justify-content-between mb-1">
                                        <span class="fw-semibold">Archived Notices</span>
                                        <span class="fw-bold"><%= archivedPercentage %>%</span>
                                    </div>
                                    <div class="progress" style="height: 9px;">
                                        <div class="progress-bar bg-secondary" style="width: <%= archivedPercentage %>%;"></div>
                                    </div>
                                    <small class="text-secondary"><%= archivedCount %> notices are archived as history.</small>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="col-xl-6">
                        <div class="app-card p-4 h-100">
                            <div class="d-flex justify-content-between align-items-start mb-3">
                                <div>
                                    <h4 class="fw-bold mb-1">Notice Workflow</h4>
                                    <p class="text-secondary mb-0">
                                        Recommended announcement process.
                                    </p>
                                </div>

                                <span class="badge badge-soft-primary">Process</span>
                            </div>

                            <div class="timeline">
                                <div class="timeline-item">
                                    <div class="activity-title">Create Notice</div>
                                    <small class="text-secondary">Write title, message, date, target group, and priority.</small>
                                </div>

                                <div class="timeline-item">
                                    <div class="activity-title">Draft Review</div>
                                    <small class="text-secondary">Keep as draft until the message is verified.</small>
                                </div>

                                <div class="timeline-item">
                                    <div class="activity-title">Publish</div>
                                    <small class="text-secondary">Published notices become visible to the selected role.</small>
                                </div>

                                <div class="timeline-item">
                                    <div class="activity-title">Archive</div>
                                    <small class="text-secondary">Archive old notices instead of deleting published records.</small>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            <% } %>

            <div class="page-header">
                <div>
                    <h2 class="page-title"><%= canManageNotices ? "Notice Records" : "Published Notices" %></h2>
                    <p class="page-description">
                        <%= canManageNotices
                                ? "Search, filter, view, update, archive, and manage academic announcements."
                                : "Search and view notices published for your role." %>
                    </p>
                </div>

                <% if (canManageNotices) { %>
                    <button class="btn btn-primary"
                            type="button"
                            data-bs-toggle="modal"
                            data-bs-target="#noticeModal">
                        <i class="bi bi-plus-lg me-2"></i>
                        Add Notice
                    </button>
                <% } %>
            </div>

            <div class="app-card crud-card p-4">
                <div class="crud-toolbar">
                    <div class="input-group search-control">
                        <span class="input-group-text">
                            <i class="bi bi-search"></i>
                        </span>

                        <input type="search"
                               class="form-control"
                               id="noticeSearch"
                               placeholder="Search by notice ID, title, target group, date, priority, or status">
                    </div>

                    <div class="d-flex gap-2 flex-wrap">
                        <select class="form-select" id="noticeTargetFilter" style="width: 160px;">
                            <option value="">All Groups</option>
                            <option value="all">All</option>
                            <option value="student">Student</option>
                            <option value="lecturer">Lecturer</option>
                            <option value="admin">Admin</option>
                        </select>

                        <select class="form-select" id="noticePriorityFilter" style="width: 160px;">
                            <option value="">All Priority</option>
                            <option value="low">Low</option>
                            <option value="normal">Normal</option>
                            <option value="high">High</option>
                            <option value="urgent">Urgent</option>
                        </select>

                        <% if (canManageNotices) { %>
                            <select class="form-select" id="noticeStatusFilter" style="width: 160px;">
                                <option value="">All Status</option>
                                <option value="draft">Draft</option>
                                <option value="published">Published</option>
                                <option value="archived">Archived</option>
                            </select>
                        <% } %>

                        <button class="btn btn-outline-secondary" type="button" id="clearNoticeFiltersBtn">
                            <i class="bi bi-x-circle me-1"></i>
                            Clear
                        </button>
                    </div>
                </div>

                <% if (canManageNotices) { %>
                    <div class="table-responsive">
                        <table class="table table-hover align-middle" id="noticeTable">
                            <thead>
                            <tr>
                                <th>Notice ID</th>
                                <th>Title</th>
                                <th>Target</th>
                                <th>Date</th>
                                <th>Priority</th>
                                <th>Status</th>
                                <th>Visibility</th>
                                <th class="text-end">Actions</th>
                            </tr>
                            </thead>

                            <tbody>
                            <% if (notices != null && !notices.isEmpty()) {
                                for (Notice notice : notices) {
                            %>
                                <tr data-target="<%= FileUtil.h(notice.getTargetGroup().toLowerCase()) %>"
                                    data-priority="<%= FileUtil.h(notice.getPriority().toLowerCase()) %>"
                                    data-status="<%= FileUtil.h(notice.getStatus().toLowerCase()) %>">
                                    <td class="fw-bold"><%= FileUtil.h(notice.getNoticeId()) %></td>

                                    <td>
                                        <div class="fw-bold"><%= FileUtil.h(notice.getTitle()) %></div>
                                        <small class="text-secondary"><%= FileUtil.h(notice.getShortDescription()) %></small>
                                    </td>

                                    <td>
                                        <span class="badge <%= notice.getTargetBadgeClass() %>">
                                            <%= FileUtil.h(notice.getTargetGroup()) %>
                                        </span>
                                    </td>

                                    <td><%= FileUtil.h(notice.getDisplayNoticeDate()) %></td>

                                    <td>
                                        <span class="badge <%= notice.getPriorityBadgeClass() %>">
                                            <%= FileUtil.h(notice.getPriority()) %>
                                        </span>
                                    </td>

                                    <td>
                                        <form action="<%= request.getContextPath() %>/notices"
                                              method="post"
                                              class="d-flex gap-2 align-items-center">
                                            <input type="hidden" name="action" value="status">
                                            <input type="hidden" name="noticeId" value="<%= FileUtil.h(notice.getNoticeId()) %>">

                                            <select name="status" class="form-select form-select-sm" style="min-width: 125px;">
                                                <option value="Draft" <%= notice.isDraft() ? "selected" : "" %>>Draft</option>
                                                <option value="Published" <%= notice.isPublished() ? "selected" : "" %>>Published</option>
                                                <option value="Archived" <%= notice.isArchived() ? "selected" : "" %>>Archived</option>
                                            </select>

                                            <button class="btn btn-sm btn-outline-primary" type="submit" title="Update Status">
                                                <i class="bi bi-check2"></i>
                                            </button>
                                        </form>
                                    </td>

                                    <td>
                                        <span class="badge <%= notice.getVisibilityBadgeClass() %>">
                                            <%= FileUtil.h(notice.getVisibilityLabel()) %>
                                        </span>
                                    </td>

                                    <td>
                                        <div class="action-group">
                                            <button class="btn btn-sm btn-outline-primary view-notice-btn"
                                                    type="button"
                                                    title="View Notice"
                                                    data-bs-toggle="modal"
                                                    data-bs-target="#viewNoticeModal"
                                                    data-notice-id="<%= FileUtil.h(notice.getNoticeId()) %>"
                                                    data-title="<%= FileUtil.h(notice.getTitle()) %>"
                                                    data-description="<%= FileUtil.h(notice.getDescription()) %>"
                                                    data-date="<%= FileUtil.h(notice.getNoticeDate()) %>"
                                                    data-display-date="<%= FileUtil.h(notice.getDisplayNoticeDate()) %>"
                                                    data-target-group="<%= FileUtil.h(notice.getTargetGroup()) %>"
                                                    data-priority="<%= FileUtil.h(notice.getPriority()) %>"
                                                    data-status="<%= FileUtil.h(notice.getStatus()) %>">
                                                <i class="bi bi-eye"></i>
                                            </button>

                                            <% if (notice.canEdit()) { %>
                                                <button class="btn btn-sm btn-outline-primary edit-notice-btn"
                                                        type="button"
                                                        title="Edit Notice"
                                                        data-bs-toggle="modal"
                                                        data-bs-target="#editNoticeModal"
                                                        data-notice-id="<%= FileUtil.h(notice.getNoticeId()) %>"
                                                        data-title="<%= FileUtil.h(notice.getTitle()) %>"
                                                        data-description="<%= FileUtil.h(notice.getDescription()) %>"
                                                        data-date="<%= FileUtil.h(notice.getNoticeDate()) %>"
                                                        data-target-group="<%= FileUtil.h(notice.getTargetGroup()) %>"
                                                        data-priority="<%= FileUtil.h(notice.getPriority()) %>"
                                                        data-status="<%= FileUtil.h(notice.getStatus()) %>">
                                                    <i class="bi bi-pencil-square"></i>
                                                </button>
                                            <% } else { %>
                                                <button class="btn btn-sm btn-outline-secondary"
                                                        type="button"
                                                        disabled
                                                        title="Archived notices cannot be edited">
                                                    <i class="bi bi-lock-fill"></i>
                                                </button>
                                            <% } %>

                                            <% if (notice.canDelete()) { %>
                                                <button class="btn btn-sm btn-outline-danger"
                                                        type="button"
                                                        title="Delete Notice"
                                                        data-bs-toggle="modal"
                                                        data-bs-target="#deleteModal"
                                                        data-delete-name="<%= FileUtil.h(notice.getNoticeId() + " - " + notice.getTitle()) %>"
                                                        data-delete-id="<%= FileUtil.h(notice.getNoticeId()) %>"
                                                        data-delete-url="<%= request.getContextPath() %>/notices">
                                                    <i class="bi bi-trash3"></i>
                                                </button>
                                            <% } else { %>
                                                <button class="btn btn-sm btn-outline-secondary"
                                                        type="button"
                                                        disabled
                                                        title="Published notices should be archived instead of deleted">
                                                    <i class="bi bi-trash3"></i>
                                                </button>
                                            <% } %>
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
                                            <h5>No notice records found</h5>
                                            <p>Add a notice to display records here.</p>
                                        </div>
                                    </td>
                                </tr>
                            <% } %>
                            </tbody>
                        </table>
                    </div>
                <% } else { %>
                    <div class="row g-3" id="noticeCardGrid">
                        <% if (notices != null && !notices.isEmpty()) {
                            for (Notice notice : notices) {
                        %>
                            <div class="col-md-6 col-xl-4 notice-card-item"
                                 data-target="<%= FileUtil.h(notice.getTargetGroup().toLowerCase()) %>"
                                 data-priority="<%= FileUtil.h(notice.getPriority().toLowerCase()) %>"
                                 data-status="<%= FileUtil.h(notice.getStatus().toLowerCase()) %>">
                                <div class="app-card p-4 h-100">
                                    <div class="d-flex justify-content-between align-items-start gap-2 mb-3">
                                        <span class="badge <%= notice.getPriorityBadgeClass() %>">
                                            <%= FileUtil.h(notice.getPriority()) %>
                                        </span>

                                        <small class="text-secondary">
                                            <%= FileUtil.h(notice.getDisplayNoticeDate()) %>
                                        </small>
                                    </div>

                                    <h5 class="fw-bold mb-2">
                                        <%= FileUtil.h(notice.getTitle()) %>
                                    </h5>

                                    <p class="text-secondary mb-3">
                                        <%= FileUtil.h(notice.getDescription()) %>
                                    </p>

                                    <div class="d-flex gap-2 flex-wrap">
                                        <span class="badge <%= notice.getTargetBadgeClass() %>">
                                            <%= FileUtil.h(notice.getTargetGroup()) %>
                                        </span>

                                        <span class="badge <%= notice.getStatusBadgeClass() %>">
                                            <%= FileUtil.h(notice.getStatus()) %>
                                        </span>
                                    </div>
                                </div>
                            </div>
                        <% }
                        } else { %>
                            <div class="col-12">
                                <div class="empty-state">
                                    <div class="empty-state-icon">
                                        <i class="bi bi-inbox"></i>
                                    </div>
                                    <h5>No notices available</h5>
                                    <p>There are no published notices for your role right now.</p>
                                </div>
                            </div>
                        <% } %>
                    </div>
                <% } %>
            </div>

        </section>
    </main>
</div>

<% if (canManageNotices) { %>

<!-- Add Notice Modal -->
<div class="modal fade" id="noticeModal" tabindex="-1" aria-labelledby="noticeModalTitle" aria-hidden="true">
    <div class="modal-dialog modal-lg modal-dialog-centered">
        <div class="modal-content border-0 shadow-lg">

            <form class="needs-validation"
                  novalidate
                  action="<%= request.getContextPath() %>/notices"
                  method="post">

                <input type="hidden" name="action" value="add">

                <div class="modal-header">
                    <div>
                        <h5 class="modal-title fw-bold" id="noticeModalTitle">Add Notice</h5>
                        <small class="text-secondary">
                            Create an academic announcement for a selected audience.
                        </small>
                    </div>

                    <button class="btn-close" type="button" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>

                <div class="modal-body">
                    <div class="row g-3">
                        <div class="col-md-6">
                            <label class="form-label">Notice ID <span class="required">*</span></label>
                            <input type="text"
                                   name="noticeId"
                                   class="form-control"
                                   placeholder="Example: N004"
                                   maxlength="30"
                                   required>
                            <div class="invalid-feedback">Notice ID is required.</div>
                        </div>

                        <div class="col-md-6">
                            <label class="form-label">Date <span class="required">*</span></label>
                            <input type="date"
                                   name="noticeDate"
                                   class="form-control"
                                   required>
                            <div class="invalid-feedback">Notice date is required.</div>
                        </div>

                        <div class="col-12">
                            <label class="form-label">Title <span class="required">*</span></label>
                            <input type="text"
                                   name="title"
                                   class="form-control"
                                   placeholder="Enter notice title"
                                   maxlength="150"
                                   required>
                            <div class="invalid-feedback">Title is required.</div>
                        </div>

                        <div class="col-12">
                            <label class="form-label">Description <span class="required">*</span></label>
                            <textarea name="description"
                                      class="form-control"
                                      rows="4"
                                      maxlength="2500"
                                      placeholder="Write notice message..."
                                      required></textarea>
                            <div class="invalid-feedback">Description is required.</div>
                        </div>

                        <div class="col-md-4">
                            <label class="form-label">Target Group <span class="required">*</span></label>
                            <select name="targetGroup" class="form-select" required>
                                <option value="">Choose group</option>
                                <option value="All">All</option>
                                <option value="Student">Student</option>
                                <option value="Lecturer">Lecturer</option>
                                <option value="Admin">Admin</option>
                            </select>
                            <div class="invalid-feedback">Target group is required.</div>
                        </div>

                        <div class="col-md-4">
                            <label class="form-label">Priority <span class="required">*</span></label>
                            <select name="priority" class="form-select" required>
                                <option value="">Choose priority</option>
                                <option value="Low">Low</option>
                                <option value="Normal">Normal</option>
                                <option value="High">High</option>
                                <option value="Urgent">Urgent</option>
                            </select>
                            <div class="invalid-feedback">Priority is required.</div>
                        </div>

                        <div class="col-md-4">
                            <label class="form-label">Status <span class="required">*</span></label>
                            <select name="status" class="form-select" required>
                                <option value="">Choose status</option>
                                <option value="Draft">Draft</option>
                                <option value="Published">Published</option>
                                <option value="Archived">Archived</option>
                            </select>
                            <div class="invalid-feedback">Status is required.</div>
                        </div>
                    </div>

                    <div class="alert alert-info mt-4 mb-0">
                        <strong>Visibility rule:</strong>
                        Only published notices are visible to students. Draft and archived notices are hidden from student view.
                    </div>
                </div>

                <div class="modal-footer">
                    <button class="btn btn-outline-secondary" type="button" data-bs-dismiss="modal">
                        Cancel
                    </button>

                    <button class="btn btn-primary" type="submit">
                        <i class="bi bi-save me-2"></i>
                        Save Notice
                    </button>
                </div>
            </form>

        </div>
    </div>
</div>

<!-- Edit Notice Modal -->
<div class="modal fade" id="editNoticeModal" tabindex="-1" aria-labelledby="editNoticeModalTitle" aria-hidden="true">
    <div class="modal-dialog modal-lg modal-dialog-centered">
        <div class="modal-content border-0 shadow-lg">

            <form class="needs-validation"
                  novalidate
                  action="<%= request.getContextPath() %>/notices"
                  method="post">

                <input type="hidden" name="action" value="update">

                <div class="modal-header">
                    <div>
                        <h5 class="modal-title fw-bold" id="editNoticeModalTitle">Edit Notice</h5>
                        <small class="text-secondary">
                            Update an existing academic announcement.
                        </small>
                    </div>

                    <button class="btn-close" type="button" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>

                <div class="modal-body">
                    <div class="row g-3">
                        <div class="col-md-6">
                            <label class="form-label">Notice ID</label>
                            <input type="text"
                                   id="editNoticeId"
                                   name="noticeId"
                                   class="form-control"
                                   readonly
                                   required>
                            <div class="invalid-feedback">Notice ID is required.</div>
                        </div>

                        <div class="col-md-6">
                            <label class="form-label">Date</label>
                            <input type="date"
                                   id="editNoticeDate"
                                   name="noticeDate"
                                   class="form-control"
                                   required>
                            <div class="invalid-feedback">Notice date is required.</div>
                        </div>

                        <div class="col-12">
                            <label class="form-label">Title</label>
                            <input type="text"
                                   id="editNoticeTitle"
                                   name="title"
                                   class="form-control"
                                   maxlength="150"
                                   required>
                            <div class="invalid-feedback">Title is required.</div>
                        </div>

                        <div class="col-12">
                            <label class="form-label">Description</label>
                            <textarea id="editNoticeDescription"
                                      name="description"
                                      class="form-control"
                                      rows="4"
                                      maxlength="2500"
                                      required></textarea>
                            <div class="invalid-feedback">Description is required.</div>
                        </div>

                        <div class="col-md-4">
                            <label class="form-label">Target Group</label>
                            <select id="editNoticeTargetGroup" name="targetGroup" class="form-select" required>
                                <option value="">Choose group</option>
                                <option value="All">All</option>
                                <option value="Student">Student</option>
                                <option value="Lecturer">Lecturer</option>
                                <option value="Admin">Admin</option>
                            </select>
                            <div class="invalid-feedback">Target group is required.</div>
                        </div>

                        <div class="col-md-4">
                            <label class="form-label">Priority</label>
                            <select id="editNoticePriority" name="priority" class="form-select" required>
                                <option value="">Choose priority</option>
                                <option value="Low">Low</option>
                                <option value="Normal">Normal</option>
                                <option value="High">High</option>
                                <option value="Urgent">Urgent</option>
                            </select>
                            <div class="invalid-feedback">Priority is required.</div>
                        </div>

                        <div class="col-md-4">
                            <label class="form-label">Status</label>
                            <select id="editNoticeStatus" name="status" class="form-select" required>
                                <option value="">Choose status</option>
                                <option value="Draft">Draft</option>
                                <option value="Published">Published</option>
                                <option value="Archived">Archived</option>
                            </select>
                            <div class="invalid-feedback">Status is required.</div>
                        </div>
                    </div>

                    <div class="alert alert-info mt-4 mb-0">
                        <strong>Update rule:</strong>
                        Archived notices cannot be edited from the table. Published notices should be archived instead of deleted.
                    </div>
                </div>

                <div class="modal-footer">
                    <button class="btn btn-outline-secondary" type="button" data-bs-dismiss="modal">
                        Cancel
                    </button>

                    <button class="btn btn-primary" type="submit">
                        <i class="bi bi-save me-2"></i>
                        Update Notice
                    </button>
                </div>
            </form>

        </div>
    </div>
</div>

<!-- View Notice Modal -->
<div class="modal fade" id="viewNoticeModal" tabindex="-1" aria-labelledby="viewNoticeModalTitle" aria-hidden="true">
    <div class="modal-dialog modal-lg modal-dialog-centered">
        <div class="modal-content border-0 shadow-lg">

            <div class="modal-header">
                <div>
                    <h5 class="modal-title fw-bold" id="viewNoticeModalTitle">Notice Details</h5>
                    <small class="text-secondary">
                        View selected notice information.
                    </small>
                </div>

                <button class="btn-close" type="button" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>

            <div class="modal-body">
                <div class="row g-3">
                    <div class="col-md-6">
                        <div class="exam-info-box">
                            <small>Notice ID</small>
                            <strong id="viewNoticeId">-</strong>
                        </div>
                    </div>

                    <div class="col-md-6">
                        <div class="exam-info-box">
                            <small>Date</small>
                            <strong id="viewNoticeDate">-</strong>
                        </div>
                    </div>

                    <div class="col-12">
                        <div class="exam-info-box">
                            <small>Title</small>
                            <strong id="viewNoticeTitle">-</strong>
                        </div>
                    </div>

                    <div class="col-12">
                        <div class="exam-info-box">
                            <small>Description</small>
                            <strong id="viewNoticeDescription">-</strong>
                        </div>
                    </div>

                    <div class="col-md-4">
                        <div class="exam-info-box">
                            <small>Target Group</small>
                            <strong id="viewNoticeTargetGroup">-</strong>
                        </div>
                    </div>

                    <div class="col-md-4">
                        <div class="exam-info-box">
                            <small>Priority</small>
                            <strong id="viewNoticePriority">-</strong>
                        </div>
                    </div>

                    <div class="col-md-4">
                        <div class="exam-info-box">
                            <small>Status</small>
                            <strong id="viewNoticeStatus">-</strong>
                        </div>
                    </div>
                </div>

                <div class="alert alert-info mt-4 mb-0">
                    <strong>Notice record:</strong>
                    These details are loaded from the selected notice row and displayed for quick review.
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

<% } %>

<script>
    document.addEventListener("DOMContentLoaded", function () {
        const noticeSearch = document.getElementById("noticeSearch");
        const noticeTargetFilter = document.getElementById("noticeTargetFilter");
        const noticePriorityFilter = document.getElementById("noticePriorityFilter");
        const noticeStatusFilter = document.getElementById("noticeStatusFilter");
        const clearNoticeFiltersBtn = document.getElementById("clearNoticeFiltersBtn");

        const noticeRows = document.querySelectorAll("#noticeTable tbody tr[data-target]");
        const noticeCards = document.querySelectorAll(".notice-card-item");

        const editNoticeModal = document.getElementById("editNoticeModal");
        const viewNoticeModal = document.getElementById("viewNoticeModal");

        function getNoticeData(button) {
            return {
                noticeId: button.getAttribute("data-notice-id") || "",
                title: button.getAttribute("data-title") || "",
                description: button.getAttribute("data-description") || "",
                date: button.getAttribute("data-date") || "",
                displayDate: button.getAttribute("data-display-date") || "",
                targetGroup: button.getAttribute("data-target-group") || "",
                priority: button.getAttribute("data-priority") || "",
                status: button.getAttribute("data-status") || ""
            };
        }

        function matchesFilters(element, searchValue, targetValue, priorityValue, statusValue) {
            const text = element.innerText.toLowerCase();
            const target = element.getAttribute("data-target") || "";
            const priority = element.getAttribute("data-priority") || "";
            const status = element.getAttribute("data-status") || "";

            const matchesSearch = text.includes(searchValue);
            const matchesTarget = targetValue === "" || target === targetValue;
            const matchesPriority = priorityValue === "" || priority === priorityValue;
            const matchesStatus = statusValue === "" || status === statusValue;

            return matchesSearch && matchesTarget && matchesPriority && matchesStatus;
        }

        function filterNotices() {
            const searchValue = noticeSearch ? noticeSearch.value.toLowerCase().trim() : "";
            const targetValue = noticeTargetFilter ? noticeTargetFilter.value.toLowerCase().trim() : "";
            const priorityValue = noticePriorityFilter ? noticePriorityFilter.value.toLowerCase().trim() : "";
            const statusValue = noticeStatusFilter ? noticeStatusFilter.value.toLowerCase().trim() : "";

            noticeRows.forEach(function (row) {
                row.style.display = matchesFilters(row, searchValue, targetValue, priorityValue, statusValue) ? "" : "none";
            });

            noticeCards.forEach(function (card) {
                card.style.display = matchesFilters(card, searchValue, targetValue, priorityValue, statusValue) ? "" : "none";
            });
        }

        if (noticeSearch) {
            noticeSearch.addEventListener("input", filterNotices);
        }

        if (noticeTargetFilter) {
            noticeTargetFilter.addEventListener("change", filterNotices);
        }

        if (noticePriorityFilter) {
            noticePriorityFilter.addEventListener("change", filterNotices);
        }

        if (noticeStatusFilter) {
            noticeStatusFilter.addEventListener("change", filterNotices);
        }

        if (clearNoticeFiltersBtn) {
            clearNoticeFiltersBtn.addEventListener("click", function () {
                if (noticeSearch) {
                    noticeSearch.value = "";
                }

                if (noticeTargetFilter) {
                    noticeTargetFilter.value = "";
                }

                if (noticePriorityFilter) {
                    noticePriorityFilter.value = "";
                }

                if (noticeStatusFilter) {
                    noticeStatusFilter.value = "";
                }

                filterNotices();
            });
        }

        if (editNoticeModal) {
            editNoticeModal.addEventListener("show.bs.modal", function (event) {
                const button = event.relatedTarget;

                if (!button) {
                    return;
                }

                const notice = getNoticeData(button);

                document.getElementById("editNoticeId").value = notice.noticeId;
                document.getElementById("editNoticeTitle").value = notice.title;
                document.getElementById("editNoticeDescription").value = notice.description;
                document.getElementById("editNoticeDate").value = notice.date;
                document.getElementById("editNoticeTargetGroup").value = notice.targetGroup;
                document.getElementById("editNoticePriority").value = notice.priority;
                document.getElementById("editNoticeStatus").value = notice.status;
            });
        }

        if (viewNoticeModal) {
            viewNoticeModal.addEventListener("show.bs.modal", function (event) {
                const button = event.relatedTarget;

                if (!button) {
                    return;
                }

                const notice = getNoticeData(button);

                document.getElementById("viewNoticeId").textContent = notice.noticeId || "-";
                document.getElementById("viewNoticeTitle").textContent = notice.title || "-";
                document.getElementById("viewNoticeDescription").textContent = notice.description || "-";
                document.getElementById("viewNoticeDate").textContent = notice.displayDate || notice.date || "-";
                document.getElementById("viewNoticeTargetGroup").textContent = notice.targetGroup || "-";
                document.getElementById("viewNoticePriority").textContent = notice.priority || "-";
                document.getElementById("viewNoticeStatus").textContent = notice.status || "-";
            });
        }
    });
</script>

<% if (canManageNotices) { %>
    <%@ include file="../includes/delete-modal.jsp" %>
<% } %>

<%@ include file="../includes/footer.jsp" %>