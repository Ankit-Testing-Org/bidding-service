package com.evatech.bidplatform.contract.dto.request;


import com.evatech.bidplatform.contract.dto.HighlightReviewStatus;

public record HighlightReviewRequest(HighlightReviewStatus status, String comment){
}