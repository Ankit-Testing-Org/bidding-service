package com.evatech.bidplatform.bid.dto.response;

public record LatestBidDocumentResponse(

        Long contractId,

        boolean documentExists,

        BidDocumentDetailsResponse document,

        boolean generationAllowed,

        String message
) {
}
