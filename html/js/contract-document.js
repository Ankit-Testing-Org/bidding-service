'use strict';

const API_BASE_URL = "http://localhost:8080";
const CONTRACTS_DASHBOARD_URL =
    API_BASE_URL + "/api/contracts/dashboard";

const CONTRACT_SEARCH_URL =
    API_BASE_URL + "/api/contracts/search";
const ANALYSIS_PAGE_URL = "ContractDocumentAnalysis.html";

const CURRENT_USER_ID = "ankit";

let contracts = [];
let filteredContracts = [];
let selectedContract = null;

document.addEventListener("DOMContentLoaded", function () {
    bindEvents();
    loadContracts();
});

function bindEvents() {
    document.getElementById("sidebarToggle").addEventListener("click", toggleSidebar);
    document.getElementById("contractSearchInput").addEventListener("input", applyFilters);
    document.getElementById("globalSearchInput").addEventListener("input", syncGlobalSearch);
    document.getElementById("statusFilter").addEventListener("change", applyFilters);
    document.getElementById("assignmentFilter").addEventListener("change", applyFilters);

    document.getElementById("openPdfButton").addEventListener("click", function () {
        if (!selectedContract) {
            return;
        }

        openPdfInNewTab(selectedContract);
    });

    document.getElementById("analysisButton").addEventListener("click", function () {
        if (!selectedContract) {
            return;
        }

        goToAnalysis(selectedContract);
    });
}

function toggleSidebar() {
    const sidebar = document.getElementById("sidebar");
    const collapseIcon = document.getElementById("collapseIcon");

    sidebar.classList.toggle("collapsed");
    collapseIcon.textContent = sidebar.classList.contains("collapsed") ? "⇥" : "⇤";
}

function syncGlobalSearch(event) {
    document.getElementById("contractSearchInput").value = event.target.value;
    applyFilters();
}

async function loadContracts() {

    showLoading(true);

    try {

        const response = await fetch(
            CONTRACTS_DASHBOARD_URL,
            {
                method: "GET",
                headers: buildAuthHeaders()
            }
        );

        const apiResponse = await response.json();

        if (!apiResponse.success) {
            throw new Error(apiResponse.message);
        }

        const dashboard = apiResponse.data;

        contracts = dashboard.contracts || [];

        updateDashboardSummary(
            dashboard.summary
        );

        filteredContracts = [...contracts];

        renderContracts();

        showToast(apiResponse.message);

    } catch (error) {

        console.error(error);

        showToast(
            "Failed to load contracts"
        );

    } finally {

        showLoading(false);
    }
}

function updateDashboardSummary(summary) {

    document.getElementById(
        "totalContractsStat"
    ).textContent =
        summary.totalContracts;

    document.getElementById(
        "assignedToMeStat"
    ).textContent =
        summary.assignedToMe;

    document.getElementById(
        "totalValuationStat"
    ).textContent =
        formatCompactMoney(
            summary.totalValuation,
            "EUR"
        );

    document.getElementById(
        "uploadedCount"
    ).textContent =
        summary.uploaded;

    document.getElementById(
        "analysisCount"
    ).textContent =
        summary.underAnalysis;

    document.getElementById(
        "assignedCount"
    ).textContent =
        summary.assignedToMe;

    document.getElementById(
        "unassignedCount"
    ).textContent =
        summary.unassigned;
}

function refreshContracts() {
    selectedContract = null;
    resetViewer();
    loadContracts();
}


async function applyFilters() {

    try {

        const request = {

            searchText:
            document
                .getElementById(
                    "contractSearchInput"
                )
                .value,

            status:
            document
                .getElementById(
                    "statusFilter"
                )
                .value,

            assignmentStatus:
            document
                .getElementById(
                    "assignmentFilter"
                )
                .value,

            page: 0,

            size: 20,

            sortBy: "uploadedAt",

            sortDirection: "DESC"
        };

        const response = await fetch(
            CONTRACT_SEARCH_URL,
            {
                method: "POST",
                headers: {
                    ...buildAuthHeaders(),
                    "Content-Type":
                        "application/json"
                },
                body: JSON.stringify(request)
            }
        );

        const apiResponse =
            await response.json();

        if (!apiResponse.success) {
            throw new Error(
                apiResponse.message
            );
        }

        filteredContracts =
            apiResponse.data.content;

        renderContracts();

    } catch (error) {

        console.error(error);

        showToast(
            "Failed to search contracts"
        );
    }
}

function renderContracts() {
    const tbody = document.getElementById("contractsTableBody");
    const emptyState = document.getElementById("emptyState");

    tbody.innerHTML = "";

    if (filteredContracts.length === 0) {
        emptyState.style.display = "block";
        return;
    }

    emptyState.style.display = "none";

    filteredContracts.forEach(function (contract) {
        const tr = document.createElement("tr");
        tr.dataset.contractId = contract.contractId;

        if (selectedContract && selectedContract.id === contract.contractId) {
            tr.classList.add("selected");
        }

        tr.innerHTML = `
          <td>
            <div class="contract-name">${escapeHtml(contract.contractName)}</div>
            <div class="contract-meta">
              Uploaded ${formatDateTime(contract.uploadedAt)} · ID ${contract.contractId}
            </div>
          </td>

          <td>
            <span class="pill ${getStatusClass(contract.status)}">
              ${getStatusIcon(contract.status)}
              ${formatEnum(contract.status)}
            </span>
          </td>

          <td>
            <span class="pill ${getAssignmentClass(contract.assignmentStatus)}">
              ${getAssignmentIcon(contract.assignmentStatus)}
              ${formatAssignment(contract)}
            </span>
          </td>

          <td>
            <span class="stage-text">${formatEnum(contract.stage)}</span>
          </td>

          <td>
            <span class="valuation">${formatMoney(contract.valuation, contract.currency)}</span>
          </td>
        `;

        tr.addEventListener("click", function () {
            handleContractClick(contract);
        });

        tbody.appendChild(tr);
    });
}

async function handleContractClick(
    contract
) {

    selectedContract = contract;

    renderContracts();

    const response = await fetch(
        API_BASE_URL +
        "/api/contracts/" +
        contract.contractId +
        "/analysis-access",
        {
            headers: buildAuthHeaders()
        }
    );

    const apiResponse =
        await response.json();

    if (
        apiResponse.success &&
        apiResponse.data.canOpenAnalysis
    ) {

        window.location.href =
            "ContractDocumentAnalysis.html?contractId="
            + contract.contractId;

        return;
    }

    loadContractInViewer(contract);
}

function isAssignedToCurrentUser(contract) {
    if (contract.assignmentStatus === "ASSIGNED_TO_ME") {
        return true;
    }

    return contract.assignedTo && contract.assignedTo === CURRENT_USER_ID;
}

function goToAnalysis(contract) {
    const targetUrl = ANALYSIS_PAGE_URL + "?contractId=" + encodeURIComponent(contract.contractId);
    window.location.href = targetUrl;
}

async function loadContractInViewer(
    contract
) {

    const response = await fetch(
        API_BASE_URL +
        "/api/contracts/" +
        contract.contractId,
        {
            headers: buildAuthHeaders()
        }
    );

    const apiResponse =
        await response.json();

    const details =
        apiResponse.data;

    document.getElementById(
        "viewerContractTitle"
    ).textContent =
        details.contractName;

    document.getElementById(
        "viewerContractSubtitle"
    ).textContent =
        details.clientName || "";

    document.getElementById(
        "detailStatus"
    ).textContent =
        details.status;

    document.getElementById(
        "detailOwner"
    ).textContent =
        details.assignedToName || "N/A";

    document.getElementById(
        "detailUploadDate"
    ).textContent =
        formatDateTime(
            details.uploadedAt
        );

    document.getElementById(
        "pdfFrame"
    ).src =
        API_BASE_URL +
        "/api/contracts/" +
        details.contractId +
        "/pdf";

    document.getElementById(
        "pdfFrame"
    ).style.display =
        "block";

    document.getElementById(
        "viewerPlaceholder"
    ).style.display =
        "none";
}

function resetViewer() {
    const pdfFrame = document.getElementById("pdfFrame");
    const placeholder = document.getElementById("viewerPlaceholder");

    document.getElementById("viewerContractTitle").textContent = "No contract selected";
    document.getElementById("viewerContractSubtitle").textContent = "Select any contract from the list.";

    document.getElementById("detailStatus").textContent = "-";
    document.getElementById("detailOwner").textContent = "-";
    document.getElementById("detailUploadDate").textContent = "-";

    document.getElementById("openPdfButton").disabled = true;
    document.getElementById("analysisButton").disabled = true;

    pdfFrame.removeAttribute("src");
    pdfFrame.style.display = "none";
    placeholder.style.display = "flex";
}


function openPdfInNewTab(
    contract
) {

    window.open(
        API_BASE_URL +
        "/api/contracts/" +
        contract.contractId +
        "/pdf",
        "_blank"
    );
}


function getStatusClass(status) {
    switch (status) {
        case "UPLOADED":
            return "uploaded";
        case "ANALYSIS_IN_PROGRESS":
            return "analysis";
        case "REVIEW_PENDING":
            return "review";
        case "APPROVED":
            return "approved";
        case "REJECTED":
            return "rejected";
        default:
            return "uploaded";
    }
}

function getStatusIcon(status) {
    switch (status) {
        case "UPLOADED":
            return "⬆️";
        case "ANALYSIS_IN_PROGRESS":
            return "🤖";
        case "REVIEW_PENDING":
            return "🕵️";
        case "APPROVED":
            return "✅";
        case "REJECTED":
            return "❌";
        default:
            return "📄";
    }
}

function getAssignmentClass(assignationStatus) {
    switch (assignationStatus) {
        case "ASSIGNED_TO_ME":
            return "assigned-me";
        case "ASSIGNED_TO_OTHER":
            return "assigned-other";
        case "UNASSIGNED":
            return "unassigned";
        default:
            return "unassigned";
    }
}

function getAssignmentIcon(assignationStatus) {
    switch (assignationStatus) {
        case "ASSIGNED_TO_ME":
            return "👤";
        case "ASSIGNED_TO_OTHER":
            return "👥";
        case "UNASSIGNED":
            return "○";
        default:
            return "○";
    }
}

function formatAssignment(contract) {
    if (contract.assignmentStatus === "ASSIGNED_TO_ME") {
        return "Assigned to Me";
    }

    if (contract.assignmentStatus === "ASSIGNED_TO_OTHER") {
        return contract.assignedToName ? "Assigned to " + contract.assignedToName : "Assigned to Other";
    }

    return "Unassigned";
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

function formatDateTime(value) {
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
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit"
    }).format(date);
}

function formatMoney(value, currency) {
    const numericValue = Number(value) || 0;

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