package com.evatech.bidplatform.dashboard.dto.bid.response;

public record LatestBidDocumentResponse(

        Long contractId,

        boolean documentExists,

        BidDocumentDetailsResponse document,

        boolean generationAllowed,

        String message
) {
}
