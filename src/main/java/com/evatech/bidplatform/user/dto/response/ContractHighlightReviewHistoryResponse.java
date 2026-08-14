package com.evatech.bidplatform.user.dto.response;

import com.evatech.bidplatform.contract.dto.HighlightReviewStatus;

import java.time.LocalDateTime;

public record ContractHighlightReviewHistoryResponse(
        Long id,
        HighlightReviewStatus reviewStatus,
        String reviewedBy, LocalDateTime reviewedAt,
        String reviewComment
) {
}