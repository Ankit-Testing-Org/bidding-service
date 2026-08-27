'use strict';

const API_BASE_URL = "http://localhost:8080/api";

let lastDashboardResponse = null;
let selectedContractFile = null;

document.addEventListener("DOMContentLoaded", function () {
    loadOverviewDashboard();
});

function getAccessToken() {
    if (window.keycloak && window.keycloak.token) {
        return window.keycloak.token;
    }

    const possibleTokenKeys = [
        "access_token",
        "accessToken",
        "kc_token",
        "smartbid_access_token"
    ];

    for (const key of possibleTokenKeys) {
        const localToken = localStorage.getItem(key);

        if (localToken) {
            return localToken;
        }

        const sessionToken = sessionStorage.getItem(key);

        if (sessionToken) {
            return sessionToken;
        }
    }

    return "";
}

function buildAuthHeaders(extraHeaders) {
    const token = getAccessToken();
    const headers = extraHeaders || {};

    if (token) {
        headers["Authorization"] = "Bearer " + token;
    }

    return headers;
}

async function loadOverviewDashboard() {
    showLoading(true);
    showError("");

    try {
        const response = await fetch(
            API_BASE_URL + "/dashboard/overview",
            {
                method: "GET",
                headers: buildAuthHeaders({
                    "Content-Type": "application/json"
                })
            }
        );

        if (!response.ok) {
            throw new Error("Overview API failed with status " + response.status);
        }

        const dashboard = await response.json();

        lastDashboardResponse = dashboard;

        populateOverviewDashboard(dashboard);
        showToast("Dashboard overview loaded");

    } catch (error) {
        console.error(error);
        showError("Unable to load dashboard overview. Check /api/dashboard/overview and authentication token.");
    } finally {
        showLoading(false);
    }
}

function populateOverviewDashboard(dashboard) {
    populateSummary(dashboard.summary || {});
    populatePipeline(dashboard.pipeline || {});
    populateValuation(dashboard.valuation || {});
    populateWorkload(dashboard.workload || {});
    populateDeadlines(dashboard.deadlines || []);
    populateActivities(dashboard.activities || []);
}

function populateSummary(summary) {
    setText("heroActiveValue", formatCurrency(summary.activeProposalValue));
    setText("heroClosedValue", formatCurrency(summary.closedProposalValue));
    setText("heroWonValue", formatCurrency(summary.wonProposalValue));

    setText("activeProposalCount", formatNumber(summary.activeProposalCount));
    setText("activeProposalValue", formatCurrency(summary.activeProposalValue));

    setText("closedProposalCount", formatNumber(summary.closedProposalCount));
    setText("closedProposalValue", formatCurrency(summary.closedProposalValue));

    setText("wonProposalCount", formatNumber(summary.wonProposalCount));
    setText("wonProposalValue", formatCurrency(summary.wonProposalValue));

    setText("lostProposalCount", formatNumber(summary.lostProposalCount));
    setText("lostProposalValue", formatCurrency(summary.lostProposalValue));

    setText("notBiddedProposalCount", formatNumber(summary.notBiddedProposalCount));
    setText("notBiddedProposalValue", formatCurrency(summary.notBiddedProposalValue));
}

function populatePipeline(pipeline) {
    const draft = toNumber(pipeline.draft);
    const submitted = toNumber(pipeline.submitted);
    const review = toNumber(pipeline.review);
    const awarded = toNumber(pipeline.awarded);
    const lost = toNumber(pipeline.lost);
    const notBidded = toNumber(pipeline.notBidded);

    const maxValue = Math.max(draft, submitted, review, awarded, lost, notBidded, 1);

    setText("draftCount", draft);
    setText("submittedCount", submitted);
    setText("reviewCount", review);
    setText("awardedCount", awarded);
    setText("pipelineLostCount", lost);
    setText("pipelineNotBiddedCount", notBidded);

    setBarWidth("draftBar", draft, maxValue);
    setBarWidth("submittedBar", submitted, maxValue);
    setBarWidth("reviewBar", review, maxValue);
    setBarWidth("awardedBar", awarded, maxValue);
    setBarWidth("lostBar", lost, maxValue);
    setBarWidth("notBiddedBar", notBidded, maxValue);
}

function populateValuation(valuation) {
    const activeValue = toNumber(valuation.activeValue);
    const wonValue = toNumber(valuation.wonValue);
    const lostValue = toNumber(valuation.lostValue);
    const notBiddedValue = toNumber(valuation.notBiddedValue);

    const total = activeValue + wonValue + lostValue + notBiddedValue;

    setText("donutCenterValue", formatCurrency(activeValue));
    setText("valuationActiveValue", formatCurrency(activeValue));
    setText("valuationWonValue", formatCurrency(wonValue));
    setText("valuationLostValue", formatCurrency(lostValue));
    setText("valuationNotBiddedValue", formatCurrency(notBiddedValue));

    updateDonut(activeValue, wonValue, lostValue, notBiddedValue, total);
}

function populateWorkload(workload) {
    const container = document.getElementById("workloadContainer");

    container.innerHTML = `
        <div class="workload-item">
          <div>
            <div class="workload-name">Legal Reviews</div>
            <div class="workload-count">${formatNumber(workload.legalReviews)} pending</div>
          </div>
          ⚖️
        </div>

        <div class="workload-item">
          <div>
            <div class="workload-name">Finance Reviews</div>
            <div class="workload-count">${formatNumber(workload.financeReviews)} pending</div>
          </div>
          💰
        </div>

        <div class="workload-item">
          <div>
            <div class="workload-name">Commercial Reviews</div>
            <div class="workload-count">${formatNumber(workload.commercialReviews)} pending</div>
          </div>
          📈
        </div>

        <div class="workload-item">
          <div>
            <div class="workload-name">Manager Approvals</div>
            <div class="workload-count">${formatNumber(workload.managerApprovals)} pending</div>
          </div>
          ✅
        </div>
      `;
}

function populateDeadlines(deadlines) {
    const container = document.getElementById("deadlinesContainer");

    if (!deadlines || deadlines.length === 0) {
        container.innerHTML = '<div class="empty-list">No upcoming deadlines found.</div>';
        return;
    }

    container.innerHTML = "";

    deadlines.forEach(function (deadline) {
        const priority = normalizePriority(deadline.priority);

        const item = document.createElement("div");
        item.className = "deadline-item";

        item.innerHTML = `
          <div>
            <div class="deadline-name">${escapeHtml(deadline.proposalName || "Proposal")}</div>
            <div class="deadline-date">${buildDeadlineText(deadline)}</div>
          </div>
          <span class="priority ${priority}">${priority}</span>
        `;

        container.appendChild(item);
    });
}

function populateActivities(activities) {
    const container = document.getElementById("activitiesContainer");

    if (!activities || activities.length === 0) {
        container.innerHTML = '<div class="empty-list">No recent activities found.</div>';
        return;
    }

    container.innerHTML = "";

    activities.forEach(function (activity) {
        const activityType = activity.activityType || "";
        const badgeClass = getActivityBadgeClass(activityType);
        const badgeText = getActivityBadgeText(activityType);

        const item = document.createElement("div");
        item.className = "activity-item";
        item.setAttribute("data-search-text", buildActivitySearchText(activity));

        item.innerHTML = `
          <div class="activity-left">
            <div class="activity-icon">${getActivityIcon(activityType)}</div>
            <div>
              <div class="activity-title">${escapeHtml(activity.title || "Activity")}</div>
              <div class="activity-desc">${escapeHtml(activity.description || "")}</div>
              <span class="badge ${badgeClass}">${escapeHtml(badgeText)}</span>
            </div>
          </div>
          <div class="activity-meta">${formatRelativeDate(activity.createdAt)}</div>
        `;

        container.appendChild(item);
    });
}

function filterActivities() {
    const input = document.getElementById("dashboardSearchInput");
    const query = input ? input.value.trim().toLowerCase() : "";
    const rows = document.querySelectorAll("#activitiesContainer .activity-item");

    rows.forEach(function (row) {
        const text = row.getAttribute("data-search-text") || "";
        row.style.display = !query || text.includes(query) ? "flex" : "none";
    });
}

function buildActivitySearchText(activity) {
    return [
        activity.title,
        activity.description,
        activity.createdBy,
        activity.referenceType,
        activity.activityType
    ]
        .filter(Boolean)
        .join(" ")
        .toLowerCase();
}

function updateDonut(activeValue, wonValue, lostValue, notBiddedValue, total) {
    const donut = document.getElementById("valuationDonut");

    if (!total || total <= 0) {
        donut.style.background = "conic-gradient(#e5e7eb 0deg 360deg)";
        return;
    }

    const activeDeg = Math.round((activeValue / total) * 360);
    const wonDeg = Math.round((wonValue / total) * 360);
    const lostDeg = Math.round((lostValue / total) * 360);
    const notBiddedDeg = Math.round((notBiddedValue / total) * 360);

    const d1 = activeDeg;
    const d2 = d1 + wonDeg;
    const d3 = d2 + lostDeg;
    const d4 = d3 + notBiddedDeg;

    donut.style.background =
        "conic-gradient(" +
        "#7c3aed 0deg " + d1 + "deg, " +
        "#059669 " + d1 + "deg " + d2 + "deg, " +
        "#ea580c " + d2 + "deg " + d3 + "deg, " +
        "#64748b " + d3 + "deg " + d4 + "deg, " +
        "#e5e7eb " + d4 + "deg 360deg" +
        ")";
}

function setBarWidth(elementId, value, maxValue) {
    const element = document.getElementById(elementId);

    if (!element) {
        return;
    }

    const width = value <= 0 ? 0 : Math.max(6, Math.round((value / maxValue) * 100));
    element.style.width = width + "%";
}

function buildDeadlineText(deadline) {
    const days = toNumber(deadline.daysRemaining);

    if (days < 0) {
        return "Overdue by " + Math.abs(days) + " days";
    }

    if (days === 0) {
        return "Due today";
    }

    if (days === 1) {
        return "Due tomorrow";
    }

    return "Due in " + days + " days";
}

function getActivityIcon(activityType) {
    const value = String(activityType || "").toUpperCase();

    if (value.includes("DRAFT")) {
        return "📝";
    }

    if (value.includes("SUBMITTED")) {
        return "📤";
    }

    if (value.includes("WON") || value.includes("AWARDED")) {
        return "🏆";
    }

    if (value.includes("LOST") || value.includes("REJECTED")) {
        return "❌";
    }

    if (value.includes("APPROVED")) {
        return "✅";
    }

    return "📌";
}

function getActivityBadgeClass(activityType) {
    const value = String(activityType || "").toUpperCase();

    if (value.includes("DRAFT")) {
        return "draft";
    }

    if (value.includes("SUBMITTED")) {
        return "submitted";
    }

    if (value.includes("WON") || value.includes("AWARDED") || value.includes("APPROVED")) {
        return "won";
    }

    if (value.includes("LOST") || value.includes("REJECTED")) {
        return "lost";
    }

    return "generic";
}

function getActivityBadgeText(activityType) {
    if (!activityType) {
        return "Activity";
    }

    return String(activityType)
        .replaceAll("_", " ")
        .toLowerCase()
        .replace(/\b\w/g, function (char) {
            return char.toUpperCase();
        });
}

function normalizePriority(priority) {
    const value = String(priority || "LOW").toLowerCase();

    if (value === "high") {
        return "high";
    }

    if (value === "medium") {
        return "medium";
    }

    return "low";
}

function formatRelativeDate(dateValue) {
    if (!dateValue) {
        return "-";
    }

    const date = new Date(dateValue);

    if (Number.isNaN(date.getTime())) {
        return "-";
    }

    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMinutes = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMinutes / 60);
    const diffDays = Math.floor(diffHours / 24);

    if (diffMinutes < 1) {
        return "Just now";
    }

    if (diffMinutes < 60) {
        return diffMinutes + " minutes ago";
    }

    if (diffHours < 24) {
        return diffHours + " hours ago";
    }

    if (diffDays === 1) {
        return "Yesterday";
    }

    if (diffDays < 7) {
        return diffDays + " days ago";
    }

    return date.toLocaleDateString("de-DE");
}

function formatCurrency(value) {
    return new Intl.NumberFormat(
        "de-DE",
        {
            style: "currency",
            currency: "EUR",
            maximumFractionDigits: 0
        }
    ).format(toNumber(value));
}

function formatNumber(value) {
    return new Intl.NumberFormat("de-DE").format(toNumber(value));
}

function toNumber(value) {
    if (value === null || value === undefined || value === "") {
        return 0;
    }

    const number = Number(value);

    if (Number.isNaN(number)) {
        return 0;
    }

    return number;
}

function setText(elementId, value) {
    const element = document.getElementById(elementId);

    if (element) {
        element.innerText = value === null || value === undefined ? "" : String(value);
    }
}

function showLoading(isLoading) {
    const loadingBanner = document.getElementById("loadingBanner");

    if (loadingBanner) {
        loadingBanner.style.display = isLoading ? "block" : "none";
    }
}

function showError(message) {
    const errorBanner = document.getElementById("errorBanner");

    if (!errorBanner) {
        return;
    }

    if (!message) {
        errorBanner.style.display = "none";
        errorBanner.innerText = "";
        return;
    }

    errorBanner.innerText = message;
    errorBanner.style.display = "block";
}

function showToast(message) {
    const toast = document.getElementById("toast");

    if (!toast) {
        return;
    }

    toast.innerText = message;
    toast.style.display = "block";

    setTimeout(function () {
        toast.style.display = "none";
    }, 2600);
}

function escapeHtml(value) {
    return String(value || "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

function navigateTo(url) {
    window.location.href = url;
}

const dropZone =
    document.getElementById("dropZone");

const fileInput =
    document.getElementById("contractFile");

const selectedFileInfo =
    document.getElementById("selectedFileInfo");

function triggerFileUpload() {
    document.getElementById("contractFile").click();
}

function uploadSelectedFile() {

    const input = document.getElementById("contractFile");
    if (!input.files.length) {
        return;
    }
    selectedContractFile = input.files[0];
    updateSelectedFile(selectedContractFile);
    uploadContract(selectedContractFile);
}

fileInput.addEventListener(
    "change",
    function () {

        if (this.files.length > 0) {

            updateSelectedFile(
                this.files[0]
            );
        }
    }
);

dropZone.addEventListener(
    "drop",
    async event => {

        event.preventDefault();

        dropZone.classList.remove(
            "dragover"
        );

        const file =
            event.dataTransfer.files[0];

        if (!file) {
            return;
        }

        selectedContractFile = file;

        updateSelectedFile(file);

        await uploadContract(file);
    }
);

function updateSelectedFile(file) {

    document.getElementById(
        "selectedFileInfo"
    ).innerHTML = `
        <strong>${file.name}</strong>
        <br>
        ${(file.size / 1024 / 1024).toFixed(2)} MB
    `;
}

async function uploadContract(file) {

    showToast(
        "Uploading contract..."
    );

    const formData = new FormData();

    formData.append(
        "file",
        file
    );

    try {

        const response = await fetch(
            "/api/contracts/upload",
            {
                method: "POST",
                body: formData
            }
        );

        const result =
            await response.json();

        if (!result.success) {

            showError(
                result.message
            );

            return;
        }

        showToast(
            "Contract uploaded successfully"
        );

        addProcessingQueueItem(
            result.data
        );

    } catch (error) {

        console.error(error);

        showError(
            "Failed to upload contract"
        );
    }
}

function addProcessingQueueItem(contract) {

    const container =
        document.getElementById(
            "processingQueueContainer"
        );

    const empty =
        container.querySelector(
            ".empty-list"
        );

    if (empty) {
        empty.remove();
    }

    const html = `
        <div class="processing-item"
             id="contract-${contract.id}">

            <div class="processing-header">

                <div class="processing-name">
                    ${contract.originalFileName}
                </div>

                <span class="status-badge processing">
                    Upload Complete
                </span>

            </div>

            <div class="progress-bar">

                <div
                    class="progress-fill"
                    style="width:10%">
                </div>

            </div>

            <div class="processing-footer">

                <span>
                    Contract ID:
                    ${contract.id}
                </span>

                <span>
                    Uploaded
                </span>

            </div>

        </div>
    `;

    container.insertAdjacentHTML(
        "afterbegin",
        html
    );

    updateQueueCounter();
}

function updateQueueCounter() {

    const total =
        document.querySelectorAll(
            ".processing-item"
        ).length;

    document.getElementById(
        "processingCount"
    ).innerText =
        `${total} Active`;
}

async function loadProcessingQueue() {

    try {

        const response =
            await fetch(
                "/api/dashboard/processing-queue"
            );

        const result =
            await response.json();

        if (!result.success) {
            return;
        }

        renderProcessingQueue(
            result.data
        );

    } catch (error) {

        console.error(
            "Failed to load processing queue",
            error
        );
    }
}
let processingPollInterval = null;

document.addEventListener(
    "DOMContentLoaded",
    () => {

        loadProcessingQueue();

        startProcessingQueuePolling();
    }
);

function startProcessingQueuePolling() {

    if (processingPollInterval) {

        clearInterval(
            processingPollInterval
        );
    }

    processingPollInterval =
        setInterval(
            loadProcessingQueue,
            5000
        );
}

function renderProcessingQueue(
    contracts
) {

    const container =
        document.getElementById(
            "processingQueueContainer"
        );

    document.getElementById(
        "processingCount"
    ).innerText =
        `${contracts.length} Active`;

    if (!contracts.length) {

        container.innerHTML = `
            <div class="empty-list">
                No contracts currently being processed
            </div>
        `;

        return;
    }

    container.innerHTML =
        contracts.map(
            createProcessingItem
        ).join("");
}