'use strict';

const API_BASE_URL = '/api/contracts';

const urlParams =
    new URLSearchParams(window.location.search);

const contractId =
    urlParams.get('contractId') || '1';

let lots = [];
let selectedLot = null;
let currentAnalysisResult = null;
let proposalReadiness = null;
let toastTimeoutId = null;

/*
 * Your current controller uses GET for qualification and
 * unqualification. If those controller methods are changed to
 * @PostMapping, change these two constants to "POST".
 */
const QUALIFICATION_HTTP_METHOD = 'GET';
const UNQUALIFICATION_HTTP_METHOD = 'GET';

document.addEventListener(
    'DOMContentLoaded',
    initializeLotWorkspace
);

async function initializeLotWorkspace() {
    configureInitialPageState();
    await refreshLots();
}

function configureInitialPageState() {
    setButtonDisabled(
        'generateProposalButton',
        true
    );

    setSelectedLotButtonsDisabled(true);
    setAnalysisReviewControlsDisabled(true);

    setText(
        'projectStatus',
        'Status: Loading lots'
    );

    setText(
        'proposalReadinessStatus',
        'Proposal readiness: Not checked'
    );
}

/* ================================================================
   API
   ================================================================ */

async function apiRequest(
    url,
    method = 'GET',
    body = null
) {
    const headers = {
        Accept: 'application/json'
    };

    const accessToken =
        resolveAccessToken();

    if (accessToken) {
        headers.Authorization =
            `Bearer ${accessToken}`;
    }

    const options = {
        method,
        credentials: 'include',
        headers
    };

    if (body !== null && method !== 'GET') {
        headers['Content-Type'] =
            'application/json';

        options.body =
            JSON.stringify(body);
    }

    const response =
        await fetch(url, options);

    const responseBody =
        await parseResponseBody(response);

    if (!response.ok) {
        throw new Error(
            resolveApiErrorMessage(
                responseBody,
                response.status
            )
        );
    }

    return responseBody;
}

function resolveAccessToken() {
    if (
        window.keycloak &&
        window.keycloak.token
    ) {
        return window.keycloak.token;
    }

    return (
        sessionStorage.getItem('access_token') ||
        localStorage.getItem('access_token') ||
        null
    );
}

async function parseResponseBody(response) {
    if (response.status === 204) {
        return null;
    }

    const contentType =
        response.headers.get('content-type') || '';

    if (contentType.includes('application/json')) {
        return response.json();
    }

    const text =
        await response.text();

    return text || null;
}

function resolveApiErrorMessage(
    responseBody,
    status
) {
    if (typeof responseBody === 'string') {
        return responseBody;
    }

    if (
        responseBody &&
        typeof responseBody.message === 'string'
    ) {
        return responseBody.message;
    }

    if (
        responseBody &&
        typeof responseBody.error === 'string'
    ) {
        return responseBody.error;
    }

    return `API request failed with status ${status}`;
}

function unwrapApiResponse(response) {
    if (
        response &&
        typeof response === 'object' &&
        Object.prototype.hasOwnProperty.call(
            response,
            'data'
        )
    ) {
        return response.data;
    }

    return response;
}

/* ================================================================
   LOAD WORKSPACE
   ================================================================ */

async function refreshLots() {
    const previouslySelectedLotId =
        selectedLot?.id || null;

    try {
        setPageLoading(
            true,
            'Loading contract lots...'
        );

        clearPageError();

        setButtonDisabled(
            'refreshLotsButton',
            true
        );

        const response =
            await apiRequest(
                `${API_BASE_URL}/${encodeURIComponent(contractId)}/lots`,
                'GET'
            );

        const responseData =
            unwrapApiResponse(response);

        lots =
            Array.isArray(responseData)
                ? responseData
                : [];

        renderLots();
        renderDashboardStats();

        if (lots.length === 0) {
            clearSelectedLot();
        } else {
            const matchingLot =
                previouslySelectedLotId !== null
                    ? lots.find(
                        lot =>
                            String(lot.id) ===
                            String(previouslySelectedLotId)
                    )
                    : null;

            selectLot(
                matchingLot
                    ? matchingLot.id
                    : lots[0].id
            );
        }

        await refreshProposalReadiness();

        showToast(
            'Lots loaded successfully'
        );
    } catch (error) {
        console.error(error);

        lots = [];
        selectedLot = null;
        currentAnalysisResult = null;

        renderLots();
        renderDashboardStats();
        clearSelectedLot();

        showPageError(
            error.message ||
            'Failed to load lots'
        );

        showToast(
            error.message ||
            'Failed to load lots',
            true
        );
    } finally {
        setPageLoading(false);

        setButtonDisabled(
            'refreshLotsButton',
            false
        );
    }
}

/* ================================================================
   LOT LIST
   ================================================================ */

function renderLots() {
    const lotList =
        getElement('lotList');

    if (!lotList) {
        return;
    }

    setText(
        'lotListCount',
        String(lots.length)
    );

    if (!lots.length) {
        lotList.innerHTML = `
            <div class="empty-state">
                No lots found.
            </div>
        `;

        return;
    }

    lotList.innerHTML =
        lots
            .map(buildLotCard)
            .join('');
}

function buildLotCard(lot) {
    const activeClass =
        selectedLot &&
        String(selectedLot.id) === String(lot.id)
            ? 'active'
            : '';

    const participationStatus =
        normalizeParticipationStatus(
            lot.participationStatus
        );

    const statusClass =
        getParticipationStatusClass(
            participationStatus
        );

    const analysisStatus =
        lot.analysisStatus ||
        lot.status ||
        'NOT_ANALYSED';

    return `
        <div
            class="lot ${activeClass}"
            role="button"
            tabindex="0"
            data-lot-id="${escapeHtml(lot.id)}"
            onclick="selectLot(${toJavaScriptId(lot.id)})"
            onkeydown="handleLotSelectionKey(event, ${toJavaScriptId(lot.id)})"
        >
            <div class="lot-top">
                <div class="icon">
                    ${getLotIcon(lot.lotName)}
                </div>

                <div>
                    <div class="title">
                        ${escapeHtml(lot.lotName || 'Unnamed Lot')}
                    </div>

                    <div class="value">
                        ${formatMoney(
        lot.valuation,
        lot.currency
    )}
                    </div>
                </div>
            </div>

            <div class="lot-footer">
                <span>
                    Lot ${escapeHtml(lot.lotNumber || '-')}
                </span>

                <span class="${statusClass}">
                    ${escapeHtml(participationStatus)}
                </span>
            </div>

            <div class="lot-footer">
                <span>
                    Analysis
                </span>

                <span>
                    ${escapeHtml(analysisStatus)}
                </span>
            </div>
        </div>
    `;
}

function handleLotSelectionKey(
    event,
    lotId
) {
    if (
        event.key !== 'Enter' &&
        event.key !== ' '
    ) {
        return;
    }

    event.preventDefault();
    selectLot(lotId);
}

function selectLot(lotId) {
    const lot =
        lots.find(
            item =>
                String(item.id) ===
                String(lotId)
        );

    if (!lot) {
        showToast(
            'The selected lot could not be found.',
            true
        );

        return;
    }

    const changed =
        !selectedLot ||
        String(selectedLot.id) !==
        String(lot.id);

    selectedLot = lot;

    renderLots();
    renderSelectedLot();

    if (changed) {
        resetAnalysisView();
    }

    updateAssistantContext();
}

function clearSelectedLot() {
    selectedLot = null;
    currentAnalysisResult = null;

    setText(
        'lotTitle',
        'No lot selected'
    );

    setText('lotId', '-');
    setText('lotNumber', '-');
    setText('lotValue', '-');
    setText('lotDescription', '-');
    setText('lotPages', '-');
    setText('lotStatus', '-');
    setText('analysisStatus', '-');
    setText('lastAnalysedAt', '-');

    setText(
        'lotAnalysisStatusText',
        'Select a procurement lot to review its details.'
    );

    setText(
        'lotParticipationBadge',
        'UNDECIDED'
    );

    setSelectedLotButtonsDisabled(true);
    setAnalysisReviewControlsDisabled(true);

    resetAnalysisView();
    updateAssistantContext();
}

function renderSelectedLot() {
    if (!selectedLot) {
        clearSelectedLot();
        return;
    }

    const participationStatus =
        normalizeParticipationStatus(
            selectedLot.participationStatus
        );

    const analysisStatus =
        selectedLot.analysisStatus ||
        selectedLot.status ||
        'NOT_ANALYSED';

    setText(
        'lotTitle',
        selectedLot.lotName ||
        'Unnamed Lot'
    );

    setText(
        'lotId',
        valueOrDash(selectedLot.id)
    );

    setText(
        'lotNumber',
        valueOrDash(
            selectedLot.lotNumber
        )
    );

    setText(
        'lotValue',
        formatMoney(
            selectedLot.valuation,
            selectedLot.currency
        )
    );

    setText(
        'lotDescription',
        valueOrDash(
            selectedLot.description
        )
    );

    setText(
        'lotPages',
        buildPageRange(selectedLot)
    );

    setText(
        'lotStatus',
        participationStatus
    );

    setText(
        'analysisStatus',
        analysisStatus
    );

    setText(
        'lastAnalysedAt',
        formatDateTime(
            selectedLot.lastAnalysedAt
        )
    );

    setText(
        'lotAnalysisStatusText',
        buildAnalysisStatusText(
            analysisStatus
        )
    );

    renderParticipationBadge(
        participationStatus
    );

    setSelectedLotButtonsDisabled(false);

    const hasAnalysis =
        isAnalysed(selectedLot) ||
        currentAnalysisResult !== null;

    setButtonDisabled(
        'reanalyseLotButton',
        !hasAnalysis
    );

    setAnalysisReviewControlsDisabled(
        !hasAnalysis
    );

    renderExistingReviewInformation(
        selectedLot
    );
}

function buildAnalysisStatusText(
    analysisStatus
) {
    const normalized =
        String(
            analysisStatus ||
            'NOT_ANALYSED'
        )
            .trim()
            .toUpperCase();

    switch (normalized) {
        case 'ANALYSED':
        case 'ANALYZED':
            return 'AI analysis is available for this lot.';

        case 'ANALYSIS_IN_PROGRESS':
        case 'IN_PROGRESS':
            return 'AI analysis is currently in progress.';

        case 'ANALYSIS_FAILED':
        case 'FAILED':
            return 'The previous AI analysis failed. Reanalysis may be required.';

        case 'REANALYSIS_REQUESTED':
            return 'Reanalysis has been requested.';

        default:
            return 'This lot has not yet been analysed.';
    }
}

function renderParticipationBadge(
    participationStatus
) {
    const badge =
        getElement(
            'lotParticipationBadge'
        );

    if (!badge) {
        return;
    }

    badge.className =
        `risk ${getParticipationStatusClass(
            participationStatus
        )}`;

    badge.textContent =
        participationStatus;
}

/* ================================================================
   DASHBOARD STATISTICS
   ================================================================ */

function renderDashboardStats() {
    const total =
        lots.length;

    const qualified =
        lots.filter(
            lot =>
                normalizeParticipationStatus(
                    lot.participationStatus
                ) === 'QUALIFIED'
        ).length;

    const unqualified =
        lots.filter(
            lot =>
                normalizeParticipationStatus(
                    lot.participationStatus
                ) === 'UNQUALIFIED'
        ).length;

    const undecided =
        total -
        qualified -
        unqualified;

    setText(
        'totalLots',
        String(total)
    );

    setText(
        'qualifiedLots',
        String(qualified)
    );

    setText(
        'unqualifiedLots',
        String(unqualified)
    );

    setText(
        'undecidedLots',
        String(undecided)
    );

    setText(
        'lotListCount',
        String(total)
    );

    setText(
        'projectCapacity',
        `Capacity: ${total} lot${total === 1 ? '' : 's'}`
    );

    setText(
        'projectSubtitle',
        `${total} Lots | Qualified ${qualified} | ` +
        `Unqualified ${unqualified} | Undecided ${undecided}`
    );

    if (!proposalReadiness) {
        setText(
            'projectStatus',
            total > 0
                ? 'Status: Lots loaded'
                : 'Status: No lots available'
        );
    }
}

/* ================================================================
   QUALIFICATION
   ================================================================ */

async function qualifySelectedLot() {
    if (!validateSelectedLot()) {
        return;
    }

    try {
        setButtonDisabled(
            'qualifyLotButton',
            true
        );

        const response =
            await apiRequest(
                `${API_BASE_URL}/${encodeURIComponent(contractId)}` +
                `/lots/${encodeURIComponent(selectedLot.id)}` +
                '/qualification',
                QUALIFICATION_HTTP_METHOD
            );

        const updatedLot =
            unwrapApiResponse(response);

        updateLotInState(updatedLot);

        await refreshProposalReadiness();

        showToast(
            'Lot qualified successfully'
        );
    } catch (error) {
        console.error(error);

        showToast(
            error.message ||
            'Failed to qualify lot',
            true
        );
    } finally {
        setButtonDisabled(
            'qualifyLotButton',
            false
        );
    }
}

function unqualifySelectedLot() {
    if (!validateSelectedLot()) {
        return;
    }

    const modal =
        getElement(
            'unqualificationModal'
        );

    if (modal) {
        setValue(
            'unqualificationComment',
            ''
        );

        showModal(modal);

        focusElement(
            'unqualificationComment'
        );

        return;
    }

    submitUnqualification();
}

async function submitUnqualification() {
    if (!validateSelectedLot()) {
        return;
    }

    const comment =
        getValue(
            'unqualificationComment'
        ).trim();

    try {
        setButtonDisabled(
            'confirmUnqualificationButton',
            true
        );

        setButtonDisabled(
            'unqualifyLotButton',
            true
        );

        /*
         * Your current controller does not accept a request body for
         * unqualification, so the comment is not sent.
         */
        const response =
            await apiRequest(
                `${API_BASE_URL}/${encodeURIComponent(contractId)}` +
                `/lots/${encodeURIComponent(selectedLot.id)}` +
                '/unqualification',
                UNQUALIFICATION_HTTP_METHOD
            );

        const updatedLot =
            unwrapApiResponse(response);

        updateLotInState(updatedLot);

        closeUnqualificationModal();

        await refreshProposalReadiness();

        showToast(
            'Lot unqualified successfully'
        );
    } catch (error) {
        console.error(error);

        showToast(
            error.message ||
            'Failed to unqualify lot',
            true
        );
    } finally {
        setButtonDisabled(
            'confirmUnqualificationButton',
            false
        );

        setButtonDisabled(
            'unqualifyLotButton',
            false
        );
    }
}

function closeUnqualificationModal() {
    hideModal(
        getElement(
            'unqualificationModal'
        )
    );
}

function updateLotInState(updatedLot) {
    if (
        !updatedLot ||
        updatedLot.id === null ||
        updatedLot.id === undefined
    ) {
        return;
    }

    lots =
        lots.map(
            lot =>
                String(lot.id) ===
                String(updatedLot.id)
                    ? {
                        ...lot,
                        ...updatedLot
                    }
                    : lot
        );

    selectedLot =
        lots.find(
            lot =>
                String(lot.id) ===
                String(updatedLot.id)
        ) || {
            ...selectedLot,
            ...updatedLot
        };

    renderLots();
    renderSelectedLot();
    renderDashboardStats();
}

/* ================================================================
   ANALYSIS
   ================================================================ */

async function analyseSelectedLot(
    reanalyse = false
) {
    if (!validateSelectedLot()) {
        return;
    }

    try {
        setAnalysisLoading(true);

        setButtonDisabled(
            'analyseLotButton',
            true
        );

        setButtonDisabled(
            'reanalyseLotButton',
            true
        );

        const response =
            await apiRequest(
                `${API_BASE_URL}/${encodeURIComponent(contractId)}` +
                `/lots/${encodeURIComponent(selectedLot.id)}` +
                `/analyse?reanalyse=${encodeURIComponent(reanalyse)}`,
                'GET'
            );

        currentAnalysisResult =
            unwrapApiResponse(response);

        renderAnalysisResult(
            currentAnalysisResult
        );

        mergeAnalysisIntoSelectedLot(
            currentAnalysisResult
        );

        setAnalysisReviewControlsDisabled(
            false
        );

        showToast(
            reanalyse
                ? 'Lot reanalysis completed'
                : 'Lot analysis completed'
        );
    } catch (error) {
        console.error(error);

        showToast(
            error.message ||
            'Failed to analyse lot',
            true
        );
    } finally {
        setAnalysisLoading(false);

        setButtonDisabled(
            'analyseLotButton',
            false
        );

        setButtonDisabled(
            'reanalyseLotButton',
            false
        );
    }
}

function openReanalysePrompt() {
    if (!validateSelectedLot()) {
        return;
    }

    const modal =
        getElement(
            'reanalysisModal'
        );

    if (modal) {
        setValue(
            'reanalysisComment',
            ''
        );

        showModal(modal);

        focusElement(
            'reanalysisComment'
        );

        return;
    }

    const comment =
        window.prompt(
            'Why should this lot be reanalysed?'
        );

    if (!comment || !comment.trim()) {
        return;
    }

    executeReanalysis(
        comment.trim()
    );
}

async function submitReanalysis() {
    const comment =
        getValue(
            'reanalysisComment'
        ).trim();

    if (!comment) {
        showToast(
            'Reanalysis reason is required.',
            true
        );

        focusElement(
            'reanalysisComment'
        );

        return;
    }

    await executeReanalysis(comment);
}

async function executeReanalysis(comment) {
    if (!validateSelectedLot()) {
        return;
    }

    try {
        setAnalysisLoading(true);

        setButtonDisabled(
            'confirmReanalysisButton',
            true
        );

        setButtonDisabled(
            'reanalyseLotButton',
            true
        );

        const response =
            await apiRequest(
                `${API_BASE_URL}/${encodeURIComponent(contractId)}` +
                `/lots/${encodeURIComponent(selectedLot.id)}` +
                '/reanalyse',
                'POST',
                {
                    userComment: comment
                }
            );

        currentAnalysisResult =
            unwrapApiResponse(response);

        renderAnalysisResult(
            currentAnalysisResult
        );

        mergeAnalysisIntoSelectedLot(
            currentAnalysisResult
        );

        closeReanalysisModal();

        setAnalysisReviewControlsDisabled(
            false
        );

        showToast(
            'Lot reanalysis completed'
        );
    } catch (error) {
        console.error(error);

        showToast(
            error.message ||
            'Failed to reanalyse lot',
            true
        );
    } finally {
        setAnalysisLoading(false);

        setButtonDisabled(
            'confirmReanalysisButton',
            false
        );

        setButtonDisabled(
            'reanalyseLotButton',
            false
        );
    }
}

function closeReanalysisModal() {
    hideModal(
        getElement(
            'reanalysisModal'
        )
    );
}

function mergeAnalysisIntoSelectedLot(
    result
) {
    if (!selectedLot || !result) {
        return;
    }

    const analysis =
        resolveAnalysis(result);

    const updatedLot = {
        ...selectedLot,
        analysisStatus:
            result.analysisStatus ||
            analysis.analysisStatus ||
            'ANALYSED',
        bidScore:
            analysis.bidScore ??
            selectedLot.bidScore,
        winProbability:
            analysis.winProbability ??
            selectedLot.winProbability,
        overallRiskLevel:
            analysis.overallRiskLevel ??
            selectedLot.overallRiskLevel,
        recommendedToBid:
            analysis.recommendedToBid ??
            selectedLot.recommendedToBid,
        lastAnalysedAt:
            analysis.analysedAt ||
            result.analysedAt ||
            selectedLot.lastAnalysedAt
    };

    updateLotInState(updatedLot);
}

function renderAnalysisResult(result) {
    if (!result) {
        resetAnalysisView();
        return;
    }

    const analysis =
        resolveAnalysis(result);

    const highlights =
        resolveHighlights(result);

    setText(
        'score',
        formatPercent(
            analysis.bidScore
        )
    );

    setText(
        'overallBidScore',
        formatPercent(
            analysis.bidScore
        )
    );

    setText(
        'winProbability',
        formatPercent(
            analysis.winProbability
        )
    );

    renderRisk(
        analysis.overallRiskLevel
    );

    renderRecommendedToBid(
        analysis.recommendedToBid
    );

    setText(
        'recommendationText',
        analysis.recommendation ||
        'No recommendation available.'
    );

    setText(
        'executiveSummaryText',
        analysis.executiveSummary ||
        'No executive summary available.'
    );

    renderHighlightGroups(highlights);

    setText(
        'findingsCount',
        `${highlights.length} finding` +
        `${highlights.length === 1 ? '' : 's'}`
    );

    if (analysis.executiveSummary) {
        addAiMessage(
            'Executive Summary:\n\n' +
            analysis.executiveSummary
        );
    }

    const review =
        resolveReviewInformation(result);

    if (review) {
        renderAnalysisReview(review);
    }
}

function resolveAnalysis(result) {
    return (
        result.analysis ||
        result.analysisResult ||
        result.summary ||
        result
    );
}

function resolveHighlights(result) {
    const values =
        result.highlights ||
        result.findings ||
        result.analysisHighlights ||
        [];

    return Array.isArray(values)
        ? values
        : [];
}

function resetAnalysisView() {
    currentAnalysisResult = null;

    setText('score', '-');
    setText('overallBidScore', '-');
    setText('winProbability', '-');

    const risk =
        getElement('risk');

    if (risk) {
        risk.textContent =
            'Not Analysed';

        risk.className =
            'risk';
    }

    const recommendation =
        getElement(
            'recommendedToBid'
        );

    if (recommendation) {
        recommendation.textContent = '-';
        recommendation.className = 'risk';
    }

    setText(
        'recommendationText',
        'No recommendation available.'
    );

    setText(
        'executiveSummaryText',
        'No executive summary available.'
    );

    setHtml(
        'strengthsList',
        '<li class="empty-state">' +
        'No strengths available.' +
        '</li>'
    );

    setHtml(
        'weaknessesList',
        '<li class="empty-state">' +
        'No weaknesses available.' +
        '</li>'
    );

    setHtml(
        'missingRequirementsList',
        '<div class="empty-state">' +
        'No missing requirements available.' +
        '</div>'
    );

    setHtml(
        'findingsTableBody',
        '<tr>' +
        '<td colspan="6">' +
        'No findings available.' +
        '</td>' +
        '</tr>'
    );

    setText(
        'findingsCount',
        '0 findings'
    );

    resetAnalysisReviewView();
}

function renderRisk(riskLevel) {
    const risk =
        getElement('risk');

    if (!risk) {
        return;
    }

    risk.className = 'risk';

    const normalized =
        String(riskLevel || '')
            .trim()
            .toUpperCase();

    if (!normalized) {
        risk.textContent =
            'Not Analysed';

        return;
    }

    risk.textContent =
        `${normalized} Risk`;

    switch (normalized) {
        case 'LOW':
            risk.classList.add('low');
            break;

        case 'MEDIUM':
            risk.classList.add('medium');
            break;

        case 'HIGH':
        case 'CRITICAL':
            risk.classList.add('high');
            break;

        default:
            break;
    }
}

function renderRecommendedToBid(value) {
    const element =
        getElement(
            'recommendedToBid'
        );

    if (!element) {
        return;
    }

    element.className = 'risk';

    if (value === true) {
        element.textContent = 'YES';
        element.classList.add('low');
        return;
    }

    if (value === false) {
        element.textContent = 'NO';
        element.classList.add('high');
        return;
    }

    element.textContent = '-';
}

/* ================================================================
   ANALYSIS HIGHLIGHTS
   ================================================================ */

function renderHighlightGroups(highlights) {
    const safeHighlights =
        Array.isArray(highlights)
            ? highlights
            : [];

    const strengths =
        filterByCategory(
            safeHighlights,
            'STRENGTH'
        );

    const weaknesses = [
        ...filterByCategory(
            safeHighlights,
            'WEAKNESS'
        ),
        ...filterByCategory(
            safeHighlights,
            'CAPABILITY_GAP'
        )
    ];

    const missing =
        filterByCategory(
            safeHighlights,
            'MISSING_REQUIREMENT'
        );

    renderList(
        'strengthsList',
        strengths,
        '✅',
        'No strengths available.'
    );

    renderList(
        'weaknessesList',
        weaknesses,
        '❌',
        'No weaknesses available.'
    );

    renderMissingRequirements(
        missing
    );

    renderFindingsTable(
        safeHighlights
    );
}

function filterByCategory(
    highlights,
    category
) {
    return highlights.filter(
        highlight =>
            normalizeCategory(
                highlight.category
            ) === category
    );
}

function normalizeCategory(category) {
    return String(category || '')
        .trim()
        .toUpperCase()
        .replaceAll(' ', '_')
        .replaceAll('-', '_');
}

function renderList(
    elementId,
    items,
    icon,
    emptyText
) {
    const element =
        getElement(elementId);

    if (!element) {
        return;
    }

    if (!items.length) {
        element.innerHTML = `
            <li class="empty-state">
                ${escapeHtml(emptyText)}
            </li>
        `;

        return;
    }

    element.innerHTML =
        items
            .map(item => `
                <li>
                    ${icon}
                    <strong>
                        ${escapeHtml(
                item.title ||
                item.finding ||
                'Untitled'
            )}
                    </strong>

                    ${
                item.description
                    ? `<div>${escapeHtml(item.description)}</div>`
                    : ''
            }
                </li>
            `)
            .join('');
}

function renderMissingRequirements(items) {
    const element =
        getElement(
            'missingRequirementsList'
        );

    if (!element) {
        return;
    }

    if (!items.length) {
        element.innerHTML = `
            <div class="empty-state">
                No missing requirements available.
            </div>
        `;

        return;
    }

    element.innerHTML =
        items
            .map(item => `
                <div class="missing-item">
                    <strong>
                        ❌
                        ${escapeHtml(
                item.title ||
                item.finding ||
                'Missing requirement'
            )}
                    </strong>

                    ${
                item.description
                    ? `<div>${escapeHtml(item.description)}</div>`
                    : ''
            }

                    ${
                resolvePageNumber(item) !== '-'
                    ? `<div>Page ${escapeHtml(resolvePageNumber(item))}</div>`
                    : ''
            }
                </div>
            `)
            .join('');
}

function renderFindingsTable(highlights) {
    const body =
        getElement(
            'findingsTableBody'
        );

    if (!body) {
        return;
    }

    if (!highlights.length) {
        body.innerHTML = `
            <tr>
                <td colspan="6">
                    No findings available.
                </td>
            </tr>
        `;

        return;
    }

    body.innerHTML =
        highlights
            .map(item => `
                <tr>
                    <td>
                        ${escapeHtml(
                item.category || '-'
            )}
                    </td>

                    <td>
                        <strong>
                            ${escapeHtml(
                item.title ||
                item.finding ||
                '-'
            )}
                        </strong>

                        ${
                item.description
                    ? `<br><span>${escapeHtml(item.description)}</span>`
                    : ''
            }
                    </td>

                    <td>
                        ${escapeHtml(
                item.riskLevel ||
                item.risk ||
                '-'
            )}
                    </td>

                    <td>
                        ${escapeHtml(
                resolvePageNumber(item)
            )}
                    </td>

                    <td>
                        ${formatBoolean(
                item.bidCapable
            )}
                    </td>

                    <td>
                        ${escapeHtml(
                item.recommendedAction ||
                item.action ||
                '-'
            )}
                    </td>
                </tr>
            `)
            .join('');
}

function resolvePageNumber(item) {
    return (
        item.pageNumber ??
        item.page ??
        item.sourcePage ??
        '-'
    );
}

/* ================================================================
   ANALYSIS REVIEW
   ================================================================ */

async function approveSelectedAnalysis() {
    if (!validateAnalysisReview()) {
        return;
    }

    const comment =
        getValue(
            'analysisReviewComment'
        ).trim();

    try {
        setButtonDisabled(
            'approveAnalysisButton',
            true
        );

        setButtonDisabled(
            'rejectAnalysisButton',
            true
        );

        const response =
            await apiRequest(
                `${API_BASE_URL}/${encodeURIComponent(contractId)}` +
                `/lots/${encodeURIComponent(selectedLot.id)}` +
                '/analysis/approve',
                'POST',
                {
                    comment
                }
            );

        const review =
            unwrapApiResponse(response);

        renderAnalysisReview(review);
        mergeReviewIntoSelectedLot(review);

        showToast(
            'Analysis approved successfully'
        );
    } catch (error) {
        console.error(error);

        showToast(
            error.message ||
            'Failed to approve analysis',
            true
        );
    } finally {
        setButtonDisabled(
            'approveAnalysisButton',
            false
        );

        setButtonDisabled(
            'rejectAnalysisButton',
            false
        );
    }
}

async function rejectSelectedAnalysis() {
    if (!validateAnalysisReview()) {
        return;
    }

    const comment =
        getValue(
            'analysisReviewComment'
        ).trim();

    if (!comment) {
        showToast(
            'A review comment is required to reject the analysis.',
            true
        );

        focusElement(
            'analysisReviewComment'
        );

        return;
    }

    try {
        setButtonDisabled(
            'approveAnalysisButton',
            true
        );

        setButtonDisabled(
            'rejectAnalysisButton',
            true
        );

        const response =
            await apiRequest(
                `${API_BASE_URL}/${encodeURIComponent(contractId)}` +
                `/lots/${encodeURIComponent(selectedLot.id)}` +
                '/analysis/reject',
                'POST',
                {
                    comment
                }
            );

        const review =
            unwrapApiResponse(response);

        renderAnalysisReview(review);
        mergeReviewIntoSelectedLot(review);

        showToast(
            'Analysis rejected successfully'
        );
    } catch (error) {
        console.error(error);

        showToast(
            error.message ||
            'Failed to reject analysis',
            true
        );
    } finally {
        setButtonDisabled(
            'approveAnalysisButton',
            false
        );

        setButtonDisabled(
            'rejectAnalysisButton',
            false
        );
    }
}

function validateAnalysisReview() {
    if (!validateSelectedLot()) {
        return false;
    }

    if (!currentAnalysisResult &&
        !isAnalysed(selectedLot)) {
        showToast(
            'Please analyse the selected lot first.',
            true
        );

        return false;
    }

    return true;
}

function renderAnalysisReview(review) {
    if (!review) {
        resetAnalysisReviewView();
        return;
    }

    const status =
        review.analysisReviewStatus ||
        review.reviewStatus ||
        review.status ||
        'NOT_REVIEWED';

    const comment =
        review.reviewComment ||
        review.comment ||
        '-';

    const reviewedBy =
        resolveReviewedBy(
            review.reviewedBy
        );

    const reviewedAt =
        formatDateTime(
            review.reviewedAt
        );

    setText(
        'analysisReviewStatus',
        status
    );

    setText(
        'analysisReviewedBy',
        reviewedBy
    );

    setText(
        'analysisReviewedAt',
        reviewedAt
    );

    setText(
        'analysisReviewedComment',
        comment
    );

    const details =
        getElement(
            'analysisReviewDetails'
        );

    if (details) {
        details.hidden = false;
    }

    setValue(
        'analysisReviewComment',
        comment === '-' ? '' : comment
    );
}

function renderExistingReviewInformation(lot) {
    if (
        !lot ||
        (
            !lot.analysisReviewStatus &&
            !lot.reviewStatus &&
            !lot.reviewedAt &&
            !lot.reviewedBy
        )
    ) {
        resetAnalysisReviewView();
        return;
    }

    renderAnalysisReview({
        analysisReviewStatus:
            lot.analysisReviewStatus ||
            lot.reviewStatus,
        reviewComment:
        lot.reviewComment,
        reviewedBy:
        lot.reviewedBy,
        reviewedAt:
        lot.reviewedAt
    });
}

function resolveReviewInformation(result) {
    if (!result) {
        return null;
    }

    if (result.review) {
        return result.review;
    }

    if (
        result.analysisReviewStatus ||
        result.reviewStatus ||
        result.reviewedAt ||
        result.reviewedBy
    ) {
        return result;
    }

    const analysis =
        resolveAnalysis(result);

    if (
        analysis.analysisReviewStatus ||
        analysis.reviewStatus ||
        analysis.reviewedAt ||
        analysis.reviewedBy
    ) {
        return analysis;
    }

    return null;
}

function mergeReviewIntoSelectedLot(review) {
    if (!selectedLot || !review) {
        return;
    }

    updateLotInState({
        ...selectedLot,
        analysisReviewStatus:
            review.analysisReviewStatus ||
            review.reviewStatus ||
            review.status,
        reviewComment:
            review.reviewComment ||
            review.comment,
        reviewedBy:
        review.reviewedBy,
        reviewedAt:
        review.reviewedAt
    });
}

function resetAnalysisReviewView() {
    setText(
        'analysisReviewStatus',
        'NOT REVIEWED'
    );

    setText(
        'analysisReviewedBy',
        '-'
    );

    setText(
        'analysisReviewedAt',
        '-'
    );

    setText(
        'analysisReviewedComment',
        '-'
    );

    setValue(
        'analysisReviewComment',
        ''
    );

    const details =
        getElement(
            'analysisReviewDetails'
        );

    if (details) {
        details.hidden = true;
    }

    const disable =
        !selectedLot ||
        (
            !currentAnalysisResult &&
            !isAnalysed(selectedLot)
        );

    setAnalysisReviewControlsDisabled(
        disable
    );
}

function setAnalysisReviewControlsDisabled(
    disabled
) {
    setButtonDisabled(
        'approveAnalysisButton',
        disabled
    );

    setButtonDisabled(
        'rejectAnalysisButton',
        disabled
    );

    setElementDisabled(
        'analysisReviewComment',
        disabled
    );
}

/* ================================================================
   PROPOSAL READINESS
   ================================================================ */

async function refreshProposalReadiness() {
    try {
        setButtonDisabled(
            'generateProposalButton',
            true
        );

        const response =
            await apiRequest(
                `${API_BASE_URL}/${encodeURIComponent(contractId)}` +
                '/lots/readiness',
                'GET'
            );

        proposalReadiness =
            unwrapApiResponse(response);

        renderProposalReadiness();
    } catch (error) {
        console.error(error);

        proposalReadiness = {
            contractId:
                Number(contractId),
            totalLots:
            lots.length,
            qualifiedLots:
            lots.filter(
                lot =>
                    normalizeParticipationStatus(
                        lot.participationStatus
                    ) === 'QUALIFIED'
            ).length,
            unqualifiedLots:
            lots.filter(
                lot =>
                    normalizeParticipationStatus(
                        lot.participationStatus
                    ) === 'UNQUALIFIED'
            ).length,
            undecidedLots:
            lots.filter(
                lot =>
                    normalizeParticipationStatus(
                        lot.participationStatus
                    ) === 'UNDECIDED'
            ).length,
            allLotsDecided:
                false,
            atLeastOneQualifiedLot:
                false,
            proposalGenerationAllowed:
                false,
            message:
                'Proposal readiness could not be loaded.',
            qualifiedLotIds:
                [],
            undecidedLotIds:
                []
        };

        renderProposalReadiness();

        showToast(
            error.message ||
            'Failed to load proposal readiness',
            true
        );
    }
}

function renderProposalReadiness() {
    const readiness =
        proposalReadiness || {};

    const allowed =
        readiness
            .proposalGenerationAllowed === true;

    setButtonDisabled(
        'generateProposalButton',
        !allowed
    );

    setElementTitle(
        'generateProposalButton',
        readiness.message ||
        'Proposal generation is not available.'
    );

    setText(
        'proposalReadinessStatus',
        'Proposal readiness: ' +
        (
            readiness.message ||
            'Not available'
        )
    );

    setText(
        'projectStatus',
        'Status: ' +
        (
            readiness.message ||
            'Proposal readiness not available'
        )
    );

    if (
        readiness.totalLots !== null &&
        readiness.totalLots !== undefined
    ) {
        setText(
            'totalLots',
            String(readiness.totalLots)
        );
    }

    if (
        readiness.qualifiedLots !== null &&
        readiness.qualifiedLots !== undefined
    ) {
        setText(
            'qualifiedLots',
            String(readiness.qualifiedLots)
        );
    }

    if (
        readiness.unqualifiedLots !== null &&
        readiness.unqualifiedLots !== undefined
    ) {
        setText(
            'unqualifiedLots',
            String(readiness.unqualifiedLots)
        );
    }

    if (
        readiness.undecidedLots !== null &&
        readiness.undecidedLots !== undefined
    ) {
        setText(
            'undecidedLots',
            String(readiness.undecidedLots)
        );
    }
}

function navigateToProposal() {
    if (
        !proposalReadiness ||
        proposalReadiness
            .proposalGenerationAllowed !== true
    ) {
        showToast(
            proposalReadiness?.message ||
            'All lots must be decided before generating a proposal.',
            true
        );

        return;
    }

    window.location.href =
        '/bids/create?contractId=' +
        encodeURIComponent(contractId);
}

/* ================================================================
   ASSISTANT
   ================================================================ */

function askSuggestion(element) {
    const text =
        element?.innerText?.trim();

    if (!text) {
        return;
    }

    addUserMessage(text);
    addAssistantAnswer(text);
}

function sendMessage(event) {
    if (event.key !== 'Enter') {
        return;
    }

    event.preventDefault();

    const input =
        getElement('chatInput');

    if (!input) {
        return;
    }

    const text =
        input.value.trim();

    if (!text) {
        return;
    }

    addUserMessage(text);
    addAssistantAnswer(text);

    input.value = '';
}

function addAssistantAnswer(question) {
    const normalized =
        String(question || '')
            .toLowerCase();

    if (!selectedLot) {
        addAiMessage(
            'Please select a lot first.'
        );

        return;
    }

    if (!currentAnalysisResult) {
        addAiMessage(
            'Please analyse the selected lot first.'
        );

        return;
    }

    const analysis =
        resolveAnalysis(
            currentAnalysisResult
        );

    const highlights =
        resolveHighlights(
            currentAnalysisResult
        );

    if (normalized.includes('missing')) {
        const missing =
            filterByCategory(
                highlights,
                'MISSING_REQUIREMENT'
            );

        if (!missing.length) {
            addAiMessage(
                'No missing requirements were found for this lot.'
            );

            return;
        }

        addAiMessage(
            'Missing Requirements:\n\n' +
            missing
                .map(
                    item =>
                        '• ' +
                        (
                            item.title ||
                            item.finding ||
                            'Missing requirement'
                        )
                )
                .join('\n')
        );

        return;
    }

    if (normalized.includes('risk')) {
        addAiMessage(
            'Risk Level: ' +
            (
                analysis.overallRiskLevel ||
                '-'
            ) +
            '\n\nRecommendation:\n' +
            (
                analysis.recommendation ||
                '-'
            )
        );

        return;
    }

    if (normalized.includes('bid')) {
        let recommendation = '-';

        if (analysis.recommendedToBid === true) {
            recommendation = 'Yes';
        } else if (
            analysis.recommendedToBid === false
        ) {
            recommendation = 'No';
        }

        addAiMessage(
            'Recommended To Bid: ' +
            recommendation +
            '\n\nBid Score: ' +
            formatPercent(
                analysis.bidScore
            ) +
            '\nWin Probability: ' +
            formatPercent(
                analysis.winProbability
            )
        );

        return;
    }

    if (normalized.includes('summary')) {
        addAiMessage(
            analysis.executiveSummary ||
            'No executive summary is available.'
        );

        return;
    }

    addAiMessage(
        'I can answer based on the current lot analysis, ' +
        'highlights, missing requirements, risk level, ' +
        'recommendation, bid score, and win probability.'
    );
}

function addUserMessage(message) {
    appendChatMessage(
        message,
        'user'
    );
}

function addAiMessage(message) {
    appendChatMessage(
        message,
        'ai'
    );
}

function appendChatMessage(
    message,
    type
) {
    const chatBody =
        getElement('chatBody');

    if (!chatBody) {
        return;
    }

    const messageElement =
        document.createElement('div');

    messageElement.className =
        `message ${type}`;

    messageElement.textContent =
        String(message || '');

    chatBody.appendChild(
        messageElement
    );

    chatBody.scrollTop =
        chatBody.scrollHeight;
}

function updateAssistantContext() {
    setText(
        'assistantLotContext',
        selectedLot
            ? (
                selectedLot.lotName ||
                selectedLot.lotNumber ||
                `Lot ${selectedLot.id}`
            )
            : 'No lot selected'
    );
}

/* ================================================================
   PAGE STATE
   ================================================================ */

function setPageLoading(
    loading,
    message = 'Loading...'
) {
    const loadingState =
        getElement(
            'pageLoadingState'
        );

    if (!loadingState) {
        return;
    }

    loadingState.hidden =
        !loading;

    setText(
        'pageLoadingMessage',
        message
    );
}

function setAnalysisLoading(loading) {
    const element =
        getElement(
            'analysisLoadingState'
        );

    if (element) {
        element.hidden =
            !loading;
    }
}

function showPageError(message) {
    const errorState =
        getElement(
            'pageErrorState'
        );

    if (errorState) {
        errorState.hidden = false;
    }

    setText(
        'pageErrorMessage',
        message
    );
}

function clearPageError() {
    const errorState =
        getElement(
            'pageErrorState'
        );

    if (errorState) {
        errorState.hidden = true;
    }

    setText(
        'pageErrorMessage',
        ''
    );
}

function setSelectedLotButtonsDisabled(
    disabled
) {
    setButtonDisabled(
        'analyseLotButton',
        disabled
    );

    setButtonDisabled(
        'reanalyseLotButton',
        disabled
    );

    setButtonDisabled(
        'qualifyLotButton',
        disabled
    );

    setButtonDisabled(
        'unqualifyLotButton',
        disabled
    );
}

function validateSelectedLot() {
    if (selectedLot) {
        return true;
    }

    showToast(
        'Please select a lot first.',
        true
    );

    return false;
}

/* ================================================================
   NAVIGATION
   ================================================================ */

function navigateTo(url) {
    window.location.href = url;
}

function handleNavigationKey(
    event,
    url
) {
    if (
        event.key !== 'Enter' &&
        event.key !== ' '
    ) {
        return;
    }

    event.preventDefault();
    navigateTo(url);
}

/* ================================================================
   FORMATTERS
   ================================================================ */

function getLotIcon(lotName) {
    const name =
        String(lotName || '')
            .toLowerCase();

    if (
        name.includes('medicine') ||
        name.includes('pharma')
    ) {
        return '💊';
    }

    if (
        name.includes('bed') ||
        name.includes('furniture')
    ) {
        return '🛏️';
    }

    if (
        name.includes('device') ||
        name.includes('equipment')
    ) {
        return '🏥';
    }

    if (name.includes('lab')) {
        return '🧪';
    }

    if (name.includes('ambulance')) {
        return '🚑';
    }

    if (
        name.includes('it') ||
        name.includes('system')
    ) {
        return '💻';
    }

    return '📦';
}

function normalizeParticipationStatus(status) {
    const normalized =
        String(status || '')
            .trim()
            .toUpperCase();

    if (
        normalized === 'QUALIFIED' ||
        normalized === 'UNQUALIFIED'
    ) {
        return normalized;
    }

    return 'UNDECIDED';
}

function getParticipationStatusClass(status) {
    switch (
        normalizeParticipationStatus(status)
        ) {
        case 'QUALIFIED':
            return 'success';

        case 'UNQUALIFIED':
            return 'danger';

        default:
            return 'warning';
    }
}

function buildPageRange(lot) {
    const startPage =
        lot?.startPage;

    const endPage =
        lot?.endPage;

    if (
        startPage !== null &&
        startPage !== undefined &&
        endPage !== null &&
        endPage !== undefined
    ) {
        return `Pages ${startPage} - ${endPage}`;
    }

    if (
        startPage !== null &&
        startPage !== undefined
    ) {
        return `Page ${startPage}`;
    }

    return '-';
}

function formatMoney(
    value,
    currency = 'EUR'
) {
    if (
        value === null ||
        value === undefined ||
        value === ''
    ) {
        return '-';
    }

    const numericValue =
        Number(value);

    if (!Number.isFinite(numericValue)) {
        return String(value);
    }

    try {
        return new Intl.NumberFormat(
            'en-IE',
            {
                style: 'currency',
                currency:
                    currency || 'EUR',
                maximumFractionDigits: 2
            }
        ).format(numericValue);
    } catch (error) {
        return `€${numericValue.toLocaleString()}`;
    }
}

function formatPercent(value) {
    if (
        value === null ||
        value === undefined ||
        value === ''
    ) {
        return '-';
    }

    const numericValue =
        Number(value);

    if (!Number.isFinite(numericValue)) {
        return '-';
    }

    return `${Math.round(numericValue)}%`;
}

function formatDateTime(value) {
    if (!value) {
        return '-';
    }

    const date =
        new Date(value);

    if (
        Number.isNaN(
            date.getTime()
        )
    ) {
        return String(value);
    }

    return new Intl.DateTimeFormat(
        'en-GB',
        {
            dateStyle: 'medium',
            timeStyle: 'short'
        }
    ).format(date);
}

function formatBoolean(value) {
    if (value === true) {
        return 'Yes';
    }

    if (value === false) {
        return 'No';
    }

    return '-';
}

function resolveReviewedBy(reviewedBy) {
    if (!reviewedBy) {
        return '-';
    }

    if (typeof reviewedBy === 'string') {
        return reviewedBy;
    }

    return (
        reviewedBy.displayName ||
        reviewedBy.fullName ||
        reviewedBy.name ||
        reviewedBy.email ||
        '-'
    );
}

function valueOrDash(value) {
    if (
        value === null ||
        value === undefined ||
        value === ''
    ) {
        return '-';
    }

    return String(value);
}

function isAnalysed(lot) {
    const status =
        String(
            lot?.analysisStatus ||
            lot?.status ||
            ''
        )
            .trim()
            .toUpperCase();

    return (
        status === 'ANALYSED' ||
        status === 'ANALYZED' ||
        lot?.bidScore !== null &&
        lot?.bidScore !== undefined
    );
}

function escapeHtml(value) {
    return String(
        value === null ||
        value === undefined
            ? ''
            : value
    )
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#039;');
}

function toJavaScriptId(value) {
    const numericValue =
        Number(value);

    if (Number.isFinite(numericValue)) {
        return String(numericValue);
    }

    return JSON.stringify(
        String(value)
    );
}

/* ================================================================
   DOM HELPERS
   ================================================================ */

function getElement(elementId) {
    return document.getElementById(
        elementId
    );
}

function setText(
    elementId,
    value
) {
    const element =
        getElement(elementId);

    if (!element) {
        return;
    }

    element.textContent =
        value === null ||
        value === undefined
            ? ''
            : String(value);
}

function setHtml(
    elementId,
    value
) {
    const element =
        getElement(elementId);

    if (!element) {
        return;
    }

    element.innerHTML = value;
}

function setValue(
    elementId,
    value
) {
    const element =
        getElement(elementId);

    if (!element) {
        return;
    }

    element.value =
        value === null ||
        value === undefined
            ? ''
            : String(value);
}

function getValue(elementId) {
    const element =
        getElement(elementId);

    return element?.value || '';
}

function setButtonDisabled(
    elementId,
    disabled
) {
    const element =
        getElement(elementId);

    if (!element) {
        return;
    }

    element.disabled =
        Boolean(disabled);
}

function setElementDisabled(
    elementId,
    disabled
) {
    const element =
        getElement(elementId);

    if (!element) {
        return;
    }

    element.disabled =
        Boolean(disabled);
}

function setElementTitle(
    elementId,
    title
) {
    const element =
        getElement(elementId);

    if (!element) {
        return;
    }

    element.title =
        title || '';
}

function focusElement(elementId) {
    window.setTimeout(
        function () {
            getElement(
                elementId
            )?.focus();
        },
        0
    );
}

function showModal(element) {
    if (!element) {
        return;
    }

    element.hidden = false;
    element.style.display = 'flex';
}

function hideModal(element) {
    if (!element) {
        return;
    }

    element.hidden = true;
    element.style.display = 'none';
}

/* ================================================================
   TOAST
   ================================================================ */

function showToast(
    message,
    error = false
) {
    const toast =
        getElement('toast');

    if (!toast) {
        return;
    }

    if (toastTimeoutId) {
        window.clearTimeout(
            toastTimeoutId
        );
    }

    toast.textContent =
        message || '';

    toast.classList.toggle(
        'error',
        Boolean(error)
    );

    toast.style.display =
        'block';

    toastTimeoutId =
        window.setTimeout(
            function () {
                toast.style.display =
                    'none';

                toast.classList.remove(
                    'error'
                );
            },
            3500
        );
}