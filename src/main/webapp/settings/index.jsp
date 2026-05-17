<%--
    Admin System Settings and Branding Panel.

    Safe version:
    - Avoids direct SystemSettingDAO import/constants in JSP
    - Avoids duplicate appName variable conflict with includes/head.jsp
    - Uses settingAppName instead of appName

    Responsible Member:
    IT25103045 - De Silva H.L.D.C.P.C
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="lk.nextexam.dao.FileUtil" %>

<%
    String pageTitle = "System Settings";
    String activeMenu = "settings";
    String topbarTitle = "System Settings";

    Map<String, String> settingsMap = null;

    try {
        settingsMap = (Map<String, String>) request.getAttribute("settingsMap");
    } catch (Exception e) {
        settingsMap = null;
    }

    if (settingsMap == null) {
        settingsMap = new HashMap<String, String>();
    }

    String settingAppName = settingsMap.get("appName");
    String institutionName = settingsMap.get("institutionName");
    String academicYear = settingsMap.get("academicYear");
    String semester = settingsMap.get("semester");
    String supportEmail = settingsMap.get("supportEmail");
    String supportPhone = settingsMap.get("supportPhone");
    String footerText = settingsMap.get("footerText");
    String systemStatus = settingsMap.get("systemStatus");
    String defaultExamNote = settingsMap.get("defaultExamNote");
    String helpDeskMessage = settingsMap.get("helpDeskMessage");

    if (settingAppName == null || settingAppName.trim().isEmpty()) {
        settingAppName = "NextExamLK";
    }

    if (institutionName == null || institutionName.trim().isEmpty()) {
        institutionName = "Sri Lanka Institute of Information Technology";
    }

    if (academicYear == null || academicYear.trim().isEmpty()) {
        academicYear = "2026";
    }

    if (semester == null || semester.trim().isEmpty()) {
        semester = "Year 1 Semester 2";
    }

    if (supportEmail == null || supportEmail.trim().isEmpty()) {
        supportEmail = "support@nextexam.lk";
    }

    if (supportPhone == null || supportPhone.trim().isEmpty()) {
        supportPhone = "+94 77 000 0000";
    }

    if (footerText == null || footerText.trim().isEmpty()) {
        footerText = "Secure Online Examination and Result Management Platform";
    }

    if (systemStatus == null || systemStatus.trim().isEmpty()) {
        systemStatus = "Online";
    }

    if (defaultExamNote == null || defaultExamNote.trim().isEmpty()) {
        defaultExamNote = "Please read all exam rules carefully before starting the examination.";
    }

    if (helpDeskMessage == null || helpDeskMessage.trim().isEmpty()) {
        helpDeskMessage = "Contact the academic support team if you face login, exam, result, or document issues.";
    }

    String success = request.getParameter("success");
    String error = request.getParameter("error");
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
                            <i class="bi bi-sliders me-1"></i>
                            Admin Configuration
                        </span>

                        <h1 class="hero-title">System Settings & Branding</h1>

                        <p class="hero-text">
                            Manage platform branding, academic year details, support contacts,
                            footer text, and system status from one Admin-only configuration panel.
                        </p>
                    </div>

                    <a href="<%= request.getContextPath() %>/dashboard.jsp" class="btn btn-outline-primary">
                        <i class="bi bi-speedometer2 me-2"></i>
                        Dashboard
                    </a>
                </div>
            </div>

            <% if ("settingsUpdated".equalsIgnoreCase(success)) { %>
                <div class="alert alert-success" data-auto-close="4000">
                    <i class="bi bi-check-circle-fill me-1"></i>
                    System settings updated successfully.
                </div>
            <% } %>

            <% if ("missingRequired".equalsIgnoreCase(error)) { %>
                <div class="alert alert-danger">
                    <i class="bi bi-exclamation-triangle-fill me-1"></i>
                    Application name, institution name, and support email are required.
                </div>
            <% } else if ("settingsUpdateFailed".equalsIgnoreCase(error)) { %>
                <div class="alert alert-danger">
                    <i class="bi bi-exclamation-triangle-fill me-1"></i>
                    Settings update failed. Please try again.
                </div>
            <% } %>

            <div class="row g-4">
                <div class="col-xl-8">
                    <form method="post"
                          action="<%= request.getContextPath() %>/settings"
                          class="needs-validation"
                          novalidate>

                        <div class="app-card settings-form-card p-4 mb-4">
                            <div class="settings-section-heading">
                                <div>
                                    <h4>Branding Settings</h4>
                                    <p>Control how the system identity appears across the platform.</p>
                                </div>

                                <span class="badge badge-soft-primary">Branding</span>
                            </div>

                            <div class="row g-3">
                                <div class="col-md-6">
                                    <label class="form-label">
                                        Application Name <span class="required">*</span>
                                    </label>

                                    <input type="text"
                                           class="form-control"
                                           name="appName"
                                           value="<%= FileUtil.h(settingAppName) %>"
                                           required>
                                </div>

                                <div class="col-md-6">
                                    <label class="form-label">
                                        Institution Name <span class="required">*</span>
                                    </label>

                                    <input type="text"
                                           class="form-control"
                                           name="institutionName"
                                           value="<%= FileUtil.h(institutionName) %>"
                                           required>
                                </div>

                                <div class="col-12">
                                    <label class="form-label">Footer Branding Text</label>

                                    <input type="text"
                                           class="form-control"
                                           name="footerText"
                                           value="<%= FileUtil.h(footerText) %>">
                                </div>
                            </div>
                        </div>

                        <div class="app-card settings-form-card p-4 mb-4">
                            <div class="settings-section-heading">
                                <div>
                                    <h4>Academic Settings</h4>
                                    <p>Store academic period and general exam note information.</p>
                                </div>

                                <span class="badge badge-soft-info">Academic</span>
                            </div>

                            <div class="row g-3">
                                <div class="col-md-6">
                                    <label class="form-label">Academic Year</label>

                                    <input type="text"
                                           class="form-control"
                                           name="academicYear"
                                           value="<%= FileUtil.h(academicYear) %>">
                                </div>

                                <div class="col-md-6">
                                    <label class="form-label">Semester</label>

                                    <input type="text"
                                           class="form-control"
                                           name="semester"
                                           value="<%= FileUtil.h(semester) %>">
                                </div>

                                <div class="col-12">
                                    <label class="form-label">Default Exam Note</label>

                                    <textarea class="form-control"
                                              rows="3"
                                              name="defaultExamNote"><%= FileUtil.h(defaultExamNote) %></textarea>
                                </div>
                            </div>
                        </div>

                        <div class="app-card settings-form-card p-4 mb-4">
                            <div class="settings-section-heading">
                                <div>
                                    <h4>Support Settings</h4>
                                    <p>Manage help desk contact information and support message.</p>
                                </div>

                                <span class="badge badge-soft-success">Support</span>
                            </div>

                            <div class="row g-3">
                                <div class="col-md-6">
                                    <label class="form-label">
                                        Support Email <span class="required">*</span>
                                    </label>

                                    <input type="email"
                                           class="form-control"
                                           name="supportEmail"
                                           value="<%= FileUtil.h(supportEmail) %>"
                                           required>
                                </div>

                                <div class="col-md-6">
                                    <label class="form-label">Support Phone</label>

                                    <input type="text"
                                           class="form-control"
                                           name="supportPhone"
                                           value="<%= FileUtil.h(supportPhone) %>">
                                </div>

                                <div class="col-md-6">
                                    <label class="form-label">System Status</label>

                                    <select class="form-select" name="systemStatus">
                                        <option value="Online" <%= "Online".equalsIgnoreCase(systemStatus) ? "selected" : "" %>>
                                            Online
                                        </option>

                                        <option value="Maintenance" <%= "Maintenance".equalsIgnoreCase(systemStatus) ? "selected" : "" %>>
                                            Maintenance
                                        </option>

                                        <option value="Limited Access" <%= "Limited Access".equalsIgnoreCase(systemStatus) ? "selected" : "" %>>
                                            Limited Access
                                        </option>
                                    </select>
                                </div>

                                <div class="col-12">
                                    <label class="form-label">Help Desk Message</label>

                                    <textarea class="form-control"
                                              rows="3"
                                              name="helpDeskMessage"><%= FileUtil.h(helpDeskMessage) %></textarea>
                                </div>
                            </div>
                        </div>

                        <div class="d-flex justify-content-end gap-2 flex-wrap">
                            <a href="<%= request.getContextPath() %>/dashboard.jsp" class="btn btn-light">
                                Cancel
                            </a>

                            <button type="submit" class="btn btn-primary">
                                <i class="bi bi-save-fill me-2"></i>
                                Save Settings
                            </button>
                        </div>
                    </form>
                </div>

                <div class="col-xl-4">
                    <div class="app-card settings-preview-card p-4 mb-4">
                        <span class="badge badge-soft-primary mb-3">
                            <i class="bi bi-eye-fill me-1"></i>
                            Live Preview
                        </span>

                        <div class="settings-preview-brand">
                            <div class="settings-preview-logo">
                                <i class="bi bi-mortarboard-fill"></i>
                            </div>

                            <div>
                                <h3><%= FileUtil.h(settingAppName) %></h3>
                                <p><%= FileUtil.h(institutionName) %></p>
                            </div>
                        </div>

                        <div class="settings-preview-list">
                            <div>
                                <small>Academic Year</small>
                                <strong><%= FileUtil.h(academicYear) %></strong>
                            </div>

                            <div>
                                <small>Semester</small>
                                <strong><%= FileUtil.h(semester) %></strong>
                            </div>

                            <div>
                                <small>System Status</small>
                                <strong><%= FileUtil.h(systemStatus) %></strong>
                            </div>

                            <div>
                                <small>Support Email</small>
                                <strong><%= FileUtil.h(supportEmail) %></strong>
                            </div>

                            <div>
                                <small>Support Phone</small>
                                <strong><%= FileUtil.h(supportPhone) %></strong>
                            </div>
                        </div>
                    </div>

                    <div class="app-card settings-help-card p-4">
                        <h5 class="fw-bold mb-2">
                            <i class="bi bi-info-circle-fill me-1"></i>
                            Configuration Note
                        </h5>

                        <p class="text-secondary mb-0">
                            These settings are stored in a file-based configuration record and can be reused
                            across the interface without changing source code.
                        </p>
                    </div>
                </div>
            </div>

        </section>
    </main>
</div>

<%@ include file="../includes/footer.jsp" %>