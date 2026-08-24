package com.evatech.bidplatform.bid.dto.response;

public record GeneratedBidDocumentResponse(

        Long contractId,

        Long bidId,

        BidDocumentDetailsResponse document,

        String generationStatus,

        String message
) {
}