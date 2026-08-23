package com.evatech.bidplatform.dashboard.dto.bid.response;

public record BidPreparationStatusResponse(

        Long contractId,

        Long bidId,

        boolean bidAssigned,

        boolean generatedDocumentAvailable,

        boolean completedDocumentUploaded,

        boolean readyForApproval,

        boolean submittedForApproval,

        BidDocumentDetailsResponse generatedDocument,

        BidDocumentDetailsResponse completedDocument,

        String bidStatus,

        String workflowStatus,

        String currentStep
) {
}