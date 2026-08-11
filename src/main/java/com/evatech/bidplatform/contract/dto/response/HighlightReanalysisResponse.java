package com.evatech.bidplatform.contract.dto.response;

import com.evatech.bidplatform.contract.entity.RiskLevel;
import com.evatech.bidplatform.contract.entity.analysis.ContractHighlightCategory;

public record HighlightReanalysisResponse(
        ContractHighlightCategory category,
        String title,
        String description,
        RiskLevel riskLevel,
        Integer severityScore,
        String recommendedAction,
        Double confidenceScore
) {
}