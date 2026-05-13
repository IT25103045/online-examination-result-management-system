<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="lk.nextexam.dao.FileUtil" %>
<%@ page import="lk.nextexam.model.User" %>
<%@ page import="lk.nextexam.dao.UserDAO" %>

<%
    String pageTitle = "Users";
    String activeMenu = "users";
    String topbarTitle = "User Management";

    String sessionRole = session != null && session.getAttribute("userRole") != null
            ? session.getAttribute("userRole").toString()
            : "";

    if (!"Admin".equalsIgnoreCase(sessionRole)) {
        response.sendRedirect(request.getContextPath() + "/dashboard.jsp?error=accessDenied");
        return;
    }

    List<User> users = (List<User>) request.getAttribute("users");

    if (users == null) {
        UserDAO userDAO = new UserDAO();
        users = userDAO.getAllUsers(application);
    }

    int totalUsers = users != null ? users.size() : 0;
    int adminCount = 0;
    int lecturerCount = 0;
    int studentCount = 0;
    int activeCount = 0;
    int inactiveCount = 0;

    if (users != null) {
        for (User user : users) {
            if ("Admin".equalsIgnoreCase(user.getRole())) {
                adminCount++;
            } else if ("Lecturer".equalsIgnoreCase(user.getRole())) {
                lecturerCount++;
            } else if ("Student".equalsIgnoreCase(user.getRole())) {
                studentCount++;
            }

            if ("Active".equalsIgnoreCase(user.getStatus())) {
                activeCount++;
            } else {
                inactiveCount++;
            }
        }
    }

    int activePercentage = totalUsers > 0 ? (activeCount * 100) / totalUsers : 0;
    int inactivePercentage = totalUsers > 0 ? (inactiveCount * 100) / totalUsers : 0;
    int roleAssignmentPercentage = totalUsers > 0 ? 100 : 0;

    String success = request.getParameter("success");
    String error = request.getParameter("error");

    String alertType = "";
    String alertMessage = "";

    if (success != null) {
        alertType = "success";

        if ("userAdded".equalsIgnoreCase(success)) {
            alertMessage = "User account created successfully.";
        } else if ("userUpdated".equalsIgnoreCase(success)) {
            alertMessage = "User account updated successfully.";
        } else if ("userDeleted".equalsIgnoreCase(success)) {
            alertMessage = "User account deleted successfully.";
        } else if ("userActivated".equalsIgnoreCase(success)) {
            alertMessage = "User account activated successfully.";
        } else if ("userDeactivated".equalsIgnoreCase(success)) {
            alertMessage = "User account deactivated successfully.";
        } else if ("userStatusUpdated".equalsIgnoreCase(success)) {
            alertMessage = "User account status updated successfully.";
        } else {
            alertMessage = "Operation completed successfully.";
        }
    }

    if (error != null) {
        alertType = "danger";

        if ("missingUserId".equalsIgnoreCase(error)) {
            alertMessage = "User ID is missing.";
        } else if ("missingUsername".equalsIgnoreCase(error)) {
            alertMessage = "Username is required.";
        } else if ("missingPassword".equalsIgnoreCase(error)) {
            alertMessage = "Password is required.";
        } else if ("missingEmail".equalsIgnoreCase(error)) {
            alertMessage = "Email address is required.";
        } else if ("invalidEmail".equalsIgnoreCase(error)) {
            alertMessage = "Please enter a valid email address.";
        } else if ("missingRole".equalsIgnoreCase(error)) {
            alertMessage = "User role is required.";
        } else if ("invalidRole".equalsIgnoreCase(error)) {
            alertMessage = "Invalid user role selected.";
        } else if ("missingStatus".equalsIgnoreCase(error)) {
            alertMessage = "Account status is required.";
        } else if ("invalidStatus".equalsIgnoreCase(error)) {
            alertMessage = "Invalid account status selected.";
        } else if ("userAddFailed".equalsIgnoreCase(error)) {
            alertMessage = "User account could not be created. Check duplicate User ID, username, or email.";
        } else if ("userUpdateFailed".equalsIgnoreCase(error)) {
            alertMessage = "User account could not be updated.";
        } else if ("userDeleteFailed".equalsIgnoreCase(error)) {
            alertMessage = "User account could not be deleted.";
        } else if ("userStatusUpdateFailed".equalsIgnoreCase(error)) {
            alertMessage = "User account status could not be updated.";
        } else if ("invalidAction".equalsIgnoreCase(error)) {
            alertMessage = "Invalid user management action.";
        } else {
            alertMessage = "Something went wrong. Please check the user details and try again.";
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
                            <i class="bi bi-person-gear me-1"></i>
                            NextExamLK Access Control Center
                        </span>

                        <h1 class="hero-title">User & Role Management</h1>

                        <p class="hero-text">
                            Manage administrator, lecturer, and student login accounts. This module controls
                            authentication, role-based access, account availability, and secure platform access
                            for the online examination system.
                        </p>
                    </div>

                    <div class="d-flex gap-2 flex-wrap">
                        <button class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#userModal">
                            <i class="bi bi-person-plus-fill me-2"></i>
                            Add User
                        </button>

                        <a href="<%= request.getContextPath() %>/dashboard.jsp" class="btn btn-outline-primary">
                            <i class="bi bi-grid-1x2-fill me-2"></i>
                            Dashboard
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
                                <div class="stat-label">Total Users</div>
                                <div class="stat-value"><%= totalUsers %></div>
                                <div class="stat-meta">All platform accounts</div>
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
                                <div class="stat-label">Administrators</div>
                                <div class="stat-value"><%= adminCount %></div>
                                <div class="stat-meta">System control users</div>
                            </div>

                            <div class="stat-icon">
                                <i class="bi bi-shield-lock-fill"></i>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-md-6 col-xl-3">
                    <div class="app-card stat-card">
                        <div class="d-flex justify-content-between gap-3">
                            <div>
                                <div class="stat-label">Lecturers</div>
                                <div class="stat-value"><%= lecturerCount %></div>
                                <div class="stat-meta">Exam and result handlers</div>
                            </div>

                            <div class="stat-icon">
                                <i class="bi bi-person-video3"></i>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-md-6 col-xl-3">
                    <div class="app-card stat-card">
                        <div class="d-flex justify-content-between gap-3">
                            <div>
                                <div class="stat-label">Students</div>
                                <div class="stat-value"><%= studentCount %></div>
                                <div class="stat-meta">Candidate login accounts</div>
                            </div>

                            <div class="stat-icon">
                                <i class="bi bi-mortarboard-fill"></i>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row g-4 mb-4">
                <div class="col-xl-5">
                    <div class="app-card p-4 h-100">
                        <div class="d-flex justify-content-between align-items-start mb-3">
                            <div>
                                <h4 class="fw-bold mb-1">Account Health</h4>
                                <p class="text-secondary mb-0">
                                    Current access and account availability status.
                                </p>
                            </div>

                            <span class="badge badge-soft-primary">Live</span>
                        </div>

                        <div class="readiness-board border-0 shadow-none p-0">
                            <div class="readiness-item mb-3">
                                <div class="d-flex justify-content-between mb-1">
                                    <span class="fw-semibold">Active Accounts</span>
                                    <span class="fw-bold"><%= activePercentage %>%</span>
                                </div>

                                <div class="progress" style="height: 9px;">
                                    <div class="progress-bar bg-success" style="width: <%= activePercentage %>%;"></div>
                                </div>

                                <small class="text-secondary">Accounts currently allowed to access the platform.</small>
                            </div>

                            <div class="readiness-item mb-3">
                                <div class="d-flex justify-content-between mb-1">
                                    <span class="fw-semibold">Role Assignment</span>
                                    <span class="fw-bold"><%= roleAssignmentPercentage %>%</span>
                                </div>

                                <div class="progress" style="height: 9px;">
                                    <div class="progress-bar" style="width: <%= roleAssignmentPercentage %>%;"></div>
                                </div>

                                <small class="text-secondary">Each user account has an assigned role.</small>
                            </div>

                            <div class="readiness-item">
                                <div class="d-flex justify-content-between mb-1">
                                    <span class="fw-semibold">Inactive Accounts</span>
                                    <span class="fw-bold"><%= inactivePercentage %>%</span>
                                </div>

                                <div class="progress" style="height: 9px;">
                                    <div class="progress-bar bg-warning" style="width: <%= inactivePercentage %>%;"></div>
                                </div>

                                <small class="text-secondary">Accounts disabled or pending review.</small>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-xl-7">
                    <div class="app-card p-4 h-100">
                        <div class="d-flex justify-content-between align-items-start flex-wrap gap-3 mb-4">
                            <div>
                                <h4 class="fw-bold mb-1">Role Permissions Overview</h4>
                                <p class="text-secondary mb-0">
                                    Clear separation of platform responsibilities by user role.
                                </p>
                            </div>

                            <span class="badge badge-soft-secondary">Access Matrix</span>
                        </div>

                        <div class="row g-3">
                            <div class="col-md-4">
                                <div class="role-permission-card h-100 p-3">
                                    <div class="quick-icon mb-3">
                                        <i class="bi bi-shield-lock-fill"></i>
                                    </div>
                                    <h6 class="fw-bold">Admin</h6>
                                    <ul class="role-permission-list">
                                        <li>Manage users</li>
                                        <li>Manage students</li>
                                        <li>Manage faculties</li>
                                        <li>Review platform activity</li>
                                    </ul>
                                </div>
                            </div>

                            <div class="col-md-4">
                                <div class="role-permission-card h-100 p-3">
                                    <div class="quick-icon mb-3">
                                        <i class="bi bi-person-video3"></i>
                                    </div>
                                    <h6 class="fw-bold">Lecturer</h6>
                                    <ul class="role-permission-list">
                                        <li>Create exams</li>
                                        <li>Manage questions</li>
                                        <li>Review submissions</li>
                                        <li>Verify results</li>
                                    </ul>
                                </div>
                            </div>

                            <div class="col-md-4">
                                <div class="role-permission-card h-100 p-3">
                                    <div class="quick-icon mb-3">
                                        <i class="bi bi-mortarboard-fill"></i>
                                    </div>
                                    <h6 class="fw-bold">Student</h6>
                                    <ul class="role-permission-list">
                                        <li>Access my exams</li>
                                        <li>Attempt exams</li>
                                        <li>View notices</li>
                                        <li>Check results</li>
                                    </ul>
                                </div>
                            </div>
                        </div>

                        <div class="alert alert-info mt-4 mb-0">
                            <strong>Login workflow:</strong>
                            LoginServlet validates username or email, password, selected role, and active account status
                            using records saved in <code>users.txt</code>.
                        </div>
                    </div>
                </div>
            </div>

            <div class="page-header">
                <div>
                    <h2 class="page-title">User Account Records</h2>
                    <p class="page-description">
                        Search, create, update, activate, deactivate, and delete platform user accounts.
                    </p>
                </div>

                <button class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#userModal">
                    <i class="bi bi-person-plus me-2"></i>
                    Add User
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
                               id="userSearch"
                               placeholder="Search by user ID, username, email, role, or status">
                    </div>

                    <div class="d-flex gap-2 flex-wrap">
                        <select class="form-select" id="roleFilter" style="width:170px;">
                            <option value="">All Roles</option>
                            <option value="admin">Admin</option>
                            <option value="lecturer">Lecturer</option>
                            <option value="student">Student</option>
                        </select>

                        <select class="form-select" id="userStatusFilter" style="width:170px;">
                            <option value="">All Status</option>
                            <option value="active">Active</option>
                            <option value="inactive">Inactive</option>
                        </select>

                        <button class="btn btn-outline-secondary" type="button" id="clearUserFiltersBtn">
                            <i class="bi bi-x-circle me-1"></i>
                            Clear
                        </button>
                    </div>
                </div>

                <div class="table-responsive">
                    <table class="table table-hover align-middle" id="userTable">
                        <thead>
                        <tr>
                            <th>User ID</th>
                            <th>Username</th>
                            <th>Email</th>
                            <th>Role</th>
                            <th>Status</th>
                            <th>Security</th>
                            <th class="text-end">Actions</th>
                        </tr>
                        </thead>

                        <tbody>
                        <%
                            if (users != null && !users.isEmpty()) {
                                for (User user : users) {
                                    String roleClass = "badge-soft-secondary";
                                    String statusClass = "badge-soft-secondary";
                                    String securityLabel = "Standard";

                                    if ("Admin".equalsIgnoreCase(user.getRole())) {
                                        roleClass = "badge-soft-primary";
                                        securityLabel = "High Privilege";
                                    } else if ("Lecturer".equalsIgnoreCase(user.getRole())) {
                                        roleClass = "badge-soft-warning";
                                        securityLabel = "Academic Staff";
                                    } else if ("Student".equalsIgnoreCase(user.getRole())) {
                                        roleClass = "badge-soft-success";
                                        securityLabel = "Candidate";
                                    }

                                    if ("Active".equalsIgnoreCase(user.getStatus())) {
                                        statusClass = "badge-soft-success";
                                    } else {
                                        statusClass = "badge-soft-danger";
                                    }
                        %>
                        <tr data-role="<%= FileUtil.h(user.getRole().toLowerCase()) %>"
                            data-status="<%= FileUtil.h(user.getStatus().toLowerCase()) %>">
                            <td class="fw-bold"><%= FileUtil.h(user.getUserId()) %></td>
                            <td><%= FileUtil.h(user.getUsername()) %></td>
                            <td><%= FileUtil.h(user.getEmail()) %></td>

                            <td>
                                <span class="badge <%= roleClass %>">
                                    <%= FileUtil.h(user.getRole()) %>
                                </span>
                            </td>

                            <td>
                                <span class="badge <%= statusClass %>">
                                    <%= FileUtil.h(user.getStatus()) %>
                                </span>
                            </td>

                            <td>
                                <span class="badge badge-soft-secondary">
                                    <i class="bi bi-shield-lock me-1"></i>
                                    <%= FileUtil.h(securityLabel) %>
                                </span>
                            </td>

                            <td>
                                <div class="action-group">
                                    <button class="btn btn-sm btn-outline-primary"
                                            type="button"
                                            title="View User"
                                            data-bs-toggle="modal"
                                            data-bs-target="#viewUserModal"
                                            data-user-id="<%= FileUtil.h(user.getUserId()) %>"
                                            data-username="<%= FileUtil.h(user.getUsername()) %>"
                                            data-email="<%= FileUtil.h(user.getEmail()) %>"
                                            data-role="<%= FileUtil.h(user.getRole()) %>"
                                            data-status="<%= FileUtil.h(user.getStatus()) %>"
                                            data-security="<%= FileUtil.h(securityLabel) %>">
                                        <i class="bi bi-eye"></i>
                                    </button>

                                    <button class="btn btn-sm btn-outline-primary"
                                            type="button"
                                            title="Edit User"
                                            data-bs-toggle="modal"
                                            data-bs-target="#editUserModal"
                                            data-user-id="<%= FileUtil.h(user.getUserId()) %>"
                                            data-username="<%= FileUtil.h(user.getUsername()) %>"
                                            data-email="<%= FileUtil.h(user.getEmail()) %>"
                                            data-role="<%= FileUtil.h(user.getRole()) %>"
                                            data-status="<%= FileUtil.h(user.getStatus()) %>">
                                        <i class="bi bi-pencil-square"></i>
                                    </button>

                                    <% if ("Active".equalsIgnoreCase(user.getStatus())) { %>
                                        <form action="<%= request.getContextPath() %>/users"
                                              method="post"
                                              class="d-inline">
                                            <input type="hidden" name="action" value="deactivate">
                                            <input type="hidden" name="userId" value="<%= FileUtil.h(user.getUserId()) %>">
                                            <button class="btn btn-sm btn-outline-warning"
                                                    type="submit"
                                                    title="Deactivate User">
                                                <i class="bi bi-person-dash"></i>
                                            </button>
                                        </form>
                                    <% } else { %>
                                        <form action="<%= request.getContextPath() %>/users"
                                              method="post"
                                              class="d-inline">
                                            <input type="hidden" name="action" value="activate">
                                            <input type="hidden" name="userId" value="<%= FileUtil.h(user.getUserId()) %>">
                                            <button class="btn btn-sm btn-outline-success"
                                                    type="submit"
                                                    title="Activate User">
                                                <i class="bi bi-person-check"></i>
                                            </button>
                                        </form>
                                    <% } %>

                                    <button class="btn btn-sm btn-outline-danger"
                                            type="button"
                                            title="Delete User"
                                            data-bs-toggle="modal"
                                            data-bs-target="#deleteModal"
                                            data-delete-name="<%= FileUtil.h(user.getUserId() + " - " + user.getUsername()) %>"
                                            data-delete-id="<%= FileUtil.h(user.getUserId()) %>"
                                            data-delete-url="<%= request.getContextPath() %>/users">
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
                            <td colspan="7">
                                <div class="empty-state">
                                    <div class="empty-state-icon">
                                        <i class="bi bi-inbox"></i>
                                    </div>
                                    <h5>No user records found</h5>
                                    <p>Add a user to display records here.</p>
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

<!-- Add User Modal -->
<div class="modal fade" id="userModal" tabindex="-1" aria-labelledby="userModalTitle" aria-hidden="true">
    <div class="modal-dialog modal-lg modal-dialog-centered">
        <div class="modal-content border-0 shadow-lg">

            <form class="needs-validation"
                  novalidate
                  action="<%= request.getContextPath() %>/users"
                  method="post">

                <input type="hidden" name="action" value="add">

                <div class="modal-header">
                    <div>
                        <h5 class="modal-title fw-bold" id="userModalTitle">Add User</h5>
                        <small class="text-secondary">
                            Create a login account for an administrator, lecturer, or student.
                        </small>
                    </div>

                    <button class="btn-close" type="button" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>

                <div class="modal-body">
                    <div class="row g-3">
                        <div class="col-md-6">
                            <label class="form-label">User ID <span class="required">*</span></label>
                            <input type="text"
                                   name="userId"
                                   class="form-control"
                                   placeholder="Example: U004"
                                   maxlength="30"
                                   required>
                            <div class="invalid-feedback">User ID is required.</div>
                        </div>

                        <div class="col-md-6">
                            <label class="form-label">Username <span class="required">*</span></label>
                            <input type="text"
                                   name="username"
                                   class="form-control"
                                   placeholder="Example: lecturer02"
                                   maxlength="80"
                                   required>
                            <div class="invalid-feedback">Username is required.</div>
                        </div>

                        <div class="col-md-6">
                            <label class="form-label">Password <span class="required">*</span></label>
                            <input type="password"
                                   name="password"
                                   class="form-control"
                                   placeholder="Enter password"
                                   minlength="4"
                                   maxlength="120"
                                   required>
                            <div class="invalid-feedback">Password is required.</div>
                        </div>

                        <div class="col-md-6">
                            <label class="form-label">Email <span class="required">*</span></label>
                            <input type="email"
                                   name="email"
                                   class="form-control"
                                   placeholder="user@nextexamlk.local"
                                   maxlength="120"
                                   required>
                            <div class="invalid-feedback">Valid email is required.</div>
                        </div>

                        <div class="col-md-6">
                            <label class="form-label">Role <span class="required">*</span></label>
                            <select name="role" class="form-select" required>
                                <option value="">Choose role</option>
                                <option value="Admin">Admin</option>
                                <option value="Lecturer">Lecturer</option>
                                <option value="Student">Student</option>
                            </select>
                            <div class="invalid-feedback">Role is required.</div>
                        </div>

                        <div class="col-md-6">
                            <label class="form-label">Status <span class="required">*</span></label>
                            <select name="status" class="form-select" required>
                                <option value="">Choose status</option>
                                <option value="Active">Active</option>
                                <option value="Inactive">Inactive</option>
                            </select>
                            <div class="invalid-feedback">Status is required.</div>
                        </div>
                    </div>

                    <div class="alert alert-info mt-4 mb-0">
                        <strong>Security note:</strong>
                        Passwords are currently stored in the file-based system. For production, upgrade this to hashed
                        passwords using BCrypt or another secure password hashing method.
                    </div>
                </div>

                <div class="modal-footer">
                    <button class="btn btn-outline-secondary" type="button" data-bs-dismiss="modal">
                        Cancel
                    </button>

                    <button class="btn btn-primary" type="submit">
                        <i class="bi bi-save me-2"></i>
                        Save User
                    </button>
                </div>
            </form>

        </div>
    </div>
</div>

<!-- Edit User Modal -->
<div class="modal fade" id="editUserModal" tabindex="-1" aria-labelledby="editUserModalTitle" aria-hidden="true">
    <div class="modal-dialog modal-lg modal-dialog-centered">
        <div class="modal-content border-0 shadow-lg">

            <form class="needs-validation"
                  novalidate
                  action="<%= request.getContextPath() %>/users"
                  method="post">

                <input type="hidden" name="action" value="update">

                <div class="modal-header">
                    <div>
                        <h5 class="modal-title fw-bold" id="editUserModalTitle">Edit User</h5>
                        <small class="text-secondary">
                            Update an existing platform user account.
                        </small>
                    </div>

                    <button class="btn-close" type="button" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>

                <div class="modal-body">
                    <div class="row g-3">
                        <div class="col-md-6">
                            <label class="form-label">User ID</label>
                            <input type="text"
                                   id="editUserId"
                                   name="userId"
                                   class="form-control"
                                   readonly
                                   required>
                            <div class="invalid-feedback">User ID is required.</div>
                        </div>

                        <div class="col-md-6">
                            <label class="form-label">Username</label>
                            <input type="text"
                                   id="editUsername"
                                   name="username"
                                   class="form-control"
                                   maxlength="80"
                                   required>
                            <div class="invalid-feedback">Username is required.</div>
                        </div>

                        <div class="col-md-6">
                            <label class="form-label">New / Current Password</label>
                            <input type="password"
                                   id="editPassword"
                                   name="password"
                                   class="form-control"
                                   placeholder="Enter new or existing password"
                                   minlength="4"
                                   maxlength="120"
                                   required>
                            <div class="invalid-feedback">Password is required.</div>
                            <small class="text-secondary">
                                Password is not shown for security. Enter the current password again or set a new one.
                            </small>
                        </div>

                        <div class="col-md-6">
                            <label class="form-label">Email</label>
                            <input type="email"
                                   id="editEmail"
                                   name="email"
                                   class="form-control"
                                   maxlength="120"
                                   required>
                            <div class="invalid-feedback">Valid email is required.</div>
                        </div>

                        <div class="col-md-6">
                            <label class="form-label">Role</label>
                            <select id="editRole" name="role" class="form-select" required>
                                <option value="">Choose role</option>
                                <option value="Admin">Admin</option>
                                <option value="Lecturer">Lecturer</option>
                                <option value="Student">Student</option>
                            </select>
                            <div class="invalid-feedback">Role is required.</div>
                        </div>

                        <div class="col-md-6">
                            <label class="form-label">Status</label>
                            <select id="editUserStatus" name="status" class="form-select" required>
                                <option value="">Choose status</option>
                                <option value="Active">Active</option>
                                <option value="Inactive">Inactive</option>
                            </select>
                            <div class="invalid-feedback">Status is required.</div>
                        </div>
                    </div>

                    <div class="alert alert-warning mt-4 mb-0">
                        <strong>Important:</strong>
                        For security, the existing password is not exposed in the page source. Enter the password value
                        you want to save for this account.
                    </div>
                </div>

                <div class="modal-footer">
                    <button class="btn btn-outline-secondary" type="button" data-bs-dismiss="modal">
                        Cancel
                    </button>

                    <button class="btn btn-primary" type="submit">
                        <i class="bi bi-save me-2"></i>
                        Update User
                    </button>
                </div>
            </form>

        </div>
    </div>
</div>

<!-- View User Modal -->
<div class="modal fade" id="viewUserModal" tabindex="-1" aria-labelledby="viewUserModalTitle" aria-hidden="true">
    <div class="modal-dialog modal-lg modal-dialog-centered">
        <div class="modal-content border-0 shadow-lg">

            <div class="modal-header">
                <div>
                    <h5 class="modal-title fw-bold" id="viewUserModalTitle">User Details</h5>
                    <small class="text-secondary">
                        View selected platform account information.
                    </small>
                </div>

                <button class="btn-close" type="button" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>

            <div class="modal-body">
                <div class="row g-3">
                    <div class="col-md-6">
                        <div class="exam-info-box">
                            <small>User ID</small>
                            <strong id="viewUserId">-</strong>
                        </div>
                    </div>

                    <div class="col-md-6">
                        <div class="exam-info-box">
                            <small>Username</small>
                            <strong id="viewUsername">-</strong>
                        </div>
                    </div>

                    <div class="col-md-6">
                        <div class="exam-info-box">
                            <small>Email</small>
                            <strong id="viewEmail">-</strong>
                        </div>
                    </div>

                    <div class="col-md-6">
                        <div class="exam-info-box">
                            <small>Password</small>
                            <strong>Hidden for security</strong>
                        </div>
                    </div>

                    <div class="col-md-4">
                        <div class="exam-info-box">
                            <small>Role</small>
                            <strong id="viewRole">-</strong>
                        </div>
                    </div>

                    <div class="col-md-4">
                        <div class="exam-info-box">
                            <small>Status</small>
                            <strong id="viewUserStatus">-</strong>
                        </div>
                    </div>

                    <div class="col-md-4">
                        <div class="exam-info-box">
                            <small>Security Level</small>
                            <strong id="viewSecurity">-</strong>
                        </div>
                    </div>
                </div>

                <div class="alert alert-info mt-4 mb-0">
                    <strong>User record:</strong>
                    Password values are intentionally hidden from the interface and page source.
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
        const editUserModal = document.getElementById("editUserModal");
        const viewUserModal = document.getElementById("viewUserModal");

        const userSearch = document.getElementById("userSearch");
        const roleFilter = document.getElementById("roleFilter");
        const userStatusFilter = document.getElementById("userStatusFilter");
        const clearUserFiltersBtn = document.getElementById("clearUserFiltersBtn");
        const userRows = document.querySelectorAll("#userTable tbody tr[data-role]");

        function getUserData(button) {
            return {
                userId: button.getAttribute("data-user-id") || "",
                username: button.getAttribute("data-username") || "",
                email: button.getAttribute("data-email") || "",
                role: button.getAttribute("data-role") || "",
                status: button.getAttribute("data-status") || "",
                security: button.getAttribute("data-security") || ""
            };
        }

        function filterUsers() {
            const searchValue = userSearch ? userSearch.value.toLowerCase().trim() : "";
            const roleValue = roleFilter ? roleFilter.value.toLowerCase().trim() : "";
            const statusValue = userStatusFilter ? userStatusFilter.value.toLowerCase().trim() : "";

            userRows.forEach(function (row) {
                const rowText = row.innerText.toLowerCase();
                const rowRole = row.getAttribute("data-role") || "";
                const rowStatus = row.getAttribute("data-status") || "";

                const matchesSearch = rowText.includes(searchValue);
                const matchesRole = roleValue === "" || rowRole === roleValue;
                const matchesStatus = statusValue === "" || rowStatus === statusValue;

                row.style.display = matchesSearch && matchesRole && matchesStatus ? "" : "none";
            });
        }

        if (userSearch) {
            userSearch.addEventListener("input", filterUsers);
        }

        if (roleFilter) {
            roleFilter.addEventListener("change", filterUsers);
        }

        if (userStatusFilter) {
            userStatusFilter.addEventListener("change", filterUsers);
        }

        if (clearUserFiltersBtn) {
            clearUserFiltersBtn.addEventListener("click", function () {
                if (userSearch) {
                    userSearch.value = "";
                }

                if (roleFilter) {
                    roleFilter.value = "";
                }

                if (userStatusFilter) {
                    userStatusFilter.value = "";
                }

                filterUsers();
            });
        }

        if (editUserModal) {
            editUserModal.addEventListener("show.bs.modal", function (event) {
                const button = event.relatedTarget;

                if (!button) {
                    return;
                }

                const user = getUserData(button);

                document.getElementById("editUserId").value = user.userId;
                document.getElementById("editUsername").value = user.username;
                document.getElementById("editPassword").value = "";
                document.getElementById("editEmail").value = user.email;
                document.getElementById("editRole").value = user.role;
                document.getElementById("editUserStatus").value = user.status;
            });
        }

        if (viewUserModal) {
            viewUserModal.addEventListener("show.bs.modal", function (event) {
                const button = event.relatedTarget;

                if (!button) {
                    return;
                }

                const user = getUserData(button);

                document.getElementById("viewUserId").textContent = user.userId || "-";
                document.getElementById("viewUsername").textContent = user.username || "-";
                document.getElementById("viewEmail").textContent = user.email || "-";
                document.getElementById("viewRole").textContent = user.role || "-";
                document.getElementById("viewUserStatus").textContent = user.status || "-";
                document.getElementById("viewSecurity").textContent = user.security || "-";
            });
        }
    });
</script>

<%@ include file="../includes/delete-modal.jsp" %>
<%@ include file="../includes/footer.jsp" %>