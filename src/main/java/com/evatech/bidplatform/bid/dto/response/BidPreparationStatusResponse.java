package com.evatech.bidplatform.bid.dto.response;

public record BidPreparationStatusResponse(

        Long bidId,

        String status,

        long totalFields,

        long completedFields,

        double completionPercentage,

        long documentCount
) {
}