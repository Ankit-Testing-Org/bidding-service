package com.evatech.bidplatform.bid.dto.response;

public record SubmitBidForApprovalResponse(

        Long bidId,

        String status,

        String message
) {
}