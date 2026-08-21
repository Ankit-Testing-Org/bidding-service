'use strict';
const API_BASE_URL = "/api/contracts";

const urlParams =
    new URLSearchParams(window.location.search);

const contractId =
    urlParams.get("contractId") || "1";

let lots = [];
let selectedLot = null;
let currentAnalysisResult = null;

document.addEventListener("DOMContentLoaded", async function () {
    await refreshLots();
});

async function apiRequest(url, method = "GET", body = null) {
    const options = {
        method: method,
        credentials: "include",
        headers: {
            "Content-Type": "application/json"
        }
    };

    if (body !== null) {
        options.body = JSON.stringify(body);
    }

    const response = await fetch(url, options);

    if (!response.ok) {
        const text = await response.text();
        throw new Error(text || "API request failed");
    }

    return response.json();
}

function unwrapApiResponse(response) {
    if (response && Object.prototype.hasOwnProperty.call(response, "data")) {
        return response.data;
    }

    return response;
}

async function refreshLots() {
    try {
        const response =
            await apiRequest(
                `${API_BASE_URL}/${contractId}/fetch/contract/lots`
            );

        lots =
            unwrapApiResponse(response) || [];

        renderLots();
        renderDashboardStats();

        if (lots.length > 0) {
            selectLot(lots[0].id);
        } else {
            clearSelectedLot();
        }

        showToast("Lots loaded");

    } catch (error) {
        console.error(error);
        showToast("Failed to load lots");
    }
}

function renderLots() {
    const lotList =
        document.getElementById("lotList");

    if (!lots.length) {
        lotList.innerHTML =
            `<div class="empty-state">No lots found.</div>`;
        return;
    }

    lotList.innerHTML =
        lots.map(function (lot) {
            return buildLotCard(lot);
        }).join("");
}

function buildLotCard(lot) {
    const activeClass =
        selectedLot && selectedLot.id === lot.id
            ? "active"
            : "";

    const statusClass =
        getParticipationStatusClass(lot.participationStatus);

    const statusLabel =
        lot.participationStatus || "UNDECIDED";

    return `
            <div class="lot ${activeClass}" onclick="selectLot(${lot.id})">
                <div class="lot-top">
                    <div class="icon">${getLotIcon(lot.lotName)}</div>

                    <div>
                        <div class="title">${escapeHtml(lot.lotName || "Unnamed Lot")}</div>
                        <div class="value">${formatMoney(lot.valuation)}</div>
                    </div>
                </div>

                <div class="lot-footer">
                    <span>Lot ${escapeHtml(lot.lotNumber || "-")}</span>
                    <span class="${statusClass}">${escapeHtml(statusLabel)}</span>
                </div>
            </div>
        `;
}

function selectLot(lotId) {
    selectedLot =
        lots.find(function (lot) {
            return lot.id === lotId;
        });

    renderLots();
    renderSelectedLot();
    resetAnalysisView();
}

function clearSelectedLot() {
    selectedLot = null;
    document.getElementById("lotTitle").innerText = "No lot selected";
}

function renderSelectedLot() {
    if (!selectedLot) {
        clearSelectedLot();
        return;
    }

    document.getElementById("lotTitle").innerText =
        selectedLot.lotName || "Unnamed Lot";

    document.getElementById("lotNumber").innerText =
        selectedLot.lotNumber || "-";

    document.getElementById("lotValue").innerText =
        formatMoney(selectedLot.valuation);

    document.getElementById("lotDescription").innerText =
        selectedLot.description || "-";

    document.getElementById("lotPages").innerText =
        buildPageRange(selectedLot);

    document.getElementById("lotStatus").innerText =
        selectedLot.participationStatus || "UNDECIDED";
}

function renderDashboardStats() {
    const total =
        lots.length;

    const qualified =
        lots.filter(function (lot) {
            return lot.participationStatus === "QUALIFIED";
        }).length;

    document.getElementById("totalLots").innerText =
        total;

    document.getElementById("qualifiedLots").innerText =
        qualified;

    document.getElementById("overallBidScore").innerText =
        "-";

    document.getElementById("winProbability").innerText =
        "-";

    document.getElementById("projectSubtitle").innerText =
        `${total} Lots | Qualified ${qualified}`;

    document.getElementById("projectStatus").innerText =
        "Status: Lots Loaded";
}

async function qualifySelectedLot() {
    if (!selectedLot) {
        showToast("Please select a lot first");
        return;
    }

    try {
        const response =
            await apiRequest(
                `${API_BASE_URL}/${contractId}/lots/${encodeURIComponent(selectedLot.lotNumber)}/qualify`
            );

        updateLotInState(
            unwrapApiResponse(response)
        );

        showToast("Lot qualified");

    } catch (error) {
        console.error(error);
        showToast("Failed to qualify lot");
    }
}

async function unqualifySelectedLot() {
    if (!selectedLot) {
        showToast("Please select a lot first");
        return;
    }

    try {
        const response =
            await apiRequest(
                `${API_BASE_URL}/${contractId}/lots/${encodeURIComponent(selectedLot.lotNumber)}/unqualify`
            );

        updateLotInState(
            unwrapApiResponse(response)
        );

        showToast("Lot unqualified");

    } catch (error) {
        console.error(error);
        showToast("Failed to unqualify lot");
    }
}

function updateLotInState(updatedLot) {
    lots =
        lots.map(function (lot) {
            if (lot.id === updatedLot.id) {
                return updatedLot;
            }

            return lot;
        });

    selectedLot =
        updatedLot;

    renderLots();
    renderSelectedLot();
    renderDashboardStats();
}

async function analyseSelectedLot(reanalyse) {
    if (!selectedLot) {
        showToast("Please select a lot first");
        return;
    }

    try {
        const response =
            await apiRequest(
                `${API_BASE_URL}/lots/${selectedLot.id}/analyse?reanalyse=${reanalyse}`,
                "GET"
            );

        currentAnalysisResult =
            unwrapApiResponse(response);

        renderAnalysisResult(currentAnalysisResult);

        showToast("Lot analysis completed");

    } catch (error) {
        console.error(error);
        showToast("Failed to analyse lot");
    }
}

async function openReanalysePrompt() {
    if (!selectedLot) {
        showToast("Please select a lot first");
        return;
    }

    const comment =
        prompt("Why should this lot be reanalysed?");

    if (!comment) {
        return;
    }

    try {
        const response =
            await apiRequest(
                `${API_BASE_URL}/${selectedLot.id}/reanalyse`,
                "POST",
                {
                    userComment: comment
                }
            );

        currentAnalysisResult =
            unwrapApiResponse(response);

        renderAnalysisResult(currentAnalysisResult);

        showToast("Lot reanalysis completed");

    } catch (error) {
        console.error(error);
        showToast("Failed to reanalyse lot");
    }
}

function renderAnalysisResult(result) {
    if (!result) {
        resetAnalysisView();
        return;
    }

    const analysis =
        result.analysis || {};

    const highlights =
        result.highlights || [];

    document.getElementById("score").innerText =
        valueOrDash(formatPercent(analysis.bidScore));

    document.getElementById("overallBidScore").innerText =
        valueOrDash(formatPercent(analysis.bidScore));

    document.getElementById("winProbability").innerText =
        valueOrDash(formatPercent(analysis.winProbability));

    renderRisk(analysis.overallRiskLevel);

    document.getElementById("recommendationText").innerText =
        analysis.recommendation || "No recommendation available.";

    renderHighlightGroups(highlights);

    if (analysis.executiveSummary) {
        addAiMessage(
            "Executive Summary:\n\n" + analysis.executiveSummary
        );
    }
}

function resetAnalysisView() {
    currentAnalysisResult = null;

    document.getElementById("score").innerText = "-";
    document.getElementById("risk").innerText = "Not Analysed";
    document.getElementById("risk").className = "risk";
    document.getElementById("recommendationText").innerText =
        "No recommendation available.";

    document.getElementById("strengthsList").innerHTML =
        `<li class="empty-state">No strengths available.</li>`;

    document.getElementById("weaknessesList").innerHTML =
        `<li class="empty-state">No weaknesses available.</li>`;

    document.getElementById("missingRequirementsList").innerHTML =
        `<div class="empty-state">No missing requirements available.</div>`;

    document.getElementById("findingsTableBody").innerHTML =
        `<tr><td colspan="6">No findings available.</td></tr>`;
}

function renderRisk(riskLevel) {
    const risk =
        document.getElementById("risk");

    risk.className =
        "risk";

    if (!riskLevel) {
        risk.innerText =
            "Not Analysed";
        return;
    }

    risk.innerText =
        riskLevel + " Risk";

    if (riskLevel === "LOW") {
        risk.classList.add("low");
    }

    if (riskLevel === "MEDIUM") {
        risk.classList.add("medium");
    }

    if (riskLevel === "HIGH") {
        risk.classList.add("high");
    }
}

function renderHighlightGroups(highlights) {
    const strengths =
        filterByCategory(highlights, "STRENGTH");

    const weaknesses =
        filterByCategory(highlights, "WEAKNESS")
            .concat(filterByCategory(highlights, "CAPABILITY_GAP"));

    const missing =
        filterByCategory(highlights, "MISSING_REQUIREMENT");

    renderList(
        "strengthsList",
        strengths,
        "✅",
        "No strengths available."
    );

    renderList(
        "weaknessesList",
        weaknesses,
        "❌",
        "No weaknesses available."
    );

    renderMissingRequirements(missing);
    renderFindingsTable(highlights);
}

function filterByCategory(highlights, category) {
    return highlights.filter(function (highlight) {
        return highlight.category === category;
    });
}

function renderList(elementId, items, icon, emptyText) {
    const element =
        document.getElementById(elementId);

    if (!items.length) {
        element.innerHTML =
            `<li class="empty-state">${escapeHtml(emptyText)}</li>`;
        return;
    }

    element.innerHTML =
        items.map(function (item) {
            return `
                    <li>
                        ${icon} ${escapeHtml(item.title || "Untitled")}
                    </li>
                `;
        }).join("");
}

function renderMissingRequirements(items) {
    const element =
        document.getElementById("missingRequirementsList");

    if (!items.length) {
        element.innerHTML =
            `<div class="empty-state">No missing requirements available.</div>`;
        return;
    }

    element.innerHTML =
        items.map(function (item) {
            return `
                    <div class="missing-item">
                        ❌ ${escapeHtml(item.title || "Missing requirement")}
                    </div>
                `;
        }).join("");
}

function renderFindingsTable(highlights) {
    const body =
        document.getElementById("findingsTableBody");

    if (!highlights.length) {
        body.innerHTML =
            `<tr><td colspan="6">No findings available.</td></tr>`;
        return;
    }

    body.innerHTML =
        highlights.map(function (item) {
            return `
                    <tr>
                        <td>${escapeHtml(item.category || "-")}</td>
                        <td>
                            <strong>${escapeHtml(item.title || "-")}</strong>
                            <br>
                            <span>${escapeHtml(item.description || "")}</span>
                        </td>
                        <td>${escapeHtml(item.riskLevel || "-")}</td>
                        <td>${item.pageNumber || "-"}</td>
                        <td>${item.bidCapable === true ? "Yes" : item.bidCapable === false ? "No" : "-"}</td>
                        <td>${escapeHtml(item.recommendedAction || "-")}</td>
                    </tr>
                `;
        }).join("");
}

function askSuggestion(element) {
    const text =
        element.innerText.trim();

    addUserMessage(text);
    addAssistantAnswer(text);
}

function sendMessage(event) {
    if (event.key !== "Enter") {
        return;
    }

    const input =
        document.getElementById("chatInput");

    const text =
        input.value.trim();

    if (!text) {
        return;
    }

    addUserMessage(text);
    addAssistantAnswer(text);
    input.value = "";
}

function addAssistantAnswer(question) {
    const normalized =
        question.toLowerCase();

    if (!currentAnalysisResult) {
        addAiMessage("Please analyse the selected lot first.");
        return;
    }

    const analysis =
        currentAnalysisResult.analysis || {};

    const highlights =
        currentAnalysisResult.highlights || [];

    if (normalized.includes("missing")) {
        const missing =
            filterByCategory(highlights, "MISSING_REQUIREMENT");

        if (!missing.length) {
            addAiMessage("No missing requirements were found for this lot.");
            return;
        }

        addAiMessage(
            "Missing Requirements:\n\n" +
            missing.map(function (item) {
                return "• " + item.title;
            }).join("\n")
        );

        return;
    }

    if (normalized.includes("risk")) {
        addAiMessage(
            "Risk Level: " + (analysis.overallRiskLevel || "-") +
            "\n\nRecommendation:\n" +
            (analysis.recommendation || "-")
        );

        return;
    }

    if (normalized.includes("bid")) {
        addAiMessage(
            "Recommended To Bid: " +
            (analysis.recommendedToBid === true ? "Yes" : "No") +
            "\n\nBid Score: " +
            formatPercent(analysis.bidScore) +
            "\nWin Probability: " +
            formatPercent(analysis.winProbability)
        );

        return;
    }

    if (normalized.includes("summary")) {
        addAiMessage(
            analysis.executiveSummary ||
            "No executive summary is available."
        );

        return;
    }

    addAiMessage(
        "I can answer based on the current lot analysis, highlights, missing requirements, risk level, and recommendation."
    );
}

function addUserMessage(message) {
    const chatBody =
        document.getElementById("chatBody");

    chatBody.insertAdjacentHTML(
        "beforeend",
        `<div class="message user">${escapeHtml(message)}</div>`
    );

    chatBody.scrollTop =
        chatBody.scrollHeight;
}

function addAiMessage(message) {
    const chatBody =
        document.getElementById("chatBody");

    chatBody.insertAdjacentHTML(
        "beforeend",
        `<div class="message ai">${escapeHtml(message)}</div>`
    );

    chatBody.scrollTop =
        chatBody.scrollHeight;
}

function getLotIcon(lotName) {
    const name =
        String(lotName || "").toLowerCase();

    if (name.includes("medicine") || name.includes("pharma")) {
        return "💊";
    }

    if (name.includes("bed") || name.includes("furniture")) {
        return "🛏️";
    }

    if (name.includes("device") || name.includes("equipment")) {
        return "🏥";
    }

    if (name.includes("lab")) {
        return "🧪";
    }

    if (name.includes("ambulance")) {
        return "🚑";
    }

    if (name.includes("it") || name.includes("system")) {
        return "💻";
    }

    return "📦";
}

function getParticipationStatusClass(status) {
    if (status === "QUALIFIED") {
        return "success";
    }

    if (status === "UNQUALIFIED") {
        return "danger";
    }

    return "warning";
}

function buildPageRange(lot) {
    if (lot.startPage && lot.endPage) {
        return "Pages " + lot.startPage + " - " + lot.endPage;
    }

    if (lot.startPage) {
        return "Page " + lot.startPage;
    }

    return "-";
}

function formatMoney(value) {
    if (value === null || value === undefined) {
        return "-";
    }

    return "€" + Number(value).toLocaleString();
}

function formatPercent(value) {
    if (value === null || value === undefined) {
        return "-";
    }

    return Math.round(Number(value)) + "%";
}

function valueOrDash(value) {
    if (value === null || value === undefined || value === "") {
        return "-";
    }

    return value;
}

function escapeHtml(value) {
    return String(value || "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
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

function navigateToProposal() {
    window.location.href =
        `/bids/create?contractId=${contractId}`;
}