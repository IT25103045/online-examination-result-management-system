document.addEventListener("DOMContentLoaded", function () {
    NextExamApp.init();
});

const DEMO_MODE = false;

const NextExamApp = {
    init: function () {
        this.enableValidation();
        this.enableMobileSidebar();
        this.enableDeleteModal();
        this.enableGradePreview();
        this.enableTableSearch();
        this.enableQuestionTypeSwitching();
        this.enableFeedbackCharacterCounter();
        this.enableToastAlerts();
        this.enablePasswordToggle();
        this.enableDemoLoginButtons();
        this.enableTopbarQuickSearch();
        this.enableAutoCloseAlerts();
        this.enableModalFocusFix();
    },

    /* -------------------------------------------------------
       1. Bootstrap Form Validation
    ------------------------------------------------------- */
    enableValidation: function () {
        const forms = document.querySelectorAll(".needs-validation");

        forms.forEach(function (form) {
            form.addEventListener("submit", function (event) {
                if (!form.checkValidity()) {
                    event.preventDefault();
                    event.stopPropagation();
                    NextExamApp.showToast("Please complete all required fields correctly.", "warning");
                } else if (DEMO_MODE) {
                    event.preventDefault();
                    NextExamApp.showToast("Form validated successfully.", "success");
                }

                form.classList.add("was-validated");
            });
        });
    },

    /* -------------------------------------------------------
       2. Mobile Sidebar
    ------------------------------------------------------- */
    enableMobileSidebar: function () {
        const menuBtn = document.getElementById("mobileMenuBtn");
        const sidebar = document.getElementById("sidebar");
        const backdrop = document.getElementById("sidebarBackdrop");

        if (!menuBtn || !sidebar || !backdrop) {
            return;
        }

        menuBtn.setAttribute("aria-expanded", "false");

        menuBtn.addEventListener("click", function () {
            sidebar.classList.add("show");
            backdrop.classList.add("show");
            document.body.style.overflow = "hidden";
            menuBtn.setAttribute("aria-expanded", "true");
        });

        backdrop.addEventListener("click", function () {
            NextExamApp.closeMobileSidebar(sidebar, backdrop, menuBtn);
        });

        document.querySelectorAll(".sidebar-link").forEach(function (link) {
            link.addEventListener("click", function () {
                NextExamApp.closeMobileSidebar(sidebar, backdrop, menuBtn);
            });
        });

        document.addEventListener("keydown", function (event) {
            if (event.key === "Escape") {
                NextExamApp.closeMobileSidebar(sidebar, backdrop, menuBtn);
            }
        });

        window.addEventListener("resize", function () {
            if (window.innerWidth > 991) {
                NextExamApp.closeMobileSidebar(sidebar, backdrop, menuBtn);
            }
        });
    },

    closeMobileSidebar: function (sidebar, backdrop, menuBtn) {
        if (sidebar) {
            sidebar.classList.remove("show");
        }

        if (backdrop) {
            backdrop.classList.remove("show");
        }

        if (menuBtn) {
            menuBtn.setAttribute("aria-expanded", "false");
        }

        document.body.style.overflow = "";
    },

    /* -------------------------------------------------------
       3. Delete Modal
    ------------------------------------------------------- */
    enableDeleteModal: function () {
        const deleteName = document.getElementById("deleteRecordName");
        const deleteRecordId = document.getElementById("deleteRecordId");
        const deleteForm = document.getElementById("deleteForm");

        document.querySelectorAll("[data-delete-name]").forEach(function (button) {
            button.addEventListener("click", function () {
                const name = button.getAttribute("data-delete-name") || "this record";
                const id = button.getAttribute("data-delete-id") || "";
                const url = button.getAttribute("data-delete-url") || "#";
                const examId = button.getAttribute("data-exam-id") || "";

                if (deleteName) {
                    deleteName.textContent = name;
                }

                if (deleteRecordId) {
                    deleteRecordId.value = id;
                }

                if (deleteForm) {
                    deleteForm.setAttribute("action", url);
                }

                const hiddenExamId = document.getElementById("deleteExamId");

                if (hiddenExamId) {
                    hiddenExamId.value = examId;
                }
            });
        });
    },

    /* -------------------------------------------------------
       4. Result Grade Preview
    ------------------------------------------------------- */
    enableGradePreview: function () {
        const marksInputs = document.querySelectorAll("#marks, [data-grade-preview]");

        marksInputs.forEach(function (marksInput) {
            const previewId = marksInput.getAttribute("data-grade-preview") || "gradePreview";
            const preview = document.getElementById(previewId);
            const form = marksInput.closest("form") || document;

            const gradeInput = form.querySelector('input[name="grade"]') || document.getElementById("grade");
            const statusInput = form.querySelector('input[name="status"]') || document.getElementById("status");

            if (!preview) {
                return;
            }

            marksInput.addEventListener("input", function () {
                const value = marksInput.value.trim();
                const marks = Number(value);

                if (value === "" || Number.isNaN(marks) || marks < 0 || marks > 100) {
                    preview.innerHTML =
                        '<span class="badge badge-soft-danger">Invalid</span>' +
                        '<span class="ms-2 text-secondary">Marks must be between 0 and 100.</span>';

                    if (gradeInput) {
                        gradeInput.value = "";
                    }

                    if (statusInput) {
                        statusInput.value = "";
                    }

                    return;
                }

                const result = NextExamApp.calculateGrade(marks);

                preview.innerHTML =
                    '<span class="badge ' + result.badgeClass + '">' + result.grade + '</span>' +
                    '<span class="ms-2 fw-bold">' + result.status + '</span>' +
                    '<div class="small text-secondary mt-2">' + result.message + '</div>';

                if (gradeInput) {
                    gradeInput.value = result.grade;
                }

                if (statusInput) {
                    statusInput.value = result.status;
                }
            });
        });
    },

    calculateGrade: function (marks) {
        if (marks >= 75) {
            return {
                grade: "A",
                status: "Pass",
                badgeClass: "badge-soft-success",
                message: "Excellent performance range."
            };
        }

        if (marks >= 65) {
            return {
                grade: "B",
                status: "Pass",
                badgeClass: "badge-soft-primary",
                message: "Strong performance range."
            };
        }

        if (marks >= 55) {
            return {
                grade: "C",
                status: "Pass",
                badgeClass: "badge-soft-info",
                message: "Average performance range."
            };
        }

        if (marks >= 40) {
            return {
                grade: "S",
                status: "Pass",
                badgeClass: "badge-soft-warning",
                message: "Minimum pass range."
            };
        }

        return {
            grade: "F",
            status: "Fail",
            badgeClass: "badge-soft-danger",
            message: "Below pass range."
        };
    },

    /* -------------------------------------------------------
       5. Live Table Search
    ------------------------------------------------------- */
    enableTableSearch: function () {
        document.querySelectorAll("[data-table-search]").forEach(function (input) {
            const tableId = input.getAttribute("data-table-search");
            const table = document.getElementById(tableId);

            if (!table) {
                return;
            }

            input.addEventListener("input", function () {
                NextExamApp.filterTable(input, table);
            });
        });
    },

    filterTable: function (input, table) {
        const keyword = input.value.toLowerCase().trim();
        const rows = table.querySelectorAll("tbody tr:not(.search-empty-row)");
        let visibleCount = 0;

        rows.forEach(function (row) {
            const text = row.innerText.toLowerCase();

            if (text.includes(keyword)) {
                row.style.display = "";
                visibleCount++;
            } else {
                row.style.display = "none";
            }
        });

        this.updateSearchEmptyState(table, visibleCount, keyword);
    },

    updateSearchEmptyState: function (table, visibleCount, keyword) {
        const tbody = table.querySelector("tbody");

        if (!tbody) {
            return;
        }

        let emptyRow = tbody.querySelector(".search-empty-row");

        if (keyword !== "" && visibleCount === 0) {
            if (!emptyRow) {
                emptyRow = document.createElement("tr");
                emptyRow.className = "search-empty-row";
                emptyRow.innerHTML =
                    '<td colspan="20">' +
                    '<div class="empty-state">' +
                    '<div class="empty-state-icon"><i class="bi bi-search"></i></div>' +
                    '<h5>No matching records found</h5>' +
                    '<p>Try another keyword or clear the search field.</p>' +
                    '</div>' +
                    '</td>';

                tbody.appendChild(emptyRow);
            }
        } else if (emptyRow) {
            emptyRow.remove();
        }
    },

    /* -------------------------------------------------------
       6. Question Type Switching
    ------------------------------------------------------- */
    enableQuestionTypeSwitching: function () {
        const typeSelectors = document.querySelectorAll("#questionType, [data-question-type]");

        typeSelectors.forEach(function (questionType) {
            const form = questionType.closest("form") || document;
            const mcqFields = form.querySelectorAll(".mcq-field");
            const essayFields = form.querySelectorAll(".essay-field");

            function updateQuestionFields() {
                const selectedType = questionType.value;

                if (selectedType === "Essay") {
                    mcqFields.forEach(function (field) {
                        field.style.display = "none";

                        field.querySelectorAll("input, textarea, select").forEach(function (input) {
                            input.required = false;
                        });
                    });

                    essayFields.forEach(function (field) {
                        field.style.display = "";
                    });
                } else {
                    mcqFields.forEach(function (field) {
                        field.style.display = "";
                    });

                    essayFields.forEach(function (field) {
                        field.style.display = "none";
                    });
                }
            }

            questionType.addEventListener("change", updateQuestionFields);
            updateQuestionFields();
        });
    },

    /* -------------------------------------------------------
       7. Character Counter
    ------------------------------------------------------- */
    enableFeedbackCharacterCounter: function () {
        document.querySelectorAll("[data-character-counter]").forEach(function (textarea) {
            const counterId = textarea.getAttribute("data-character-counter");
            const counter = document.getElementById(counterId);
            const maxLength = textarea.getAttribute("maxlength");

            if (!counter) {
                return;
            }

            function updateCounter() {
                const currentLength = textarea.value.length;

                if (maxLength) {
                    counter.textContent = currentLength + " / " + maxLength + " characters";
                } else {
                    counter.textContent = currentLength + " characters";
                }

                if (maxLength && currentLength > Number(maxLength) * 0.9) {
                    counter.classList.add("text-danger");
                } else {
                    counter.classList.remove("text-danger");
                }
            }

            textarea.addEventListener("input", updateCounter);
            updateCounter();
        });
    },

    /* -------------------------------------------------------
       8. Toast Alerts
    ------------------------------------------------------- */
    enableToastAlerts: function () {
        if (!document.getElementById("appToastContainer")) {
            const container = document.createElement("div");
            container.id = "appToastContainer";
            container.style.position = "fixed";
            container.style.top = "92px";
            container.style.right = "24px";
            container.style.zIndex = "3000";
            container.style.display = "flex";
            container.style.flexDirection = "column";
            container.style.gap = "10px";
            container.style.pointerEvents = "none";
            document.body.appendChild(container);
        }
    },

    showToast: function (message, type) {
        const container = document.getElementById("appToastContainer");

        if (!container) {
            return;
        }

        const toast = document.createElement("div");

        let icon = "bi-info-circle-fill";
        let borderColor = "#2563eb";
        let title = this.getToastTitle(type);

        if (type === "success") {
            icon = "bi-check-circle-fill";
            borderColor = "#16a34a";
        } else if (type === "warning") {
            icon = "bi-exclamation-triangle-fill";
            borderColor = "#d97706";
        } else if (type === "danger") {
            icon = "bi-x-circle-fill";
            borderColor = "#dc2626";
        }

        toast.style.background = "#ffffff";
        toast.style.border = "1px solid #e2e8f0";
        toast.style.borderLeft = "5px solid " + borderColor;
        toast.style.borderRadius = "16px";
        toast.style.boxShadow = "0 14px 40px rgba(15, 23, 42, 0.16)";
        toast.style.padding = "14px 16px";
        toast.style.minWidth = "300px";
        toast.style.maxWidth = "380px";
        toast.style.display = "flex";
        toast.style.gap = "12px";
        toast.style.alignItems = "flex-start";
        toast.style.pointerEvents = "auto";
        toast.style.opacity = "1";
        toast.style.transform = "translateX(0)";

        toast.innerHTML =
            '<i class="bi ' + icon + '" style="color:' + borderColor + ';font-size:20px;"></i>' +
            '<div>' +
            '<div style="font-weight:850;color:#0f172a;">' + this.escapeHtml(title) + '</div>' +
            '<div style="font-size:13px;color:#64748b;">' + this.escapeHtml(message) + '</div>' +
            '</div>';

        container.appendChild(toast);

        setTimeout(function () {
            toast.style.opacity = "0";
            toast.style.transform = "translateX(20px)";
            toast.style.transition = "0.25s ease";

            setTimeout(function () {
                toast.remove();
            }, 280);
        }, 3500);
    },

    getToastTitle: function (type) {
        if (type === "success") {
            return "Success";
        }

        if (type === "warning") {
            return "Check Required";
        }

        if (type === "danger") {
            return "Action Alert";
        }

        return "Information";
    },

    /* -------------------------------------------------------
       9. Password Toggle
    ------------------------------------------------------- */
    enablePasswordToggle: function () {
        const toggleBtn = document.getElementById("togglePasswordBtn");

        if (toggleBtn) {
            toggleBtn.addEventListener("click", this.togglePasswordVisibility);
        }
    },

    togglePasswordVisibility: function () {
        const passwordInput = document.querySelector('input[name="password"]');
        const icon = document.getElementById("togglePasswordIcon");

        if (!passwordInput) {
            return;
        }

        if (passwordInput.type === "password") {
            passwordInput.type = "text";

            if (icon) {
                icon.classList.remove("bi-eye");
                icon.classList.add("bi-eye-slash");
            }
        } else {
            passwordInput.type = "password";

            if (icon) {
                icon.classList.remove("bi-eye-slash");
                icon.classList.add("bi-eye");
            }
        }
    },

    /* -------------------------------------------------------
       10. Demo Login Buttons
    ------------------------------------------------------- */
    enableDemoLoginButtons: function () {
        document.querySelectorAll("[data-demo-role]").forEach(function (button) {
            button.addEventListener("click", function () {
                const username = button.getAttribute("data-demo-username") || "admin";
                const role = button.getAttribute("data-demo-role") || "Admin";

                NextExamApp.fillDemoLogin(username, role);
            });
        });
    },

    fillDemoLogin: function (username, role) {
        const usernameInput = document.querySelector('input[name="username"]');
        const passwordInput = document.querySelector('input[name="password"]');
        const roleSelect = document.querySelector('select[name="role"]');

        let password = "admin123";

        if (role === "Lecturer") {
            password = "lecturer123";
        } else if (role === "Student") {
            password = "student123";
        }

        if (usernameInput) {
            usernameInput.value = username;
        }

        if (passwordInput) {
            passwordInput.value = password;
        }

        if (roleSelect) {
            roleSelect.value = role;
        }

        this.showToast(role + " demo credentials filled.", "success");
    },

    /* -------------------------------------------------------
       11. Topbar Quick Search
    ------------------------------------------------------- */
    enableTopbarQuickSearch: function () {
        const searchInput = document.getElementById("topbarQuickSearch");

        if (!searchInput) {
            return;
        }

        const currentRole = this.getCurrentRole();

        const staffRoutes = [
            { keyword: "dashboard", url: "dashboard.jsp" },
            { keyword: "student", url: "students" },
            { keyword: "faculty", url: "faculties" },
            { keyword: "exam", url: "exams" },
            { keyword: "question", url: "questions" },
            { keyword: "result", url: "results" },
            { keyword: "user", url: "users" },
            { keyword: "notice", url: "notices" },
            { keyword: "feedback", url: "feedback" }
        ];

        const studentRoutes = [
            { keyword: "my result", url: "my-results" },
            { keyword: "result", url: "my-results" },
            { keyword: "exam", url: "my-exams" },
            { keyword: "my exam", url: "my-exams" },
            { keyword: "notice", url: "notices" },
            { keyword: "feedback", url: "feedback" }
        ];

        const routes = currentRole === "student" ? studentRoutes : staffRoutes;

        searchInput.addEventListener("keydown", function (event) {
            if (event.key !== "Enter") {
                return;
            }

            const query = searchInput.value.toLowerCase().trim();

            if (query === "") {
                NextExamApp.showToast("Type a module name such as exams, results, notices, or feedback.", "warning");
                return;
            }

            const match = routes.find(function (route) {
                return query === route.keyword || query.includes(route.keyword);
            });

            if (match) {
                const contextPath = NextExamApp.getContextPath();
                window.location.href = contextPath + "/" + match.url;
            } else {
                NextExamApp.showToast("No module found for: " + query, "warning");
            }
        });
    },

    getCurrentRole: function () {
        const rolePill = document.querySelector(".role-pill");
        const userRole = document.querySelector(".topbar-user-role");

        const roleText = (
            rolePill ? rolePill.innerText : userRole ? userRole.innerText : ""
        ).toLowerCase();

        if (roleText.includes("student")) {
            return "student";
        }

        if (roleText.includes("lecturer")) {
            return "lecturer";
        }

        if (roleText.includes("admin")) {
            return "admin";
        }

        return "guest";
    },

    getContextPath: function () {
        const path = window.location.pathname;
        const parts = path.split("/").filter(Boolean);

        if (parts.length === 0) {
            return "";
        }

        return "/" + parts[0];
    },

    /* -------------------------------------------------------
       12. Auto Close Alerts
    ------------------------------------------------------- */
    enableAutoCloseAlerts: function () {
        document.querySelectorAll(".alert[data-auto-close]").forEach(function (alert) {
            const delay = Number(alert.getAttribute("data-auto-close")) || 4000;

            setTimeout(function () {
                alert.style.opacity = "0";
                alert.style.transition = "0.25s ease";

                setTimeout(function () {
                    alert.remove();
                }, 300);
            }, delay);
        });
    },

    /* -------------------------------------------------------
       13. Modal Focus Fix
    ------------------------------------------------------- */
    enableModalFocusFix: function () {
        document.querySelectorAll(".modal").forEach(function (modal) {
            modal.addEventListener("hidden.bs.modal", function () {
                const activeElement = document.activeElement;

                if (activeElement && typeof activeElement.blur === "function") {
                    activeElement.blur();
                }
            });
        });
    },

    /* -------------------------------------------------------
       Utilities
    ------------------------------------------------------- */
    escapeHtml: function (value) {
        return String(value)
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#039;");
    }
};

/* -------------------------------------------------------
   Global helpers used by existing JSP pages
------------------------------------------------------- */
function showToast(message, type) {
    NextExamApp.showToast(message, type);
}

function clearTableSearch(inputId) {
    const input = document.getElementById(inputId);

    if (!input) {
        return;
    }

    input.value = "";
    input.dispatchEvent(new Event("input"));
}

function escapeHtml(value) {
    return NextExamApp.escapeHtml(value);
}