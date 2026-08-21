'use strict';
const API_BASE_URL = "http://localhost:8080";
const MY_SUMMARY_API_URL = API_BASE_URL + "/api/my-summary";

const DASHBOARD_URL =
    MY_SUMMARY_API_URL + "/dashboard";

const SEARCH_WORK_ITEMS_URL =
    MY_SUMMARY_API_URL + "/work-items/search";

const FOCUS_URL =
    MY_SUMMARY_API_URL + "/focus";

const ACTIVITIES_URL =
    MY_SUMMARY_API_URL + "/activities";

const AUDITS_URL =
    MY_SUMMARY_API_URL + "/audits";
``
const AUDIT_TRAIL_URL = "AuditTrail.html";

let summary = null;
let workItems = [];
let filteredWorkItems = [];
let selectedItem = null;

document.addEventListener("DOMContentLoaded", function () {
    bindEvents();
    loadMySummary();
});

function bindEvents() {
    document.getElementById("taskSearchInput").addEventListener("input", applyFilters);
    document.getElementById("globalSearchInput").addEventListener("input", syncGlobalSearch);
    document.getElementById("typeFilter").addEventListener("change", applyFilters);
    document.getElementById("statusFilter").addEventListener("change", applyFilters);

    document.getElementById("openItemButton").addEventListener("click", function () {
        if (!selectedItem) {
            return;
        }
        window.location.href = selectedItem.sourcePageUrl;
    });

    document.getElementById("auditTrailButton").addEventListener("click", function () {
        if (!selectedItem) {
            return;
        }
        window.location.href = selectedItem.auditTrailUrl;});
}

function goToPage(url) {
    window.location.href = url;
}

function syncGlobalSearch(event) {
    document.getElementById("taskSearchInput").value = event.target.value;
    applyFilters();
}

async function loadMySummary() {

    showLoading(true);

    try {

        await Promise.all([
            loadDashboard(),
            loadWorkItems(),
            loadFocus(),
            loadActivities(),
            loadAudits()
        ]);

    } catch (error) {

        console.error(error);

        showToast(
            "Unable to load dashboard"
        );

    } finally {

        showLoading(false);

    }
}

async function loadAudits() {

    const response =
        await authorizedFetch(
            AUDITS_URL +
            "?limit=10"
        );

    const result =
        await response.json();

    renderAudits(
        result.data || []
    );
}

async function loadDashboard() {

    const response =
        await authorizedFetch(
            DASHBOARD_URL
        );

    const result =
        await response.json();

    const dashboard =
        result.data;

    renderProfile(
        dashboard.profile
    );

    renderStats(
        dashboard.stats
    );
}

async function loadFocus() {

    const response =
        await authorizedFetch(
            FOCUS_URL
        );

    const result =
        await response.json();

    const focus =
        result.data;

    renderFocus(
        focus.focusItems || []
    );

    renderDeadlines(
        focus.deadlines || []
    );

    updateProgress(
        "reviewProgress",
        "reviewProgressText",
        focus.progress.reviewProgressPercentage
    );

    updateProgress(
        "approvalProgress",
        "approvalProgressText",
        focus.progress.approvalProgressPercentage
    );

    updateProgress(
        "proposalProgress",
        "proposalProgressText",
        focus.progress.proposalProgressPercentage
    );
}

function refreshSummary() {
    selectedItem = null;
    resetDetailPanel();
    loadMySummary();
}

function sortByDueDate(items) {
    return [...items].sort(function (a, b) {
        return new Date(a.dueDate).getTime() - new Date(b.dueDate).getTime();
    });
}

function applyFilters() {

    searchWorkItems();
}

function renderProfile(profile) {
    document.getElementById("profileName").textContent = profile.displayName || "Ankit Manchanda";
    document.getElementById("profileMeta").innerHTML =
        escapeHtml(profile.role || "Custom Software Engineering Specialist") + "<br>" +
        escapeHtml(profile.location || "Frankfurt") + " · Contract bidding platform";
}

function renderStats(
    stats
) {

    document.getElementById(
        "openTasksStat"
    ).textContent =
        stats.openTaskCount ?? 0;

    document.getElementById(
        "dueSoonStat"
    ).textContent =
        stats.dueSoonCount ?? 0;

    document.getElementById(
        "myPipelineStat"
    ).textContent =
        formatCompactMoney(
            stats.pipelineValue,
            "EUR"
        );

    document.getElementById(
        "proposalCount"
    ).textContent =
        stats.assignedProposalCount ?? 0;

    document.getElementById(
        "reviewCount"
    ).textContent =
        stats.pendingReviewCount ?? 0;

    document.getElementById(
        "approvalCount"
    ).textContent =
        stats.pendingApprovalCount ?? 0;

    document.getElementById(
        "highPriorityCount"
    ).textContent =
        stats.highPriorityCount ?? 0;

    updateProgress(
        "reviewProgress",
        "reviewProgressText",
        stats.reviewProgressPercentage
    );

    updateProgress(
        "approvalProgress",
        "approvalProgressText",
        stats.approvalProgressPercentage
    );

    updateProgress(
        "proposalProgress",
        "proposalProgressText",
        stats.proposalProgressPercentage
    );
}

async function loadWorkItems() {

    await searchWorkItems();
}

async function searchWorkItems() {

    const request = {

        searchText:
            document.getElementById(
                "taskSearchInput"
            ).value || null,

        type:
            document.getElementById(
                "typeFilter"
            ).value === "ALL"
                ? null
                : document.getElementById(
                    "typeFilter"
                ).value,

        status:
            document.getElementById(
                "statusFilter"
            ).value === "ALL"
                ? null
                : document.getElementById(
                    "statusFilter"
                ).value,

        page: 0,
        size: 20,
        sortBy: "dueDate",
        sortDirection: "ASC"
    };

    const response =
        await authorizedFetch(
            SEARCH_WORK_ITEMS_URL,
            {
                method: "POST",
                body:
                    JSON.stringify(
                        request
                    )
            }
        );

    const result =
        await response.json();

    workItems =
        result.data.content || [];

    filteredWorkItems = [
        ...workItems
    ];

    renderWorkItems();
}

function updateProgress(barId, textId, value) {
    const normalized = Math.max(0, Math.min(100, Number(value) || 0));
    document.getElementById(barId).style.width = normalized + "%";
    document.getElementById(textId).textContent = normalized + "%";
}

function renderWorkItems() {
    const tbody = document.getElementById("tasksTableBody");
    const emptyState = document.getElementById("emptyState");
    tbody.innerHTML = "";

    if (filteredWorkItems.length === 0) {
        emptyState.style.display = "block";
        return;
    }

    emptyState.style.display = "none";

    filteredWorkItems.forEach(function (item) {
        const tr = document.createElement("tr");
        tr.dataset.itemId = item.id;

        if (selectedItem && selectedItem.id === item.id) {
            tr.classList.add("selected");
        }

        tr.innerHTML = `
          <td>
            <div class="task-title">${escapeHtml(item.title)}</div>
            <div class="task-meta">${escapeHtml(item.relatedObjectName)} · ${escapeHtml(item.nextBestAction)}</div>
          </td>
          <td>
            <span class="pill ${getTypeClass(item.type)}">${getTypeIcon(item.type)} ${formatEnum(item.type)}</span>
          </td>
          <td>
            <span class="pill ${getStatusClass(item.status)}">${getStatusIcon(item.status)} ${formatEnum(item.status)}</span>
          </td>
          <td>
            <span class="priority ${getPriorityClass(item.priority)}">${formatEnum(item.priority)}</span>
          </td>
          <td>
            <span class="task-title">${formatDate(item.dueDate)}</span>
          </td>
          <td>
            <span class="task-title">${formatMoney(item.value, item.currency)}</span>
          </td>
        `;

        tr.addEventListener("click", function () {
            handleItemClick(item);
        });

        tbody.appendChild(tr);
    });
}

async function handleItemClick(
    item
) {

    const response =
        await authorizedFetch(
            MY_SUMMARY_API_URL +
            "/work-items/" +
            item.id
        );

    const result =
        await response.json();

    selectedItem =
        result.data;

    renderWorkItems();

    loadDetailPanel(
        selectedItem
    );
}

async function loadActivities() {

    const response =
        await authorizedFetch(
            ACTIVITIES_URL +
            "?limit=10"
        );

    const result =
        await response.json();

    renderActivities(
        result.data || []
    );
}

function loadDetailPanel(item) {
    document.getElementById("detailPlaceholder").style.display = "none";
    document.getElementById("detailContent").style.display = "block";
    document.getElementById("detailTitle").textContent = item.title;
    document.getElementById("detailSubtitle").textContent = item.relatedObjectName;
    document.getElementById("detailType").textContent = formatEnum(item.type);
    document.getElementById("detailStatus").textContent = formatEnum(item.status);
    document.getElementById("detailPriority").textContent = formatEnum(item.priority);
    document.getElementById("detailDueDate").textContent = formatDate(item.dueDate);
    document.getElementById("detailObject").textContent = item.relatedObjectName || "-";
    document.getElementById("detailValue").textContent = formatMoney(item.value, item.currency);
    document.getElementById("detailContext").textContent = item.context || "-";
    document.getElementById("detailNextAction").textContent = item.nextBestAction || "-";
}

function resetDetailPanel() {
    document.getElementById("detailPlaceholder").style.display = "flex";
    document.getElementById("detailContent").style.display = "none";
}

function renderFocus(items) {
    const list = document.getElementById("focusList");
    list.innerHTML = "";
    items.forEach(function (item) {
        list.appendChild(createTimelineItem(item.icon, item.title, item.text));
    });
}

function renderDeadlines(items) {
    const list = document.getElementById("deadlineList");
    list.innerHTML = "";
    items.forEach(function (item) {
        list.appendChild(createTimelineItem(item.icon, item.title, item.text));
    });
}

function renderActivities(items) {
    const list = document.getElementById("activityList");
    list.innerHTML = "";
    items.forEach(function (activity) {
        const row = document.createElement("div");
        row.className = "activity-item";
        row.innerHTML = `
          <div>
            <div class="activity-title">${escapeHtml(activity.title)}</div>
            <div class="activity-desc">${escapeHtml(activity.description)}</div>
          </div>
          <div class="activity-meta">${escapeHtml(activity.meta)}</div>
        `;
        list.appendChild(row);
    });
}

function renderAudits(items) {
    const list = document.getElementById("auditList");
    list.innerHTML = "";
    items.forEach(function (item) {
        list.appendChild(
            createTimelineItem(
                "📝",
                item.title,
                item.description
            )
        );
    });
}

async function authorizedFetch(
    url,
    options = {}
) {

    const token =
        getAccessToken();

    return fetch(
        url,
        {
            ...options,
            headers: {
                "Content-Type":
                    "application/json",
                "Authorization":
                    token
                        ? "Bearer " +
                        token
                        : "",
                ...(options.headers || {})
            }
        }
    );
}

function createTimelineItem(icon, title, text) {
    const row = document.createElement("div");
    row.className = "timeline-item";
    row.innerHTML = `
        <div class="timeline-icon">${icon}</div>
        <div>
          <div class="timeline-title">${escapeHtml(title)}</div>
          <div class="timeline-text">${escapeHtml(text)}</div>
        </div>
      `;
    return row;
}

function getAccessToken() {
    if (window.keycloak && window.keycloak.token) {
        return window.keycloak.token;
    }
    return localStorage.getItem("access_token") || sessionStorage.getItem("access_token");
}

function getTypeClass(type) {
    switch (type) {
        case "CONTRACT": return "contract";
        case "PROPOSAL": return "proposal";
        case "REVIEW": return "review";
        case "APPROVAL": return "approval";
        case "REPORT": return "report";
        default: return "report";
    }
}

function getTypeIcon(type) {
    switch (type) {
        case "CONTRACT": return "📂";
        case "PROPOSAL": return "📝";
        case "REVIEW": return "✅";
        case "APPROVAL": return "✔";
        case "REPORT": return "📊";
        default: return "👤";
    }
}

function getStatusClass(status) {
    switch (status) {
        case "PENDING": return "pending";
        case "IN_PROGRESS": return "pending";
        case "DRAFT": return "draft";
        case "APPROVED": return "approved";
        case "CHANGES_REQUESTED": return "changes";
        case "REJECTED": return "rejected";
        case "SUBMITTED": return "submitted";
        default: return "draft";
    }
}

function getStatusIcon(status) {
    switch (status) {
        case "PENDING": return "⏳";
        case "IN_PROGRESS": return "🔄";
        case "DRAFT": return "📝";
        case "APPROVED": return "✅";
        case "CHANGES_REQUESTED": return "🔁";
        case "REJECTED": return "❌";
        case "SUBMITTED": return "📤";
        default: return "👤";
    }
}

function getPriorityClass(priority) {
    switch (priority) {
        case "HIGH": return "high";
        case "MEDIUM": return "medium";
        case "LOW": return "low";
        default: return "medium";
    }
}

function formatEnum(value) {
    if (!value) {
        return "-";
    }
    return String(value)
        .replaceAll("_", " ")
        .toLowerCase()
        .replace(/\b\w/g, function (char) {
            return char.toUpperCase();
        });
}

function formatDate(value) {
    if (!value) {
        return "-";
    }
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
        return value;
    }
    return new Intl.DateTimeFormat("en-GB", {
        day: "2-digit",
        month: "short",
        year: "numeric"
    }).format(date);
}

function formatMoney(value, currency) {
    const numericValue = Number(value) || 0;
    if (numericValue === 0) {
        return "-";
    }
    return new Intl.NumberFormat("en-DE", {
        style: "currency",
        currency: currency || "EUR",
        maximumFractionDigits: 0
    }).format(numericValue);
}

function formatCompactMoney(value, currency) {
    const numericValue = Number(value) || 0;
    return new Intl.NumberFormat("en-DE", {
        style: "currency",
        currency: currency || "EUR",
        notation: "compact",
        maximumFractionDigits: 1
    }).format(numericValue);
}

function escapeHtml(value) {
    if (value === null || value === undefined) {
        return "";
    }
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

function showLoading(isLoading) {
    document.getElementById("loadingIndicator").style.display = isLoading ? "flex" : "none";
}

function showToast(message) {
    const toast = document.getElementById("toast");
    toast.textContent = message;
    toast.style.display = "block";
    window.clearTimeout(showToast.timeoutId);
    showToast.timeoutId = window.setTimeout(function () {
        toast.style.display = "none";
    }, 2600);
}