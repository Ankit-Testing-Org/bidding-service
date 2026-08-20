package com.evatech.bidplatform.dashboard.dto.review.response;

import com.evatech.bidplatform.dashboard.dto.review.ReviewStatus;

import java.time.LocalDateTime;

public record ReviewDecisionResponse(

        Long reviewId,
        ReviewStatus status,
        String message,
        LocalDateTime updatedAt

) {
}