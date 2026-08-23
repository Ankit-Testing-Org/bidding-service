'use strict';
const API_BASE_URL = "http://localhost:8080/api";

let generatedDocumentUrl = "";
let generatedPreviewUrl = "";
let generatedDocumentName = "";
let generatedDocumentId = null;
let uploadedDocumentUrl = "";
let selectedBidFile = null;
let uploadedDocumentId = null;

let bidState = {
    searched: false,
    generated: false,
    existingDocumentFound: false,
    downloaded: false,
    uploaded: false,
    approvalReady: false
};

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

function getGenerationPayload() {
    return {
        outputFormat: document.getElementById("outputFormat").value,
        generationNotes: document.getElementById("generationNotes").value.trim(),
        includeQualifiedLotsOnly: true,
        includeContractSummary: true,
        includeComplianceRequirements: true,
        includeCommercialOffer: true,
        includeMandatoryDocuments: true
    };
}

async function searchBidDocument() {
    const contractId = getContractId();

    if (!contractId) {
        showToast("Contract ID is required");
        return;
    }

    setSearchLoading(true);
    resetGeneratedDocumentOnly();

    try {
        const response = await fetch(
            API_BASE_URL +
            "/bid/contracts/" +
            contractId +
            "/documents/latest",
            {
                method: "GET",
                headers: buildAuthHeaders()
            }
        );

        const result = await readApiResponse(response);
        const data = result.data;

        if (data.documentExists && data.document) {
            const document = data.document;

            generatedDocumentId = document.documentId;
            generatedDocumentName = document.fileName;
            generatedDocumentUrl = toAbsoluteApiUrl(document.documentUrl);
            generatedPreviewUrl = toAbsoluteApiUrl(
                document.previewUrl || document.documentUrl
            );

            applyExistingDocument(
                generatedDocumentName,
                generatedDocumentUrl,
                generatedPreviewUrl
            );

            return;
        }

        applyDocumentNotFound();

    } catch (error) {
        console.error(error);
        showToast(error.message);
        applySearchError();
    } finally {
        setSearchLoading(false);
    }
}

function applySearchError() {
    bidState.searched = false;
    bidState.generated = false;
    bidState.existingDocumentFound = false;

    updateSearchTile("Search Failed", "badge badge-red");

    updateDocumentTile(
        "Not Checked",
        "badge badge-gray",
        "Unable to check document",
        "The search request failed. Retry before generating a document."
    );

    enableGenerateActions(false);
    enableDocumentActions(false);
    clearPreview();
    updateBidProgress();
}

function applyExistingDocument(fileName, documentUrl, previewUrl) {
    bidState.searched = true;
    bidState.generated = true;
    bidState.existingDocumentFound = true;

    updateSearchTile("Document Found", "badge badge-green");
    updateDocumentTile(
        "Generated",
        "badge badge-green",
        fileName,
        "Existing bid document found. Open or download it."
    );

    setDocumentLink(documentUrl);
    enableDocumentActions(true);
    enableGenerateActions(false);
    document.getElementById("resetGeneratedBtn").disabled = false;

    showGeneratedPreview(fileName, previewUrl || documentUrl);
    updateBidProgress();

    showToast("Existing bid document found");
    addAiMessage(buildAiAnswer("Bid document already exists for this contract. Generation is disabled to avoid duplicate documents."));
}

function applyDocumentNotFound() {
    bidState.searched = true;
    bidState.generated = false;
    bidState.existingDocumentFound = false;

    updateSearchTile("Not Generated", "badge badge-yellow");
    updateDocumentTile(
        "Not Generated",
        "badge badge-yellow",
        "No bid document generated yet",
        "No generated bid document was found for this contract. You can now generate it."
    );

    hideDocumentLink();
    enableDocumentActions(false);
    enableGenerateActions(true);
    document.getElementById("resetGeneratedBtn").disabled = false;

    clearPreview();
    updateBidProgress();

    showToast("No generated bid document found");
    addAiMessage(buildAiAnswer("No generated bid document exists for this contract. You can generate a new one now."));
}

async function generateBidDocument() {
    const contractId = getContractId();

    if (!contractId) {
        showToast("Contract ID is required");
        return;
    }

    if (!bidState.searched) {
        showToast("Search bid document first");
        return;
    }

    if (bidState.existingDocumentFound) {
        showToast(
            "Bid document already exists. Download the existing document."
        );
        return;
    }

    setGenerateLoading(true);

    try {
        const response = await fetch(
            API_BASE_URL +
            "/bid/contracts/" +
            contractId +
            "/documents/generate",
            {
                method: "POST",
                headers: buildAuthHeaders({
                    "Content-Type": "application/json"
                }),
                body: JSON.stringify(getGenerationPayload())
            }
        );

        const result = await readApiResponse(response);
        const document = result.data.document;

        generatedDocumentId = document.documentId;
        generatedDocumentName = document.fileName;
        generatedDocumentUrl = toAbsoluteApiUrl(document.documentUrl);
        generatedPreviewUrl = toAbsoluteApiUrl(
            document.previewUrl || document.documentUrl
        );

        applyGeneratedDocument(
            generatedDocumentName,
            generatedDocumentUrl,
            generatedPreviewUrl
        );

    } catch (error) {
        console.error(error);
        showToast(error.message);
    } finally {
        setGenerateLoading(false);
    }
}

function applyGeneratedDocument(fileName, documentUrl, previewUrl) {
    bidState.generated = true;
    bidState.existingDocumentFound = false;

    updateSearchTile("Generated Now", "badge badge-green");
    updateDocumentTile(
        "Generated",
        "badge badge-green",
        fileName,
        "Bid document generated successfully. Download it and upload the final prepared version."
    );

    setDocumentLink(documentUrl);
    enableDocumentActions(true);
    enableGenerateActions(false);
    document.getElementById("resetGeneratedBtn").disabled = false;

    showGeneratedPreview(fileName, previewUrl || documentUrl);
    updateBidProgress();

    showToast("Bid document generated successfully");
    addAiMessage(buildAiAnswer("Bid document generated successfully. You can now download it and upload the final prepared version."));
}

function updateSearchTile(text, className) {
    const badge = document.getElementById("searchStatusBadge");
    badge.innerText = text;
    badge.className = className;
}

function updateDocumentTile(statusText, statusClass, fileName, description) {
    const status = document.getElementById("generatedDocumentStatus");
    status.innerText = statusText;
    status.className = statusClass;

    document.getElementById("generatedDocumentName").innerText = fileName;
    document.getElementById("generatedDocumentDescription").innerText = description;
}

function setDocumentLink(documentUrl) {
    const link = document.getElementById("generatedDocumentLink");
    link.href = documentUrl;
    link.innerText = documentUrl;
    link.style.display = "block";
}

function hideDocumentLink() {
    const link = document.getElementById("generatedDocumentLink");
    link.href = "#";
    link.innerText = "Generated bid document";
    link.style.display = "none";
}

function enableDocumentActions(enabled) {
    document.getElementById("openGeneratedBtn").disabled = !enabled;
    document.getElementById("downloadGeneratedBtn").disabled = !enabled;
    document.getElementById("previewOpenBtn").disabled = !enabled;
    document.getElementById("previewDownloadBtn").disabled = !enabled;
}

function enableGenerateActions(enabled) {
    document.getElementById("generateBtn").disabled = !enabled;
    document.getElementById("generateTopBtn").disabled = !enabled;
}

function showGeneratedPreview(fileName, documentUrl) {
    document.getElementById("previewTitle").innerText = fileName;

    const emptyPreview = document.getElementById("emptyPreview");
    const frame = document.getElementById("documentPreviewFrame");

    emptyPreview.style.display = "none";
    frame.style.display = "block";
    frame.src = documentUrl;
}

function clearPreview() {
    document.getElementById("previewTitle").innerText = "No generated document selected";
    document.getElementById("emptyPreview").style.display = "flex";
    document.getElementById("documentPreviewFrame").style.display = "none";
    document.getElementById("documentPreviewFrame").src = "";
}

function openGeneratedDocument() {
    if (!generatedDocumentUrl) {
        showToast("Search or generate bid document first");
        return;
    }

    bidState.downloaded = true;
    updateBidProgress();

    window.open(generatedDocumentUrl, "_blank");
    showToast("Opened generated bid document");
}

async function downloadGeneratedDocument() {
    if (!generatedDocumentUrl) {
        showToast("Search or generate bid document first");
        return;
    }

    bidState.downloaded = true;
    updateBidProgress();

    try {
        const response = await fetch(generatedDocumentUrl, {
            method: "GET",
            headers: buildAuthHeaders()
        });

        if (!response.ok) {
            throw new Error("Download failed with status " + response.status);
        }

        const blob = await response.blob();
        const blobUrl = URL.createObjectURL(blob);

        const anchor = document.createElement("a");
        anchor.href = blobUrl;
        anchor.download = generatedDocumentName || "generated-bid-document";
        document.body.appendChild(anchor);
        anchor.click();
        anchor.remove();

        URL.revokeObjectURL(blobUrl);
        showToast("Download started");

    } catch (error) {
        console.error(error);

        const anchor = document.createElement("a");
        anchor.href = generatedDocumentUrl;
        anchor.download = generatedDocumentName || "generated-bid-document";
        document.body.appendChild(anchor);
        anchor.click();
        anchor.remove();

        showToast("Download opened using browser link");
    }
}

function resetSearch() {
    generatedDocumentUrl = "";
    generatedPreviewUrl = "";
    generatedDocumentName = "";
    generatedDocumentId = null;
    uploadedDocumentUrl = "";
    selectedBidFile = null;
    uploadedDocumentId = null;

    bidState = {
        searched: false,
        generated: false,
        existingDocumentFound: false,
        downloaded: false,
        uploaded: false,
        approvalReady: false
    };

    updateSearchTile("Search Required", "badge badge-blue");
    updateDocumentTile(
        "Not Checked",
        "badge badge-gray",
        "No document selected",
        "Search bid document first."
    );

    hideDocumentLink();
    enableDocumentActions(false);
    enableGenerateActions(false);

    document.getElementById("resetGeneratedBtn").disabled = true;
    document.getElementById("uploadStatusBadge").innerText = "Waiting";
    document.getElementById("uploadStatusBadge").className = "badge badge-gray";

    clearSelectedFile();
    clearPreview();
    updateBidProgress();

    showToast("Search reset");
}

function resetGeneratedDocumentOnly() {
    generatedDocumentUrl = "";
    generatedPreviewUrl = "";
    generatedDocumentName = "";
    generatedDocumentId = null;

    hideDocumentLink();
    enableDocumentActions(false);
    clearPreview();
}

function openFilePicker() {
    if (!bidState.downloaded) {
        showToast("Download or open generated document before uploading final version");
        return;
    }

    document.getElementById("bidDocumentFile").click();
}

function handleFileSelected(event) {
    const file = event.target.files[0];

    if (!file) {
        return;
    }

    setSelectedFile(file);
}

function setSelectedFile(file) {
    const allowedTypes = [
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    ];

    const allowedExtensions = [".pdf", ".doc", ".docx"];
    const lowerFileName = file.name.toLowerCase();

    const hasAllowedExtension = allowedExtensions.some(function (extension) {
        return lowerFileName.endsWith(extension);
    });

    if (!allowedTypes.includes(file.type) && !hasAllowedExtension) {
        showToast("Only PDF, DOC and DOCX files are allowed");
        return;
    }

    const maxSizeInBytes = 25 * 1024 * 1024;

    if (file.size > maxSizeInBytes) {
        showToast("File size should not exceed 25 MB");
        return;
    }

    selectedBidFile = file;

    document.getElementById("selectedFileName").innerText = file.name;
    document.getElementById("selectedFileMeta").innerText = formatFileSize(file.size) + " | " + (file.type || "Document");
    document.getElementById("selectedFileBox").style.display = "flex";
    document.getElementById("uploadBtn").disabled = false;

    showToast("File selected");
}

function clearSelectedFile() {
    selectedBidFile = null;
    document.getElementById("bidDocumentFile").value = "";
    document.getElementById("selectedFileBox").style.display = "none";
    document.getElementById("uploadBtn").disabled = true;
}

async function uploadPreparedBidDocument() {
    if (!selectedBidFile) {
        showToast("Select prepared bid document first");
        return;
    }

    const contractId = getContractId();

    const formData = new FormData();
    formData.append("file", selectedBidFile);
    formData.append("documentType", "PREPARED_BID_DOCUMENT");

    if (generatedDocumentId !== null) {
        formData.append(
            "sourceGeneratedDocumentId",
            String(generatedDocumentId)
        );
    }

    const uploadButton = document.getElementById("uploadBtn");

    uploadButton.disabled = true;
    uploadButton.innerHTML =
        '<span class="loading">' +
        '<span class="spinner"></span>' +
        'Uploading' +
        '</span>';

    try {
        const response = await fetch(
            API_BASE_URL +
            "/bid/contracts/" +
            contractId +
            "/documents/upload-completed",
            {
                method: "POST",
                headers: buildAuthHeaders(),
                body: formData
            }
        );

        const result = await readApiResponse(response);
        const data = result.data;
        const uploadedDocument = data.uploadedDocument;

        uploadedDocumentId = uploadedDocument.documentId;
        uploadedDocumentUrl = toAbsoluteApiUrl(
            uploadedDocument.documentUrl
        );

        applyUploadedDocument(uploadedDocument.fileName);

    } catch (error) {
        console.error(error);
        showToast(error.message);
    } finally {
        uploadButton.innerText = "Upload";
        uploadButton.disabled = selectedBidFile === null;
    }
}

function applyUploadedDocument(fileName) {
    bidState.uploaded = true;

    document.getElementById("uploadStatusBadge").innerText = "Uploaded";
    document.getElementById("uploadStatusBadge").className = "badge badge-green";

    updateBidProgress();

    showToast("Prepared bid document uploaded");
    addAiMessage(buildAiAnswer("Prepared bid document uploaded successfully: " + escapeHtml(fileName) + ". The bid can now be sent for approval."));
}

async function sendForApproval() {
    if (!bidState.approvalReady) {
        showToast(
            "Complete document generation, download and upload before approval"
        );
        return;
    }

    if (!uploadedDocumentId) {
        showToast("Uploaded document ID is not available");
        return;
    }

    const contractId = getContractId();

    const request = {
        uploadedDocumentId: uploadedDocumentId,
        sourceGeneratedDocumentId: generatedDocumentId,
        comment: "Submitted from Bid Preparation Workspace"
    };

    try {
        const response = await fetch(
            API_BASE_URL +
            "/bid/contracts/" +
            contractId +
            "/submit-for-approval",
            {
                method: "POST",
                headers: buildAuthHeaders({
                    "Content-Type": "application/json"
                }),
                body: JSON.stringify(request)
            }
        );

        const result = await readApiResponse(response);
        const data = result.data;

        document.getElementById("readinessBadge").innerText =
            data.workflowStatus || "Submitted";

        document.getElementById("readinessBadge").className =
            "badge badge-purple";

        document.getElementById("approvalReadinessText").innerText =
            "Submitted";

        document.getElementById("sendApprovalBtn").disabled = true;
        document.getElementById("previewApprovalBtn").disabled = true;

        showToast("Bid sent for approval");

        addAiMessage(
            buildAiAnswer(
                "Bid has been submitted to the approval workflow."
            )
        );

    } catch (error) {
        console.error(error);
        showToast(error.message);
    }
}

function updateBidProgress() {
    bidState.approvalReady = bidState.searched && bidState.generated && bidState.downloaded && bidState.uploaded;

    setMiniStep("progressSearch", bidState.searched, !bidState.searched);
    setMiniStep("progressGenerate", bidState.generated, bidState.searched && !bidState.generated);
    setMiniStep("progressDownload", bidState.downloaded, bidState.generated && !bidState.downloaded);
    setMiniStep("progressUpload", bidState.uploaded, bidState.downloaded && !bidState.uploaded);

    setCheckBadge("checkSearched", bidState.searched, "Search Completed", "Search Pending");
    setCheckBadge("checkGenerated", bidState.generated, "Document Available", "Document Pending");
    setCheckBadge("checkDownloaded", bidState.downloaded, "Download Completed", "Download Pending");
    setCheckBadge("checkUploaded", bidState.uploaded, "Upload Completed", "Upload Pending");

    document.getElementById("sendApprovalBtn").disabled = !bidState.approvalReady;
    document.getElementById("previewApprovalBtn").disabled = !bidState.approvalReady;

    const uploadZone = document.getElementById("uploadZone");

    if (bidState.downloaded) {
        uploadZone.classList.remove("disabled");
        document.getElementById("previewUploadBtn").disabled = false;
    } else {
        uploadZone.classList.add("disabled");
        document.getElementById("previewUploadBtn").disabled = true;
    }

    if (bidState.approvalReady) {
        document.getElementById("currentStepText").innerText = "Ready";
        document.getElementById("currentStepBadge").innerText = "Ready";
        document.getElementById("currentStepBadge").className = "badge badge-green";
        document.getElementById("readinessBadge").innerText = "Ready";
        document.getElementById("readinessBadge").className = "badge badge-green";
        document.getElementById("approvalReadinessText").innerText = "Ready";
    } else if (bidState.downloaded) {
        document.getElementById("currentStepText").innerText = "Upload";
        document.getElementById("currentStepBadge").innerText = "Step 4";
        document.getElementById("currentStepBadge").className = "badge badge-purple";
        document.getElementById("approvalReadinessText").innerText = "Pending";
        document.getElementById("readinessBadge").innerText = "In Progress";
        document.getElementById("readinessBadge").className = "badge badge-yellow";
    } else if (bidState.generated) {
        document.getElementById("currentStepText").innerText = "Download";
        document.getElementById("currentStepBadge").innerText = "Step 3";
        document.getElementById("currentStepBadge").className = "badge badge-blue";
        document.getElementById("approvalReadinessText").innerText = "Pending";
        document.getElementById("readinessBadge").innerText = "In Progress";
        document.getElementById("readinessBadge").className = "badge badge-yellow";
    } else if (bidState.searched) {
        document.getElementById("currentStepText").innerText = "Generate";
        document.getElementById("currentStepBadge").innerText = "Step 2";
        document.getElementById("currentStepBadge").className = "badge badge-blue";
        document.getElementById("approvalReadinessText").innerText = "Pending";
        document.getElementById("readinessBadge").innerText = "In Progress";
        document.getElementById("readinessBadge").className = "badge badge-yellow";
    } else {
        document.getElementById("currentStepText").innerText = "Search";
        document.getElementById("currentStepBadge").innerText = "Step 1";
        document.getElementById("currentStepBadge").className = "badge badge-blue";
        document.getElementById("approvalReadinessText").innerText = "Pending";
        document.getElementById("readinessBadge").innerText = "In Progress";
        document.getElementById("readinessBadge").className = "badge badge-yellow";
    }
}

function setMiniStep(elementId, done, active) {
    const element = document.getElementById(elementId);

    element.classList.remove("done");
    element.classList.remove("active");

    if (done) {
        element.classList.add("done");
    } else if (active) {
        element.classList.add("active");
    }
}

function setCheckBadge(elementId, done, doneText, pendingText) {
    const element = document.getElementById(elementId);

    if (done) {
        element.innerText = doneText;
        element.className = "badge badge-green";
    } else {
        element.innerText = pendingText;
        element.className = "badge badge-gray";
    }
}

function previewSearchPayload() {
    const details = {
        endpoint: "/api/contracts/{contractId}/bid-documents/latest",
        method: "GET",
        auth: "Authorization: Bearer <keycloak-access-token>",
        contractId: getContractId(),
        successResponse: {
            documentId: 501,
            contractId: Number(getContractId()),
            fileName: "CGH-" + getContractId() + "-Bid-Document.docx",
            documentUrl: API_BASE_URL + "/documents/501/download",
            previewUrl: API_BASE_URL + "/documents/501/preview",
            status: "GENERATED"
        },
        notFoundResponse: {
            status: 404,
            message: "No generated bid document found for contract"
        }
    };

    openPayloadModal(
        "Search Bid Document API",
        "Use this endpoint to check whether bid document already exists.",
        JSON.stringify(details, null, 4)
    );
}

function previewGenerationPayload() {
    openPayloadModal(
        "Generate Bid Document Request",
        "Generation is allowed only after search confirms no generated document exists.",
        JSON.stringify(getGenerationPayload(), null, 4)
    );
}

function previewUploadPayload() {
    const details = {
        endpoint: "/api/contracts/{contractId}/bid-documents/upload",
        method: "POST",
        contentType: "multipart/form-data",
        auth: "Authorization: Bearer <keycloak-access-token>",
        formData: {
            file: selectedBidFile ? selectedBidFile.name : "No file selected",
            contractId: getContractId(),
            sourceGeneratedDocumentId: generatedDocumentId || null,
            documentType: "PREPARED_BID_DOCUMENT"
        }
    };

    openPayloadModal(
        "Upload Prepared Bid Document",
        "Use multipart form data for uploading final prepared bid document.",
        JSON.stringify(details, null, 4)
    );
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

function setGenerateLoading(isLoading) {
    const generateBtn = document.getElementById("generateBtn");
    const generateTopBtn = document.getElementById("generateTopBtn");

    if (isLoading) {
        generateBtn.disabled = true;
        generateTopBtn.disabled = true;
        generateBtn.innerHTML = '<span class="loading"><span class="spinner"></span>Generating</span>';
        generateTopBtn.innerHTML = '<span class="loading"><span class="spinner"></span>Generating</span>';
    } else {
        generateBtn.innerText = "Generate";
        generateTopBtn.innerText = "Generate Bid Document";

        if (bidState.searched && !bidState.generated) {
            generateBtn.disabled = false;
            generateTopBtn.disabled = false;
        }
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
            addAiMessage(buildAiAnswer("First enter Contract ID and click Search. Generation remains disabled until search confirms that no generated document exists."));
        } else if (normalizedQuestion.includes("missing")) {
            addAiMessage(buildMissingItemsAnswer());
        } else if (normalizedQuestion.includes("search")) {
            addAiMessage(buildAiAnswer("Search API should call GET /api/contracts/{contractId}/bid-documents/latest. If the document exists, show the link. If API returns 404, enable Generate."));
        } else {
            addAiMessage(buildAiAnswer("This page follows search-first logic: search existing document, show link if found, otherwise allow generation, then download and upload final version."));
        }
    }, 250);

    input.value = "";
}

function buildMissingItemsAnswer() {
    const missing = [];

    if (!bidState.searched) {
        missing.push("search bid document");
    }

    if (!bidState.generated) {
        missing.push("have a generated bid document");
    }

    if (!bidState.downloaded) {
        missing.push("open or download generated bid document");
    }

    if (!bidState.uploaded) {
        missing.push("upload prepared bid document");
    }

    if (missing.length === 0) {
        return buildAiAnswer("Nothing is missing. Bid is ready to be sent for approval.");
    }

    return buildAiAnswer("Before approval, complete these items: " + missing.join(", ") + ".");
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
                <strong>AI Bid Assistant</strong>
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

function formatFileSize(bytes) {
    if (bytes < 1024) {
        return bytes + " B";
    }

    if (bytes < 1024 * 1024) {
        return (bytes / 1024).toFixed(1) + " KB";
    }

    return (bytes / (1024 * 1024)).toFixed(1) + " MB";
}

function escapeHtml(value) {
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

function navigateTo(url) {
    window.location.href = url;
}

const uploadZone = document.getElementById("uploadZone");

uploadZone.addEventListener("dragover", function (event) {
    event.preventDefault();

    if (!bidState.downloaded) {
        return;
    }

    uploadZone.classList.add("drag-over");
});

uploadZone.addEventListener("dragleave", function () {
    uploadZone.classList.remove("drag-over");
});

uploadZone.addEventListener("drop", function (event) {
    event.preventDefault();
    uploadZone.classList.remove("drag-over");

    if (!bidState.downloaded) {
        showToast("Download or open generated document before uploading final version");
        return;
    }

    const file = event.dataTransfer.files[0];

    if (file) {
        setSelectedFile(file);
    }
});

updateBidProgress();

async function readApiResponse(response) {
    const contentType = response.headers.get("content-type") || "";

    let result = null;

    if (contentType.includes("application/json")) {
        result = await response.json();
    }

    if (!response.ok) {
        const message =
            result && result.message
                ? result.message
                : "Request failed with HTTP status " + response.status;

        throw new Error(message);
    }

    if (!result) {
        throw new Error("Backend returned an empty response");
    }

    if (result.success === false) {
        throw new Error(result.message || "Request was not successful");
    }

    return result;
}

function toAbsoluteApiUrl(url) {
    if (!url) {
        return "";
    }

    if (
        url.startsWith("http://") ||
        url.startsWith("https://") ||
        url.startsWith("blob:")
    ) {
        return url;
    }

    if (url.startsWith("/api/")) {
        return window.location.origin + url;
    }

    if (url.startsWith("/")) {
        return window.location.origin + url;
    }

    return API_BASE_URL + "/" + url;
}