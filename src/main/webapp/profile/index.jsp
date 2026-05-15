<%--
    Professional profile page for Nextexam.
    Allows authenticated users to view account details and upload profile image.

    Responsible Member:
    IT25103045 - De Silva H.L.D.C.P.C
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ page import="lk.nextexam.dao.FileUtil" %>
<%@ page import="lk.nextexam.dao.UserDAO" %>
<%@ page import="lk.nextexam.dao.StudentDAO" %>
<%@ page import="lk.nextexam.model.User" %>
<%@ page import="lk.nextexam.model.Student" %>

<%
    String pageTitle = "My Profile";
    String activeMenu = "profile";
    String topbarTitle = "My Profile";

    String sessionUserId = session != null && session.getAttribute("userId") != null
            ? String.valueOf(session.getAttribute("userId"))
            : "";

    String sessionRole = session != null && session.getAttribute("userRole") != null
            ? String.valueOf(session.getAttribute("userRole"))
            : "";

    UserDAO userDAO = new UserDAO();
    StudentDAO studentDAO = new StudentDAO();

    User profileUser = userDAO.getUserById(application, sessionUserId);
    Student studentProfile = null;

    if ("Student".equalsIgnoreCase(sessionRole)) {
        studentProfile = studentDAO.getStudentById(application, sessionUserId);
    }

    if (profileUser == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp?error=sessionExpired");
        return;
    }

    String profileImage = profileUser.getProfileImage();
    boolean hasProfileImage = profileImage != null && !profileImage.trim().isEmpty();

    String success = request.getParameter("success");
    String error = request.getParameter("error");

    String alertType = "";
    String alertMessage = "";

    if (success != null) {
        alertType = "success";

        if ("imageUpdated".equalsIgnoreCase(success)) {
            alertMessage = "Profile image updated successfully.";
        } else {
            alertMessage = "Profile updated successfully.";
        }
    }

    if (error != null) {
        alertType = "danger";

        if ("missingImage".equalsIgnoreCase(error)) {
            alertMessage = "Please select an image before uploading.";
        } else if ("invalidImageType".equalsIgnoreCase(error)) {
            alertMessage = "Invalid image type. Please upload JPG, JPEG, PNG, or WEBP.";
        } else if ("fileTooLarge".equalsIgnoreCase(error)) {
            alertMessage = "Image is too large. Maximum allowed size is 3 MB.";
        } else if ("updateFailed".equalsIgnoreCase(error)) {
            alertMessage = "Could not update profile image. Please try again.";
        } else if ("userNotFound".equalsIgnoreCase(error)) {
            alertMessage = "User profile could not be found.";
        } else {
            alertMessage = "Something went wrong. Please try again.";
        }
    }
%>

<%@ include file="../includes/head.jsp" %>

<div class="app-shell">
    <%@ include file="../includes/sidebar.jsp" %>

    <main class="main-content">
        <%@ include file="../includes/topbar.jsp" %>

        <section class="page-wrapper">

            <div class="hero-card mb-4 profile-hero-card">
                <div class="d-flex justify-content-between align-items-start flex-wrap gap-3">
                    <div>
                        <span class="badge badge-soft-primary mb-3">
                            <i class="bi bi-person-circle me-1"></i>
                            Nextexam Account Center
                        </span>

                        <h1 class="hero-title">My Professional Profile</h1>

                        <p class="hero-text">
                            View your account details, role information, academic profile, and upload a professional profile image.
                        </p>
                    </div>

                    <a href="<%= request.getContextPath() %>/logout" class="btn btn-outline-primary">
                        <i class="bi bi-box-arrow-left me-2"></i>
                        Logout
                    </a>
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

            <div class="row g-4">
                <div class="col-xl-4">
                    <div class="app-card profile-card p-4 h-100">
                        <div class="profile-avatar-xl">
                            <% if (hasProfileImage) { %>
                                <img src="<%= request.getContextPath() %>/<%= FileUtil.h(profileImage) %>"
                                     alt="Profile image">
                            <% } else { %>
                                <span><%= FileUtil.h(profileUser.getInitials()) %></span>
                            <% } %>
                        </div>

                        <h3 class="profile-name-xl">
                            <%= FileUtil.h(profileUser.getDisplayName()) %>
                        </h3>

                        <p class="profile-email-xl">
                            <%= FileUtil.h(profileUser.getEmail()) %>
                        </p>

                        <div class="d-flex justify-content-center gap-2 flex-wrap mb-4">
                            <span class="badge badge-soft-primary">
                                <i class="bi bi-person-badge me-1"></i>
                                <%= FileUtil.h(profileUser.getRole()) %>
                            </span>

                            <span class="badge <%= profileUser.isActive() ? "badge-soft-success" : "badge-soft-warning" %>">
                                <i class="bi bi-shield-check me-1"></i>
                                <%= FileUtil.h(profileUser.getStatus()) %>
                            </span>
                        </div>

                        <form action="<%= request.getContextPath() %>/profile"
                              method="post"
                              enctype="multipart/form-data"
                              class="profile-upload-form">

                            <input type="hidden" name="action" value="uploadImage">

                            <label class="form-label">Upload Profile Image</label>

                            <input type="file"
                                   name="profileImage"
                                   class="form-control mb-3"
                                   accept=".jpg,.jpeg,.png,.webp"
                                   required>

                            <button type="submit" class="btn btn-primary w-100">
                                <i class="bi bi-cloud-upload me-2"></i>
                                Update Image
                            </button>

                            <small class="text-secondary d-block mt-3 text-center">
                                Supported: JPG, JPEG, PNG, WEBP. Max size: 3 MB.
                            </small>
                        </form>
                    </div>
                </div>

                <div class="col-xl-8">
                    <div class="row g-4">
                        <div class="col-12">
                            <div class="app-card p-4">
                                <div class="d-flex justify-content-between align-items-start flex-wrap gap-3 mb-3">
                                    <div>
                                        <h4 class="fw-bold mb-1">Account Information</h4>
                                        <p class="text-secondary mb-0">
                                            These details are loaded from the authenticated user record.
                                        </p>
                                    </div>

                                    <span class="badge badge-soft-secondary">
                                        users.txt
                                    </span>
                                </div>

                                <div class="row g-3">
                                    <div class="col-md-6">
                                        <div class="exam-info-box">
                                            <small>User ID</small>
                                            <strong><%= FileUtil.h(profileUser.getUserId()) %></strong>
                                        </div>
                                    </div>

                                    <div class="col-md-6">
                                        <div class="exam-info-box">
                                            <small>Username</small>
                                            <strong><%= FileUtil.h(profileUser.getUsername()) %></strong>
                                        </div>
                                    </div>

                                    <div class="col-md-6">
                                        <div class="exam-info-box">
                                            <small>Email</small>
                                            <strong><%= FileUtil.h(profileUser.getEmail()) %></strong>
                                        </div>
                                    </div>

                                    <div class="col-md-6">
                                        <div class="exam-info-box">
                                            <small>Role</small>
                                            <strong><%= FileUtil.h(profileUser.getRole()) %></strong>
                                        </div>
                                    </div>

                                    <div class="col-md-6">
                                        <div class="exam-info-box">
                                            <small>Account Status</small>
                                            <strong><%= FileUtil.h(profileUser.getStatus()) %></strong>
                                        </div>
                                    </div>

                                    <div class="col-md-6">
                                        <div class="exam-info-box">
                                            <small>Profile Image Path</small>
                                            <strong><%= hasProfileImage ? FileUtil.h(profileImage) : "Not uploaded" %></strong>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <% if (studentProfile != null) { %>
                            <div class="col-12">
                                <div class="app-card p-4">
                                    <div class="d-flex justify-content-between align-items-start flex-wrap gap-3 mb-3">
                                        <div>
                                            <h4 class="fw-bold mb-1">Academic Profile</h4>
                                            <p class="text-secondary mb-0">
                                                Student academic information connected with this account.
                                            </p>
                                        </div>

                                        <span class="badge badge-soft-info">
                                            Student Record
                                        </span>
                                    </div>

                                    <div class="row g-3">
                                        <div class="col-md-6">
                                            <div class="exam-info-box">
                                                <small>Student Name</small>
                                                <strong><%= FileUtil.h(studentProfile.getDisplayName()) %></strong>
                                            </div>
                                        </div>

                                        <div class="col-md-6">
                                            <div class="exam-info-box">
                                                <small>Course</small>
                                                <strong><%= FileUtil.h(studentProfile.getCourse()) %></strong>
                                            </div>
                                        </div>

                                        <div class="col-md-6">
                                            <div class="exam-info-box">
                                                <small>Batch</small>
                                                <strong><%= FileUtil.h(studentProfile.getBatch()) %></strong>
                                            </div>
                                        </div>

                                        <div class="col-md-6">
                                            <div class="exam-info-box">
                                                <small>Contact</small>
                                                <strong><%= FileUtil.h(studentProfile.getContact()) %></strong>
                                            </div>
                                        </div>

                                        <div class="col-md-6">
                                            <div class="exam-info-box">
                                                <small>Exam Status</small>
                                                <strong><%= FileUtil.h(studentProfile.getExamStatus()) %></strong>
                                            </div>
                                        </div>

                                        <div class="col-md-6">
                                            <div class="exam-info-box">
                                                <small>Profile Summary</small>
                                                <strong><%= FileUtil.h(studentProfile.getProfileSummary()) %></strong>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        <% } %>

                        <div class="col-12">
                            <div class="app-card p-4">
                                <div class="d-flex align-items-start gap-3">
                                    <div class="profile-security-icon">
                                        <i class="bi bi-shield-lock-fill"></i>
                                    </div>

                                    <div>
                                        <h4 class="fw-bold mb-1">Security Note</h4>
                                        <p class="text-secondary mb-0">
                                            Your profile image path is stored in the user text-file record, while the physical image
                                            file is stored in <strong>uploads/profile</strong>. This feature demonstrates image upload,
                                            servlet file handling, DAO-based update, and role-based authenticated access.
                                        </p>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

        </section>
    </main>
</div>

<%@ include file="../includes/footer.jsp" %>