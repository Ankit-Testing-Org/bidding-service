package com.evatech.bidplatform.contract.dto.response.highlight;

import java.time.LocalDateTime;

public record ContractHighlightReviewHistoryResponse(
        Long id,
        HighlightReviewStatus reviewStatus,
        String reviewedBy, LocalDateTime reviewedAt,
        String reviewComment
) {
}