package com.evatech.bidplatform.dashboard.dto.bid.response;

public record GeneratedBidDocumentResponse(

        Long contractId,

        Long bidId,

        BidDocumentDetailsResponse document,

        String generationStatus,

        String message
) {
}