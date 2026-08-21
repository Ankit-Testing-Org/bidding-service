'use strict';

const API_BASE_URL = "http://localhost:8080";
const APPROVALS_API_URL = API_BASE_URL + "/api/approvals";

const DASHBOARD_URL = APPROVALS_API_URL + "/dashboard";
const SEARCH_URL = APPROVALS_API_URL + "/search";
const AUDIT_TRAIL_URL = "ApprovalAuditTrail.html";

const DEFAULT_PAGE = 0;
const DEFAULT_SIZE = 20;
const DEFAULT_SORT_BY = "createdAt";
const DEFAULT_SORT_DIRECTION = "DESC";

let approvals = [];
let selectedApproval = null;

let currentPage = DEFAULT_PAGE;
let totalPages = 0;
let totalElements = 0;

let searchTimerId = null;
let searchRequestSequence = 0;
let detailRequestSequence = 0;

document.addEventListener("DOMContentLoaded", function () {
    bindEvents();
    initializePage();
});

async function initializePage() {
    resetApprovalPanel();
    renderActivities([]);

    await Promise.all([
        loadDashboard(),
        searchApprovals(DEFAULT_PAGE)
    ]);
}

function bindEvents() {
    const approvalSearchInput =
        document.getElementById("approvalSearchInput");

    const globalSearchInput =
        document.getElementById("globalSearchInput");

    approvalSearchInput.addEventListener("input", function () {
        globalSearchInput.value = approvalSearchInput.value;
        scheduleSearch();
    });

    globalSearchInput.addEventListener("input", function () {
        approvalSearchInput.value = globalSearchInput.value;
        scheduleSearch();
    });

    document
        .getElementById("statusFilter")
        .addEventListener("change", function () {
            searchApprovals(DEFAULT_PAGE);
        });

    document
        .getElementById("stageFilter")
        .addEventListener("change", function () {
            searchApprovals(DEFAULT_PAGE);
        });

    document
        .getElementById("openProposalButton")
        .addEventListener("click", openSelectedProposal);

    document
        .getElementById("openReviewButton")
        .addEventListener("click", openSelectedReview);

    document
        .getElementById("auditTrailButton")
        .addEventListener("click", openSelectedAuditTrail);

    document
        .getElementById("downloadButton")
        .addEventListener("click", downloadSelectedProposal);
}

function scheduleSearch() {
    window.clearTimeout(searchTimerId);

    searchTimerId = window.setTimeout(function () {
        searchApprovals(DEFAULT_PAGE);
    }, 350);
}

function buildSearchRequest(page) {
    const searchText =
        document.getElementById("approvalSearchInput").value.trim();

    const status =
        document.getElementById("statusFilter").value;

    const approvalStage =
        document.getElementById("stageFilter").value;

    return {
        searchText: searchText || null,
        status: status === "ALL" ? null : status,
        approvalStage:
            approvalStage === "ALL" ? null : approvalStage,
        page: page,
        size: DEFAULT_SIZE,
        sortBy: DEFAULT_SORT_BY,
        sortDirection: DEFAULT_SORT_DIRECTION
    };
}

async function loadDashboard() {
    try {
        const responseBody = await apiRequest(
            DASHBOARD_URL,
            {
                method: "GET"
            }
        );

        renderDashboard(responseBody.data);
    } catch (error) {
        console.error("Unable to load approval dashboard:", error);
        resetDashboard();
        showToast(error.message);
    }
}

function renderDashboard(dashboard) {
    dashboard = dashboard || {};

    setText(
        "totalApprovalsStat",
        dashboard.totalApprovalCount ?? 0
    );

    setText(
        "pendingApprovalsStat",
        dashboard.pendingCount ?? 0
    );

    setText(
        "approvedValueStat",
        formatCompactMoney(
            dashboard.approvedValue,
            dashboard.currency || "EUR"
        )
    );

    setText("pendingCount", dashboard.pendingCount ?? 0);
    setText("approvedCount", dashboard.approvedCount ?? 0);
    setText("rejectedCount", dashboard.rejectedCount ?? 0);
    setText("delegatedCount", dashboard.delegatedCount ?? 0);
}

function resetDashboard() {
    setText("totalApprovalsStat", 0);
    setText("pendingApprovalsStat", 0);
    setText("approvedValueStat", formatCompactMoney(0, "EUR"));
    setText("pendingCount", 0);
    setText("approvedCount", 0);
    setText("rejectedCount", 0);
    setText("delegatedCount", 0);
}

async function searchApprovals(page) {
    const requestSequence = ++searchRequestSequence;

    showLoading(true);

    try {
        const request = buildSearchRequest(page);

        const responseBody = await apiRequest(
            SEARCH_URL,
            {
                method: "POST",
                body: JSON.stringify(request)
            }
        );

        if (requestSequence !== searchRequestSequence) {
            return;
        }

        const pageResponse = responseBody.data || {};

        approvals = Array.isArray(pageResponse.content)
            ? pageResponse.content
            : [];

        currentPage =
            pageResponse.page ??
            pageResponse.number ??
            request.page;

        totalPages = pageResponse.totalPages ?? 0;
        totalElements = pageResponse.totalElements ?? 0;

        if (
            selectedApproval &&
            !approvals.some(function (item) {
                return String(item.id) ===
                    String(selectedApproval.id);
            })
        ) {
            selectedApproval = null;
            resetApprovalPanel();
        }

        renderApprovals();
    } catch (error) {
        if (requestSequence !== searchRequestSequence) {
            return;
        }

        console.error("Unable to search approval tasks:", error);

        approvals = [];
        totalElements = 0;
        totalPages = 0;

        renderApprovals();
        showToast(error.message);
    } finally {
        if (requestSequence === searchRequestSequence) {
            showLoading(false);
        }
    }
}

async function refreshApprovals() {
    selectedApproval = null;
    resetApprovalPanel();

    await Promise.all([
        loadDashboard(),
        searchApprovals(currentPage)
    ]);
}

function renderApprovals() {
    const tableBody =
        document.getElementById("approvalsTableBody");

    const emptyState =
        document.getElementById("emptyState");

    tableBody.innerHTML = "";

    if (approvals.length === 0) {
        emptyState.style.display = "block";
        return;
    }

    emptyState.style.display = "none";

    approvals.forEach(function (approval) {
        const row = document.createElement("tr");

        row.dataset.approvalId = approval.id;

        if (
            selectedApproval &&
            String(selectedApproval.id) === String(approval.id)
        ) {
            row.classList.add("selected");
        }

        row.innerHTML = `
        <td>
          <div class="approval-title">
            ${escapeHtml(approval.title || "-")}
          </div>

          <div class="approval-meta">
            Created ${formatDateTime(approval.createdAt)}
            · ${escapeHtml(formatEnum(approval.priority))} priority
          </div>
        </td>

        <td>
          <div class="approval-title">
            ${escapeHtml(approval.proposalName || "-")}
          </div>

          <div class="approval-meta">
            ${escapeHtml(approval.proposalNumber || "-")}
          </div>
        </td>

        <td>
          <span class="pill ${getStageClass(approval.approvalStage)}">
            ${getStageIcon(approval.approvalStage)}
            ${escapeHtml(formatEnum(approval.approvalStage))}
          </span>
        </td>

        <td>
          <span class="pill ${getStatusClass(approval.status)}">
            ${getStatusIcon(approval.status)}
            ${escapeHtml(formatEnum(approval.status))}
          </span>
        </td>

        <td>
          <div class="approval-title">
            ${escapeHtml(approval.approverName || "Unassigned")}
          </div>

          <div class="approval-meta">
            ${escapeHtml(approval.approverUserId || "")}
          </div>
        </td>

        <td>
          <span class="valuation">
            ${formatMoney(
            approval.proposalValue,
            approval.currency || "EUR"
        )}
          </span>
        </td>

        <td>
          <span class="priority ${getPriorityClass(approval.priority)}">
            ${formatDate(approval.dueDate)}
          </span>
        </td>
      `;

        row.addEventListener("click", function () {
            fetchApprovalDetail(approval.id);
        });

        tableBody.appendChild(row);
    });
}

async function fetchApprovalDetail(approvalId) {
    const requestSequence = ++detailRequestSequence;

    setApprovalPanelLoading(true);

    try {
        const responseBody = await apiRequest(
            APPROVALS_API_URL +
            "/" +
            encodeURIComponent(approvalId),
            {
                method: "GET"
            }
        );

        if (requestSequence !== detailRequestSequence) {
            return;
        }

        selectedApproval = responseBody.data;

        if (!selectedApproval) {
            throw new Error(
                "Approval detail response does not contain data."
            );
        }

        renderApprovals();
        renderApprovalPanel(selectedApproval);

        if (selectedApproval.canDelegate) {
            await loadEligibleDelegates(selectedApproval.id);
        } else {
            renderDelegateOptions([]);
        }
    } catch (error) {
        if (requestSequence !== detailRequestSequence) {
            return;
        }

        console.error("Unable to load approval:", error);

        selectedApproval = null;
        resetApprovalPanel();
        showToast(error.message);
    } finally {
        if (requestSequence === detailRequestSequence) {
            setApprovalPanelLoading(false);
        }
    }
}

function renderApprovalPanel(approval) {
    document.getElementById(
        "approvalPlaceholder"
    ).style.display = "none";

    document.getElementById(
        "approvalContent"
    ).style.display = "block";

    setText("detailTitle", approval.title || "-");

    setText(
        "detailSubtitle",
        [
            approval.proposalNumber,
            approval.contractName
        ].filter(Boolean).join(" · ") || "-"
    );

    setText(
        "detailProposalNumber",
        approval.proposalNumber || "-"
    );

    setText(
        "detailContract",
        approval.contractName || "-"
    );

    setText(
        "detailStage",
        formatEnum(approval.approvalStage)
    );

    setText(
        "detailStatus",
        formatEnum(approval.status)
    );

    setText(
        "detailValue",
        formatMoney(
            approval.proposalValue,
            approval.currency || "EUR"
        )
    );

    setText(
        "detailMargin",
        approval.expectedMargin !== null &&
        approval.expectedMargin !== undefined
            ? approval.expectedMargin + "%"
            : "-"
    );

    setText(
        "detailLots",
        approval.qualifiedLotCount !== null &&
        approval.qualifiedLotCount !== undefined
            ? approval.qualifiedLotCount + " qualified lots"
            : "-"
    );

    setText(
        "detailDueDate",
        formatDate(approval.dueDate)
    );

    setText(
        "riskScore",
        approval.riskScore !== null &&
        approval.riskScore !== undefined
            ? approval.riskScore + "/10"
            : "-"
    );

    setText(
        "commercialRisks",
        approval.commercialRisks ?? "-"
    );

    setText(
        "legalRisks",
        approval.legalRisks ?? "-"
    );

    setText(
        "complianceRisks",
        approval.complianceRisks ?? "-"
    );

    setText(
        "approvalContextText",
        approval.context || "-"
    );

    document.getElementById("approvalComment").value = "";
    document.getElementById("delegateRow").classList.remove("show");

    renderApprovalChain(approval.approvalChain);
    renderReviewerComments(approval.reviewerComments);
    renderHistory(approval.history);
    configureApprovalActions(approval);
}

function configureApprovalActions(approval) {
    const approveButton =
        document.querySelector(
            ".decision-actions .green-btn"
        );

    const delegateButton =
        document.querySelector(
            ".decision-actions .orange-btn"
        );

    const clarificationButton =
        document.querySelector(
            ".decision-actions .secondary"
        );

    const rejectButton =
        document.querySelector(
            ".decision-actions .red-btn"
        );

    approveButton.disabled =
        approval.canApprove !== true;

    delegateButton.disabled =
        approval.canDelegate !== true;

    clarificationButton.disabled =
        approval.canRequestClarification !== true;

    rejectButton.disabled =
        approval.canReject !== true;

    const canComment =
        approval.canApprove === true ||
        approval.canDelegate === true ||
        approval.canRequestClarification === true ||
        approval.canReject === true;

    document.getElementById(
        "approvalComment"
    ).disabled = !canComment;
}

async function loadEligibleDelegates(approvalId) {
    try {
        const responseBody = await apiRequest(
            APPROVALS_API_URL +
            "/" +
            encodeURIComponent(approvalId) +
            "/delegates",
            {
                method: "GET"
            }
        );

        renderDelegateOptions(
            Array.isArray(responseBody.data)
                ? responseBody.data
                : []
        );
    } catch (error) {
        console.error("Unable to load delegates:", error);
        renderDelegateOptions([]);
        showToast(error.message);
    }
}

function renderDelegateOptions(delegateUsers) {
    const select =
        document.getElementById("delegateUserId");

    select.innerHTML =
        '<option value="">Select delegate user</option>';

    delegateUsers.forEach(function (delegateUser) {
        const option =
            document.createElement("option");

        option.value = delegateUser.userId;

        option.textContent =
            delegateUser.role
                ? delegateUser.fullName +
                " · " +
                delegateUser.role
                : delegateUser.fullName;

        select.appendChild(option);
    });
}

function toggleDelegateRow() {
    if (!selectedApproval) {
        showToast("Select an approval task first.");
        return;
    }

    if (selectedApproval.canDelegate !== true) {
        showToast(
            "You are not authorized to delegate this task."
        );
        return;
    }

    const delegateRow =
        document.getElementById("delegateRow");

    if (!delegateRow.classList.contains("show")) {
        delegateRow.classList.add("show");
        showToast(
            "Select a delegate, add a comment, then click Delegate again."
        );
        return;
    }

    submitDecision("DELEGATE");
}

async function submitDecision(decision) {
    if (!selectedApproval) {
        showToast("Select an approval task first.");
        return;
    }

    const comment =
        document.getElementById("approvalComment").value.trim();

    const delegateUserValue =
        document.getElementById("delegateUserId").value;

    if (
        (
            decision === "REJECT" ||
            decision === "REQUEST_CLARIFICATION"
        ) &&
        !comment
    ) {
        showToast(
            "A comment is required for rejection or clarification."
        );
        return;
    }

    if (
        decision === "DELEGATE" &&
        !delegateUserValue
    ) {
        document.getElementById(
            "delegateRow"
        ).classList.add("show");

        showToast("Select a delegate user.");
        return;
    }

    const delegateUserId =
        delegateUserValue
            ? Number(delegateUserValue)
            : null;

    setDecisionButtonsDisabled(true);

    try {
        const responseBody = await apiRequest(
            APPROVALS_API_URL +
            "/" +
            encodeURIComponent(selectedApproval.id) +
            "/decision",
            {
                method: "POST",
                body: JSON.stringify({
                    decision: decision,
                    comment: comment || null,
                    delegateUserId: delegateUserId
                })
            }
        );

        showToast(
            responseBody.message ||
            "Approval decision submitted successfully."
        );

        const approvalId = selectedApproval.id;

        await Promise.all([
            loadDashboard(),
            searchApprovals(currentPage)
        ]);

        await fetchApprovalDetail(approvalId);
    } catch (error) {
        console.error(
            "Unable to submit approval decision:",
            error
        );

        showToast(error.message);
    } finally {
        setDecisionButtonsDisabled(false);
    }
}

function resetApprovalPanel() {
    document.getElementById(
        "approvalPlaceholder"
    ).style.display = "flex";

    document.getElementById(
        "approvalContent"
    ).style.display = "none";

    document.getElementById(
        "approvalComment"
    ).value = "";

    document.getElementById(
        "delegateRow"
    ).classList.remove("show");

    renderDelegateOptions([]);
}

function setApprovalPanelLoading(isLoading) {
    const placeholder =
        document.getElementById("approvalPlaceholder");

    const content =
        document.getElementById("approvalContent");

    if (isLoading) {
        placeholder.style.display = "flex";
        content.style.display = "none";

        placeholder.innerHTML = `
        <div>
          <div class="approval-placeholder-icon">
            <span class="spinner"></span>
          </div>
          <h3>Loading approval</h3>
          <p>
            Retrieving proposal, risk, review,
            and approval workflow information.
          </p>
        </div>
      `;

        return;
    }

    restoreApprovalPlaceholder();
}

function restoreApprovalPlaceholder() {
    document.getElementById(
        "approvalPlaceholder"
    ).innerHTML = `
      <div>
        <div class="approval-placeholder-icon">✔</div>
        <h3>Select an approval task</h3>
        <p>
          Proposal details, financial summary, reviewer
          feedback, approval chain, and decision actions
          will appear here.
        </p>
      </div>
    `;
}

function renderApprovalChain(chain) {
    const list =
        document.getElementById("approvalChainList");

    list.innerHTML = "";

    const items =
        Array.isArray(chain) ? chain : [];

    if (items.length === 0) {
        renderEmptyListItem(
            list,
            "timeline-item",
            "timeline-icon",
            "timeline-title",
            "timeline-text",
            "No approval chain",
            "No approval chain is available."
        );

        return;
    }

    items.forEach(function (item) {
        const row = document.createElement("div");
        row.className = "timeline-item";

        const title =
            item.stageName ||
            item.title ||
            "Approval stage";

        const textParts = [
            item.approverName,
            item.comment,
            item.actionDate
                ? formatDateTime(item.actionDate)
                : null
        ].filter(Boolean);

        row.innerHTML = `
        <div class="timeline-icon">
          ${getChainIcon(item.status)}
        </div>
        <div>
          <div class="timeline-title">
            ${escapeHtml(title)}
          </div>
          <div class="timeline-text">
            ${escapeHtml(textParts.join(" · ") || "-")}
          </div>
        </div>
      `;

        list.appendChild(row);
    });
}

function renderReviewerComments(comments) {
    const list =
        document.getElementById("reviewerCommentsList");

    list.innerHTML = "";

    const items =
        Array.isArray(comments) ? comments : [];

    if (items.length === 0) {
        renderEmptyListItem(
            list,
            "comment-item",
            "comment-icon",
            "comment-title",
            "comment-text",
            "No reviewer comments",
            "No reviewer comments are available."
        );

        return;
    }

    items.forEach(function (item) {
        const row = document.createElement("div");
        row.className = "comment-item";

        const title =
            item.reviewerName ||
            item.title ||
            "Reviewer";

        const text =
            item.comment ||
            item.text ||
            "-";

        row.innerHTML = `
        <div class="comment-icon">📝</div>
        <div>
          <div class="comment-title">
            ${escapeHtml(title)}
          </div>
          <div class="comment-text">
            ${escapeHtml(text)}
            ${
            item.createdAt
                ? " · " +
                escapeHtml(formatDateTime(item.createdAt))
                : ""
        }
          </div>
        </div>
      `;

        list.appendChild(row);
    });
}

function renderHistory(history) {
    const list =
        document.getElementById("historyList");

    list.innerHTML = "";

    const items =
        Array.isArray(history) ? history : [];

    if (items.length === 0) {
        renderEmptyListItem(
            list,
            "history-item",
            "history-icon",
            "history-title",
            "history-text",
            "No history",
            "No approval history is available."
        );

        return;
    }

    items.forEach(function (item) {
        const row = document.createElement("div");
        row.className = "history-item";

        const title =
            item.action ||
            item.title ||
            "Approval activity";

        const textParts = [
            item.comment || item.text,
            item.performedBy
                ? "By " + item.performedBy
                : null,
            item.createdAt
                ? formatDateTime(item.createdAt)
                : null
        ].filter(Boolean);

        row.innerHTML = `
        <div class="history-icon">🕘</div>
        <div>
          <div class="history-title">
            ${escapeHtml(formatEnum(title))}
          </div>
          <div class="history-text">
            ${escapeHtml(textParts.join(" · ") || "-")}
          </div>
        </div>
      `;

        list.appendChild(row);
    });
}

function renderActivities(activities) {
    const list =
        document.getElementById("activityList");

    list.innerHTML = "";

    const items =
        Array.isArray(activities) ? activities : [];

    if (items.length === 0) {
        const item = document.createElement("div");
        item.className = "activity-item";

        item.innerHTML = `
        <div>
          <div class="activity-title">
            No recent activity loaded
          </div>
          <div class="activity-desc">
            Add an approval activity endpoint if this
            section should contain tenant-wide activity.
          </div>
        </div>
        <div class="activity-meta">-</div>
      `;

        list.appendChild(item);
        return;
    }

    items.forEach(function (activity) {
        const item = document.createElement("div");
        item.className = "activity-item";

        item.innerHTML = `
        <div>
          <div class="activity-title">
            ${escapeHtml(activity.title || "-")}
          </div>
          <div class="activity-desc">
            ${escapeHtml(activity.description || "-")}
          </div>
        </div>
        <div class="activity-meta">
          ${escapeHtml(activity.meta || "-")}
        </div>
      `;

        list.appendChild(item);
    });
}

function renderEmptyListItem(
    container,
    rowClass,
    iconClass,
    titleClass,
    textClass,
    title,
    text
) {
    const row = document.createElement("div");
    row.className = rowClass;

    row.innerHTML = `
      <div class="${iconClass}">ℹ</div>
      <div>
        <div class="${titleClass}">
          ${escapeHtml(title)}
        </div>
        <div class="${textClass}">
          ${escapeHtml(text)}
        </div>
      </div>
    `;

    container.appendChild(row);
}

function openSelectedProposal() {
    if (!selectedApproval) {
        showToast("Select an approval task first.");
        return;
    }

    const url =
        selectedApproval.proposalPageUrl ||
        (
            selectedApproval.proposalId
                ? "ProposalWorkspace.html?proposalId=" +
                encodeURIComponent(selectedApproval.proposalId)
                : null
        );

    navigateIfAvailable(
        url,
        "Proposal page is not available."
    );
}

function openSelectedReview() {
    if (!selectedApproval) {
        showToast("Select an approval task first.");
        return;
    }

    const url =
        selectedApproval.reviewPageUrl ||
        (
            selectedApproval.proposalId
                ? "ProposalReview.html?proposalId=" +
                encodeURIComponent(selectedApproval.proposalId)
                : null
        );

    navigateIfAvailable(
        url,
        "Review page is not available."
    );
}

function openSelectedAuditTrail() {
    if (!selectedApproval) {
        showToast("Select an approval task first.");
        return;
    }

    window.location.href =
        AUDIT_TRAIL_URL +
        "?approvalId=" +
        encodeURIComponent(selectedApproval.id);
}

async function downloadSelectedProposal() {
    if (!selectedApproval) {
        showToast("Select an approval task first.");
        return;
    }

    const downloadUrl =
        selectedApproval.documentDownloadUrl;

    if (!downloadUrl) {
        showToast("Proposal document is not available.");
        return;
    }

    try {
        const token = getAccessToken();

        const absoluteUrl =
            downloadUrl.startsWith("http")
                ? downloadUrl
                : API_BASE_URL + downloadUrl;

        const response = await fetch(
            absoluteUrl,
            {
                method: "GET",
                headers: token
                    ? {
                        "Authorization": "Bearer " + token
                    }
                    : {}
            }
        );

        if (!response.ok) {
            throw new Error(
                "Download failed with status " +
                response.status
            );
        }

        const blob = await response.blob();

        const temporaryUrl =
            window.URL.createObjectURL(blob);

        const link =
            document.createElement("a");

        link.href = temporaryUrl;
        link.download =
            (selectedApproval.proposalNumber || "proposal") +
            ".pdf";

        document.body.appendChild(link);
        link.click();
        link.remove();

        window.URL.revokeObjectURL(temporaryUrl);
    } catch (error) {
        console.error("Unable to download proposal:", error);
        showToast(error.message);
    }
}

function navigateIfAvailable(url, unavailableMessage) {
    if (!url) {
        showToast(unavailableMessage);
        return;
    }

    window.location.href = url;
}

function goToPage(url) {
    window.location.href = url;
}

async function apiRequest(url, options) {
    const token = getAccessToken();

    const requestOptions = {
        method: options?.method || "GET",
        headers: {
            "Accept": "application/json"
        }
    };

    if (token) {
        requestOptions.headers.Authorization =
            "Bearer " + token;
    }

    if (options?.body !== undefined) {
        requestOptions.headers["Content-Type"] =
            "application/json";

        requestOptions.body = options.body;
    }

    const response =
        await fetch(url, requestOptions);

    const contentType =
        response.headers.get("content-type") || "";

    let responseBody;

    if (contentType.includes("application/json")) {
        responseBody = await response.json();
    } else {
        responseBody = {
            success: response.ok,
            message: await response.text(),
            data: null
        };
    }

    if (!response.ok) {
        throw new Error(
            responseBody?.message ||
            "Request failed with status " + response.status
        );
    }

    if (!responseBody) {
        throw new Error("Backend returned an empty response.");
    }

    if (responseBody.success === false) {
        throw new Error(
            responseBody.message ||
            "Backend request failed."
        );
    }

    return responseBody;
}

function getAccessToken() {
    if (window.keycloak?.token) {
        return window.keycloak.token;
    }

    return (
        localStorage.getItem("access_token") ||
        sessionStorage.getItem("access_token") ||
        ""
    );
}

function setDecisionButtonsDisabled(disabled) {
    document
        .querySelectorAll(".decision-actions button")
        .forEach(function (button) {
            button.disabled = disabled;
        });
}

function getStageClass(stage) {
    switch (stage) {
        case "FINANCE_APPROVAL":
            return "finance";
        case "LEGAL_APPROVAL":
            return "legal";
        case "COMMERCIAL_APPROVAL":
            return "commercial";
        case "EXECUTIVE_APPROVAL":
            return "executive";
        case "DELIVERY_APPROVAL":
            return "delivery";
        default:
            return "executive";
    }
}

function getStageIcon(stage) {
    switch (stage) {
        case "FINANCE_APPROVAL":
            return "💰";
        case "LEGAL_APPROVAL":
            return "⚖️";
        case "COMMERCIAL_APPROVAL":
            return "📈";
        case "EXECUTIVE_APPROVAL":
            return "🏛️";
        case "DELIVERY_APPROVAL":
            return "🚚";
        default:
            return "✔";
    }
}

function getStatusClass(status) {
    switch (status) {
        case "PENDING":
            return "pending";
        case "APPROVED":
            return "approved";
        case "REJECTED":
            return "rejected";
        case "DELEGATED":
            return "delegated";
        case "CLARIFICATION_REQUESTED":
            return "clarification";
        default:
            return "pending";
    }
}

function getStatusIcon(status) {
    switch (status) {
        case "PENDING":
            return "⏳";
        case "APPROVED":
            return "✅";
        case "REJECTED":
            return "❌";
        case "DELEGATED":
            return "🔄";
        case "CLARIFICATION_REQUESTED":
            return "❓";
        default:
            return "⏳";
    }
}

function getPriorityClass(priority) {
    switch (priority) {
        case "HIGH":
            return "high";
        case "LOW":
            return "low";
        default:
            return "medium";
    }
}

function getChainIcon(status) {
    switch (status) {
        case "APPROVED":
            return "✅";
        case "PENDING":
            return "⏳";
        case "REJECTED":
            return "❌";
        case "DELEGATED":
            return "🔄";
        case "CLARIFICATION_REQUESTED":
            return "❓";
        case "WAITING":
            return "○";
        default:
            return "✔";
    }
}

function formatEnum(value) {
    if (!value) {
        return "-";
    }

    return String(value)
        .replaceAll("_", " ")
        .toLowerCase()
        .replace(/\b\w/g, function (character) {
            return character.toUpperCase();
        });
}

function formatDate(value) {
    if (!value) {
        return "-";
    }

    const date = new Date(value);

    if (Number.isNaN(date.getTime())) {
        return String(value);
    }

    return new Intl.DateTimeFormat(
        "en-GB",
        {
            day: "2-digit",
            month: "short",
            year: "numeric"
        }
    ).format(date);
}

function formatDateTime(value) {
    if (!value) {
        return "-";
    }

    const date = new Date(value);

    if (Number.isNaN(date.getTime())) {
        return String(value);
    }

    return new Intl.DateTimeFormat(
        "en-GB",
        {
            day: "2-digit",
            month: "short",
            year: "numeric",
            hour: "2-digit",
            minute: "2-digit"
        }
    ).format(date);
}

function formatMoney(value, currency) {
    return new Intl.NumberFormat(
        "en-DE",
        {
            style: "currency",
            currency: currency || "EUR",
            maximumFractionDigits: 0
        }
    ).format(Number(value) || 0);
}

function formatCompactMoney(value, currency) {
    return new Intl.NumberFormat(
        "en-DE",
        {
            style: "currency",
            currency: currency || "EUR",
            notation: "compact",
            maximumFractionDigits: 1
        }
    ).format(Number(value) || 0);
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

function setText(elementId, value) {
    const element =
        document.getElementById(elementId);

    if (element) {
        element.textContent =
            value === null || value === undefined
                ? "-"
                : String(value);
    }
}

function showLoading(isLoading) {
    document.getElementById(
        "loadingIndicator"
    ).style.display =
        isLoading ? "flex" : "none";
}

function showToast(message) {
    const toast =
        document.getElementById("toast");

    toast.textContent =
        message || "Operation completed.";

    toast.style.display = "block";

    window.clearTimeout(showToast.timeoutId);

    showToast.timeoutId =
        window.setTimeout(function () {
            toast.style.display = "none";
        }, 3000);
}