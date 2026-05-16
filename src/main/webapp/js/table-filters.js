/*
 * Advanced Table Filtering Helper for Nextexam.
 *
 * Features:
 * - Search by text
 * - Filter by row data attributes
 * - Live visible record count
 * - Empty/no-match message
 * - Reset filters
 * - Works across Students, Exams, Questions, Submissions, Results, Appeals, Feedback
 *
 * Responsible Member:
 * IT25103045 - De Silva H.L.D.C.P.C
 */

(function () {
    "use strict";

    const TABLE_FILTER_CONFIGS = [
        {
            tableId: "studentsTable",
            searchId: "studentSearch",
            resetId: "clearStudentFiltersBtn",
            counterLabel: "students",
            filters: [
                { inputId: "batchFilter", attribute: "data-batch" },
                { inputId: "statusFilter", attribute: "data-status" }
            ]
        },
        {
            tableId: "examTable",
            searchId: "examSearch",
            resetId: "clearExamFiltersBtn",
            counterLabel: "exams",
            filters: [
                { inputId: "examStatusFilter", attribute: "data-status" }
            ]
        },
        {
            tableId: "questionsTable",
            searchId: "questionSearch",
            resetId: "clearQuestionFiltersBtn",
            counterLabel: "questions",
            filters: [
                { inputId: "questionTypeFilter", attribute: "data-type" },
                { inputId: "questionStatusFilter", attribute: "data-status" }
            ]
        },
        {
            tableId: "submissionTable",
            searchId: "submissionSearch",
            resetId: "clearSubmissionFiltersBtn",
            counterLabel: "submissions",
            filters: [
                { inputId: "statusFilter", attribute: "data-status" }
            ]
        },
        {
            tableId: "resultsTable",
            searchId: "resultSearch",
            resetId: "clearResultFiltersBtn",
            counterLabel: "results",
            filters: [
                { inputId: "gradeFilter", attribute: "data-grade" },
                { inputId: "statusFilter", attribute: "data-status" },
                { inputId: "verificationFilter", attribute: "data-verification" },
                { inputId: "publishedFilter", attribute: "data-published" }
            ]
        },
        {
            tableId: "appealTable",
            searchId: "appealSearch",
            resetId: "clearAppealFiltersBtn",
            counterLabel: "appeals",
            filters: [
                { inputId: "appealStatusFilter", attribute: "data-status" }
            ]
        },
        {
            tableId: "feedbackTable",
            searchId: "feedbackSearch",
            resetId: "clearFeedbackFiltersBtn",
            counterLabel: "feedback records",
            filters: [
                { inputId: "feedbackCategoryFilter", attribute: "data-category" },
                { inputId: "feedbackStatusFilter", attribute: "data-status" }
            ]
        }
    ];

    function normalize(value) {
        return (value || "").toString().toLowerCase().trim();
    }

    function getRows(table) {
        if (!table) {
            return [];
        }

        return Array.from(table.querySelectorAll("tbody tr")).filter(function (row) {
            return !row.classList.contains("advanced-filter-empty-row");
        });
    }

    function createCounter(table, config) {
        const existing = document.getElementById(config.tableId + "FilterCounter");

        if (existing) {
            return existing;
        }

        const wrapper = table.closest(".app-card, .crud-card, .table-responsive");

        const counter = document.createElement("div");
        counter.id = config.tableId + "FilterCounter";
        counter.className = "advanced-filter-counter";
        counter.innerHTML = "<i class='bi bi-funnel-fill'></i> <span>Showing all records</span>";

        if (wrapper && wrapper.classList.contains("table-responsive")) {
            wrapper.parentNode.insertBefore(counter, wrapper);
        } else if (wrapper) {
            const tableResponsive = wrapper.querySelector(".table-responsive");
            if (tableResponsive) {
                wrapper.insertBefore(counter, tableResponsive);
            } else {
                wrapper.appendChild(counter);
            }
        }

        return counter;
    }

    function createEmptyRow(table, columnCount, config) {
        let emptyRow = table.querySelector("tbody tr.advanced-filter-empty-row");

        if (emptyRow) {
            return emptyRow;
        }

        emptyRow = document.createElement("tr");
        emptyRow.className = "advanced-filter-empty-row";
        emptyRow.style.display = "none";

        const td = document.createElement("td");
        td.colSpan = columnCount || 10;
        td.innerHTML =
            "<div class='advanced-filter-empty'>" +
            "<div class='empty-state-icon'><i class='bi bi-search'></i></div>" +
            "<h5>No matching " + config.counterLabel + " found</h5>" +
            "<p>Try changing your search text or filter selection.</p>" +
            "</div>";

        emptyRow.appendChild(td);

        const tbody = table.querySelector("tbody");

        if (tbody) {
            tbody.appendChild(emptyRow);
        }

        return emptyRow;
    }

    function ensureResetButton(config) {
        const existing = document.getElementById(config.resetId);

        if (existing) {
            return existing;
        }

        const searchInput = document.getElementById(config.searchId);

        if (!searchInput) {
            return null;
        }

        const toolbar = searchInput.closest(".crud-toolbar") || searchInput.closest(".d-flex");

        if (!toolbar) {
            return null;
        }

        const button = document.createElement("button");
        button.type = "button";
        button.id = config.resetId;
        button.className = "btn btn-outline-primary advanced-filter-reset";
        button.innerHTML = "<i class='bi bi-arrow-counterclockwise me-1'></i> Reset";

        toolbar.appendChild(button);

        return button;
    }

    function rowMatchesSearch(row, searchValue) {
        if (!searchValue) {
            return true;
        }

        return normalize(row.innerText).includes(searchValue);
    }

    function rowMatchesFilters(row, config) {
        for (const filter of config.filters || []) {
            const input = document.getElementById(filter.inputId);

            if (!input) {
                continue;
            }

            const selectedValue = normalize(input.value);

            if (!selectedValue) {
                continue;
            }

            const rowValue = normalize(row.getAttribute(filter.attribute));

            if (rowValue !== selectedValue) {
                return false;
            }
        }

        return true;
    }

    function updateCounter(counter, visibleCount, totalCount, config) {
        if (!counter) {
            return;
        }

        const text = counter.querySelector("span");

        if (!text) {
            return;
        }

        if (visibleCount === totalCount) {
            text.textContent = "Showing all " + totalCount + " " + config.counterLabel;
        } else {
            text.textContent = "Showing " + visibleCount + " of " + totalCount + " " + config.counterLabel;
        }
    }

    function applyFilter(config) {
        const table = document.getElementById(config.tableId);

        if (!table) {
            return;
        }

        const rows = getRows(table);
        const searchInput = document.getElementById(config.searchId);
        const searchValue = searchInput ? normalize(searchInput.value) : "";

        const counter = createCounter(table, config);
        const columnCount = table.querySelectorAll("thead th").length || 10;
        const emptyRow = createEmptyRow(table, columnCount, config);

        let visibleCount = 0;

        rows.forEach(function (row) {
            const matchesSearch = rowMatchesSearch(row, searchValue);
            const matchesFilters = rowMatchesFilters(row, config);
            const visible = matchesSearch && matchesFilters;

            row.style.display = visible ? "" : "none";

            if (visible) {
                visibleCount++;
            }
        });

        if (emptyRow) {
            emptyRow.style.display = visibleCount === 0 ? "" : "none";
        }

        updateCounter(counter, visibleCount, rows.length, config);
    }

    function resetFilter(config) {
        const searchInput = document.getElementById(config.searchId);

        if (searchInput) {
            searchInput.value = "";
        }

        for (const filter of config.filters || []) {
            const input = document.getElementById(filter.inputId);

            if (input) {
                input.value = "";
            }
        }

        applyFilter(config);
    }

    function attachConfig(config) {
        const table = document.getElementById(config.tableId);

        if (!table) {
            return;
        }

        const searchInput = document.getElementById(config.searchId);

        if (searchInput) {
            searchInput.addEventListener("input", function () {
                applyFilter(config);
            });
        }

        for (const filter of config.filters || []) {
            const input = document.getElementById(filter.inputId);

            if (input) {
                input.addEventListener("change", function () {
                    applyFilter(config);
                });
            }
        }

        const resetButton = ensureResetButton(config);

        if (resetButton) {
            resetButton.addEventListener("click", function () {
                resetFilter(config);
            });
        }

        applyFilter(config);
    }

    function setupNotificationFiltering() {
        const list = document.querySelector(".notification-list");

        if (!list) {
            return;
        }

        const items = Array.from(list.querySelectorAll(".notification-item"));

        if (items.length === 0) {
            return;
        }

        const parentCard = list.closest(".app-card");

        if (!parentCard) {
            return;
        }

        let toolbar = parentCard.querySelector(".advanced-notification-toolbar");

        if (!toolbar) {
            toolbar = document.createElement("div");
            toolbar.className = "advanced-notification-toolbar";

            toolbar.innerHTML =
                "<div class='input-group search-control'>" +
                "<span class='input-group-text'><i class='bi bi-search'></i></span>" +
                "<input type='search' class='form-control' id='notificationSearch' placeholder='Search notifications'>" +
                "</div>" +
                "<select class='form-select' id='notificationStatusFilter'>" +
                "<option value=''>All Status</option>" +
                "<option value='unread'>Unread</option>" +
                "<option value='read'>Read</option>" +
                "</select>" +
                "<select class='form-select' id='notificationTypeFilter'>" +
                "<option value=''>All Types</option>" +
                "<option value='result'>Result</option>" +
                "<option value='appeal'>Appeal</option>" +
                "<option value='document'>Document</option>" +
                "<option value='feedback'>Feedback</option>" +
                "<option value='exam'>Exam</option>" +
                "<option value='notice'>Notice</option>" +
                "<option value='system'>System</option>" +
                "</select>" +
                "<button type='button' class='btn btn-outline-primary' id='clearNotificationFiltersBtn'>" +
                "<i class='bi bi-arrow-counterclockwise me-1'></i> Reset" +
                "</button>" +
                "<div class='advanced-filter-counter' id='notificationFilterCounter'>" +
                "<i class='bi bi-funnel-fill'></i> <span>Showing all notifications</span>" +
                "</div>";

            parentCard.insertBefore(toolbar, list);
        }

        const search = document.getElementById("notificationSearch");
        const statusFilter = document.getElementById("notificationStatusFilter");
        const typeFilter = document.getElementById("notificationTypeFilter");
        const reset = document.getElementById("clearNotificationFiltersBtn");
        const counter = document.getElementById("notificationFilterCounter");

        function applyNotificationFilter() {
            const searchValue = normalize(search ? search.value : "");
            const statusValue = normalize(statusFilter ? statusFilter.value : "");
            const typeValue = normalize(typeFilter ? typeFilter.value : "");

            let visibleCount = 0;

            items.forEach(function (item) {
                const text = normalize(item.innerText);
                const isUnread = item.classList.contains("unread");
                const statusText = isUnread ? "unread" : "read";

                const badgeTexts = Array.from(item.querySelectorAll(".badge"))
                    .map(function (badge) {
                        return normalize(badge.innerText);
                    })
                    .join(" ");

                const matchesSearch = !searchValue || text.includes(searchValue);
                const matchesStatus = !statusValue || statusText === statusValue || badgeTexts.includes(statusValue);
                const matchesType = !typeValue || badgeTexts.includes(typeValue);

                const visible = matchesSearch && matchesStatus && matchesType;

                item.style.display = visible ? "" : "none";

                if (visible) {
                    visibleCount++;
                }
            });

            if (counter) {
                const span = counter.querySelector("span");

                if (span) {
                    span.textContent = "Showing " + visibleCount + " of " + items.length + " notifications";
                }
            }

            let empty = parentCard.querySelector(".advanced-notification-empty");

            if (!empty) {
                empty = document.createElement("div");
                empty.className = "advanced-notification-empty advanced-filter-empty";
                empty.style.display = "none";
                empty.innerHTML =
                    "<div class='empty-state-icon'><i class='bi bi-search'></i></div>" +
                    "<h5>No matching notifications found</h5>" +
                    "<p>Try changing your search text or filter selection.</p>";
                list.parentNode.insertBefore(empty, list.nextSibling);
            }

            empty.style.display = visibleCount === 0 ? "" : "none";
        }

        if (search) {
            search.addEventListener("input", applyNotificationFilter);
        }

        if (statusFilter) {
            statusFilter.addEventListener("change", applyNotificationFilter);
        }

        if (typeFilter) {
            typeFilter.addEventListener("change", applyNotificationFilter);
        }

        if (reset) {
            reset.addEventListener("click", function () {
                if (search) {
                    search.value = "";
                }

                if (statusFilter) {
                    statusFilter.value = "";
                }

                if (typeFilter) {
                    typeFilter.value = "";
                }

                applyNotificationFilter();
            });
        }

        applyNotificationFilter();
    }

    document.addEventListener("DOMContentLoaded", function () {
        TABLE_FILTER_CONFIGS.forEach(attachConfig);
        setupNotificationFiltering();
    });
})();