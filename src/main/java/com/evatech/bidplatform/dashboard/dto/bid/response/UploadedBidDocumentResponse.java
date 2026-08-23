package com.evatech.bidplatform.dashboard.dto.bid.response;

public record UploadedBidDocumentResponse(

        Long contractId,

        Long bidId,

        Long sourceGeneratedDocumentId,

        BidDocumentDetailsResponse uploadedDocument,

        boolean readyForApproval,

        String message
) {
}