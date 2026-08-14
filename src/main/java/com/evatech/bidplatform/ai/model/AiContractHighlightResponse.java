package com.evatech.bidplatform.ai.model;

import com.evatech.bidplatform.contract.entity.RiskLevel;
import com.evatech.bidplatform.contract.entity.analysis.ContractHighlightCategory;

public record AiContractHighlightResponse(

        ContractHighlightCategory category,

        String title,

        String description,

        Integer pageNumber,

        String reference,

        RiskLevel riskLevel,

        Integer severityScore,

        String recommendedAction,

        Boolean mandatory,

        Double confidenceScore

) {
}