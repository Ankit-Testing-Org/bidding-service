'use strict';

const API_BASE_URL = "/api/contracts";

const urlParams = new URLSearchParams(window.location.search);

const contractId =
    urlParams.get("contractId") || "1";

const pdfFile =
    urlParams.get("pdf") || "sample-contract.pdf";

let currentPage = 1;
let currentPdfTitle = "Contract Document";
let highlights = [];
let pendingReview = null;

document.addEventListener("DOMContentLoaded", async function () {
    updatePdfViewer();
    await loadHighlights();
});

/*
    Keycloak / Bearer token handling
*/

async function getAccessToken() {
    if (window.keycloak && window.keycloak.token) {
        if (typeof window.keycloak.updateToken === "function") {
            try {
                await window.keycloak.updateToken(30);
            } catch (error) {
                console.error("Unable to refresh Keycloak token", error);
            }
        }

        return window.keycloak.token;
    }

    return localStorage.getItem("access_token")
        || sessionStorage.getItem("access_token")
        || "";
}

async function apiRequest(url, method = "GET", body = null) {
    const token = await getAccessToken();

    if (!token) {
        showToast("Missing Keycloak access token");
        throw new Error("Missing Keycloak access token");
    }

    const options = {
        method: method,
        headers: {
            "Authorization": "Bearer " + token,
            "Content-Type": "application/json"
        }
    };

    if (body !== null) {
        options.body = JSON.stringify(body);
    }

    const response = await fetch(url, options);

    if (response.status === 401) {
        showToast("Session expired. Please login again.");
        throw new Error("Unauthorized");
    }

    if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText || "API request failed");
    }

    return response.json();
}

/*
    Backend integration
*/

async function loadHighlights() {
    try {
        const response =
            await apiRequest(
                `${API_BASE_URL}/${contractId}/fetch/contract/highlights`
            );

        highlights = response.data || [];

        renderAllHighlightSections();
        renderReviewHistoryFromHighlights();

        showToast("Contract highlights loaded");

    } catch (error) {
        console.error(error);
        showToast("Failed to load highlights");
    }
}

async function approveHighlight(highlightId) {
    try {
        const response =
            await apiRequest(
                `${API_BASE_URL}/contract/highlights/${highlightId}/approve`,
                "POST"
            );

        updateHighlightInState(response.data);
        renderAllHighlightSections();
        renderReviewHistoryFromHighlights();

        showToast("Highlight approved");

    } catch (error) {
        console.error(error);
        showToast("Failed to approve highlight");
    }
}

async function submitReview() {
    if (!pendingReview) {
        return;
    }

    const comment =
        document.getElementById("reviewComment").value.trim();

    try {
        let endpoint;

        if (pendingReview.status === "REJECTED") {
            endpoint =
                `${API_BASE_URL}/contract/highlights/${pendingReview.highlightId}/reject`;
        } else if (pendingReview.status === "REANALYSE") {
            endpoint =
                `${API_BASE_URL}/contract/highlights/${pendingReview.highlightId}/reanalyse`;
        } else {
            endpoint =
                `${API_BASE_URL}/contract/highlights/${pendingReview.highlightId}/approve`;
        }

        const response =
            await apiRequest(
                endpoint,
                "POST",
                {
                    comment: comment
                }
            );

        updateHighlightInState(response.data);
        renderAllHighlightSections();
        renderReviewHistoryFromHighlights();

        closeReviewModal();
        showToast("Review saved");

    } catch (error) {
        console.error(error);
        showToast("Failed to save review");
    }
}

function updateHighlightInState(updatedHighlight) {
    highlights =
        highlights.map(function (item) {
            if (item.id === updatedHighlight.id) {
                return updatedHighlight;
            }

            return item;
        });
}

/*
    Rendering
*/

function renderAllHighlightSections() {
    renderCategory(
        "EXECUTIVE_SUMMARY",
        "executiveSummaryContainer",
        "executiveSummaryCount",
        "Insights"
    );

    renderCategory(
        "TERMS_AND_CONDITIONS",
        "termsContainer",
        "termsCount",
        "Findings"
    );

    renderCategory(
        "COMPLIANCE_REQUIREMENT",
        "complianceContainer",
        "complianceCount",
        "Requirements"
    );

    renderCategory(
        "MANDATORY_ACTION",
        "actionsContainer",
        "actionsCount",
        "Actions"
    );

    renderCategory(
        "AI_RECOMMENDATION",
        "recommendationsContainer",
        "recommendationsCount",
        "Recommendations"
    );
}

function renderCategory(category, containerId, countId, label) {
    const container =
        document.getElementById(containerId);

    const items =
        highlights.filter(function (highlight) {
            return highlight.category === category;
        });

    document.getElementById(countId).innerText =
        items.length + " " + label;

    if (items.length === 0) {
        container.innerHTML =
            `
                <div class="finding-tile">
                    <div class="finding-header">
                        <h4>No items found</h4>
                        <span class="badge badge-gray">Empty</span>
                    </div>
                    <div class="finding-description">
                        No AI highlights are available for this section.
                    </div>
                </div>
                `;
        return;
    }

    container.innerHTML =
        items.map(buildHighlightTile).join("");
}

function buildHighlightTile(highlight) {
    const reviewClass =
        getReviewClass(highlight.reviewStatus);

    const riskClass =
        getRiskBadgeClass(highlight.riskLevel);

    const safeTitle =
        escapeHtml(highlight.title || "Untitled");

    const safeDescription =
        escapeHtml(highlight.description || "");

    const safeReference =
        escapeHtml(highlight.reference || "");

    const pageNumber =
        highlight.pageNumber || 1;

    return `
            <div class="finding-tile ${reviewClass}" id="tile-${highlight.id}">
                <div class="finding-header">
                    <h4>${safeTitle}</h4>
                    <span class="${riskClass}">
                        ${escapeHtml(highlight.riskLevel || "UNKNOWN")}
                    </span>
                </div>

                <div class="finding-description">
                    ${safeDescription}
                </div>

                <button class="expand-btn" onclick="toggleTile(this)">
                    Show More ▼
                </button>

                <div class="reference">
                    Page ${pageNumber}${safeReference ? " | " + safeReference : ""}
                </div>

                <div class="review-status" id="status-${highlight.id}">
                    Review Status: ${escapeHtml(highlight.reviewStatus || "PENDING")}
                </div>

                <div class="review-actions">
                    <button
                            class="review-btn open"
                            onclick="openHighlight(${pageNumber}, '${escapeJs(highlight.title || "Highlight")}')">
                        Open in PDF
                    </button>

                    <button
                            class="review-btn correct"
                            onclick="approveHighlight(${highlight.id})">
                        ✓ Correct
                    </button>

                    <button
                            class="review-btn incorrect"
                            onclick="openReviewModal(${highlight.id}, '${escapeJs(highlight.title || "Highlight")}', 'REJECTED')">
                        ✗ Incorrect
                    </button>

                    <button
                            class="review-btn reanalyse"
                            onclick="openReviewModal(${highlight.id}, '${escapeJs(highlight.title || "Highlight")}', 'REANALYSE')">
                        ↻ Reanalyse
                    </button>

                    <button
                            class="review-btn why"
                            onclick="askAiWhy('${escapeJs(highlight.title || "Highlight")}')">
                        Why?
                    </button>
                </div>
            </div>
        `;
}

function renderReviewHistoryFromHighlights() {
    const allHistory = [];

    highlights.forEach(function (highlight) {
        const history = highlight.reviewHistory || [];

        history.forEach(function (item) {
            allHistory.push({
                highlightTitle: highlight.title,
                reviewStatus: item.reviewStatus,
                reviewedBy: item.reviewedBy,
                reviewedAt: item.reviewedAt,
                reviewComment: item.reviewComment
            });
        });
    });

    const feedbackList =
        document.getElementById("feedbackList");

    document.getElementById("feedbackCount").innerText =
        allHistory.length + " Signals";

    if (allHistory.length === 0) {
        feedbackList.innerHTML =
            `
                <div class="feedback-row">
                    <div>
                        <strong>No feedback captured yet</strong>
                        <span>Review history appears here after user decisions.</span>
                    </div>
                    <span class="badge badge-gray">Pending</span>
                </div>
                `;
        return;
    }

    feedbackList.innerHTML =
        allHistory.map(function (item) {
            return `
                    <div class="feedback-row">
                        <div>
                            <strong>${escapeHtml(item.highlightTitle || "Highlight")}</strong>
                            <span>
                                ${escapeHtml(item.reviewComment || "No comment")}
                                <br>
                                ${escapeHtml(item.reviewedBy || "Unknown reviewer")}
                                ${item.reviewedAt ? " | " + escapeHtml(item.reviewedAt) : ""}
                            </span>
                        </div>
                        <span class="${getStatusBadgeClass(item.reviewStatus)}">
                            ${escapeHtml(item.reviewStatus || "PENDING")}
                        </span>
                    </div>
                `;
        }).join("");
}

/*
    PDF
*/

function openHighlight(pageNumber, title) {
    currentPage = pageNumber || 1;
    currentPdfTitle = title || "Contract Document";
    updatePdfViewer();
    showToast("Opened " + currentPdfTitle + " on page " + currentPage);
}

function goToPreviousPage() {
    if (currentPage > 1) {
        currentPage--;
        currentPdfTitle = "Manual page navigation";
        updatePdfViewer();
    }
}

function goToNextPage() {
    currentPage++;
    currentPdfTitle = "Manual page navigation";
    updatePdfViewer();
}

function updatePdfViewer() {
    document.getElementById("contractPdfViewer").src =
        pdfFile + "#page=" + currentPage;

    document.getElementById("pageTitle").innerText =
        currentPdfTitle + " | Page " + currentPage;
}

function openPdfInNewTab() {
    window.open(pdfFile + "#page=" + currentPage, "_blank");
}

/*
    Review modal
*/

function openReviewModal(highlightId, title, status) {
    pendingReview = {
        highlightId: highlightId,
        title: title,
        status: status
    };

    document.getElementById("reviewModalTitle").innerText =
        getStatusLabel(status) + ": " + title;

    document.getElementById("reviewModalSubtitle").innerText =
        "This feedback will be saved and returned in review history.";

    const textarea =
        document.getElementById("reviewComment");

    textarea.value = "";

    if (status === "REJECTED") {
        textarea.placeholder =
            "Example: Incorrect. This clause is informational only.";
    } else if (status === "REANALYSE") {
        textarea.placeholder =
            "Example: Reanalyse because the risk level or category is incorrect.";
    }

    document.getElementById("reviewModalBackdrop").style.display = "flex";
}

function closeReviewModal() {
    pendingReview = null;
    document.getElementById("reviewModalBackdrop").style.display = "none";
}

/*
    Accordions and interaction
*/

function toggleAccordion(headerElement) {
    const accordionItem = headerElement.parentElement;
    accordionItem.classList.toggle("active");
}

function toggleTile(button) {
    const tile =
        button.closest(".finding-tile");

    tile.classList.toggle("expanded");

    button.innerText =
        tile.classList.contains("expanded")
            ? "Show Less ▲"
            : "Show More ▼";
}

/*
    Assistant mock
*/

function askSuggestedQuestion(question) {
    document.getElementById("userQuestion").value = question;
    sendQuestion();
}

function sendQuestion() {
    const input =
        document.getElementById("userQuestion");

    const question =
        input.value.trim();

    if (!question) {
        return;
    }

    addUserMessage(question);

    setTimeout(function () {
        addAiMessage(findAnswer(question.toLowerCase()));
    }, 300);

    input.value = "";
}

function findAnswer(question) {
    if (question.includes("feedback") || question.includes("reviewer")) {
        return buildFeedbackAnswer();
    }

    if (question.includes("selected page") || question.includes("current page")) {
        return `
                <div class="answer-section">
                    <strong>Current PDF Context</strong>
                    You are currently viewing <strong>${escapeHtml(currentPdfTitle)}</strong>
                    on page ${currentPage}.
                </div>
            `;
    }

    if (question.includes("risk")) {
        return `
                <div class="answer-section">
                    <strong>Major Risks</strong>
                    The visible AI findings marked as HIGH or MEDIUM risk are the major risk indicators.
                    Use the review buttons to approve, reject, or request reanalysis.
                </div>
            `;
    }

    return `
            <div class="answer-section">
                <strong>AI Assistant</strong>
                In production, send this question with contractId, currentPage, selected highlight, and review history to your backend AI endpoint.
            </div>
        `;
}

function buildFeedbackAnswer() {
    const total =
        highlights.reduce(function (count, highlight) {
            return count + ((highlight.reviewHistory || []).length);
        }, 0);

    return `
            <div class="answer-section">
                <strong>Reviewer Feedback</strong>
                ${total === 0
        ? "No review feedback has been captured yet."
        : "There are " + total + " review history entries captured."}
            </div>
        `;
}

function addUserMessage(message) {
    const chatMessages =
        document.getElementById("chatMessages");

    const messageDiv =
        document.createElement("div");

    messageDiv.className =
        "message user-message";

    messageDiv.innerText =
        message;

    chatMessages.appendChild(messageDiv);
    scrollChatToBottom();
}

function addAiMessage(messageHtml) {
    const chatMessages =
        document.getElementById("chatMessages");

    const messageDiv =
        document.createElement("div");

    messageDiv.className =
        "message ai-message";

    messageDiv.innerHTML =
        messageHtml;

    chatMessages.appendChild(messageDiv);
    scrollChatToBottom();
}

function askAiWhy(itemTitle) {
    addUserMessage("Why did AI identify this: " + itemTitle + "?");

    setTimeout(function () {
        addAiMessage(`
                <div class="answer-section">
                    <strong>Why AI identified this</strong>
                    AI classified <strong>${escapeHtml(itemTitle)}</strong> based on extracted contract text, risk indicators,
                    obligations, compliance language, deadlines, penalties, or bidder responsibilities.
                </div>
            `);
    }, 300);
}

function scrollChatToBottom() {
    const chatMessages =
        document.getElementById("chatMessages");

    chatMessages.scrollTop =
        chatMessages.scrollHeight;
}

function handleEnter(event) {
    if (event.key === "Enter") {
        sendQuestion();
    }
}

/*
    Helpers
*/

function getReviewClass(status) {
    if (status === "APPROVED") {
        return "review-approved";
    }

    if (status === "REJECTED") {
        return "review-rejected";
    }

    if (status === "REANALYSE" || status === "REANALYSE_REQUESTED") {
        return "review-reanalyse";
    }

    return "";
}

function getRiskBadgeClass(riskLevel) {
    if (riskLevel === "HIGH") {
        return "badge badge-red";
    }

    if (riskLevel === "MEDIUM") {
        return "badge badge-yellow";
    }

    if (riskLevel === "LOW") {
        return "badge badge-green";
    }

    return "badge badge-gray";
}

function getStatusBadgeClass(status) {
    if (status === "APPROVED") {
        return "badge badge-green";
    }

    if (status === "REJECTED") {
        return "badge badge-red";
    }

    if (status === "REANALYSE" || status === "REANALYSE_REQUESTED") {
        return "badge badge-purple";
    }

    return "badge badge-gray";
}

function getStatusLabel(status) {
    if (status === "APPROVED") {
        return "Correct";
    }

    if (status === "REJECTED") {
        return "Incorrect";
    }

    if (status === "REANALYSE" || status === "REANALYSE_REQUESTED") {
        return "Reanalyse Requested";
    }

    return "Pending";
}

function escapeHtml(value) {
    return String(value || "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

function escapeJs(value) {
    return String(value || "")
        .replaceAll("\\", "\\\\")
        .replaceAll("'", "\\'")
        .replaceAll('"', '\\"')
        .replaceAll("\n", " ")
        .replaceAll("\r", " ");
}

function showToast(message) {
    const toast =
        document.getElementById("toast");

    toast.innerText =
        message;

    toast.style.display =
        "block";

    setTimeout(function () {
        toast.style.display =
            "none";
    }, 2500);
}

function navigateTo(url) {
    window.location.href = url;
}