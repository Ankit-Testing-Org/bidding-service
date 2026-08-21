'use strict';
const API_BASE_URL = "http://localhost:8080";
const REVIEWS_API_URL = API_BASE_URL + "/api/reviews";

const REVIEW_DASHBOARD_URL = REVIEWS_API_URL + "/dashboard";
const REVIEW_SEARCH_URL = REVIEWS_API_URL + "/search";

const AUDIT_TRAIL_URL = "ReviewAuditTrail.html";

const DEFAULT_PAGE = 0;
const DEFAULT_SIZE = 20;
const DEFAULT_SORT_BY = "createdAt";
const DEFAULT_SORT_DIRECTION = "DESC";

let reviews = [];
let selectedReview = null;

let currentPage = DEFAULT_PAGE;
let pageSize = DEFAULT_SIZE;
let totalElements = 0;
let totalPages = 0;
let firstPage = true;
let lastPage = true;

let searchTimeoutId = null;
let currentSearchRequestId = 0;
let currentDetailRequestId = 0;

document.addEventListener("DOMContentLoaded", function () {
    bindEvents();
    initializeReviewPage();
});

async function initializeReviewPage() {
    resetReviewPanel();

    await Promise.all([
        loadReviewDashboard(),
        searchReviews(DEFAULT_PAGE)
    ]);
}

function bindEvents() {
    const reviewSearchInput = document.getElementById("reviewSearchInput");
    const globalSearchInput = document.getElementById("globalSearchInput");
    const statusFilter = document.getElementById("statusFilter");
    const typeFilter = document.getElementById("typeFilter");
    const openSourceButton = document.getElementById("openSourceButton");
    const auditTrailButton = document.getElementById("auditTrailButton");

    reviewSearchInput.addEventListener("input", function () {
        globalSearchInput.value = reviewSearchInput.value;
        scheduleSearch();
    });

    globalSearchInput.addEventListener("input", function () {
        reviewSearchInput.value = globalSearchInput.value;
        scheduleSearch();
    });

    statusFilter.addEventListener("change", function () {
        searchReviews(DEFAULT_PAGE);
    });

    typeFilter.addEventListener("change", function () {
        searchReviews(DEFAULT_PAGE);
    });

    openSourceButton.addEventListener("click", function () {
        if (!selectedReview) {
            showToast("Select a review task first.");
            return;
        }

        openReviewSource(selectedReview);
    });

    auditTrailButton.addEventListener("click", function () {
        if (!selectedReview) {
            showToast("Select a review task first.");
            return;
        }

        window.location.href =
            AUDIT_TRAIL_URL +
            "?reviewId=" +
            encodeURIComponent(selectedReview.id);
    });
}

function scheduleSearch() {
    window.clearTimeout(searchTimeoutId);

    searchTimeoutId = window.setTimeout(function () {
        searchReviews(DEFAULT_PAGE);
    }, 350);
}

async function loadReviewDashboard() {
    try {
        const responseBody = await apiRequest(
            REVIEW_DASHBOARD_URL,
            {
                method: "GET"
            }
        );

        const dashboard = responseBody.data;

        if (!dashboard) {
            throw new Error("Review dashboard response does not contain data.");
        }

        renderDashboard(dashboard);
    } catch (error) {
        console.error("Unable to load review dashboard:", error);
        resetDashboard();
        showToast(error.message || "Unable to load review dashboard.");
    }
}

function renderDashboard(dashboard) {
    setText(
        "totalReviewsStat",
        firstDefined(
            dashboard.totalReviewCount,
            dashboard.totalReviews,
            0
        )
    );

    setText(
        "pendingReviewsStat",
        firstDefined(
            dashboard.pendingCount,
            dashboard.pendingReviewCount,
            0
        )
    );

    setText(
        "highPriorityStat",
        firstDefined(
            dashboard.highPriorityCount,
            0
        )
    );

    setText(
        "pendingCount",
        firstDefined(
            dashboard.pendingCount,
            dashboard.pendingReviewCount,
            0
        )
    );

    setText(
        "approvedCount",
        firstDefined(
            dashboard.approvedCount,
            dashboard.approvedReviewCount,
            0
        )
    );

    setText(
        "changesCount",
        firstDefined(
            dashboard.changesRequestedCount,
            dashboard.changeRequestedCount,
            0
        )
    );

    setText(
        "rejectedCount",
        firstDefined(
            dashboard.rejectedCount,
            dashboard.rejectedReviewCount,
            0
        )
    );
}

function resetDashboard() {
    setText("totalReviewsStat", "0");
    setText("pendingReviewsStat", "0");
    setText("highPriorityStat", "0");
    setText("pendingCount", "0");
    setText("approvedCount", "0");
    setText("changesCount", "0");
    setText("rejectedCount", "0");
}

function buildReviewSearchRequest(page) {
    const searchText =
        document.getElementById("reviewSearchInput").value.trim();

    const selectedStatus =
        document.getElementById("statusFilter").value;

    const selectedType =
        document.getElementById("typeFilter").value;

    return {
        searchText: searchText || null,
        status: selectedStatus === "ALL" ? null : selectedStatus,
        reviewType: selectedType === "ALL" ? null : selectedType,
        page: Number.isInteger(page) && page >= 0
            ? page
            : DEFAULT_PAGE,
        size: pageSize,
        sortBy: DEFAULT_SORT_BY,
        sortDirection: DEFAULT_SORT_DIRECTION
    };
}

async function searchReviews(page) {
    const requestId = ++currentSearchRequestId;

    showLoading(true);

    try {
        const requestBody = buildReviewSearchRequest(page);

        const responseBody = await apiRequest(
            REVIEW_SEARCH_URL,
            {
                method: "POST",
                body: JSON.stringify(requestBody)
            }
        );

        if (requestId !== currentSearchRequestId) {
            return;
        }

        const pageData = responseBody.data;

        if (!pageData) {
            throw new Error("Review search response does not contain data.");
        }

        reviews = Array.isArray(pageData.content)
            ? pageData.content
            : [];

        currentPage = toNumber(
            firstDefined(pageData.page, pageData.number, requestBody.page),
            requestBody.page
        );

        pageSize = toNumber(
            firstDefined(pageData.size, requestBody.size),
            requestBody.size
        );

        totalElements = toNumber(
            firstDefined(pageData.totalElements, reviews.length),
            reviews.length
        );

        totalPages = toNumber(
            firstDefined(pageData.totalPages, reviews.length > 0 ? 1 : 0),
            reviews.length > 0 ? 1 : 0
        );

        firstPage = typeof pageData.first === "boolean"
            ? pageData.first
            : currentPage === 0;

        lastPage = typeof pageData.last === "boolean"
            ? pageData.last
            : totalPages === 0 || currentPage >= totalPages - 1;

        if (
            selectedReview &&
            !reviews.some(function (review) {
                return review.id === selectedReview.id;
            })
        ) {
            selectedReview = null;
            resetReviewPanel();
        }

        renderReviews();

        if (reviews.length === 0) {
            showToast("No review tasks matched the selected filters.");
        }
    } catch (error) {
        if (requestId !== currentSearchRequestId) {
            return;
        }

        console.error("Unable to search reviews:", error);

        reviews = [];
        currentPage = DEFAULT_PAGE;
        totalElements = 0;
        totalPages = 0;
        firstPage = true;
        lastPage = true;

        renderReviews();
        showToast(error.message || "Unable to load review tasks.");
    } finally {
        if (requestId === currentSearchRequestId) {
            showLoading(false);
        }
    }
}

async function refreshReviews() {
    selectedReview = null;
    resetReviewPanel();

    await Promise.all([
        loadReviewDashboard(),
        searchReviews(currentPage)
    ]);
}

function renderReviews() {
    const tableBody =
        document.getElementById("reviewsTableBody");

    const emptyState =
        document.getElementById("emptyState");

    tableBody.innerHTML = "";

    if (!Array.isArray(reviews) || reviews.length === 0) {
        emptyState.style.display = "block";
        return;
    }

    emptyState.style.display = "none";

    reviews.forEach(function (review) {
        const row = document.createElement("tr");

        row.dataset.reviewId = review.id;

        if (
            selectedReview &&
            String(selectedReview.id) === String(review.id)
        ) {
            row.classList.add("selected");
        }

        row.innerHTML = `
        <td>
          <div class="review-title">
            ${escapeHtml(review.title || "-")}
          </div>

          <div class="review-meta">
            ${escapeHtml(review.sourceName || "-")}
            · Created ${formatDateTime(review.createdAt)}
          </div>
        </td>

        <td>
          <span class="pill ${getTypeClass(review.reviewType)}">
            ${getTypeIcon(review.reviewType)}
            ${escapeHtml(formatEnum(review.reviewType))}
          </span>
        </td>

        <td>
          <span class="pill ${getStatusClass(review.status)}">
            ${getStatusIcon(review.status)}
            ${escapeHtml(formatEnum(review.status))}
          </span>
        </td>

        <td>
          <div class="review-title">
            ${escapeHtml(review.reviewerName || "Unassigned")}
          </div>

          <div class="review-meta">
            ${escapeHtml(review.reviewerUserId || "")}
          </div>
        </td>

        <td>
          <span class="priority ${getPriorityClass(review.priority)}">
            ${escapeHtml(formatEnum(review.priority))}
          </span>
        </td>

        <td>
          <span class="review-title">
            ${formatDate(review.dueDate)}
          </span>
        </td>
      `;

        row.addEventListener("click", function () {
            fetchReviewDetail(review.id);
        });

        tableBody.appendChild(row);
    });
}

async function fetchReviewDetail(reviewId) {
    if (reviewId === null || reviewId === undefined) {
        showToast("Review identifier is not available.");
        return;
    }

    const requestId = ++currentDetailRequestId;

    setReviewPanelLoading(true);

    try {
        const responseBody = await apiRequest(
            REVIEWS_API_URL +
            "/" +
            encodeURIComponent(reviewId),
            {
                method: "GET"
            }
        );

        if (requestId !== currentDetailRequestId) {
            return;
        }

        const detail = responseBody.data;

        if (!detail) {
            throw new Error("Review detail response does not contain data.");
        }

        selectedReview = detail;

        renderReviews();
        renderReviewPanel(detail);
        showToast("Review task loaded.");
    } catch (error) {
        if (requestId !== currentDetailRequestId) {
            return;
        }

        console.error("Unable to load review detail:", error);
        selectedReview = null;
        resetReviewPanel();
        showToast(error.message || "Unable to load review details.");
    } finally {
        if (requestId === currentDetailRequestId) {
            setReviewPanelLoading(false);
        }
    }
}

function renderReviewPanel(review) {
    document.getElementById("reviewPlaceholder").style.display = "none";
    document.getElementById("reviewContent").style.display = "block";

    setText("detailTitle", review.title || "-");
    setText("detailSubtitle", review.sourceName || "-");
    setText("detailType", formatEnum(review.reviewType));
    setText("detailStatus", formatEnum(review.status));
    setText("detailReviewer", review.reviewerName || "Unassigned");
    setText("detailPriority", formatEnum(review.priority));
    setText("detailDueDate", formatDate(review.dueDate));
    setText("detailObject", review.sourceName || "-");
    setText("reviewContextText", review.context || "-");

    document.getElementById("reviewComment").value = "";

    configureDecisionControls(review);
    renderHistory(review.history);
}

function configureDecisionControls(review) {
    const decisionButtons =
        document.querySelectorAll(".decision-actions button");

    const commentInput =
        document.getElementById("reviewComment");

    const isPending =
        review.status === "PENDING" ||
        review.status === "CHANGES_REQUESTED";

    decisionButtons.forEach(function (button) {
        button.disabled = !isPending;
    });

    commentInput.disabled = !isPending;

    if (!isPending) {
        commentInput.placeholder =
            "This review already has a final decision.";
    } else {
        commentInput.placeholder =
            "Write review comment here...";
    }
}

function resetReviewPanel() {
    document.getElementById("reviewPlaceholder").style.display = "flex";
    document.getElementById("reviewContent").style.display = "none";
    document.getElementById("reviewComment").value = "";
    document.getElementById("historyList").innerHTML = "";
}

function setReviewPanelLoading(isLoading) {
    const placeholder =
        document.getElementById("reviewPlaceholder");

    const content =
        document.getElementById("reviewContent");

    if (isLoading) {
        placeholder.style.display = "flex";
        content.style.display = "none";

        placeholder.innerHTML = `
        <div>
          <div class="review-placeholder-icon">
            <span class="spinner"></span>
          </div>
          <h3>Loading review</h3>
          <p>Retrieving review context and decision history.</p>
        </div>
      `;

        return;
    }

    if (!selectedReview) {
        restoreReviewPlaceholder();
    }
}

function restoreReviewPlaceholder() {
    const placeholder =
        document.getElementById("reviewPlaceholder");

    placeholder.innerHTML = `
      <div>
        <div class="review-placeholder-icon">✅</div>
        <h3>Select a review task</h3>
        <p>
          Once selected, the task context, review history,
          comment box, and decision actions will appear here.
        </p>
      </div>
    `;
}

function renderHistory(history) {
    const historyList =
        document.getElementById("historyList");

    historyList.innerHTML = "";

    const historyItems =
        Array.isArray(history) ? history : [];

    if (historyItems.length === 0) {
        const emptyHistory =
            document.createElement("div");

        emptyHistory.className = "history-item";

        emptyHistory.innerHTML = `
        <div class="history-icon">ℹ</div>
        <div>
          <div class="history-title">No history</div>
          <div class="history-text">
            No review history is available for this task.
          </div>
        </div>
      `;

        historyList.appendChild(emptyHistory);
        return;
    }

    historyItems.forEach(function (historyItem) {
        const row = document.createElement("div");
        row.className = "history-item";

        const historyTitle =
            firstDefined(
                historyItem.title,
                historyItem.action,
                historyItem.status,
                "Review activity"
            );

        const historyText =
            firstDefined(
                historyItem.text,
                historyItem.comment,
                historyItem.description,
                ""
            );

        const performedBy =
            firstDefined(
                historyItem.createdBy,
                historyItem.performedBy,
                historyItem.reviewedBy,
                ""
            );

        const activityDate =
            firstDefined(
                historyItem.createdAt,
                historyItem.performedAt,
                historyItem.updatedAt,
                null
            );

        const metadataParts = [];

        if (historyText) {
            metadataParts.push(historyText);
        }

        if (performedBy) {
            metadataParts.push("By " + performedBy);
        }

        if (activityDate) {
            metadataParts.push(formatDateTime(activityDate));
        }

        row.innerHTML = `
        <div class="history-icon">
          ${getHistoryIcon(historyTitle)}
        </div>

        <div>
          <div class="history-title">
            ${escapeHtml(formatEnum(historyTitle))}
          </div>

          <div class="history-text">
            ${escapeHtml(metadataParts.join(" · ") || "-")}
          </div>
        </div>
      `;

        historyList.appendChild(row);
    });
}

async function submitDecision(decision) {
    if (!selectedReview) {
        showToast("Select a review task first.");
        return;
    }

    if (
        selectedReview.status !== "PENDING" &&
        selectedReview.status !== "CHANGES_REQUESTED"
    ) {
        showToast("This review already has a final decision.");
        return;
    }

    const comment =
        document.getElementById("reviewComment").value.trim();

    if (
        !comment &&
        (
            decision === "CHANGES_REQUESTED" ||
            decision === "REJECTED"
        )
    ) {
        showToast(
            "A comment is required when requesting changes or rejecting."
        );
        return;
    }

    const decisionButtons =
        document.querySelectorAll(".decision-actions button");

    setButtonsDisabled(decisionButtons, true);

    try {
        const responseBody = await apiRequest(
            REVIEWS_API_URL +
            "/" +
            encodeURIComponent(selectedReview.id) +
            "/decision",
            {
                method: "POST",
                body: JSON.stringify({
                    decision: decision,
                    comment: comment || null
                })
            }
        );

        const decisionResult = responseBody.data;

        if (!decisionResult) {
            throw new Error(
                "Review decision response does not contain data."
            );
        }

        const returnedStatus =
            firstDefined(
                decisionResult.status,
                decisionResult.decision,
                decision
            );

        selectedReview.status = returnedStatus;

        document.getElementById("reviewComment").value = "";

        await Promise.all([
            loadReviewDashboard(),
            searchReviews(currentPage)
        ]);

        await fetchReviewDetail(selectedReview.id);

        showToast(
            responseBody.message ||
            decisionResult.message ||
            "Review decision submitted successfully."
        );
    } catch (error) {
        console.error("Unable to submit review decision:", error);
        showToast(error.message || "Unable to submit review decision.");
    } finally {
        if (
            selectedReview &&
            (
                selectedReview.status === "PENDING" ||
                selectedReview.status === "CHANGES_REQUESTED"
            )
        ) {
            setButtonsDisabled(decisionButtons, false);
        }
    }
}

function openReviewSource(review) {
    if (review.sourcePageUrl) {
        window.location.href = review.sourcePageUrl;
        return;
    }

    const sourceUrl =
        buildSourcePageUrl(
            review.reviewType,
            review.sourceId
        );

    if (!sourceUrl) {
        showToast("Source page is not available for this review.");
        return;
    }

    window.location.href = sourceUrl;
}

function buildSourcePageUrl(reviewType, sourceId) {
    if (sourceId === null || sourceId === undefined) {
        return null;
    }

    switch (reviewType) {
        case "CONTRACT":
        case "CONTRACT_HIGHLIGHT":
        case "CONTRACT_LOT":
        case "LOT":
        case "HIGHLIGHT":
            return (
                "ContractDocumentAnalysis.html?contractId=" +
                encodeURIComponent(sourceId)
            );

        case "PROPOSAL":
        case "PROPOSAL_PRICING":
        case "PROPOSAL_DOCUMENT":
        case "BID_SUBMISSION":
            return (
                "ProposalReview.html?proposalId=" +
                encodeURIComponent(sourceId)
            );

        default:
            return null;
    }
}

async function apiRequest(url, options) {
    const token = getAccessToken();

    const requestOptions = {
        method: options && options.method
            ? options.method
            : "GET",
        headers: {
            "Accept": "application/json"
        }
    };

    if (options && options.body !== undefined) {
        requestOptions.body = options.body;
        requestOptions.headers["Content-Type"] = "application/json";
    }

    if (token) {
        requestOptions.headers["Authorization"] =
            "Bearer " + token;
    }

    const response = await fetch(url, requestOptions);

    let responseBody = null;

    const contentType =
        response.headers.get("content-type") || "";

    if (contentType.includes("application/json")) {
        responseBody = await response.json();
    } else {
        const responseText = await response.text();

        responseBody = {
            success: response.ok,
            message: responseText || response.statusText,
            data: null
        };
    }

    if (!response.ok) {
        throw new Error(
            firstDefined(
                responseBody && responseBody.message,
                "Request failed with status " + response.status
            )
        );
    }

    if (!responseBody) {
        throw new Error("Backend returned an empty response.");
    }

    if (responseBody.success === false) {
        throw new Error(
            responseBody.message || "Backend request failed."
        );
    }

    return responseBody;
}

function getAccessToken() {
    if (
        window.keycloak &&
        window.keycloak.token
    ) {
        return window.keycloak.token;
    }

    return (
        localStorage.getItem("access_token") ||
        sessionStorage.getItem("access_token") ||
        ""
    );
}

function getTypeClass(type) {
    switch (type) {
        case "CONTRACT":
            return "contract";

        case "PROPOSAL":
        case "PROPOSAL_PRICING":
        case "PROPOSAL_DOCUMENT":
        case "BID_SUBMISSION":
            return "proposal";

        case "LOT":
        case "CONTRACT_LOT":
            return "lot";

        case "HIGHLIGHT":
        case "CONTRACT_HIGHLIGHT":
            return "highlight";

        default:
            return "highlight";
    }
}

function getTypeIcon(type) {
    switch (type) {
        case "CONTRACT":
            return "📂";

        case "PROPOSAL":
        case "PROPOSAL_PRICING":
        case "PROPOSAL_DOCUMENT":
        case "BID_SUBMISSION":
            return "📝";

        case "LOT":
        case "CONTRACT_LOT":
            return "📦";

        case "HIGHLIGHT":
        case "CONTRACT_HIGHLIGHT":
            return "🔎";

        default:
            return "✅";
    }
}

function getStatusClass(status) {
    switch (status) {
        case "PENDING":
            return "pending";

        case "APPROVED":
            return "approved";

        case "CHANGES_REQUESTED":
            return "changes";

        case "REJECTED":
            return "rejected";

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

        case "CHANGES_REQUESTED":
            return "🔁";

        case "REJECTED":
            return "❌";

        default:
            return "⏳";
    }
}

function getPriorityClass(priority) {
    switch (priority) {
        case "HIGH":
            return "high";

        case "MEDIUM":
            return "medium";

        case "LOW":
            return "low";

        default:
            return "medium";
    }
}

function getHistoryIcon(title) {
    const normalizedTitle =
        String(title || "").toUpperCase();

    if (normalizedTitle.includes("APPROV")) {
        return "✅";
    }

    if (normalizedTitle.includes("REJECT")) {
        return "❌";
    }

    if (
        normalizedTitle.includes("CHANGE") ||
        normalizedTitle.includes("REVISION")
    ) {
        return "🔁";
    }

    if (
        normalizedTitle.includes("ASSIGN") ||
        normalizedTitle.includes("CREATE")
    ) {
        return "👤";
    }

    return "🕘";
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

function escapeHtml(value) {
    if (
        value === null ||
        value === undefined
    ) {
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
    document.getElementById("loadingIndicator").style.display =
        isLoading ? "flex" : "none";
}

function showToast(message) {
    const toast =
        document.getElementById("toast");

    toast.textContent = message;
    toast.style.display = "block";

    window.clearTimeout(showToast.timeoutId);

    showToast.timeoutId =
        window.setTimeout(function () {
            toast.style.display = "none";
        }, 3000);
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

function firstDefined() {
    for (
        let index = 0;
        index < arguments.length;
        index++
    ) {
        const value = arguments[index];

        if (
            value !== undefined &&
            value !== null
        ) {
            return value;
        }
    }

    return null;
}

function toNumber(value, fallback) {
    const numberValue = Number(value);

    return Number.isFinite(numberValue)
        ? numberValue
        : fallback;
}

function setButtonsDisabled(buttons, disabled) {
    buttons.forEach(function (button) {
        button.disabled = disabled;
    });
}