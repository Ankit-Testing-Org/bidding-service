'use strict';
const API_BASE_URL = "http://localhost:8080/api";

let approvalState = {
    searched: false,
    contractId: null,
    contractStatus: null,
    approvalPhase: false,
    currentStage: null,
    workflowId: null,
    stages: [],
    tasks: [],
    history: []
};

let pendingDecision = null;

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

function getContractId() {
    return document.getElementById("contractId").value.trim();
}

async function searchApprovalWorkflow() {
    const contractId = getContractId();

    if (!contractId) {
        showToast("Contract ID is required");
        return;
    }

    setSearchLoading(true);
    resetWorkflowViewOnly();

    try {
        const response = await fetch(
            API_BASE_URL + "/contracts/" + contractId + "/approval-workflow",
            {
                method: "GET",
                headers: buildAuthHeaders()
            }
        );

        if (response.status === 404) {
            applyNotInApprovalPhase({
                contractId: Number(contractId),
                contractStatus: "NOT_SUBMITTED",
                message: "Current contract is not in Approval phase."
            });
            return;
        }

        if (!response.ok) {
            throw new Error("Approval workflow API failed with status " + response.status);
        }

        const result = await response.json();
        applyWorkflowResponse(result);

    } catch (error) {
        console.error(error);

        if (contractId === "84") {
            applyWorkflowResponse(buildMockSubmittedWorkflow(contractId));
        } else {
            applyNotInApprovalPhase({
                contractId: Number(contractId),
                contractStatus: "BID_UPLOADED",
                message: "Current contract is not in Approval phase. Submit the prepared bid document first from Bid Preparation page."
            });
        }
    } finally {
        setSearchLoading(false);
    }
}

function applyWorkflowResponse(result) {
    approvalState.searched = true;
    approvalState.contractId = result.contractId || Number(getContractId());
    approvalState.contractStatus = result.contractStatus || "UNKNOWN";
    approvalState.approvalPhase = result.approvalPhase === true;
    approvalState.workflowId = result.workflowId || null;
    approvalState.currentStage = result.currentStage || null;
    approvalState.stages = result.stages || [];
    approvalState.tasks = result.tasks || [];
    approvalState.history = result.history || [];

    if (!approvalState.approvalPhase) {
        applyNotInApprovalPhase({
            contractId: approvalState.contractId,
            contractStatus: approvalState.contractStatus,
            message: result.message || "Current contract is not in Approval phase."
        });
        return;
    }

    updateTopTilesForSubmittedWorkflow();
    renderWorkflowStages();
    renderTaskTable();
    renderHistory();

    document.getElementById("emptyState").style.display = "none";
    document.getElementById("notApprovalState").style.display = "none";
    document.getElementById("approvalLayout").style.display = "grid";

    document.getElementById("workflowSubtitle").innerText =
        "Contract " + approvalState.contractId + " is in approval workflow. Review stages, approver tasks and history below.";

    document.getElementById("workflowOverallBadge").innerText = "In Approval";
    document.getElementById("workflowOverallBadge").className = "badge badge-purple";

    showToast("Approval workflow loaded");
    addAiMessage(buildAiAnswer("Approval workflow loaded. Current stage: " + escapeHtml(approvalState.currentStage || "Not available") + "."));
}

function applyNotInApprovalPhase(result) {
    approvalState.searched = true;
    approvalState.contractId = result.contractId || Number(getContractId());
    approvalState.contractStatus = result.contractStatus || "NOT_IN_APPROVAL";
    approvalState.approvalPhase = false;
    approvalState.currentStage = null;
    approvalState.workflowId = null;
    approvalState.stages = [];
    approvalState.tasks = [];
    approvalState.history = [];

    document.getElementById("searchStatusBadge").innerText = "Found";
    document.getElementById("searchStatusBadge").className = "badge badge-green";

    document.getElementById("contractStatusText").innerText = approvalState.contractStatus;
    document.getElementById("contractStatusBadge").innerText = approvalState.contractStatus;
    document.getElementById("contractStatusBadge").className = "badge badge-yellow";

    document.getElementById("approvalPhaseText").innerText = "Not Active";
    document.getElementById("approvalPhaseBadge").innerText = "Not In Approval";
    document.getElementById("approvalPhaseBadge").className = "badge badge-yellow";

    document.getElementById("currentStageText").innerText = "None";
    document.getElementById("currentStageBadge").innerText = "No Stage";
    document.getElementById("currentStageBadge").className = "badge badge-gray";

    document.getElementById("pendingTaskCount").innerText = "0";
    document.getElementById("taskStatusBadge").innerText = "No Tasks";
    document.getElementById("taskStatusBadge").className = "badge badge-gray";

    document.getElementById("workflowSubtitle").innerText =
        "Contract " + approvalState.contractId + " is not currently in approval workflow.";

    document.getElementById("workflowOverallBadge").innerText = "Not In Approval";
    document.getElementById("workflowOverallBadge").className = "badge badge-yellow";

    document.getElementById("notApprovalMessage").innerText =
        result.message || "Current contract is not in Approval phase.";

    document.getElementById("emptyState").style.display = "none";
    document.getElementById("approvalLayout").style.display = "none";
    document.getElementById("notApprovalState").style.display = "flex";

    showToast("Contract is not in approval phase");
    addAiMessage(buildAiAnswer("Current contract is not in Approval phase. Approval stages will be visible only after the contract is submitted for approval."));
}

function updateTopTilesForSubmittedWorkflow() {
    const pendingTasks = approvalState.tasks.filter(function (task) {
        return task.status === "PENDING" || task.status === "IN_PROGRESS";
    });

    document.getElementById("searchStatusBadge").innerText = "Found";
    document.getElementById("searchStatusBadge").className = "badge badge-green";

    document.getElementById("contractStatusText").innerText = approvalState.contractStatus;
    document.getElementById("contractStatusBadge").innerText = approvalState.contractStatus;
    document.getElementById("contractStatusBadge").className = getContractStatusBadgeClass(approvalState.contractStatus);

    document.getElementById("approvalPhaseText").innerText = "Active";
    document.getElementById("approvalPhaseBadge").innerText = "In Approval";
    document.getElementById("approvalPhaseBadge").className = "badge badge-purple";

    document.getElementById("currentStageText").innerText = approvalState.currentStage || "Not Available";
    document.getElementById("currentStageBadge").innerText = approvalState.currentStage || "Current Stage";
    document.getElementById("currentStageBadge").className = "badge badge-blue";

    document.getElementById("pendingTaskCount").innerText = String(pendingTasks.length);

    if (pendingTasks.length > 0) {
        document.getElementById("taskStatusBadge").innerText = "Pending";
        document.getElementById("taskStatusBadge").className = "badge badge-yellow";
    } else {
        document.getElementById("taskStatusBadge").innerText = "No Pending Tasks";
        document.getElementById("taskStatusBadge").className = "badge badge-green";
    }
}

function renderWorkflowStages() {
    const stageList = document.getElementById("stageList");
    stageList.innerHTML = "";

    approvalState.stages.forEach(function (stage, index) {
        const stageCard = document.createElement("div");
        stageCard.className = "stage-card " + getStageClass(stage.status);

        stageCard.innerHTML = `
                <div class="stage-number">${index + 1}</div>
                <div class="stage-main">
                    <div class="stage-top">
                        <div>
                            <div class="stage-name">${escapeHtml(stage.name)}</div>
                            <div class="stage-owner">${escapeHtml(stage.ownerGroup || "Approver Group")} | ${escapeHtml(stage.approver || "Assigned Approver")}</div>
                        </div>
                        <span class="${getStatusBadgeClass(stage.status)}">${formatStatus(stage.status)}</span>
                    </div>

                    <div class="stage-description">
                        ${escapeHtml(stage.description || "Approval stage configured for this contract.")}
                    </div>

                    <div class="stage-actions">
                        <button class="secondary-btn" onclick="openStageDetails('${escapeJs(stage.name)}')">View Details</button>
                        <button class="primary-btn" onclick="openDecisionModal('${escapeJs(stage.name)}', 'APPROVED')" ${stage.status === "IN_PROGRESS" ? "" : "disabled"}>Approve</button>
                        <button class="danger-btn" onclick="openDecisionModal('${escapeJs(stage.name)}', 'REJECTED')" ${stage.status === "IN_PROGRESS" ? "" : "disabled"}>Reject</button>
                    </div>
                </div>
            `;

        stageList.appendChild(stageCard);
    });
}

function renderTaskTable() {
    const taskTableBody = document.getElementById("taskTableBody");
    taskTableBody.innerHTML = "";

    if (approvalState.tasks.length === 0) {
        taskTableBody.innerHTML = `
                <tr>
                    <td colspan="3">No approval tasks available.</td>
                </tr>
            `;
        return;
    }

    approvalState.tasks.forEach(function (task) {
        const row = document.createElement("tr");

        row.innerHTML = `
                <td>
                    <strong>${escapeHtml(task.approver || "Approver")}</strong>
                    <br>
                    <span style="font-size: 12px; color: #64748b;">${escapeHtml(task.role || "Role not specified")}</span>
                </td>
                <td>${escapeHtml(task.stage || "Stage")}</td>
                <td><span class="${getStatusBadgeClass(task.status)}">${formatStatus(task.status)}</span></td>
            `;

        taskTableBody.appendChild(row);
    });
}

function renderHistory() {
    const historyList = document.getElementById("historyList");
    historyList.innerHTML = "";

    if (approvalState.history.length === 0) {
        historyList.innerHTML = `
                <div class="timeline-item">
                    <div class="timeline-icon">i</div>
                    <div class="timeline-content">
                        <strong>No workflow history yet</strong>
                        <span>Approval decisions will be shown here.</span>
                    </div>
                </div>
            `;
        return;
    }

    approvalState.history.forEach(function (item) {
        const historyItem = document.createElement("div");
        historyItem.className = "timeline-item";

        historyItem.innerHTML = `
                <div class="timeline-icon">${getHistoryIcon(item.action)}</div>
                <div class="timeline-content">
                    <strong>${escapeHtml(item.action || "Workflow Event")}</strong>
                    <span>
                        ${escapeHtml(item.message || "")}
                        <br>
                        ${escapeHtml(item.performedBy || "System")} | ${escapeHtml(item.performedAt || "")}
                    </span>
                </div>
            `;

        historyList.appendChild(historyItem);
    });
}

function openStageDetails(stageName) {
    addAiMessage(buildAiAnswer("Stage details: " + escapeHtml(stageName) + ". In backend, open the stage task details, approver group, decision history and escalation rules."));
    showToast("Stage details shown in assistant");
}

function openDecisionModal(stageName, decision) {
    pendingDecision = {
        stageName: stageName,
        decision: decision
    };

    document.getElementById("decisionModalTitle").innerText =
        decision === "APPROVED" ? "Approve Stage" : "Reject Stage";

    document.getElementById("decisionModalSubtitle").innerText =
        stageName + " will be marked as " + formatStatus(decision) + ".";

    document.getElementById("decisionComment").value = "";
    document.getElementById("decisionModalBackdrop").style.display = "flex";
}

function closeDecisionModal() {
    pendingDecision = null;
    document.getElementById("decisionModalBackdrop").style.display = "none";
}

async function submitDecision() {
    if (!pendingDecision) {
        return;
    }

    const comment = document.getElementById("decisionComment").value.trim();

    try {
        const response = await fetch(
            API_BASE_URL + "/approval-workflows/" + approvalState.workflowId + "/stages/decision",
            {
                method: "POST",
                headers: buildAuthHeaders({
                    "Content-Type": "application/json"
                }),
                body: JSON.stringify({
                    contractId: approvalState.contractId,
                    stageName: pendingDecision.stageName,
                    decision: pendingDecision.decision,
                    comment: comment || "No comment provided"
                })
            }
        );

        if (!response.ok) {
            throw new Error("Decision API failed with status " + response.status);
        }

        showToast("Decision saved");

    } catch (error) {
        console.error(error);
        showToast("Decision simulated for UI testing");
    }

    addAiMessage(buildAiAnswer(
        pendingDecision.stageName + " marked as " + formatStatus(pendingDecision.decision) +
        ". Comment: " + escapeHtml(comment || "No comment provided")
    ));

    closeDecisionModal();
}

function previewSearchPayload() {
    const details = {
        endpoint: "/api/contracts/{contractId}/approval-workflow",
        method: "GET",
        auth: "Authorization: Bearer <keycloak-access-token>",
        contractId: getContractId(),
        responseWhenInApprovalPhase: {
            contractId: Number(getContractId() || 84),
            contractStatus: "SUBMITTED_FOR_APPROVAL",
            approvalPhase: true,
            workflowId: 301,
            currentStage: "Finance Review",
            stages: [
                {
                    name: "Manager Review",
                    status: "COMPLETED",
                    ownerGroup: "Bid Management",
                    approver: "Bid Manager"
                },
                {
                    name: "Finance Review",
                    status: "IN_PROGRESS",
                    ownerGroup: "Finance",
                    approver: "Finance Approver"
                }
            ],
            tasks: [
                {
                    approver: "Finance Approver",
                    stage: "Finance Review",
                    status: "PENDING"
                }
            ]
        },
        responseWhenNotInApprovalPhase: {
            contractId: Number(getContractId() || 84),
            contractStatus: "BID_UPLOADED",
            approvalPhase: false,
            message: "Current contract is not in Approval phase."
        }
    };

    openPayloadModal(
        "Approval Workflow Search API",
        "Use this endpoint to determine whether contract is in approval phase.",
        JSON.stringify(details, null, 4)
    );
}

function resetWorkflow() {
    approvalState = {
        searched: false,
        contractId: null,
        contractStatus: null,
        approvalPhase: false,
        currentStage: null,
        workflowId: null,
        stages: [],
        tasks: [],
        history: []
    };

    resetWorkflowViewOnly();

    document.getElementById("searchStatusBadge").innerText = "Search Required";
    document.getElementById("searchStatusBadge").className = "badge badge-blue";

    document.getElementById("contractStatusText").innerText = "Unknown";
    document.getElementById("contractStatusBadge").innerText = "Not Checked";
    document.getElementById("contractStatusBadge").className = "badge badge-gray";

    document.getElementById("approvalPhaseText").innerText = "Pending";
    document.getElementById("approvalPhaseBadge").innerText = "Not Checked";
    document.getElementById("approvalPhaseBadge").className = "badge badge-gray";

    document.getElementById("currentStageText").innerText = "None";
    document.getElementById("currentStageBadge").innerText = "Waiting";
    document.getElementById("currentStageBadge").className = "badge badge-gray";

    document.getElementById("pendingTaskCount").innerText = "0";
    document.getElementById("taskStatusBadge").innerText = "No Tasks";
    document.getElementById("taskStatusBadge").className = "badge badge-gray";

    document.getElementById("workflowSubtitle").innerText =
        "Search a contract to view approval workflow. Stages will appear only when the contract is in approval phase.";

    document.getElementById("workflowOverallBadge").innerText = "Not Checked";
    document.getElementById("workflowOverallBadge").className = "badge badge-gray";

    showToast("Workflow reset");
}

function resetWorkflowViewOnly() {
    document.getElementById("emptyState").style.display = "flex";
    document.getElementById("notApprovalState").style.display = "none";
    document.getElementById("approvalLayout").style.display = "none";
    document.getElementById("stageList").innerHTML = "";
    document.getElementById("taskTableBody").innerHTML = "";
    document.getElementById("historyList").innerHTML = "";
}

function buildMockSubmittedWorkflow(contractId) {
    return {
        contractId: Number(contractId),
        contractStatus: "SUBMITTED_FOR_APPROVAL",
        approvalPhase: true,
        workflowId: 301,
        currentStage: "Finance Review",
        stages: [
            {
                name: "Manager Review",
                status: "COMPLETED",
                ownerGroup: "Bid Management",
                approver: "Bid Manager",
                description: "Initial review of bid completeness, selected lots and supporting documents."
            },
            {
                name: "Finance Review",
                status: "IN_PROGRESS",
                ownerGroup: "Finance",
                approver: "Finance Approver",
                description: "Review commercial value, pricing assumptions, margin, payment terms and financial exposure."
            },
            {
                name: "Legal Review",
                status: "PENDING",
                ownerGroup: "Legal",
                approver: "Legal Approver",
                description: "Review contractual risks, penalties, liability clauses and compliance language."
            },
            {
                name: "Final Approval",
                status: "PENDING",
                ownerGroup: "Leadership",
                approver: "Business Owner",
                description: "Final approval before bid submission to hospital procurement authority."
            }
        ],
        tasks: [
            {
                approver: "Bid Manager",
                role: "Bid Management",
                stage: "Manager Review",
                status: "APPROVED"
            },
            {
                approver: "Finance Approver",
                role: "Finance",
                stage: "Finance Review",
                status: "PENDING"
            },
            {
                approver: "Legal Approver",
                role: "Legal",
                stage: "Legal Review",
                status: "WAITING"
            },
            {
                approver: "Business Owner",
                role: "Leadership",
                stage: "Final Approval",
                status: "WAITING"
            }
        ],
        history: [
            {
                action: "Submitted For Approval",
                message: "Prepared bid document submitted to approval workflow.",
                performedBy: "Ankit Manchanda",
                performedAt: "Today"
            },
            {
                action: "Manager Review Approved",
                message: "Bid completeness and selected lots were approved.",
                performedBy: "Bid Manager",
                performedAt: "Today"
            }
        ]
    };
}

function getContractStatusBadgeClass(status) {
    if (status === "SUBMITTED_FOR_APPROVAL") {
        return "badge badge-purple";
    }

    if (status === "APPROVED") {
        return "badge badge-green";
    }

    if (status === "REJECTED") {
        return "badge badge-red";
    }

    if (status === "BID_UPLOADED" || status === "DOCUMENT_GENERATED") {
        return "badge badge-yellow";
    }

    return "badge badge-gray";
}

function getStageClass(status) {
    if (status === "COMPLETED" || status === "APPROVED") {
        return "completed";
    }

    if (status === "IN_PROGRESS" || status === "PENDING") {
        return "active";
    }

    if (status === "REJECTED") {
        return "rejected";
    }

    return "";
}

function getStatusBadgeClass(status) {
    if (status === "COMPLETED" || status === "APPROVED") {
        return "badge badge-green";
    }

    if (status === "IN_PROGRESS" || status === "PENDING") {
        return "badge badge-yellow";
    }

    if (status === "REJECTED") {
        return "badge badge-red";
    }

    if (status === "WAITING") {
        return "badge badge-gray";
    }

    return "badge badge-gray";
}

function formatStatus(status) {
    if (!status) {
        return "Unknown";
    }

    return String(status)
        .replaceAll("_", " ")
        .toLowerCase()
        .replace(/\b\w/g, function (char) {
            return char.toUpperCase();
        });
}

function getHistoryIcon(action) {
    if (!action) {
        return "i";
    }

    const normalized = String(action).toLowerCase();

    if (normalized.includes("approved")) {
        return "✓";
    }

    if (normalized.includes("rejected")) {
        return "!";
    }

    if (normalized.includes("submitted")) {
        return "→";
    }

    return "i";
}

function openPayloadModal(title, subtitle, content) {
    document.getElementById("payloadModalTitle").innerText = title;
    document.getElementById("payloadModalSubtitle").innerText = subtitle;
    document.getElementById("payloadModalContent").innerText = content;
    document.getElementById("payloadModalBackdrop").style.display = "flex";
}

function closePayloadModal() {
    document.getElementById("payloadModalBackdrop").style.display = "none";
}

function setSearchLoading(isLoading) {
    const searchBtn = document.getElementById("searchBtn");

    if (isLoading) {
        searchBtn.disabled = true;
        searchBtn.innerHTML = '<span class="loading"><span class="spinner"></span>Searching</span>';
    } else {
        searchBtn.disabled = false;
        searchBtn.innerText = "Search";
    }
}

function askSuggestedQuestion(question) {
    document.getElementById("userQuestion").value = question;
    sendQuestion();
}

function sendQuestion() {
    const input = document.getElementById("userQuestion");
    const question = input.value.trim();

    if (!question) {
        return;
    }

    addUserMessage(question);

    const normalizedQuestion = question.toLowerCase();

    setTimeout(function () {
        if (normalizedQuestion.includes("first")) {
            addAiMessage(buildAiAnswer("First enter Contract ID and click Search. Stages are shown only if the contract is submitted for approval."));
        } else if (normalizedQuestion.includes("not visible") || normalizedQuestion.includes("why")) {
            addAiMessage(buildAiAnswer("Approval stages are hidden when approvalPhase is false. Backend should return approvalPhase: true only for statuses like SUBMITTED_FOR_APPROVAL or APPROVAL_IN_PROGRESS."));
        } else if (normalizedQuestion.includes("pending") || normalizedQuestion.includes("who")) {
            addAiMessage(buildPendingApproverAnswer());
        } else {
            addAiMessage(buildAiAnswer("This page follows search-first logic: search contract approval status, show stages if submitted, otherwise show that the contract is not in approval phase."));
        }
    }, 250);

    input.value = "";
}

function buildPendingApproverAnswer() {
    if (!approvalState.approvalPhase) {
        return buildAiAnswer("No pending approver is available because the current contract is not in approval phase.");
    }

    const pendingTasks = approvalState.tasks.filter(function (task) {
        return task.status === "PENDING" || task.status === "IN_PROGRESS";
    });

    if (pendingTasks.length === 0) {
        return buildAiAnswer("There are no pending approval tasks.");
    }

    let message = "Pending approval tasks: ";

    pendingTasks.forEach(function (task, index) {
        message += (index + 1) + ". " + task.approver + " for " + task.stage + ". ";
    });

    return buildAiAnswer(message);
}

function addUserMessage(message) {
    const chatMessages = document.getElementById("chatMessages");

    const messageDiv = document.createElement("div");
    messageDiv.className = "message user-message";
    messageDiv.innerText = message;

    chatMessages.appendChild(messageDiv);
    scrollChatToBottom();
}

function addAiMessage(messageHtml) {
    const chatMessages = document.getElementById("chatMessages");

    const messageDiv = document.createElement("div");
    messageDiv.className = "message ai-message";
    messageDiv.innerHTML = messageHtml;

    chatMessages.appendChild(messageDiv);
    scrollChatToBottom();
}

function buildAiAnswer(message) {
    return `
            <div class="answer-section">
                <strong>AI Approval Assistant</strong>
                ${message}
            </div>
        `;
}

function scrollChatToBottom() {
    const chatMessages = document.getElementById("chatMessages");
    chatMessages.scrollTop = chatMessages.scrollHeight;
}

function handleEnter(event) {
    if (event.key === "Enter") {
        sendQuestion();
    }
}

function showToast(message) {
    const toast = document.getElementById("toast");
    toast.innerText = message;
    toast.style.display = "block";

    setTimeout(function () {
        toast.style.display = "none";
    }, 2500);
}

function escapeHtml(value) {
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

function escapeJs(value) {
    return String(value)
        .replaceAll("\\", "\\\\")
        .replaceAll("'", "\\'")
        .replaceAll('"', '\\"');
}

function navigateTo(url) {
    window.location.href = url;
}

resetWorkflow();