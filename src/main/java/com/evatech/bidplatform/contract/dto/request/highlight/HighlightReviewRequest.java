package com.evatech.bidplatform.contract.dto.request.highlight;


import com.evatech.bidplatform.contract.dto.response.highlight.HighlightReviewStatus;

public record HighlightReviewRequest(HighlightReviewStatus status, String comment){
}