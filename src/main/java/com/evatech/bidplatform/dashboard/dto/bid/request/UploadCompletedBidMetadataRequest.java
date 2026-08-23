package com.evatech.bidplatform.dashboard.dto.bid.request;

import com.evatech.bidplatform.bid.entity.BidDocumentType;

public record UploadCompletedBidMetadataRequest(

        Long sourceGeneratedDocumentId,

        BidDocumentType documentType
) {
}