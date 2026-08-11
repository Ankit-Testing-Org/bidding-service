package com.evatech.bidplatform.contract.dto.response;

import com.evatech.bidplatform.contract.entity.analysis.LotAnalysisReviewStatus;

import java.time.LocalDateTime;

public record AnalysisReviewResponse(
        Long lotId,
        LotAnalysisReviewStatus reviewStatus,
        String reviewComment,
        String reviewedBy,
        LocalDateTime reviewedAt) {
}