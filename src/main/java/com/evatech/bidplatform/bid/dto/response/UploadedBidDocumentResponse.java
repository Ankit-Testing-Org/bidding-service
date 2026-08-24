package com.evatech.bidplatform.bid.dto.response;

public record UploadedBidDocumentResponse(

        Long bidId,

        Long documentId,

        String uploadStatus,

        String message
) {
}