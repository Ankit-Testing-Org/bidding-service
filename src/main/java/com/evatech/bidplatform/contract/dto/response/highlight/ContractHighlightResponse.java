package com.evatech.bidplatform.contract.dto.response.highlight;

import com.evatech.bidplatform.contract.entity.RiskLevel;
import com.evatech.bidplatform.contract.entity.analysis.ContractHighlightCategory;

import java.time.LocalDateTime;
import java.util.List;

public record ContractHighlightResponse(
        Long id,
        ContractHighlightCategory category,
        String title,
        String description,
        Integer pageNumber,
        String reference,
        RiskLevel riskLevel,
        Integer severityScore,
        String recommendedAction,
        Boolean mandatory,
        Double confidenceScore,
        HighlightReviewStatus reviewStatus,
        String userComment,
        String reviewedBy,
        LocalDateTime reviewedAt,
        String reviewComment,
        List<ContractHighlightReviewHistoryResponse> reviewHistory
) {
}