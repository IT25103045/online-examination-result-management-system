<%@ page import="java.util.List" %>
<%@ page import="lk.nextexam.model.Faculty" %>

<%
    String pageTitle = "Faculty Management";
    String activeMenu = "faculties";
    String topbarTitle = "Faculty Management";

    List<Faculty> faculties = (List<Faculty>) request.getAttribute("faculties");

    int totalFaculties = faculties != null ? faculties.size() : 0;
    int activeFaculties = 0;
    int inactiveFaculties = 0;

    if (faculties != null) {
        for (Faculty faculty : faculties) {
            if ("Active".equalsIgnoreCase(faculty.getStatus())) {
                activeFaculties++;
            } else {
                inactiveFaculties++;
            }
        }
    }
%>

<%@ include file="../includes/head.jsp" %>
<%@ include file="../includes/sidebar.jsp" %>

<main class="main-content">
    <%@ include file="../includes/topbar.jsp" %>

    <div class="page-wrapper">

        <div class="hero-card mb-4">
            <div class="d-flex justify-content-between align-items-start gap-3">
                <div>
                    <h1 class="hero-title">Faculty Management</h1>
                    <p class="hero-text">
                        Manage university faculties such as Computing and Engineering for multi-faculty academic operations.
                    </p>
                </div>

                <button class="btn btn-primary"
                        data-bs-toggle="modal"
                        data-bs-target="#facultyModal"
                        onclick="openAddFacultyModal()">
                    <i class="bi bi-plus-circle me-1"></i>
                    Add Faculty
                </button>
            </div>
        </div>

        <div class="row g-3 mb-4">
            <div class="col-12 col-md-4">
                <div class="stat-card">
                    <div class="d-flex justify-content-between align-items-start">
                        <div>
                            <div class="stat-label">Total Faculties</div>
                            <div class="stat-value"><%= totalFaculties %></div>
                            <div class="stat-meta">Academic faculties registered</div>
                        </div>
                        <div class="stat-icon">
                            <i class="bi bi-building-fill"></i>
                        </div>
                    </div>
                </div>
            </div>

            <div class="col-12 col-md-4">
                <div class="stat-card">
                    <div class="d-flex justify-content-between align-items-start">
                        <div>
                            <div class="stat-label">Active</div>
                            <div class="stat-value"><%= activeFaculties %></div>
                            <div class="stat-meta">Currently available faculties</div>
                        </div>
                        <div class="stat-icon">
                            <i class="bi bi-check-circle-fill"></i>
                        </div>
                    </div>
                </div>
            </div>

            <div class="col-12 col-md-4">
                <div class="stat-card">
                    <div class="d-flex justify-content-between align-items-start">
                        <div>
                            <div class="stat-label">Inactive</div>
                            <div class="stat-value"><%= inactiveFaculties %></div>
                            <div class="stat-meta">Hidden or disabled faculties</div>
                        </div>
                        <div class="stat-icon">
                            <i class="bi bi-pause-circle-fill"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <% if (request.getParameter("success") != null) { %>
            <div class="alert alert-success">
                <i class="bi bi-check-circle-fill me-1"></i>
                Operation completed successfully.
            </div>
        <% } %>

        <% if (request.getParameter("error") != null) { %>
            <div class="alert alert-danger">
                <i class="bi bi-exclamation-triangle-fill me-1"></i>
                Something went wrong. Please check the details and try again.
            </div>
        <% } %>

        <div class="app-card p-4">
            <div class="crud-toolbar">
                <div>
                    <h5 class="fw-bold mb-1">Faculty Directory</h5>
                    <p class="text-muted mb-0">
                        View, edit, and maintain academic faculty records.
                    </p>
                </div>

                <div class="d-flex gap-2">
                    <div class="input-group search-control">
                        <span class="input-group-text bg-white">
                            <i class="bi bi-search"></i>
                        </span>
                        <input type="search"
                               id="facultySearch"
                               class="form-control"
                               placeholder="Search faculty...">
                    </div>

                    <select id="facultyStatusFilter" class="form-select" style="max-width: 170px;">
                        <option value="">All Status</option>
                        <option value="active">Active</option>
                        <option value="inactive">Inactive</option>
                    </select>
                </div>
            </div>

            <div class="table-responsive">
                <table class="table table-hover align-middle" id="facultyTable">
                    <thead>
                    <tr>
                        <th>Faculty ID</th>
                        <th>Faculty Name</th>
                        <th>Dean / Head</th>
                        <th>Contact Email</th>
                        <th>Status</th>
                        <th class="text-end">Actions</th>
                    </tr>
                    </thead>

                    <tbody>
                    <% if (faculties == null || faculties.isEmpty()) { %>
                        <tr>
                            <td colspan="6">
                                <div class="empty-state">
                                    <div class="empty-state-icon">
                                        <i class="bi bi-building"></i>
                                    </div>
                                    <h5>No faculties found</h5>
                                    <p>Start by adding Faculty of Computing or Faculty of Engineering.</p>
                                </div>
                            </td>
                        </tr>
                    <% } else {
                        for (Faculty faculty : faculties) {
                            String statusClass = "Active".equalsIgnoreCase(faculty.getStatus())
                                    ? "badge-soft-success"
                                    : "badge-soft-secondary";
                    %>
                        <tr>
                            <td>
                                <strong><%= faculty.getFacultyId() %></strong>
                            </td>

                            <td>
                                <div class="fw-bold"><%= faculty.getFacultyName() %></div>
                                <small class="text-muted">Academic faculty</small>
                            </td>

                            <td><%= faculty.getDeanName() %></td>
                            <td><%= faculty.getContactEmail() %></td>

                            <td>
                                <span class="badge <%= statusClass %>">
                                    <%= faculty.getStatus() %>
                                </span>
                            </td>

                            <td class="text-end">
                                <div class="action-group">
                                    <button class="btn btn-sm btn-outline-primary"
                                            data-bs-toggle="modal"
                                            data-bs-target="#facultyModal"
                                            onclick="openEditFacultyModal(
                                                '<%= faculty.getFacultyId() %>',
                                                '<%= faculty.getFacultyName() %>',
                                                '<%= faculty.getDeanName() %>',
                                                '<%= faculty.getContactEmail() %>',
                                                '<%= faculty.getStatus() %>'
                                            )">
                                        <i class="bi bi-pencil-square me-1"></i>
                                        Edit
                                    </button>

                                    <a class="btn btn-sm btn-outline-danger"
                                       href="<%= request.getContextPath() %>/faculties?action=delete&id=<%= faculty.getFacultyId() %>"
                                       onclick="return confirm('Are you sure you want to delete this faculty?')">
                                        <i class="bi bi-trash me-1"></i>
                                        Delete
                                    </a>
                                </div>
                            </td>
                        </tr>
                    <% }
                    } %>
                    </tbody>
                </table>
            </div>
        </div>

    </div>
</main>

<div class="modal fade" id="facultyModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <form class="modal-content" method="post" action="<%= request.getContextPath() %>/faculties">
            <div class="modal-header">
                <div>
                    <h5 class="modal-title" id="facultyModalTitle">Add Faculty</h5>
                    <small class="text-muted">Create or update academic faculty records.</small>
                </div>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>

            <div class="modal-body">
                <input type="hidden" name="action" id="facultyAction" value="add">

                <div class="row g-3">
                    <div class="col-12 col-md-6">
                        <label class="form-label">Faculty ID <span class="required">*</span></label>
                        <input type="text"
                               class="form-control"
                               name="facultyId"
                               id="facultyId"
                               placeholder="Example: F001"
                               required>
                    </div>

                    <div class="col-12 col-md-6">
                        <label class="form-label">Status <span class="required">*</span></label>
                        <select class="form-select" name="status" id="status" required>
                            <option value="Active">Active</option>
                            <option value="Inactive">Inactive</option>
                        </select>
                    </div>

                    <div class="col-12">
                        <label class="form-label">Faculty Name <span class="required">*</span></label>
                        <input type="text"
                               class="form-control"
                               name="facultyName"
                               id="facultyName"
                               placeholder="Example: Faculty of Computing"
                               required>
                    </div>

                    <div class="col-12 col-md-6">
                        <label class="form-label">Dean / Head Name</label>
                        <input type="text"
                               class="form-control"
                               name="deanName"
                               id="deanName"
                               placeholder="Example: Dean - Computing">
                    </div>

                    <div class="col-12 col-md-6">
                        <label class="form-label">Contact Email</label>
                        <input type="email"
                               class="form-control"
                               name="contactEmail"
                               id="contactEmail"
                               placeholder="Example: computing@sliit.lk">
                    </div>
                </div>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-light" data-bs-dismiss="modal">
                    Cancel
                </button>
                <button type="submit" class="btn btn-primary">
                    <i class="bi bi-save me-1"></i>
                    Save Faculty
                </button>
            </div>
        </form>
    </div>
</div>

<script>
    function openAddFacultyModal() {
        document.getElementById("facultyModalTitle").innerText = "Add Faculty";
        document.getElementById("facultyAction").value = "add";
        document.getElementById("facultyId").readOnly = false;

        document.getElementById("facultyId").value = "";
        document.getElementById("facultyName").value = "";
        document.getElementById("deanName").value = "";
        document.getElementById("contactEmail").value = "";
        document.getElementById("status").value = "Active";
    }

    function openEditFacultyModal(facultyId, facultyName, deanName, contactEmail, status) {
        document.getElementById("facultyModalTitle").innerText = "Edit Faculty";
        document.getElementById("facultyAction").value = "update";
        document.getElementById("facultyId").readOnly = true;

        document.getElementById("facultyId").value = facultyId;
        document.getElementById("facultyName").value = facultyName;
        document.getElementById("deanName").value = deanName;
        document.getElementById("contactEmail").value = contactEmail;
        document.getElementById("status").value = status;
    }

    const facultySearch = document.getElementById("facultySearch");
    const facultyStatusFilter = document.getElementById("facultyStatusFilter");
    const facultyRows = document.querySelectorAll("#facultyTable tbody tr");

    function filterFaculties() {
        const searchValue = facultySearch.value.toLowerCase();
        const statusValue = facultyStatusFilter.value.toLowerCase();

        facultyRows.forEach(function (row) {
            const rowText = row.innerText.toLowerCase();
            const statusText = rowText.includes("active") && !rowText.includes("inactive")
                ? "active"
                : rowText.includes("inactive")
                    ? "inactive"
                    : "";

            const matchesSearch = rowText.includes(searchValue);
            const matchesStatus = statusValue === "" || statusText === statusValue;

            row.style.display = matchesSearch && matchesStatus ? "" : "none";
        });
    }

    if (facultySearch && facultyStatusFilter) {
        facultySearch.addEventListener("input", filterFaculties);
        facultyStatusFilter.addEventListener("change", filterFaculties);
    }
</script>

<%@ include file="../includes/footer.jsp" %>