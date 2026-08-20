package com.evatech.bidplatform.dashboard.dto.review.request;

import com.evatech.bidplatform.dashboard.dto.review.ReviewStatus;

public record ReviewDecisionRequest(

        ReviewStatus decision,
        String comment
) {
}