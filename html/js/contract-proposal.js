'use strict';
const API_BASE_URL = "http://localhost:8080";
const PROPOSALS_API_URL = API_BASE_URL + "/api/proposals";

const PROPOSAL_WORKSPACE_URL = "ProposalWorkspace.html";
const PROPOSAL_REVIEW_URL = "ProposalReview.html";
const SUBMISSION_DETAILS_URL = "SubmissionDetails.html";
const AWARD_DETAILS_URL = "AwardDetails.html";
const CONTRACTS_URL = "Contracts.html";
const AUDIT_TRAIL_URL = "ProposalAuditTrail.html";

let proposals = [];
let filteredProposals = [];
let selectedProposal = null;

document.addEventListener("DOMContentLoaded", function () {
    bindEvents();
    loadDashboard();
    loadProposals();
});

function bindEvents() {
    document.getElementById("sidebarToggle").addEventListener("click", toggleSidebar);
    document.getElementById("proposalSearchInput").addEventListener("input", applyFilters);
    document.getElementById("globalSearchInput").addEventListener("input", syncGlobalSearch);
    document.getElementById("statusFilter").addEventListener("change", applyFilters);
    document.getElementById("stageFilter").addEventListener("change", applyFilters);

    document.getElementById("openProposalButton").addEventListener("click", function () {
        if (!selectedProposal) {
            return;
        }

        openProposal(selectedProposal);
    });

    document.getElementById("viewContractButton").addEventListener("click", function () {
        if (!selectedProposal) {
            return;
        }

        window.location.href = CONTRACTS_URL + "?contractId=" + encodeURIComponent(selectedProposal.contractId);
    });

    document.getElementById("auditTrailButton").addEventListener("click", function () {
        if (!selectedProposal) {
            return;
        }

        window.location.href = AUDIT_TRAIL_URL + "?proposalId=" + encodeURIComponent(selectedProposal.id);
    });

    document.getElementById("downloadButton").addEventListener("click", function () {
        if (!selectedProposal) {
            return;
        }

        downloadProposal(selectedProposal);
    });
}

function toggleSidebar() {
    const sidebar = document.getElementById("sidebar");
    const collapseIcon = document.getElementById("collapseIcon");

    sidebar.classList.toggle("collapsed");
    collapseIcon.textContent = sidebar.classList.contains("collapsed") ? "⇥" : "⇤";
}

function syncGlobalSearch(event) {
    document.getElementById("proposalSearchInput").value = event.target.value;
    applyFilters();
}

async function loadDashboard() {

    try {

        const token = getAccessToken();

        const response = await fetch(
            API_BASE_URL + "/api/proposals/dashboard",
            {
                headers: {
                    "Authorization": "Bearer " + token
                }
            }
        );

        const result = await response.json();

        const dashboard = result.data;

        document.getElementById("totalProposalsStat").textContent =
            dashboard.totalProposalCount;

        document.getElementById("activeValueStat").textContent =
            formatCompactMoney(
                dashboard.activeProposalValue,
                "EUR"
            );

        document.getElementById("wonValueStat").textContent =
            formatCompactMoney(
                dashboard.wonProposalValue,
                "EUR"
            );

        document.getElementById("draftCount").textContent =
            dashboard.draftCount;

        document.getElementById("reviewCount").textContent =
            dashboard.inReviewCount;

        document.getElementById("submittedCount").textContent =
            dashboard.submittedCount;

        document.getElementById("wonCount").textContent =
            dashboard.wonCount;

    } catch (error) {

        console.error(error);

    }
}

async function loadProposals() {

    showLoading(true);

    try {

        const token = getAccessToken();

        const response = await fetch(
            API_BASE_URL + "/api/proposals/search",
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": "Bearer " + token
                },
                body: JSON.stringify({
                    searchText: "",
                    status: null,
                    page: 0,
                    size: 20,
                    sortBy: "createdAt",
                    sortDirection: "DESC"
                })
            }
        );

        const result = await response.json();

        proposals = result.data.content || [];

        filteredProposals = [...proposals];

        renderProposals();

    } catch(error) {

        console.error(error);
        showToast("Unable to load proposals.");

    } finally {

        showLoading(false);

    }
}

function refreshProposals() {
    selectedProposal = null;
    resetSummary();
    loadProposals();
}

function sortLatestToOld(items) {
    return [...items].sort(function (a, b) {
        return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime();
    });
}

function applyFilters() {
    const searchValue = document.getElementById("proposalSearchInput").value.trim().toLowerCase();
    const statusValue = document.getElementById("statusFilter").value;
    const stageValue = document.getElementById("stageFilter").value;

    filteredProposals = proposals.filter(function (proposal) {
        const searchableText = [
            proposal.proposalNumber,
            proposal.name,
            proposal.contractName,
            proposal.status,
            proposal.stage,
            proposal.ownerName
        ].join(" ").toLowerCase();

        const matchesSearch = !searchValue || searchableText.includes(searchValue);
        const matchesStatus = statusValue === "ALL" || proposal.status === statusValue;
        const matchesStage = stageValue === "ALL" || proposal.stage === stageValue;

        return matchesSearch && matchesStatus && matchesStage;
    });

    renderProposals();
}

function renderProposals() {
    const tbody = document.getElementById("proposalsTableBody");
    const emptyState = document.getElementById("emptyState");

    tbody.innerHTML = "";

    if (filteredProposals.length === 0) {
        emptyState.style.display = "block";
        return;
    }

    emptyState.style.display = "none";

    filteredProposals.forEach(function (proposal) {
        const tr = document.createElement("tr");
        tr.dataset.proposalId = proposal.id;

        if (selectedProposal && selectedProposal.id === proposal.id) {
            tr.classList.add("selected");
        }

        tr.innerHTML = `
        <td>
            <div class="proposal-name">
                ${escapeHtml(proposal.title)}
            </div>

            <div class="proposal-meta">
                ${escapeHtml(proposal.proposalNumber)}
            </div>
        </td>

        <td>
            <div class="contract-ref">
                ${escapeHtml(proposal.contractDocumentName)}
            </div>

            <div class="proposal-meta">
                Contract ID ${proposal.contractDocumentId}
            </div>
        </td>

        <td>
            <span class="pill ${getStatusClass(proposal.status)}">
                ${formatEnum(proposal.status)}
            </span>
        </td>

        <td>
            <span class="stage-text">
                ${getStageFromStatus(proposal.status)}
            </span>
        </td>

        <td>
            <span class="due-date">
                ${formatDate(proposal.submissionDate)}
            </span>
        </td>

        <td>
            <span class="valuation">
                ${formatMoney(proposal.proposalValue,"EUR")}
            </span>
        </td>
        `;

        tr.addEventListener("click", function () {
            handleProposalClick(proposal);
        });

        tbody.appendChild(tr);
    });
}

async function handleProposalClick(proposal) {

    try {

        const token = getAccessToken();

        const response = await fetch(
            API_BASE_URL +
            "/api/proposals/" +
            proposal.id +
            "/history",
            {
                headers:{
                    "Authorization":"Bearer " + token
                }
            }
        );

        const result = await response.json();

        selectedProposal = result.data.proposal;

        renderProposalDetail(
            result.data.proposal,
            result.data.history
        );

        renderProposals();

    } catch(error) {

        console.error(error);
        showToast("Unable to load proposal");

    }
}

function renderProposalDetail(
    proposal,
    history
) {

    document.getElementById(
        "summaryPlaceholder"
    ).style.display = "none";

    document.getElementById(
        "summaryContent"
    ).style.display = "block";

    document.getElementById(
        "summaryTitle"
    ).textContent =
        proposal.title;

    document.getElementById(
        "summarySubtitle"
    ).textContent =
        proposal.proposalNumber;

    document.getElementById(
        "detailStatus"
    ).textContent =
        proposal.status;

    document.getElementById(
        "detailStage"
    ).textContent =
        getStageFromStatus(
            proposal.status
        );

    document.getElementById(
        "detailOwner"
    ).textContent =
        proposal.createdBy;

    document.getElementById(
        "detailDueDate"
    ).textContent =
        formatDate(
            proposal.submissionDate
        );

    document.getElementById(
        "detailValue"
    ).textContent =
        formatMoney(
            proposal.proposalValue,
            "EUR"
        );

    document.getElementById(
        "detailLots"
    ).textContent =
        proposal.selectedLots +
        " / " +
        proposal.totalLots;

    renderHistoryTimeline(history);
}

function resetSummary() {
    document.getElementById("summaryPlaceholder").style.display = "flex";
    document.getElementById("summaryContent").style.display = "none";
}

function renderHistoryTimeline(
    history
) {

    const timeline =
        document.getElementById(
            "timelineList"
        );

    timeline.innerHTML = "";

    history.forEach(item => {

        const row =
            document.createElement("div");

        row.className =
            "timeline-item";

        row.innerHTML = `
            <div class="timeline-icon">
                ✅
            </div>

            <div>
                <div class="timeline-title">
                    ${item.action}
                </div>

                <div class="timeline-text">
                    ${item.comment}
                    <br>
                    ${item.performedBy}
                    <br>
                    ${formatDateTime(item.performedAt)}
                </div>
            </div>
        `;

        timeline.appendChild(row);

    });
}

function openProposal(proposal) {
    const targetUrl = getProposalTargetUrl(proposal);
    window.location.href = targetUrl + "?proposalId=" + encodeURIComponent(proposal.id);
}

function getProposalTargetUrl(proposal) {
    switch (proposal.status) {
        case "DRAFT":
            return PROPOSAL_WORKSPACE_URL;
        case "UNDER_REVIEW":
            return PROPOSAL_REVIEW_URL;
        case "APPROVED":
            return PROPOSAL_WORKSPACE_URL;
        case "SUBMITTED":
            return SUBMISSION_DETAILS_URL;
        case "WON":
            return AWARD_DETAILS_URL;
        case "LOST":
            return AWARD_DETAILS_URL;
        case "WITHDRAWN":
            return SUBMISSION_DETAILS_URL;
        default:
            return PROPOSAL_WORKSPACE_URL;
    }
}

function downloadProposal(proposal) {
    if (!proposal.documentUrl) {
        showToast("Proposal document is not available.");
        return;
    }

    const token = getAccessToken();

    if (proposal.documentUrl.startsWith("http")) {
        window.open(proposal.documentUrl, "_blank", "noopener,noreferrer");
        return;
    }

    const url = API_BASE_URL + proposal.documentUrl;

    fetch(url, {
        method: "GET",
        headers: {
            "Authorization": token ? "Bearer " + token : ""
        }
    })
        .then(function (response) {
            if (!response.ok) {
                throw new Error("Download failed with status " + response.status);
            }

            return response.blob();
        })
        .then(function (blob) {
            const temporaryUrl = window.URL.createObjectURL(blob);
            const link = document.createElement("a");
            link.href = temporaryUrl;
            link.download = proposal.proposalNumber + ".pdf";
            document.body.appendChild(link);
            link.click();
            link.remove();
            window.URL.revokeObjectURL(temporaryUrl);
        })
        .catch(function (error) {
            console.error(error);
            showToast("Unable to download proposal document.");
        });
}

function getAccessToken() {
    if (window.keycloak && window.keycloak.token) {
        return window.keycloak.token;
    }

    return localStorage.getItem("access_token") || sessionStorage.getItem("access_token");
}

function getStatusClass(status) {
    switch (status) {
        case "DRAFT":
            return "draft";
        case "UNDER_REVIEW":
            return "review";
        case "APPROVED":
            return "approved";
        case "SUBMITTED":
            return "submitted";
        case "WON":
            return "won";
        case "LOST":
            return "lost";
        case "WITHDRAWN":
            return "withdrawn";
        default:
            return "draft";
    }
}

function getStatusIcon(status) {
    switch (status) {
        case "DRAFT":
            return "📝";
        case "UNDER_REVIEW":
            return "✅";
        case "APPROVED":
            return "✔";
        case "SUBMITTED":
            return "📤";
        case "WON":
            return "🏆";
        case "LOST":
            return "❌";
        case "WITHDRAWN":
            return "↩";
        default:
            return "📝";
    }
}

function getDueDateClass(value) {
    if (!value) {
        return "normal";
    }

    const today = new Date();
    const dueDate = new Date(value);
    const differenceInMs = dueDate.getTime() - today.getTime();
    const differenceInDays = Math.ceil(differenceInMs / (1000 * 60 * 60 * 24));

    if (differenceInDays <= 2) {
        return "urgent";
    }

    if (differenceInDays <= 5) {
        return "soon";
    }

    return "normal";
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

function getStageFromStatus(status) {

    switch(status) {

        case "DRAFT":
            return "Drafting";

        case "ACTIVE":
            return "Preparation";

        case "IN_REVIEW":
            return "Internal Review";

        case "SUBMITTED":
            return "Submitted";

        case "WON":
            return "Awarded";

        case "LOST":
            return "Closed";

        case "NOT_BIDDED":
            return "Not Bidded";

        case "WITHDRAWN":
            return "Withdrawn";

        default:
            return "-";
    }
}