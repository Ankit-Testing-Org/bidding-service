package com.evatech.bidplatform.bid.dto.request;

public record CreateBidRequest(
        Long contractId,
        String lotNumber,
        String title
) {
}